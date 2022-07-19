package dev.kir.packedinventory.api.v1.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class TooltipSyncDataProviderRegistryImpl implements TooltipSyncDataProviderRegistry {
    public static final TooltipSyncDataProviderRegistryImpl INSTANCE = new TooltipSyncDataProviderRegistryImpl();

    private final Map<Item, Entry<?>> itemBasedProviders = new HashMap<>();
    private final Set<Entry<?>> genericProvidersSet = new HashSet<>();
    private final Deque<Entry<?>> genericProviders = new ArrayDeque<>();

    @Override
    public Optional<TooltipSyncData> getTooltipSyncData(ItemStack stack, PlayerEntity player) {
        Entry<?> providerEntry = this.findProvider(stack, player);
        if (providerEntry == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providerEntry.getProvider().getTooltipSyncData(stack, player));
    }

    private @Nullable Entry<?> findProvider(ItemStack stack, PlayerEntity player) {
        Entry<?> entry = this.itemBasedProviders.get(stack.getItem());
        if (entry != null && entry.getPredicate().test(stack, player)) {
            return entry;
        }

        for (Entry<?> genericEntry : this.genericProviders) {
            if (genericEntry.getPredicate().test(stack, player)) {
                return genericEntry;
            }
        }

        return null;
    }

    @Override
    public <T extends TooltipSyncData> Entry<T> register(Entry<T> entry) {
        if (entry.getPredicate() instanceof TooltipSyncDataProvider.ItemPredicate) {
            for (Item item : ((TooltipSyncDataProvider.ItemPredicate)entry.getPredicate()).getItems()) {
                this.itemBasedProviders.put(item, entry);
            }
        } else {
            if (this.genericProvidersSet.add(entry)) {
                this.genericProviders.push(entry);
            }
        }
        return entry;
    }

    @Override
    public <T extends TooltipSyncData> Entry<T> register(TooltipSyncDataProvider<T> provider, Collection<Item> items) {
        return this.register(provider, TooltipSyncDataProvider.ItemPredicate.of(items, TooltipSyncDataProvider.Predicate.TRUE));
    }

    @Override
    public <T extends TooltipSyncData> boolean unregister(TooltipSyncDataProvider<T> provider) {
        return this.itemBasedProviders.entrySet().removeIf(x -> x.getValue().getProvider() == provider) | this.genericProvidersSet.removeIf(x -> x.getProvider() == provider) && this.genericProviders.removeIf(x -> x.getProvider() == provider);
    }
}
