package dev.kir.packedinventory.api.v1.networking;

import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.inventory.InventoryAction;
import dev.kir.packedinventory.api.v1.inventory.InventoryTransferOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A class that can be used to send inventory edit requests.
 * @deprecated Use {@link InventoryActionRequest} instead.
 */
@Deprecated(since = "0.3.0", forRemoval = true)
public final class PackedInventoryEditRequest {
    /**
     * Identifier of this request.
     */
    public static final Identifier ID = PackedInventory.locate("packed_inventory_edit_request");

    /**
     * Cursor stack slot index.
     */
    public static final int CURSOR_SLOT = InventoryAction.CURSOR_SLOT;

    /**
     * Sends an edit request for an item in the player inventory at the given index.
     * @param playerInventorySlotIndex Slot index within the player inventory.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(int playerInventorySlotIndex) {
        ActionType.DEFAULT.toInventoryAction(playerInventorySlotIndex, CURSOR_SLOT, false).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends an edit request for an item in the specified slot.
     * @param slotIndex Slot index.
     * @param isHandledScreenSlot {@code true} if slot index is relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(int slotIndex, boolean isHandledScreenSlot) {
        ActionType.DEFAULT.toInventoryAction(slotIndex, CURSOR_SLOT, isHandledScreenSlot).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends an edit request for an item in the specified slot.
     * @param slotIndex Slot index.
     * @param isHandledScreenSlot {@code true} if slot index is relative to the current screen handler; otherwise, {@code false}.
     * @param shouldReturnToPreviousScreen {@code true} if current screen should be reopened after this action succeeds; otherwise, {@code false}.
     * @deprecated Use {@link PackedInventoryEditRequest#sendToServer(int, boolean)} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated(since = "0.2.0")
    @Environment(EnvType.CLIENT)
    public static void sendToServer(int slotIndex, boolean isHandledScreenSlot, boolean shouldReturnToPreviousScreen) {
        ActionType.DEFAULT.toInventoryAction(slotIndex, CURSOR_SLOT, isHandledScreenSlot).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends an edit request for an item in the specified slot.
     * @param actionType Inventory edit action type.
     * @param slotIndex Slot index.
     * @param isHandledScreenSlots {@code true} if the slot index is relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, int slotIndex, boolean isHandledScreenSlots) {
        actionType.toInventoryAction(slotIndex, CURSOR_SLOT, isHandledScreenSlots).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends an edit request for an item in the specified primary slot.
     * @param actionType Inventory edit action type.
     * @param primarySlotIndex Primary slot index.
     * @param secondarySlotIndex Secondary slot index.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, int primarySlotIndex, int secondarySlotIndex, boolean isHandledScreenSlots) {
        actionType.toInventoryAction(primarySlotIndex, secondarySlotIndex, isHandledScreenSlots).ifPresent(InventoryAction::invoke);
    }

    private static void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ActionType actionType = buf.readEnumConstant(ActionType.class);
        boolean isHandledScreenSlot = buf.readBoolean();
        int primarySlotIndex = normalizeSlot(buf.readVarInt(), isHandledScreenSlot);
        int secondarySlotIndex = normalizeSlot( buf.readVarInt(), isHandledScreenSlot);

        if (actionType == ActionType.QUICK_TRANSFER && primarySlotIndex == CURSOR_SLOT && !player.getInventory().getStack(secondarySlotIndex).isEmpty()) {
            int tmp = primarySlotIndex;
            primarySlotIndex = secondarySlotIndex;
            secondarySlotIndex = tmp;
        }

        actionType.toInventoryAction(primarySlotIndex, secondarySlotIndex, false)
            .ifPresent(action -> server.execute(() -> action.invoke(player)));
    }

    /**
     * Registers server receiver for this request.
     * @param receiver Request receiver.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, PackedInventoryEditRequest::execute);
    }

    private static int normalizeSlot(int slot, boolean isHandledScreenSlots) {
        final int HOTBAR_START = 36;
        return slot + (!isHandledScreenSlots && slot >= 0 && slot <= 9 ? HOTBAR_START : 0);
    }

    private PackedInventoryEditRequest() { }


    /**
     * Represents available inventory edit actions.
     */
    public enum ActionType {
        /**
         * An inventory view will be handled via {@link dev.kir.packedinventory.api.v1.inventory.InventoryViewHandler}.
         */
        DEFAULT,

        /**
         * An item stack will be transferred into/out of the selected inventory view.
         */
        QUICK_TRANSFER,

        /**
         * Contents of the selected inventory view will be dropped into the world.
         */
        DROP;

        /**
         * @return An equivalent of this action type instantiated with the specified arguments.
         */
        Optional<InventoryAction> toInventoryAction(int primarySlotIndex, int secondarySlotIndex, boolean isHandledScreenSlots) {
            return Optional.of(switch (this) {
                case DEFAULT -> InventoryAction.handle(normalizeSlot(primarySlotIndex, isHandledScreenSlots));
                case QUICK_TRANSFER -> InventoryAction.transfer(normalizeSlot(primarySlotIndex, isHandledScreenSlots), normalizeSlot(secondarySlotIndex, isHandledScreenSlots), EnumSet.of(InventoryTransferOptions.PREFER_INSERTION));
                case DROP -> InventoryAction.drop(normalizeSlot(primarySlotIndex, isHandledScreenSlots));
            });
        }

        /**
         * @return An equivalent of this action type instance applicable to {@link PackedInventoryBulkEditRequest}, if any; otherwise, {@link Optional#empty()}.
         */
        public Optional<PackedInventoryBulkEditRequest.ActionType> asBulkEditActionType() {
            return Optional.of(switch (this) {
                case DEFAULT -> PackedInventoryBulkEditRequest.ActionType.DEFAULT;
                case QUICK_TRANSFER -> PackedInventoryBulkEditRequest.ActionType.QUICK_TRANSFER;
                case DROP -> PackedInventoryBulkEditRequest.ActionType.DROP;
            });
        }
    }
}
