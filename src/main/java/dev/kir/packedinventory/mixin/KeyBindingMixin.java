package dev.kir.packedinventory.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.packedinventory.client.input.LocalKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(value = KeyBinding.class, priority = 0)
abstract class KeyBindingMixin {
    @WrapOperation(method = { "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", "Lnet/minecraft/client/option/KeyBinding;updateKeysByCode()V" }, at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object skipLocalKeyBindingRegistration(Map<Object, Object> keyBindings, Object key, Object keyBinding, Operation<Object> put) {
        if (key instanceof InputUtil.Key && keyBinding instanceof LocalKeyBinding) {
            return keyBinding;
        }

        return put.call(keyBindings, key, keyBinding);
    }
}