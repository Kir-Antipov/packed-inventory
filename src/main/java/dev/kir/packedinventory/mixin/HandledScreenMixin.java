package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.api.v1.networking.PackedInventoryBulkEditRequest;
import dev.kir.packedinventory.api.v1.networking.PackedInventoryEditRequest;
import dev.kir.packedinventory.client.input.KeyInfo;
import dev.kir.packedinventory.client.input.PackedInventoryKeyBindings;
import dev.kir.packedinventory.client.screen.CustomHandleableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
abstract class HandledScreenMixin extends Screen implements CustomHandleableScreen {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected @Final ScreenHandler handler;

    @Shadow
    protected @Nullable Slot focusedSlot;

    private boolean isInteracting = false;

    private final Set<Slot> interactedSlots = new LinkedHashSet<>();

    private HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button);

    @Shadow
    protected abstract @Nullable Slot getSlotAt(double x, double y);

    @Override
    public void handleCustomMouseMoved(double mouseX, double mouseY) {
        if (!this.isInteracting) {
            return;
        }

        Slot interactedSlot = this.getSlotAt(mouseX, mouseY);
        if (interactedSlot != null && this.interactedSlots.stream().findFirst().map(x -> x.getStack().isEmpty() == interactedSlot.getStack().isEmpty()).orElse(true)) {
            this.interactedSlots.add(interactedSlot);
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0, shift = At.Shift.AFTER))
    private void drawSlot(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if (this.isInteracting && this.interactedSlots.size() > 1 && this.interactedSlots.contains(slot)) {
            fill(matrices, slot.x, slot.y, slot.x + 16, slot.y + 16, -2130706433);
        }
    }

    @Override
    public void handleCustomKeyPressed(KeyInfo key, double mouseX, double mouseY) {
        if (!key.matches(PackedInventoryKeyBindings.INTERACT_WITH_ITEM)) {
            return;
        }

        this.interactedSlots.clear();
        this.isInteracting = !this.handler.getCursorStack().isEmpty();
        if (!this.isInteracting) {
            return;
        }

        Slot interactedSlot = this.getSlotAt(mouseX, mouseY);
        if (interactedSlot != null) {
            this.interactedSlots.add(interactedSlot);
        }
    }

    @Override
    public void handleCustomKeyReleased(KeyInfo key, double mouseX, double mouseY) {
        if (!key.matches(PackedInventoryKeyBindings.INTERACT_WITH_ITEM)) {
            return;
        }

        List<Integer> slotIndices = this.interactedSlots.stream().map(this.handler.slots::indexOf).filter(x -> x >= 0).toList();
        this.interactedSlots.clear();
        this.isInteracting = false;
        if (slotIndices.size() > 1) {
            this.sendBulkEditRequest(slotIndices);
        } else {
            boolean isOutOfBounds = this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, GLFW.GLFW_MOUSE_BUTTON_LEFT);
            int focusedSlotIndex = !slotIndices.isEmpty()
                ? slotIndices.get(0)
                : this.focusedSlot == null
                    ? -1
                    : this.handler.slots.indexOf(this.focusedSlot);

            this.sendEditRequest(focusedSlotIndex, isOutOfBounds);
        }
    }

    private void sendEditRequest(int focusedSlotIndex, boolean isOutOfBounds) {
        PackedInventoryEditRequest.ActionType actionType = isOutOfBounds
            ? PackedInventoryEditRequest.ActionType.DROP
            : this.handler.getCursorStack().isEmpty()
                ? PackedInventoryEditRequest.ActionType.DEFAULT
                : PackedInventoryEditRequest.ActionType.QUICK_TRANSFER;

        if (focusedSlotIndex == -1) {
            if (actionType == PackedInventoryEditRequest.ActionType.QUICK_TRANSFER) {
                return;
            }
            focusedSlotIndex = PackedInventoryEditRequest.CURSOR_SLOT;
        }

        int primarySlotIndex;
        int secondarySlotIndex;
        if (actionType == PackedInventoryEditRequest.ActionType.QUICK_TRANSFER) {
            primarySlotIndex = PackedInventoryEditRequest.CURSOR_SLOT;
            secondarySlotIndex = focusedSlotIndex;
        } else {
            primarySlotIndex = focusedSlotIndex;
            secondarySlotIndex = PackedInventoryEditRequest.CURSOR_SLOT;
        }
        PackedInventoryEditRequest.sendToServer(actionType, primarySlotIndex, secondarySlotIndex, true);
    }

    private void sendBulkEditRequest(List<Integer> slotIndices) {
        if (this.handler.getCursorStack().isEmpty()) {
            return;
        }

        PackedInventoryBulkEditRequest.sendToServer(PackedInventoryBulkEditRequest.ActionType.QUICK_TRANSFER, PackedInventoryBulkEditRequest.CURSOR_SLOT, slotIndices, true);
    }
}
