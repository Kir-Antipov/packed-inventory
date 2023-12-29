package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.util.collection.ListUtil;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import dev.kir.packedinventory.util.inventory.InventoryViewUtil;
import dev.kir.packedinventory.util.network.PacketByteBufUtil;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

final class DefaultInventoryAction implements InventoryAction {
    public static final InventoryActionType<?> TYPE = InventoryActionType.create(DefaultInventoryAction::read, DefaultInventoryAction::write);

    private final List<Integer> slots;

    public DefaultInventoryAction(List<Integer> slots) {
        this.slots = slots;
    }

    @Override
    public boolean invoke(ServerPlayerEntity player) {
        return DefaultInventoryAction.invoke(player, ListUtil.copyOf(this.slots));
    }

    @Override
    public InventoryActionType<?> getType() {
        return TYPE;
    }

    private static boolean invoke(PlayerEntity player, IntList slots) {
        Inventory inventory = InventoryUtil.getPlayerInventoryAndNormalizeSlots(player, slots);
        return InventoryViewUtil.handleViews(inventory, slots, player);
    }

    private static DefaultInventoryAction read(PacketByteBuf buffer) {
        return new DefaultInventoryAction(buffer.readIntList());
    }

    private static void write(PacketByteBuf buffer, DefaultInventoryAction inventoryAction) {
        PacketByteBufUtil.writeIntegerList(buffer, inventoryAction.slots);
    }
}