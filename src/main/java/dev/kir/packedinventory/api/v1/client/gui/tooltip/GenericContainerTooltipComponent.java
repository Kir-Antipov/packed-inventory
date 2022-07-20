package dev.kir.packedinventory.api.v1.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kir.packedinventory.api.v1.item.GenericContainerTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * A {@link TooltipComponent} associated with {@link GenericContainerTooltipData}.
 */
@Environment(EnvType.CLIENT)
public class GenericContainerTooltipComponent implements TooltipComponent {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/bundle.png");
    private static final int BORDER_SIZE = 1;
    private static final int VERTICAL_MARGIN = 4;
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int TEXTURE_SIZE = 128;
    private static final float[] TRUE_WHITE = new float[] { 1F, 1F, 1F };

    private final Inventory inventory;
    private final float[] color;
    private final int rows;
    private final int columns;

    /**
     * Creates a {@link GenericContainerTooltipComponent} from the given {@link GenericContainerTooltipData}.
     *
     * @param tooltipData A {@link GenericContainerTooltipData} that contains all the data needed for this {@link TooltipComponent}.
     */
    public GenericContainerTooltipComponent(GenericContainerTooltipData tooltipData) {
        this.inventory = tooltipData.getInventory();
        this.color = tooltipData.getColor() == null ? TRUE_WHITE : tooltipData.getColor().getColorComponents();
        this.rows = tooltipData.getRows();
        this.columns = tooltipData.getColumns();
    }

    /**
     * @return Inventory displayed by this {@link TooltipComponent}.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * @return Shader color ({ R, G, B }) used by this {@link TooltipComponent}.
     */
    public float[] getColor() {
        return this.color;
    }

    /**
     * @return Number of columns of this {@link TooltipComponent}'s {@link Inventory}.
     */
    public int getColumns() {
        return this.columns;
    }

    /**
     * @return Number of rows of this {@link TooltipComponent}'s {@link Inventory}.
     */
    private int getRows() {
        return this.rows;
    }

    /**
     * @return Height of this {@link TooltipComponent}.
     */
    @Override
    public int getHeight() {
        return this.getRows() * SLOT_HEIGHT + BORDER_SIZE * 2 + VERTICAL_MARGIN;
    }

    /**
     * @param textRenderer A {@link TextRenderer} instance that should be used to measure text width.
     * @return Height of this {@link TooltipComponent}.
     */
    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.getColumns() * SLOT_WIDTH + BORDER_SIZE * 2;
    }

    /**
     * Renders this {@link TooltipComponent}'s {@link Inventory}.
     *
     * @param textRenderer A {@link TextRenderer} instance that should be used to render text.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param matrices The {@link MatrixStack} instance.
     * @param itemRenderer A {@link ItemRenderer} instance that should be used to render items.
     * @param z Z-index.
     */
    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
        int columns = this.getColumns();
        int rows = this.getRows();
        for (int rI = 0, i = 0; rI < rows; ++rI) {
            for (int cI = 0; cI < columns; ++cI) {
                int xS = x + cI * SLOT_WIDTH + BORDER_SIZE;
                int yS = y + rI * SLOT_HEIGHT + BORDER_SIZE;
                this.drawSlot(matrices, i++, xS, yS, z, textRenderer, itemRenderer);
            }
        }
        this.drawOutline(matrices, x, y, z, columns, rows);
    }

    /**
     * Renders an item located in this {@link TooltipComponent}'s {@link Inventory} at the given index.
     *
     * @param matrices The {@link MatrixStack} instance.
     * @param index Index of the item that should be rendered.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param z Z-index.
     * @param textRenderer A {@link TextRenderer} instance that should be used to render text.
     * @param itemRenderer A {@link ItemRenderer} instance that should be used to render items.
     */
    protected void drawSlot(MatrixStack matrices, int index, int x, int y, int z, TextRenderer textRenderer, ItemRenderer itemRenderer) {
        Inventory inventory = this.getInventory();
        if (index >= inventory.size()) {
            this.draw(matrices, x, y, z, Sprite.BLOCKED_SLOT);
            return;
        }

        ItemStack itemStack = inventory.getStack(index);
        this.draw(matrices, x, y, z, Sprite.SLOT);
        itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index);
        itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1);
    }

    /**
     * Renders outline.
     *
     * @param matrices The {@link MatrixStack} instance.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param z Z-index.
     * @param columns Number of columns of this {@link TooltipComponent}'s {@link Inventory}.
     * @param rows Number of rows of this {@link TooltipComponent}'s {@link Inventory}.
     */
    protected void drawOutline(MatrixStack matrices, int x, int y, int z, int columns, int rows) {
        this.draw(matrices, x, y, z, Sprite.BORDER_CORNER_TOP);
        this.draw(matrices, x + columns * SLOT_WIDTH + BORDER_SIZE, y, z, Sprite.BORDER_CORNER_TOP);

        for (int i = 0; i < columns; ++i) {
            this.draw(matrices, x + BORDER_SIZE + i * SLOT_WIDTH, y, z, Sprite.BORDER_HORIZONTAL_TOP);
            this.draw(matrices, x + BORDER_SIZE + i * SLOT_WIDTH, y + rows * SLOT_HEIGHT, z, Sprite.BORDER_HORIZONTAL_BOTTOM);
        }

        for (int i = 0; i < rows; ++i) {
            this.draw(matrices, x, y + i * SLOT_HEIGHT + BORDER_SIZE, z, Sprite.BORDER_VERTICAL);
            this.draw(matrices, x + columns * SLOT_WIDTH + BORDER_SIZE, y + i * SLOT_HEIGHT + BORDER_SIZE, z, Sprite.BORDER_VERTICAL);
        }

        this.draw(matrices, x, y + rows * SLOT_HEIGHT, z, Sprite.BORDER_CORNER_BOTTOM);
        this.draw(matrices, x + columns * SLOT_WIDTH + BORDER_SIZE, y + rows * SLOT_HEIGHT, z, Sprite.BORDER_CORNER_BOTTOM);
    }

    private void draw(MatrixStack matrices, int x, int y, int z, Sprite sprite) {
        this.draw(matrices, x, y, z, sprite, this.getColor());
    }

    /**
     * Renders a sprite.
     *
     * @param matrices The {@link MatrixStack} instance.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param z Z-index.
     * @param sprite The {@link Sprite} that should be rendered.
     * @param color Shader color ({ R, G, B }).
     */
    protected void draw(MatrixStack matrices, int x, int y, int z, Sprite sprite, float[] color) {
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        DrawableHelper.drawTexture(matrices, x, y, z, sprite.u, sprite.v, sprite.width, sprite.height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    /**
     * Describes the sprites that can be used by {@link GenericContainerTooltipComponent}.
     */
    protected enum Sprite {
        /**
         * Empty slot sprite.
         */
        SLOT(0, 0, SLOT_WIDTH, SLOT_HEIGHT),

        /**
         * Blocked slot sprite.
         */
        BLOCKED_SLOT(0, 2 * SLOT_HEIGHT, SLOT_WIDTH, SLOT_HEIGHT),

        /**
         * Vertical border sprite.
         */
        BORDER_VERTICAL(0, SLOT_WIDTH, BORDER_SIZE, SLOT_HEIGHT),

        /**
         * Top part of the horizontal border sprite.
         */
        BORDER_HORIZONTAL_TOP(0, SLOT_HEIGHT, SLOT_WIDTH, BORDER_SIZE),

        /**
         * Bottom part of the horizontal border sprite.
         */
        BORDER_HORIZONTAL_BOTTOM(0, 3 * SLOT_HEIGHT, SLOT_WIDTH, BORDER_SIZE),

        /**
         * Top corner border sprite.
         */
        BORDER_CORNER_TOP(0, SLOT_HEIGHT, BORDER_SIZE, BORDER_SIZE),

        /**
         * Bottom corner border sprite.
         */
        BORDER_CORNER_BOTTOM(0, 3 * SLOT_HEIGHT, BORDER_SIZE, BORDER_SIZE);

        /**
         * U value of the sprite.
         */
        public final int u;

        /**
         * V value of the sprite.
         */
        public final int v;

        /**
         * Width of the sprite.
         */
        public final int width;

        /**
         * Height of the sprite.
         */
        public final int height;

        Sprite(int u, int v, int width, int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }
}
