package dev.kir.packedinventory.api.v1.networking;

import com.mojang.datafixers.util.Either;
import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.FailureReason;
import dev.kir.packedinventory.inventory.CombinedInventory;
import dev.kir.packedinventory.inventory.StackReferenceInventory;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
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

import java.util.*;
import java.util.function.BiFunction;

/**
 * A class that can be used to send inventory bulk edit requests.
 */
public final class PackedInventoryBulkEditRequest {
    /**
     * Identifier of this request.
     */
    public static final Identifier ID = PackedInventory.locate("packed_inventory_bulk_edit_request");

    /**
     * Cursor stack slot index.
     */
    public static final int CURSOR_SLOT = PackedInventoryEditRequest.CURSOR_SLOT;

    /**
     * Sends a bulk edit request for items in the specified slots of the player's inventory.
     * @param slots Slot indices.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(List<Integer> slots) {
        PackedInventoryBulkEditRequest.sendToServer(slots, false);
    }

    /**
     * Sends a bulk edit request for items in the specified slots.
     * @param slots Slot indices.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(List<Integer> slots, boolean isHandledScreenSlots) {
        PackedInventoryBulkEditRequest.sendToServer(ActionType.DEFAULT, slots, isHandledScreenSlots);
    }

    /**
     * Sends a bulk edit request for items in the specified slots.
     * @param actionType Inventory edit action type.
     * @param slots Slot indices.
     * @param isHandledScreenSlots {@code true} if the slot indices are relative to the current screen handler; otherwise, {@code false}.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ActionType actionType, List<Integer> slots, boolean isHandledScreenSlots) {
        PackedInventoryBulkEditRequest.sendToServer(actionType, slots, IntLists.singleton(CURSOR_SLOT), isHandledScreenSlots);
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
        PackedInventoryBulkEditRequest.sendToServer(actionType, IntLists.singleton(primarySlotIndex), secondarySlotIndices, isHandledScreenSlots);
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
        if (!ClientPlayNetworking.canSend(ID)) {
            return;
        }

        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeEnumConstant(actionType);
        buffer.writeBoolean(isHandledScreenSlots);
        buffer.writeIntList(asIntList(primarySlotIndices));
        buffer.writeIntList(asIntList(secondarySlotIndices));
        ClientPlayNetworking.send(ID, buffer);
    }

    private static void execute(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ActionType actionType = buf.readEnumConstant(ActionType.class);
        boolean isHandledScreenSlot = buf.readBoolean();
        IntList primarySlotIndices = buf.readIntList();
        IntList secondarySlotIndices = buf.readIntList();

        Inventory inventory;
        IntList primarySlots;
        IntList secondarySlots;
        if (isHandledScreenSlot) {
            List<Slot> slots = player.currentScreenHandler.slots;
            int slotCount = slots.size();

            boolean hasCursorSlot = primarySlotIndices.contains(CURSOR_SLOT);
            List<Slot> primarySlotHandlers = primarySlotIndices.stream().map(x -> x >= 0 && x < slotCount ? slots.get(x) : null).toList();
            if (!hasCursorSlot && primarySlotHandlers.stream().allMatch(Objects::isNull)) {
                return;
            }
            List<Slot> secondarySlotHandlers = secondarySlotIndices.stream().map(x -> x >= 0 && x < slotCount ? slots.get(x) : null).toList();
            hasCursorSlot = hasCursorSlot || secondarySlotIndices.contains(CURSOR_SLOT);
            Set<Inventory> inventories = new LinkedHashSet<>(3);
            for (Slot primarySlotHandler : primarySlotHandlers) {
                if (primarySlotHandler != null) {
                    inventories.add(primarySlotHandler.inventory);
                }
            }
            for (Slot secondarySlotHandler : secondarySlotHandlers) {
                if (secondarySlotHandler != null) {
                    inventories.add(secondarySlotHandler.inventory);
                }
            }
            if (hasCursorSlot) {
                inventories.add(StackReferenceInventory.ofCursorStack(player.currentScreenHandler));
            }
            inventory = CombinedInventory.of(inventories);
            int cursorSlot = inventory.size() - 1;
            primarySlots = new IntArrayList(primarySlotHandlers.size());
            for (Slot slotHandler : primarySlotHandlers) {
                int slot = slotHandler == null ? cursorSlot : (InventoryUtil.firstIndexOf(inventory, slotHandler.inventory) + slotHandler.getIndex());
                primarySlots.add(slot);
            }
            secondarySlots = new IntArrayList(secondarySlotHandlers.size());
            for (Slot slotHandler : secondarySlotHandlers) {
                int slot = slotHandler == null ? cursorSlot : (InventoryUtil.firstIndexOf(inventory, slotHandler.inventory) + slotHandler.getIndex());
                secondarySlots.add(slot);
            }
        } else {
            boolean hasCursorSlot = primarySlotIndices.contains(CURSOR_SLOT) || secondarySlotIndices.contains(CURSOR_SLOT);
            inventory = hasCursorSlot ? CombinedInventory.of(player.getInventory(), StackReferenceInventory.ofCursorStack(player.playerScreenHandler)) : player.getInventory();
            int cursorSlot = inventory.size() - 1;
            primarySlots = new IntArrayList(primarySlotIndices.size());
            for (int i = 0; i < primarySlotIndices.size(); ++i) {
                int slotIndex = primarySlotIndices.getInt(i);
                primarySlots.add(slotIndex == CURSOR_SLOT ? cursorSlot : slotIndex);
            }
            secondarySlots = new IntArrayList(secondarySlotIndices.size());
            for (int i = 0; i < secondarySlotIndices.size(); ++i) {
                int slotIndex = secondarySlotIndices.getInt(i);
                secondarySlots.add(slotIndex == CURSOR_SLOT ? cursorSlot : slotIndex);
            }
        }

        switch (actionType) {
            case DEFAULT:
                server.execute(() -> executeDefaultAction(inventory, primarySlots, player));
                break;
            case QUICK_TRANSFER:
                server.execute(() -> executeQuickTransferAction(inventory, primarySlots, secondarySlots, player));
                break;
            case DROP:
                server.execute(() -> executeDropAction(inventory, primarySlots, player));
                break;
        }
    }

    private static void executeDefaultAction(Inventory inventory, IntList slots, ServerPlayerEntity player) {
        executeAction(inventory, slots, player, (view, slot) -> {
            PackedInventoryEditRequest.handleView(view, inventory, slot, player);
            return true;
        });
    }

    private static void executeQuickTransferAction(Inventory inventory, IntList primarySlots, IntList secondarySlots, ServerPlayerEntity player) {
        boolean shouldExtract = secondarySlots.stream().anyMatch(x -> inventory.getStack(x).isEmpty());
        executeAction(inventory, primarySlots, player, (view, slot) -> {
            executeQuickTransferAction(inventory, view, secondarySlots, shouldExtract);
            return secondarySlots.isEmpty();
        });
    }

    private static void executeQuickTransferAction(Inventory inventory, Inventory view, IntList secondarySlots, boolean shouldExtract) {
        for (int i = 0; i < secondarySlots.size(); ++i) {
            int slot = secondarySlots.getInt(i);
            if (inventory.getStack(slot).isEmpty() != shouldExtract) {
                continue;
            }

            Inventory from;
            int fromSlot;
            Inventory to;
            int toSlot;
            if (shouldExtract) {
                from = view;
                fromSlot = -1;
                to = inventory;
                toSlot = slot;
            } else {
                from = inventory;
                fromSlot = slot;
                to = view;
                toSlot = -1;
            }
            PackedInventoryEditRequest.handleQuickView(from, fromSlot, to, toSlot);
        }
    }

    private static void executeDropAction(Inventory inventory, IntList slots, ServerPlayerEntity player) {
        executeAction(inventory, slots, player, (view, slot) -> {
            PackedInventoryEditRequest.handleDropView(view, player);
            return false;
        });
    }

    private static IntList asIntList(List<Integer> ints) {
        return ints instanceof IntList ? (IntList)ints : new IntArrayList(ints);
    }

    private static void executeAction(Inventory inventory, IntList slots, ServerPlayerEntity player, BiFunction<Inventory, Integer, Boolean> viewHandler) {
        boolean succeed = false;
        int lastSlot = -1;
        FailureReason lastFailure = null;
        for (int i = 0; i < slots.size(); ++i) {
            int slot = slots.getInt(i);
            Optional<Either<Inventory, FailureReason>> optionalView = PackedInventoryEditRequest.getView(inventory, slot, player);
            if (optionalView.isEmpty()) {
                continue;
            }

            Either<Inventory, FailureReason> view = optionalView.get();
            if (view.left().isPresent()) {
                if (viewHandler.apply(view.left().get(), slot)) {
                    return;
                }

                succeed = true;
                continue;
            }

            if (view.right().isPresent()) {
                lastSlot = slot;
                lastFailure = view.right().get();
            }
        }

        if (!succeed && lastFailure != null) {
            PackedInventoryEditRequest.handleFailure(lastFailure, inventory, lastSlot, player);
        }
    }

    /**
     * Registers server receiver for this request.
     * @param receiver Request receiver.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, PackedInventoryBulkEditRequest::execute);
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
