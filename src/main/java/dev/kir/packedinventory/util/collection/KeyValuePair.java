package dev.kir.packedinventory.util.collection;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Objects;

public abstract class KeyValuePair<K, V> implements Map.Entry<K, V> {
    @SuppressWarnings("unchecked")
    public static <K, V> KeyValuePair<K, V> of(K key, V value) {
        if (key instanceof Identifier) {
            // Do you want a joke?
            // Java... has... generics!
            // 🤣😂😂😂😂🤣😅😆
            // Have never heard bullshit funnier than this
            return (KeyValuePair<K, V>)new IdentifierKeyValuePair<>((Identifier)key, value);
        }

        return new GenericKeyValuePair<>(key, value);
    }

    public abstract K setKey(K key);

    @Override
    public boolean equals(Object o) {
        return o instanceof Map.Entry<?, ?> entry && Objects.equals(this.getKey(), entry.getKey()) && Objects.equals(this.getValue(), entry.getValue());
    }

    @Override
    public int hashCode() {
        return (this.getKey() == null ? 0 : this.getKey().hashCode()) ^ (this.getValue() == null ? 0 : this.getValue().hashCode());
    }

    @Override
    public String toString() {
        return this.getKey() + "=" + this.getValue();
    }

    private final static class IdentifierKeyValuePair<V> extends KeyValuePair<Identifier, V> {
        private Identifier key;
        private V value;

        private IdentifierKeyValuePair(Identifier key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Identifier getKey() {
            return this.key;
        }

        @Override
        public Identifier setKey(Identifier key) {
            Identifier oldKey = this.key;
            this.key = key;
            return oldKey;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    private final static class GenericKeyValuePair<K, V> extends KeyValuePair<K, V> {
        private K key;
        private V value;

        private GenericKeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public K setKey(K key) {
            K oldKey = this.key;
            this.key = key;
            return oldKey;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
