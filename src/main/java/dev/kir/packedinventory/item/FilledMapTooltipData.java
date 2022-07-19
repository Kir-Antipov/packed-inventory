package dev.kir.packedinventory.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;

public final class FilledMapTooltipData implements TooltipData {
    public static final int DEFAULT_MAP_SIZE = 128;

    private final int id;
    private final MapState mapState;
    private final boolean hidePlayerIcons;
    private final int size;

    public FilledMapTooltipData(ItemStack stack, World world) {
        this(stack, world, false, DEFAULT_MAP_SIZE);
    }

    public FilledMapTooltipData(ItemStack stack, World world, int size) {
        this(stack, world, false, size);
    }

    public FilledMapTooltipData(ItemStack stack, World world, boolean hidePlayerIcons) {
        this(stack, world, hidePlayerIcons, DEFAULT_MAP_SIZE);
    }

    public FilledMapTooltipData(ItemStack stack, World world, boolean hidePlayerIcons, int size) {
        Integer optionalId = FilledMapItem.getMapId(stack);
        this.id = optionalId == null ? -1 : optionalId;
        this.mapState = FilledMapItem.getMapState(optionalId, world);
        this.hidePlayerIcons = hidePlayerIcons;
        this.size = size;
    }

    public FilledMapTooltipData(int id, MapState mapState, boolean hidePlayerIcons, int size) {
        this.id = id;
        this.mapState = mapState;
        this.hidePlayerIcons = hidePlayerIcons;
        this.size = size;
    }

    public int getId() {
        return this.id;
    }

    public MapState getMapState() {
        return this.mapState;
    }

    public boolean shouldHidePlayerIcons() {
        return this.hidePlayerIcons;
    }

    public int getSize() {
        return this.size;
    }
}
