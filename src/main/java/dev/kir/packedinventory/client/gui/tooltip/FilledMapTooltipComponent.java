package dev.kir.packedinventory.client.gui.tooltip;

import dev.kir.packedinventory.item.FilledMapTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;

@Environment(EnvType.CLIENT)
public final class FilledMapTooltipComponent implements TooltipComponent {
    private static final int MAX_LIGHT_LEVEL = (15 << 20) | (15 << 4);
    private static final int VERTICAL_MARGIN = 4;

    private final int id;
    private final MapState mapState;
    private final boolean hidePlayerIcons;
    private final int size;
    private final MapRenderer mapRenderer;

    public FilledMapTooltipComponent(FilledMapTooltipData tooltipData) {
        this.id = tooltipData.getId();
        this.mapState = tooltipData.getMapState();
        this.hidePlayerIcons = tooltipData.shouldHidePlayerIcons();
        this.size = tooltipData.getSize();
        this.mapRenderer = MinecraftClient.getInstance().gameRenderer.getMapRenderer();
    }

    private boolean canRenderMap() {
        return this.id >= 0 && this.mapState != null;
    }

    @Override
    public int getHeight() {
        return this.canRenderMap() ? (this.size + VERTICAL_MARGIN) : 0;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.canRenderMap() ? this.size : 0;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        if (!this.canRenderMap()) {
            return;
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        float scale = (float)this.size / FilledMapTooltipData.DEFAULT_MAP_SIZE;
        matrices.scale(scale, scale, 0);
        VertexConsumerProvider.Immediate vertexConsumers = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        this.mapRenderer.draw(matrices, vertexConsumers, this.id, this.mapState, this.hidePlayerIcons, MAX_LIGHT_LEVEL);
        vertexConsumers.draw();
        matrices.pop();
    }
}
