package dev.kir.packedinventory.util.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class KeyBindingUtil {
    public static boolean isKeyBindingPressed(KeyBinding binding) {
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(binding);
        if (binding.isUnbound()) {
            return false;
        }

        Window window = MinecraftClient.getInstance().getWindow();
        if (window == null) {
            return false;
        }

        switch (boundKey.getCategory()) {
            case KEYSYM:
                return GLFW.glfwGetKey(window.getHandle(), boundKey.getCode()) == GLFW.GLFW_PRESS;
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
