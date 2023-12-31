package dev.kir.packedinventory.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public final class CombinedInventory implements Inventory {
    private final Collection<Inventory> inventories;
    private final int size;
    private final int maxCountPerStack;

    private CombinedInventory(Collection<Inventory> inventories) {
        this.inventories = inventories;
        this.size = inventories.stream().map(Inventory::size).reduce(0, Integer::sum);
        this.maxCountPerStack = inventories.stream().map(Inventory::getMaxCountPerStack).max(Comparator.comparingInt(a -> a)).orElse(MAX_COUNT_PER_STACK);
    }

    public static Inventory of() {
        return EmptyInventory.getInstance();
    }

    public static Inventory of(Inventory inventory) {
        return inventory;
    }

    public static Inventory of(Inventory left, Inventory right) {
        return new CombinedInventory(List.of(left, right));
    }

    public static Inventory of(Inventory... inventories) {
        return CombinedInventory.of(Arrays.asList(inventories));
    }

    public static Inventory of(Collection<Inventory> inventories) {
        return switch (inventories.size()) {
            case 0 -> EmptyInventory.getInstance();
            case 1 -> inventories.iterator().next();
            default -> new CombinedInventory(inventories.stream().flatMap(CombinedInventory::asStream).toList());
        };
    }

    public static Stream<Inventory> asStream(Inventory inventory) {
        return inventory instanceof CombinedInventory ? ((CombinedInventory)inventory).inventories.stream() : Stream.of(inventory);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.inventories.stream().allMatch(Inventory::isEmpty);
    }

    protected <T> T execute(int slot, BiFunction<Inventory, Integer, T> func, T defaultValue) {
        Inventory currentInventory;
        for (Iterator<Inventory> iterator = this.inventories.iterator(); iterator.hasNext(); slot -= currentInventory.size()) {
            currentInventory = iterator.next();
            if (slot < currentInventory.size()) {
                return func.apply(currentInventory, slot);
            }
        }
        return defaultValue;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.execute(slot, Inventory::getStack, ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.execute(slot, (inv, i) -> inv.removeStack(i, amount), ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.execute(slot, Inventory::removeStack, ItemStack.EMPTY);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.execute(slot, (inv, i) -> {
            inv.setStack(i, stack);
            return stack;
        }, ItemStack.EMPTY);
    }

    @Override
    public int getMaxCountPerStack() {
        return this.maxCountPerStack;
    }

    @Override
    public void markDirty() {
        this.inventories.forEach(Inventory::markDirty);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.inventories.stream().allMatch(x -> x.canPlayerUse(player));
    }

    @Override
    public void onOpen(PlayerEntity player) {
        this.inventories.forEach(x -> x.onOpen(player));
    }

    @Override
    public void onClose(PlayerEntity player) {
        this.inventories.forEach(x -> x.onClose(player));
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return this.execute(slot, (inv, i) -> inv.isValid(i, stack), false);
    }

    @Override
    public void clear() {
        this.inventories.forEach(Inventory::clear);
    }
}
