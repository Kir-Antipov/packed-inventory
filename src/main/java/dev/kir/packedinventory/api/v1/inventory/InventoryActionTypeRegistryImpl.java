package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class InventoryActionTypeRegistryImpl implements InventoryActionTypeRegistry {
    public static final InventoryActionTypeRegistryImpl INSTANCE = new InventoryActionTypeRegistryImpl();

    private final Map<Identifier, InventoryActionType<?>> typeById;
    private final Map<InventoryActionType<?>, Identifier> idByType;

    private InventoryActionTypeRegistryImpl() {
        this.typeById = new HashMap<>();
        this.idByType = new HashMap<>();
    }

    @Override
    public Optional<InventoryAction> read(PacketByteBuf buffer) {
        Identifier id = buffer.readIdentifier();
        InventoryActionType<?> type = this.typeById.get(id);
        if (type == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(type.read(buffer));
    }

    @Override
    public boolean write(PacketByteBuf buffer, InventoryAction inventoryAction) {
        InventoryActionType<?> type = inventoryAction.getType();
        Identifier id = this.idByType.get(type);
        if (id == null) {
            return false;
        }

        buffer.writeIdentifier(id);
        writeInventoryAction(type, buffer, inventoryAction);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <T extends InventoryAction> void writeInventoryAction(InventoryActionType<?> type, PacketByteBuf buffer, InventoryAction inventoryAction) {
        ((InventoryActionType<T>)type).write(buffer, (T)inventoryAction);
    }

    @Override
    public <T extends InventoryAction> Entry<T> register(Entry<T> entry) {
        this.typeById.put(entry.getId(), entry.getType());
        this.idByType.put(entry.getType(), entry.getId());
        return entry;
    }

    @Override
    public boolean unregister(Identifier id) {
        InventoryActionType<?> type = this.typeById.get(id);
        if (type == null) {
            return false;
        }

        this.idByType.remove(type);
        return true;
    }
}