package com.darkona.dropletsofthirst.compat.jade;

import com.darkona.dropletsofthirst.config.ClientConfig;
import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(DropletsOfThirst.ID)
public class JadePlugin implements IWailaPlugin
{
    @Override
    public void register(IWailaCommonRegistration registration)
    {
        registration.registerBlockDataProvider(PurityProvider.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerBlockComponent(PurityProvider.INSTANCE, Block.class);
    }

    private enum PurityProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        private static final ResourceLocation UID = DropletsOfThirst.asResource("purity");
        private static final String KEY = UID.toString();
        private static final String STORED_KEY = KEY + "_stored";

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor)
        {
            BlockEntity blockEntity = accessor.getBlockEntity();
            if (blockEntity == null || !WaterPurity.enabled())
                return;
            IFluidHandler handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if (handler != null)
                appendTanks(data, handler);
            IItemHandler items = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (items != null && !hasUngeneratedLoot(blockEntity))
                appendStored(data, items);
        }

        private static boolean hasUngeneratedLoot(BlockEntity blockEntity)
        {
            return blockEntity instanceof RandomizableContainerBlockEntity && blockEntity.saveWithoutMetadata().contains("LootTable");
        }

        private static void appendTanks(CompoundTag data, IFluidHandler handler)
        {
            IntArrayList purities = new IntArrayList();
            for (int tank = 0; tank < handler.getTanks(); tank++)
            {
                FluidStack fluid = handler.getFluidInTank(tank);
                if (!fluid.isEmpty() && (WaterPurity.hasPurity(fluid) || fluid.getFluid().is(FluidTags.WATER)))
                    purities.add(WaterPurity.getPurity(fluid));
            }
            if (!purities.isEmpty())
                data.putIntArray(KEY, purities.toIntArray());
        }

        private static void appendStored(CompoundTag data, IItemHandler items)
        {
            int[] counts = new int[WaterPurity.MAX_PURITY + 1];
            boolean found = false;
            for (int slot = 0; slot < items.getSlots(); slot++)
            {
                ItemStack stack = items.getStackInSlot(slot);
                if (!stack.isEmpty() && (WaterPurity.hasPurity(stack) || WaterPurity.isWaterFilledContainer(stack)))
                {
                    counts[WaterPurity.getPurity(stack)] += stack.getCount();
                    found = true;
                }
            }
            if (found)
                data.putIntArray(STORED_KEY, counts);
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
        {
            if (!WaterPurity.enabled())
                return;
            BlockState state = accessor.getBlockState();
            if (state.is(Blocks.WATER_CAULDRON))
                add(tooltip, CauldronPurity.at(accessor.getLevel(), accessor.getPosition()));
            else
                for (int purity : accessor.getServerData().getIntArray(KEY))
                    add(tooltip, WaterPurity.sanitizePurity(purity));
            if (ClientConfig.JADE_STORED_WATER_PURITY.get() && config.get(Identifiers.UNIVERSAL_ITEM_STORAGE) && accessor.getServerData().contains(Identifiers.UNIVERSAL_ITEM_STORAGE.toString()))
                addStored(tooltip, accessor.getServerData().getIntArray(STORED_KEY));
        }

        private static void addStored(ITooltip tooltip, int[] counts)
        {
            for (int purity = 0; purity < counts.length; purity++)
            {
                int color = WaterPurity.getPurityColor(purity);
                if (counts[purity] > 0)
                    tooltip.add(Component.literal(WaterPurity.getPurityText(purity) + " x" + counts[purity]).withStyle(style -> style.withColor(color)));
            }
        }

        private static void add(ITooltip tooltip, int purity)
        {
            tooltip.add(Component.literal(WaterPurity.getPurityText(purity)).withStyle(style -> style.withColor(WaterPurity.getPurityColor(purity))));
        }

        @Override
        public int getDefaultPriority()
        {
            return TooltipPosition.TAIL;
        }

        @Override
        public ResourceLocation getUid()
        {
            return UID;
        }
    }
}
