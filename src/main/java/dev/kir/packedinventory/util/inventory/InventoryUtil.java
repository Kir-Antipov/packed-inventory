package dev.kir.packedinventory.util.inventory;

import dev.kir.packedinventory.inventory.CombinedInventory;
import dev.kir.packedinventory.inventory.ListInventory;
import dev.kir.packedinventory.screen.StackReferenceSlot;
import dev.kir.packedinventory.util.block.entity.BlockEntityUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InventoryUtil {
    public static final int CURSOR_SLOT = -1;
    public static final String ITEMS_KEY = "Items";
    public static final String SLOT_KEY = "Slot";
    public static final String SIZE_KEY = "Size";
    public static final String COUNT_KEY = "Count";
    public static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";

    private static final IntList EMPTY_SLOTS = new IntArrayList();

    public static int indexOf(Inventory inventory, ItemStack stack) {
        int size = inventory.size();
        for (int i = 0; i < size; ++i) {
            if (inventory.getStack(i) == stack) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(Inventory inventory, BiPredicate<ItemStack, Integer> predicate) {
        int size = inventory.size();
        for (int i = 0; i < size; ++i) {
            if (predicate.test(inventory.getStack(i), i)) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(Inventory inventory, BiPredicate<ItemStack, Integer> predicate) {
        for (int i = inventory.size() - 1; i >= 0; --i) {
            if (predicate.test(inventory.getStack(i), i)) {
                return i;
            }
        }
        return -1;
    }

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

    public static int firstIndexOf(Inventory parentInventory, Inventory targetInventory) {
        if (Objects.equals(parentInventory, targetInventory)) {
            return 0;
        }

        int currentIndex = 0;
        for (Inventory innerInventory : (Iterable<Inventory>)CombinedInventory.asStream(parentInventory)::iterator) {
            if (Objects.equals(targetInventory, innerInventory)) {
                return currentIndex;
            }
            currentIndex += innerInventory.size();
        }
        return -1;
    }

    public static int indexOfExtractionSlot(Inventory from, Inventory to, int toSlot) {
        for (int i = from.size() - 1; i >= 0; i--) {
            ItemStack fromStack = from.getStack(i);
            if (fromStack.isEmpty() || InventoryUtil.isSameSlot(from, i, to, toSlot)) {
                continue;
            }

            if (InventoryUtil.canInsertOrPartiallyCombine(to, toSlot, fromStack)) {
                return i;
            }
        }

        return -1;
    }

    public static int indexOfInsertionSlot(Inventory from, int fromSlot, Inventory to) {
        int insertionIndex = -1;
        ItemStack fromStack = from.getStack(fromSlot);
        int toSize = to.size();

        for (int i = 0; i < toSize; i++) {
            if (InventoryUtil.isSameSlot(from, fromSlot, to, i)) {
                continue;
            }

            if (!InventoryUtil.canInsertOrPartiallyCombine(to, i, fromStack)) {
                continue;
            }

            ItemStack toStack = to.getStack(i);
            if (toStack.isEmpty()) {
                insertionIndex = insertionIndex == -1 ? i : insertionIndex;
            }
            else {
                return i;
            }
        }

        return insertionIndex;
    }

    public static boolean isSameSlot(Inventory a, int aSlot, Inventory b, int bSlot) {
        if (!(a instanceof CombinedInventory) && !(b instanceof CombinedInventory)) {
            return Objects.equals(a, b) && aSlot == bSlot;
        }

        for (Inventory innerA : (Iterable<Inventory>)CombinedInventory.asStream(a)::iterator) {
            if (aSlot < innerA.size()) {
                a = innerA;
                break;
            }
            aSlot -= innerA.size();
        }

        for (Inventory innerB : (Iterable<Inventory>)CombinedInventory.asStream(b)::iterator) {
            if (bSlot < innerB.size()) {
                b = innerB;
                break;
            }
            bSlot -= innerB.size();
        }
        return InventoryUtil.isSameSlot(a, aSlot, b, bSlot);
    }

    public static boolean canInsert(Inventory inventory, int slot, ItemStack stack) {
        return InventoryUtil.canInsert(inventory, slot, stack, null);
    }

    public static boolean canInsert(Inventory inventory, int slot, ItemStack stack, @Nullable Direction direction) {
        if (inventory instanceof SidedInventory) {
            return ((SidedInventory)inventory).canInsert(slot, stack, direction);
        }
        return inventory.isValid(slot, stack);
    }

    public static boolean canInsertOrCombine(Inventory inventory, int slot, ItemStack stack) {
        return InventoryUtil.canInsertOrCombine(inventory, slot, stack, null);
    }

    public static boolean canInsertOrCombine(Inventory inventory, int slot, ItemStack stack, @Nullable Direction direction) {
        ItemStack currentStack = inventory.getStack(slot);
        if (!currentStack.isEmpty()) {
            return ItemStack.canCombine(currentStack, stack) && (currentStack.getCount() + stack.getCount()) <= currentStack.getMaxCount();
        }
        return InventoryUtil.canInsert(inventory, slot, stack, direction);
    }

    public static boolean canInsertOrPartiallyCombine(Inventory inventory, int slot, ItemStack stack) {
        return InventoryUtil.canInsertOrPartiallyCombine(inventory, slot, stack, null);
    }

    public static boolean canInsertOrPartiallyCombine(Inventory inventory, int slot, ItemStack stack, @Nullable Direction direction) {
        ItemStack currentStack = inventory.getStack(slot);
        if (!currentStack.isEmpty()) {
            return ItemStack.canCombine(currentStack, stack) && currentStack.getCount() < currentStack.getMaxCount();
        }
        return InventoryUtil.canInsert(inventory, slot, stack, direction);
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

    public static boolean transfer(Inventory from, Inventory to) {
        boolean success = true;
        int size = from.size();

        for (int i = 0; i < size; i++) {
            if (!from.getStack(i).isEmpty()) {
                success &= InventoryUtil.transfer(from, i, to, -1);
            }
        }

        return success;
    }

    public static boolean transfer(Inventory from, IntList fromSlots, Inventory to) {
        for (IntListIterator iterator = fromSlots.iterator(); iterator.hasNext(); ) {
            int fromSlot = iterator.nextInt();
            if (InventoryUtil.transfer(from, fromSlot, to, -1)) {
                iterator.remove();
            }
        }

        return fromSlots.isEmpty();
    }

    public static boolean transfer(Inventory from, Inventory to, IntList toSlots) {
        for (IntListIterator iterator = toSlots.iterator(); iterator.hasNext(); ) {
            int toSlot = iterator.nextInt();
            if (InventoryUtil.transfer(from, -1, to, toSlot)) {
                iterator.remove();
            }
        }

        return toSlots.isEmpty();
    }

    public static boolean transfer(Inventory from, int fromSlot, Inventory to) {
        return InventoryUtil.transfer(from, fromSlot, to, -1);
    }

    public static boolean transfer(Inventory from, Inventory to, int toSlot) {
        return InventoryUtil.transfer(from, -1, to, toSlot);
    }

    public static boolean transfer(Inventory from, int fromSlot, Inventory to, int toSlot) {
        if (fromSlot < 0 && toSlot < 0) {
            return InventoryUtil.transfer(from, to);
        }

        int originalFromSlot = fromSlot;
        int originalToSlot = toSlot;

        fromSlot = fromSlot >= 0 ? fromSlot : InventoryUtil.indexOfExtractionSlot(from, to, toSlot);
        toSlot = toSlot >= 0 ? toSlot : InventoryUtil.indexOfInsertionSlot(from, fromSlot, to);
        if (fromSlot < 0 || toSlot < 0) {
            return false;
        }

        ItemStack fromStack = from.getStack(fromSlot);
        ItemStack toStack = to.getStack(toSlot);
        if (!InventoryUtil.canInsertOrPartiallyCombine(to, toSlot, fromStack)) {
            return false;
        }

        if (toStack.isEmpty()) {
            toStack = fromStack.copy();
            fromStack.setCount(0);
        } else {
            int d = Math.min(toStack.getMaxCount() - toStack.getCount(), fromStack.getCount());
            fromStack.decrement(d);
            toStack.increment(d);
        }
        to.setStack(toSlot, toStack);

        if (fromStack.isEmpty()) {
            from.removeStack(fromSlot);
        } else {
            from.setStack(fromSlot, fromStack);
        }

        boolean isToStackFilled = originalFromSlot >= 0 || !toStack.isStackable() || toStack.getCount() == toStack.getMaxCount();
        if (fromStack.isEmpty() && isToStackFilled) {
            return true;
        }

        return InventoryUtil.transfer(from, originalFromSlot, to, originalToSlot);
    }

    public static void drop(Inventory inventory, PlayerEntity owner) {
        int size = inventory.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.removeStack(i);
            if (!stack.isEmpty()) {
                owner.dropItem(stack, true);
            }
        }
    }

    public static Inventory getPlayerInventoryAndNormalizeSlots(PlayerEntity player, IntList slots) {
        return InventoryUtil.getPlayerInventoryAndNormalizeSlots(player, slots, EMPTY_SLOTS);
    }

    public static Inventory getPlayerInventoryAndNormalizeSlots(PlayerEntity player, IntList primarySlots, IntList secondarySlots) {
        List<Slot> slots = player.currentScreenHandler.slots;
        int slotCount = slots.size();

        boolean hasCursorSlot = primarySlots.contains(CURSOR_SLOT) || secondarySlots.contains(CURSOR_SLOT);
        List<Slot> primarySlotHandlers = primarySlots.intStream().mapToObj(x -> x >= 0 && x < slotCount ? slots.get(x) : null).toList();
        List<Slot> secondarySlotHandlers = secondarySlots.intStream().mapToObj(x -> x >= 0 && x < slotCount ? slots.get(x) : null).toList();
        Stream<Slot> slotHandlers = Stream.concat(Stream.concat(primarySlotHandlers.stream(), secondarySlotHandlers.stream()), Stream.ofNullable(hasCursorSlot ? StackReferenceSlot.ofCursorStack(player.currentScreenHandler) : null));
        Set<Inventory> inventories = slotHandlers.filter(Objects::nonNull).map(x -> x.inventory).collect(Collectors.toCollection(LinkedHashSet::new));
        Inventory inventory = CombinedInventory.of(inventories);
        int cursorSlot = inventory.size() - 1;

        primarySlots.clear();
        secondarySlots.clear();
        primarySlotHandlers.stream().mapToInt(x -> x == null ? cursorSlot : (InventoryUtil.firstIndexOf(inventory, x.inventory) + x.getIndex())).forEach(primarySlots::add);
        secondarySlotHandlers.stream().mapToInt(x -> x == null ? cursorSlot : (InventoryUtil.firstIndexOf(inventory, x.inventory) + x.getIndex())).forEach(secondarySlots::add);
        return inventory;
    }

    public static DefaultedList<ItemStack> getInventory(ItemStack stack) {
        return InventoryUtil.getInventory(stack, InventoryUtil.getInventorySize(stack));
    }

    public static DefaultedList<ItemStack> getInventory(ItemStack stack, int size) {
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        NbtList list = InventoryUtil.getItemsList(stack);
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                NbtCompound nbt = list.getCompound(i);
                int slot = nbt.contains(SLOT_KEY) ? nbt.getByte(SLOT_KEY) : i;
                if (slot < inventory.size()) {
                    inventory.set(slot, ItemStack.fromNbt(nbt));
                }
            }
        }
        return inventory;
    }

    private static int getInventorySize(ItemStack stack) {
        Optional<Integer> size = BlockEntityUtil.getBlockEntityType(stack.getItem()).flatMap(BlockEntityUtil::getInventorySize);
        if (size.isPresent()) {
            return size.get();
        }

        NbtList items = InventoryUtil.getItemsList(stack);
        if (items == null) {
            return 0;
        }

        int maxSlot = -1;
        int entryCount = 0;
        for (NbtElement item : items) {
            if (item instanceof NbtCompound && ((NbtCompound)item).contains(SLOT_KEY)) {
                maxSlot = Math.max(maxSlot, ((NbtCompound)item).getByte(SLOT_KEY));
            }
            ++entryCount;
        }

        return maxSlot == -1 ? entryCount : (maxSlot + 1);
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
