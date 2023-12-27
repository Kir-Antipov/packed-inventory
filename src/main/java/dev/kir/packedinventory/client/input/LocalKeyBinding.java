package dev.kir.packedinventory.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public final class LocalKeyBinding extends KeyBinding {
    private LocalKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
        super(translationKey, type, code, category);
    }

    public static KeyBinding create(String translationKey, int code, String category) {
        return LocalKeyBinding.create(translationKey, InputUtil.Type.KEYSYM, code, category);
    }

    public static KeyBinding create(String translationKey, InputUtil.Type type, int code, String category) {
        return new LocalKeyBinding(translationKey, type, code, category);
    }
}