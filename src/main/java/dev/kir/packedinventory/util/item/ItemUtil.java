package dev.kir.packedinventory.util.item;

import dev.kir.packedinventory.util.block.BlockUtil;
import dev.kir.packedinventory.util.block.entity.BlockEntityUtil;
import net.minecraft.item.*;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public final class ItemUtil {
    public static boolean isInventory(ItemStack stack) {
        return ItemUtil.isInventory(stack.getItem());
    }

    public static boolean isInventory(ItemConvertible itemConvertible) {
        Item item = itemConvertible.asItem();
        return BlockEntityUtil.getBlockEntityType(item).flatMap(BlockEntityUtil::getInventorySize).isPresent();
    }

    public static @Nullable DyeColor getColor(ItemStack stack) {
        return ItemUtil.getColor(stack.getItem());
    }

    public static @Nullable DyeColor getColor(ItemConvertible itemConvertible) {
        Item item = itemConvertible.asItem();
        if (item instanceof DyeItem) {
            return ((DyeItem)item).getColor();
        }

        if (item instanceof BlockItem) {
            return BlockUtil.getColor(((BlockItem)item).getBlock());
        }
        return null;
    }

    private ItemUtil() { }
}
