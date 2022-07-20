package dev.kir.packedinventory.client.item;

import dev.kir.packedinventory.api.v1.config.GenericSyncedTooltipConfig;
import dev.kir.packedinventory.api.v1.config.GenericTooltipConfig;
import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.item.*;
import dev.kir.packedinventory.config.FilledMapTooltipConfig;
import dev.kir.packedinventory.inventory.ListInventory;
import dev.kir.packedinventory.item.FilledMapTooltipData;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import dev.kir.packedinventory.util.item.ItemUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

import java.util.Arrays;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class PackedInventoryTooltipProviders {
    private static TooltipProviderRegistry.Entry SHULKER_BOX;
    private static TooltipProviderRegistry.Entry ENDER_CHEST;
    private static TooltipProviderRegistry.Entry FILLED_MAP;
    private static TooltipProviderRegistry.Entry DEFAULT;

    public static TooltipProviderRegistry.Entry getShulkerBox() {
        return SHULKER_BOX;
    }

    public static TooltipProviderRegistry.Entry getEnderChest() {
        return ENDER_CHEST;
    }

    public static TooltipProviderRegistry.Entry getFilledMap() {
        return FILLED_MAP;
    }

    public static TooltipProviderRegistry.Entry getDefault() {
        return DEFAULT;
    }

    public static void initClient(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        SHULKER_BOX = registerShulkerBox(registry, config);
        ENDER_CHEST = registerEnderChest(registry, config);
        FILLED_MAP = registerFilledMap(registry, config);
        DEFAULT = registerDefault(registry, config);
    }

    private static TooltipProviderRegistry.Entry registerShulkerBox(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        config.registerTooltipConfig(Items.SHULKER_BOX, GenericTooltipConfig::new);
        return registry.register(
            TooltipProvider.builder()
                .when((s, c) -> config.getTooltipConfigOrDefault(Items.SHULKER_BOX).isEnabled())
                .modifyTooltipText((text, stack, context) -> text.clear(TooltipText.Part.ADDITIONAL))
                .tooltipData((stack, context) -> {
                    GenericTooltipConfig cfg = config.getTooltipConfigOrDefault(Items.SHULKER_BOX);
                    Inventory inventory = ListInventory.wrap(cfg.isCompact() ? InventoryUtil.zip(InventoryUtil.getInventory(stack)) : InventoryUtil.getInventory(stack));
                    if (!cfg.shouldShowWhenEmpty() && inventory.isEmpty()) {
                        return null;
                    }

                    DyeColor color = cfg.usePredefinedColor() ? ItemUtil.getColor(stack) : null;
                    if (color == null) {
                        color = cfg.color();
                    }
                    return GenericContainerTooltipData.of(inventory, cfg.rows(), cfg.columns(), color);
                })
                .build(),
            Stream.concat(Arrays.stream(DyeColor.values()), Stream.of((DyeColor)null)).map(ShulkerBoxBlock::get).map(Block::asItem).toList()
        );
    }

    private static TooltipProviderRegistry.Entry registerEnderChest(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        config.registerTooltipConfig(Items.ENDER_CHEST, GenericSyncedTooltipConfig::new);
        return registry.register(
            TooltipProvider.builder()
                .when((s, c) -> config.getTooltipConfigOrDefault(Items.ENDER_CHEST).isEnabled())
                .useSyncData(GenericContainerTooltipSyncData::of, ItemStackComponentStorage.singleton(Items.ENDER_CHEST))
                .tooltipData((stack, context, data) -> {
                    GenericTooltipConfig cfg = config.getTooltipConfigOrDefault(Items.ENDER_CHEST);
                    Inventory inventory = cfg.isCompact() ? InventoryUtil.zip(data.getInventory()) : data.getInventory();
                    if (!cfg.shouldShowWhenEmpty() && inventory.isEmpty()) {
                        return null;
                    }

                    DyeColor color = cfg.usePredefinedColor() ? ItemUtil.getColor(stack) : null;
                    if (color == null) {
                        color = cfg.color();
                    }
                    return GenericContainerTooltipData.of(inventory, cfg.rows(), cfg.columns(), color);
                })
                .syncInterval(s -> config.getTooltipConfigOrDefault(Items.ENDER_CHEST, GenericSyncedTooltipConfig.DEFAULT).syncInterval())
                .build(),
            Items.ENDER_CHEST
        );
    }

    private static TooltipProviderRegistry.Entry registerFilledMap(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        config.registerTooltipConfig(Items.FILLED_MAP, FilledMapTooltipConfig::new);
        return registry.register(
            TooltipProvider.builder()
                .when((s, c) -> config.getTooltipConfigOrDefault(Items.FILLED_MAP, FilledMapTooltipConfig.DEFAULT).isEnabled())
                .tooltipData((stack, context) -> {
                    if (context.getPlayer() == null) {
                        return null;
                    }

                    FilledMapTooltipConfig cfg = config.getTooltipConfigOrDefault(Items.FILLED_MAP, FilledMapTooltipConfig.DEFAULT);
                    return new FilledMapTooltipData(stack, context.getPlayer().world, cfg.isCompact(), cfg.size());
                })
                .build(),
            Items.FILLED_MAP
        );
    }

    private static TooltipProviderRegistry.Entry registerDefault(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        return registry.registerDefault(
            TooltipProvider.builder()
                .when((s, c) -> ItemUtil.isInventory(s) && config.getTooltipConfigOrDefault(s.getItem()).isEnabled())
                .modifyTooltipText((text, stack, context) -> text.clear(TooltipText.Part.LORE))
                .tooltipData((stack, context) -> {
                    GenericTooltipConfig cfg = config.getTooltipConfigOrDefault(stack.getItem());
                    Inventory inventory = ListInventory.wrap(cfg.isCompact() ? InventoryUtil.zip(InventoryUtil.getInventory(stack)) : InventoryUtil.getInventory(stack));
                    if (!cfg.shouldShowWhenEmpty() && inventory.isEmpty()) {
                        return null;
                    }

                    DyeColor color = cfg.usePredefinedColor() ? ItemUtil.getColor(stack) : null;
                    if (color == null) {
                        color = cfg.color();
                    }
                    return GenericContainerTooltipData.of(inventory, cfg.rows(), cfg.columns(), color);
                })
            .build()
        );
    }

    private PackedInventoryTooltipProviders() { }
}
