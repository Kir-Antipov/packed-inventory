package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.FailureReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link InventoryValidationFailureHandler} registry.
 */
public interface InventoryValidationFailureHandlerRegistry {
    /**
     * @return {@link InventoryValidationFailureHandlerRegistry} instance.
     */
    @ApiStatus.Internal
    static InventoryValidationFailureHandlerRegistry getInstance() {
        return InventoryValidationFailureHandlerRegistryImpl.INSTANCE;
    }


    /**
     * Handles a failure that occurred when trying to open an inventory view.
     * @param failureReason Failure reason.
     * @param inventory Inventory.
     * @param slot Inventory slot.
     * @param player Player.
     * @return {@code true} if the failure was handled; otherwise, {@code false}.
     */
    boolean handle(FailureReason failureReason, Inventory inventory, int slot, PlayerEntity player);


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
    default Entry register(InventoryValidationFailureHandler handler, InventoryValidationFailureHandler.Predicate predicate) {
        return this.register(new Entry() {
            @Override
            public InventoryValidationFailureHandler getHandler() {
                return handler;
            }

            @Override
            public InventoryValidationFailureHandler.Predicate getPredicate() {
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
    default Entry register(InventoryValidationFailureHandler handler, Item item) {
        return this.register(handler, List.of(item));
    }

    /**
     * Registers the provided {@code handler}.
     * @param handler Handler to be registered.
     * @param items Items this {@code handler} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryValidationFailureHandler handler, Item... items) {
        return this.register(handler, Arrays.asList(items));
    }

    /**
     * Registers the provided {@code handler}.
     * @param handler Handler to be registered.
     * @param items Items this {@code handler} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryValidationFailureHandler handler, Collection<Item> items) {
        return this.register(handler, InventoryValidationFailureHandler.ItemPredicate.of(items));
    }

    /**
     * Registers the provided {@code handler} as a default handler.
     * @param handler Handler to be registered.
     * @return Registered entry.
     */
    default Entry registerDefault(InventoryValidationFailureHandler handler) {
        return this.register(handler, InventoryValidationFailureHandler.Predicate.TRUE);
    }


    /**
     * Unregisters the given {@code handler}.
     * @param handler Handler to be unregistered.
     * @return {@code true} if the {@code handler} was unregistered; otherwise, {@code false}.
     */
    boolean unregister(InventoryValidationFailureHandler handler);

    /**
     * Unregisters the given {@code entry}.
     * @param entry Entry to be unregistered.
     * @return {@code true} if the {@code entry} was unregistered; otherwise, {@code false}.
     */
    default boolean unregister(Entry entry) {
        return this.unregister(entry.getHandler());
    }


    /**
     * {@link InventoryValidationFailureHandlerRegistry} entry.
     */
    interface Entry {
        /**
         * @return {@link InventoryValidationFailureHandler} instance.
         */
        InventoryValidationFailureHandler getHandler();

        /**
         * @return Predicate that should pass in order for the {@code handler} to be used.
         */
        InventoryValidationFailureHandler.Predicate getPredicate();
    }
}
