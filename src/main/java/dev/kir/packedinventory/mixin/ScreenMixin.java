package dev.kir.packedinventory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
abstract class ScreenMixin {
    @ModifyConstant(method = "renderTooltipFromComponents", constant = @Constant(intValue = 8, ordinal = 0), require = 0)
    private int fixTooltipRenderPosition(int constant) {
        return -1000000;
    }
}
