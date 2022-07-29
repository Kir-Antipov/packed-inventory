package dev.kir.packedinventory;

import dev.kir.packedinventory.api.v1.PackedInventoryClientInitializer;
import dev.kir.packedinventory.api.v1.PackedInventoryInitializer;
import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewerRegistry;
import dev.kir.packedinventory.api.v1.item.TooltipProviderRegistry;
import dev.kir.packedinventory.api.v1.item.TooltipSyncDataProviderRegistry;
import dev.kir.packedinventory.client.gui.tooltip.PackedInventoryTooltipComponents;
import dev.kir.packedinventory.client.item.PackedInventoryTooltipProviders;
import dev.kir.packedinventory.config.PackedInventoryConfig;
import dev.kir.packedinventory.client.input.PackedInventoryKeyBindings;
import dev.kir.packedinventory.inventory.PackedInventoryInventoryValidationFailureHandlers;
import dev.kir.packedinventory.inventory.PackedInventoryInventoryViewHandlers;
import dev.kir.packedinventory.inventory.PackedInventoryInventoryViewers;
import dev.kir.packedinventory.item.PackedInventoryTooltipSyncDataProviders;
import dev.kir.packedinventory.networking.PackedInventoryPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class PackedInventory implements ModInitializer, ClientModInitializer, PackedInventoryInitializer, PackedInventoryClientInitializer {
    public static final String MOD_ID = "packed-inventory";
    private static final String ENTRYPOINT = MOD_ID;
    private static final String ENTRYPOINT_CLIENT = ENTRYPOINT + "-client";
    private static final PackedInventoryConfig CONFIG = PackedInventoryConfig.resolve();

    public static Identifier locate(String location) {
        return new Identifier(MOD_ID, location);
    }

    public static PackedInventoryConfig getConfig() {
        return CONFIG;
    }

    @Override
    public void onInitialize() {
        PackedInventoryPackets.init();

        for (PackedInventoryInitializer initializer : FabricLoader.getInstance().getEntrypoints(ENTRYPOINT, PackedInventoryInitializer.class)) {
            initializer.registerInventoryViewers(InventoryViewerRegistry.getInstance(), CONFIG);
            initializer.registerInventoryViewHandlers(InventoryViewHandlerRegistry.getInstance(), CONFIG);
            initializer.registerInventoryValidationFailureHandlers(InventoryValidationFailureHandlerRegistry.getInstance(), CONFIG);
            initializer.registerTooltipSyncDataProviders(TooltipSyncDataProviderRegistry.getInstance(), CONFIG);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        PackedInventoryPackets.initClient();
        PackedInventoryKeyBindings.initClient();
        PackedInventoryTooltipComponents.initClient();

        for (PackedInventoryClientInitializer initializer : FabricLoader.getInstance().getEntrypoints(ENTRYPOINT_CLIENT, PackedInventoryClientInitializer.class)) {
            initializer.registerTooltipProviders(TooltipProviderRegistry.getInstance(), CONFIG);
        }
    }

    @Override
    public void registerInventoryViewers(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        PackedInventoryInventoryViewers.init(registry, config);
    }

    @Override
    public void registerInventoryViewHandlers(InventoryViewHandlerRegistry registry, PackedInventoryApiConfig config) {
        PackedInventoryInventoryViewHandlers.init(registry, config);
    }

    @Override
    public void registerInventoryValidationFailureHandlers(InventoryValidationFailureHandlerRegistry registry, PackedInventoryApiConfig config) {
        PackedInventoryInventoryValidationFailureHandlers.init(registry, config);
    }

    @Override
    public void registerTooltipSyncDataProviders(TooltipSyncDataProviderRegistry registry, PackedInventoryApiConfig config) {
        PackedInventoryTooltipSyncDataProviders.init(registry, config);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void registerTooltipProviders(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        PackedInventoryTooltipProviders.initClient(registry, config);
    }
}
