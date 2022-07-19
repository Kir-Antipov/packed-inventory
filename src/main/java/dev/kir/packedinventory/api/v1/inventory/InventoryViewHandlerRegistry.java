package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link InventoryViewHandler} registry.
 */
public interface InventoryViewHandlerRegistry {
    /**
     * @return {@link InventoryViewHandlerRegistry} instance.
     */
    @ApiStatus.Internal
    static InventoryViewHandlerRegistry getInstance() {
        return InventoryViewHandlerRegistryImpl.INSTANCE;
    }


    /**
     * Handles an inventory view.
     * @param inventory Inventory view to be handled.
     * @param parentInventory Parent inventory of the given {@code inventory}.
     * @param slot Slot index where {@code inventory} is located in the {@code parentInventory}.
     * @param player Player.
     * @return {@code true} if the inventory view was handled; otherwise, {@code false}.
     */
    boolean handle(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player);


    /**
     * Registers the given {@code entry} in the registry.
     * @param entry Entry to be registered.
     * @return Registered entry.
     */
    Entry register(Entry entry);

    /**
     * Registers the provided {@code handler}.
     * @param handler Handler to be registered.
     * @param predicate Predicate that should pass in order for the {@code handler} to be used.
     * @return Registered entry.
     */
    default Entry register(InventoryViewHandler handler, InventoryViewHandler.Predicate predicate) {
        return this.register(new Entry() {
            @Override
            public InventoryViewHandler getHandler() {
                return handler;
            }

            @Override
            public InventoryViewHandler.Predicate getPredicate() {
                return predicate;
            }
        });
    }

    /**
     * Registers the provided {@code handler}.
     * @param handler Handler to be registered.
     * @param item Item this {@code handler} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewHandler handler, Item item) {
        return this.register(handler, List.of(item));
    }

    /**
     * Registers the provided {@code handler}.
     * @param handler Handler to be registered.
     * @param items Items this {@code handler} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewHandler handler, Item... items) {
        return this.register(handler, Arrays.asList(items));
    }

    /**
     * Registers the provided {@code handler}.
     * @param handler Handler to be registered.
     * @param items Items this {@code handler} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewHandler handler, Collection<Item> items) {
        return this.register(handler, InventoryViewHandler.ItemPredicate.of(items));
    }

    /**
     * Registers the provided {@code handler} as a default handler.
     * @param handler Handler to be registered.
     * @return Registered entry.
     */
    default Entry registerDefault(InventoryViewHandler handler) {
        return this.register(handler, InventoryViewHandler.Predicate.TRUE);
    }


    /**
     * Unregisters the given {@code handler}.
     * @param handler Handler to be unregistered.
     * @return {@code true} if the {@code handler} was unregistered; otherwise, {@code false}.
     */
    boolean unregister(InventoryViewHandler handler);

    /**
     * Unregisters the given {@code entry}.
     * @param entry Entry to be unregistered.
     * @return {@code true} if the {@code entry} was unregistered; otherwise, {@code false}.
     */
    default boolean unregister(Entry entry) {
        return this.unregister(entry.getHandler());
    }


    /**
     * {@link InventoryViewHandlerRegistry} entry.
     */
    interface Entry {
        /**
         * @return {@link InventoryViewHandler} instance.
         */
        InventoryViewHandler getHandler();

        /**
         * @return Predicate that should pass in order for the {@code handler} to be used.
         */
        InventoryViewHandler.Predicate getPredicate();
    }
}
