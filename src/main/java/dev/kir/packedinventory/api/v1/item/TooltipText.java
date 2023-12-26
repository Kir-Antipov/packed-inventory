package dev.kir.packedinventory.api.v1.item;

import dev.kir.packedinventory.util.collection.EmptyList;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Wraps {@link List<Text>} to make structural changes easier for user.
 */
public final class TooltipText {
    /**
     * Default order of tooltip parts.
     */
    public static final List<Part> DEFAULT_ORDER = List.of(Part.values());

    private final List<Text> list;
    private final List<Segment> segments;
    private final Map<Segment, SegmentedTextList> subLists;

    private TooltipText(List<Text> list, List<Segment> segments) {
        this.list = list;
        this.segments = segments;
        this.subLists = new HashMap<>();
    }

    /**
     * @return Empty {@link TooltipText}.
     */
    public static TooltipText empty() {
        Builder builder = new Builder(new ArrayList<>());
        for (Part part : DEFAULT_ORDER) {
            builder.beginSection(part);
            builder.endSection();
        }
        return builder.build();
    }

    /**
     * Returns new tooltip text builder.
     * @param list Underlying text list.
     * @return New tooltip text builder.
     */
    @ApiStatus.Internal
    public static Builder builder(List<Text> list) {
        return new Builder(list);
    }

    /**
     * Returns {@code true} if tooltip text has the given {@link Part}; otherwise, {@code false}.
     * @param part Text part to be searched for.
     * @return {@code true} if tooltip text has the given {@link Part}; otherwise, {@code false}.
     */
    public boolean containsPart(Part part) {
        return this.findSegmentIndex(part) != -1;
    }

    /**
     * Appends new text part to the tooltip text.
     * @param part Text part to be appended.
     */
    public void appendPart(Part part) {
        this.addPart(part, this.segments.size());
    }

    /**
     * Prepends new text part to the tooltip text.
     * @param part Text part to be prepended.
     */
    public void prependPart(Part part) {
        this.addPart(part, 0);
    }

    /**
     * Inserts new text part after {@code anchorPart}.
     * @param part Text part to be inserted.
     * @param anchorPart Text part after which the {@code part} should be inserted.
     * @return {@code true} if the given part was inserted; otherwise, {@code false}.
     */
    public boolean insertPartAfter(Part part, Part anchorPart) {
        return this.addPartByAnchor(part, anchorPart, 1);
    }

    /**
     * Inserts new text part before {@code anchorPart}.
     * @param part Text part to be inserted.
     * @param anchorPart Text part before which the {@code part} should be inserted.
     * @return {@code true} if the given part was inserted; otherwise, {@code false}.
     */
    public boolean insertPartBefore(Part part, Part anchorPart) {
        return this.addPartByAnchor(part, anchorPart, 0);
    }

    private boolean addPartByAnchor(Part part, Part anchorPart, int shift) {
        int i = this.findSegmentIndex(anchorPart);
        if (i == -1) {
            return false;
        }

        this.addPart(part, i + shift);
        return true;
    }

    private void addPart(Part part, int i) {
        int listIndex = i == 0 ? 0 : this.segments.get(i - 1).end;
        this.segments.add(i, new Segment(part, listIndex, listIndex));
    }

    /**
     * Returns a view of the portion of this tooltip text for the given
     * {@code part}. If {@code part} does not exist in this tooltip text,
     * the returned list is empty and unmodifiable. The returned list is backed
     * by this instance so all changes in the returned list are reflected in
     * this tooltip text, and vice-versa.
     *
     * The list returned by this method supports all methods and modifications,
     * even the <i>structural ones</i>.
     *
     * @param part Text part that should be contained in the returned list.
     * @return View of the portion of this tooltip text for the given {@code part}.
     */
    public List<Text> subList(Part part) {
        int i = this.findSegmentIndex(part);
        if (i == -1) {
            return EmptyList.getInstance();
        }
        return this.subLists.computeIfAbsent(this.segments.get(i), segment -> new SegmentedTextList(this, segment, i));
    }

    /**
     * Appends the specified element to the end of the given {@code part}.
     * @param part Text part the element should be appended to.
     * @param text Text that should be appended.
     * @return {@code true} if the element was appended; otherwise, {@code false}.
     */
    public boolean append(Part part, Text text) {
        return this.subList(part).add(text);
    }

    /**
     * Appends all specified elements to the end of the given {@code part}.
     * @param part Text part the elements should be appended to.
     * @param text Text that should be appended.
     * @return {@code true} if the element was appended; otherwise, {@code false}.
     */
    public boolean appendAll(Part part, Text... text) {
        return this.appendAll(part, Arrays.asList(text));
    }

    /**
     * Appends all specified elements to the end of the given {@code part}.
     * @param part Text part the elements should be appended to.
     * @param text Text that should be appended.
     * @return {@code true} if the elements was appended; otherwise, {@code false}.
     */
    public boolean appendAll(Part part, Collection<Text> text) {
        return this.subList(part).addAll(text);
    }

    /**
     * Prepends the specified element to the start of the given {@code part}.
     * @param part Text part the element should be prepended to.
     * @param text Text that should be prepended.
     * @return {@code true} if the element was prepended; otherwise, {@code false}.
     */
    public boolean prepend(Part part, Text text) {
        List<Text> sublist = this.subList(part);
        if (sublist == EmptyList.<Text>getInstance()) {
            return false;
        }

        sublist.add(0, text);
        return true;
    }

    /**
     * Prepends all specified elements to the start of the given {@code part}.
     * @param part Text part the elements should be prepended to.
     * @param text Text that should be prepended.
     * @return {@code true} if the elements was prepended; otherwise, {@code false}.
     */
    public boolean prependAll(Part part, Text... text) {
        return this.prependAll(part, Arrays.asList(text));
    }

    /**
     * Prepends all specified elements to the start of the given {@code part}.
     * @param part Text part the elements should be prepended to.
     * @param text Text that should be prepended.
     * @return {@code true} if the elements was prepended; otherwise, {@code false}.
     */
    public boolean prependAll(Part part, Collection<Text> text) {
        List<Text> sublist = this.subList(part);
        if (sublist == EmptyList.<Text>getInstance()) {
            return false;
        }

        sublist.addAll(0, text);
        return true;
    }

    /**
     * Replaces content of the given {@code part} with the specified {@code text}.
     * @param part Text part that should be replaced with the new content.
     * @param text New content of the given {@code part}.
     * @return {@code true} if content of the given {@code part} was replaced with the specified {@code text}; otherwise, {@code false}.
     */
    public boolean set(Part part, Text text) {
        this.clear(part);
        return this.append(part, text);
    }

    /**
     * Replaces content of the given {@code part} with the specified {@code text}.
     * @param part Text part that should be replaced with the new content.
     * @param text New content of the given {@code part}.
     * @return {@code true} if content of the given {@code part} was replaced with the specified {@code text}; otherwise, {@code false}.
     */
    public boolean setAll(Part part, Text... text) {
        return this.setAll(part, Arrays.asList(text));
    }

    /**
     * Replaces content of the given {@code part} with the specified {@code text}.
     * @param part Text part that should be replaced with the new content.
     * @param text New content of the given {@code part}.
     * @return {@code true} if content of the given {@code part} was replaced with the specified {@code text}; otherwise, {@code false}.
     */
    public boolean setAll(Part part, Collection<Text> text) {
        this.clear(part);
        return this.appendAll(part, text);
    }

    /**
     * Removes specified {@code text} from the given {@code part}.
     * @param part Text part the {@code text} should be removed from.
     * @param text Text that should be removed.
     * @return {@code true} if the specified {@code text} was removed from the given {@code part}; otherwise, {@code false}.
     */
    public boolean remove(Part part, Text text) {
        return this.subList(part).remove(text);
    }

    /**
     * Removes specified {@code text} from the given {@code part}.
     * @param part Text part the {@code text} should be removed from.
     * @param text Text that should be removed.
     * @return {@code true} if the specified {@code text} was removed from the given {@code part}; otherwise, {@code false}.
     */
    public boolean removeAll(Part part, Text... text) {
        return this.removeAll(part, Arrays.asList(text));
    }

    /**
     * Removes specified {@code text} from the given {@code part}.
     * @param part Text part the {@code text} should be removed from.
     * @param text Text that should be removed.
     * @return {@code true} if the specified {@code text} was removed from the given {@code part}; otherwise, {@code false}.
     */
    public boolean removeAll(Part part, Collection<Text> text) {
        return this.subList(part).removeAll(text);
    }

    /**
     * Returns {@code true} if the given {@code part} contains specified {@code text}; otherwise, {@code false}.
     * @param part Text part the {@code text} should be searched in.
     * @param text Text that should be searched for.
     * @return {@code true} if the given {@code part} contains specified {@code text}; otherwise, {@code false}.
     */
    public boolean contains(Part part, Text text) {
        return this.subList(part).contains(text);
    }

    /**
     * Clears content of the specified {@code part}.
     * @param part Text part that should be cleared.
     */
    public void clear(Part part) {
        this.subList(part).clear();
    }

    private int findSegmentIndex(Part part) {
        for (int i = 0; i < this.segments.size(); ++i) {
            if (this.segments.get(i).part == part) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return List representation of this tooltip text that can be consumed by the vanilla pipeline.
     */
    public List<Text> toList() {
        return new ArrayList<>(this.list);
    }

    /**
     * Coverts content of this tooltip text to the list that can be consumed by the vanilla pipeline.
     * @param order Order in which tooltip parts should be added to the list.
     * @return List representation of this tooltip text that can be consumed by the vanilla pipeline.
     */
    public List<Text> toList(Part... order) {
        return this.toList(Arrays.asList(order));
    }

    /**
     * Coverts content of this tooltip text to the list that can be consumed by the vanilla pipeline.
     * @param order Order in which tooltip parts should be added to the list.
     * @return List representation of this tooltip text that can be consumed by the vanilla pipeline.
     */
    public List<Text> toList(Collection<Part> order) {
        List<Text> orderedList = new ArrayList<>(order.size());
        for (Part part : order) {
            orderedList.addAll(this.subList(part));
        }
        return orderedList;
    }


    /**
     * Tooltip text builder.
     */
    @ApiStatus.Internal
    public final static class Builder {
        private final List<Text> list;
        private final List<Segment> segments;
        private final BuilderList wrapper;
        private int currentIndex;
        private boolean isInvalid;
        private @Nullable Part currentPart;
        private @Nullable TooltipText builtTooltipText;

        private Builder(List<Text> list) {
            this.list = list;
            this.segments = new ArrayList<>();
            this.currentIndex = list.size();
            this.wrapper = new BuilderList(this);
        }

        /**
         * Returns {@code true} if the given {@code part} exists; otherwise, {@code false}.
         * @param part Text part to be searched for.
         * @return {@code true} if the given {@code part} exists; otherwise, {@code false}.
         */
        public boolean containsSection(Part part) {
            if (this.currentPart == part) {
                return true;
            }

            for (Segment segment : this.segments) {
                if (segment.part == part) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Begins new {@link Part} section.
         * @param part Text part.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder beginSection(Part part) {
            if (this.currentPart == part || this.isInvalid) {
                return this;
            }

            if (this.currentPart != null) {
                this.endSection();
            }

            this.currentPart = part;
            this.currentIndex = this.list.size();
            return this;
        }

        /**
         * Ends the section that is being currently built, if any.
         * @return {@code this} instance for chaining purposes.
         */
        public Builder endSection() {
            if (this.isInvalid || this.currentPart == null) {
                return this;
            }

            if (this.list.size() < this.currentIndex) {
                this.isInvalid = true;
                this.segments.clear();
                return this;
            }

            this.segments.add(new Segment(this.currentPart, this.currentIndex, this.list.size()));
            this.currentPart = null;
            return this;
        }

        /**
         * Finalizes the build process.
         * @return Built {@link TooltipText} instance.
         */
        public TooltipText build() {
            if (this.builtTooltipText != null) {
                return this.builtTooltipText;
            }

            this.endSection();
            this.isInvalid = true;
            this.builtTooltipText = new TooltipText(this.list, this.segments);
            return this.builtTooltipText;
        }

        /**
         * @deprecated Use {@link Builder#build()} and {@link TooltipText#toList()} instead.
         */
        @ApiStatus.Internal
        @Deprecated
        public BuilderList asList() {
            return this.wrapper;
        }
    }

    /**
     * Known tooltip text parts.
     */
    public enum Part {
        /**
         * Item name.
         */
        NAME,
        /**
         * Map Id.
         */
        MAP_ID,
        /**
         * Additional info.
         * It's usually provided by methods like:
         *
         * <ul>
         *     <li>{@link net.minecraft.item.Item#appendTooltip(ItemStack, World, List, TooltipContext)}</li>
         *     <li>{@link net.minecraft.block.Block#appendTooltip(ItemStack, BlockView, List, TooltipContext)}</li>
         * </ul>
         * 
         * Common examples:
         * 
         * <ul>
         *     <li>{@link net.minecraft.block.ShulkerBoxBlock#appendTooltip(ItemStack, BlockView, List, TooltipContext)}</li>
         * </ul>
         */
        ADDITIONAL(ItemStack.TooltipSection.ADDITIONAL),
        /**
         * Enchantment list.
         */
        UPGRADES(ItemStack.TooltipSection.UPGRADES),
        /**
         * Enchantment list.
         */
        ENCHANTMENTS(ItemStack.TooltipSection.ENCHANTMENTS),
        /**
         * Dye color.
         */
        DYE(ItemStack.TooltipSection.DYE),
        /**
         * THE LOOOOOOOORE!
         *
         * <p>
         *     In all seriousness, most of the time it's that ugly purple "(+NBT)" label,
         *     that you see on blocks picked up in creative via CTRL + MMB.
         * </p>
         */
        LORE,
        /**
         * Item modifiers.
         *
         * <p>E.g., attack speed, attack damage, etc.</p>
         */
        MODIFIERS(ItemStack.TooltipSection.MODIFIERS),
        /**
         * Shows if an item is unbreakable.
         *
         * <p>Mostly used by adventure maps.</p>
         */
        UNBREAKABLE(ItemStack.TooltipSection.UNBREAKABLE),
        /**
         * Shows if an item can destroy only a specific set of blocks.
         *
         * <p>Mostly used by adventure maps.</p>
         */
        CAN_DESTROY(ItemStack.TooltipSection.CAN_DESTROY),
        /**
         * Shows if an item can be placed only on a specific set of blocks.
         *
         * <p>Mostly used by adventure maps.</p>
         */
        CAN_PLACE(ItemStack.TooltipSection.CAN_PLACE),
        /**
         * Item durability.
         */
        DURABILITY,
        /**
         * Item id.
         */
        ITEM_ID,
        /**
         * Nbt tags.
         */
        NBT_TAGS,
        /**
         * This part should always be empty.
         */
        UNKNOWN;

        private final @Nullable ItemStack.TooltipSection section;

        Part() {
            this(null);
        }

        Part(@Nullable ItemStack.TooltipSection section) {
            this.section = section;
        }

        /**
         * @return Vanilla {@link net.minecraft.item.ItemStack.TooltipSection} associated with this text part, if any; otherwise, {@code null}.
         */
        public @Nullable ItemStack.TooltipSection getSection() {
            return section;
        }
    }

    private final static class Segment {
        public final Part part;
        public int start;
        public int end;

        public Segment(Part part, int start, int end) {
            this.part = part;
            this.start = start;
            this.end = end;
        }
    }

    private static class SegmentedTextList implements List<Text> {
        protected final TooltipText text;
        protected final Segment segment;
        protected final int segmentIndex;

        public SegmentedTextList(TooltipText text, Segment segment, int segmentIndex) {
            this.text = text;
            this.segment = segment;
            this.segmentIndex = segmentIndex;
            this.updateSegment();
        }

        protected void updateSegment() {
            this.segment.start = Math.min(this.segment.start, this.text.list.size());
            this.segment.end = Math.min(this.segment.end, this.text.list.size());
        }

        protected void updateSegment(int lengthDelta) {
            this.segment.end = MathHelper.clamp(this.segment.end + lengthDelta, this.segment.start, this.text.list.size());
        }

        private boolean updateSegments(int lengthDelta) {
            this.updateSegment(lengthDelta);
            for (int i = this.segmentIndex + 1; i < this.text.segments.size(); ++i) {
                Segment segment = this.text.segments.get(i);
                segment.start = MathHelper.clamp(segment.start + lengthDelta, 0, this.text.list.size());
                segment.end = MathHelper.clamp(segment.end + lengthDelta, segment.start, this.text.list.size());
            }
            return lengthDelta != 0;
        }

        private int toExistingListIndex(int index) {
            if (index < 0 || index >= this.size()) {
                throw new IndexOutOfBoundsException();
            }
            return this.segment.start + index;
        }

        private int toListIndex(int index) {
            if (index < 0 || index > this.size()) {
                throw new IndexOutOfBoundsException();
            }
            return this.segment.start + index;
        }

        @Override
        public int size() {
            this.updateSegment();
            return this.segment.end - this.segment.start;
        }

        @Override
        public boolean isEmpty() {
            this.updateSegment();
            return this.segment.end == this.segment.start;
        }

        @Override
        public boolean contains(Object o) {
            this.updateSegment();
            for (int i = this.segment.start; i < this.segment.end; ++i) {
                if (Objects.equals(this.text.list.get(i), o)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public @NotNull Iterator<Text> iterator() {
            return this.listIterator();
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public @NotNull Object[] toArray() {
            int size = this.size();
            Object[] array = new Object[size];
            for (int i = this.segment.start, j = 0; i < this.segment.end; ++i, ++j) {
                array[j] = this.text.list.get(i);
            }
            return array;
        }

        @Override
        @SuppressWarnings({"NullableProblems", "unchecked", "ConstantConditions"})
        public <T> @NotNull T[] toArray(@NotNull T[] a) {
            Class<?> componentType = a.getClass().componentType();
            if (!componentType.isAssignableFrom(Text.class)) {
                throw new ArrayStoreException();
            }

            int size = this.size();
            if (a.length < size) {
                a = (T[])Array.newInstance(componentType, size);
            }
            for (int i = this.segment.start, j = 0; i < this.segment.end; ++i, ++j) {
                a[j] = (T)this.text.list.get(i);
            }
            if (a.length > size) {
                a[size] = null;
            }
            return a;
        }

        @Override
        public boolean add(Text text) {
            this.text.list.add(this.segment.end, text);
            this.updateSegments(1);
            return true;
        }

        @Override
        public boolean remove(Object o) {
            this.updateSegment();
            for (int i = this.segment.start; i < this.segment.end; ++i) {
                if (Objects.equals(o, this.text.list.get(i))) {
                    this.text.list.remove(i);
                    this.updateSegments(-1);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for (Object x : c) {
                if (!this.contains(x)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Text> c) {
            return this.addAll(this.size(), c);
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends Text> c) {
            int startIndex = this.toListIndex(index);
            int currentIndex = startIndex;
            for (Text text : c) {
                this.text.list.add(currentIndex++, text);
            }
            return this.updateSegments(currentIndex - startIndex);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            boolean removed = false;
            for (Object x : c) {
                removed |= this.remove(x);
            }
            return removed;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            List<Object> remove = new ArrayList<>(c.size());
            for (Object x : c) {
                if (!this.contains(x)) {
                    remove.add(x);
                }
            }
            return this.removeAll(remove);
        }

        @Override
        public void clear() {
            int size = this.size();
            if (size == 0) {
                return;
            }

            this.text.list.subList(this.segment.start, this.segment.end).clear();
            this.updateSegments(-size);
        }

        @Override
        public Text get(int index) {
            return this.text.list.get(this.toExistingListIndex(index));
        }

        @Override
        public Text set(int index, Text element) {
            return this.text.list.set(this.toExistingListIndex(index), element);
        }

        @Override
        public void add(int index, Text element) {
            this.text.list.add(this.toListIndex(index), element);
            this.updateSegments(1);
        }

        @Override
        public Text remove(int index) {
            Text removed = this.text.list.remove(this.toExistingListIndex(index));
            this.updateSegments(-1);
            return removed;
        }

        @Override
        public int indexOf(Object o) {
            this.updateSegment();
            for (int i = this.segment.start, j = 0; i < this.segment.end; ++i, ++j) {
                if (Objects.equals(o, this.text.list.get(i))) {
                    return j;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            this.updateSegment();
            for (int i = this.segment.end - 1, j = this.size() - 1; i >= this.segment.start; --i, --j) {
                if (Objects.equals(o, this.text.list.get(i))) {
                    return j;
                }
            }
            return -1;
        }

        @Override
        public @NotNull ListIterator<Text> listIterator() {
            return this.listIterator(0);
        }

        @Override
        public @NotNull ListIterator<Text> listIterator(int index) {
            if (index == 0 && this.isEmpty()) {
                return EmptyList.<Text>getInstance().listIterator();
            }

            int listStart = this.toExistingListIndex(index);
            int listEnd = this.segment.end;
            SegmentedTextList it = this;
            return new ListIterator<>() {
                private final SegmentedTextList textList = it;
                private int currentIndex = listStart - 1;
                private final int start = listStart;
                private int end = listEnd;

                @Override
                public boolean hasNext() {
                    return (this.currentIndex + 1) < this.end;
                }

                @Override
                public Text next() {
                    return this.textList.text.list.get(++this.currentIndex);
                }

                @Override
                public boolean hasPrevious() {
                    return this.currentIndex > this.start;
                }

                @Override
                public Text previous() {
                    return this.textList.text.list.get(--this.currentIndex);
                }

                @Override
                public int nextIndex() {
                    return Math.min(this.currentIndex + 1, this.end);
                }

                @Override
                public int previousIndex() {
                    if (this.currentIndex <= this.start) {
                        return -1;
                    }

                    return this.currentIndex - 1;
                }

                @Override
                public void remove() {
                    this.textList.text.list.remove(this.currentIndex--);
                    this.end--;
                    this.textList.updateSegments(-1);
                }

                @Override
                public void set(Text text) {
                    this.textList.text.list.set(this.currentIndex, text);
                }

                @Override
                public void add(Text text) {
                    this.textList.text.list.add(this.currentIndex++, text);
                    this.end++;
                    this.textList.updateSegments(1);
                }
            };
        }

        @Override
        public @NotNull List<Text> subList(int fromIndex, int toIndex) {
            SegmentedTextList parentList = this;
            int newStart = this.toExistingListIndex(fromIndex);
            int newEnd = this.toListIndex(toIndex);
            return new SegmentedTextList(this.text, new Segment(this.segment.part, newStart, newEnd), this.segmentIndex) {
                @Override
                protected void updateSegment() {
                    super.updateSegment();
                    parentList.updateSegment();
                }

                @Override
                protected void updateSegment(int lengthDelta) {
                    super.updateSegment(lengthDelta);
                    parentList.updateSegment(lengthDelta);
                }
            };
        }
    }

    @ApiStatus.Internal
    public static final class BuilderList extends ArrayList<Text> {
        private final Builder builder;

        private BuilderList(Builder builder) {
            this.builder = builder;
        }

        public Builder asBuilder() {
            return this.builder;
        }

        @Override
        public int size() {
            return this.builder.list.size();
        }

        @Override
        public boolean isEmpty() {
            return this.builder.list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return this.builder.list.contains(o);
        }

        @NotNull
        @Override
        public Iterator<Text> iterator() {
            return this.builder.list.iterator();
        }

        @Override
        public void forEach(Consumer<? super Text> action) {
            this.builder.list.forEach(action);
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public @NotNull Object[] toArray() {
            return this.builder.list.toArray();
        }

        @Override
        @SuppressWarnings({"NullableProblems", "SuspiciousToArrayCall"})
        public <T> @NotNull T[] toArray(@NotNull T[] a) {
            return this.builder.list.toArray(a);
        }

        @Override
        @SuppressWarnings("SuspiciousToArrayCall")
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return this.builder.list.toArray(generator);
        }

        @Override
        public boolean add(Text text) {
            return this.builder.list.add(text);
        }

        @Override
        public boolean remove(Object o) {
            return this.builder.list.remove(o);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return this.builder.list.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Text> c) {
            return this.builder.list.addAll(c);
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends Text> c) {
            return this.builder.list.addAll(index, c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return this.builder.list.removeAll(c);
        }

        @Override
        public boolean removeIf(Predicate<? super Text> filter) {
            return this.builder.list.removeIf(filter);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return this.builder.list.retainAll(c);
        }

        @Override
        public void replaceAll(UnaryOperator<Text> operator) {
            this.builder.list.replaceAll(operator);
        }

        @Override
        public void sort(Comparator<? super Text> c) {
            this.builder.list.sort(c);
        }

        @Override
        public void clear() {
            this.builder.list.clear();
        }

        @Override
        public Text get(int index) {
            return this.builder.list.get(index);
        }

        @Override
        public Text set(int index, Text element) {
            return this.builder.list.set(index, element);
        }

        @Override
        public void add(int index, Text element) {
            this.builder.list.add(index, element);
        }

        @Override
        public Text remove(int index) {
            return this.builder.list.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return this.builder.list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return this.builder.list.lastIndexOf(o);
        }

        @NotNull
        @Override
        public ListIterator<Text> listIterator() {
            return this.builder.list.listIterator();
        }

        @NotNull
        @Override
        public ListIterator<Text> listIterator(int index) {
            return this.builder.list.listIterator(index);
        }

        @NotNull
        @Override
        public List<Text> subList(int fromIndex, int toIndex) {
            return this.builder.list.subList(fromIndex, toIndex);
        }

        @Override
        public Spliterator<Text> spliterator() {
            return this.builder.list.spliterator();
        }

        @Override
        public Stream<Text> stream() {
            return this.builder.list.stream();
        }

        @Override
        public Stream<Text> parallelStream() {
            return this.builder.list.parallelStream();
        }

        @Override
        public void trimToSize() {

        }

        @Override
        public void ensureCapacity(int minCapacity) {

        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public Object clone() {
            return this;
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            if (toIndex >= fromIndex) {
                this.subList(fromIndex, toIndex + 1).clear();
            }
        }
    }
}
