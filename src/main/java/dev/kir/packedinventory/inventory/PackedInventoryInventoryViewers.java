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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.util.DyeColor;

import java.util.Arrays;
import java.util.stream.Stream;

public final class PackedInventoryInventoryViewers {
    private static InventoryViewerRegistry.Entry SHULKER_BOX;
    private static InventoryViewerRegistry.Entry ENDER_CHEST;
    private static InventoryViewerRegistry.Entry CRAFTING_TABLE;
    private static InventoryViewerRegistry.Entry CARTOGRAPHY_TABLE;
    private static InventoryViewerRegistry.Entry STONECUTTER;
    private static InventoryViewerRegistry.Entry LOOM;
    private static InventoryViewerRegistry.Entry SMITHING_TABLE;
    private static InventoryViewerRegistry.Entry GRINDSTONE;
    private static InventoryViewerRegistry.Entry ANVIL;

    public static InventoryViewerRegistry.Entry getShulkerBox() {
        return SHULKER_BOX;
    }

    public static InventoryViewerRegistry.Entry getEnderChest() {
        return ENDER_CHEST;
    }

    public static InventoryViewerRegistry.Entry getCraftingTable() {
        return CRAFTING_TABLE;
    }

    public static InventoryViewerRegistry.Entry getCartographyTable() {
        return CARTOGRAPHY_TABLE;
    }

    public static InventoryViewerRegistry.Entry getStonecutter() {
        return STONECUTTER;
    }

    public static InventoryViewerRegistry.Entry getLoom() {
        return LOOM;
    }

    public static InventoryViewerRegistry.Entry getSmithingTable() {
        return SMITHING_TABLE;
    }

    public static InventoryViewerRegistry.Entry getGrindstone() {
        return GRINDSTONE;
    }

    public static InventoryViewerRegistry.Entry getAnvil() {
        return ANVIL;
    }

    public static void init(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        SHULKER_BOX = registerShulkerBox(registry, config);
        ENDER_CHEST = registerEnderChest(registry, config);

        CRAFTING_TABLE = registerWorkStation(Items.CRAFTING_TABLE, registry, config);
        CARTOGRAPHY_TABLE = registerWorkStation(Items.CARTOGRAPHY_TABLE, registry, config);
        STONECUTTER = registerWorkStation(Items.STONECUTTER, registry, config);
        LOOM = registerWorkStation(Items.LOOM, registry, config);
        SMITHING_TABLE = registerWorkStation(Items.SMITHING_TABLE, registry, config);
        GRINDSTONE = registerWorkStation(Items.GRINDSTONE, registry, config);
        ANVIL = registerAnvil(registry, config);
    }

    private static InventoryViewerRegistry.Entry registerShulkerBox(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        config.registerValidationConfig(Items.SHULKER_BOX, GenericValidationConfig::new);

        InventoryViewer.Validator isEnabled = InventoryValidators.config(Items.SHULKER_BOX, config, ValidationConfig.DEFAULT, ValidationConfig::isEnabled);
        InventoryViewer.Validator isInCreative = InventoryValidators.config(Items.SHULKER_BOX, config, GenericValidationConfig.DEFAULT, GenericValidationConfig::isSuppressedInCreative).and(InventoryValidators.IS_IN_CREATIVE);
        InventoryViewer.Validator isSingleItem = InventoryValidators.SINGLE_ITEM;
        InventoryViewer.Validator isOnGround = InventoryValidators.config(Items.SHULKER_BOX, config, GenericValidationConfig.DEFAULT, x -> !x.requiresPlayerOnGround()).or(InventoryValidators.IS_ON_GROUND);
        return registry.register(
            (inventory, slot, player) -> NbtItemsInventory.create(inventory, slot, player, ShulkerBoxScreenHandler::new),
            isEnabled.and(isInCreative.or(isSingleItem.and(isOnGround))),
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

    private static InventoryViewerRegistry.Entry registerWorkStation(Item workStation, InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        config.registerValidationConfig(workStation, GenericValidationConfig::new);

        InventoryViewer.Validator isEnabled = InventoryValidators.config(workStation, config, ValidationConfig.DEFAULT, ValidationConfig::isEnabled);
        InventoryViewer.Validator isInCreative = InventoryValidators.config(workStation, config, GenericValidationConfig.DEFAULT, GenericValidationConfig::isSuppressedInCreative).and(InventoryValidators.IS_IN_CREATIVE);
        InventoryViewer.Validator isOnGround = InventoryValidators.config(workStation, config, GenericValidationConfig.DEFAULT, x -> !x.requiresPlayerOnGround()).or(InventoryValidators.IS_ON_GROUND);
        return registry.register(
            (inventory, slot, player) -> EmptyInventory.getInstance(),
            isEnabled.and(isInCreative.or(isOnGround)),
            workStation
        );
    }

    private static InventoryViewerRegistry.Entry registerAnvil(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        config.registerValidationConfig(Items.ANVIL, GenericValidationConfig::new);

        InventoryViewer.Validator isEnabled = InventoryValidators.config(Items.ANVIL, config, ValidationConfig.DEFAULT, ValidationConfig::isEnabled);
        InventoryViewer.Validator isInCreative = InventoryValidators.config(Items.ANVIL, config, GenericValidationConfig.DEFAULT, GenericValidationConfig::isSuppressedInCreative).and(InventoryValidators.IS_IN_CREATIVE);
        InventoryViewer.Validator isSingleItem = InventoryValidators.SINGLE_ITEM;
        InventoryViewer.Validator isOnGround = InventoryValidators.config(Items.ANVIL, config, GenericValidationConfig.DEFAULT, x -> !x.requiresPlayerOnGround()).or(InventoryValidators.IS_ON_GROUND);
        return registry.register(
            (inventory, slot, player) -> EmptyInventory.getInstance(),
            isEnabled.and(isInCreative.or(isSingleItem.and(isOnGround))),
            Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL
        );
    }

    private PackedInventoryInventoryViewers() { }
}
