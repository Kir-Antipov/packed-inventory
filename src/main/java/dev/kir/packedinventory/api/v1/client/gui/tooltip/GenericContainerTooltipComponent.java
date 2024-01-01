package dev.kir.packedinventory.api.v1.client.gui.tooltip;

import dev.kir.packedinventory.api.v1.item.GenericContainerTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * A {@link TooltipComponent} associated with {@link GenericContainerTooltipData}.
 */
@Environment(EnvType.CLIENT)
public class GenericContainerTooltipComponent implements TooltipComponent {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("container/bundle/background");
    private static final int BORDER_SIZE = 1;
    private static final int VERTICAL_MARGIN = 4;
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
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
    public int getRows() {
        return this.rows;
    }

    /**
     * @return Height of this {@link TooltipComponent}.
     */
    @Override
    public int getHeight() {
        return GenericContainerTooltipComponent.getLogicalHeight(this.getRows()) + VERTICAL_MARGIN;
    }

    /**
     * @param textRenderer A {@link TextRenderer} instance that should be used to measure text width.
     * @return Width of this {@link TooltipComponent}.
     */
    @Override
    public int getWidth(TextRenderer textRenderer) {
        return GenericContainerTooltipComponent.getLogicalWidth(this.getColumns());
    }

    /**
     * @return Height of a {@link TooltipComponent} with the specified number of rows.
     */
    private static int getLogicalHeight(int rows) {
        return rows * SLOT_HEIGHT + BORDER_SIZE * 2;
    }

    /**
     * @return Width of a {@link TooltipComponent} with the specified number of columns.
     */
    private static int getLogicalWidth(int columns) {
        return columns * SLOT_WIDTH + BORDER_SIZE * 2;
    }

    /**
     * Renders this {@link TooltipComponent}'s {@link Inventory}.
     *
     * @param textRenderer A {@link TextRenderer} instance that should be used to render text.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param context The {@link DrawContext} instance.
     */
    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int columns = this.getColumns();
        int rows = this.getRows();
        float[] color = this.getColor();

        this.drawOutline(context, x, y, columns, rows, color);

        for (int rI = 0, i = 0; rI < rows; ++rI) {
            for (int cI = 0; cI < columns; ++cI) {
                int xS = x + cI * SLOT_WIDTH + BORDER_SIZE;
                int yS = y + rI * SLOT_HEIGHT + BORDER_SIZE;
                this.drawSlot(context, xS, yS, i++, color, textRenderer);
            }
        }
    }

    /**
     * Renders an item located in this {@link TooltipComponent}'s {@link Inventory} at the given index.
     *
     * @param context The {@link DrawContext} instance.
     * @param index Index of the item that should be rendered.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param color Shader color ({ R, G, B }).
     * @param textRenderer A {@link TextRenderer} instance that should be used to render text.
     */
    protected void drawSlot(DrawContext context, int x, int y, int index, float[] color, TextRenderer textRenderer) {
        Inventory inventory = this.getInventory();
        if (index >= inventory.size()) {
            this.draw(context, x, y, Sprite.BLOCKED_SLOT, color);
            return;
        }

        ItemStack itemStack = inventory.getStack(index);
        this.draw(context, x, y, Sprite.SLOT, color);
        context.drawItem(itemStack, x + 1, y + 1, index);
        context.drawItemInSlot(textRenderer, itemStack, x + 1, y + 1);
    }

    /**
     * Renders outline.
     *
     * @param context The {@link DrawContext} instance.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param columns Number of columns of this {@link TooltipComponent}'s {@link Inventory}.
     * @param rows Number of rows of this {@link TooltipComponent}'s {@link Inventory}.
     * @param color Shader color ({ R, G, B }).
     */
    protected void drawOutline(DrawContext context, int x, int y, int columns, int rows, float[] color) {
        int width = GenericContainerTooltipComponent.getLogicalWidth(columns);
        int height = GenericContainerTooltipComponent.getLogicalHeight(rows);

        context.setShaderColor(color[0], color[1], color[2], 1.0F);
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders a sprite.
     *
     * @param context The {@link DrawContext} instance.
     * @param x X screen coordinate.
     * @param y Y screen coordinate.
     * @param sprite The {@link Sprite} that should be rendered.
     * @param color Shader color ({ R, G, B }).
     */
    protected void draw(DrawContext context, int x, int y, Sprite sprite, float[] color) {
        context.setShaderColor(color[0], color[1], color[2], 1.0F);
        context.drawGuiTexture(sprite.texture, x, y, 0, sprite.width, sprite.height);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Describes the sprites that can be used by {@link GenericContainerTooltipComponent}.
     */
    protected enum Sprite {
        /**
         * Empty slot sprite.
         */
        SLOT(new Identifier("container/bundle/slot"), SLOT_WIDTH, SLOT_HEIGHT),

        /**
         * Blocked slot sprite.
         */
        BLOCKED_SLOT(new Identifier("container/bundle/blocked_slot"), SLOT_WIDTH, SLOT_HEIGHT);

        /**
         * Texture id.
         */
        public final Identifier texture;

        /**
         * Width of the sprite.
         */
        public final int width;

        /**
         * Height of the sprite.
         */
        public final int height;

        Sprite(Identifier texture, int width, int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }
}
