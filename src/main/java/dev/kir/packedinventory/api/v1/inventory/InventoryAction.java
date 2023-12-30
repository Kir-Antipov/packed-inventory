package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.networking.InventoryActionRequest;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Represents an action that can be performed on an inventory.
 */
public interface InventoryAction {
    /**
     * Represents the cursor slot in a handled screen.
     */
    int CURSOR_SLOT = -1;

    /**
     * Invokes the inventory action.
     */
    @Environment(EnvType.CLIENT)
    default void invoke() {
        InventoryActionRequest.sendToServer(this);
    }

    /**
     * Invokes the inventory action.
     * @param player The player performing the action.
     * @return {@code true} if the action was successful; otherwise, {@code false}.
     */
    boolean invoke(ServerPlayerEntity player);

    /**
     * Returns the type of the inventory action.
     * @return The type of the inventory action
     */
    InventoryActionType<?> getType();

    /**
     * Creates an inventory action that handles a single slot.
     * @param slot The slot to be handled.
     * @return An inventory action that handles the specified slot.
     */
    static InventoryAction handle(int slot) {
        return InventoryAction.handle(IntList.of(slot));
    }

    /**
     * Creates an inventory action that handles multiple slots.
     * @param slots The slots to be handled.
     * @return An inventory action that handles the specified slots.
     */
    static InventoryAction handle(List<Integer> slots) {
        return new DefaultInventoryAction(slots);
    }

    /**
     * Creates an inventory action that transfers items from one slot to another.
     * @param fromSlot The slot from which items are transferred.
     * @param toSlot The slot to which items are transferred.
     * @return An inventory action that transfers items between the specified slots.
     */
    static InventoryAction transfer(int fromSlot, int toSlot) {
        return InventoryAction.transfer(IntList.of(fromSlot), IntList.of(toSlot), InventoryTransferOptions.NONE);
    }

    /**
     * Creates an inventory action that transfers items from one slot to another, with specified options.
     * @param fromSlot The slot from which items are transferred.
     * @param toSlot The slot to which items are transferred.
     * @param options The options for the transfer.
     * @return An inventory action that transfers items between the specified slots with the specified options.
     */
    static InventoryAction transfer(int fromSlot, int toSlot, EnumSet<InventoryTransferOptions> options) {
        return InventoryAction.transfer(IntList.of(fromSlot), IntList.of(toSlot), options);
    }

    /**
     * Creates an inventory action that transfers items between multiple slots.
     * @param fromSlots The slots from which items are transferred.
     * @param toSlots The slots to which items are transferred.
     * @return An inventory action that transfers items between the specified slots.
     */
    static InventoryAction transfer(List<Integer> fromSlots, List<Integer> toSlots) {
        return InventoryAction.transfer(fromSlots, toSlots, InventoryTransferOptions.NONE);
    }

    /**
     * Creates an inventory action that transfers items between multiple slots, with specified options.
     * @param fromSlots The slots from which items are transferred.
     * @param toSlots The slots to which items are transferred.
     * @param options The options for the transfer.
     * @return An inventory action that transfers items between the specified slots with the specified options.
     */
    static InventoryAction transfer(List<Integer> fromSlots, List<Integer> toSlots, EnumSet<InventoryTransferOptions> options) {
        return new TransferInventoryAction(fromSlots, toSlots, options);
    }

    /**
     * Creates an inventory action that drops items from a specific slot.
     * @param slot the slot from which items are to be dropped.
     * @return An inventory action that drops items from the specified slot.
     */
    static InventoryAction drop(int slot) {
        return InventoryAction.drop(IntList.of(slot));
    }

    /**
     * Creates an inventory action that drops items from multiple slots.
     * @param slots The slots from which items are to be dropped.
     * @return An inventory action that drops items from the specified slots.
     */
    static InventoryAction drop(List<Integer> slots) {
        return new DropInventoryAction(slots);
    }
}
