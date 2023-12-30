package dev.kir.packedinventory.api.v1.inventory;

/**
 * Defines the default types of inventory actions that can be performed.
 */
public final class InventoryActionTypes {
    /**
     * Represents the default inventory action.
     */
    public static final InventoryActionType<?> DEFAULT = DefaultInventoryAction.TYPE;

    /**
     * Represents the item transferring action.
     */
    public static final InventoryActionType<?> TRANSFER = TransferInventoryAction.TYPE;

    /**
     * Represents the item dropping action.
     */
    public static final InventoryActionType<?> DROP = DropInventoryAction.TYPE;

    private InventoryActionTypes() { }
}
