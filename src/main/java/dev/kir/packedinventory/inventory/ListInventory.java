package dev.kir.packedinventory.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Collection;

public final class ListInventory implements Inventory {
    private final DefaultedList<ItemStack> inventory;

    private ListInventory(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public static Inventory wrap(DefaultedList<ItemStack> inventory) {
        if (inventory.isEmpty()) {
            return EmptyInventory.getInstance();
        }

        return new ListInventory(inventory);
    }

    public static Inventory wrap(Collection<ItemStack> inventory) {
        if (inventory.isEmpty()) {
            return EmptyInventory.getInstance();
        }

        if (inventory instanceof DefaultedList) {
            return new ListInventory((DefaultedList<ItemStack>)inventory);
        }

        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        int i = 0;
        for (ItemStack stack : inventory) {
            if (stack != null) {
                defaultedList.set(i, stack);
            }
            ++i;
        }
        return new ListInventory(defaultedList);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.inventory.size() ? this.inventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.inventory.size()){
            this.inventory.set(slot, stack);
        }
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public void markDirty() { }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }
}
