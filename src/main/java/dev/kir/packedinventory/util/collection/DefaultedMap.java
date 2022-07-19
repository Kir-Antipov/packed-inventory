package dev.kir.packedinventory.util.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface DefaultedMap<K, V> extends Map<K, V> {
    default K getDefaultKey() {
        return this.getDefaultEntry().getKey();
    }

    default V getDefaultValue() {
        return this.getDefaultEntry().getValue();
    }

    default V getOrDefault(Object key) {
        return this.getOrDefault(key, this.getDefaultValue());
    }

    default Entry<K, V> getDefaultEntry() {
        return this.getDefaultEntrySupplier().get();
    }

    Supplier<Entry<K, V>> getDefaultEntrySupplier();

    static <K, V> DefaultedMap<K, V> wrap(Map<K, V> map, Supplier<K> defaultKey, Supplier<V> defaultValue) {
        return DefaultedMap.wrap(map, () -> KeyValuePair.of(defaultKey.get(), defaultValue.get()));
    }

    static <K, V> DefaultedMap<K, V> wrap(Map<K, V> map, Supplier<Entry<K, V>> defaultEntry) {
        return new DefaultedMap<>() {
            @Override
            public Supplier<Entry<K, V>> getDefaultEntrySupplier() {
                return defaultEntry;
            }

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return map.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return map.containsValue(value);
            }

            @Override
            public V get(Object key) {
                return map.get(key);
            }

            @Override
            public V getOrDefault(Object key, V defaultValue) {
                return map.getOrDefault(key, defaultValue);
            }

            @Nullable
            @Override
            public V put(K key, V value) {
                return map.put(key, value);
            }

            @Override
            public V remove(Object key) {
                return map.remove(key);
            }

            @Override
            public boolean remove(Object key, Object value) {
                return map.remove(key, value);
            }

            @Override
            public void forEach(BiConsumer<? super K, ? super V> action) {
                map.forEach(action);
            }

            @Override
            public boolean replace(K key, V oldValue, V newValue) {
                return map.replace(key, oldValue, newValue);
            }

            @Override
            public V replace(K key, V value) {
                return map.replace(key, value);
            }

            @Override
            public V putIfAbsent(K key, V value) {
                return map.putIfAbsent(key, value);
            }

            @Override
            public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
                return map.compute(key, remappingFunction);
            }

            @Override
            public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
                return map.computeIfAbsent(key, mappingFunction);
            }

            @Override
            public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
                return map.computeIfPresent(key, remappingFunction);
            }

            @Override
            public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
                map.replaceAll(function);
            }

            @Override
            public void putAll(@NotNull Map<? extends K, ? extends V> m) {
                map.putAll(m);
            }

            @Override
            public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
                return map.merge(key, value, remappingFunction);
            }

            @Override
            public void clear() {
                map.clear();
            }

            @NotNull
            @Override
            public Set<K> keySet() {
                return map.keySet();
            }

            @NotNull
            @Override
            public Collection<V> values() {
                return map.values();
            }

            @NotNull
            @Override
            public Set<Entry<K, V>> entrySet() {
                return map.entrySet();
            }

            @Override
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            public boolean equals(Object obj) {
                return map.equals(obj);
            }

            @Override
            public int hashCode() {
                return map.hashCode();
            }

            @Override
            public String toString() {
                return map.toString();
            }
        };
    }
}
