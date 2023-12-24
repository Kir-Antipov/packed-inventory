package dev.kir.packedinventory.api.v1.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Context used by {@link TooltipProvider}.
 */
public interface TooltipProviderContext {
    /**
     * @return {@link TooltipContext} instance.
     */
    TooltipContext getTooltipContext();

    /**
     * @return Player, if any; otherwise, {@code null}.
     */
    @Nullable PlayerEntity getPlayer();

    /**
     * @return Tooltip text.
     */
    TooltipText getTooltipText();

    /**
     * @return {@link TooltipData}, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<TooltipData> getTooltipData();


    /**
     * @return Empty {@link TooltipProviderContext}.
     */
    @Environment(EnvType.CLIENT)
    static TooltipProviderContext of() {
        return TooltipProviderContext.of(null, Optional.empty());
    }

    /**
     * Returns {@link TooltipProviderContext} without tooltip text, or {@link TooltipData}.
     * @param player Player.
     * @param context {@link TooltipContext} instance.
     * @return {@link TooltipProviderContext} without tooltip text, or {@link TooltipData}.
     */
    static TooltipProviderContext of(@Nullable PlayerEntity player, TooltipContext context) {
        return TooltipProviderContext.of(null, Optional.empty(), player, context);
    }

    /**
     * Returns {@link TooltipProviderContext} without tooltip text.
     * @param data {@link TooltipData} instance.
     * @return {@link TooltipProviderContext} without tooltip text.
     */
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static TooltipProviderContext of(Optional<TooltipData> data) {
        return TooltipProviderContext.of(null, data);
    }

    /**
     * Returns {@link TooltipProviderContext} without tooltip text.
     * @param data {@link TooltipData} instance.
     * @param player Player.
     * @param context {@link TooltipContext} instance.
     * @return {@link TooltipProviderContext} without tooltip text.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static TooltipProviderContext of(Optional<TooltipData> data, @Nullable PlayerEntity player, TooltipContext context) {
        return TooltipProviderContext.of(null, data, player, context);
    }

    /**
     * Returns {@link TooltipProviderContext} without {@link TooltipData}.
     * @param text Tooltip text.
     * @return {@link TooltipProviderContext} without {@link TooltipData}.
     */
    @Environment(EnvType.CLIENT)
    static TooltipProviderContext of(TooltipText text) {
        return TooltipProviderContext.of(text, Optional.empty());
    }

    /**
     * Returns {@link TooltipProviderContext} without {@link TooltipData}.
     * @param text Tooltip text.
     * @param player Player.
     * @param context {@link TooltipContext} instance.
     * @return {@link TooltipProviderContext} without {@link TooltipData}.
     */
    static TooltipProviderContext of(TooltipText text, @Nullable PlayerEntity player, TooltipContext context) {
        return TooltipProviderContext.of(text, Optional.empty(), player, context);
    }

    /**
     * Returns fully constructed {@link TooltipProviderContext}.
     * @param text Tooltip text.
     * @param data {@link TooltipData} instance.
     * @return Fully constructed {@link TooltipProviderContext}.
     */
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static TooltipProviderContext of(@Nullable TooltipText text, Optional<TooltipData> data) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        TooltipContext context = client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC;
        return TooltipProviderContext.of(text, data, player, context);
    }

    /**
     * Returns fully constructed {@link TooltipProviderContext}.
     * @param text Tooltip text.
     * @param data {@link TooltipData} instance.
     * @param player Player.
     * @param context {@link TooltipContext} instance.
     * @return Fully constructed {@link TooltipProviderContext}.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static TooltipProviderContext of(@Nullable TooltipText text, Optional<TooltipData> data, @Nullable PlayerEntity player, TooltipContext context) {
        return new TooltipProviderContext() {
            private @Nullable TooltipText tooltipText = text;

            @Override
            public TooltipContext getTooltipContext() {
                return context;
            }

            @Override
            public @Nullable PlayerEntity getPlayer() {
                return player;
            }

            @Override
            public TooltipText getTooltipText() {
                if (this.tooltipText == null) {
                    this.tooltipText = TooltipText.empty();
                }
                return this.tooltipText;
            }

            @Override
            public Optional<TooltipData> getTooltipData() {
                return data;
            }
        };
    }
}
