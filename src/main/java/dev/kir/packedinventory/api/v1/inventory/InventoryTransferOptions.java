package dev.kir.packedinventory.api.v1.inventory;

import java.util.EnumSet;

public enum InventoryTransferOptions {
    PREFER_INSERTION,
    PREFER_EXTRACTION;

    public static final EnumSet<InventoryTransferOptions> NONE = EnumSet.noneOf(InventoryTransferOptions.class);
}