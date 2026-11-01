package com.darkona.dropletsofthirst.core;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DataMapType<T, V>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DropletsOfThirst.ID + "/data_maps");

    private final ResourceLocation id;
    private final ResourceKey<? extends Registry<T>> registry;
    private final Codec<V> codec;
    private final ResourceLocation file;
    private final boolean optionalIds;

    private volatile List<RawEntry<V>> pending = List.of();
    private volatile Map<ResourceKey<T>, V> values = Map.of();

    private DataMapType(ResourceLocation id, ResourceKey<? extends Registry<T>> registry, Codec<V> codec, boolean optionalIds)
    {
        this.optionalIds = optionalIds;
        this.id = id;
        this.registry = registry;
        this.codec = codec;
        String registryPath = registry.location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
                ? registry.location().getPath() : registry.location().getNamespace() + "/" + registry.location().getPath();
        this.file = new ResourceLocation(id.getNamespace(), "data_maps/" + registryPath + "/" + id.getPath() + ".json");
    }

    public static <T, V> DataMapType<T, V> create(ResourceLocation id, ResourceKey<? extends Registry<T>> registry, Codec<V> codec)
    {
        return new DataMapType<>(id, registry, codec, false);
    }

    public static <T, V> DataMapType<T, V> createOptional(ResourceLocation id, ResourceKey<? extends Registry<T>> registry, Codec<V> codec)
    {
        return new DataMapType<>(id, registry, codec, true);
    }

    public ResourceLocation id()
    {
        return id;
    }

    public ResourceKey<? extends Registry<T>> registryKey()
    {
        return registry;
    }

    public Codec<V> codec()
    {
        return codec;
    }

    public Map<ResourceKey<T>, V> getDataMap()
    {
        return values;
    }

    public @Nullable V get(ResourceKey<T> key)
    {
        return values.get(key);
    }

    public @Nullable V get(Holder<T> holder)
    {
        if (holder instanceof Holder.Reference<T> reference)
            return values.get(reference.key());
        return null;
    }

    public void set(Map<ResourceKey<T>, V> resolved)
    {
        values = Map.copyOf(resolved);
    }

    public SimplePreparableReloadListener<List<RawEntry<V>>> reloadListener(ICondition.IContext conditions)
    {
        return new SimplePreparableReloadListener<>()
        {
            @Override
            protected List<RawEntry<V>> prepare(ResourceManager manager, ProfilerFiller profiler)
            {
                return read(manager, conditions);
            }

            @Override
            protected void apply(List<RawEntry<V>> entries, ResourceManager manager, ProfilerFiller profiler)
            {
                pending = entries;
            }
        };
    }

    public void bind(RegistryAccess access)
    {
        Optional<? extends Registry<T>> lookup = access.registry(registry);
        if (lookup.isEmpty())
            return;
        Registry<T> reg = lookup.get();
        Map<ResourceKey<T>, V> resolved = new HashMap<>();
        for (RawEntry<V> entry : pending)
        {
            if (entry.clear)
            {
                resolved.clear();
                continue;
            }
            if (entry.tag)
            {
                Optional<HolderSet.Named<T>> set = reg.getTag(TagKey.create(registry, entry.target));
                if (set.isEmpty())
                    continue;
                for (Holder<T> holder : set.get())
                    holder.unwrapKey().ifPresent(key -> apply(resolved, key, entry.value));
            }
            else
            {
                ResourceKey<T> key = ResourceKey.create(registry, entry.target);
                if (!reg.containsKey(key))
                {
                    if (entry.value != null && !optionalIds)
                        LOGGER.error("Data map {}: unknown id {} (add a mod_loaded condition for entries of optional mods)", id, entry.target);
                    continue;
                }
                apply(resolved, key, entry.value);
            }
        }
        values = Map.copyOf(resolved);
    }

    private void apply(Map<ResourceKey<T>, V> resolved, ResourceKey<T> key, @Nullable V value)
    {
        if (value == null)
            resolved.remove(key);
        else
            resolved.put(key, value);
    }

    private List<RawEntry<V>> read(ResourceManager manager, ICondition.IContext conditions)
    {
        List<RawEntry<V>> entries = new ArrayList<>();
        for (Resource resource : manager.getResourceStack(file))
        {
            try (Reader reader = resource.openAsReader())
            {
                JsonObject json = GsonHelper.convertToJsonObject(JsonParser.parseReader(reader), "data map");
                if (GsonHelper.getAsBoolean(json, "replace", false))
                    entries.add(RawEntry.clearAll());
                JsonObject valuesJson = GsonHelper.getAsJsonObject(json, "values", new JsonObject());
                for (Map.Entry<String, JsonElement> value : valuesJson.entrySet())
                    readValue(entries, value.getKey(), value.getValue(), conditions, resource.sourcePackId());
                if (json.has("remove"))
                    for (JsonElement removed : GsonHelper.getAsJsonArray(json, "remove"))
                        entries.add(target(removed.getAsString(), null));
            }
            catch (Exception e)
            {
                LOGGER.error("Couldn't read data map {} from pack {}", file, resource.sourcePackId(), e);
            }
        }
        return entries;
    }

    private void readValue(List<RawEntry<V>> entries, String key, JsonElement element, ICondition.IContext conditions, String pack)
    {
        JsonElement value = element;
        if (element.isJsonObject())
        {
            JsonObject object = element.getAsJsonObject().deepCopy();
            if (!conditionsHold(object, conditions))
                return;
            if (object.has("value"))
            {
                if (GsonHelper.getAsBoolean(object, "replace", false))
                    entries.add(target(key, null));
                value = object.get("value");
            }
            else
                value = object;
        }
        Optional<V> decoded = codec.parse(JsonOps.INSTANCE, value).resultOrPartial(error -> LOGGER.error("Data map {} in pack {}: invalid value for {}: {}", id, pack, key, error));
        decoded.ifPresent(v -> entries.add(target(key, v)));
    }

    private static boolean conditionsHold(JsonObject object, ICondition.IContext context)
    {
        for (String member : new String[]{"neoforge:conditions", "forge:conditions"})
        {
            if (!object.has(member))
                continue;
            JsonArray conditions = GsonHelper.getAsJsonArray(object, member);
            object.remove(member);
            for (JsonElement condition : conditions)
            {
                JsonObject json = forgeCondition(condition.getAsJsonObject());
                if (!CraftingHelper.getCondition(json).test(context))
                    return false;
            }
        }
        return true;
    }

    private static JsonObject forgeCondition(JsonObject condition)
    {
        JsonObject copy = condition.deepCopy();
        String type = GsonHelper.getAsString(copy, "type", "");
        if (type.startsWith("neoforge:"))
            copy.addProperty("type", "forge:" + type.substring("neoforge:".length()));
        if (copy.has("value") && copy.get("value").isJsonObject())
            copy.add("value", forgeCondition(copy.getAsJsonObject("value")));
        if (copy.has("values") && copy.get("values").isJsonArray())
        {
            JsonArray values = new JsonArray();
            for (JsonElement nested : copy.getAsJsonArray("values"))
                values.add(forgeCondition(nested.getAsJsonObject()));
            copy.add("values", values);
        }
        return copy;
    }

    private static <V> RawEntry<V> target(String key, @Nullable V value)
    {
        boolean tag = key.startsWith("#");
        return new RawEntry<>(new ResourceLocation(tag ? key.substring(1) : key), tag, value, false);
    }

    public record RawEntry<V>(ResourceLocation target, boolean tag, @Nullable V value, boolean clear)
    {
        static <V> RawEntry<V> clearAll()
        {
            return new RawEntry<>(new ResourceLocation("clear"), false, null, true);
        }
    }
}
