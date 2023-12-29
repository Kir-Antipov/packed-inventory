package dev.kir.packedinventory.api.v1.inventory;

public final class InventoryActionTypes {
    public static final InventoryActionType<?> DEFAULT = DefaultInventoryAction.TYPE;
    public static final InventoryActionType<?> DROP = DropInventoryAction.TYPE;

    private InventoryActionTypes() { }
}