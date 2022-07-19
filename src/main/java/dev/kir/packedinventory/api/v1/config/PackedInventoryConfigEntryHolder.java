package dev.kir.packedinventory.api.v1.config;

import com.google.gson.Gson;

import java.util.function.Supplier;

/**
 * Represents a storage for config entries.
 *
 * @param <T> Type of the stored value.
 */
public interface PackedInventoryConfigEntryHolder<T> {
    /**
     * @return Currently stored value.
     */
    T getConfigEntry();

    /**
     * Swaps currently stored value with the given one.
     * @param entry Value to be stored in this storage.
     */
    void setConfigEntry(T entry);

    /**
     * @return JSON string that represents currently stored value.
     */
    String toJson();

    /**
     * Swaps currently stored value with the one parsed from the given JSON string.
     * @param json JSON string that represents value to be stored in this storage.
     */
    void fromJson(String json);

    /**
     * Creates new {@link PackedInventoryConfigEntryHolder<T>} instance backed by {@link Gson}.
     *
     * @param defaultValueFactory A {@link Supplier<T>} that creates new {@param <T>} instances when requested.
     * @param <T> Type of the stored value.
     * @return New {@link PackedInventoryConfigEntryHolder<T>} instance.
     */
    static <T> PackedInventoryConfigEntryHolder<T> gson(Supplier<T> defaultValueFactory) {
        return new PackedInventoryConfigEntryHolder<>() {
            private static final Gson gson = new Gson();
            private T value = defaultValueFactory.get();
            @SuppressWarnings("unchecked")
            private final Class<T> valueClass = (Class<T>)this.value.getClass();

            @Override
            public T getConfigEntry() {
                return this.value;
            }

            @Override
            public void setConfigEntry(T entry) {
                if (entry != null && !this.valueClass.isInstance(entry)) {
                    entry = defaultValueFactory.get();
                }
                this.value = entry;
            }

            @Override
            public String toJson() {
                try {
                    return gson.toJson(this.value);
                } catch (Throwable e) {
                    return "";
                }
            }

            @Override
            public void fromJson(String json) {
                try {
                    this.value = gson.fromJson(json, this.valueClass);
                } catch (Throwable e) {
                    this.value = defaultValueFactory.get();
                }
            }
        };
    }
}
