package dev.kir.packedinventory.api.v1.item;

import dev.kir.packedinventory.inventory.EmptyInventory;
import dev.kir.packedinventory.inventory.ListInventory;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

/**
 * Basic implementation of {@link TooltipSyncData} that should suit containers
 * that only requires their inventory information to be synced with client.
 */
public class GenericContainerTooltipSyncData implements TooltipSyncData {
    private Inventory inventory;

    /**
     * Creates new {@link GenericContainerTooltipSyncData} instance with empty {@code inventory}.
     */
    protected GenericContainerTooltipSyncData() {
        this(EmptyInventory.getInstance());
    }

    /**
     * Creates new {@link GenericContainerTooltipSyncData} instance.
     * @param inventory Inventory stored by this instance.
     */
    protected GenericContainerTooltipSyncData(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * @return Inventory stored by this instance.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * @return New {@link GenericContainerTooltipSyncData} instance with empty {@code inventory}.
     */
    public static GenericContainerTooltipSyncData of() {
        return new GenericContainerTooltipSyncData();
    }

    /**
     * Returns new {@link GenericContainerTooltipSyncData} instance.
     * @param inventory Inventory.
     * @return New {@link GenericContainerTooltipSyncData} instance.
     */
    public static GenericContainerTooltipSyncData of(Inventory inventory) {
        return new GenericContainerTooltipSyncData(inventory);
    }

    /**
     * Returns new {@link GenericContainerTooltipSyncData} instance.
     * @param inventory Inventory.
     * @return New {@link GenericContainerTooltipSyncData} instance.
     */
    public static GenericContainerTooltipSyncData of(DefaultedList<ItemStack> inventory) {
        return new GenericContainerTooltipSyncData(ListInventory.wrap(inventory));
    }

    /**
     * Returns new {@link GenericContainerTooltipSyncData} instance with {@code inventory} of size {@code size} filled with {@link ItemStack#EMPTY}.
     * @param size Inventory size.
     * @return New {@link GenericContainerTooltipSyncData} instance with {@code inventory} of size {@code size} filled with {@link ItemStack#EMPTY}.
     */
    public static GenericContainerTooltipSyncData ofSize(int size) {
        return new GenericContainerTooltipSyncData(ListInventory.wrap(DefaultedList.ofSize(size, ItemStack.EMPTY)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        int size = nbt.getInt(InventoryUtil.SIZE_KEY);
        if (size == 0) {
            this.inventory = EmptyInventory.getInstance();
            return;
        }

        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
        NbtList items = nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        for (NbtElement item : items) {
            if (!(item instanceof NbtCompound)) {
                continue;
            }

            int slot = ((NbtCompound)item).getByte(InventoryUtil.SLOT_KEY) & 255;
            if (slot < size) {
                inventory.set(slot, ItemStack.fromNbt((NbtCompound)item));
            }
        }
        this.inventory = ListInventory.wrap(inventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeNbt(NbtCompound nbt) {
        Inventory inventory = this.inventory;
        int size = inventory.size();

        nbt.putInt(InventoryUtil.SIZE_KEY, size);
        if (size == 0) {
            return;
        }

        NbtList items = new NbtList();
        for (int i = 0; i < size; ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            NbtCompound stackNbt = new NbtCompound();
            stack.writeNbt(stackNbt);
            stackNbt.putByte(InventoryUtil.SLOT_KEY, (byte)i);

            items.add(stackNbt);
        }
        nbt.put(InventoryUtil.ITEMS_KEY, items);
    }
}
