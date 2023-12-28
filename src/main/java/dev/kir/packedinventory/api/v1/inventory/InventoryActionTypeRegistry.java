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

    Optional<InventoryAction> read(PacketByteBuf buffer);

    boolean write(PacketByteBuf buffer, InventoryAction inventoryAction);

    /**
     * Registers the given {@code entry} in the registry.
     * @param entry Entry to be registered.
     * @return Registered entry.
     */
    <T extends InventoryAction> Entry<T> register(Entry<T> entry);

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
