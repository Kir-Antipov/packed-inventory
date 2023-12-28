package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface InventoryActionType<T extends InventoryAction> {
    T read(PacketByteBuf buffer);

    void write(PacketByteBuf buffer, T inventoryAction);

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