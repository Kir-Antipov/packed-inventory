package dev.kir.packedinventory.api.v1.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Alternative version of {@link ScreenHandlerFactory} that requires synchronization id, player inventory, and container inventory to create {@link ScreenHandler}.
 */
@FunctionalInterface
public interface InventoryDependentScreenHandlerFactory {
    /**
     * Does not create {@link ScreenHandler}.
     */
    InventoryDependentScreenHandlerFactory EMPTY = (id, pInv, inv) -> null;
    /**
     * Creates {@link GenericContainerScreenHandler} of size {@code 9}.
     */
    InventoryDependentScreenHandlerFactory GENERIC_9X1 = (id, pInv, inv) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, id, pInv, inv, 1);
    /**
     * Creates {@link GenericContainerScreenHandler} of size {@code 18}.
     */
    InventoryDependentScreenHandlerFactory GENERIC_9X2 = (id, pInv, inv) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, id, pInv, inv, 2);
    /**
     * Creates {@link GenericContainerScreenHandler} of size {@code 27}.
     */
    InventoryDependentScreenHandlerFactory GENERIC_9X3 = (id, pInv, inv) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, id, pInv, inv, 3);
    /**
     * Creates {@link GenericContainerScreenHandler} of size {@code 36}.
     */
    InventoryDependentScreenHandlerFactory GENERIC_9X4 = (id, pInv, inv) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, id, pInv, inv, 4);
    /**
     * Creates {@link GenericContainerScreenHandler} of size {@code 45}.
     */
    InventoryDependentScreenHandlerFactory GENERIC_9X5 = (id, pInv, inv) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, id, pInv, inv, 5);
    /**
     * Creates {@link GenericContainerScreenHandler} of size {@code 54}.
     */
    InventoryDependentScreenHandlerFactory GENERIC_9X6 = (id, pInv, inv) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, id, pInv, inv, 6);

    /**
     * Creates new {@link ScreenHandler} instance, if any; otherwise {@code null}.
     * @param syncId Synchronization id.
     * @param playerInventory Player inventory.
     * @param inventory Container inventory.
     * @return New {@link ScreenHandler} instance, if any; otherwise {@code null}.
     */
    @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, Inventory inventory);

    /**
     * Transforms this instance into {@link ScreenHandlerFactory}.
     * @param inventory Container inventory.
     * @return New {@link ScreenHandlerFactory} instance that wraps this factory.
     */
    default ScreenHandlerFactory asScreenHandlerFactory(Inventory inventory) {
        return (s, i, __) -> this.createMenu(s, i, inventory);
    }

    /**
     * Transforms this instance into {@link NamedScreenHandlerFactory}.
     * @param inventory Container inventory.
     * @param name Container name.
     * @return New {@link NamedScreenHandlerFactory} instance that wraps this factory.
     */
    default NamedScreenHandlerFactory asNamedScreenHandlerFactory(Inventory inventory, Text name) {
        return new SimpleNamedScreenHandlerFactory(this.asScreenHandlerFactory(inventory), name);
    }

    /**
     * Returns the most suitable {@link InventoryDependentScreenHandlerFactory} for the given inventory size, if any; otherwise, {@code null}.
     * @param size Inventory size. Should be at least {@code 9} for this method to return a non-null value.
     * @return The most suitable {@link InventoryDependentScreenHandlerFactory} for the given inventory size, if any; otherwise, {@code null}.
     */
    static @Nullable InventoryDependentScreenHandlerFactory genericOfSize(int size) {
        if (size >= 54) {
            return GENERIC_9X6;
        }

        if (size >= 45) {
            return GENERIC_9X5;
        }

        if (size >= 36) {
            return GENERIC_9X4;
        }

        if (size >= 27) {
            return GENERIC_9X3;
        }

        if (size >= 18) {
            return GENERIC_9X2;
        }

        if (size >= 9) {
            return GENERIC_9X1;
        }

        return null;
    }

    /**
     * Returns the most suitable {@link InventoryDependentScreenHandlerFactory} for the given inventory size, if any; otherwise, defaultFactory.
     * @param size Inventory size.
     * @return The most suitable {@link InventoryDependentScreenHandlerFactory} for the given inventory size, if any; otherwise, defaultFactory.
     */
    static InventoryDependentScreenHandlerFactory genericOfSize(int size, InventoryDependentScreenHandlerFactory defaultFactory) {
        InventoryDependentScreenHandlerFactory factory = InventoryDependentScreenHandlerFactory.genericOfSize(size);
        return factory == null ? defaultFactory : factory;
    }

    /**
     * Returns the most suitable {@link InventoryDependentScreenHandlerFactory} for the given inventory size.
     * @param size Inventory size. Should be at least {@code 9} for this method to return a non-empty value.
     * @return The most suitable {@link InventoryDependentScreenHandlerFactory} for the given inventory size.
     */
    static Optional<InventoryDependentScreenHandlerFactory> optionalGenericOfSize(int size) {
        return Optional.ofNullable(InventoryDependentScreenHandlerFactory.genericOfSize(size));
    }
}
