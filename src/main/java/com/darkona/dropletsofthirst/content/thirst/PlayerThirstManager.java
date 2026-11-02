package com.darkona.dropletsofthirst.content.thirst;

import net.minecraft.world.entity.player.Player;
import com.darkona.dropletsofthirst.api.event.ThirstChangeEvent;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.darkona.dropletsofthirst.config.ClientConfig;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.darkona.dropletsofthirst.network.ThirstValuesSyncMessage;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import com.darkona.dropletsofthirst.network.DropletsPackets;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.stream.Stream;

@EventBusSubscriber
public class PlayerThirstManager {
    private static volatile RecipeManager reloadingRecipes;

    @SubscribeEvent
    public static void eatBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player))
            return;
        DrinkValues values = DropletsDataMaps.HYDRATING_BLOCKS.get(event.getLevel().getBlockState(event.getPos()).getBlock().builtInRegistryHolder());
        if (values == null)
            return;
        int food = player.getFoodData().getFoodLevel();
        MinecraftServer server = player.serverLevel().getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (player.getFoodData().getFoodLevel() > food)
                PlayerThirst.eat(player, ItemStack.EMPTY, values.thirst(), values.hydration());
        }));
    }

    @SubscribeEvent
    public static void drink(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        ItemStack item = event.getItem();
        if (WaterPurity.isWaterFilledContainer(item) || item.getFoodProperties(player) == null)
            PlayerThirst.consume(item, player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.START) {
            if (simulates(player) && player.isAlive())
                DropletsCapabilities.thirst(player).tick(player);
            return;
        }
        if (player instanceof ServerPlayer serverPlayer)
            DropletsCapabilities.thirst(serverPlayer).syncIfChanged(serverPlayer);
    }

    private static boolean simulates(Player player) {
        return player instanceof ServerPlayer || player.isLocalPlayer();
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerThirst thirst = DropletsCapabilities.thirst(event.getEntity());
        thirst.updateThirstData(event.getEntity());
        thirst.invalidateModifier();
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerThirst thirst = DropletsCapabilities.thirst(event.getEntity());
        if (!event.isEndConquered()) {
            int respawnThirst = GameplayConfig.RESPAWN_THIRST.get();
            int respawnHydration = GameplayConfig.RESPAWN_HYDRATION.get();
            thirst.change(event.getEntity(), respawnThirst >= 0 ? respawnThirst : thirst.getThirst(), respawnHydration >= 0 ? respawnHydration : thirst.getHydration(), ThirstChangeEvent.Cause.DEATH);
        }
        thirst.updateThirstData(event.getEntity());
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSlot().getType() == EquipmentSlot.Type.ARMOR)
            DropletsCapabilities.thirst(player).invalidateModifier();
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        effectsChanged(event);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        effectsChanged(event);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        effectsChanged(event);
    }

    private static void effectsChanged(MobEffectEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            DropletsCapabilities.thirst(player).invalidateModifier();
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && simulates(player))
            activity(player, player.isSprinting() ? GameplayConfig.SPRINT_JUMP : GameplayConfig.JUMP, 1.0F);
    }

    public static void attackLanded(ServerPlayer player) {
        activity(player, GameplayConfig.ATTACK, 1.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player)
            activity(player, GameplayConfig.BLOCK_BREAK, 1.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamaged(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0)
            activity(player, GameplayConfig.DAMAGE_MULTIPLIER, event.getSource().getFoodExhaustion());
    }

    private static void activity(Player player, ForgeConfigSpec.DoubleValue value, float factor) {
        if (player.level().isClientSide() || GameplayConfig.MODE.get() == GameplayConfig.Mode.OWN)
            DropletsCapabilities.thirst(player).addActivity(player, value.get().floatValue() * factor);
    }

    @SubscribeEvent
    public static void captureRecipes(AddReloadListenerEvent event){
        reloadingRecipes = event.getServerResources().getRecipeManager();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void rebuildDrinks(TagsUpdatedEvent event){
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            RecipeManager recipes = reloadingRecipes;
            reloadingRecipes = null;
            ThirstHelper.rebuild(recipes, event.getRegistryAccess());
        }
        else if (event.shouldUpdateStaticData() && !ThirstHelper.hasServerTables())
            ThirstHelper.rebuild(null, null);
    }

    public static void estimatedTooltip(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        if (!ClientConfig.SHOW_TOOLTIP_ICONS.get() && ThirstHelper.isEstimated(stack))
            event.getToolTip().add(estimatedLine(ThirstHelper.getThirst(stack), ThirstHelper.getHydration(stack)));
    }

    private record EstimatedLine(int thirst, int hydration, Component line) {}

    private static volatile @Nullable EstimatedLine lastEstimated;

    public static Component estimatedLine(int thirst, int hydration){
        EstimatedLine last = lastEstimated;
        if (last == null || last.thirst() != thirst || last.hydration() != hydration)
            lastEstimated = last = new EstimatedLine(thirst, hydration, Component.translatable("droplets_of_thirst.tooltip.estimated", thirst, hydration).withStyle(ChatFormatting.GRAY));
        return last.line();
    }

    @SubscribeEvent
    public static void syncValues(OnDatapackSyncEvent event){
        ServerPlayer player = event.getPlayer();
        sendValues(player != null ? Stream.of(player) : event.getPlayerList().getPlayers().stream());
    }

    private static void sendValues(Stream<ServerPlayer> players){
        ThirstValuesSyncMessage message = ThirstValuesSyncMessage.fromTables();
        players.filter(player -> !player.connection.connection.isMemoryConnection())
                .forEach(player -> DropletsPackets.sendToPlayer(player, message));
    }

    public static void onConfigReloaded(ModConfigEvent.Reloading event){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || event.getConfig().getType() == ModConfig.Type.CLIENT || !DropletsOfThirst.ID.equals(event.getConfig().getModId()))
            return;
        server.execute(() -> {
            ThirstHelper.rebuild(server.getRecipeManager(), server.registryAccess());
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                PlayerThirst thirst = DropletsCapabilities.thirst(player);
                thirst.invalidateModifier();
                thirst.updateThirstData(player);
            }
            sendValues(players.stream());
        });
    }
}
