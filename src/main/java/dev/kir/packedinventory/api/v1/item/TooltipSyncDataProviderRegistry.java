package dev.kir.packedinventory.api.v1.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * {@link TooltipSyncDataProvider} registry.
 */
public interface TooltipSyncDataProviderRegistry {
    /**
     * @return {@link TooltipSyncDataProviderRegistry} instance.
     */
    @ApiStatus.Internal
    static TooltipSyncDataProviderRegistry getInstance() {
        return TooltipSyncDataProviderRegistryImpl.INSTANCE;
    }


    /**
     * Supplies requester with synchronization data for the given {@link ItemStack}.
     * @param stack {@link ItemStack} that requires synchronization data.
     * @param player Player.
     * @return Synchronization data for the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<TooltipSyncData> getTooltipSyncData(ItemStack stack, PlayerEntity player);

    /**
     * Registers the given {@code entry} in the registry.
     * @param entry Entry to be registered.
     * @return Registered entry.
     */
    <T extends TooltipSyncData> Entry<T> register(Entry<T> entry);

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param predicate Predicate that should pass in order for the {@code provider} to be used.
     * @return Registered entry.
     */
    default <T extends TooltipSyncData> Entry<T> register(TooltipSyncDataProvider<T> provider, TooltipSyncDataProvider.Predicate predicate) {
        return this.register(new Entry<>() {
            @Override
            public TooltipSyncDataProvider<T> getProvider() {
                return provider;
            }

            @Override
            public TooltipSyncDataProvider.Predicate getPredicate() {
                return predicate;
            }
        });
    }

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param item Item this {@code provider} is associated with.
     * @return Registered entry.
     */
    default <T extends TooltipSyncData> Entry<T> register(TooltipSyncDataProvider<T> provider, Item item) {
        return this.register(provider, List.of(item));
    }

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param items Items this {@code provider} is associated with.
     * @return Registered entry.
     */
    default <T extends TooltipSyncData> Entry<T> register(TooltipSyncDataProvider<T> provider, Item... items) {
        return this.register(provider, Arrays.asList(items));
    }

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param items Items this {@code provider} is associated with.
     * @return Registered entry.
     */
    default <T extends TooltipSyncData> Entry<T> register(TooltipSyncDataProvider<T> provider, Collection<Item> items) {
        return this.register(provider, TooltipSyncDataProvider.ItemPredicate.of(items));
    }


    /**
     * Unregisters the given {@code provider}.
     * @param provider Provider to be unregistered.
     * @return {@code true} if the {@code provider} was unregistered; otherwise, {@code false}.
     */
    <T extends TooltipSyncData> boolean unregister(TooltipSyncDataProvider<T> provider);

    /**
     * Unregisters the given {@code entry}.
     * @param entry Entry to be unregistered.
     * @return {@code true} if the {@code entry} was unregistered; otherwise, {@code false}.
     */
    default <T extends TooltipSyncData> boolean unregister(Entry<T> entry) {
        return this.unregister(entry.getProvider());
    }


    /**
     * {@link TooltipSyncDataProviderRegistry} entry.
     */
    interface Entry<T extends TooltipSyncData> {
        /**
         * @return {@link TooltipSyncDataProvider<T>} instance.
         */
        TooltipSyncDataProvider<T> getProvider();

        /**
         * @return Predicate that should pass in order for the {@code provider} to be used.
         */
        TooltipSyncDataProvider.Predicate getPredicate();
    }
}
