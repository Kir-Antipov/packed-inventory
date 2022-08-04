package dev.kir.packedinventory.item;

import dev.kir.packedinventory.nbt.NbtListProvider;
import dev.kir.packedinventory.util.inventory.NbtItemListUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class NbtListItemStack extends EquatableItemStack {
    private @Nullable NbtListProvider nbtListProvider;
    private int slot;

    private NbtListItemStack(ItemConvertible item, int count, @Nullable NbtListProvider nbtListProvider, int slot) {
        super(item, count);
        this.nbtListProvider = nbtListProvider;
        this.slot = slot;
        this.refreshNbt();
    }

    public static ItemStack of(NbtListProvider nbtListProvider, int slot) {
        NbtList list = nbtListProvider.getNbtList().orElse(null);
        int i = list == null ? -1 : NbtItemListUtil.binarySearch(list, slot);
        ItemStack stack = i < 0 ? ItemStack.EMPTY : NbtItemListUtil.asItemStack(list.getCompound(i));
        return stack.isEmpty() ? ItemStack.EMPTY : new NbtListItemStack(stack.getItem(), stack.getCount(), nbtListProvider, slot);
    }

    @Override
    public int getCount() {
        if (!this.isUnbound()) {
            super.setCount(this.get(ItemStack::getCount, 0));
        }
        return super.getCount();
    }

    @Override
    public void setCount(int count) {
        super.setCount(count);
        this.update(x -> x.setCount(count));
    }

    @Override
    public int getDamage() {
        if (!this.isUnbound()) {
            int realDamage = this.get(ItemStack::getDamage, 0);
            int currentDamage = super.getDamage();
            if (realDamage != currentDamage) {
                super.setDamage(realDamage);
            }
        }
        return super.getDamage();
    }

    @Override
    public void setDamage(int damage) {
        super.setDamage(damage);
        this.update(x -> x.setDamage(damage));
    }

    @Override
    public boolean hasNbt() {
        this.refreshNbt();
        return super.hasNbt();
    }

    @Override
    public @Nullable NbtCompound getNbt() {
        this.refreshNbt();
        return super.getNbt();
    }

    @Override
    public void setNbt(@Nullable NbtCompound nbt) {
        super.setNbt(nbt);
        this.update(x -> x.setNbt(nbt));
    }

    @Override
    public NbtCompound getOrCreateSubNbt(String key) {
        this.refreshNbt();
        return super.getOrCreateSubNbt(key);
    }

    @Override
    public @Nullable NbtCompound getSubNbt(String key) {
        if (!this.isUnbound()) {
            NbtCompound realSubNbt = this.get(x -> x.getSubNbt(key), null);
            NbtCompound currentSubNbt = super.getSubNbt(key);
            if (!Objects.equals(realSubNbt, currentSubNbt)) {
                if (realSubNbt == null) {
                    super.removeSubNbt(key);
                } else {
                    super.setSubNbt(key, realSubNbt);
                }
            }
        }
        return super.getSubNbt(key);
    }

    @Override
    public void setSubNbt(String key, NbtElement element) {
        super.setSubNbt(key, element);
        this.update(x -> x.setSubNbt(key, element));
    }

    @Override
    public void removeSubNbt(String key) {
        super.removeSubNbt(key);
        this.update(x -> x.removeSubNbt(key));
    }

    @Override
    public Text getName() {
        if (!this.isUnbound()) {
            return this.get(ItemStack::getName, this.getItem().getName(this));
        }
        return super.getName();
    }

    @Override
    public ItemStack setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        this.update(x -> x.setCustomName(name));
        return this;
    }

    @Override
    public boolean hasCustomName() {
        if (!this.isUnbound()) {
            return this.get(ItemStack::hasCustomName, false);
        }
        return super.hasCustomName();
    }

    @Override
    public boolean hasEnchantments() {
        this.refreshNbt();
        return super.hasEnchantments();
    }

    @Override
    public NbtList getEnchantments() {
        this.refreshNbt();
        return super.getEnchantments();
    }

    @Override
    public void addEnchantment(Enchantment enchantment, int level) {
        super.addEnchantment(enchantment, level);
        this.update(x -> x.addEnchantment(enchantment, level));
    }

    @Override
    public @Nullable Entity getHolder() {
        if (!this.isUnbound()) {
            Entity realHolder = this.get(ItemStack::getHolder, null);
            Entity currentHolder = super.getHolder();
            if (!Objects.equals(realHolder, currentHolder)) {
                super.setHolder(realHolder);
            }
        }
        return super.getHolder();
    }

    @Override
    public void setHolder(@Nullable Entity holder) {
        super.setHolder(holder);
        this.update(x -> x.setHolder(holder));
    }

    @Override
    public int getRepairCost() {
        this.refreshNbt();
        return super.getRepairCost();
    }

    @Override
    public void setRepairCost(int repairCost) {
        super.setRepairCost(repairCost);
        this.update(x -> x.setRepairCost(repairCost));
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isUnbound() {
        return this.nbtListProvider == null || this.slot < 0;
    }

    public void unbound() {
        this.nbtListProvider = null;
        this.slot = -1;
    }

    private void refreshNbt() {
        if (this.isUnbound()) {
            return;
        }

        NbtCompound realNbt = this.get(ItemStack::getNbt, null);
        NbtCompound currentNbt = super.getNbt();
        if (!Objects.equals(realNbt, currentNbt)) {
            super.setNbt(realNbt);
        }
    }

    private <T> T get(Function<ItemStack, T> getter, T defaultValue) {
        if (this.nbtListProvider == null || this.slot < 0) {
            return defaultValue;
        }

        NbtList list = this.nbtListProvider.getNbtList().orElse(null);
        if (list == null) {
            return defaultValue;
        }

        return getter.apply(NbtItemListUtil.get(list, this.slot));
    }

    private void update(Consumer<ItemStack> stackUpdater) {
        if (this.nbtListProvider == null || this.slot < 0) {
            return;
        }

        NbtItemListUtil.update(this.nbtListProvider.getOrCreateNbtList(), this.slot, stackUpdater);
    }
}
