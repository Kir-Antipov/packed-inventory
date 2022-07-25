package dev.kir.packedinventory.api.v1.item;

import dev.kir.packedinventory.api.v1.networking.TooltipSyncRequest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link TooltipProvider} that requires synchronization with server in order to show an accurate tooltip.
 * @param <TTooltipSyncData> Synchronization data type.
 */
public interface SyncedTooltipProvider<TTooltipSyncData extends TooltipSyncData> extends TooltipProvider {
    /**
     * Default synchronization interval.
     */
    long DEFAULT_SYNC_INTERVAL = 5000;

    /**
     * {@inheritDoc}
     */
    @Override
    default Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context) {
        if (this.shouldSync(stack)) {
            this.sync(stack);
        }
        Optional<TTooltipSyncData> syncData = this.getTooltipSyncData(stack);
        return this.getTooltipData(stack, context, syncData.orElse(null));
    }

    /**
     * Returns {@link TooltipData} that contains information about this {@link ItemStack} required by its {@link net.minecraft.client.gui.tooltip.TooltipComponent}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
     * @return {@link TooltipData} that contains information about this {@link ItemStack} required by its {@link net.minecraft.client.gui.tooltip.TooltipComponent}, if any; otherwise, {@link Optional#empty()}.
     */
    default Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData) {
        return TooltipProvider.super.getTooltipData(stack, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context) {
        if (this.shouldSync(stack)) {
            this.sync(stack);
        }
        Optional<TTooltipSyncData> syncData = this.getTooltipSyncData(stack);
        return this.getTooltipText(stack, context, syncData.orElse(null));
    }

    /**
     * Returns tooltip text that describes this {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
     * @return Tooltip text that describes this {@link ItemStack}.
     */
    default List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData) {
        return TooltipProvider.super.getTooltipText(stack, context);
    }

    /**
     * Returns time when the given {@link ItemStack} was last synchronized.
     * @param stack {@link ItemStack} instance.
     * @return Time when the given {@link ItemStack} was last synchronized, in the form number of milliseconds from the epoch of 1970-01-01T00:00:00Z.
     */
    long getLastSyncTime(ItemStack stack);

    /**
     * Returns synchronization interval for the given {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @return Synchronization interval for the given {@link ItemStack}, in the form of number of milliseconds.
     */
    default long getSyncInterval(ItemStack stack) {
        return DEFAULT_SYNC_INTERVAL;
    }

    /**
     * Returns {@code true} if the given {@link ItemStack} should be synchronized; otherwise, {@code false}.
     * @param stack {@link ItemStack} instance.
     * @return {@code true} if the given {@link ItemStack} should be synchronized; otherwise, {@code false}.
     */
    default boolean shouldSync(ItemStack stack) {
        return System.currentTimeMillis() - this.getLastSyncTime(stack) >= this.getSyncInterval(stack);
    }

    /**
     * Synchronizes the given {@link ItemStack} with server.
     * @param stack {@link ItemStack} instance.
     */
    @Environment(EnvType.CLIENT)
    default void sync(ItemStack stack) {
        TooltipSyncRequest.sendToServer(stack);
    }

    /**
     * Returns synchronization data for the given {@link ItemStack} received from the server, if any; otherwise, {@link Optional#empty()}.
     * @param stack {@link ItemStack} instance.
     * @return Synchronization data for the given {@link ItemStack} received from the server, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<TTooltipSyncData> getTooltipSyncData(ItemStack stack);

    /**
     * Updates synchronization data for the given {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @param nbt Synchronization data Nbt.
     */
    void readTooltipSyncDataNbt(ItemStack stack, TooltipProviderContext context, NbtCompound nbt);


    /**
     * Returns new {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
     * @param syncDataSupplier Synchronization data container factory.
     * @param <TTooltipSyncData> Synchronization data type.
     * @return New {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
     */
    static <TTooltipSyncData extends TooltipSyncData> Builder<TTooltipSyncData> builder(Supplier<TTooltipSyncData> syncDataSupplier) {
        return SyncedTooltipProvider.builder(syncDataSupplier, ItemStackComponentStorage.weakMap());
    }

    /**
     * Returns new {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
     * @param syncDataSupplier Synchronization data container factory.
     * @param componentStorage Storage class used to attach custom components to {@link ItemStack}s.
     * @param <TTooltipSyncData> Synchronization data type.
     * @return New {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
     */
    static <TTooltipSyncData extends TooltipSyncData> Builder<TTooltipSyncData> builder(Supplier<TTooltipSyncData> syncDataSupplier, ItemStackComponentStorage<TTooltipSyncData> componentStorage) {
        return new Builder<>(syncDataSupplier, componentStorage);
    }


    /**
     * {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
     * @param <TTooltipSyncData> Synchronization data type.
     */
    final class Builder<TTooltipSyncData extends TooltipSyncData> {
        private final Supplier<TTooltipSyncData> syncDataSupplier;
        private final ItemStackComponentStorage<TTooltipSyncData> componentStorage;
        private @Nullable TooltipSyncIntervalProvider syncInterval;
        private @Nullable TooltipPredicate<TTooltipSyncData> tooltipPredicate;
        private @Nullable SyncedTooltipProvider.Builder.TooltipOptionalDataSupplier<TTooltipSyncData> tooltipDataSupplier;
        private @Nullable TooltipTextSupplier<TTooltipSyncData> tooltipTextSupplier;
        private final List<TooltipTextModifier<TTooltipSyncData>> tooltipTextModifiers = new ArrayList<>();

        private Builder(Supplier<TTooltipSyncData> syncDataSupplier, ItemStackComponentStorage<TTooltipSyncData> componentStorage) {
            this.syncDataSupplier = syncDataSupplier;
            this.componentStorage = componentStorage;
        }

        /**
         * Replaces {@link TooltipData} provided by vanilla logic with the one provided by the specified supplier.
         * @param tooltipDataSupplier Delegate that provides custom {@link TooltipData}.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> optionalTooltipData(TooltipOptionalDataSupplier<TTooltipSyncData> tooltipDataSupplier) {
            this.tooltipDataSupplier = tooltipDataSupplier;
            return this;
        }

        /**
         * Replaces {@link TooltipData} provided by vanilla logic with the one provided by the specified supplier,
         * if it was not {@code null}; otherwise, fallbacks to vanilla behavior.
         *
         * @param tooltipDataSupplier Delegate that provides custom {@link TooltipData}.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> tooltipData(TooltipDataSupplier<TTooltipSyncData> tooltipDataSupplier) {
            this.tooltipDataSupplier = (s, c, d) -> {
                TooltipData tooltipData = d == null ? null : tooltipDataSupplier.getTooltipData(s, c, d);
                return tooltipData == null ? c.getTooltipData() : Optional.of(tooltipData);
            };
            return this;
        }

        /**
         * Replaces {@link TooltipData} provided by vanilla logic with the one provided by the specified supplier.
         * @param tooltipDataSupplier Delegate that provides custom {@link TooltipData}.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> optionalTooltipData(TooltipProvider.Builder.TooltipOptionalDataSupplier tooltipDataSupplier) {
            this.tooltipDataSupplier = (s, c, d) -> tooltipDataSupplier.getTooltipData(s, c);
            return this;
        }

        /**
         * Replaces {@link TooltipData} provided by vanilla logic with the one provided by the specified supplier,
         * if it was not {@code null}; otherwise, fallbacks to vanilla behavior.
         *
         * @param tooltipDataSupplier Delegate that provides custom {@link TooltipData}.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> tooltipData(TooltipProvider.Builder.TooltipDataSupplier tooltipDataSupplier) {
            this.tooltipDataSupplier = (s, c, d) -> {
                TooltipData tooltipData = tooltipDataSupplier.getTooltipData(s, c);
                return tooltipData == null ? c.getTooltipData() : Optional.of(tooltipData);
            };
            return this;
        }

        /**
         * Replaces tooltip text provided by vanilla logic with the one provided by the specified supplier.
         * @param tooltipTextSupplier Delegate that provides custom tooltip text.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> tooltipText(TooltipTextSupplier<TTooltipSyncData> tooltipTextSupplier) {
            this.tooltipTextSupplier = tooltipTextSupplier;
            return this;
        }

        /**
         * Replaces tooltip text provided by vanilla logic with the one provided by the specified supplier.
         * @param tooltipTextSupplier Delegate that provides custom tooltip text.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> tooltipText(TooltipProvider.Builder.TooltipTextSupplier tooltipTextSupplier) {
            this.tooltipTextSupplier = (s, c, d) -> tooltipTextSupplier.getTooltipText(s, c);
            return this;
        }

        /**
         * Modifies tooltip text provided by vanilla logic.
         *
         * <p>
         *     Unlike other methods, this one may be called several times.
         *     Modifications will be applied in the same order these methods were called on the builder.
         * </p>
         *
         * @param tooltipTextModifier Delegate that modifies tooltip text.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> modifyTooltipText(TooltipTextModifier<TTooltipSyncData> tooltipTextModifier) {
            this.tooltipTextModifiers.add(tooltipTextModifier);
            return this;
        }

        /**
         * Modifies tooltip text provided by vanilla logic.
         *
         * <p>
         *     Unlike other methods, this one may be called several times.
         *     Modifications will be applied in the same order these methods were called on the builder.
         * </p>
         *
         * @param tooltipTextModifier Delegate that modifies tooltip text.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> modifyTooltipText(TooltipProvider.Builder.TooltipTextModifier tooltipTextModifier) {
            this.tooltipTextModifiers.add((t, s, c, d) -> tooltipTextModifier.modifyTooltipText(t, s, c));
            return this;
        }

        /**
         * Specifies when this tooltip should be shown instead of vanilla one.
         * @param tooltipPredicate Predicate that specifies when this tooltip should be shown instead of vanilla one.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> when(TooltipPredicate<TTooltipSyncData> tooltipPredicate) {
            this.tooltipPredicate = tooltipPredicate;
            return this;
        }

        /**
         * Specifies when this tooltip should be shown instead of vanilla one.
         * @param tooltipPredicate Predicate that specifies when this tooltip should be shown instead of vanilla one.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> when(TooltipProvider.Builder.TooltipPredicate tooltipPredicate) {
            this.tooltipPredicate = (s, c, d) -> tooltipPredicate.shouldShowTooltip(s, c);
            return this;
        }

        /**
         * Sets synchronization interval for the {@link SyncedTooltipProvider<TTooltipSyncData>} to the fixed number.
         * @param syncInterval Synchronization interval in the form of number of milliseconds.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> syncInterval(long syncInterval) {
            this.syncInterval = s -> syncInterval;
            return this;
        }

        /**
         * Specifies synchronization interval.
         * @param syncInterval Synchronization interval provider.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder<TTooltipSyncData> syncInterval(TooltipSyncIntervalProvider syncInterval) {
            this.syncInterval = syncInterval;
            return this;
        }

        /**
         * Finalizes the build process.
         * @return New {@link SyncedTooltipProvider<TTooltipSyncData>} instance.
         */
        public SyncedTooltipProvider<TTooltipSyncData> build() {
            Supplier<TTooltipSyncData> syncDataSupplier = this.syncDataSupplier;
            ItemStackComponentStorage<TTooltipSyncData> componentStorage = this.componentStorage;
            TooltipSyncIntervalProvider syncIntervalProvider = this.syncInterval == null ? s -> DEFAULT_SYNC_INTERVAL : this.syncInterval;
            TooltipPredicate<TTooltipSyncData> tooltipPredicate = this.tooltipPredicate == null ? (s, c, d) -> true : this.tooltipPredicate;
            TooltipOptionalDataSupplier<TTooltipSyncData> tooltipDataSupplier = this.tooltipDataSupplier == null ? (s, c, d) -> c.getTooltipData() : this.tooltipDataSupplier;
            TooltipTextSupplier<TTooltipSyncData> tooltipTextSupplier = this.tooltipTextSupplier == null ? (s, c, d) -> c.getTooltipText().toList() : this.tooltipTextSupplier;
            List<TooltipTextModifier<TTooltipSyncData>> tooltipTextModifiers = new ArrayList<>(this.tooltipTextModifiers);

            return new SyncedTooltipProvider<>() {
                @Override
                public Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData) {
                    return !tooltipPredicate.shouldShowTooltip(stack, context, syncData) ? context.getTooltipData() : tooltipDataSupplier.getTooltipData(stack, context, syncData);
                }

                @Override
                public List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData) {
                    if (!tooltipPredicate.shouldShowTooltip(stack, context, syncData)) {
                        return context.getTooltipText().toList();
                    }

                    for (TooltipTextModifier<TTooltipSyncData> tooltipTextModifier : tooltipTextModifiers) {
                        tooltipTextModifier.modifyTooltipText(context.getTooltipText(), stack, context, syncData);
                    }
                    return tooltipTextSupplier.getTooltipText(stack, context, syncData);
                }

                @Override
                public long getSyncInterval(ItemStack stack) {
                    return syncIntervalProvider.getSyncInterval(stack);
                }

                @Override
                public long getLastSyncTime(ItemStack stack) {
                    return componentStorage.getLastComponentUpdateTime(stack);
                }

                @Override
                public Optional<TTooltipSyncData> getTooltipSyncData(ItemStack stack) {
                    return componentStorage.getComponent(stack);
                }

                @Override
                public void sync(ItemStack stack) {
                    stack = componentStorage.getReferenceStack(stack);
                    SyncedTooltipProvider.super.sync(stack);
                    componentStorage.attachComponent(stack, componentStorage.getComponent(stack).orElse(null));
                }

                @Override
                public void readTooltipSyncDataNbt(ItemStack stack, TooltipProviderContext context, NbtCompound nbt) {
                    TTooltipSyncData syncData = syncDataSupplier.get();
                    syncData.readNbt(nbt);
                    componentStorage.attachComponent(stack, syncData);
                }
            };
        }

        /**
         * A predicate that checks if a tooltip should be shown.
         * @param <TTooltipSyncData> Synchronization data type.
         */
        @FunctionalInterface
        public interface TooltipPredicate<TTooltipSyncData extends TooltipSyncData> {
            /**
             * Checks if a tooltip should be shown.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
             * @return {@code} true if a tooltip should be shown; otherwise, {@code false}.
             */
            boolean shouldShowTooltip(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData);
        }

        /**
         * Provides optional tooltip data.
         * @param <TTooltipSyncData> Synchronization data type.
         */
        @FunctionalInterface
        public interface TooltipOptionalDataSupplier<TTooltipSyncData extends TooltipSyncData> {
            /**
             * Returns {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
             * @return {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
             */
            Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData);
        }

        /**
         * Provides tooltip data.
         * @param <TTooltipSyncData> Synchronization data type.
         */
        @FunctionalInterface
        public interface TooltipDataSupplier<TTooltipSyncData extends TooltipSyncData> {
            /**
             * Returns {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@code null}.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
             * @return {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@code null}.
             */
            @Nullable TooltipData getTooltipData(ItemStack stack, TooltipProviderContext context, TTooltipSyncData syncData);
        }

        /**
         * Provides tooltip text.
         * @param <TTooltipSyncData> Synchronization data type.
         */
        @FunctionalInterface
        public interface TooltipTextSupplier<TTooltipSyncData extends TooltipSyncData> {
            /**
             * Returns tooltip text for the given {@link ItemStack}.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
             * @return Tooltip text for the given {@link ItemStack}.
             */
            List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData);
        }

        /**
         * Modifies tooltip text.
         * @param <TTooltipSyncData> Synchronization data type.
         */
        @FunctionalInterface
        public interface TooltipTextModifier<TTooltipSyncData extends TooltipSyncData> {
            /**
             * Modifies tooltip text.
             * @param text Tooltip text.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param syncData Synchronization data received from server, if any; otherwise, {@code null}.
             * @param context Context.
             */
            void modifyTooltipText(TooltipText text, ItemStack stack, TooltipProviderContext context, @Nullable TTooltipSyncData syncData);
        }

        /**
         * Provides synchronization interval for the given {@link ItemStack}.
         */
        @FunctionalInterface
        public interface TooltipSyncIntervalProvider {
            /**
             * Returns synchronization interval for the given {@link ItemStack}.
             * @param stack {@link ItemStack} instance.
             * @return Synchronization interval for the given {@link ItemStack}, in the form of number of milliseconds.
             */
            long getSyncInterval(ItemStack stack);
        }
    }
}
