package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.api.v1.networking.PackedInventoryEditRequest;
import dev.kir.packedinventory.input.PackedInventoryKeyBindings;
import dev.kir.packedinventory.util.input.InputUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
abstract class HandledScreenMixin extends Screen {
    @Shadow
    protected @Nullable Slot focusedSlot;

    @Shadow
    protected @Final ScreenHandler handler;

    private HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.focusedSlot == null || !InputUtil.isKeyBindingPressed(PackedInventoryKeyBindings.OPEN_EDIT_SCREEN)) {
            return;
        }

        int index = this.handler.slots.indexOf(this.focusedSlot);
        if (index != -1) {
            PackedInventoryEditRequest.sendToServer(index, true);
        }
    }
}
