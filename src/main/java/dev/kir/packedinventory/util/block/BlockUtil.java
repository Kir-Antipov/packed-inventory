package dev.kir.packedinventory.util.block;

import dev.kir.packedinventory.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class BlockUtil {
    private static final Map<MapColor, DyeColor> COLORS = new HashMap<>();

    public static @Nullable DyeColor getColor(Block block) {
        if (block instanceof ShulkerBoxBlock) {
            return ShulkerBoxBlock.getColor(block);
        }
        if (block instanceof WallBannerBlock) {
            return ((WallBannerBlock)block).getColor();
        }
        return mapColorToDyeColor(block.getDefaultMapColor());
    }

    public static DyeColor mapColorToDyeColor(MapColor color) {
        return COLORS.get(color);
    }

    private BlockUtil() { }

    static {
        Map<DyeColor, float[]> dyeColors = Arrays.stream(DyeColor.values()).collect(Collectors.toMap(x -> x, x -> ColorUtil.RGB2LAB(x.getColorComponents())));
        for (int i = 0; i < 64; ++i) {
            MapColor mapColor = MapColor.get(i);
            float[] lab = ColorUtil.RGB2LAB(ColorUtil.toRGB(mapColor.color));
            DyeColor dyeColor = dyeColors.keySet().stream().map(x -> new Pair<>(x, ColorUtil.computeXYZDistance(lab, dyeColors.get(x)))).min(Comparator.comparingDouble(Pair::getRight)).map(Pair::getLeft).orElse(DyeColor.WHITE);
            COLORS.put(mapColor, dyeColor);
        }
    }
}
