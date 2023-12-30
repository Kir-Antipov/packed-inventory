package dev.kir.packedinventory.api.v1.networking;

import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.inventory.InventoryAction;
import dev.kir.packedinventory.api.v1.inventory.InventoryTransferOptions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A class that can be used to send inventory bulk edit requests.
 * @deprecated Use {@link InventoryActionRequest} instead.
 */
@Deprecated(since = "0.3.0", forRemoval = true)
public final class PackedInventoryBulkEditRequest {
    /**
     * Identifier of this request.
     */
    public static final Identifier ID = PackedInventory.locate("packed_inventory_bulk_edit_request");

    /**
     * Cursor stack slot index.
     */
    public static final int CURSOR_SLOT = InventoryAction.CURSOR_SLOT;

    /**
     * Sends a bulk edit request for items in the specified slots of the player's inventory.
     * @param slots Slot indices.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(List<Integer> slots) {
        ActionType.DEFAULT.toInventoryAction(slots, IntList.of(CURSOR_SLOT), false).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends a bulk edit request for items in the specified slots.
     * @param slots Slot indices.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(List<Integer> slots, boolean isHandledScreenSlots) {
        ActionType.DEFAULT.toInventoryAction(slots, IntList.of(CURSOR_SLOT), isHandledScreenSlots).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends a bulk edit request for items in the specified slots.
     * @param actionType Inventory edit action type.
     * @param slots Slot indices.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, List<Integer> slots, boolean isHandledScreenSlots) {
        actionType.toInventoryAction(slots, IntList.of(CURSOR_SLOT), isHandledScreenSlots).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends a bulk edit request for an item in the specified primary slot.
     * @param actionType Inventory edit action type.
     * @param primarySlotIndex Primary slot index.
     * @param secondarySlotIndices Secondary slot indices.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, int primarySlotIndex, List<Integer> secondarySlotIndices, boolean isHandledScreenSlots) {
        actionType.toInventoryAction(IntList.of(primarySlotIndex), secondarySlotIndices, isHandledScreenSlots).ifPresent(InventoryAction::invoke);
    }

    /**
     * Sends a bulk edit request for items in the specified primary slots.
     * @param actionType Inventory edit action type.
     * @param primarySlotIndices Primary slot indices.
     * @param secondarySlotIndices Secondary slot indices.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, List<Integer> primarySlotIndices, List<Integer> secondarySlotIndices, boolean isHandledScreenSlots) {
        actionType.toInventoryAction(primarySlotIndices, secondarySlotIndices, isHandledScreenSlots).ifPresent(InventoryAction::invoke);
    }

    private static void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ActionType actionType = buf.readEnumConstant(ActionType.class);
        boolean isHandledScreenSlot = buf.readBoolean();
        List<Integer> primarySlotIndices = normalizeSlots(buf.readIntList(), isHandledScreenSlot);
        List<Integer> secondarySlotIndices = normalizeSlots(buf.readIntList(), isHandledScreenSlot);

        if (actionType == ActionType.QUICK_TRANSFER && primarySlotIndices.contains(CURSOR_SLOT) && secondarySlotIndices.stream().anyMatch(i -> !player.getInventory().getStack(i).isEmpty())) {
            List<Integer> tmp = primarySlotIndices;
            primarySlotIndices = secondarySlotIndices;
            secondarySlotIndices = tmp;
        }

        actionType.toInventoryAction(primarySlotIndices, secondarySlotIndices, false)
            .ifPresent(action -> server.execute(() -> action.invoke(player)));
    }

    /**
     * Registers server receiver for this request.
     * @param receiver Request receiver.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, PackedInventoryBulkEditRequest::execute);
    }

    private static List<Integer> normalizeSlots(List<Integer> slots, boolean isHandledScreenSlots) {
        if (isHandledScreenSlots) {
            return slots;
        }

        final int HOTBAR_START = 36;
        return IntArrayList.toList(slots.stream().mapToInt(x -> x + (x >= 0 && x <= 9 ? HOTBAR_START : 0)));
    }

    /**
     * Represents available inventory bulk edit actions.
     */
    public enum ActionType {
        /**
         * An inventory view will be handled via {@link dev.kir.packedinventory.api.v1.inventory.InventoryViewHandler}.
         */
        DEFAULT,

        /**
         * An item stack will be transferred into/out of the selected inventory views.
         */
        QUICK_TRANSFER,

        /**
         * Contents of the selected inventory views will be dropped into the world.
         */
        DROP;

        /**
         * @return An equivalent of this action type instantiated with the specified arguments.
         */
        Optional<InventoryAction> toInventoryAction(List<Integer> primarySlotIndices, List<Integer> secondarySlotIndices, boolean isHandledScreenSlots) {
            return Optional.of(switch (this) {
                case DEFAULT -> InventoryAction.handle(normalizeSlots(primarySlotIndices, isHandledScreenSlots));
                case QUICK_TRANSFER -> InventoryAction.transfer(normalizeSlots(primarySlotIndices, isHandledScreenSlots), normalizeSlots(secondarySlotIndices, isHandledScreenSlots), EnumSet.of(InventoryTransferOptions.PREFER_INSERTION));
                case DROP -> InventoryAction.drop(normalizeSlots( primarySlotIndices, isHandledScreenSlots));
            });
        }

        /**
         * @return An equivalent of this bulk action type instance applicable to {@link PackedInventoryEditRequest}, if any; otherwise, {@link Optional#empty()}.
         */
        public Optional<PackedInventoryEditRequest.ActionType> asEditActionType() {
            return Optional.of(switch (this) {
                case DEFAULT -> PackedInventoryEditRequest.ActionType.DEFAULT;
                case QUICK_TRANSFER -> PackedInventoryEditRequest.ActionType.QUICK_TRANSFER;
                case DROP -> PackedInventoryEditRequest.ActionType.DROP;
            });
        }
    }

    private PackedInventoryBulkEditRequest() { }
}
