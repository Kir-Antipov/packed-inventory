package dev.kir.packedinventory.api.v1.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * A storage class used to attach custom components to {@link ItemStack}s.
 * @param <TComponent> Component type.
 */
public interface ItemStackComponentStorage<TComponent> {
    /**
     * Returns "reference" stack for the given {@link ItemStack}.
     *
     * <p>
     *     "Reference" stack is a simplified version of the given {@link ItemStack} that has the same component domain.
     * </p>
     *
     * @param stack {@link ItemStack} instance.
     * @return "Reference" stack for the given {@link ItemStack}.
     */
    ItemStack getReferenceStack(ItemStack stack);

    /**
     * Returns time when the given {@link ItemStack} was last updated.
     * @param stack {@link ItemStack} instance.
     * @return Time when the given {@link ItemStack} was last updated, in the form number of milliseconds from the epoch of 1970-01-01T00:00:00Z.
     */
    long getLastComponentUpdateTime(ItemStack stack);

    /**
     * Returns component attached to the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
     * @param stack {@link ItemStack} instance.
     * @return Component attached to the given {@link ItemStack}, if any; otherwise, {@link Optional#empty()}.
     */
    Optional<TComponent> getComponent(ItemStack stack);

    /**
     * Attaches new component to the given {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     * @param component Component to be attached.
     */
    void attachComponent(ItemStack stack, TComponent component);

    /**
     * Detaches component from the given {@link ItemStack}.
     * @param stack {@link ItemStack} instance.
     */
    default void detachComponent(ItemStack stack) {
        this.attachComponent(stack, null);
    }

    /**
     * Returns an {@link ItemStackComponentStorage<TComponent>} instance that treats all item stacks as the same one.
     * @param <TComponent> Component type.
     * @return {@link ItemStackComponentStorage<TComponent>} instance that treats all item stacks as the same one.
     */
    static <TComponent> ItemStackComponentStorage<TComponent> singleton() {
        return ItemStackComponentStorage.singleton((ItemStack)null);
    }

    /**
     * Returns an {@link ItemStackComponentStorage<TComponent>} instance that treats all
     * item stacks of the given {@code referenceItem} as the same one.
     *
     * @param referenceItem {@link Item} used for the reference stack.
     * @param <TComponent> Component type.
     * @return {@link ItemStackComponentStorage<TComponent>} instance that treats item stacks of the given {@code referenceItem} as the same one.
     */
    static <TComponent> ItemStackComponentStorage<TComponent> singleton(Item referenceItem) {
        return ItemStackComponentStorage.singleton(new ItemStack(referenceItem));
    }

    /**
     * Returns an {@link ItemStackComponentStorage<TComponent>} instance that treats all item stacks as the same one.
     * @param stack Reference stack.
     * @param <TComponent> Component type.
     * @return {@link ItemStackComponentStorage<TComponent>} instance that treats all item stacks as the same one.
     */
    static <TComponent> ItemStackComponentStorage<TComponent> singleton(@Nullable ItemStack stack) {
        return new ItemStackComponentStorage<>() {
            private final @Nullable ItemStack referenceStack = stack;
            private @Nullable TComponent component;
            private long lastComponentUpdateTime;

            @Override
            public ItemStack getReferenceStack(ItemStack stack) {
                return this.referenceStack == null ? stack : this.referenceStack;
            }

            @Override
            public long getLastComponentUpdateTime(ItemStack stack) {
                return this.lastComponentUpdateTime;
            }

            @Override
            public Optional<TComponent> getComponent(ItemStack stack) {
                return Optional.ofNullable(this.component);
            }

            @Override
            public void attachComponent(ItemStack stack, TComponent component) {
                this.component = component;
                this.lastComponentUpdateTime = Instant.now().toEpochMilli();
            }
        };
    }

    /**
     * Returns a new {@link ItemStackComponentStorage<TComponent>} instance that uses {@link WeakHashMap} to attach components to {@link ItemStack}s.
     * @param <TComponent> Component type.
     * @return New {@link ItemStackComponentStorage<TComponent>} instance that uses {@link WeakHashMap} to attach components to {@link ItemStack}s.
     */
    static <TComponent> ItemStackComponentStorage<TComponent> weakMap() {
        return new ItemStackComponentStorage<>() {
            private final Map<ItemStack, TComponent> components = new WeakHashMap<>();
            private final Map<ItemStack, Long> updates = new WeakHashMap<>();

            @Override
            public ItemStack getReferenceStack(ItemStack stack) {
                if (this.components.containsKey(stack)) {
                    return stack;
                }

                for (Map.Entry<ItemStack, TComponent> entry : this.components.entrySet()) {
                    ItemStack key = entry.getKey();
                    if (ItemStack.areEqual(key, stack)) {
                        return key;
                    }
                }
                return stack;
            }

            @Override
            public long getLastComponentUpdateTime(ItemStack stack) {
                return this.updates.getOrDefault(this.getReferenceStack(stack), 0L);
            }

            @Override
            public Optional<TComponent> getComponent(ItemStack stack) {
                return Optional.ofNullable(this.components.get(this.getReferenceStack(stack)));
            }

            @Override
            public void attachComponent(ItemStack stack, TComponent component) {
                if (component == null) {
                    this.detachComponent(stack);
                    return;
                }

                stack = this.getReferenceStack(stack);
                this.components.put(stack, component);
                this.updates.put(stack, Instant.now().toEpochMilli());
            }

            @Override
            public void detachComponent(ItemStack stack) {
                stack = this.getReferenceStack(stack);
                this.components.remove(stack);
                this.updates.put(stack, Instant.now().toEpochMilli());
            }
        };
    }
}
