package dev.kir.packedinventory.util.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class EntityUtil {
    private static final RegistryEntry<DimensionType> FAKE_DIMENSION = new RegistryEntry<>() {
        private static final RegistryKey<DimensionType> KEY = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier("fake:fake"));
        private static final DimensionType VALUE = new DimensionType(OptionalLong.empty(), true, false, false, true, 1.0D, false, true, 0, 32, 32, BlockTags.DIRT, new Identifier("fake:fake"), 1, new DimensionType.MonsterSettings(false, false, ConstantIntProvider.ZERO, 0));

        @Override
        public DimensionType value() {
            return VALUE;
        }

        @Override
        public boolean hasKeyAndValue() {
            return true;
        }

        @Override
        public boolean matchesId(Identifier id) {
            return false;
        }

        @Override
        public boolean matchesKey(RegistryKey<DimensionType> key) {
            return false;
        }

        @Override
        public boolean matches(Predicate<RegistryKey<DimensionType>> predicate) {
            return false;
        }

        @Override
        public boolean isIn(TagKey<DimensionType> tag) {
            return false;
        }

        @Override
        public Stream<TagKey<DimensionType>> streamTags() {
            return Stream.of();
        }

        @Override
        public Either<RegistryKey<DimensionType>, DimensionType> getKeyOrValue() {
            return Either.right(VALUE);
        }

        @Override
        public Optional<RegistryKey<DimensionType>> getKey() {
            return Optional.of(KEY);
        }

        @Override
        public Type getType() {
            return null;
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<DimensionType> owner) {
            return false;
        }
    };

    private static final LivingEntity FAKE_SERVER_LIVING_ENTITY = createLivingEntity(false);
    private static final LivingEntity FAKE_CLIENT_LIVING_ENTITY = createLivingEntity(true);

    public static boolean isFakeEntity(Entity entity) {
        return entity == FAKE_SERVER_LIVING_ENTITY || entity == FAKE_CLIENT_LIVING_ENTITY;
    }

    public static LivingEntity getFakeClientLivingEntity() {
        return FAKE_CLIENT_LIVING_ENTITY;
    }

    public static LivingEntity getFakeServerLivingEntity() {
        return FAKE_SERVER_LIVING_ENTITY;
    }

    private static LivingEntity createLivingEntity(boolean isClient) {
        return new LivingEntity(EntityType.PIG, createWorld(isClient)) {
            @Override
            public Iterable<ItemStack> getArmorItems() {
                return null;
            }

            @Override
            public ItemStack getEquippedStack(EquipmentSlot slot) {
                return null;
            }

            @Override
            public void equipStack(EquipmentSlot slot, ItemStack stack) {

            }

            @Override
            public Arm getMainArm() {
                return null;
            }
        };
    }

    private static World createWorld(boolean isClient) {
        return new World(null, World.OVERWORLD, createDynamicRegistryManager(), FAKE_DIMENSION, null, isClient, false, 0, 16) {
            @Override
            public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
                return null;
            }

            @Override
            public List<? extends PlayerEntity> getPlayers() {
                return null;
            }

            @Override
            public float getBrightness(Direction direction, boolean shaded) {
                return 0;
            }

            @Override
            public DynamicRegistryManager getRegistryManager() {
                return null;
            }

            @Override
            public FeatureSet getEnabledFeatures() {
                return FeatureSet.empty();
            }

            @Override
            public QueryableTickScheduler<Block> getBlockTickScheduler() {
                return null;
            }

            @Override
            public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
                return null;
            }

            @Override
            public ChunkManager getChunkManager() {
                return null;
            }

            @Override
            public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

            }

            @Override
            public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {

            }

            @Override
            public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {

            }

            @Override
            public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

            }

            @Override
            public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {

            }

            @Override
            public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

            }

            @Override
            public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

            }


            @Override
            public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {

            }

            @Override
            public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {

            }

            @Override
            public String asString() {
                return null;
            }

            @Nullable
            @Override
            public Entity getEntityById(int id) {
                return null;
            }

            @Nullable
            @Override
            public MapState getMapState(String id) {
                return null;
            }

            @Override
            public void putMapState(String id, MapState state) {

            }

            @Override
            public int getNextMapId() {
                return 0;
            }

            @Override
            public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {

            }

            @Override
            public Scoreboard getScoreboard() {
                return null;
            }

            @Override
            public RecipeManager getRecipeManager() {
                return null;
            }

            @Override
            protected EntityLookup<Entity> getEntityLookup() {
                return null;
            }
        };
    }

    private static DynamicRegistryManager createDynamicRegistryManager() {
        SimpleRegistry<Registry<DamageType>> registries = new SimpleRegistry<>(RegistryKey.ofRegistry(new Identifier("fake", "registries")), Lifecycle.stable());

        registries.add(RegistryKeys.DAMAGE_TYPE, createRegistry(RegistryKeys.DAMAGE_TYPE, Lifecycle.stable()), Lifecycle.stable());

        return DynamicRegistryManager.of(registries);
    }

    private static <T> Registry<T> createRegistry(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        return new Registry<>() {
            @Override
            public RegistryKey<? extends Registry<T>> getKey() {
                return key;
            }

            @Nullable
            @Override
            public Identifier getId(T value) {
                return null;
            }

            @Override
            public Optional<RegistryKey<T>> getKey(T entry) {
                return Optional.empty();
            }

            @Override
            public int getRawId(@Nullable T value) {
                return 0;
            }

            @Nullable
            @Override
            public T get(@Nullable RegistryKey<T> key) {
                return null;
            }

            @Nullable
            @Override
            public T get(@Nullable Identifier id) {
                return null;
            }

            @Override
            public Lifecycle getEntryLifecycle(T entry) {
                return lifecycle;
            }

            @Override
            public Lifecycle getLifecycle() {
                return lifecycle;
            }

            @Override
            public Set<Identifier> getIds() {
                return Set.of();
            }

            @Override
            public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
                return Set.of();
            }

            @Override
            public Set<RegistryKey<T>> getKeys() {
                return Set.of();
            }

            @Override
            public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
                return Optional.empty();
            }

            @Override
            public boolean containsId(Identifier id) {
                return false;
            }

            @Override
            public boolean contains(RegistryKey<T> key) {
                return false;
            }

            @Override
            public Registry<T> freeze() {
                return this;
            }

            @Override
            public RegistryEntry.Reference<T> createEntry(T value) {
                return null;
            }

            @Override
            public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
                return Optional.empty();
            }

            @Override
            public RegistryEntry.Reference<T> entryOf(RegistryKey<T> key) {
                return null;
            }

            @Override
            public Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key) {
                return Optional.empty();
            }

            @Override
            public RegistryEntry<T> getEntry(T value) {
                return RegistryEntry.of(value);
            }

            @Override
            public Stream<RegistryEntry.Reference<T>> streamEntries() {
                return Stream.empty();
            }

            @Override
            public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
                return Optional.empty();
            }

            @Override
            public RegistryEntryList.Named<T> getOrCreateEntryList(TagKey<T> tag) {
                return null;
            }

            @Override
            public Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
                return Stream.empty();
            }

            @Override
            public Stream<TagKey<T>> streamTags() {
                return Stream.empty();
            }

            @Override
            public void clearTags() {

            }

            @Override
            public void populateTags(Map<TagKey<T>, List<RegistryEntry<T>>> tagEntries) {

            }

            @Override
            public RegistryEntryOwner<T> getEntryOwner() {
                return new RegistryEntryOwner<>() { };
            }

            @Override
            public RegistryWrapper.Impl<T> getReadOnlyWrapper() {
                return new RegistryWrapper.Impl.Delegating<>() {
                    @Override
                    protected Impl<T> getBase() {
                        return null;
                    }
                };
            }

            @Nullable
            @Override
            public T get(int index) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @NotNull
            @Override
            public Iterator<T> iterator() {
                return stream().iterator();
            }
        };
    }

    private EntityUtil() { }
}
