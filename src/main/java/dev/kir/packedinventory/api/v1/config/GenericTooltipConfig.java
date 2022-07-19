package dev.kir.packedinventory.api.v1.config;

import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link TooltipConfig} extension that should suit most tooltips.
 */
public class GenericTooltipConfig extends TooltipConfig {
    /**
     * Default instance of {@link GenericTooltipConfig}.
     */
    public static final GenericTooltipConfig DEFAULT = new GenericTooltipConfig();

    /**
     * Indicates whether a tooltip should be shown when its content is empty or not.
     */
    protected boolean showWhenEmpty;
    /**
     * Specifies the number of rows used to display tooltip content.
     */
    protected int rows;
    /**
     * Specifies the number of columns used to display tooltip content.
     */
    protected int columns;
    /**
     * Indicates whether a tooltip should use item color or not.
     */
    protected boolean usePredefinedColor;
    /**
     * Specifies default tooltip color in case {@link this#usePredefinedColor} is set to {@code false}, or item color cannot be automatically determined.
     */
    protected @Nullable DyeColor color;

    /**
     * Constructs default {@link GenericTooltipConfig} instance.
     */
    public GenericTooltipConfig() {
        this(true, false, false, -1, -1, false, null);
    }

    /**
     * Constructs new {@link GenericTooltipConfig} instance.
     *
     * @param enable Indicates whether a tooltip should be enabled by default or not.
     * @param compact Indicates whether tooltip compact mode should be enabled by default or not.
     * @param showWhenEmpty Indicates whether a tooltip should be shown when its content is empty or not.
     * @param usePredefinedColor Indicates whether a tooltip should use item color or not.
     * @param color Specifies default tooltip color in case usePredefinedColor is set to {@code false}, or item color cannot be automatically determined.
     */
    public GenericTooltipConfig(boolean enable, boolean compact, boolean showWhenEmpty, boolean usePredefinedColor, @Nullable DyeColor color) {
        this(enable, compact, showWhenEmpty, -1, -1, usePredefinedColor, color);
    }

    /**
     * Constructs new {@link GenericTooltipConfig} instance.
     *
     * @param enable Indicates whether a tooltip should be enabled by default or not.
     * @param compact Indicates whether tooltip compact mode should be enabled by default or not.
     * @param showWhenEmpty Indicates whether a tooltip should be shown when its content is empty or not.
     * @param rows Specifies the number of rows used to display tooltip content.
     * @param columns Specifies the number of columns used to display tooltip content.
     * @param usePredefinedColor Indicates whether a tooltip should use item color or not.
     * @param color Specifies default tooltip color in case usePredefinedColor is set to {@code false}, or item color cannot be automatically determined.
     */
    public GenericTooltipConfig(boolean enable, boolean compact, boolean showWhenEmpty, int rows, int columns, boolean usePredefinedColor, @Nullable DyeColor color) {
        super(enable, compact);
        this.showWhenEmpty = showWhenEmpty;
        this.rows = rows;
        this.columns = columns;
        this.usePredefinedColor = usePredefinedColor;
        this.color = color;
    }

    /**
     * @return Flag that indicates whether a tooltip should be shown when its content is empty or not.
     */
    public boolean shouldShowWhenEmpty() {
        return this.showWhenEmpty;
    }

    /**
     * @return Number of rows used to display tooltip content.
     */
    public int rows() {
        return this.rows;
    }

    /**
     * @return Number of columns used to display tooltip content.
     */
    public int columns() {
        return this.columns;
    }

    /**
     * @return Flag that indicates whether a tooltip should use item color or not.
     */
    public boolean usePredefinedColor() {
        return this.usePredefinedColor;
    }

    /**
     * @return Default tooltip color in case {@link this#usePredefinedColor} is set to {@code false}, or item color cannot be automatically determined.
     */
    public @Nullable DyeColor color() {
        return this.color;
    }
}
