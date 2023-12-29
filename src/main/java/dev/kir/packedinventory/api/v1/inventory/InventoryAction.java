package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.networking.InventoryActionRequest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumSet;
import java.util.List;

public interface InventoryAction {
    int CURSOR_SLOT = -1;

    @Environment(EnvType.CLIENT)
    default void invoke() {
        InventoryActionRequest.sendToServer(this);
    }

    boolean invoke(ServerPlayerEntity player);

    InventoryActionType<?> getType();
}