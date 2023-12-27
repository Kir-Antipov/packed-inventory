package dev.kir.packedinventory.util.network;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;

import java.util.List;

public final class PacketByteBufUtil {
    public static void writeIntegerList(PacketByteBuf buffer, List<Integer> list) {
        if (list instanceof IntList) {
            buffer.writeIntList((IntList)list);
            return;
        }

        buffer.writeVarInt(list.size());
        list.forEach(buffer::writeVarInt);
    }

    private PacketByteBufUtil() { }
}