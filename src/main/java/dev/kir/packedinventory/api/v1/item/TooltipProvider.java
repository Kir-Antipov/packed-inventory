package dev.kir.packedinventory.api.v1.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * Provides a tooltip for the given {@link ItemStack}.
 */
public interface TooltipProvider {
    /**
     * Imitates vanilla behavior.
     */
    TooltipProvider VANILLA = new TooltipProvider() { };

    /**
     * Returns {@link TooltipData} that contains information about this {@link ItemStack} required by its {@link net.minecraft.client.gui.tooltip.TooltipComponent}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @return {@link TooltipData} that contains information about this {@link ItemStack} required by its {@link net.minecraft.client.gui.tooltip.TooltipComponent}, if any; otherwise, {@link Optional#empty()}.
     */
    default Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context) {
        return context.getTooltipData();
    }

    /**
     * Returns tooltip text that describes this {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @return Tooltip text that describes this {@link ItemStack}.
     */
    default List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context) {
        return context.getTooltipText().toList();
    }

    /**
     * @return New {@link TooltipProvider} builder.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * {@link TooltipProvider} builder.
     */
    final class Builder {
        private @Nullable TooltipPredicate tooltipPredicate;
        private @Nullable TooltipOptionalDataSupplier tooltipDataSupplier;
        private @Nullable TooltipTextSupplier tooltipTextSupplier;
        private final List<TooltipTextModifier> tooltipTextModifiers = new ArrayList<>();

        private Builder() { }

        /**
         * Replaces {@link TooltipData} provided by vanilla logic with the one provided by the specified supplier.
         * @param tooltipDataSupplier Delegate that provides custom {@link TooltipData}.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder optionalTooltipData(TooltipOptionalDataSupplier tooltipDataSupplier) {
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
        public Builder tooltipData(TooltipDataSupplier tooltipDataSupplier) {
            this.tooltipDataSupplier = (s, c) -> {
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
        public Builder tooltipText(TooltipTextSupplier tooltipTextSupplier) {
            this.tooltipTextSupplier = tooltipTextSupplier;
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
        public Builder modifyTooltipText(TooltipTextModifier tooltipTextModifier) {
            this.tooltipTextModifiers.add(tooltipTextModifier);
            return this;
        }

        /**
         * Specifies when this tooltip should be shown instead of vanilla one.
         * @param tooltipPredicate Predicate that specifies when this tooltip should be shown instead of vanilla one.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder when(TooltipPredicate tooltipPredicate) {
            this.tooltipPredicate = tooltipPredicate;
            return this;
        }

        /**
         * Attaches synchronization data requirements to tooltip provider.
         * @param syncDataSupplier Synchronization data container factory.
         * @param <TTooltipSyncData> Synchronization data type.
         * @return New {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
         */
        public <TTooltipSyncData extends TooltipSyncData> SyncedTooltipProvider.Builder<TTooltipSyncData> useSyncData(Supplier<TTooltipSyncData> syncDataSupplier) {
            return this.useSyncData(syncDataSupplier, ItemStackComponentStorage.weakMap());
        }

        /**
         * Attaches synchronization data requirements to tooltip provider.
         * @param syncDataSupplier Synchronization data container factory.
         * @param componentStorage Storage class used to attach custom components to {@link ItemStack}s.
         * @param <TTooltipSyncData> Synchronization data type.
         * @return New {@link SyncedTooltipProvider<TTooltipSyncData>} builder.
         */
        public <TTooltipSyncData extends TooltipSyncData> SyncedTooltipProvider.Builder<TTooltipSyncData> useSyncData(Supplier<TTooltipSyncData> syncDataSupplier, ItemStackComponentStorage<TTooltipSyncData> componentStorage) {
            SyncedTooltipProvider.Builder<TTooltipSyncData> builder = SyncedTooltipProvider.builder(syncDataSupplier, componentStorage);
            if (this.tooltipDataSupplier != null) {
                builder.optionalTooltipData(this.tooltipDataSupplier);
            }
            if (this.tooltipTextSupplier != null) {
                builder.tooltipText(this.tooltipTextSupplier);
            }
            if (this.tooltipPredicate != null) {
                builder.when(this.tooltipPredicate);
            }
            for (TooltipTextModifier tooltipTextModifier : this.tooltipTextModifiers) {
                builder.modifyTooltipText(tooltipTextModifier);
            }
            return builder;
        }

        /**
         * Finalizes the build process.
         * @return New {@link TooltipProvider} instance.
         */
        public TooltipProvider build() {
            if (this.tooltipDataSupplier == null && this.tooltipTextSupplier == null && this.tooltipTextModifiers.size() == 0) {
                return VANILLA;
            }

            TooltipPredicate tooltipPredicate = this.tooltipPredicate == null ? (s, c) -> true : this.tooltipPredicate;
            TooltipOptionalDataSupplier tooltipDataSupplier = this.tooltipDataSupplier == null ? (s, c) -> c.getTooltipData() : this.tooltipDataSupplier;
            TooltipTextSupplier tooltipTextSupplier = this.tooltipTextSupplier == null ? (s, c) -> c.getTooltipText().toList() : this.tooltipTextSupplier;
            List<TooltipTextModifier> tooltipTextModifiers = new ArrayList<>(this.tooltipTextModifiers);
            return new TooltipProvider() {
                @Override
                public Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context) {
                    return !tooltipPredicate.shouldShowTooltip(stack, context) ? context.getTooltipData() : tooltipDataSupplier.getTooltipData(stack, context);
                }

                @Override
                public List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context) {
                    if (!tooltipPredicate.shouldShowTooltip(stack, context)) {
                        return context.getTooltipText().toList();
                    }

                    for (TooltipTextModifier tooltipTextModifier : tooltipTextModifiers) {
                        tooltipTextModifier.modifyTooltipText(context.getTooltipText(), stack, context);
                    }
                    return tooltipTextSupplier.getTooltipText(stack, context);
                }
            };
        }

        /**
         * A predicate that checks if a tooltip should be shown.
         */
        @FunctionalInterface
        public interface TooltipPredicate {
            /**
             * Checks if a tooltip should be shown.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @return {@code} true if a tooltip should be shown; otherwise, {@code false}.
             */
            boolean shouldShowTooltip(ItemStack stack, TooltipProviderContext context);
        }

        /**
         * Provides tooltip data.
         */
        @FunctionalInterface
        public interface TooltipDataSupplier {
            /**
             * Returns {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@code null}.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @return {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@code null}.
             */
            @Nullable TooltipData getTooltipData(ItemStack stack, TooltipProviderContext context);
        }

        /**
         * Provides optional tooltip data.
         */
        @FunctionalInterface
        public interface TooltipOptionalDataSupplier {
            /**
             * Returns {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @return {@link TooltipData} for the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
             */
            Optional<TooltipData> getTooltipData(ItemStack stack, TooltipProviderContext context);
        }

        /**
         * Provides tooltip text.
         */
        @FunctionalInterface
        public interface TooltipTextSupplier {
            /**
             * Returns tooltip text for the given {@link ItemStack}.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             * @return Tooltip text for the given {@link ItemStack}.
             */
            List<Text> getTooltipText(ItemStack stack, TooltipProviderContext context);
        }

        /**
         * Modifies tooltip text.
         */
        @FunctionalInterface
        public interface TooltipTextModifier {
            /**
             * Modifies tooltip text.
             * @param text Tooltip text.
             * @param stack {@link ItemStack} that requested a tooltip.
             * @param context Context.
             */
            void modifyTooltipText(TooltipText text, ItemStack stack, TooltipProviderContext context);
        }
    }

    /**
     * A predicate that checks if the given {@link TooltipProvider} can provide tooltip for the given {@link ItemStack}.
     */
    @FunctionalInterface
    interface Predicate {
        /**
         * Always returns {@code true}.
         */
        Predicate TRUE = (s, c) -> true;
        /**
         * Always returns {@code false}.
         */
        Predicate FALSE = (s, c) -> false;

        /**
         * Tests if the given {@link TooltipProvider} can provide tooltip for the given {@link ItemStack}.
         * @param stack {@link ItemStack} instance.
         * @param context Context.
         * @return {@code true} if the given {@link TooltipProvider} can provide tooltip for the given {@link ItemStack}; otherwise, {@code false}.
         */
        boolean test(ItemStack stack, TooltipProviderContext context);

        /**
         * Returns a composed predicate that represents a short-circuiting logical
         * AND of this predicate and another. When evaluating the composed
         * predicate, if this predicate is {@code false}, then the {@code other}
         * predicate is not evaluated.
         *
         * @param other Predicate that will be logically-ANDed with this predicate.
         * @return Composed predicate that represents the short-circuiting logical AND of this predicate and the {@code other} predicate.
         */
        default Predicate and(Predicate other) {
            Set<Item> items = null;
            if (this instanceof ItemPredicate) {
                items = ((ItemPredicate)this).getItems();
            }
            if (other instanceof ItemPredicate) {
                if (items == null) {
                    items = ((ItemPredicate)other).getItems();
                } else {
                    items = new HashSet<>(items);
                    items.retainAll(((ItemPredicate)other).getItems());
                    items = Set.copyOf(items);
                }
            }

            Predicate it = this;
            if (items != null) {
                Set<Item> finalItems = items;
                return new ItemPredicate() {
                    @Override
                    public Set<Item> getItems() {
                        return finalItems;
                    }

                    @Override
                    public boolean test(ItemStack stack, TooltipProviderContext context) {
                        return it.test(stack, context) && other.test(stack, context);
                    }
                };
            } else {
                return (s, c) -> it.test(s, c) && other.test(s, c);
            }
        }

        /**
         * Returns a composed predicate that represents a short-circuiting logical
         * OR of this predicate and another. When evaluating the composed
         * predicate, if this predicate is {@code true}, then the {@code other}
         * predicate is not evaluated.
         *
         * @param other Predicate that will be logically-ORed with this predicate.
         * @return Composed predicate that represents the short-circuiting logical OR of this predicate and the {@code other} predicate.
         */
        default Predicate or(Predicate other) {
            Set<Item> items = null;
            if (this instanceof ItemPredicate) {
                items = ((ItemPredicate)this).getItems();
            }
            if (other instanceof ItemPredicate) {
                if (items == null) {
                    items = ((ItemPredicate)other).getItems();
                } else {
                    items = new HashSet<>(items);
                    items.addAll(((ItemPredicate)other).getItems());
                    items = Set.copyOf(items);
                }
            }

            Predicate it = this;
            if (items != null) {
                Set<Item> finalItems = items;
                return new ItemPredicate() {
                    @Override
                    public Set<Item> getItems() {
                        return finalItems;
                    }

                    @Override
                    public boolean test(ItemStack stack, TooltipProviderContext context) {
                        return it.test(stack, context) || other.test(stack, context);
                    }
                };
            } else {
                return (s, c) -> it.test(s, c) || other.test(s, c);
            }
        }
    }

    /**
     * Item-based version of {@link Predicate} that succeeds only for the given range of items.
     */
    @FunctionalInterface
    interface ItemPredicate extends Predicate {
        /**
         * @return Range of items associated with this {@link ItemPredicate}.
         */
        Set<Item> getItems();

        /**
         * {@inheritDoc}
         */
        @Override
        default boolean test(ItemStack stack, TooltipProviderContext context) {
            return this.getItems().contains(stack.getItem());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default ItemPredicate and(Predicate other) {
            return (ItemPredicate)Predicate.super.and(other);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default ItemPredicate or(Predicate other) {
            return (ItemPredicate)Predicate.super.or(other);
        }

        /**
         * Creates new {@link ItemPredicate} that succeeds only for the given range of items.
         * @param items Items associated with new {@link ItemPredicate}.
         * @return New {@link ItemPredicate} instance that succeeds only for the given range of items.
         */
        static ItemPredicate of(Collection<Item> items) {
            return ItemPredicate.of(items, null);
        }

        /**
         * Creates new {@link ItemPredicate} that succeeds only when {@code innerPredicate} succeeds.
         * @param items Items associated with new {@link ItemPredicate}.
         * @param innerPredicate Inner predicate.
         * @return New {@link ItemPredicate} instance that succeeds only when {@code innerPredicate} succeeds.
         */
        static ItemPredicate of(Collection<Item> items, @Nullable Predicate innerPredicate) {
            Set<Item> itemSet = Set.copyOf(items);
            if (innerPredicate == null) {
                return () -> itemSet;
            } else {
                return new ItemPredicate() {
                    @Override
                    public Set<Item> getItems() {
                        return itemSet;
                    }

                    @Override
                    public boolean test(ItemStack stack, TooltipProviderContext context) {
                        return innerPredicate.test(stack, context);
                    }
                };
            }
        }
    }
}
