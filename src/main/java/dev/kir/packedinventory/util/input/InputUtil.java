package dev.kir.packedinventory.util.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class InputUtil {
    public static boolean isKeyBindingPressed(KeyBinding binding) {
        net.minecraft.client.util.InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(binding);
        if (binding.isUnbound()) {
            return false;
        }

        Window window = MinecraftClient.getInstance().getWindow();
        if (window == null) {
            return false;
        }

        switch (boundKey.getCategory()) {
            case KEYSYM:
                return net.minecraft.client.util.InputUtil.isKeyPressed(window.getHandle(), boundKey.getCode());
            case MOUSE:
                return GLFW.glfwGetMouseButton(window.getHandle(), boundKey.getCode()) == GLFW.GLFW_PRESS;
            default:
                return false;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static void stopPropagation(KeyBinding binding) {
        while (binding.wasPressed());
    }
}
