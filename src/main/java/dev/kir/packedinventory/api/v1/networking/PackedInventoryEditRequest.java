package dev.kir.packedinventory.api.v1.networking;

import com.mojang.datafixers.util.Either;
import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.FailureReason;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewerRegistry;
import dev.kir.packedinventory.inventory.CombinedInventory;
import dev.kir.packedinventory.inventory.StackReferenceInventory;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;
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
     * Cursor stack slot index.
     */
    public static final int CURSOR_SLOT = -1;

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
        PackedInventoryEditRequest.sendToServer(ActionType.DEFAULT, slotIndex, CURSOR_SLOT, isHandledScreenSlot);
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
        PackedInventoryEditRequest.sendToServer(slotIndex, isHandledScreenSlot);
    }

    /**
     * Sends an edit request for an item in the specified slot.
     * @param actionType Inventory edit action type.
     * @param slotIndex Slot index.
     * @param isHandledScreenSlots {@code true} if the slot index is relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, int slotIndex, boolean isHandledScreenSlots) {
        PackedInventoryEditRequest.sendToServer(actionType, slotIndex, CURSOR_SLOT, isHandledScreenSlots);
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
        if (!ClientPlayNetworking.canSend(ID)) {
            return;
        }

        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeEnumConstant(actionType);
        buffer.writeBoolean(isHandledScreenSlots);
        buffer.writeVarInt(primarySlotIndex);
        buffer.writeVarInt(secondarySlotIndex);
        ClientPlayNetworking.send(ID, buffer);
    }

    private static void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ActionType actionType = buf.readEnumConstant(ActionType.class);
        boolean isHandledScreenSlot = buf.readBoolean();
        int primarySlotIndex = buf.readVarInt();
        int secondarySlotIndex = buf.readVarInt();

        Inventory inventory;
        int primarySlot;
        int secondarySlot;
        if (isHandledScreenSlot) {
            DefaultedList<Slot> slots = player.currentScreenHandler.slots;
            int slotCount = slots.size();

            Slot primarySlotHandler = primarySlotIndex >= 0 && primarySlotIndex < slotCount ? slots.get(primarySlotIndex) : null;
            if (primarySlotHandler == null && primarySlotIndex != CURSOR_SLOT) {
                return;
            }
            Slot secondarySlotHandler = secondarySlotIndex >= 0 && secondarySlotIndex < slotCount ? slots.get(secondarySlotIndex) : null;
            boolean hasCursorSlot = primarySlotIndex == CURSOR_SLOT || secondarySlotIndex == CURSOR_SLOT && actionType == ActionType.QUICK_TRANSFER;
            if (primarySlotHandler != null && (secondarySlotHandler == null || primarySlotHandler.inventory == secondarySlotHandler.inventory) && !hasCursorSlot) {
                inventory = primarySlotHandler.inventory;
            } else {
                Set<Inventory> inventories = new LinkedHashSet<>(3);
                if (primarySlotHandler != null) {
                    inventories.add(primarySlotHandler.inventory);
                }
                if (secondarySlotHandler != null) {
                    inventories.add(secondarySlotHandler.inventory);
                }
                if (hasCursorSlot) {
                    inventories.add(StackReferenceInventory.ofCursorStack(player.currentScreenHandler));
                }
                inventory = CombinedInventory.of(inventories);
            }
            primarySlot = primarySlotHandler == null ? (inventory.size() - 1) : primarySlotHandler.getIndex();
            secondarySlot = secondarySlotHandler == null ? (inventory.size() - 1) : (secondarySlotHandler.getIndex() + (primarySlotHandler != null && primarySlotHandler.inventory != secondarySlotHandler.inventory ? primarySlotHandler.inventory.size() : 0));
        } else {
            inventory = primarySlotIndex == CURSOR_SLOT || secondarySlotIndex == CURSOR_SLOT && actionType == ActionType.QUICK_TRANSFER ? CombinedInventory.of(player.getInventory(), StackReferenceInventory.ofCursorStack(player.playerScreenHandler)) : player.getInventory();
            primarySlot = primarySlotIndex == CURSOR_SLOT ? (inventory.size() - 1) : primarySlotIndex;
            secondarySlot = secondarySlotIndex == CURSOR_SLOT ? (inventory.size() - 1) : secondarySlotIndex;
        }

        switch (actionType) {
            case DEFAULT:
                server.execute(() -> executeDefaultAction(inventory, primarySlot, player));
                break;
            case QUICK_TRANSFER:
                server.execute(() -> executeQuickTransferAction(inventory, primarySlot, secondarySlot, player));
                break;
            case DROP:
                server.execute(() -> executeDropAction(inventory, primarySlot, player));
                break;
        }
    }

    private static void executeDefaultAction(Inventory inventory, int slot, ServerPlayerEntity player) {
        getView(inventory, slot, player).ifPresent(view -> view.ifLeft(x -> handleView(x, inventory, slot, player)).ifRight(x -> handleFailure(x, inventory, slot, player)));
    }

    private static void executeQuickTransferAction(Inventory inventory, int primarySlot, int secondarySlot, ServerPlayerEntity player) {
        InventoryViewerRegistry registry = InventoryViewerRegistry.getInstance();
        int quickViewSlot;
        if (registry.hasView(inventory, primarySlot, player)) {
            quickViewSlot = primarySlot;
        } else if (registry.hasView(inventory, secondarySlot, player)) {
            quickViewSlot = secondarySlot;
        } else {
            quickViewSlot = -1;
        }

        Optional<Either<Inventory, FailureReason>> optionalQuickView = quickViewSlot == -1 ? Optional.empty() : getView(inventory, quickViewSlot, player);
        if (optionalQuickView.isEmpty()) {
            return;
        }

        Either<Inventory, FailureReason> quickView = optionalQuickView.get();
        if (quickView.right().isPresent()) {
            handleFailure(quickView.right().get(), inventory, quickViewSlot, player);
            return;
        }

        Inventory quickViewInventory = quickView.left().orElse(inventory);
        if (quickViewInventory.size() == 0) {
            if (quickViewSlot == primarySlot && registry.hasView(inventory, secondarySlot, player)) {
                quickViewSlot = secondarySlot;
                optionalQuickView = quickViewSlot == -1 ? Optional.empty() : getView(inventory, quickViewSlot, player);
                if (optionalQuickView.isEmpty()) {
                    return;
                }

                quickView = optionalQuickView.get();
                if (quickView.right().isPresent()) {
                    handleFailure(quickView.right().get(), inventory, quickViewSlot, player);
                    return;
                }
                quickViewInventory = quickView.left().orElse(inventory);
            } else {
                return;
            }
        }

        Inventory from;
        int fromSlot;
        Inventory to;
        int toSlot;
        if (quickViewSlot == primarySlot) {
            if (inventory.getStack(secondarySlot).isEmpty()) {
                from = quickViewInventory;
                fromSlot = -1;
                to = inventory;
                toSlot = secondarySlot;
            } else {
                from = inventory;
                fromSlot = secondarySlot;
                to = quickViewInventory;
                toSlot = -1;
            }
        } else {
            from = inventory;
            fromSlot = primarySlot;
            to = quickViewInventory;
            toSlot = -1;
        }
        handleQuickView(from, fromSlot, to, toSlot);
    }

    private static void executeDropAction(Inventory inventory, int slot, ServerPlayerEntity player) {
        getView(inventory, slot, player).ifPresent(view -> view.ifLeft(x -> handleDropView(x, player)).ifRight(x -> handleFailure(x, inventory, slot, player)));
    }

    static void handleView(Inventory view, Inventory inventory, int slot, ServerPlayerEntity player) {
        InventoryViewHandlerRegistry.getInstance().handle(view, inventory, slot, player);
    }

    static void handleFailure(FailureReason failure, Inventory inventory, int slot, ServerPlayerEntity player) {
        InventoryValidationFailureHandlerRegistry.getInstance().handle(failure, inventory, slot, player);
    }

    static void handleQuickView(Inventory from, int fromSlot, Inventory to, int toSlot) {
        int origFromSlot = fromSlot;
        int origToSlot = toSlot;

        if (fromSlot == -1) {
            int targetSlot = toSlot;
            fromSlot = InventoryUtil.lastIndexOf(from, (x, i) -> !x.isEmpty() && !InventoryUtil.isSameSlot(from, i, to, targetSlot) && InventoryUtil.canInsertOrPartiallyCombine(to, targetSlot, x));
            if (fromSlot == -1) {
                return;
            }
        }
        ItemStack fromStack = from.getStack(fromSlot);

        if (toSlot == -1) {
            int sourceSlot = fromSlot;
            toSlot = InventoryUtil.indexOf(to, (x, i) -> !InventoryUtil.isSameSlot(from, sourceSlot, to, i) && InventoryUtil.canInsertOrPartiallyCombine(to, i, fromStack));
            if (toSlot == -1) {
                return;
            }
        }
        ItemStack toStack = to.getStack(toSlot);

        if (toStack.isEmpty()) {
            toStack = fromStack.copy();
            fromStack.setCount(0);
        } else {
            int d = Math.min(toStack.getMaxCount() - toStack.getCount(), fromStack.getCount());
            fromStack.decrement(d);
            toStack.increment(d);
        }
        to.setStack(toSlot, toStack);

        if (fromStack.isEmpty()) {
            from.removeStack(fromSlot);
        } else {
            from.setStack(fromSlot, fromStack);
            handleQuickView(from, origFromSlot, to, origToSlot);
        }
    }

    static void handleDropView(Inventory view, ServerPlayerEntity player) {
        int size = view.size();
        for (int i = 0; i < size; ++i) {
            ItemStack stack = view.removeStack(i);
            if (!stack.isEmpty()) {
                player.dropItem(stack, true);
            }
        }
    }

    static Optional<Either<Inventory, FailureReason>> getView(Inventory inventory, int slot, ServerPlayerEntity player) {
        InventoryViewerRegistry registry = InventoryViewerRegistry.getInstance();
        Optional<Either<Inventory, FailureReason>> view = registry.view(inventory, slot, player);
        if (view.isEmpty()) {
            return view;
        }

        if (view.get().right().isPresent()) {
            FailureReason currentFailureReason = view.get().right().get();
            LinkedHashSet<Inventory> availableInventories = new LinkedHashSet<>();
            availableInventories.add(inventory);
            for (Slot screenHandlerSlot : player.currentScreenHandler.slots) {
                availableInventories.add(screenHandlerSlot.inventory);
            }

            if (availableInventories.size() > 1) {
                view = registry.view(CombinedInventory.of(availableInventories), slot, player).or(() -> Optional.of(Either.right(currentFailureReason)));
            }
        }
        return view;
    }

    /**
     * Registers server receiver for this request.
     * @param receiver Request receiver.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, PackedInventoryEditRequest::execute);
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
