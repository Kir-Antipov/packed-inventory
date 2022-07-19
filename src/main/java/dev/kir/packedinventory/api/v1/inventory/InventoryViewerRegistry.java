package dev.kir.packedinventory.api.v1.inventory;

import com.mojang.datafixers.util.Either;
import dev.kir.packedinventory.api.v1.FailureReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * {@link InventoryViewer} registry.
 */
public interface InventoryViewerRegistry {
    /**
     * @return {@link InventoryViewerRegistry} instance.
     */
    @ApiStatus.Internal
    static InventoryViewerRegistry getInstance() {
        return InventoryViewerRegistryImpl.INSTANCE;
    }


    /**
     * Opens an inventory view at the selected {@code slot} within the target {@code inventory} ignoring all validation rules.
     * @param inventory Inventory.
     * @param slot Inventory slot.
     * @param player Player.
     * @return Inventory view at the selected {@code slot} within the target {@code inventory}, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<Inventory> forceView(Inventory inventory, int slot, PlayerEntity player);

    /**
     * Tries to open an inventory view at the selected {@code slot} within the target {@code inventory}.
     * @param inventory Inventory.
     * @param slot Inventory slot.
     * @param player Player.
     * @return Inventory view at the selected {@code slot} within the target {@code inventory}, or validation error, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<Either<Inventory, FailureReason>> view(Inventory inventory, int slot, PlayerEntity player);


    /**
     * Registers the given {@code entry} in the registry.
     * @param entry Entry to be registered.
     * @return Registered entry.
     */
    Entry register(Entry entry);

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param predicate Predicate that should pass in order for the {@code viewer} to be used.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.Validator validator, InventoryViewer.Predicate predicate) {
        return this.register(new Entry() {
            @Override
            public InventoryViewer getInventoryViewer() {
                return viewer;
            }

            @Override
            public InventoryViewer.Validator getValidator() {
                return validator;
            }

            @Override
            public InventoryViewer.Predicate getPredicate() {
                return predicate;
            }
        });
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param item Item this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.Validator validator, Item item) {
        return this.register(viewer, validator, List.of(item));
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param items Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.Validator validator, Item... items) {
        return this.register(viewer, validator, Arrays.asList(items));
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param items Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.Validator validator, Collection<Item> items) {
        return this.register(viewer, validator, InventoryViewer.ItemPredicate.of(items));
    }


    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param predicate Predicate that should pass in order for the {@code viewer} to be used.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.ExtendedValidator validator, InventoryViewer.Predicate predicate) {
        return this.register(viewer, (InventoryViewer.Validator)validator, predicate);
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param item Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.ExtendedValidator validator, Item item) {
        return this.register(viewer, (InventoryViewer.Validator)validator, item);
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param items Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.ExtendedValidator validator, Item... items) {
        return this.register(viewer, (InventoryViewer.Validator)validator, items);
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param validator Validator that should pass in order for the {@code viewer} to succeed.
     * @param items Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.ExtendedValidator validator, Collection<Item> items) {
        return this.register(viewer, (InventoryViewer.Validator)validator, items);
    }


    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param predicate Predicate that should pass in order for the {@code viewer} to be used.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, InventoryViewer.Predicate predicate) {
        return this.register(viewer, InventoryViewer.Validator.EMPTY, predicate);
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param item Item this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, Item item) {
        return this.register(viewer, InventoryViewer.Validator.EMPTY, item);
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param items Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, Item... items) {
        return this.register(viewer, InventoryViewer.Validator.EMPTY, items);
    }

    /**
     * Registers the provided {@code viewer}.
     * @param viewer Inventory viewer to be registered.
     * @param items Items this {@code viewer} is associated with.
     * @return Registered entry.
     */
    default Entry register(InventoryViewer viewer, Collection<Item> items) {
        return this.register(viewer, InventoryViewer.Validator.EMPTY, items);
    }


    /**
     * Unregisters the given {@code viewer}.
     * @param viewer Inventory viewer to be unregistered.
     * @return {@code true} if the {@code viewer} was unregistered; otherwise, {@code false}.
     */
    boolean unregister(InventoryViewer viewer);

    /**
     * Unregisters the given {@code entry}.
     * @param entry Entry to be unregistered.
     * @return {@code true} if the {@code entry} was unregistered; otherwise, {@code false}.
     */
    default boolean unregister(Entry entry) {
        return this.unregister(entry.getInventoryViewer());
    }


    /**
     * {@link InventoryViewerRegistry} entry.
     */
    interface Entry {
        /**
         * @return {@link InventoryViewer} instance.
         */
        InventoryViewer getInventoryViewer();

        /**
         * @return Predicate that should pass in order for the {@code inventory viewer} to be used.
         */
        InventoryViewer.Predicate getPredicate();

        /**
         * @return Validator that should pass in order for the {@code inventory viewer} to succeed.
         */
        InventoryViewer.Validator getValidator();
    }
}
