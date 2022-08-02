package dev.kir.packedinventory.nbt;

import net.minecraft.nbt.NbtList;

import java.util.Optional;

public interface NbtListProvider {
    Optional<NbtList> getNbtList();

    NbtList getOrCreateNbtList();
}
