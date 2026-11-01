package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.index.DropletsBlockEntityTypes;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class CharcoalFilterBlockEntity extends BlockEntity implements IFluidHandler
{
    public static final int MB_PER_TICK = 1;

    private static final int TANK_SIZE = 1000;
    private static final int STEP_TICKS = 20;
    private static final int STEP_MB = MB_PER_TICK * STEP_TICKS;
    private static final int CAULDRON_LEVEL_MB = 333;

    private final FluidTank input = tank(fluid -> fluid.is(FluidTags.WATER) && WaterPurity.hasPurity(fluid));
    private final FluidTank output = tank(fluid -> true);

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
        IFluidHandler source = level.getCapability(Capabilities.FluidHandler.BLOCK, above, Direction.DOWN);
        if (source == null)
            return;
        for (int tank = 0; tank < source.getTanks(); tank++)
        {
            FluidStack held = source.getFluidInTank(tank);
            if (held.isEmpty() || !input.isFluidValid(held))
                continue;
            int accepted = input.fill(held.copyWithAmount(Math.min(STEP_MB, held.getAmount())), FluidAction.SIMULATE);
            FluidStack drained = accepted > 0 ? source.drain(held.copyWithAmount(accepted), FluidAction.EXECUTE) : FluidStack.EMPTY;
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
            output.fill(water.copyWithAmount(input.drain(accepted, FluidAction.EXECUTE).getAmount()), FluidAction.EXECUTE);
    }

    private void push(Level level, BlockPos below)
    {
        IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, below, Direction.UP);
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
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action)
    {
        return input.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action)
    {
        return output.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action)
    {
        return output.drain(maxDrain, action);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.put("input", input.writeToNBT(registries, new CompoundTag()));
        tag.put("output", output.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        input.readFromNBT(registries, tag.getCompound("input"));
        output.readFromNBT(registries, tag.getCompound("output"));
    }
}
