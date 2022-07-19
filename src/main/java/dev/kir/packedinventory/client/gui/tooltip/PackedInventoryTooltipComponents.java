package dev.kir.packedinventory.client.gui.tooltip;

import dev.kir.packedinventory.api.v1.client.gui.tooltip.GenericContainerTooltipComponent;
import dev.kir.packedinventory.api.v1.item.GenericContainerTooltipData;
import dev.kir.packedinventory.item.FilledMapTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

@Environment(EnvType.CLIENT)
public final class PackedInventoryTooltipComponents {
    static {
        TooltipComponentCallback.EVENT.register(x -> x instanceof GenericContainerTooltipData && x.getClass() == GenericContainerTooltipData.class ? new GenericContainerTooltipComponent((GenericContainerTooltipData)x) : null);
        TooltipComponentCallback.EVENT.register(x -> x instanceof FilledMapTooltipData ? new FilledMapTooltipComponent((FilledMapTooltipData)x) : null);
    }

    public static void initClient() { }
}
