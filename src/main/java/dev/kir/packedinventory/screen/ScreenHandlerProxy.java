package dev.kir.packedinventory.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

public class ScreenHandlerProxy extends AbstractRecipeScreenHandler<Inventory> {
    protected final ScreenHandler handler;

    public ScreenHandlerProxy(ScreenHandler handler) {
        this(handler.getType(), handler.syncId, handler);
    }

    public ScreenHandlerProxy(ScreenHandler handler, @Nullable ScreenHandlerType<?> type) {
        this(type, handler.syncId, handler);
    }

    private ScreenHandlerProxy(ScreenHandlerType<?> type, int syncId, ScreenHandler handler) {
        super(type, syncId);
        this.handler = handler;
        for (Slot slot : handler.slots) {
            this.addSlot(slot);
        }
    }

    public ScreenHandler getHandler() {
        return this.handler;
    }

    @Override
    public boolean isValid(int slot) {
        return this.handler.isValid(slot);
    }

    @Override
    public void addListener(ScreenHandlerListener listener) {
        this.handler.addListener(listener);
    }

    @Override
    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        this.handler.updateSyncHandler(handler);
    }

    @Override
    public void syncState() {
        this.handler.syncState();
    }

    @Override
    public void removeListener(ScreenHandlerListener listener) {
        this.handler.removeListener(listener);
    }

    @Override
    public DefaultedList<ItemStack> getStacks() {
        return this.handler.getStacks();
    }

    @Override
    public void sendContentUpdates() {
        this.handler.sendContentUpdates();
    }

    @Override
    public void updateToClient() {
        this.handler.updateToClient();
    }

    @Override
    public void setPreviousTrackedSlot(int slot, ItemStack stack) {
        this.handler.setPreviousTrackedSlot(slot, stack);
    }

    @Override
    public void setPreviousTrackedSlotMutable(int slot, ItemStack stack) {
        this.handler.setPreviousTrackedSlotMutable(slot, stack);
    }

    @Override
    public void setPreviousCursorStack(ItemStack stack) {
        this.handler.setPreviousCursorStack(stack);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        return this.handler.onButtonClick(player, id);
    }

    @Override
    public Slot getSlot(int index) {
        return this.handler.getSlot(index);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return this.handler.quickMove(player, index);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        this.handler.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return this.handler.canInsertIntoSlot(stack, slot);
    }

    @Override
    public boolean canInsertIntoSlot(Slot slot) {
        return this.handler.canInsertIntoSlot(slot);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        this.handler.onClosed(player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.handler.onContentChanged(inventory);
    }

    @Override
    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        this.handler.setStackInSlot(slot, revision, stack);
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        this.handler.updateSlotStacks(revision, stacks, cursorStack);
    }

    @Override
    public void setProperty(int id, int value) {
        this.handler.setProperty(id, value);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.handler.canUse(player);
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        this.handler.setCursorStack(stack);
    }

    @Override
    public ItemStack getCursorStack() {
        return this.handler.getCursorStack();
    }

    @Override
    public void disableSyncing() {
        this.handler.disableSyncing();
    }

    @Override
    public void enableSyncing() {
        this.handler.enableSyncing();
    }

    @Override
    public void copySharedSlots(ScreenHandler handler) {
        this.handler.copySharedSlots(handler);
    }

    @Override
    public OptionalInt getSlotIndex(Inventory inventory, int index) {
        return this.handler.getSlotIndex(inventory, index);
    }

    @Override
    public int getRevision() {
        return this.handler.getRevision();
    }

    @Override
    public int nextRevision() {
        return this.handler.nextRevision();
    }

    @Override
    public void fillInputSlots(boolean craftAll, Recipe<?> recipe, ServerPlayerEntity player) {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            ((AbstractRecipeScreenHandler<?>)this.handler).fillInputSlots(craftAll, recipe, player);
        }
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            ((AbstractRecipeScreenHandler<?>)this.handler).populateRecipeFinder(finder);
        }
    }

    @Override
    public void clearCraftingSlots() {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            ((AbstractRecipeScreenHandler<?>)this.handler).clearCraftingSlots();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(Recipe recipe) {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).matches(recipe);
        }
        return false;
    }

    @Override
    public int getCraftingResultSlotIndex() {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).getCraftingResultSlotIndex();
        }
        return -1;
    }

    @Override
    public int getCraftingWidth() {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).getCraftingWidth();
        }
        return 0;
    }

    @Override
    public int getCraftingHeight() {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).getCraftingHeight();
        }
        return 0;
    }

    @Override
    public int getCraftingSlotCount() {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).getCraftingSlotCount();
        }
        return 0;
    }

    @Override
    public RecipeBookCategory getCategory() {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).getCategory();
        }
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        if (this.handler instanceof AbstractRecipeScreenHandler) {
            return ((AbstractRecipeScreenHandler<?>)this.handler).canInsertIntoSlot(index);
        }
        return false;
    }
}
