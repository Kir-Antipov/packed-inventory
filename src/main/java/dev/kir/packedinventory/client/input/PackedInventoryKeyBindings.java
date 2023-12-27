package dev.kir.packedinventory.client.input;

import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.networking.PackedInventoryEditRequest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class PackedInventoryKeyBindings {
    private static final String CATEGORY = "key.categories." + PackedInventory.MOD_ID;
    private static final String KEY_PATH = "key." + PackedInventory.MOD_ID + ".";

    public static final KeyBinding INTERACT_WITH_ITEM;
    public static final KeyBinding INVERT_TOOLTIP_VISIBILITY;
    public static final KeyBinding INVERT_TOOLTIP_COMPACT_MODE;

    static {
        INTERACT_WITH_ITEM = register("interact_with_item", GLFW.GLFW_KEY_K, PackedInventoryKeyBindings::requestEdit);
        INVERT_TOOLTIP_VISIBILITY = register("invert_tooltip_visibility", GLFW.GLFW_KEY_LEFT_SHIFT);
        INVERT_TOOLTIP_COMPACT_MODE = register("invert_tooltip_compact_mode", GLFW.GLFW_KEY_C);
    }

    private static void requestEdit() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            PackedInventoryEditRequest.sendToServer(player.getInventory().selectedSlot);
        }
    }

    private static KeyBinding register(String name, int code) {
        return register(LocalKeyBinding.create(KEY_PATH + name, code, CATEGORY));
    }

    private static KeyBinding register(String name, int code, Runnable action) {
        return register(InvokableKeyBinding.create(KEY_PATH + name, code, CATEGORY, action));
    }

    private static KeyBinding register(KeyBinding keyBinding) {
        return KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    public static void initClient() { }
}
