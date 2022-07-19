package dev.kir.packedinventory.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public final class EmptyInventory implements Inventory {
    private static final EmptyInventory INSTANCE = new EmptyInventory();

    public static Inventory getInstance() {
        return INSTANCE;
    }

    private EmptyInventory() { }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) { }

    @Override
    public void markDirty() { }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int count(Item item) {
        return 0;
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return false;
    }

    @Override
    public void clear() { }
}
