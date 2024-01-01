package dev.kir.packedinventory.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.packedinventory.client.input.KeyInfo;
import dev.kir.packedinventory.client.screen.CustomHandleableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = Mouse.class, priority = 10000)
abstract class MouseMixin {
    @Shadow
    private @Final MinecraftClient client;

    @Shadow
    private double x;

    @Shadow
    private double y;

    private float lastMouseLockTime;

    private float lastX;

    private float lastY;

    @Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if (!(screen instanceof CustomHandleableScreen)) {
            return;
        }

        KeyInfo key = new KeyInfo(button);
        double scaledX = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
        double scaledY = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
        if (action == GLFW.GLFW_PRESS) {
            Screen.wrapScreenError(
                () -> ((CustomHandleableScreen)screen).handleCustomKeyPressed(key, scaledX, scaledY),
                "customKeyPressed event handler",
                screen.getClass().getName()
            );
        } else {
            Screen.wrapScreenError(
                () -> ((CustomHandleableScreen)screen).handleCustomKeyReleased(key, scaledX, scaledY),
                "customKeyReleased event handler",
                screen.getClass().getName()
            );
        }
    }

    @Inject(method = "onCursorPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0))
    private void onMouseMove(long window, double x, double y, CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if (!(screen instanceof CustomHandleableScreen)) {
            return;
        }

        double scaledX = x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
        double scaledY = y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
        Screen.wrapScreenError(
            () -> ((CustomHandleableScreen)screen).handleCustomMouseMoved(scaledX, scaledY),
            "customMouseMoved event handler",
            screen.getClass().getName()
        );
    }

    @Inject(method = "lockCursor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;x:D", ordinal = 0, shift = At.Shift.BEFORE))
    private void lockCursor(CallbackInfo ci) {
        this.lastX = (float)this.x;
        this.lastY = (float)this.y;
        this.lastMouseLockTime = (float)GlfwUtil.getTime();
    }

    @WrapOperation(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(JIDD)V", ordinal = 0))
    private void unlockCursor(long window, int cursorState, double x, double y, Operation<Void> setCursor) {
        final float MAX_SECONDS_SINCE_LAST_LOCK = 0.2F;

        float currentTime = (float)GlfwUtil.getTime();
        float secondsSinceLastLock = currentTime - this.lastMouseLockTime;

        if (secondsSinceLastLock <= MAX_SECONDS_SINCE_LAST_LOCK) {
            this.x = x = this.lastX;
            this.y = y = this.lastY;

            // If we are here, it means that the `lockCursor` method has been called
            // just before this one.
            // `lockCursor` sets the cursor position to the center of the screen using the
            // `GLFW_CURSOR_DISABLED` flag. For some reason, if we set the cursor position
            // to some other place right after that using the `GLFW_CURSOR_NORMAL` flag,
            // new coordinates will be simply ignored. However, changing the cursor state
            // back and forth a few times fixes the problem. So, let's just do that.
            InputUtil.setCursorParameters(window, InputUtil.GLFW_CURSOR_NORMAL, x, y);
            InputUtil.setCursorParameters(window, InputUtil.GLFW_CURSOR_DISABLED, x, y);
        }

        setCursor.call(window, cursorState, x, y);
    }
}
