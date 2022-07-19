package dev.kir.packedinventory.util.inventory;

import dev.kir.packedinventory.inventory.ListInventory;
import dev.kir.packedinventory.util.block.entity.BlockEntityUtil;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtil {
    public static final String ITEMS_KEY = "Items";
    public static final String SLOT_KEY = "Slot";
    public static final String SIZE_KEY = "Size";
    public static final String COUNT_KEY = "Count";
    public static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";

    public static boolean hasInventory(ItemStack stack) {
        NbtCompound blockEntityTag = stack.getSubNbt(BLOCK_ENTITY_TAG_KEY);
        if (blockEntityTag == null) {
            return false;
        }

        if (blockEntityTag.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
            return blockEntityTag.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE).size() != 0;
        }

        return false;
    }

    public static DefaultedList<ItemStack> getInventory(ItemStack stack) {
        return InventoryUtil.getInventory(stack, InventoryUtil.getInventorySize(stack));
    }

    public static Inventory zip(Inventory inventory) {
        int size = inventory.size();
        DefaultedList<ItemStack> items = DefaultedList.ofSize(size);
        for (int i = 0; i < size; ++i) {
            items.add(inventory.getStack(i));
        }
        return ListInventory.wrap(InventoryUtil.zip(items));
    }

    public static DefaultedList<ItemStack> zip(DefaultedList<ItemStack> inventory) {
        DefaultedList<ItemStack> zipped = DefaultedList.ofSize(inventory.size());
        boolean[] empty = new boolean[inventory.size()];
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.get(i);
            if (empty[i] || stack.isEmpty()) {
                continue;
            }

            stack = stack.copy();
            for (int j = i + 1; j < inventory.size(); ++j) {
                ItemStack nextStack = inventory.get(j);
                if (!empty[j] && ItemStack.canCombine(stack, nextStack)) {
                    stack.setCount(stack.getCount() + nextStack.getCount());
                    empty[j] = true;
                }
            }
            zipped.add(stack);
        }
        return zipped;
    }

    public static DefaultedList<ItemStack> getInventory(ItemStack stack, int size) {
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        NbtList list = InventoryUtil.getItemsList(stack);
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                NbtCompound nbt = list.getCompound(i);
                int slot = nbt.contains(SLOT_KEY) ? (nbt.getByte(SLOT_KEY) & 255) : i;
                if (slot < inventory.size()) {
                    inventory.set(slot, ItemStack.fromNbt(nbt));
                }
            }
        }
        return inventory;
    }

    private static int getInventorySize(ItemStack stack) {
        int size = -1;
        BlockEntityType<?> blockEntityType = BlockEntityUtil.getBlockEntityType(stack.getItem()).orElse(null);
        if (blockEntityType != null) {
            size = BlockEntityUtil.getInventorySize(blockEntityType, -1);
        }

        if (size == -1) {
            int maxSlot = -1;
            int entryCount = 0;
            NbtList list = InventoryUtil.getItemsList(stack);
            if (list != null) {
                for (NbtElement entry : list) {
                    if (entry instanceof NbtCompound && ((NbtCompound)entry).contains(SLOT_KEY)) {
                        maxSlot = Math.max(maxSlot, ((NbtCompound)entry).getByte(SLOT_KEY) & 255);
                    }
                    ++entryCount;
                }
            }
            size = maxSlot == -1 ? entryCount : (maxSlot + 1);
        }

        return size;
    }

    private static @Nullable NbtList getItemsList(ItemStack stack) {
        NbtCompound nbt = BlockItem.getBlockEntityNbt(stack);
        NbtList list = nbt == null ? null : nbt.contains(ITEMS_KEY) ? nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE) : null;
        if (list != null) {
            return list;
        }

        nbt = stack.getNbt();
        return nbt == null ? null : nbt.contains(ITEMS_KEY) ? nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE) : null;
    }

    private InventoryUtil() { }
}
