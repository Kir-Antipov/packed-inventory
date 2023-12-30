package dev.kir.packedinventory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.GenericContainerScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(value = GenericContainerScreenHandler.class, priority = 0)
abstract class GenericContainerScreenHandlerMixin {
    @Shadow
    private @Final int rows;

    @ModifyArg(method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V", ordinal = 1), index = 3, require = 0)
    private int fixPlayerInventorySlotVerticalOffset(int y) {
        int baseOffset = (this.rows - 4) * 18 + 103;
        boolean wasModified = (y - baseOffset) % 18 != 0;
        return y - (wasModified ? 0 : 1);
    }

    @ModifyArg(method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V", ordinal = 2), index = 3, require = 0)
    private int fixHotbarSlotVerticalOffset(int y) {
        int baseOffset = (this.rows - 4) * 18 + 161;
        boolean wasModified = y != baseOffset;
        return y - (wasModified ? 0 : 1);
    }
}