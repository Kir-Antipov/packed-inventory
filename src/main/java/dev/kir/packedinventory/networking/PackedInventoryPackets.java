package dev.kir.packedinventory.networking;

import dev.kir.packedinventory.api.v1.networking.PackedInventoryBulkEditRequest;
import dev.kir.packedinventory.api.v1.networking.PackedInventoryEditRequest;
import dev.kir.packedinventory.api.v1.networking.TooltipSyncRequest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class PackedInventoryPackets {
    public static void init() {
        TooltipSyncRequest.registerServerReceiver(ServerPlayNetworking::registerGlobalReceiver);
        PackedInventoryEditRequest.registerServerReceiver(ServerPlayNetworking::registerGlobalReceiver);
        PackedInventoryBulkEditRequest.registerServerReceiver(ServerPlayNetworking::registerGlobalReceiver);
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        TooltipSyncRequest.registerClientReceiver(ClientPlayNetworking::registerGlobalReceiver);
    }

    private PackedInventoryPackets() { }
}
