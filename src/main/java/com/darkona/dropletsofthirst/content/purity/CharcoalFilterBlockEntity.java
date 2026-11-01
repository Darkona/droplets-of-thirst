package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.index.DropletsBlockEntityTypes;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CharcoalFilterBlockEntity extends BlockEntity implements IFluidHandler
{
    public static final int MB_PER_TICK = 1;

    private static final int TANK_SIZE = 1000;
    private static final int STEP_TICKS = 20;
    private static final int STEP_MB = MB_PER_TICK * STEP_TICKS;
    private static final int CAULDRON_LEVEL_MB = 333;

    private final FluidTank input = tank(fluid -> fluid.getFluid().is(FluidTags.WATER) && WaterPurity.hasPurity(fluid));
    private final FluidTank output = tank(fluid -> true);
    private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> this);

    public CharcoalFilterBlockEntity(BlockPos pos, BlockState state)
    {
        super(DropletsBlockEntityTypes.CHARCOAL_FILTER.get(), pos, state);
    }

    private FluidTank tank(Predicate<FluidStack> validator)
    {
        return new FluidTank(TANK_SIZE, validator)
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CharcoalFilterBlockEntity filter)
    {
        if (level.getGameTime() % STEP_TICKS != 0)
            return;
        filter.pull(level, pos.above());
        filter.purify();
        filter.push(level, pos.below());
    }

    private void pull(Level level, BlockPos above)
    {
        BlockState state = level.getBlockState(above);
        if (state.is(Blocks.WATER_CAULDRON))
        {
            FluidStack water = WaterPurity.addPurity(new FluidStack(Fluids.WATER, CAULDRON_LEVEL_MB), CauldronPurity.at(level, above));
            if (input.fill(water, FluidAction.SIMULATE) == CAULDRON_LEVEL_MB)
            {
                input.fill(water, FluidAction.EXECUTE);
                LayeredCauldronBlock.lowerFillLevel(state, level, above);
            }
            return;
        }
        IFluidHandler source = FluidUtil.getFluidHandler(level, above, Direction.DOWN).orElse(null);
        if (source == null)
            return;
        for (int tank = 0; tank < source.getTanks(); tank++)
        {
            FluidStack held = source.getFluidInTank(tank);
            if (held.isEmpty() || !input.isFluidValid(held))
                continue;
            int accepted = input.fill(new FluidStack(held, Math.min(STEP_MB, held.getAmount())), FluidAction.SIMULATE);
            FluidStack drained = accepted > 0 ? source.drain(new FluidStack(held, accepted), FluidAction.EXECUTE) : FluidStack.EMPTY;
            if (!drained.isEmpty())
            {
                input.fill(drained, FluidAction.EXECUTE);
                return;
            }
        }
    }

    private void purify()
    {
        FluidStack water = input.drain(STEP_MB, FluidAction.SIMULATE);
        if (water.isEmpty())
            return;
        WaterPurity.addPurity(water, Math.min(WaterPurity.getPurity(water) + 1, WaterPurity.MAX_PURITY));
        int accepted = output.fill(water, FluidAction.SIMULATE);
        if (accepted > 0)
            output.fill(new FluidStack(water, input.drain(accepted, FluidAction.EXECUTE).getAmount()), FluidAction.EXECUTE);
    }

    private void push(Level level, BlockPos below)
    {
        IFluidHandler target = FluidUtil.getFluidHandler(level, below, Direction.UP).orElse(null);
        if (target != null && !output.isEmpty())
            output.drain(target.fill(output.getFluid().copy(), FluidAction.EXECUTE), FluidAction.EXECUTE);
    }

    @Override
    public int getTanks()
    {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank)
    {
        return tank == 0 ? input.getFluid() : output.getFluid();
    }

    @Override
    public int getTankCapacity(int tank)
    {
        return TANK_SIZE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack)
    {
        return tank == 0 && input.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        return input.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action)
    {
        return output.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action)
    {
        return output.drain(maxDrain, action);
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
        tag.put("input", input.writeToNBT(new CompoundTag()));
        tag.put("output", output.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);
        input.readFromNBT(tag.getCompound("input"));
        output.readFromNBT(tag.getCompound("output"));
    }
}
