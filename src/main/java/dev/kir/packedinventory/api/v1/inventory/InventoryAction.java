package dev.kir.packedinventory.api.v1.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumSet;
import java.util.List;

public interface InventoryAction {
    int CURSOR_SLOT = -1;

    boolean invoke(ServerPlayerEntity player);

    InventoryActionType<?> getType();
}