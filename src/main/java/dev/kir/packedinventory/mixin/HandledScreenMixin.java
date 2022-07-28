package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.api.v1.networking.PackedInventoryEditRequest;
import dev.kir.packedinventory.client.input.PackedInventoryKeyBindings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
abstract class HandledScreenMixin extends Screen {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected @Nullable Slot focusedSlot;

    @Shadow
    protected @Final ScreenHandler handler;

    private HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button);

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!PackedInventoryKeyBindings.INTERACT_WITH_ITEM.matchesMouse(button)) {
            return;
        }

        this.interactedWithItem(this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, button));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!PackedInventoryKeyBindings.INTERACT_WITH_ITEM.matchesKey(keyCode, scanCode)) {
            return;
        }

        if (this.client == null) {
            this.interactedWithItem(false);
        } else {
            double mouseX = this.client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
            double mouseY = this.client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
            this.interactedWithItem(this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, GLFW.GLFW_MOUSE_BUTTON_LEFT));
        }
    }

    private void interactedWithItem(boolean isOutOfBounds) {
        PackedInventoryEditRequest.ActionType actionType = isOutOfBounds
            ? PackedInventoryEditRequest.ActionType.DROP
            : this.handler.getCursorStack().isEmpty()
                ? PackedInventoryEditRequest.ActionType.DEFAULT
                : PackedInventoryEditRequest.ActionType.QUICK_TRANSFER;

        int focusedSlotIndex = this.focusedSlot == null ? -1 : this.handler.slots.indexOf(this.focusedSlot);
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
}
