package dev.kir.packedinventory.api.v1.config;

import dev.kir.packedinventory.input.PackedInventoryKeyBindings;
import dev.kir.packedinventory.util.input.InputUtil;

/**
 * Base class for tooltip configurations.
 */
public class TooltipConfig {
    /**
     * Default instance of {@link TooltipConfig}.
     */
    public static final TooltipConfig DEFAULT = new TooltipConfig();

    /**
     * Indicates whether a tooltip should be enabled by default or not.
     */
    protected boolean enable;
    /**
     * Indicates whether tooltip compact mode should be enabled by default or not.
     */
    protected boolean compact;

    /**
     * Constructs default {@link TooltipConfig} instance.
     */
    public TooltipConfig() {
        this(true, false);
    }

    /**
     * Constructs new {@link TooltipConfig} instance.
     *
     * @param enable Indicates whether a tooltip should be enabled by default or not.
     * @param compact Indicates whether tooltip compact mode should be enabled by default or not.
     */
    public TooltipConfig(boolean enable, boolean compact) {
        this.enable = enable;
        this.compact = compact;
    }

    /**
     * @return Flag that indicates whether a tooltip should is enabled or not.
     * @apiNote Unlike most other methods, this one is context-aware. It may return different values depending on user input.
     */
    public boolean isEnabled() {
        return this.enable ^ TooltipConfig.shouldInvertVisibility();
    }

    /**
     * @return Flag that indicates whether tooltip compact mode is enabled or not.
     * @apiNote Unlike most other methods, this one is context-aware. It may return different values depending on user input.
     */
    public boolean isCompact() {
        return this.compact ^ TooltipConfig.shouldInvertCompactMode();
    }

    /**
     * @return Flag that indicates whether tooltip visibility should be inverted or not.
     */
    public static boolean shouldInvertVisibility() {
        return InputUtil.isKeyBindingPressed(PackedInventoryKeyBindings.INVERT_TOOLTIP_VISIBILITY);
    }

    /**
     * @return Flag that indicates whether tooltip visibility should be inverted or not.
     */
    public static boolean shouldInvertCompactMode() {
        return InputUtil.isKeyBindingPressed(PackedInventoryKeyBindings.INVERT_TOOLTIP_COMPACT_MODE);
    }
}
