package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.networking.InventoryActionRequest;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumSet;
import java.util.List;

public interface InventoryAction {
    int CURSOR_SLOT = -1;

    @Environment(EnvType.CLIENT)
    default void invoke() {
        InventoryActionRequest.sendToServer(this);
    }

    boolean invoke(ServerPlayerEntity player);

    InventoryActionType<?> getType();

    static InventoryAction handle(int slot) {
        return InventoryAction.handle(IntList.of(slot));
    }

    static InventoryAction handle(List<Integer> slots) {
        return new DefaultInventoryAction(slots);
    }

    static InventoryAction transfer(int fromSlot, int toSlot) {
        return InventoryAction.transfer(IntList.of(fromSlot), IntList.of(toSlot), InventoryTransferOptions.NONE);
    }

    static InventoryAction transfer(int fromSlot, int toSlot, EnumSet<InventoryTransferOptions> options) {
        return InventoryAction.transfer(IntList.of(fromSlot), IntList.of(toSlot), options);
    }

    static InventoryAction transfer(List<Integer> fromSlots, List<Integer> toSlots) {
        return InventoryAction.transfer(fromSlots, toSlots, InventoryTransferOptions.NONE);
    }

    static InventoryAction transfer(List<Integer> fromSlots, List<Integer> toSlots, EnumSet<InventoryTransferOptions> options) {
        return new TransferInventoryAction(fromSlots, toSlots, options);
    }

    static InventoryAction drop(int slot) {
        return InventoryAction.drop(IntList.of(slot));
    }

    static InventoryAction drop(List<Integer> slots) {
        return new DropInventoryAction(slots);
    }
}