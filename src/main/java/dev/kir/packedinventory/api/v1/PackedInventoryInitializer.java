package dev.kir.packedinventory.api.v1;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewerRegistry;
import dev.kir.packedinventory.api.v1.item.TooltipSyncDataProviderRegistry;

/**
 * A mod initializer ran on any environment.
 *
 * <p>In {@code fabric.mod.json}, the entrypoint is defined with {@code packed-inventory} key.</p>
 *
 * @see PackedInventoryClientInitializer
 */
public interface PackedInventoryInitializer {
    /**
     * Registers {@link dev.kir.packedinventory.api.v1.inventory.InventoryViewer}s.
     *
     * @param registry {@link InventoryViewerRegistry} instance.
     * @param config {@link PackedInventoryApiConfig} instance.
     */
    default void registerInventoryViewers(InventoryViewerRegistry registry, PackedInventoryApiConfig config) { }

    /**
     * Registers {@link dev.kir.packedinventory.api.v1.inventory.InventoryViewHandler}s.
     *
     * @param registry {@link InventoryViewHandlerRegistry} instance.
     * @param config {@link PackedInventoryApiConfig} instance.
     */
    default void registerInventoryViewHandlers(InventoryViewHandlerRegistry registry, PackedInventoryApiConfig config) { }

    /**
     * Registers {@link dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandler}s.
     *
     * @param registry {@link InventoryValidationFailureHandlerRegistry} instance.
     * @param config {@link PackedInventoryApiConfig} instance.
     */
    default void registerInventoryValidationFailureHandlers(InventoryValidationFailureHandlerRegistry registry, PackedInventoryApiConfig config) { }

    /**
     * Registers {@link dev.kir.packedinventory.api.v1.item.TooltipSyncDataProvider}s.
     *
     * @param registry {@link TooltipSyncDataProviderRegistry} instance.
     * @param config {@link PackedInventoryApiConfig} instance.
     */
    default void registerTooltipSyncDataProviders(TooltipSyncDataProviderRegistry registry, PackedInventoryApiConfig config) { }
}
