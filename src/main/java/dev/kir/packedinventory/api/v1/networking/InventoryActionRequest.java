package dev.kir.packedinventory.api.v1.networking;

import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.inventory.InventoryAction;
import dev.kir.packedinventory.api.v1.inventory.InventoryActionTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Represents a request to perform an inventory action.
 */
public final class InventoryActionRequest {
    /**
     * The identifier of this request.
     */
    public static final Identifier ID = PackedInventory.locate("inventory_action_request");

    /**
     * Sends an inventory action request to the server.
     * @param inventoryAction The inventory action to send.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(InventoryAction inventoryAction) {
        if (!ClientPlayNetworking.canSend(ID)) {
            return;
        }

        PacketByteBuf buffer = PacketByteBufs.create();
        if (!InventoryActionTypeRegistry.getInstance().write(buffer, inventoryAction)) {
            return;
        }

        ClientPlayNetworking.send(ID, buffer);
    }

    private static void execute(MinecraftServer server, ServerPlayerEntity player, PacketByteBuf buffer) {
        Optional<InventoryAction> optionalInventoryAction = InventoryActionTypeRegistry.getInstance().read(buffer);
        if (optionalInventoryAction.isEmpty()) {
            return;
        }

        InventoryAction inventoryAction = optionalInventoryAction.get();
        server.execute(() -> inventoryAction.invoke(player));
    }

    /**
     * Registers a server receiver for this request.
     * @param receiver The receiver to be registered.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, (server, player, __, buffer, ___) -> execute(server, player, buffer));
    }

    private InventoryActionRequest() { }
}
