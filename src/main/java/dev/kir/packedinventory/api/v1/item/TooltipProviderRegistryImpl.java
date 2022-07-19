package dev.kir.packedinventory.api.v1.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class TooltipProviderRegistryImpl implements TooltipProviderRegistry {
    public static final TooltipProviderRegistryImpl INSTANCE = new TooltipProviderRegistryImpl();

    private final Map<Item, Entry> itemBasedProviders = new HashMap<>();
    private final Set<Entry> genericProvidersSet = new HashSet<>();
    private final Deque<Entry> genericProviders = new ArrayDeque<>();
    private @Nullable Entry defaultProvider = null;

    @Override
    public Optional<Optional<TooltipData>> getTooltipData(ItemStack stack, TooltipProviderContext context) {
        Entry providerEntry = this.findProvider(stack, context);
        if (providerEntry == null) {
            return Optional.empty();
        }

        return Optional.of(providerEntry.getProvider().getTooltipData(stack, context));
    }

    @Override
    public Optional<List<Text>> getTooltipText(ItemStack stack, TooltipProviderContext context) {
        Entry providerEntry = this.findProvider(stack, context);
        if (providerEntry == null) {
            return Optional.empty();
        }

        return Optional.of(providerEntry.getProvider().getTooltipText(stack, context));
    }

    @Override
    public boolean updateTooltipSyncData(ItemStack stack, TooltipProviderContext context, NbtCompound nbt) {
        Entry providerEntry = this.findProvider(stack, context);
        if (providerEntry == null || !(providerEntry.getProvider() instanceof SyncedTooltipProvider<?>)) {
            return false;
        }

        ((SyncedTooltipProvider<?>)providerEntry.getProvider()).readTooltipSyncDataNbt(stack, context, nbt);
        return true;
    }

    private @Nullable Entry findProvider(ItemStack stack, TooltipProviderContext context) {
        Entry entry = this.itemBasedProviders.get(stack.getItem());
        if (entry != null && entry.getPredicate().test(stack, context)) {
            return entry;
        }

        for (Entry genericEntry : this.genericProviders) {
            if (genericEntry.getPredicate().test(stack, context)) {
                return genericEntry;
            }
        }

        return this.defaultProvider;
    }

    @Override
    public Entry register(Entry entry) {
        if (entry.getPredicate() instanceof TooltipProvider.ItemPredicate) {
            for (Item item : ((TooltipProvider.ItemPredicate)entry.getPredicate()).getItems()) {
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
    public Entry register(TooltipProvider provider, Collection<Item> items) {
        return this.register(provider, TooltipProvider.ItemPredicate.of(items, TooltipProvider.Predicate.TRUE));
    }

    @Override
    public Entry registerDefault(TooltipProvider provider) {
        this.defaultProvider = new Entry() {
            @Override
            public TooltipProvider getProvider() {
                return provider;
            }

            @Override
            public TooltipProvider.Predicate getPredicate() {
                return TooltipProvider.Predicate.TRUE;
            }
        };
        return this.defaultProvider;
    }

    @Override
    public boolean unregister(TooltipProvider provider) {
        boolean removed = this.itemBasedProviders.entrySet().removeIf(x -> x.getValue().getProvider() == provider);
        removed |= this.genericProvidersSet.removeIf(x -> x.getProvider() == provider) && this.genericProviders.removeIf(x -> x.getProvider() == provider);
        if (this.defaultProvider != null && this.defaultProvider.getProvider() == provider) {
            this.defaultProvider = null;
            removed = true;
        }

        return removed;
    }
}
