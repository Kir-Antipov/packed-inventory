package dev.kir.packedinventory.inventory;

import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryAction;
import dev.kir.packedinventory.api.v1.inventory.InventoryActionType;
import dev.kir.packedinventory.api.v1.inventory.InventoryActionTypeRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryActionTypes;
import net.minecraft.util.Identifier;

public final class PackedInventoryInventoryActionTypes {
    private static InventoryActionTypeRegistry.Entry<?> DEFAULT;

    public static InventoryActionTypeRegistry.Entry<?> getDefault() {
        return DEFAULT;
    }

    @SuppressWarnings("unused")
    public static void init(InventoryActionTypeRegistry registry, PackedInventoryApiConfig config) {
        DEFAULT = register(registry, "default", InventoryActionTypes.DEFAULT);
    }

    private static <T extends InventoryAction> InventoryActionTypeRegistry.Entry<T> register(InventoryActionTypeRegistry registry, String id, InventoryActionType<T> inventoryActionType) {
        return PackedInventoryInventoryActionTypes.register(registry, PackedInventory.locate(id), inventoryActionType);
    }

    private static <T extends InventoryAction> InventoryActionTypeRegistry.Entry<T> register(InventoryActionTypeRegistry registry, Identifier id, InventoryActionType<T> inventoryActionType) {
        return registry.register(id, inventoryActionType);
    }

    private PackedInventoryInventoryActionTypes() { }
}