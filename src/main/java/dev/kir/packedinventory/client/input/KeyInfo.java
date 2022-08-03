package dev.kir.packedinventory.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

@Environment(EnvType.CLIENT)
public final class KeyInfo {
    private final int mouseCode;
    private final int keyCode;
    private final int scanCode;

    public KeyInfo(int mouseCode) {
        this(mouseCode, -1, -1);
    }

    public KeyInfo(int keyCode, int scanCode) {
        this(-1, keyCode, scanCode);
    }

    private KeyInfo(int mouseCode, int keyCode, int scanCode) {
        this.mouseCode = mouseCode;
        this.keyCode = keyCode;
        this.scanCode = scanCode;
    }

    public boolean matches(KeyBinding keyBinding) {
        return mouseCode == -1 ? keyBinding.matchesKey(this.keyCode, this.scanCode) : keyBinding.matchesMouse(this.mouseCode);
    }
}
