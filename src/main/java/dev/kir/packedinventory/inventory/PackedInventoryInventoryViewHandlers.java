package dev.kir.packedinventory.inventory;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.screen.InventoryDependentScreenHandlerFactory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public final class PackedInventoryInventoryViewHandlers {
    private static InventoryViewHandlerRegistry.Entry DEFAULT;

    public static InventoryViewHandlerRegistry.Entry getDefault() {
        return DEFAULT;
    }

    @SuppressWarnings("unused")
    public static void init(InventoryViewHandlerRegistry registry, PackedInventoryApiConfig config) {
        DEFAULT = registerDefault(registry);
    }

    private static InventoryViewHandlerRegistry.Entry registerDefault(InventoryViewHandlerRegistry registry) {
        return registry.registerDefault((inventory, parentInventory, slot, player) -> {
            if (inventory instanceof NamedScreenHandlerFactory) {
                player.openHandledScreen((NamedScreenHandlerFactory)inventory);
                return;
            }

            Text name = parentInventory.getStack(slot).getName();
            if (inventory instanceof ScreenHandlerFactory) {
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((ScreenHandlerFactory)inventory, name));
            }

            InventoryDependentScreenHandlerFactory genericFactory = InventoryDependentScreenHandlerFactory.genericOfSize(inventory.size());
            if (genericFactory != null) {
                player.openHandledScreen(genericFactory.asNamedScreenHandlerFactory(inventory, name));
            }
        });
    }

    private PackedInventoryInventoryViewHandlers() { }
}