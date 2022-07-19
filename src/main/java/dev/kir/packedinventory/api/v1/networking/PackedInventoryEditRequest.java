package dev.kir.packedinventory.api.v1.networking;

import com.mojang.datafixers.util.Either;
import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.FailureReason;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewerRegistry;
import dev.kir.packedinventory.inventory.CombinedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.function.BiFunction;

/**
 * A class that can be used to send inventory edit requests.
 */
public final class PackedInventoryEditRequest {
    /**
     * Identifier of this request.
     */
    public static final Identifier ID = PackedInventory.locate("packed_inventory_edit_request");

    /**
     * Sends an edit request for an item in the player inventory at the given index.
     * @param playerInventorySlotIndex Slot index within the player inventory.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(int playerInventorySlotIndex) {
        PackedInventoryEditRequest.sendToServer(playerInventorySlotIndex, false);
    }

    /**
     * Sends an edit request for an item in the specified slot.
     * @param slotIndex Slot index.
     * @param isHandledScreenSlot {@code true} if slot index is relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(int slotIndex, boolean isHandledScreenSlot) {
        PackedInventoryEditRequest.sendToServer(slotIndex, isHandledScreenSlot, true);
    }

    /**
     * Sends an edit request for an item in the specified slot.
     * @param slotIndex Slot index.
     * @param isHandledScreenSlot {@code true} if slot index is relative to the current screen handler; otherwise, {@code false}.
     * @param shouldReturnToPreviousScreen {@code true} if current screen should be reopened after this action succeeds; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(int slotIndex, boolean isHandledScreenSlot, boolean shouldReturnToPreviousScreen) {
        if (!ClientPlayNetworking.canSend(ID)) {
            return;
        }

        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeBoolean(isHandledScreenSlot);
        buffer.writeByte((byte)slotIndex);
        buffer.writeBoolean(shouldReturnToPreviousScreen);
        ClientPlayNetworking.send(ID, buffer);
    }

    private static void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean isHandledScreenSlot = buf.readBoolean();
        int slotIndex = buf.readByte();

        Inventory inventory;
        int slot;
        if (isHandledScreenSlot) {
            Slot slotHandler = slotIndex < player.currentScreenHandler.slots.size() ? player.currentScreenHandler.getSlot(slotIndex) : null;
            if (slotHandler == null) {
                return;
            }
            inventory = slotHandler.inventory;
            slot = slotHandler.getIndex();
        } else {
            inventory = player.getInventory();
            slot = slotIndex;
        }

        server.execute(() -> {
            Either<Inventory, FailureReason> view = InventoryViewerRegistry.getInstance().view(inventory, slot, player).orElse(null);
            if (view == null) {
                return;
            }

            if (view.right().isPresent()) {
                FailureReason currentFailureReason = view.right().get();
                LinkedHashSet<Inventory> availableInventories = new LinkedHashSet<>();
                availableInventories.add(inventory);
                for (Slot screenHandlerSlot : player.currentScreenHandler.slots) {
                    availableInventories.add(screenHandlerSlot.inventory);
                }

                if (availableInventories.size() > 1) {
                    view = InventoryViewerRegistry.getInstance().view(CombinedInventory.of(availableInventories), slot, player).orElseGet(() -> Either.right(currentFailureReason));
                }
            }

            view
                .ifLeft(inv -> InventoryViewHandlerRegistry.getInstance().handle(inv, inventory, slot, player))
                .ifRight(failure -> InventoryValidationFailureHandlerRegistry.getInstance().handle(failure, inventory, slot, player));
        });

    }

    /**
     * Registers server receiver for this request.
     * @param receiver Request receiver.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, PackedInventoryEditRequest::execute);
    }

    private PackedInventoryEditRequest() { }
}
