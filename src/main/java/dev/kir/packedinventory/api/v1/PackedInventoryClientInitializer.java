package dev.kir.packedinventory.api.v1;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.item.TooltipProviderRegistry;

/**
 * A mod initializer ran only on {@link net.fabricmc.api.EnvType#CLIENT}.
 *
 * <p>This entrypoint is suitable for registering client-specific providers, such as {@link dev.kir.packedinventory.api.v1.item.TooltipProvider}, etc.</p>
 *
 * <p>In {@code fabric.mod.json}, the entrypoint is defined with {@code packed-inventory-client} key.</p>
 *
 * @see PackedInventoryInitializer
 */
public interface PackedInventoryClientInitializer {
    /**
     * Registers {@link dev.kir.packedinventory.api.v1.item.TooltipProvider}s.
     *
     * @param registry {@link TooltipProviderRegistry} instance.
     * @param config {@link PackedInventoryApiConfig} instance.
     */
    default void registerTooltipProviders(TooltipProviderRegistry registry, PackedInventoryApiConfig config) { }
}
