package com.darkona.dropletsofthirst.network;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.content.thirst.VampireThirst;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DrinkByHandMessage()
{
    public static final DrinkByHandMessage INSTANCE = new DrinkByHandMessage();

    public static BlockHitResult pickFluid(Player player, ClipContext.Fluid fluid)
    {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.getBlockReach()));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, fluid, player));
    }

    public void encode(FriendlyByteBuf buffer) {}

    public static DrinkByHandMessage decode(FriendlyByteBuf buffer) {
        return INSTANCE;
    }

    public static void serverHandle(final DrinkByHandMessage data, final Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null || !GameplayConfig.HAND_DRINKING.get() || !player.isShiftKeyDown())
            return;

        PlayerThirst thirst = DropletsCapabilities.thirst(player);
        int tick = player.server.getTickCount();
        if (thirst.getThirst() >= PlayerThirst.MAX_THIRST || !thirst.canDrinkByHand(tick) || VampireThirst.isVampire(player))
            return;

        if (!player.getMainHandItem().isEmpty() || GameplayConfig.HAND_DRINKING_BOTH_HANDS.get() && !player.getOffhandItem().isEmpty())
            return;

        ServerLevel level = player.serverLevel();
        BlockHitResult hit = pickFluid(player, ClipContext.Fluid.ANY);
        BlockPos pos = hit.getBlockPos();
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(pos).is(FluidTags.WATER) || !level.mayInteract(player, pos))
            return;

        thirst.startHandDrinkCooldown(tick, GameplayConfig.HAND_DRINKING_COOLDOWN.get());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (GameplayConfig.HAND_DRINKING_EFFECTS.get())
        {
            player.swing(InteractionHand.MAIN_HAND, true);
            DropletsPackets.sendToTrackingAndSelf(player, new HandDrankMessage(player.getId()));
        }
        PlayerThirst.drink(player, ItemStack.EMPTY, GameplayConfig.HAND_DRINKING_THIRST.get(), GameplayConfig.HAND_DRINKING_HYDRATION.get(), WaterPurity.getBlockPurity(level, pos));
    }
}
