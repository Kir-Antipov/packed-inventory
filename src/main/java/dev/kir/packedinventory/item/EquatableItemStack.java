package dev.kir.packedinventory.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.RegistryEntry;

import java.util.Objects;

public class EquatableItemStack extends ItemStack {
    public EquatableItemStack(ItemConvertible item) {
        super(item);
    }

    public EquatableItemStack(RegistryEntry<Item> entry) {
        super(entry);
    }

    public EquatableItemStack(ItemConvertible item, int count) {
        super(item, count);
    }

    @Override
    public ItemStack copy() {
        if (this.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new EquatableItemStack(this.getItem(), this.getCount());
        stack.setBobbingAnimationTime(this.getBobbingAnimationTime());
        NbtCompound nbt = this.getNbt();
        if (nbt != null) {
            stack.setNbt(nbt.copy());
        }
        return stack;
    }

    @Override
    public boolean isEqual(ItemStack stack) {
        if (this.getCount() != stack.getCount()) {
            return false;
        }

        if (!this.isOf(stack.getItem())) {
            return false;
        }
        return Objects.equals(this.getNbt(), stack.getNbt());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ItemStack && this.isEqual((ItemStack)obj);
    }
}
