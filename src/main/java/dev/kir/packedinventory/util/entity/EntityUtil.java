package dev.kir.packedinventory.util.entity;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class EntityUtil {
    private static final RegistryEntry<DimensionType> FAKE_DIMENSION = new RegistryEntry<>() {
        private static final RegistryKey<DimensionType> KEY = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier("fake:fake"));
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
        public boolean matchesRegistry(Registry<DimensionType> registry) {
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
        return new World(null, World.OVERWORLD, FAKE_DIMENSION, null, isClient, false, 0, 16) {
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
            public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {

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

    private EntityUtil() { }
}
