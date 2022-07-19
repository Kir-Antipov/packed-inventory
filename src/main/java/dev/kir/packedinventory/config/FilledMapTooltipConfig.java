package dev.kir.packedinventory.config;

import dev.kir.packedinventory.api.v1.config.TooltipConfig;
import dev.kir.packedinventory.item.FilledMapTooltipData;

public class FilledMapTooltipConfig extends TooltipConfig {
    public static final FilledMapTooltipConfig DEFAULT = new FilledMapTooltipConfig();

    protected int size;

    public FilledMapTooltipConfig() {
        this(false, false, FilledMapTooltipData.DEFAULT_MAP_SIZE);
    }

    public FilledMapTooltipConfig(boolean enable, boolean hidePlayerIcons, int size) {
        super(enable, hidePlayerIcons);
        this.size = size;
    }

    public int size() {
        return this.size;
    }
}
