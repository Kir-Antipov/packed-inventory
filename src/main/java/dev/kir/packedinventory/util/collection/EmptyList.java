package dev.kir.packedinventory.util.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class EmptyList<T> implements List<T> {
    private static final EmptyList<?> INSTANCE = new EmptyList<>();

    private static final ListIterator<?> EMPTY_ITERATOR_INSTANCE = new ListIterator<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public Object previous() {
            return null;
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public int previousIndex() {
            return -1;
        }

        @Override
        public void remove() {
        }

        @Override
        public void set(Object o) {
        }

        @Override
        public void add(Object t) {
        }
    };

    private EmptyList() { }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getInstance() {
        return (List<T>)INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.listIterator();
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public @NotNull Object[] toArray() {
        return new Object[0];
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public <U> @NotNull U[] toArray(@NotNull U[] a) {
        return a;
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() { }

    @Override
    public T get(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public T set(int index, T element) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void add(int index, T element) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public T remove(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int indexOf(Object o) {
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<T> listIterator() {
        return (ListIterator<T>)EMPTY_ITERATOR_INSTANCE;
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        if (index == 0) {
            return this.listIterator();
        }
        throw new IndexOutOfBoundsException();
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex != 0 || toIndex != 0) {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }
}
