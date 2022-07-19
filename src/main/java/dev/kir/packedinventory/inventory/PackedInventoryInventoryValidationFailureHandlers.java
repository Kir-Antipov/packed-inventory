package dev.kir.packedinventory.inventory;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandlerRegistry;
import net.minecraft.text.Text;

public final class PackedInventoryInventoryValidationFailureHandlers {
    private static InventoryValidationFailureHandlerRegistry.Entry DEFAULT;

    public static InventoryValidationFailureHandlerRegistry.Entry getDefault() {
        return DEFAULT;
    }

    @SuppressWarnings("unused")
    public static void init(InventoryValidationFailureHandlerRegistry registry, PackedInventoryApiConfig config) {
        DEFAULT = registerDefault(registry);
    }

    private static InventoryValidationFailureHandlerRegistry.Entry registerDefault(InventoryValidationFailureHandlerRegistry registry) {
        return registry.registerDefault((failureReason, inventory, i, player) -> {
            Text text = failureReason.toText();
            if (text != null) {
                player.sendMessage(text, true);
            }
        });
    }

    private PackedInventoryInventoryValidationFailureHandlers() { }
}
