package dev.kir.packedinventory.api.v1.inventory;

import com.mojang.datafixers.util.Either;
import dev.kir.packedinventory.api.v1.FailureReason;
import dev.kir.packedinventory.inventory.CombinedInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class InventoryViewerRegistryImpl implements InventoryViewerRegistry {
    public static final InventoryViewerRegistryImpl INSTANCE = new InventoryViewerRegistryImpl();

    private final Map<Item, Entry> itemBasedInventoryViewers = new HashMap<>();
    private final Set<Entry> genericInventoryViewersSet = new HashSet<>();
    private final Deque<Entry> genericInventoryViewers = new ArrayDeque<>();

    @Override
    public Optional<Inventory> forceView(Inventory inventory, int slot, PlayerEntity player) {
        Entry entry = this.findViewerEntry(inventory, slot, player);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.getInventoryViewer().view(inventory, slot, player));
    }

    @Override
    public Optional<Either<Inventory, FailureReason>> view(Inventory inventory, int slot, PlayerEntity player) {
        Entry entry = this.findViewerEntry(inventory, slot, player);
        if (entry == null) {
            return Optional.empty();
        }

        FailureReason reason = entry.getValidator().validate(inventory, slot, player);
        if (reason != null && entry.getValidator() instanceof InventoryViewer.ExtendedValidator) {
            List<Pair<Entry, Integer>> innerViewers = new ArrayList<>();
            List<Inventory> inventories = new ArrayList<>();
            inventories.add(inventory);
            if (player.getInventory() != inventory) {
                inventories.add(player.getInventory());
            }

            for (int i = 0; i < inventory.size(); ++i) {
                if (i == slot) {
                    continue;
                }

                Entry innerEntry = this.findViewerEntry(inventory, i, player);
                if (innerEntry == null) {
                    continue;
                }

                if (innerEntry.getValidator().validate(inventory, slot, player) == null) {
                    inventories.add(innerEntry.getInventoryViewer().view(inventory, i, player));
                } else if (innerEntry.getValidator() instanceof InventoryViewer.ExtendedValidator) {
                    innerViewers.add(new Pair<>(innerEntry, i));
                }
            }

            InventoryViewer.ExtendedValidator extendedValidator = (InventoryViewer.ExtendedValidator)entry.getValidator();
            while (true) {
                Inventory extendedInventory = CombinedInventory.of(inventories);
                reason = extendedValidator.validate(inventory, slot, player, extendedInventory);
                if (reason == null) {
                    break;
                }

                boolean wasAdded = innerViewers.removeIf(x -> {
                    if (((InventoryViewer.ExtendedValidator)x.getLeft().getValidator()).validate(inventory, x.getRight(), player, extendedInventory) == null) {
                        inventories.add(x.getLeft().getInventoryViewer().view(inventory, x.getRight(), player));
                        return true;
                    }
                    return false;
                });

                if (!wasAdded) {
                    break;
                }
            }
        }

        if (reason != null) {
            return Optional.of(Either.right(reason));
        }
        return Optional.of(Either.left(entry.getInventoryViewer().view(inventory, slot, player)));
    }

    private @Nullable Entry findViewerEntry(Inventory inventory, int slot, PlayerEntity player) {
        Entry entry = this.itemBasedInventoryViewers.get(inventory.getStack(slot).getItem());
        if (entry != null && entry.getPredicate().test(inventory, slot, player)) {
            return entry;
        }

        for (Entry genericEntry : this.genericInventoryViewers) {
            if (genericEntry.getPredicate().test(inventory, slot, player)) {
                return genericEntry;
            }
        }
        return null;
    }

    @Override
    public Entry register(Entry entry) {
        if (entry.getPredicate() instanceof InventoryViewer.ItemPredicate) {
            for (Item item : ((InventoryViewer.ItemPredicate)entry.getPredicate()).getItems()) {
                this.itemBasedInventoryViewers.put(item, entry);
            }
        } else {
            if (this.genericInventoryViewersSet.add(entry)) {
                this.genericInventoryViewers.push(entry);
            }
        }
        return entry;
    }

    @Override
    public Entry register(InventoryViewer viewer, InventoryViewer.Validator validator, Collection<Item> items) {
        return this.register(viewer, validator, InventoryViewer.ItemPredicate.of(items, InventoryViewer.Predicate.TRUE));
    }

    @Override
    public boolean unregister(InventoryViewer viewer) {
        return this.itemBasedInventoryViewers.entrySet().removeIf(x -> x.getValue().getInventoryViewer() == viewer) | this.genericInventoryViewersSet.removeIf(x -> x.getInventoryViewer() == viewer) && this.genericInventoryViewers.removeIf(x -> x.getInventoryViewer() == viewer);
    }
}
