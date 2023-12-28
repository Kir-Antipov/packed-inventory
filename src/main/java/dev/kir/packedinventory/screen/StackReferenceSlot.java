package dev.kir.packedinventory.screen;

import dev.kir.packedinventory.inventory.StackReferenceInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class StackReferenceSlot extends Slot {
    private StackReferenceSlot(StackReferenceInventory inventory) {
        super(inventory, 0, 0, 0);
    }

    public static StackReferenceSlot of(Inventory inventory, int slot) {
        return new StackReferenceSlot(StackReferenceInventory.of(inventory, slot));
    }

    public static StackReferenceSlot ofCursorStack(ScreenHandler screenHandler) {
        return new StackReferenceSlot(StackReferenceInventory.ofCursorStack(screenHandler));
    }
}