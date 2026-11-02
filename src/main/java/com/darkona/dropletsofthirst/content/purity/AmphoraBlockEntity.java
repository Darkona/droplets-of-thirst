package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.index.DropletsBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmphoraBlockEntity extends BlockEntity
{
    public static final int CAPACITY = 4000;

    private final FluidTank tank = new FluidTank(CAPACITY)
    {
        @Override
        protected void onContentsChanged()
        {
            setChanged();
        }
    };
    private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> tank);
    private boolean carried;

    public AmphoraBlockEntity(BlockPos pos, BlockState state)
    {
        super(DropletsBlockEntityTypes.AMPHORA.get(), pos, state);
    }

    public FluidTank tank()
    {
        return tank;
    }

    public void markCarried()
    {
        carried = true;
    }

    public boolean carried()
    {
        return carried;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side)
    {
        return capability == ForgeCapabilities.FLUID_HANDLER ? handler.cast() : super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        super.saveAdditional(tag);
        tank.writeToNBT(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);
        tank.readFromNBT(tag);
    }
}
