package dev.kir.packedinventory.api.v1.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class InventoryViewHandlerRegistryImpl implements InventoryViewHandlerRegistry {
    public static final InventoryViewHandlerRegistryImpl INSTANCE = new InventoryViewHandlerRegistryImpl();

    private final Map<Item, Entry> itemBasedHandlers = new HashMap<>();
    private final Set<Entry> genericHandlersSet = new HashSet<>();
    private final Deque<Entry> genericHandlers = new ArrayDeque<>();
    private @Nullable Entry defaultHandler = null;

    @Override
    public boolean handle(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player) {
        Entry handlerEntry = this.findHandler(inventory, parentInventory, slot, player);
        if (handlerEntry == null) {
            return false;
        }

        handlerEntry.getHandler().handle(inventory, parentInventory, slot, player);
        return true;
    }

    private @Nullable Entry findHandler(Inventory inventory, Inventory parentInventory, int slot, PlayerEntity player) {
        Entry entry = this.itemBasedHandlers.get(inventory.getStack(slot).getItem());
        if (entry != null && entry.getPredicate().test(inventory, parentInventory, slot, player)) {
            return entry;
        }

        for (Entry genericEntry : this.genericHandlers) {
            if (genericEntry.getPredicate().test(inventory, parentInventory, slot, player)) {
                return genericEntry;
            }
        }

        return this.defaultHandler;
    }

    @Override
    public Entry register(Entry entry) {
        if (entry.getPredicate() instanceof InventoryViewHandler.ItemPredicate) {
            for (Item item : ((InventoryViewHandler.ItemPredicate)entry.getPredicate()).getItems()) {
                this.itemBasedHandlers.put(item, entry);
            }
        } else {
            if (this.genericHandlersSet.add(entry)) {
                this.genericHandlers.push(entry);
            }
        }
        return entry;
    }

    @Override
    public Entry register(InventoryViewHandler handler, Collection<Item> items) {
        return this.register(handler, InventoryViewHandler.ItemPredicate.of(items, InventoryViewHandler.Predicate.TRUE));
    }

    @Override
    public Entry registerDefault(InventoryViewHandler handler) {
        this.defaultHandler = new Entry() {
            @Override
            public InventoryViewHandler getHandler() {
                return handler;
            }

            @Override
            public InventoryViewHandler.Predicate getPredicate() {
                return InventoryViewHandler.Predicate.TRUE;
            }
        };
        return this.defaultHandler;
    }

    @Override
    public boolean unregister(InventoryViewHandler handler) {
        boolean removed = this.itemBasedHandlers.entrySet().removeIf(x -> x.getValue().getHandler() == handler);
        removed |= this.genericHandlersSet.removeIf(x -> x.getHandler() == handler) && this.genericHandlers.removeIf(x -> x.getHandler() == handler);
        if (this.defaultHandler != null && this.defaultHandler.getHandler() == handler) {
            this.defaultHandler = null;
            removed = true;
        }
        return removed;
    }
}
