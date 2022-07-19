package dev.kir.packedinventory.api.v1.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * {@link TooltipProvider} registry.
 */
public interface TooltipProviderRegistry {
    /**
     * @return {@link TooltipProviderRegistry} instance.
     */
    @ApiStatus.Internal
    static TooltipProviderRegistry getInstance() {
        return TooltipProviderRegistryImpl.INSTANCE;
    }


    /**
     * Returns {@link TooltipData} that contains information about this {@link ItemStack} required by its {@link net.minecraft.client.gui.tooltip.TooltipComponent}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @return {@link TooltipData} that contains information about this {@link ItemStack} required by its {@link net.minecraft.client.gui.tooltip.TooltipComponent}, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<Optional<TooltipData>> getTooltipData(ItemStack stack, TooltipProviderContext context);

    /**
     * Returns tooltip text that describes this {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @return Tooltip text that describes this {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<List<Text>> getTooltipText(ItemStack stack, TooltipProviderContext context);

    /**
     * Updates synchronization data for the given {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @param context Context.
     * @param nbt Synchronization data Nbt.
     * @return {@code true} if the synchronization data was updated; otherwise, {@code false}.
     */
    boolean updateTooltipSyncData(ItemStack stack, TooltipProviderContext context, NbtCompound nbt);


    /**
     * Registers the given {@code entry} in the registry.
     * @param entry Entry to be registered.
     * @return Registered entry.
     */
    Entry register(Entry entry);

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param predicate Predicate that should pass in order for the {@code provider} to be used.
     * @return Registered entry.
     */
    default Entry register(TooltipProvider provider, TooltipProvider.Predicate predicate) {
        return this.register(new Entry() {
            @Override
            public TooltipProvider getProvider() {
                return provider;
            }

            @Override
            public TooltipProvider.Predicate getPredicate() {
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
    default Entry register(TooltipProvider provider, Item item) {
        return this.register(provider, List.of(item));
    }

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param items Items this {@code provider} is associated with.
     * @return Registered entry.
     */
    default Entry register(TooltipProvider provider, Item... items) {
        return this.register(provider, Arrays.asList(items));
    }

    /**
     * Registers the specified {@code provider}.
     * @param provider Provider to be registered.
     * @param items Items this {@code provider} is associated with.
     * @return Registered entry.
     */
    default Entry register(TooltipProvider provider, Collection<Item> items) {
        return this.register(provider, TooltipProvider.ItemPredicate.of(items));
    }

    /**
     * Registers the specified {@code provider} as a default provider.
     * @param provider Provider to be registered.
     * @return Registered entry.
     */
    default Entry registerDefault(TooltipProvider provider) {
        return this.register(provider, TooltipProvider.Predicate.TRUE);
    }


    /**
     * Unregisters the given {@code provider}.
     * @param provider Provider to be unregistered.
     * @return {@code true} if the {@code provider} was unregistered; otherwise, {@code false}.
     */
    boolean unregister(TooltipProvider provider);

    /**
     * Unregisters the given {@code entry}.
     * @param entry Entry to be unregistered.
     * @return {@code true} if the {@code entry} was unregistered; otherwise, {@code false}.
     */
    default boolean unregister(Entry entry) {
        return this.unregister(entry.getProvider());
    }


    /**
     * {@link TooltipProviderRegistry} entry.
     */
    interface Entry {
        /**
         * @return {@link TooltipProvider} instance.
         */
        TooltipProvider getProvider();

        /**
         * @return Predicate that should pass in order for the {@code provider} to be used.
         */
        TooltipProvider.Predicate getPredicate();
    }
}
