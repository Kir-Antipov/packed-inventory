package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a specific type of inventory action that can be serialized to and from a {@link PacketByteBuf}.
 *
 * @param <T> The specific type of inventory action that can be serialized.
 */
public interface InventoryActionType<T extends InventoryAction> {
    /**
     * Reads an inventory action from the provided buffer.
     * @param buffer The buffer to read from.
     * @return The read inventory action.
     */
    T read(PacketByteBuf buffer);

    /**
     * Writes an inventory action to the provided buffer.
     * @param buffer The buffer to write to.
     * @param inventoryAction The inventory action to write.
     */
    void write(PacketByteBuf buffer, T inventoryAction);

    /**
     * Creates a new inventory action type from the provided reader and writer functions.
     * @param reader The function to use for reading the inventory action from a buffer.
     * @param writer The function to use for writing the inventory action to a buffer.
     * @return The created inventory action type.
     */
    static <T extends InventoryAction> InventoryActionType<T> create(Function<PacketByteBuf, T> reader, BiConsumer<PacketByteBuf, T> writer) {
        return new InventoryActionType<>() {
            @Override
            public T read(PacketByteBuf buffer) {
                return reader.apply(buffer);
            }

            @Override
            public void write(PacketByteBuf buffer, T inventoryAction) {
                writer.accept(buffer, inventoryAction);
            }
        };
    }
}
