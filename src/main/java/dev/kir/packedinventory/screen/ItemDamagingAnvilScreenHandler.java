package dev.kir.packedinventory.screen;

import dev.kir.packedinventory.util.inventory.InventoryUtil;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ItemDamagingAnvilScreenHandler extends AnvilScreenHandler {
    public static final Text TITLE = Text.translatable("container.repair");

    protected ItemStack anvil;

    public ItemDamagingAnvilScreenHandler(int syncId, ItemStack anvil, PlayerEntity player) {
        this(syncId, anvil, player.getInventory(), ItemDamagingAnvilScreenHandler.createContext(player.world, player.getBlockPos()));
    }

    public ItemDamagingAnvilScreenHandler(int syncId, ItemStack anvil, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(syncId, playerInventory, context);
        this.anvil = anvil;
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        if (this.context instanceof AnvilScreenHandlerContext) {
            ((AnvilScreenHandlerContext)this.context).overrideRunWith(this::damageAnvil);
        }
        super.onTakeOutput(player, stack);
    }

    private void damageAnvil(World world, BlockPos pos) {
        if (!(this.anvil.getItem() instanceof BlockItem)) {
            return;
        }

        PlayerInventory inventory = this.player.getInventory();
        int anvilIndex = InventoryUtil.indexOf(inventory, this.anvil);

        BlockState anvilBlockState = ((BlockItem)this.anvil.getItem()).getBlock().getDefaultState();
        if (anvilIndex == -1 || !anvilBlockState.isIn(BlockTags.ANVIL) || !this.shouldBreak(this.player)) {
            world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
            return;
        }

        BlockState damagedAnvilState = AnvilBlock.getLandingState(anvilBlockState);
        if (damagedAnvilState == null) {
            this.anvil = ItemStack.EMPTY;
            inventory.removeStack(anvilIndex);
            world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
            return;
        }

        ItemStack newAnvil = new ItemStack(damagedAnvilState.getBlock(), this.anvil.getCount());
        if (this.anvil.getNbt() != null) {
            newAnvil.getOrCreateNbt().copyFrom(this.anvil.getNbt());
        }
        this.anvil = newAnvil;
        inventory.setStack(anvilIndex, newAnvil);
        world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
    }

    private boolean shouldBreak(PlayerEntity player) {
        return !this.player.getAbilities().creativeMode && player.getRandom().nextFloat() < 0.12F;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return !this.anvil.isEmpty();
    }

    public static ScreenHandlerContext createContext(World world, BlockPos pos) {
        return ItemDamagingAnvilScreenHandler.createContext(ScreenHandlerContext.create(world, pos));
    }

    public static ScreenHandlerContext createContext(ScreenHandlerContext context) {
        return new AnvilScreenHandlerContext(context);
    }

    private static final class AnvilScreenHandlerContext implements ScreenHandlerContext {
        private final ScreenHandlerContext context;
        private BiConsumer<World, BlockPos> runFunction;

        public AnvilScreenHandlerContext(ScreenHandlerContext context) {
            this.context = context;
            this.runFunction = null;
        }

        @Override
        public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
            return this.context.get(getter);
        }

        @Override
        public <T> T get(BiFunction<World, BlockPos, T> getter, T defaultValue) {
            return this.context.get(getter, defaultValue);
        }

        public void overrideRunWith(BiConsumer<World, BlockPos> function) {
            this.runFunction = function;
        }

        @Override
        public void run(BiConsumer<World, BlockPos> function) {
            if (this.runFunction != null) {
                function = this.runFunction;
                this.runFunction = null;
            }
            this.context.run(function);
        }
    }
}
