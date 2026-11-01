package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.index.DropletsItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmphoraBlock extends BaseEntityBlock
{
    private static final int BOTTLE = 250;
    private static final int SPILL_LEVEL = 1;
    private static final VoxelShape SHAPE = Shapes.or(box(5, 0, 5, 11, 1, 11), box(3, 1, 3, 13, 11, 13), box(5, 11, 5, 11, 15, 11), box(4, 15, 4, 12, 16, 12));

    public AmphoraBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new AmphoraBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
    {
        return SHAPE;
    }

    private static boolean holdsWaterItem(ItemStack stack)
    {
        return WaterPurity.isWaterFilledContainer(stack) || stack.is(Items.GLASS_BOTTLE) || stack.is(DropletsItems.TERRACOTTA_BOWL.get());
    }

    private static boolean pourIn(ItemStack stack, FluidTank tank, Player player, InteractionHand hand)
    {
        FluidStack water = WaterPurity.addPurity(new FluidStack(Fluids.WATER, BOTTLE), WaterPurity.getPurity(stack));
        if (tank.fill(water, IFluidHandler.FluidAction.SIMULATE) < BOTTLE)
            return false;
        tank.fill(water, IFluidHandler.FluidAction.EXECUTE);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, WaterPurity.emptyContainer(stack)));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static boolean takeOut(ItemStack stack, FluidTank tank, Player player, InteractionHand hand)
    {
        FluidStack held = tank.getFluid();
        if (!held.getFluid().is(FluidTags.WATER) || held.getAmount() < BOTTLE)
            return false;
        ItemStack filled = stack.is(Items.GLASS_BOTTLE) ? PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER) : new ItemStack(DropletsItems.TERRACOTTA_WATER_BOWL.get());
        if (WaterPurity.hasPurity(held))
            WaterPurity.addPurity(filled, WaterPurity.getPurity(held));
        tank.drain(BOTTLE, IFluidHandler.FluidAction.EXECUTE);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
    {
        ItemStack stack = player.getItemInHand(hand);
        boolean waterItem = holdsWaterItem(stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora)
        {
            if (!waterItem && (stack.isEmpty() || !FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent()))
                player.displayClientMessage(contents(amphora.tank().getFluid()), true);
            else if (!FluidUtil.interactWithFluidHandler(player, hand, amphora.tank()) && waterItem)
            {
                if (WaterPurity.isWaterFilledContainer(stack))
                    pourIn(stack, amphora.tank(), player, hand);
                else
                    takeOut(stack, amphora.tank(), player, hand);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
    {
        if (!level.isClientSide() && !player.isCreative() && player.getMainHandItem().isEmpty() && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora)
        {
            ItemStack carried = new ItemStack(this);
            if (!amphora.tank().isEmpty())
                amphora.saveToItem(carried);
            amphora.tank().setFluid(FluidStack.EMPTY);
            player.getInventory().placeItemBackInInventory(carried);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack tool)
    {
        if (!tool.isEmpty())
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance)
    {
        if (!level.isClientSide() && entity instanceof FallingBlockEntity falling && falling.getBlockState().is(BlockTags.ANVIL))
            level.destroyBlock(pos, true, entity);
        else
            super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> lines, @NotNull TooltipFlag flag)
    {
        CompoundTag stored = BlockItem.getBlockEntityData(stack);
        lines.add(contents(stored == null ? FluidStack.EMPTY : FluidStack.loadFluidStackFromNBT(stored)).copy().withStyle(ChatFormatting.GRAY));
    }

    private static Component contents(FluidStack fluid)
    {
        if (fluid.isEmpty())
            return Component.translatable("block.droplets_of_thirst.amphora.empty");
        Component name = fluid.getDisplayName();
        if (WaterPurity.hasPurity(fluid))
            name = name.copy().append(" (").append(Component.translatable(PurityLevel.values()[WaterPurity.getPurity(fluid)].translationKey())).append(")");
        return name.copy().append(": " + fluid.getAmount() + " / " + AmphoraBlockEntity.CAPACITY + " mB");
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved)
    {
        if (level instanceof ServerLevel server && !state.is(newState.getBlock()) && !server.dimensionType().ultraWarm()
                && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora && amphora.tank().getFluid().getFluid().is(FluidTags.WATER))
            spill(server, pos);
        super.onRemove(state, level, pos, newState, moved);
    }

    private static void spill(ServerLevel level, BlockPos pos)
    {
        MinecraftServer server = level.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (level.isEmptyBlock(pos))
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, SPILL_LEVEL));
        }));
    }

    @Override
    public void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile)
    {
        if (!level.isClientSide() && projectile.mayInteract(level, hit.getBlockPos()))
            level.destroyBlock(hit.getBlockPos(), true, projectile);
    }
}
