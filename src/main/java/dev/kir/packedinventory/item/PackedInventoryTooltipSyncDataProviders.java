package dev.kir.packedinventory.item;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.item.GenericContainerTooltipSyncData;
import dev.kir.packedinventory.api.v1.item.TooltipSyncDataProviderRegistry;
import net.minecraft.item.Items;

public final class PackedInventoryTooltipSyncDataProviders {
    private static TooltipSyncDataProviderRegistry.Entry<GenericContainerTooltipSyncData> ENDER_CHEST;

    public static TooltipSyncDataProviderRegistry.Entry<GenericContainerTooltipSyncData> getEnderChest() {
        return ENDER_CHEST;
    }

    @SuppressWarnings("unused")
    public static void init(TooltipSyncDataProviderRegistry registry, PackedInventoryApiConfig config) {
        ENDER_CHEST = registerEnderChest(registry);
    }

    private static TooltipSyncDataProviderRegistry.Entry<GenericContainerTooltipSyncData> registerEnderChest(TooltipSyncDataProviderRegistry registry) {
        return registry.register(
            (stack, player) -> GenericContainerTooltipSyncData.of(player.getEnderChestInventory()),
            Items.ENDER_CHEST
        );
    }

    private PackedInventoryTooltipSyncDataProviders() { }
}
