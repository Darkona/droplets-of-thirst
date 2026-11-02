package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.index.DropletsBlockEntityTypes;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

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
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components)
    {
        super.collectImplicitComponents(components);
        components.set(DropletsDataComponents.AMPHORA_FLUID, SimpleFluidContent.copyOf(tank.getFluid()));
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.@NotNull DataComponentInput input)
    {
        super.applyImplicitComponents(input);
        tank.setFluid(input.getOrDefault(DropletsDataComponents.AMPHORA_FLUID, SimpleFluidContent.EMPTY).copy());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        tank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        tank.readFromNBT(registries, tag);
    }
}
