package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.api.v1.inventory.InventoryAction;
import dev.kir.packedinventory.api.v1.inventory.InventoryTransferOptions;
import dev.kir.packedinventory.client.input.KeyInfo;
import dev.kir.packedinventory.client.input.PackedInventoryKeyBindings;
import dev.kir.packedinventory.client.screen.CustomHandleableScreen;
import dev.kir.packedinventory.util.client.input.KeyBindingUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
abstract class HandledScreenMixin extends Screen implements CustomHandleableScreen {
    private static final byte SELECTION_NONE = 0;

    private static final byte SELECTION_INTERACT = 1;

    private static final byte SELECTION_EXTRACT = 2;

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected @Final ScreenHandler handler;

    @Shadow
    protected @Nullable Slot focusedSlot;

    private byte selectionMode = SELECTION_NONE;

    private final Set<Slot> selectedSlots = new LinkedHashSet<>();

    private HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button);

    @Shadow
    protected abstract @Nullable Slot getSlotAt(double x, double y);

    @Override
    public void handleCustomMouseMoved(double mouseX, double mouseY) {
        if (this.selectionMode == SELECTION_NONE) {
            return;
        }

        Slot selectedSlot = this.getSlotAt(mouseX, mouseY);
        if (selectedSlot == null) {
            return;
        }

        if (shouldSelectSlot(selectedSlot, this.selectionMode, this.selectedSlots, this.handler.getCursorStack())) {
            this.selectedSlots.add(selectedSlot);
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0, shift = At.Shift.AFTER))
    private void drawSlot(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        final int SLOT_SIZE = 16;
        final int HIGHLIGHT_RGBA = -2130706433;

        if (this.selectionMode == SELECTION_NONE || slot == this.focusedSlot) {
            return;
        }

        if (this.selectedSlots.contains(slot)) {
            fill(matrices, slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, HIGHLIGHT_RGBA);
        }
    }

    @Override
    public void handleCustomKeyPressed(KeyInfo key, double mouseX, double mouseY) {
        if (key.matches(PackedInventoryKeyBindings.INTERACT_WITH_ITEM)) {
            this.selectionMode = KeyBindingUtil.isKeyBindingPressed(PackedInventoryKeyBindings.INTERACT_WITH_ITEM_MODIFIER) ? SELECTION_EXTRACT : SELECTION_INTERACT;
        } else if (key.matches(PackedInventoryKeyBindings.EXTRACT_FROM_ITEM)) {
            this.selectionMode = SELECTION_EXTRACT;
        } else {
            return;
        }

        this.selectedSlots.clear();

        Slot selectedSlot = this.getSlotAt(mouseX, mouseY);
        if (selectedSlot == null) {
            return;
        }

        if (shouldSelectSlot(selectedSlot, this.selectionMode, this.selectedSlots, this.handler.getCursorStack())) {
            this.selectedSlots.add(selectedSlot);
        }
    }

    @Override
    public void handleCustomKeyReleased(KeyInfo key, double mouseX, double mouseY) {
        if (this.selectionMode == SELECTION_NONE || !isExpectedKey(key, this.selectionMode)) {
            return;
        }

        IntList selectedSlots = IntArrayList.toList(this.selectedSlots.stream().mapToInt(this.handler.slots::indexOf).filter(x -> x >= 0));
        boolean hasEmptySelectedSlot = this.selectedSlots.stream().anyMatch(x -> !x.hasStack());
        boolean isCursorStackEmpty = this.handler.getCursorStack().isEmpty();
        boolean isOutOfBounds = this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        boolean preferExtraction = this.selectionMode == SELECTION_EXTRACT;

        InventoryAction action = getInventoryAction(selectedSlots, hasEmptySelectedSlot, isCursorStackEmpty, isOutOfBounds, preferExtraction);
        if (action != null) {
            action.invoke();
        }

        this.selectedSlots.clear();
        this.selectionMode = SELECTION_NONE;
    }

    private static boolean shouldSelectSlot(Slot slot, int selectionMode, Collection<Slot> selectedSlots, ItemStack cursorStack) {
        if (selectionMode == SELECTION_EXTRACT) {
            return !cursorStack.isEmpty() || slot.hasStack();
        }

        if (selectionMode == SELECTION_INTERACT) {
            if (cursorStack.isEmpty()) {
                return selectedSlots.isEmpty() && slot.hasStack();
            }

            return selectedSlots.isEmpty() || selectedSlots.iterator().next().hasStack() == slot.hasStack();
        }

        return false;
    }

    private static boolean isExpectedKey(KeyInfo key, int selectionMode) {
        if (selectionMode == SELECTION_INTERACT) {
            return key.matches(PackedInventoryKeyBindings.INTERACT_WITH_ITEM);
        }

        if (selectionMode == SELECTION_EXTRACT) {
            return key.matches(PackedInventoryKeyBindings.EXTRACT_FROM_ITEM) || key.matches(PackedInventoryKeyBindings.INTERACT_WITH_ITEM);
        }

        return false;
    }

    private static InventoryAction getInventoryAction(IntList selectedSlots, boolean hasEmptySelectedSlot, boolean isCursorStackEmpty, boolean isOutOfBounds, boolean preferExtraction) {
        if (isCursorStackEmpty && selectedSlots.isEmpty()) {
            return null;
        }

        if (isOutOfBounds && selectedSlots.isEmpty()) {
            return InventoryAction.drop(InventoryAction.CURSOR_SLOT);
        }

        if (isCursorStackEmpty && !preferExtraction) {
            return InventoryAction.handle(selectedSlots);
        }

        if (isCursorStackEmpty) {
            return InventoryAction.transfer(selectedSlots, IntList.of(InventoryAction.CURSOR_SLOT));
        }

        if (hasEmptySelectedSlot) {
            return InventoryAction.transfer(IntList.of(InventoryAction.CURSOR_SLOT), selectedSlots);
        }

        if (preferExtraction) {
            return InventoryAction.transfer(IntList.of(InventoryAction.CURSOR_SLOT), selectedSlots, EnumSet.of(InventoryTransferOptions.PREFER_EXTRACTION));
        }

        return InventoryAction.transfer(selectedSlots, IntList.of(InventoryAction.CURSOR_SLOT), EnumSet.of(InventoryTransferOptions.PREFER_INSERTION));
    }
}
