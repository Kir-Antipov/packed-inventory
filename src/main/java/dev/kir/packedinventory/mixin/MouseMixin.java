package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.client.input.KeyInfo;
import dev.kir.packedinventory.client.screen.CustomHandleableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
abstract class MouseMixin {
    @Shadow
    private @Final MinecraftClient client;

    @Shadow
    private double x;

    @Shadow
    private double y;

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
}
