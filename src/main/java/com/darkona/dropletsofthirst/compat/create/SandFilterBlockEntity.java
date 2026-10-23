package com.darkona.dropletsofthirst.compat.create;

import com.darkona.dropletsofthirst.config.CompatConfig;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SandFilterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation
{
    public static final int TANK_SIZE = 1000;
    SmartFluidTankBehaviour dirtyTank;
    SmartFluidTankBehaviour purifiedTank;
    private Direction nextFacing;
    private BlockPos nextPos;
    private @Nullable BlockCapabilityCache<IFluidHandler, @Nullable Direction> nextWatch;
    private @Nullable SandFilterBlockEntity next;
    private boolean nextStale = true;
    private final SandFilterTanksView tanksView = new SandFilterTanksView(this);

    public SandFilterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        dirtyTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, TANK_SIZE, false);
        behaviours.add(dirtyTank);
        purifiedTank = SmartFluidTankBehaviour.single(this, TANK_SIZE).forbidInsertion();
        behaviours.add(purifiedTank);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CreateRegistry.SAND_FILTER_BE.get(),
                (be, side) -> {
                    if (side == null)
                        return be.tanksView;
                    Direction facing = be.getBlockState().getValue(SandFilterBlock.FACING);
                    if (side == facing)
                        return be.purifiedTank.getCapability();
                    return side == facing.getOpposite() ? be.dirtyTank.getCapability() : null;
                }
        );
    }

    public void tick()
    {
        super.tick();

        if(level.isClientSide())
            return;

        int rate = CompatConfig.SAND_FILTER_MB_PER_TICK.get();
        filter(rate);
        passOn(rate);
    }

    private void filter(int rate)
    {
        SmartFluidTank dirty = dirtyTank.getPrimaryHandler();
        SmartFluidTank purified = purifiedTank.getPrimaryHandler();
        if(dirty.isEmpty() || purified.getSpace() <= 0)
            return;

        FluidStack water = dirty.drain(rate, IFluidHandler.FluidAction.SIMULATE);
        if(water.isEmpty())
            return;

        if(water.is(FluidTags.WATER))
            WaterPurity.addPurity(water, CreateCompat.sandFilterPurity(WaterPurity.getPurity(water)));

        int accepted = purified.fill(water, IFluidHandler.FluidAction.SIMULATE);
        if(accepted <= 0)
            return;

        int drained = dirty.drain(accepted, IFluidHandler.FluidAction.EXECUTE).getAmount();
        purified.fill(water.copyWithAmount(drained), IFluidHandler.FluidAction.EXECUTE);
    }

    private void passOn(int rate)
    {
        SmartFluidTank purified = purifiedTank.getPrimaryHandler();
        if(purified.isEmpty())
            return;
        SandFilterBlockEntity ahead = nextFilter(getBlockState().getValue(SandFilterBlock.FACING));
        if(ahead == null)
            return;
        SmartFluidTank nextDirty = ahead.dirtyTank.getPrimaryHandler();
        int accepted = nextDirty.fill(purified.drain(rate, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.SIMULATE);
        if(accepted > 0)
            nextDirty.fill(purified.drain(accepted, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
    }

    private @Nullable SandFilterBlockEntity nextFilter(Direction facing)
    {
        if(facing != nextFacing)
        {
            nextFacing = facing;
            nextPos = worldPosition.relative(facing);
            nextWatch = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, (ServerLevel) level, nextPos, facing.getOpposite(), () -> !isRemoved(), () -> nextStale = true);
            nextStale = true;
        }
        if(nextStale || next != null && next.isRemoved())
        {
            nextStale = false;
            nextWatch.getCapability();
            next = level.getBlockEntity(nextPos) instanceof SandFilterBlockEntity filter ? filter : null;
        }
        return next != null && next.getBlockState().getValue(SandFilterBlock.FACING) == facing ? next : null;
    }

    public boolean hasFluid()
    {
        return !dirtyTank.isEmpty() || !purifiedTank.isEmpty();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking)
    {
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);

        tankTooltip(tooltip, mb, dirtyTank);
        tankTooltip(tooltip, mb, purifiedTank);

        if(dirtyTank.isEmpty() && purifiedTank.isEmpty()){
            CreateLang.translate("gui.goggles.fluid_container.capacity")
                    .add(CreateLang.number(dirtyTank.getPrimaryHandler().getTankCapacity(0))
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }

        return hasFluid();
    }

    private static void tankTooltip(List<Component> tooltip, LangBuilder mb, SmartFluidTankBehaviour tank) {
        if(tank.isEmpty())
            return;
        SmartFluidTank handler = tank.getPrimaryHandler();
        CreateLang.builder()
                .text(WaterPurity.enabled() ? WaterPurity.getPurityText(WaterPurity.getPurity(handler.getFluid())) + " " : "")
                .add(CreateLang.fluidName(handler.getFluid()))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.builder()
                .add(CreateLang.number(handler.getFluidAmount())
                        .add(mb)
                        .style(ChatFormatting.GOLD))
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number(handler.getCapacity())
                        .add(mb)
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
    }

    static final class SandFilterTanksView implements IFluidHandler
    {
        private final SandFilterBlockEntity filter;

        SandFilterTanksView(SandFilterBlockEntity filter)
        {
            this.filter = filter;
        }

        private IFluidHandler tank(int tank)
        {
            return (tank == 0 ? filter.dirtyTank : filter.purifiedTank).getPrimaryHandler();
        }

        @Override
        public int getTanks()
        {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank)
        {
            return tank == 0 || tank == 1 ? tank(tank).getFluidInTank(0) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank)
        {
            return tank == 0 || tank == 1 ? tank(tank).getTankCapacity(0) : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack)
        {
            return false;
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action)
        {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action)
        {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action)
        {
            return FluidStack.EMPTY;
        }
    }
}
