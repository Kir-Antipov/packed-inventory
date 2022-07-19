package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.FailureReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Opens an inventory view at the selected slot within the target inventory.
 */
@FunctionalInterface
public interface InventoryViewer {
    /**
     * Opens an inventory view at the selected {@code slot} within the target {@code inventory}.
     * @param inventory Inventory.
     * @param slot Inventory slot.
     * @param player Player.
     * @return Inventory view at the selected {@code slot} within the target {@code inventory}.
     */
    Inventory view(Inventory inventory, int slot, PlayerEntity player);

    /**
     * A predicate that checks if the given {@link InventoryViewer} can open an inventory view.
     */
    @FunctionalInterface
    interface Predicate {
        /**
         * Always returns {@code true}.
         */
        Predicate TRUE = (i, s, p) -> true;
        /**
         * Always returns {@code false}.
         */
        Predicate FALSE = (i, s, p) -> false;

        /**
         * Tests if the given {@link InventoryViewer} can open an inventory view.
         * @param inventory Inventory.
         * @param slot Inventory slot.
         * @param player Player.
         * @return {@code true} if the given {@link InventoryViewer} can open an inventory view; otherwise, {@code false}.
         */
        boolean test(Inventory inventory, int slot, PlayerEntity player);

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
                    public boolean test(Inventory inventory, int slot, PlayerEntity player) {
                        return it.test(inventory, slot, player) && other.test(inventory, slot, player);
                    }
                };
            } else {
                return (i, s, p) -> it.test(i, s, p) && other.test(i, s, p);
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
                    public boolean test(Inventory inventory, int slot, PlayerEntity player) {
                        return it.test(inventory, slot, player) || other.test(inventory, slot, player);
                    }
                };
            } else {
                return (p, i, s) -> it.test(p, i, s) || other.test(p, i, s);
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
        default boolean test(Inventory inventory, int slot, PlayerEntity player) {
            return this.getItems().contains(inventory.getStack(slot).getItem());
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
                    public boolean test(Inventory inventory, int slot, PlayerEntity player) {
                        return innerPredicate.test(inventory, slot, player);
                    }
                };
            }
        }
    }

    /**
     * Validator that checks if the given {@link InventoryViewer} should open an inventory view.
     */
    @FunctionalInterface
    interface Validator {
        /**
         * Always passes.
         */
        Validator EMPTY = (i, s, p) -> null;

        /**
         * Tests if the given {@link InventoryViewer} should open an inventory view.
         * @param inventory Inventory.
         * @param slot Inventory slot.
         * @param player Player.
         * @return {@code null} if the given {@link InventoryViewer} should open an inventory view; otherwise, a {@link FailureReason} instance.
         */
        @Nullable FailureReason validate(Inventory inventory, int slot, PlayerEntity player);

        /**
         * Returns a composed validator that represents a short-circuiting logical
         * AND of this validator and another. When evaluating the composed
         * validator, if this validator is not {@code null}, then the {@code other}
         * validator is not evaluated.
         *
         * @param other Validator that will be logically-ANDed with this validator.
         * @return Composed validator that represents the short-circuiting logical AND of this validator and the {@code other} validator.
         */
        default Validator and(Validator other) {
            Validator it = this;
            if (this instanceof ExtendedValidator || other instanceof ExtendedValidator) {
                return (ExtendedValidator)((i, s, p, eI) -> {
                    FailureReason reason = it instanceof ExtendedValidator ? ((ExtendedValidator)it).validate(i, s, p, eI) : it.validate(i, s, p);
                    if (reason != null) {
                        return reason;
                    }
                    return other instanceof ExtendedValidator ? ((ExtendedValidator)other).validate(i, s, p, eI) : other.validate(i, s, p);
                });
            } else {
                return (i, s, p) -> {
                    FailureReason reason = it.validate(i, s, p);
                    return reason == null ? other.validate(i, s, p) : reason;
                };
            }
        }

        /**
         * Returns a composed validator that represents a short-circuiting logical
         * OR of this validator and another. When evaluating the composed
         * validator, if this validator is {@code null}, then the {@code other}
         * validator is not evaluated.
         *
         * @param other Validator that will be logically-ORed with this validator.
         * @return Composed validator that represents the short-circuiting logical OR of this validator and the {@code other} validator.
         */
        default Validator or(Validator other) {
            Validator it = this;
            if (this instanceof ExtendedValidator || other instanceof ExtendedValidator) {
                return (ExtendedValidator)((i, s, p, eI) -> {
                    FailureReason reason = it instanceof ExtendedValidator ? ((ExtendedValidator)it).validate(i, s, p, eI) : it.validate(i, s, p);
                    if (reason == null) {
                        return null;
                    }
                    return other instanceof ExtendedValidator ? ((ExtendedValidator)other).validate(i, s, p, eI) : other.validate(i, s, p);
                });
            } else {
                return (i, s, p) -> {
                    FailureReason reason = it.validate(i, s, p);
                    return reason == null ? null : other.validate(i, s, p);
                };
            }
        }
    }

    /**
     * Extended version of {@link Validator} that needs other inventory views to be open in order to proceed.
     */
    @FunctionalInterface
    interface ExtendedValidator extends Validator {
        /**
         * Always passes.
         */
        ExtendedValidator EMPTY = (p, s, i, eI) -> null;

        /**
         * {@inheritDoc}
         */
        @Override
        default @Nullable FailureReason validate(Inventory inventory, int slot, PlayerEntity player) {
            return this.validate(inventory, slot, player, inventory);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default ExtendedValidator and(Validator other) {
            return (ExtendedValidator)Validator.super.and(other);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default ExtendedValidator or(Validator other) {
            return (ExtendedValidator)Validator.super.or(other);
        }

        /**
         * Tests if the given {@link InventoryViewer} should open an inventory view.
         * @param inventory Inventory.
         * @param slot Inventory slot.
         * @param player Player.
         * @param extendedInventory Combined inventory that consists of all available inventory views in the given {@code inventory}.
         * @return {@code null} if the given {@link InventoryViewer} should open an inventory view; otherwise, a {@link FailureReason} instance.
         */
        @Nullable FailureReason validate(Inventory inventory, int slot, PlayerEntity player, Inventory extendedInventory);
    }
}
