package dev.kir.packedinventory.client.screen;

import dev.kir.packedinventory.client.input.KeyInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface CustomHandleableScreen {
    default void handleCustomKeyPressed(KeyInfo key, double mouseX, double mouseY) { }

    default void handleCustomKeyReleased(KeyInfo key, double mouseX, double mouseY) { }

    default void handleCustomMouseMoved(double mouseX, double mouseY) { }
}
