package com.darkona.dropletsofthirst.network;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record HandDrankMessage(int playerId) implements CustomPacketPayload
{
    private static final int SPLASH_COUNT = 6;
    private static final double SPLASH_SPREAD_XZ = 0.15;
    private static final double SPLASH_SPREAD_Y = 0.05;
    private static final double SPLASH_SPEED = 0.1;

    public static final CustomPacketPayload.Type<HandDrankMessage> TYPE = new Type<>(DropletsOfThirst.asResource("hand_drank"));
    public static final StreamCodec<ByteBuf, HandDrankMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            HandDrankMessage::playerId,
            HandDrankMessage::new
    );

    public static void clientHandle(final HandDrankMessage message, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            if (level.getEntity(message.playerId) instanceof Player drinker)
                splash(level, drinker);
        });
    }

    private static void splash(Level level, Player drinker)
    {
        BlockHitResult hit = DrinkByHandMessage.pickFluid(drinker, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER))
            return;
        RandomSource random = level.getRandom();
        for (int i = 0; i < SPLASH_COUNT; i++)
            level.addParticle(ParticleTypes.SPLASH,
                    hit.getLocation().x + random.nextGaussian() * SPLASH_SPREAD_XZ,
                    hit.getLocation().y + random.nextGaussian() * SPLASH_SPREAD_Y,
                    hit.getLocation().z + random.nextGaussian() * SPLASH_SPREAD_XZ,
                    random.nextGaussian() * SPLASH_SPEED,
                    random.nextGaussian() * SPLASH_SPEED,
                    random.nextGaussian() * SPLASH_SPEED);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
