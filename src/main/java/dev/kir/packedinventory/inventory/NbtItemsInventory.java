package dev.kir.packedinventory.inventory;

import dev.kir.packedinventory.api.v1.screen.InventoryDependentScreenHandlerFactory;
import dev.kir.packedinventory.util.block.entity.BlockEntityUtil;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import dev.kir.packedinventory.util.inventory.NbtItemListUtil;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public abstract class NbtItemsInventory implements Inventory, NamedScreenHandlerFactory {
    protected final PlayerEntity player;
    protected final Inventory inventory;
    private final DefaultedList<ItemStack> items;
    private final DefaultedList<ItemStack> trackedItems;
    private ItemStack stack;
    private ItemStack stackCopy;

    protected NbtItemsInventory(Inventory inventory, int index, PlayerEntity player) {
        this(inventory, inventory.getStack(index), player);
    }

    protected NbtItemsInventory(Inventory inventory, ItemStack stack, PlayerEntity player) {
        this.inventory = inventory;
        this.player = player;

        this.stack = stack;
        this.stackCopy = stack.copy();

        this.getItemList().ifPresent(NbtItemListUtil::clean);
        this.getItemList().ifPresent(NbtItemListUtil::sort);
        this.removeItemListIfEmpty();

        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.trackedItems = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.syncItemListWithItems();
    }

    public static Builder builder(Inventory inventory, int index, PlayerEntity player) {
        return new Builder(inventory, inventory.getStack(index), player);
    }

    public static Builder builder(Inventory inventory, ItemStack stack, PlayerEntity player) {
        return new Builder(inventory, stack, player);
    }

    public Inventory getContainingInventory() {
        return this.inventory;
    }

    public ItemStack asStack() {
        return this.stack;
    }

    protected abstract NbtCompound createNbtWithIdentifyingData();

    protected Optional<NbtList> getItemList() {
        NbtCompound nbt = this.stack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
        return nbt == null || !nbt.contains(InventoryUtil.ITEMS_KEY, NbtElement.LIST_TYPE) ? Optional.empty() : Optional.of(nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE));
    }

    protected NbtList getRequiredItemList() {
        NbtCompound nbt = this.stack.getSubNbt(InventoryUtil.BLOCK_ENTITY_TAG_KEY);
        if (nbt == null) {
            nbt = this.createNbtWithIdentifyingData();
            this.stack.setSubNbt(InventoryUtil.BLOCK_ENTITY_TAG_KEY, nbt);
        }

        if (!nbt.contains(InventoryUtil.ITEMS_KEY, NbtElement.LIST_TYPE)) {
            nbt.put(InventoryUtil.ITEMS_KEY, new NbtList());
        }

        return nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE);
    }

    protected void removeItemListIfEmpty() {
        NbtCompound nbt = this.stack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
        if (nbt != null && (!nbt.contains(InventoryUtil.ITEMS_KEY, NbtElement.LIST_TYPE) || nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE).size() == 0)) {
            this.stack.removeSubNbt(InventoryUtil.BLOCK_ENTITY_TAG_KEY);
        }
    }

    protected void refreshItemList() {
        this.removeItemListIfEmpty();
        this.inventory.markDirty();
    }

    private void syncItemListWithItems() {
        this.items.clear();
        this.trackedItems.clear();
        NbtList list = this.getItemList().orElse(null);
        if (list == null) {
            return;
        }

        int size = this.size();
        for (NbtElement nbt : list) {
            if (!(nbt instanceof NbtCompound)) {
                continue;
            }

            int slot = ((NbtCompound)nbt).getByte(InventoryUtil.SLOT_KEY);
            if (slot < 0 || slot >= size) {
                continue;
            }
            ItemStack stack = NbtItemListUtil.asItemStack((NbtCompound)nbt);
            this.items.set(slot, stack);
            this.trackedItems.set(slot, stack.copy());
        }
    }

    private void syncItemsWithItemList() {
        NbtList list = this.getRequiredItemList();
        int size = this.size();
        for (int i = 0; i < size; ++i) {
            ItemStack stack = this.items.get(i);
            ItemStack trackedStack = this.trackedItems.get(i);
            if (!ItemStack.areEqual(stack, trackedStack)) {
                this.trackedItems.set(i, stack.copy());
                NbtItemListUtil.insert(list, i, stack);
            }
        }
        this.refreshItemList();
    }

    @Override
    public Text getDisplayName() {
        return this.stack.getName();
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        InventoryDependentScreenHandlerFactory factory = InventoryDependentScreenHandlerFactory.genericOfSize(this.size());
        return factory == null ? null : factory.createMenu(syncId, inv, this);
    }

    @Override
    public abstract int size();

    @Override
    public boolean isEmpty() {
        return this.getItemList().map(x -> x.size() == 0).orElse(true);
    }

    @Override
    public ItemStack getStack(int slot) {
        ItemStack stack = this.getItemList().map(items -> NbtItemListUtil.get(items, slot)).orElse(ItemStack.EMPTY);
        if (!ItemStack.areEqual(this.items.get(slot), stack)) {
            this.items.set(slot, stack);
            this.trackedItems.set(slot, stack.copy());
        }
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = this.getItemList().map(items -> NbtItemListUtil.remove(items, slot, amount)).orElse(ItemStack.EMPTY);
        if (!removed.isEmpty()) {
            ItemStack remainingStack = NbtItemListUtil.get(this.getRequiredItemList(), slot);
            this.items.set(slot, remainingStack);
            this.trackedItems.set(slot, remainingStack.copy());
            this.refreshItemList();
        }
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = this.getItemList().map(items -> NbtItemListUtil.remove(items, slot)).orElse(ItemStack.EMPTY);
        this.items.set(slot, ItemStack.EMPTY);
        this.trackedItems.set(slot, ItemStack.EMPTY);
        if (!removed.isEmpty()) {
            this.refreshItemList();
        }
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.trackedItems.set(slot, stack.copy());
        NbtItemListUtil.insert(this.getRequiredItemList(), slot, stack);
        this.refreshItemList();
    }

    @Override
    public void markDirty() {
        this.syncItemsWithItemList();
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack != this.stack;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.player != player) {
            return false;
        }

        ItemStack cursorStack = this.player.currentScreenHandler.getCursorStack();
        if (!this.stack.isEmpty() && (cursorStack == this.stack || InventoryUtil.indexOf(this.inventory, this.stack) != -1)) {
            return true;
        }

        ItemStack copy = this.getStackCopy();
        if (!cursorStack.isEmpty()) {
            if (ItemStack.areEqual(cursorStack, copy)) {
                this.updateItemStack(cursorStack);
                return true;
            }
        }

        int i = InventoryUtil.indexOf(this.inventory, (x, s) -> ItemStack.areEqual(x, copy));
        if (i != -1) {
            this.updateItemStack(this.inventory.getStack(i));
            return true;
        }
        return false;
    }

    protected void updateItemStack(ItemStack stack) {
        this.stack = stack;
        this.stackCopy = stack.copy();
    }

    private ItemStack getStackCopy() {
        this.stackCopy.setNbt(this.stack.getNbt());
        return this.stackCopy;
    }

    @Override
    public void clear() {
        this.items.clear();
        this.trackedItems.clear();
        this.getItemList().ifPresent(NbtList::clear);
        this.removeItemListIfEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof NbtItemsInventory)) {
            return false;
        }

        return this.stack == ((NbtItemsInventory)o).stack;
    }

    public static final class Builder {
        private final PlayerEntity player;
        private final Inventory inventory;
        private final ItemStack stack;
        private @Nullable Integer size;
        private @Nullable Consumer<NbtCompound> nbtInitializer;
        private @Nullable InventoryDependentScreenHandlerFactory screenHandlerFactory;
        private @Nullable BiPredicate<ItemStack, NbtItemsInventory> isValid;

        private Builder(Inventory inventory, ItemStack stack, PlayerEntity player) {
            this.inventory = inventory;
            this.stack = stack;
            this.player = player;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder nbtInitializer(Consumer<NbtCompound> nbtInitializer) {
            this.nbtInitializer = nbtInitializer;
            return this;
        }

        public Builder screenHandler(InventoryDependentScreenHandlerFactory screenHandlerFactory) {
            this.screenHandlerFactory = screenHandlerFactory;
            return this;
        }

        public Builder isValid(BiPredicate<ItemStack, NbtItemsInventory> isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder resolveDefaults() {
            Optional<BlockEntityType<?>> blockEntityTypeOptional = BlockEntityUtil.getBlockEntityType(this.stack.getItem());
            if (blockEntityTypeOptional.isEmpty()) {
                if (this.size == null) {
                    this.size = this.getDefaultSize();
                }
                if (this.nbtInitializer == null) {
                    this.nbtInitializer = this.createDefaultNbtInitializer();
                }
            } else {
                BlockEntityType<?> blockEntityType = blockEntityTypeOptional.get();
                if (this.size == null) {
                    this.size = BlockEntityUtil.getInventorySize(blockEntityType, this.getDefaultSize());
                }
                if (this.nbtInitializer == null) {
                    this.nbtInitializer = BlockEntityUtil.getBlockEntityItemStackInitializer(blockEntityType, this.stack.getItem());
                }
            }
            if (this.screenHandlerFactory == null) {
                this.screenHandlerFactory = InventoryDependentScreenHandlerFactory.genericOfSize(this.size);
                if (this.screenHandlerFactory == null) {
                    this.screenHandlerFactory = InventoryDependentScreenHandlerFactory.EMPTY;
                }
            }
            return this;
        }

        private int getDefaultSize() {
            return BlockEntityUtil.getInventorySize(BlockEntityType.CHEST, 27);
        }

        private Consumer<NbtCompound> createDefaultNbtInitializer() {
            return BlockEntityUtil.getBlockEntityItemStackInitializer(this.stack.getItem());
        }

        public NbtItemsInventory build() {
            int size = this.size == null ? this.getDefaultSize() : this.size;
            Consumer<NbtCompound> nbtInitializer = this.nbtInitializer == null ? this.createDefaultNbtInitializer() : this.nbtInitializer;
            InventoryDependentScreenHandlerFactory screenHandlerFactory = this.screenHandlerFactory;
            BiPredicate<ItemStack, NbtItemsInventory> isValid = this.isValid;
            return new NbtItemsInventory(this.inventory, this.stack, this.player) {
                @Override
                protected NbtCompound createNbtWithIdentifyingData() {
                    NbtCompound nbt = new NbtCompound();
                    nbtInitializer.accept(nbt);
                    return nbt;
                }

                @Override
                public int size() {
                    return size;
                }

                @Override
                public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return screenHandlerFactory == null ? super.createMenu(syncId, inv, player) : screenHandlerFactory.createMenu(syncId, inv, this);
                }

                @Override
                public boolean isValid(int slot, ItemStack stack) {
                    return isValid == null ? super.isValid(slot, stack) : isValid.test(stack, this);
                }
            };
        }
    }
}
