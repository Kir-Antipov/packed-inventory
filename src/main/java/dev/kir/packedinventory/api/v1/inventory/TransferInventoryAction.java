package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.util.collection.ListUtil;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import dev.kir.packedinventory.util.inventory.InventoryViewUtil;
import dev.kir.packedinventory.util.network.PacketByteBufUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumSet;
import java.util.List;

final class TransferInventoryAction implements InventoryAction {
    public static final InventoryActionType<?> TYPE = InventoryActionType.create(TransferInventoryAction::read, TransferInventoryAction::write);

    private final List<Integer> fromSlots;

    private final List<Integer> toSlots;

    private final EnumSet<InventoryTransferOptions> options;

    public TransferInventoryAction(List<Integer> fromSlots, List<Integer> toSlots, EnumSet<InventoryTransferOptions> options) {
        this.fromSlots = fromSlots;
        this.toSlots = toSlots;
        this.options = options;
    }

    @Override
    public boolean invoke(ServerPlayerEntity player) {
        return TransferInventoryAction.invoke(player, ListUtil.copyOf(this.fromSlots), ListUtil.copyOf(this.toSlots), this.options);
    }

    @Override
    public InventoryActionType<?> getType() {
        return TYPE;
    }

    private static boolean invoke(PlayerEntity player, IntList fromSlots, IntList toSlots, EnumSet<InventoryTransferOptions> options) {
        if (options.isEmpty()) {
            return InventoryViewUtil.transferViews(InventoryUtil.getPlayerInventoryAndNormalizeSlots(player, fromSlots, toSlots), fromSlots, toSlots, player);
        }

        Inventory inventory;
        boolean shouldSwapSlots;
        if (options.contains(InventoryTransferOptions.PREFER_INSERTION)) {
            IntList toCursorSlots = indexOfCursorSlots(toSlots);
            inventory = InventoryUtil.getPlayerInventoryAndNormalizeSlots(player, fromSlots, toSlots);
            shouldSwapSlots = hasNonViewCursor(inventory, toSlots, toCursorSlots, player);
        } else if (options.contains(InventoryTransferOptions.PREFER_EXTRACTION)) {
            IntList fromCursorSlots = indexOfCursorSlots(fromSlots);
            inventory = InventoryUtil.getPlayerInventoryAndNormalizeSlots(player, fromSlots, toSlots);
            shouldSwapSlots = hasNonViewCursor(inventory, fromSlots, fromCursorSlots, player);
        } else {
            inventory = InventoryUtil.getPlayerInventoryAndNormalizeSlots(player, fromSlots, toSlots);
            shouldSwapSlots = false;
        }

        if (shouldSwapSlots) {
            return InventoryViewUtil.transferViews(inventory, toSlots, fromSlots, player);
        }

        return InventoryViewUtil.transferViews(inventory, fromSlots, toSlots, player);
    }

    private static TransferInventoryAction read(PacketByteBuf buffer) {
        IntList fromSlots = buffer.readIntList();
        IntList toSlots = buffer.readIntList();
        EnumSet<InventoryTransferOptions> options = buffer.readEnumSet(InventoryTransferOptions.class);
        return new TransferInventoryAction(fromSlots, toSlots, options);
    }

    private static void write(PacketByteBuf buffer, TransferInventoryAction inventoryAction) {
        PacketByteBufUtil.writeIntegerList(buffer, inventoryAction.fromSlots);
        PacketByteBufUtil.writeIntegerList(buffer, inventoryAction.toSlots);
        buffer.writeEnumSet(inventoryAction.options, InventoryTransferOptions.class);
    }

    private static IntList indexOfCursorSlots(IntList slots) {
        int firstIndex = slots.indexOf(InventoryAction.CURSOR_SLOT);
        if (firstIndex < 0) {
            return IntList.of();
        }

        IntArrayList cursorSlots = IntArrayList.wrap(new int[] { firstIndex });
        for (int i = firstIndex + 1; i < slots.size(); i++) {
            if (slots.getInt(i) == InventoryAction.CURSOR_SLOT) {
                cursorSlots.add(i);
            }
        }
        return cursorSlots;
    }

    private static boolean hasNonViewCursor(Inventory inventory, IntList slots, IntList cursorSlots, PlayerEntity player) {
        for (int i = cursorSlots.size() - 1; i >= 0; i--) {
            if (!InventoryViewUtil.isNonEmptyView(inventory, slots.getInt(cursorSlots.getInt(i)), player)) {
                return true;
            }
        }
        return false;
    }
}