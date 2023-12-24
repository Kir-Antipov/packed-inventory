package dev.kir.packedinventory.util.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class BlockEntityUtil {
    private static final Map<BlockEntityType<?>, BlockEntity> BLOCK_ENTITIES = new ConcurrentHashMap<>();
    private static final Map<BlockEntityType<?>, Integer> BLOCK_ENTITY_SIZES = new ConcurrentHashMap<>();
    private static final Map<BlockEntityType<?>, Consumer<NbtCompound>> BLOCK_ENTITY_NBT_INITIALIZERS = new ConcurrentHashMap<>();
    private static final Map<Item, Consumer<NbtCompound>> ITEM_NBT_INITIALIZERS = new ConcurrentHashMap<>();
    private static final Map<Item, Optional<BlockEntityType<?>>> BLOCK_ENTITIES_BY_ITEM = new ConcurrentHashMap<>();
    private static final Collection<String> PREFIXES = Arrays.stream(DyeColor.values()).map(x -> x.getName() + "_").toList();

    @SuppressWarnings("OptionalAssignedToNull")
    public static Optional<BlockEntityType<?>> getBlockEntityType(Item item) {
        Optional<BlockEntityType<?>> result = BLOCK_ENTITIES_BY_ITEM.get(item);
        if (result != null) {
            return result;
        }

        Identifier id = Registries.ITEM.getId(item);
        BlockEntityType<?> blockEntityType = Registries.BLOCK_ENTITY_TYPE.get(id);
        if (blockEntityType == null) {
            String prefix = PREFIXES.stream().filter(id.getPath()::startsWith).findFirst().orElse(null);
            if (prefix != null) {
                Identifier noPrefixId = new Identifier(id.getNamespace(), id.getPath().substring(prefix.length()));
                blockEntityType = Registries.BLOCK_ENTITY_TYPE.get(noPrefixId);
            }
        }

        result = Optional.ofNullable(blockEntityType);
        BLOCK_ENTITIES_BY_ITEM.put(item, result);
        return result;
    }

    public static Consumer<NbtCompound> getBlockEntityItemStackInitializer(Item relevantItem) {
        Consumer<NbtCompound> initializer = ITEM_NBT_INITIALIZERS.get(relevantItem);
        if (initializer != null) {
            return initializer;
        }

        Identifier id = Registries.ITEM.getId(relevantItem);
        BlockEntityType<?> blockEntityType = Registries.BLOCK_ENTITY_TYPE.get(id);
        if (blockEntityType != null) {
            return BlockEntityUtil.getBlockEntityItemStackInitializer(blockEntityType, relevantItem);
        }

        String idStr = id.toString();
        initializer = nbt -> nbt.putString("id", idStr);

        ITEM_NBT_INITIALIZERS.put(relevantItem, initializer);
        return initializer;
    }

    public static Consumer<NbtCompound> getBlockEntityItemStackInitializer(BlockEntityType<?> blockEntityType, Item relevantItem) {
        Consumer<NbtCompound> initializer = BLOCK_ENTITY_NBT_INITIALIZERS.get(blockEntityType);
        if (initializer != null) {
            return initializer;
        }

        BlockEntity blockEntity = BlockEntityUtil.getInstance(blockEntityType);
        if (blockEntity == null) {
            String id = Registries.ITEM.getId(relevantItem).toString();
            initializer = nbt -> nbt.putString("id", id);
        } else {
            initializer = x -> x.copyFrom(blockEntity.createNbtWithIdentifyingData());
        }

        BLOCK_ENTITY_NBT_INITIALIZERS.put(blockEntityType, initializer);
        return initializer;
    }

    public static Optional<Integer> getInventorySize(BlockEntityType<?> blockEntityType) {
        return Optional.ofNullable(BlockEntityUtil.getInventorySizeImpl(blockEntityType));
    }

    public static int getInventorySize(BlockEntityType<?> blockEntityType, int defaultValue) {
        Integer size = BlockEntityUtil.getInventorySizeImpl(blockEntityType);
        return size == null ? defaultValue : size;
    }

    private static @Nullable Integer getInventorySizeImpl(BlockEntityType<?> blockEntityType) {
        final int INVALID_BLOCK_ENTITY_SIZE= -1;

        Integer cachedSize = BLOCK_ENTITY_SIZES.get(blockEntityType);
        if (cachedSize != null) {
            return cachedSize == INVALID_BLOCK_ENTITY_SIZE ? null : cachedSize;
        }

        BlockEntity blockEntity = BlockEntityUtil.getInstance(blockEntityType);
        if (!(blockEntity instanceof Inventory)) {
            BLOCK_ENTITY_SIZES.put(blockEntityType, INVALID_BLOCK_ENTITY_SIZE);
            return null;
        }

        try {
            int size = ((Inventory)blockEntity).size();
            BLOCK_ENTITY_SIZES.put(blockEntityType, size);
            return size;
        } catch (Throwable error) {
            // Guard against faulty `BlockEntity` implementations.
            BLOCK_ENTITY_SIZES.put(blockEntityType, INVALID_BLOCK_ENTITY_SIZE);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> @Nullable T getInstance(BlockEntityType<T> blockEntityType) {
        return (T)BLOCK_ENTITIES.computeIfAbsent(blockEntityType, BlockEntityUtil::createInstance);
    }

    private static <T extends BlockEntity> @Nullable T createInstance(BlockEntityType<T> blockEntityType) {
        try {
            Block block = Registries.BLOCK.get(Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType));
            return blockEntityType.supports(block.getDefaultState()) ? blockEntityType.instantiate(BlockPos.ORIGIN, block.getDefaultState()) : null;
        } catch (Throwable error) {
            // Guard against faulty `BlockEntity` implementations.
            return null;
        }
    }

    private BlockEntityUtil() { }
}
