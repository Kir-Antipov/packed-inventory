package dev.kir.packedinventory.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// This mixin should be applied last to function correctly.
// Therefore, we set its priority to *almost* the maximum possible number.
// This way, we can be reasonably certain that, in most cases, this mixin will indeed be applied last.
// However, we still leave some room for mixins that know what they are doing and explicitly require
// post-application after this one.
@Environment(EnvType.CLIENT)
@Mixin(value = ItemStack.class, priority = Integer.MAX_VALUE - 512)
abstract class ItemStackMixin {
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
    @ModifyReturnValue(method = "getTooltipData", at = @At("RETURN"))
    private Optional<TooltipData> getTooltipData(Optional<TooltipData> tooltipData) {
        ItemStack it = (ItemStack)(Object)this;
        TooltipProviderContext tooltipProviderContext = TooltipProviderContext.of(tooltipData);
        return TooltipProviderRegistry.getInstance().getTooltipData(it, tooltipProviderContext).orElse(tooltipData);
    }

    @SuppressWarnings("unused")
    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> getTooltip(List<Text> tooltip, PlayerEntity player, TooltipContext context) {
        this.beginTooltipSectionAndAddMissingSections(tooltip, TooltipText.Part.UNKNOWN);

        ItemStack it = (ItemStack)(Object)this;
        TooltipText.Builder builder = tooltip instanceof TooltipText.BuilderList ? ((TooltipText.BuilderList)tooltip).asBuilder() : TooltipText.builder(tooltip);
        TooltipProviderContext tooltipProviderContext = TooltipProviderContext.of(builder.build(), player, context);
        return TooltipProviderRegistry.getInstance().getTooltipText(it, tooltipProviderContext).orElse(tooltip);
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

    @SuppressWarnings({"deprecation", "unused"})
    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", ordinal = 0))
    private ArrayList<Text> initTooltipTextBuilder(Operation<ArrayList<Text>> listFactory) {
        return TooltipText.builder(listFactory.call()).asList();
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
    private void beginUpgradesTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.UPGRADES);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginEnchantmentsTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.ENCHANTMENTS);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 3), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginDyeTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.DYE);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 4), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginModifiersTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.MODIFIERS);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 5), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginUnbreakableTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.UNBREAKABLE);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 6), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginCanDestroyTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.CAN_DESTROY);
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", shift = At.Shift.BEFORE, ordinal = 7), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void beginCanPlaceTooltipSection(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        this.beginTooltipSectionAndAddMissingSections(list, TooltipText.Part.CAN_PLACE);
    }
}
