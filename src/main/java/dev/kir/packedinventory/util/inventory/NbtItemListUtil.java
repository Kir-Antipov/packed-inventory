package dev.kir.packedinventory.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

public final class NbtItemListUtil {
    private static final Comparator<NbtElement> SLOT_COMPARATOR = Comparator.comparingInt(x -> x instanceof NbtCompound ? ((NbtCompound)x).getByte(InventoryUtil.SLOT_KEY) : 0);

    @SuppressWarnings("unchecked")
    public static void clean(NbtList list) {
        if (list.getHeldType() != NbtElement.COMPOUND_TYPE) {
            return;
        }

        Iterator<NbtCompound> iterator = (Iterator<NbtCompound>)(Object)list.iterator();
        while (iterator.hasNext()) {
            NbtCompound nbt = iterator.next();
            String id = nbt.getString("id");
            if ("air".equals(id) || "minecraft:air".equals(id) || nbt.getByte(InventoryUtil.COUNT_KEY) <= 0) {
                iterator.remove();
            }
        }
    }

    public static void sort(NbtList list) {
        list.sort(SLOT_COMPARATOR);
    }

    public static int binarySearch(NbtList list, int slot) {
        NbtCompound target = new NbtCompound();
        target.putInt(InventoryUtil.SLOT_KEY, slot);
        return Collections.binarySearch(list, target, SLOT_COMPARATOR);
    }

    public static ItemStack get(NbtList list, int slot) {
        int i = binarySearch(list, slot);
        return i < 0 ? ItemStack.EMPTY : asItemStack(list.getCompound(i));
    }

    public static ItemStack remove(NbtList list, int slot) {
        int i = binarySearch(list, slot);
        if (i < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = asItemStack(list.getCompound(i));
        list.remove(i);
        return stack;
    }

    public static ItemStack remove(NbtList list, int slot, int amount) {
        int i = binarySearch(list, slot);
        if (i < 0) {
            return ItemStack.EMPTY;
        }

        NbtCompound stackNbt = list.getCompound(i);
        ItemStack stack = asItemStack(stackNbt);
        ItemStack splitStack = stack.split(amount);
        if (stack.isEmpty()) {
            list.remove(i);
        } else {
            stackNbt.putByte(InventoryUtil.COUNT_KEY, (byte)stack.getCount());
        }
        return splitStack;
    }

    public static void insert(NbtList list, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            NbtItemListUtil.remove(list, slot);
            return;
        }

        NbtCompound stackNbt = asCompound(stack, slot);
        int i = binarySearch(list, slot);
        if (i >= 0) {
            list.set(i, stackNbt);
            return;
        }

        i = ~i;
        if (i >= list.size()) {
            list.add(stackNbt);
        } else {
            list.add(i, stackNbt);
        }
    }

    public static void update(NbtList list, int slot, Consumer<ItemStack> stackUpdater) {
        int i = binarySearch(list, slot);
        if (i < 0) {
            return;
        }

        ItemStack stack = asItemStack(list.getCompound(i));
        stackUpdater.accept(stack);
        list.set(i, asCompound(stack, slot));
    }

    public static NbtCompound asCompound(ItemStack stack, int slot) {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(InventoryUtil.SLOT_KEY, (byte)slot);
        stack.writeNbt(nbt);
        return nbt;
    }

    public static ItemStack asItemStack(NbtCompound nbt) {
        return ItemStack.fromNbt(nbt);
    }

    private NbtItemListUtil() { }
}
