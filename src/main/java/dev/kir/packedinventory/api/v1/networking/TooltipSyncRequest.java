package dev.kir.packedinventory.api.v1.networking;

import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.item.TooltipProviderContext;
import dev.kir.packedinventory.api.v1.item.TooltipProviderRegistry;
import dev.kir.packedinventory.api.v1.item.TooltipSyncDataProviderRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

/**
 * A class that can be used to send tooltip synchronization requests.
 */
public final class TooltipSyncRequest {
    /**
     * Identifier of this request.
     */
    public static final Identifier ID = PackedInventory.locate("tooltip_sync_request");

    /**
     * Sends a synchronization request for the given {@link ItemStack}.
     * @param stack {@link ItemStack} that requires synchronization.
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(ItemStack stack) {
        if (!ClientPlayNetworking.canSend(ID)) {
            return;
        }

        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeItemStack(stack);
        ClientPlayNetworking.send(ID, buffer);
    }

    private static void sendToClient(ServerPlayerEntity player, ItemStack stack, NbtCompound syncData) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeItemStack(stack);
        buffer.writeNbt(syncData);
        ServerPlayNetworking.send(player, ID, buffer);
    }

    private static void executeServer(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemStack stack = buf.readItemStack();
        TooltipSyncDataProviderRegistry.getInstance().getTooltipSyncData(stack, player).ifPresent(x -> {
            NbtCompound nbt = new NbtCompound();
            x.writeNbt(nbt);
            TooltipSyncRequest.sendToClient(player, stack, nbt);
        });
    }

    @Environment(EnvType.CLIENT)
    private static void executeClient(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemStack stack = buf.readItemStack();
        NbtCompound nbt = (NbtCompound)buf.readNbt(NbtTagSizeTracker.ofUnlimitedBytes());
        client.execute(() -> TooltipProviderRegistry.getInstance().updateTooltipSyncData(stack, TooltipProviderContext.of(), nbt));
    }

    /**
     * Registers server receiver for this request.
     * @param receiver Request receiver.
     */
    public static void registerServerReceiver(BiFunction<Identifier, ServerPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, TooltipSyncRequest::executeServer);
    }

    /**
     * Registers client receiver for this request.
     * @param receiver Request receiver.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClientReceiver(BiFunction<Identifier, ClientPlayNetworking.PlayChannelHandler, Boolean> receiver) {
        receiver.apply(ID, TooltipSyncRequest::executeClient);
    }

    private TooltipSyncRequest() { }
}
