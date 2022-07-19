package dev.kir.packedinventory.api.v1.item;

import net.minecraft.nbt.NbtCompound;

/**
 * Synchronization data used by tooltips.
 */
public interface TooltipSyncData {
    /**
     * Updates content of this instance with server-provided Nbt data.
     * @param nbt Nbt data used to update this instance.
     */
    void readNbt(NbtCompound nbt);

    /**
     * Writes content of this instance to the given {@link NbtCompound}.
     * @param nbt Nbt to store content of this instance.
     */
    void writeNbt(NbtCompound nbt);
}
