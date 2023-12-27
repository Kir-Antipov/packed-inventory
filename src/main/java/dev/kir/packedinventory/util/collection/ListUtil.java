package dev.kir.packedinventory.util.collection;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;

public final class ListUtil {
    public static IntList copyOf(IntList list) {
        return IntArrayList.wrap(list.toIntArray());
    }

    public static IntList copyOf(List<Integer> list) {
        if (list instanceof IntList) {
            return IntArrayList.wrap(((IntList)list).toIntArray());
        }

        return ListUtil.asIntList(list);
    }

    public static IntList asIntList(List<Integer> list) {
        if (list instanceof IntList) {
            return (IntList)list;
        }

        int size = list.size();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return IntArrayList.wrap(array);
    }

    private ListUtil() { }
}