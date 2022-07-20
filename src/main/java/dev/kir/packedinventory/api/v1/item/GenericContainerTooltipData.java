package dev.kir.packedinventory.api.v1.item;

import dev.kir.packedinventory.inventory.ListInventory;
import dev.kir.packedinventory.util.inventory.InventoryUtil;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation of {@link TooltipData} that should be suitable for most containers.
 *
 * @see dev.kir.packedinventory.api.v1.client.gui.tooltip.GenericContainerTooltipComponent
 */
public class GenericContainerTooltipData implements TooltipData {
    private final Inventory inventory;
    private final int rows;
    private final int columns;
    private final @Nullable DyeColor color;

    /**
     * Creates new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     */
    protected GenericContainerTooltipData(Inventory inventory) {
        this(inventory, null);
    }

    /**
     * Creates new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     */
    protected GenericContainerTooltipData(Inventory inventory, @Nullable DyeColor color) {
        this(inventory, -1, -1, color);
    }

    /**
     * Creates new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     */
    protected GenericContainerTooltipData(Inventory inventory, int rows, int columns) {
        this(inventory, rows, columns, null);
    }

    /**
     * Creates new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     */
    protected GenericContainerTooltipData(Inventory inventory, int rows, int columns, @Nullable DyeColor color) {
        this.inventory = inventory;
        this.columns = columns < 0 ? rows < 0 ? computeColumns(inventory) : Math.max(ceilDiv(inventory.size(), rows), 1) : columns;
        this.rows = rows < 0 ? Math.max(ceilDiv(inventory.size(), this.columns), 1) : rows;
        this.color = color;
    }

    private static int ceilDiv(int a, int b) {
        return a / b + (a % b == 0 ? 0 : 1);
    }

    /**
     * @return Inventory shown by the tooltip associated with this tooltip data.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * @return Number of rows used to display tooltip content.
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * @return Number of columns used to display tooltip content.
     */
    public int getColumns() {
        return this.columns;
    }

    /**
     * @return {@link DyeColor} of the tooltip represented by this tooltip data.
     */
    public @Nullable DyeColor getColor() {
        return this.color;
    }

    private static int computeColumns(Inventory inventory) {
        int size = inventory.size();
        int hotbarSize = PlayerInventory.getHotbarSize();
        if (size < hotbarSize) {
            return Math.max(size, 1);
        }
        if (size % hotbarSize == 0) {
            return hotbarSize;
        }
        int columns = Math.max((int)Math.sqrt(size), 1);
        int rows = Math.max(ceilDiv(inventory.size(), columns), 1);
        return Math.max(columns, rows);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(Inventory inventory) {
        return new GenericContainerTooltipData(inventory);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(Inventory inventory, @Nullable DyeColor color) {
        return new GenericContainerTooltipData(inventory, color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(Inventory inventory, int rows, int columns) {
        return new GenericContainerTooltipData(inventory, rows, columns);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(Inventory inventory, int rows, int columns, @Nullable DyeColor color) {
        return new GenericContainerTooltipData(inventory, rows, columns, color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(DefaultedList<ItemStack> inventory) {
        return GenericContainerTooltipData.of(ListInventory.wrap(inventory));
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(DefaultedList<ItemStack> inventory, @Nullable DyeColor color) {
        return GenericContainerTooltipData.of(ListInventory.wrap(inventory), color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(DefaultedList<ItemStack> inventory, int rows, int columns) {
        return GenericContainerTooltipData.of(ListInventory.wrap(inventory), rows, columns);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance.
     */
    public static GenericContainerTooltipData of(DefaultedList<ItemStack> inventory, int rows, int columns, @Nullable DyeColor color) {
        return GenericContainerTooltipData.of(ListInventory.wrap(inventory), rows, columns, color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(Inventory inventory) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory));
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(Inventory inventory, @Nullable DyeColor color) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory), color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(Inventory inventory, int rows, int columns) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory), rows, columns);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(Inventory inventory, int rows, int columns, @Nullable DyeColor color) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory), rows, columns, color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(DefaultedList<ItemStack> inventory) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory));
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(DefaultedList<ItemStack> inventory, @Nullable DyeColor color) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory), color);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(DefaultedList<ItemStack> inventory, int rows, int columns) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory), rows, columns);
    }

    /**
     * Returns new {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     * @param inventory Inventory shown by the tooltip associated with this tooltip data.
     * @param rows Number of rows used to display tooltip content.
     * @param columns Number of columns used to display tooltip content.
     * @param color {@link DyeColor} of the tooltip represented by this tooltip data.
     * @return New {@link GenericContainerTooltipData} instance that displays the given {@code inventory} in "zipped" ("compact") mode.
     */
    public static GenericContainerTooltipData ofZipped(DefaultedList<ItemStack> inventory, int rows, int columns, @Nullable DyeColor color) {
        return GenericContainerTooltipData.of(InventoryUtil.zip(inventory), rows, columns, color);
    }
}