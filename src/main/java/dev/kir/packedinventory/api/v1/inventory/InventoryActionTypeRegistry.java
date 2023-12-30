package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * {@link InventoryActionType} registry.
 */
public interface InventoryActionTypeRegistry {
    /**
     * @return {@link InventoryActionTypeRegistry} instance.
     */
    @ApiStatus.Internal
    static InventoryActionTypeRegistry getInstance() {
        return InventoryActionTypeRegistryImpl.INSTANCE;
    }

    /**
     * Attempts to read an inventory action from the provided buffer.
     * @param buffer The buffer to read from.
     * @return An {@link Optional} containing the read inventory action if successful; otherwise, an empty {@link Optional}.
     */
    Optional<InventoryAction> read(PacketByteBuf buffer);

    /**
     * Writes an inventory action to the provided buffer.
     * @param buffer The buffer to write to.
     * @param inventoryAction The inventory action to write.
     * @return {@code true} if the write was successful; otherwise, {@code false}.
     */
    boolean write(PacketByteBuf buffer, InventoryAction inventoryAction);

    /**
     * Registers the given {@code entry} in the registry.
     * @param entry Entry to be registered.
     * @return Registered entry.
     */
    <T extends InventoryAction> Entry<T> register(Entry<T> entry);

    /**
     * Registers a new inventory action type with the specified identifier.
     * @param id The identifier for the new inventory action type.
     * @param inventoryActionType The inventory action type to register.
     * @return Registered entry.
     */
    default <T extends InventoryAction> Entry<T> register(Identifier id, InventoryActionType<T> inventoryActionType) {
        return this.register(new Entry<>() {
            @Override
            public Identifier getId() {
                return id;
            }

            @Override
            public InventoryActionType<T> getType() {
                return inventoryActionType;
            }
        });
    }

    /**
     * Unregisters an existing inventory action type with the specified identifier.
     * @param id The identifier for the inventory action type to unregister.
     * @return {@code true} if the unregister operation was successful; otherwise, {@code false}.
     */
    boolean unregister(Identifier id);

    /**
     * Unregisters the given {@code entry}.
     * @param entry Entry to be unregistered.
     * @return {@code true} if the {@code entry} was unregistered; otherwise, {@code false}.
     */
    default boolean unregister(Entry<?> entry) {
        return this.unregister(entry.getId());
    }

    /**
     * {@link InventoryActionTypeRegistry} entry.
     */
    interface Entry<T extends InventoryAction> {
        /**
         * @return {@link InventoryViewer} instance.
         */
        Identifier getId();

        /**
         * @return Predicate that should pass in order for the {@code inventory viewer} to be used.
         */
        InventoryActionType<T> getType();
    }
}
