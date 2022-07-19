package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles newly opened inventory views.
 */
@FunctionalInterface
public interface InventoryViewHandler {
    /**
     * Handles an inventory view.
     * @param inventory Inventory view to be handled.
     * @param parentInventory Parent inventory of the given {@code inventory}.
     * @param slot Slot index where {@code inventory} is located in the {@code parentInventory}.
     * @param player Player.
     */
    void handle(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player);

    /**
     * A predicate that checks if the given {@link InventoryViewHandler} can handle an inventory view.
     */
    @FunctionalInterface
    interface Predicate {
        /**
         * Always returns {@code true}.
         */
        Predicate TRUE = (i, pI, s, p) -> true;
        /**
         * Always returns {@code false}.
         */
        Predicate FALSE = (i, pI, s, p) -> false;

        /**
         * Tests if the given {@link InventoryViewHandler} can handle an inventory view.
         * @param inventory Inventory view to be handled.
         * @param parentInventory Parent inventory of the given {@code inventory}.
         * @param slot Slot index where {@code inventory} is located in the {@code parentInventory}.
         * @param player Player.
         * @return {@code true} if the given {@link InventoryViewHandler} can handle an inventory view; otherwise, {@code false}.
         */
        boolean test(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player);

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
                    public boolean test(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player) {
                        return it.test(inventory, parentInventory, slot, player) && other.test(inventory, parentInventory, slot, player);
                    }
                };
            } else {
                return (i, pI, s, p) -> it.test(i, pI, s, p) && other.test(i, pI, s, p);
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
                    public boolean test(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player) {
                        return it.test(inventory, parentInventory, slot, player) || other.test(inventory, parentInventory, slot, player);
                    }
                };
            } else {
                return (i, pI, s, p) -> it.test(i, pI, s, p) || other.test(i, pI, s, p);
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
        default boolean test(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player) {
            return this.getItems().contains(parentInventory.getStack(slot).getItem());
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
                    public boolean test(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player) {
                        return innerPredicate.test(inventory, parentInventory, slot, player);
                    }
                };
            }
        }
    }
}
