package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.client.input.KeyInfo;
import dev.kir.packedinventory.client.screen.CustomHandleableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Keyboard.class)
abstract class KeyboardMixin {
    @Shadow
    private @Final MinecraftClient client;

    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"))
    private void onKeyPress(long window, int button, int scanCode, int action, int modifiers, CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if (!(screen instanceof CustomHandleableScreen)) {
            return;
        }

        KeyInfo key = new KeyInfo(button, scanCode);
        double scaledX = this.client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
        double scaledY = this.client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
        if (action == GLFW.GLFW_PRESS) {
            Screen.wrapScreenError(
                () -> ((CustomHandleableScreen)screen).handleCustomKeyPressed(key, scaledX, scaledY),
                "customKeyPressed event handler",
                screen.getClass().getName()
            );
        } else if (action == GLFW.GLFW_RELEASE) {
            Screen.wrapScreenError(
                () -> ((CustomHandleableScreen)screen).handleCustomKeyReleased(key, scaledX, scaledY),
                "customKeyReleased event handler",
                screen.getClass().getName()
            );
        }
    }
}
