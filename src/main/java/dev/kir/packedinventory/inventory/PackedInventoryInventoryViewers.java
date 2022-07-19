package dev.kir.packedinventory.inventory;

import dev.kir.packedinventory.api.v1.config.GenericValidationConfig;
import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.config.SilkTouchableGenericValidationConfig;
import dev.kir.packedinventory.api.v1.config.ValidationConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidators;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewer;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.Items;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.util.DyeColor;

import java.util.Arrays;
import java.util.stream.Stream;

public final class PackedInventoryInventoryViewers {
    private static InventoryViewerRegistry.Entry SHULKER_BOX;
    private static InventoryViewerRegistry.Entry ENDER_CHEST;

    public static InventoryViewerRegistry.Entry getShulkerBox() {
        return SHULKER_BOX;
    }

    public static InventoryViewerRegistry.Entry getEnderChest() {
        return ENDER_CHEST;
    }

    public static void init(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        SHULKER_BOX = registerShulkerBox(registry, config);
        ENDER_CHEST = registerEnderChest(registry, config);
    }

    private static InventoryViewerRegistry.Entry registerShulkerBox(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        config.registerValidationConfig(Items.SHULKER_BOX, GenericValidationConfig::new);

        InventoryViewer.Validator isEnabled = InventoryValidators.config(Items.SHULKER_BOX, config, ValidationConfig.DEFAULT, ValidationConfig::isEnabled);
        InventoryViewer.Validator isInCreative = InventoryValidators.config(Items.SHULKER_BOX, config, GenericValidationConfig.DEFAULT, GenericValidationConfig::isSuppressedInCreative).and(InventoryValidators.IS_IN_CREATIVE);
        InventoryViewer.Validator isOnGround = InventoryValidators.config(Items.SHULKER_BOX, config, GenericValidationConfig.DEFAULT, x -> !x.requiresPlayerOnGround()).or(InventoryValidators.IS_ON_GROUND);
        return registry.register(
            (inventory, slot, player) -> NbtItemsInventory.create(inventory, slot, player, ShulkerBoxScreenHandler::new),
            isEnabled.and(isInCreative.or(isOnGround)),
            Stream.concat(Arrays.stream(DyeColor.values()), Stream.of((DyeColor)null)).map(ShulkerBoxBlock::get).map(Block::asItem).toList()
        );
    }

    private static InventoryViewerRegistry.Entry registerEnderChest(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        config.registerValidationConfig(Items.ENDER_CHEST, SilkTouchableGenericValidationConfig::new);

        InventoryViewer.Validator isEnabled = InventoryValidators.config(Items.ENDER_CHEST, config, ValidationConfig.DEFAULT, ValidationConfig::isEnabled);
        InventoryViewer.Validator isInCreative = InventoryValidators.config(Items.ENDER_CHEST, config, GenericValidationConfig.DEFAULT, GenericValidationConfig::isSuppressedInCreative).and(InventoryValidators.IS_IN_CREATIVE);
        InventoryViewer.Validator isOnGround = InventoryValidators.config(Items.ENDER_CHEST, config, GenericValidationConfig.DEFAULT, x -> !x.requiresPlayerOnGround()).or(InventoryValidators.IS_ON_GROUND);
        InventoryViewer.Validator hasSilkTouch = InventoryValidators.config(Items.ENDER_CHEST, config, SilkTouchableGenericValidationConfig.DEFAULT, x -> !x.requiresSilkTouch()).or(InventoryValidators.HAS_PICKAXE_WITH_SILK_TOUCH);
        return registry.register(
            (inventory, slot, player) -> player.getEnderChestInventory(),
            isEnabled.and(isInCreative.or(isOnGround.and(hasSilkTouch))),
            Items.ENDER_CHEST
        );
    }

    private PackedInventoryInventoryViewers() { }
}
