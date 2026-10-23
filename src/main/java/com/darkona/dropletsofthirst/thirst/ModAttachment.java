package com.darkona.dropletsofthirst.thirst;


import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.PouredWater;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachment
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DropletsOfThirst.ID);
    public static final Supplier<AttachmentType<PlayerThirst>> PLAYER_THIRST = ATTACHMENT_TYPES.register(
            "player_thirst", () -> AttachmentType.serializable(PlayerThirst::new).copyOnDeath().build()
    );
    public static final Supplier<AttachmentType<PouredWater>> POURED_WATER = ATTACHMENT_TYPES.register(
            "poured_water", () -> AttachmentType.builder(PouredWater::new).serialize(PouredWater.CODEC, water -> !water.isEmpty()).build()
    );
}
