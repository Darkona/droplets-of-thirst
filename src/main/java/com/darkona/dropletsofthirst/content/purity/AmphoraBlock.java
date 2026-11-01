package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmphoraBlock extends BaseEntityBlock
{
    public static final MapCodec<AmphoraBlock> CODEC = simpleCodec(AmphoraBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final int BOTTLE = 250;
    private static final int SPILL_LEVEL = 1;
    private static final VoxelShape SHAPE = Shapes.or(box(5, 0, 5, 11, 1, 11), box(3, 1, 3, 13, 11, 13), box(5, 11, 5, 11, 15, 11), box(4, 15, 4, 12, 16, 12));

    public AmphoraBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new AmphoraBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
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
        if (!held.is(FluidTags.WATER) || held.getAmount() < BOTTLE)
            return false;
        ItemStack filled = stack.is(Items.GLASS_BOTTLE) ? PotionContents.createItemStack(Items.POTION, Potions.WATER) : new ItemStack(DropletsItems.TERRACOTTA_WATER_BOWL.get());
        if (WaterPurity.hasPurity(held))
            WaterPurity.addPurity(filled, WaterPurity.getPurity(held));
        tank.drain(BOTTLE, IFluidHandler.FluidAction.EXECUTE);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
    {
        boolean waterItem = holdsWaterItem(stack);
        if (!waterItem && FluidUtil.getFluidHandler(stack.copyWithCount(1)).isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora
                && !FluidUtil.interactWithFluidHandler(player, hand, amphora.tank()) && waterItem)
        {
            if (WaterPurity.isWaterFilledContainer(stack))
                pourIn(stack, amphora.tank(), player, hand);
            else
                takeOut(stack, amphora.tank(), player, hand);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit)
    {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora)
            player.displayClientMessage(contents(amphora.tank().getFluid()), true);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
    {
        if (!level.isClientSide() && !player.isCreative() && player.getMainHandItem().isEmpty() && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora)
        {
            ItemStack carried = new ItemStack(this);
            carried.applyComponents(amphora.collectComponents());
            amphora.tank().setFluid(FluidStack.EMPTY);
            player.getInventory().placeItemBackInInventory(carried);
        }
        return super.playerWillDestroy(level, pos, state, player);
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
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag)
    {
        lines.add(contents(stack.getOrDefault(DropletsDataComponents.AMPHORA_FLUID, SimpleFluidContent.EMPTY).copy()).copy().withStyle(ChatFormatting.GRAY));
    }

    private static Component contents(FluidStack fluid)
    {
        if (fluid.isEmpty())
            return Component.translatable("block.droplets_of_thirst.amphora.empty");
        Component name = fluid.getHoverName();
        if (WaterPurity.hasPurity(fluid))
            name = name.copy().append(" (").append(Component.translatable(PurityLevel.values()[WaterPurity.getPurity(fluid)].translationKey())).append(")");
        return name.copy().append(": " + fluid.getAmount() + " / " + AmphoraBlockEntity.CAPACITY + " mB");
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved)
    {
        if (level instanceof ServerLevel server && !state.is(newState.getBlock()) && !server.dimensionType().ultraWarm()
                && level.getBlockEntity(pos) instanceof AmphoraBlockEntity amphora && amphora.tank().getFluid().is(FluidTags.WATER))
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
    protected void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile)
    {
        if (!level.isClientSide() && projectile.mayInteract(level, hit.getBlockPos()))
            level.destroyBlock(hit.getBlockPos(), true, projectile);
    }
}
