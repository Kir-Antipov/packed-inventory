package dev.kir.packedinventory.mixin;

import dev.kir.packedinventory.api.v1.item.TooltipProviderContext;
import dev.kir.packedinventory.api.v1.item.TooltipProviderRegistry;
import dev.kir.packedinventory.api.v1.item.TooltipText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltipData", at = @At("RETURN"), cancellable = true)
    private void getTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        ItemStack it = (ItemStack)(Object)this;
        TooltipProviderContext tooltipProviderContext = TooltipProviderContext.of(cir.getReturnValue());
        TooltipProviderRegistry.getInstance().getTooltipData(it, tooltipProviderContext).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        this.beginTooltipSectionAndAddMissingSections(cir.getReturnValue(), TooltipText.Part.UNKNOWN);

        ItemStack it = (ItemStack)(Object)this;
        List<Text> list = cir.getReturnValue();
        TooltipText.Builder builder = list instanceof TooltipText.BuilderList ? ((TooltipText.BuilderList)list).asBuilder() : TooltipText.builder(list);
        TooltipProviderContext tooltipProviderContext = TooltipProviderContext.of(builder.build(), player, context);
        TooltipProviderRegistry.getInstance().getTooltipText(it, tooltipProviderContext).ifPresent(cir::setReturnValue);
    }

    private void beginTooltipSectionAndAddMissingSections(List<Text> list, TooltipText.Part part) {
        if (!(list instanceof TooltipText.BuilderList)) {
            return;
        }

        TooltipText.Builder builder = ((TooltipText.BuilderList)list).asBuilder();
        int ordinal = part.ordinal();
        TooltipText.Part[] parts = TooltipText.Part.values();
        int i = ordinal;
        while (i > 0 && !builder.containsSection(parts[i - 1])) {
            --i;
        }
        for (; i < ordinal; ++i) {
            builder.beginSection(parts[i]);
        }

        builder.beginSection(part);
    }

    @SuppressWarnings("deprecation")
    @ModifyVariable(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;formatted(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;", shift = At.Shift.AFTER, ordinal = 0))
    private List<Text> initTooltipTextBuilder(List<Text> list) {
        return TooltipText.builder(list).asList();
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginNameTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.NAME);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z", shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginMapIdTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.MAP_ID);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getType(Ljava/lang/String;)B", shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginLoreTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.LORE);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE, ordinal = 15), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginDurabilityTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.DURABILITY);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE, ordinal = 16), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginItemIdTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.ITEM_ID);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE, ordinal = 17), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginNbtTagsTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.NBT_TAGS);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginAdditionalTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.ADDITIONAL);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginEnchantmentsTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.ENCHANTMENTS);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginDyeTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.DYE);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 3), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginModifiersTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.MODIFIERS);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 4), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginUnbreakableTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.UNBREAKABLE);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 5), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginCanDestroyTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.CAN_DESTROY);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 6), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginCanPlaceTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.CAN_PLACE);
    }
}
