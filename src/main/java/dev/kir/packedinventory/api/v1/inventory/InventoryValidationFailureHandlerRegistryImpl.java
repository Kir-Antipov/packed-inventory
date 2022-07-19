package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.FailureReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class InventoryValidationFailureHandlerRegistryImpl implements InventoryValidationFailureHandlerRegistry {
    public static final InventoryValidationFailureHandlerRegistryImpl INSTANCE = new InventoryValidationFailureHandlerRegistryImpl();

    private final Map<Item, Entry> itemBasedHandlers = new HashMap<>();
    private final Set<Entry> genericHandlersSet = new HashSet<>();
    private final Deque<Entry> genericHandlers = new ArrayDeque<>();
    private @Nullable Entry defaultHandler = null;

    @Override
    public boolean handle(FailureReason failureReason, Inventory inventory, int slot, PlayerEntity player) {
        Entry handlerEntry = this.findHandler(failureReason, inventory, slot, player);
        if (handlerEntry == null) {
            return false;
        }

        handlerEntry.getHandler().handle(failureReason, inventory, slot, player);
        return true;
    }

    private @Nullable Entry findHandler(FailureReason failureReason, Inventory inventory, int slot, PlayerEntity player) {
        Entry entry = this.itemBasedHandlers.get(inventory.getStack(slot).getItem());
        if (entry != null && entry.getPredicate().test(failureReason, inventory, slot, player)) {
            return entry;
        }

        for (Entry genericEntry : this.genericHandlers) {
            if (genericEntry.getPredicate().test(failureReason, inventory, slot, player)) {
                return genericEntry;
            }
        }

        return this.defaultHandler;
    }

    @Override
    public Entry register(Entry entry) {
        if (entry.getPredicate() instanceof InventoryValidationFailureHandler.ItemPredicate) {
            for (Item item : ((InventoryValidationFailureHandler.ItemPredicate)entry.getPredicate()).getItems()) {
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
    public Entry register(InventoryValidationFailureHandler handler, Collection<Item> items) {
        return this.register(handler, InventoryValidationFailureHandler.ItemPredicate.of(items, InventoryValidationFailureHandler.Predicate.TRUE));
    }

    @Override
    public Entry registerDefault(InventoryValidationFailureHandler handler) {
        this.defaultHandler = new Entry() {
            @Override
            public InventoryValidationFailureHandler getHandler() {
                return handler;
            }

            @Override
            public InventoryValidationFailureHandler.Predicate getPredicate() {
                return InventoryValidationFailureHandler.Predicate.TRUE;
            }
        };
        return this.defaultHandler;
    }

    @Override
    public boolean unregister(InventoryValidationFailureHandler handler) {
        boolean removed = this.itemBasedHandlers.entrySet().removeIf(x -> x.getValue().getHandler() == handler);
        removed |= this.genericHandlersSet.removeIf(x -> x.getHandler() == handler) && this.genericHandlers.removeIf(x -> x.getHandler() == handler);
        if (this.defaultHandler != null && this.defaultHandler.getHandler() == handler) {
            this.defaultHandler = null;
            removed = true;
        }

        return removed;
    }
}
