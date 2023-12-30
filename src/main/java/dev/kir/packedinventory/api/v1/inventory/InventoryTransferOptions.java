package dev.kir.packedinventory.api.v1.inventory;

import java.util.EnumSet;

/**
 * Represents the options for transferring items in an inventory.
 */
public enum InventoryTransferOptions {
    /**
     * Indicates that the transfer should prioritize inserting items into the target inventory.
     */
    PREFER_INSERTION,

    /**
     * Indicates that the transfer should prioritize extracting items from the source inventory.
     */
    PREFER_EXTRACTION;

    /**
     * Represents a state where no specific transfer options are preferred.
     */
    public static final EnumSet<InventoryTransferOptions> NONE = EnumSet.noneOf(InventoryTransferOptions.class);
}
