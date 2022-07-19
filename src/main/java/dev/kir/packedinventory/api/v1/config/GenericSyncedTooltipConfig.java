package dev.kir.packedinventory.api.v1.config;

import dev.kir.packedinventory.api.v1.item.SyncedTooltipProvider;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link GenericTooltipConfig} extension that contains properties specific for synced tooltips.
 */
public class GenericSyncedTooltipConfig extends GenericTooltipConfig {
    /**
     * Default instance of {@link GenericSyncedTooltipConfig}.
     */
    public static final GenericSyncedTooltipConfig DEFAULT = new GenericSyncedTooltipConfig();

    /**
     * Determines how often synchronization should occur.
     */
    protected long syncInterval;

    /**
     * Constructs default {@link GenericSyncedTooltipConfig} instance.
     */
    public GenericSyncedTooltipConfig() {
        this(true, false, false, -1, -1, false, null);
    }

    /**
     * Constructs new {@link GenericSyncedTooltipConfig} instance.
     *
     * @param enable Indicates whether a tooltip should be enabled by default or not.
     * @param compact Indicates whether tooltip compact mode should be enabled by default or not.
     * @param showWhenEmpty Indicates whether a tooltip should be shown when its content is empty or not.
     * @param usePredefinedColor Indicates whether a tooltip should use item color or not.
     * @param color Specifies default tooltip color in case usePredefinedColor is set to {@code false}, or item color cannot be automatically determined.
     */
    public GenericSyncedTooltipConfig(boolean enable, boolean compact, boolean showWhenEmpty, boolean usePredefinedColor, @Nullable DyeColor color) {
        this(enable, compact, showWhenEmpty, -1, -1, usePredefinedColor, color);
    }

    /**
     * Constructs new {@link GenericSyncedTooltipConfig} instance.
     *
     * @param enable Indicates whether a tooltip should be enabled by default or not.
     * @param compact Indicates whether tooltip compact mode should be enabled by default or not.
     * @param showWhenEmpty Indicates whether a tooltip should be shown when its content is empty or not.
     * @param rows Specifies the number of rows used to display tooltip content.
     * @param columns Specifies the number of columns used to display tooltip content.
     * @param usePredefinedColor Indicates whether a tooltip should use item color or not.
     * @param color Specifies default tooltip color in case usePredefinedColor is set to {@code false}, or item color cannot be automatically determined.
     */
    public GenericSyncedTooltipConfig(boolean enable, boolean compact, boolean showWhenEmpty, int rows, int columns, boolean usePredefinedColor, @Nullable DyeColor color) {
        this(enable, compact, showWhenEmpty, rows, columns, usePredefinedColor, color, SyncedTooltipProvider.DEFAULT_SYNC_INTERVAL);
    }

    /**
     * Constructs new {@link GenericSyncedTooltipConfig} instance.
     *
     * @param enable Indicates whether a tooltip should be enabled by default or not.
     * @param compact Indicates whether tooltip compact mode should be enabled by default or not.
     * @param showWhenEmpty Indicates whether a tooltip should be shown when its content is empty or not.
     * @param rows Specifies the number of rows used to display tooltip content.
     * @param columns Specifies the number of columns used to display tooltip content.
     * @param usePredefinedColor Indicates whether a tooltip should use item color or not.
     * @param color Specifies default tooltip color in case usePredefinedColor is set to {@code false}, or item color cannot be automatically determined.
     * @param syncInterval Determines how often synchronization of tooltip content should occur.
     */
    public GenericSyncedTooltipConfig(boolean enable, boolean compact, boolean showWhenEmpty, int rows, int columns, boolean usePredefinedColor, @Nullable DyeColor color, long syncInterval) {
        super(enable, compact, showWhenEmpty, rows, columns, usePredefinedColor, color);
        this.syncInterval = syncInterval;
    }

    /**
     * @return Synchronization interval in milliseconds.
     */
    public long syncInterval() {
        return this.syncInterval;
    }
}
