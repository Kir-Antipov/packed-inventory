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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class NbtItemsInventory implements Inventory, NamedScreenHandlerFactory {
    protected final PlayerEntity player;
    protected final Inventory inventory;
    protected final int index;
    protected final ItemStack stack;

    protected NbtItemsInventory(Inventory inventory, int index, PlayerEntity player) {
        this.inventory = inventory;
        this.index = index;
        this.player = player;
        this.stack = inventory.getStack(index);
        this.getItemList().ifPresent(NbtItemListUtil::clean);
        this.getItemList().ifPresent(NbtItemListUtil::sort);
        this.removeItemListIfEmpty();
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player) {
        return NbtItemsInventory.create(inventory, index, player, (InventoryDependentScreenHandlerFactory)null);
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player, @Nullable InventoryDependentScreenHandlerFactory screenHandlerFactory) {
        ItemStack stack = inventory.getStack(index);
        Optional<BlockEntityType<?>> blockEntityType = BlockEntityUtil.getBlockEntityType(stack.getItem());
        if (blockEntityType.isPresent()) {
            return NbtItemsInventory.create(inventory, index, player, blockEntityType.get(), screenHandlerFactory);
        } else {
            int defaultSize = BlockEntityUtil.getInventorySize(BlockEntityType.CHEST, 27);
            Consumer<NbtCompound> nbtInitializer = BlockEntityUtil.getBlockEntityItemStackInitializer(stack.getItem());
            return NbtItemsInventory.create(inventory, index, player, defaultSize, nbtInitializer, screenHandlerFactory);
        }
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player, BlockEntityType<?> blockEntityType) {
        return NbtItemsInventory.create(inventory, index, player, blockEntityType, null);
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player, BlockEntityType<?> blockEntityType, @Nullable InventoryDependentScreenHandlerFactory screenHandlerFactory) {
        ItemStack stack = inventory.getStack(index);
        int defaultSize = BlockEntityUtil.getInventorySize(BlockEntityType.CHEST, 27);
        return NbtItemsInventory.create(inventory, index, player, BlockEntityUtil.getInventorySize(blockEntityType, defaultSize), BlockEntityUtil.getBlockEntityItemStackInitializer(blockEntityType, stack.getItem()), screenHandlerFactory);
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player, int size) {
        return NbtItemsInventory.create(inventory, index, player, size, null);
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player, int size, @Nullable InventoryDependentScreenHandlerFactory screenHandlerFactory) {
        return NbtItemsInventory.create(inventory, index, player, size, null, screenHandlerFactory);
    }

    public static NbtItemsInventory create(Inventory inventory, int index, PlayerEntity player, int size, @Nullable Consumer<NbtCompound> nbtInitializer, @Nullable InventoryDependentScreenHandlerFactory screenHandlerFactory) {
        if (nbtInitializer == null) {
            nbtInitializer = BlockEntityUtil.getBlockEntityItemStackInitializer(inventory.getStack(index).getItem());
        }
        if (screenHandlerFactory == null) {
            screenHandlerFactory = InventoryDependentScreenHandlerFactory.genericOfSize(size);
            if (screenHandlerFactory == null) {
                screenHandlerFactory = InventoryDependentScreenHandlerFactory.EMPTY;
            }
        }

        Consumer<NbtCompound> finalNbtInitializer = nbtInitializer;
        InventoryDependentScreenHandlerFactory finalScreenHandlerFactory = screenHandlerFactory;
        return new NbtItemsInventory(inventory, index, player) {
            @Override
            protected NbtCompound createNbtWithIdentifyingData() {
                NbtCompound nbt = new NbtCompound();
                finalNbtInitializer.accept(nbt);
                return nbt;
            }

            @Override
            public int size() {
                return size;
            }

            @Override
            public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return finalScreenHandlerFactory.createMenu(syncId, inv, this);
            }
        };
    }

    public Inventory getContainingInventory() {
        return this.inventory;
    }

    public int getIndex() {
        return this.index;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    protected abstract NbtCompound createNbtWithIdentifyingData();

    protected Optional<NbtList> getItemList() {
        NbtCompound nbt = this.stack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
        return nbt == null || !nbt.contains(InventoryUtil.ITEMS_KEY, NbtElement.LIST_TYPE) ? Optional.empty() : Optional.of(nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE));
    }

    protected void removeItemListIfEmpty() {
        NbtCompound nbt = this.stack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
        if (nbt != null && (!nbt.contains(InventoryUtil.ITEMS_KEY, NbtElement.LIST_TYPE) || nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE).size() == 0)) {
            this.stack.removeSubNbt(InventoryUtil.BLOCK_ENTITY_TAG_KEY);
        }
    }

    protected NbtList getRequiredItemList() {
        NbtCompound nbt = this.stack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
        if (nbt == null) {
            nbt = this.createNbtWithIdentifyingData();
            this.stack.setSubNbt(InventoryUtil.BLOCK_ENTITY_TAG_KEY, nbt);
        }

        if (!nbt.contains(InventoryUtil.ITEMS_KEY, NbtElement.LIST_TYPE)) {
            nbt.put(InventoryUtil.ITEMS_KEY, new NbtList());
        }

        return nbt.getList(InventoryUtil.ITEMS_KEY, NbtElement.COMPOUND_TYPE);
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
        return this.getItemList().map(items -> NbtItemListUtil.get(items, slot)).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = this.getItemList().map(items -> NbtItemListUtil.remove(items, slot, amount)).orElse(ItemStack.EMPTY);
        if (removed != ItemStack.EMPTY) {
            this.markDirty();
        }
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = this.getItemList().map(items -> NbtItemListUtil.remove(items, slot)).orElse(ItemStack.EMPTY);
        if (removed != ItemStack.EMPTY) {
            this.markDirty();
        }
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        NbtItemListUtil.insert(this.getRequiredItemList(), slot, stack);
        this.markDirty();
    }

    @Override
    public void markDirty() {
        this.removeItemListIfEmpty();
        this.inventory.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.player == player;
    }

    @Override
    public void clear() {
        this.getItemList().ifPresent(NbtList::clear);
        this.removeItemListIfEmpty();
    }
}
