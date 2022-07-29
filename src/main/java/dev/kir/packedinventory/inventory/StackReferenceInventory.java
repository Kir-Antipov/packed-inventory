package dev.kir.packedinventory.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import java.util.Set;

public class StackReferenceInventory implements Inventory {
    private static final int SLOT = 0;
    private final StackReference stack;

    public StackReferenceInventory(StackReference stack) {
        this.stack = stack;
    }

    public static StackReferenceInventory of(Inventory inventory, int slot) {
        return new StackReferenceInventory(StackReference.of(inventory, slot));
    }

    public static StackReferenceInventory ofCursorStack(ScreenHandler handler) {
        return new StackReferenceInventory(new StackReference() {
            @Override
            public ItemStack get() {
                return handler.getCursorStack();
            }

            @Override
            public boolean set(ItemStack stack) {
                handler.setCursorStack(stack);
                return true;
            }
        });
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.stack.get().isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == SLOT ? this.stack.get() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = this.stack.get();
        if (slot != SLOT || amount <= 0 || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack splitStack = itemStack.split(amount);
        this.stack.set(itemStack);
        return splitStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot == SLOT) {
            ItemStack currentStack = this.stack.get();
            this.stack.set(ItemStack.EMPTY);
            return currentStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == SLOT) {
            this.stack.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
    }

    @Override
    public void markDirty() { }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot == SLOT;
    }

    @Override
    public int count(Item item) {
        ItemStack itemStack = this.stack.get();
        return itemStack.getItem().equals(item) ? itemStack.getCount() : 0;
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return items.contains(this.stack.get().getItem());
    }

    @Override
    public void clear() {
        this.stack.set(ItemStack.EMPTY);
    }
}
