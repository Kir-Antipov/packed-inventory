package dev.kir.packedinventory.inventory;

import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.screen.InventoryDependentScreenHandlerFactory;
import dev.kir.packedinventory.screen.ItemDamagingAnvilScreenHandler;
import dev.kir.packedinventory.screen.ScreenHandlerProxy;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public final class PackedInventoryInventoryViewHandlers {
    private static InventoryViewHandlerRegistry.Entry DEFAULT;
    private static InventoryViewHandlerRegistry.Entry CRAFTING_TABLE;
    private static InventoryViewHandlerRegistry.Entry CARTOGRAPHY_TABLE;
    private static InventoryViewHandlerRegistry.Entry STONECUTTER;
    private static InventoryViewHandlerRegistry.Entry LOOM;
    private static InventoryViewHandlerRegistry.Entry SMITHING_TABLE;
    private static InventoryViewHandlerRegistry.Entry GRINDSTONE;
    private static InventoryViewHandlerRegistry.Entry ANVIL;

    public static InventoryViewHandlerRegistry.Entry getDefault() {
        return DEFAULT;
    }

    public static InventoryViewHandlerRegistry.Entry getCraftingTable() {
        return CRAFTING_TABLE;
    }

    public static InventoryViewHandlerRegistry.Entry getCartographyTable() {
        return CARTOGRAPHY_TABLE;
    }

    public static InventoryViewHandlerRegistry.Entry getStonecutter() {
        return STONECUTTER;
    }

    public static InventoryViewHandlerRegistry.Entry getLoom() {
        return LOOM;
    }

    public static InventoryViewHandlerRegistry.Entry getSmithingTable() {
        return SMITHING_TABLE;
    }

    public static InventoryViewHandlerRegistry.Entry getGrindstone() {
        return GRINDSTONE;
    }

    public static InventoryViewHandlerRegistry.Entry getAnvil() {
        return ANVIL;
    }

    @SuppressWarnings("unused")
    public static void init(InventoryViewHandlerRegistry registry, PackedInventoryApiConfig config) {
        DEFAULT = registerDefault(registry);
        CRAFTING_TABLE = registerWorkStation(Blocks.CRAFTING_TABLE, registry);
        CARTOGRAPHY_TABLE = registerWorkStation(Blocks.CARTOGRAPHY_TABLE, registry);
        STONECUTTER = registerWorkStation(Blocks.STONECUTTER, registry);
        LOOM = registerWorkStation(Blocks.LOOM, registry);
        SMITHING_TABLE = registerWorkStation(Blocks.SMITHING_TABLE, registry);
        GRINDSTONE = registerWorkStation(Blocks.GRINDSTONE, registry);
        ANVIL = registerAnvil(registry);
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
                return;
            }

            InventoryDependentScreenHandlerFactory genericFactory = InventoryDependentScreenHandlerFactory.genericOfSize(inventory.size());
            if (genericFactory != null) {
                player.openHandledScreen(genericFactory.asNamedScreenHandlerFactory(inventory, name));
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static InventoryViewHandlerRegistry.Entry registerWorkStation(Block workStation, InventoryViewHandlerRegistry registry) {
        return registry.register(
            (__, playerInventory, slot, player) -> {
                NamedScreenHandlerFactory originalScreenFactory = workStation.createScreenHandlerFactory(workStation.getDefaultState(), player.world, player.getBlockPos());
                if (originalScreenFactory == null) {
                    return;
                }

                SimpleNamedScreenHandlerFactory screenFactory = new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> {
                        ScreenHandler handler = originalScreenFactory.createMenu(syncId, inv, p);
                        if (handler == null) {
                            return null;
                        }

                        return new ScreenHandlerProxy(handler) {
                            @Override
                            public boolean canUse(PlayerEntity player) {
                                return true;
                            }
                        };
                    },
                    originalScreenFactory.getDisplayName()
                );
                player.openHandledScreen(screenFactory);
            },
            workStation.asItem()
        );
    }

    private static InventoryViewHandlerRegistry.Entry registerAnvil(InventoryViewHandlerRegistry registry) {
        return registry.register(
            (__, playerInventory, slot, player) -> {
                ItemStack anvilStack = playerInventory.getStack(slot);
                SimpleNamedScreenHandlerFactory screenFactory = new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> new ItemDamagingAnvilScreenHandler(syncId, anvilStack, p),
                    ItemDamagingAnvilScreenHandler.TITLE
                );
                player.openHandledScreen(screenFactory);
            },
            Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL
        );
    }

    private PackedInventoryInventoryViewHandlers() { }
}