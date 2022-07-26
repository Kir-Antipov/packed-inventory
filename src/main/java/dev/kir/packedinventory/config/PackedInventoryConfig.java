package dev.kir.packedinventory.config;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.compat.cloth.PackedInventoryClothConfig;
import net.fabricmc.loader.api.FabricLoader;

public interface PackedInventoryConfig extends PackedInventoryApiConfig {
    static PackedInventoryConfig resolve() {
        return FabricLoader.getInstance().isModLoaded("cloth-config") ? PackedInventoryClothConfig.getInstance() : PackedInventoryConfigImpl.getInstance();
    }
}
