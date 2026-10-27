package com.darkona.dropletsofthirst.network;

import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.index.DropletsAttachmentTypes;
import com.darkona.dropletsofthirst.content.thirst.VampireThirst;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record DrinkByHandMessage() implements CustomPacketPayload
{
    public static final DrinkByHandMessage INSTANCE = new DrinkByHandMessage();
    public static final CustomPacketPayload.Type<DrinkByHandMessage> TYPE = new Type<>(DropletsOfThirst.asResource("drinkbyhand"));
    public static final StreamCodec<ByteBuf, DrinkByHandMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void serverHandle(final DrinkByHandMessage data, final IPayloadContext context) {
        context.enqueueWork(() ->
        {
            if (!(context.player() instanceof ServerPlayer player) || !GameplayConfig.HAND_DRINKING.get() || !player.isShiftKeyDown())
                return;

            PlayerThirst thirst = player.getData(DropletsAttachmentTypes.PLAYER_THIRST);
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
                level.sendParticles(ParticleTypes.SPLASH, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 6, 0.15, 0.05, 0.15, 0.1);
            }
            PlayerThirst.drink(player, ItemStack.EMPTY, GameplayConfig.HAND_DRINKING_THIRST.get(), GameplayConfig.HAND_DRINKING_QUENCHED.get(), WaterPurity.getBlockPurity(level, pos));
        });
    }

    public static BlockHitResult pickFluid(Player player, ClipContext.Fluid fluid)
    {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, fluid, player));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
