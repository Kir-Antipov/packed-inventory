package dev.kir.packedinventory.api.v1.inventory;

import dev.kir.packedinventory.api.v1.FailureReason;
import dev.kir.packedinventory.api.v1.config.PackedInventoryApiConfig;
import dev.kir.packedinventory.util.entity.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains common {@link dev.kir.packedinventory.api.v1.inventory.InventoryViewer.Validator}s and tools for their creation.
 */
public final class InventoryValidators {
    private static final Map<Enchantment, Integer> SILK_TOUCH_ENCHANTMENT = Map.of(Enchantments.SILK_TOUCH, 1);

    /**
     * Always passes.
     */
    public static final InventoryViewer.Validator EMPTY = InventoryViewer.Validator.EMPTY;
    /**
     * Passes when player is on the ground.
     */
    public static final InventoryViewer.Validator IS_ON_GROUND = (inv, slot, player) -> player.isOnGround() ? null : FailureReason.PLAYER_IS_NOT_ON_THE_GROUND;
    /**
     * Passes when player is in the creative mode.
     */
    public static final InventoryViewer.Validator IS_IN_CREATIVE = (inv, slot, player) -> player.getAbilities().creativeMode ? null : FailureReason.PLAYER_IS_NOT_IN_THE_CREATIVE_MODE;
    /**
     * Passes when player has a pickaxe enchanted with silk touch enchantment.
     */
    public static final InventoryViewer.Validator HAS_PICKAXE_WITH_SILK_TOUCH = InventoryValidators.hasSilkTouchToolFor(Blocks.STONE, 1, FailureReason.PLAYER_NEEDS_PICKAXE_ENCHANTED_WITH_SILK_TOUCH_ENCHANTMENT);


    /**
     * Returns a validator that passes when a validation config with the given {@code id} of the provided
     * {@code configClass} passes the {@code predicate} and returns {@code true}.
     * @param item {@link Item} associated with the validation config.
     * @param config {@link PackedInventoryApiConfig} instance.
     * @param defaultConfig Default config value in case the requested one does not exist.
     * @param predicate Validation config predicate.
     * @param <TConfig> Validation config type.
     * @return Validator that passes when a validation config with the given {@code id} of the provided {@code configClass} exists in {@link PackedInventoryApiConfig} and {@code predicate} returns {@code true}.
     */
    public static <TConfig> InventoryViewer.Validator config(Item item, PackedInventoryApiConfig config, TConfig defaultConfig, Predicate<TConfig> predicate) {
        return InventoryValidators.config(Registry.ITEM.getId(item), config, defaultConfig, predicate);
    }

    /**
     * Returns a validator that passes when a validation config with the given {@code id} of the provided
     * {@code configClass} passes the {@code predicate} and returns {@code true}.
     * @param id Validation config identifier.
     * @param config {@link PackedInventoryApiConfig} instance.
     * @param defaultConfig Default config value in case the requested one does not exist.
     * @param predicate Validation config predicate.
     * @param <TConfig> Validation config type.
     * @return Validator that passes when a validation config with the given {@code id} of the provided {@code configClass} exists in {@link PackedInventoryApiConfig} and {@code predicate} returns {@code true}.
     */
    public static <TConfig> InventoryViewer.Validator config(Identifier id, PackedInventoryApiConfig config, TConfig defaultConfig, Predicate<TConfig> predicate) {
        return (player, inv, slot) -> {
            TConfig cfg = config.getValidationConfigOrDefault(id, defaultConfig);
            return predicate.test(cfg) ? null : FailureReason.OTHER_PROBLEM;
        };
    }

    /**
     * Returns a validator that passes when a validation config with the given {@code id} of the provided
     * {@code configClass} exists in {@link PackedInventoryApiConfig} and {@code predicate} returns {@code true}.
     * @param item {@link Item} associated with the validation config.
     * @param config {@link PackedInventoryApiConfig} instance.
     * @param configClass Validation config class.
     * @param predicate Validation config predicate.
     * @param <TConfig> Validation config type.
     * @return Validator that passes when a validation config with the given {@code id} of the provided {@code configClass} exists in {@link PackedInventoryApiConfig} and {@code predicate} returns {@code true}.
     */
    public static <TConfig> InventoryViewer.Validator config(Item item, PackedInventoryApiConfig config, Class<TConfig> configClass, Predicate<TConfig> predicate) {
        return InventoryValidators.config(Registry.ITEM.getId(item), config, configClass, predicate);
    }

    /**
     * Returns a validator that passes when a validation config with the given {@code id} of the provided
     * {@code configClass} exists in {@link PackedInventoryApiConfig} and {@code predicate} returns {@code true}.
     * @param id Validation config identifier.
     * @param config {@link PackedInventoryApiConfig} instance.
     * @param configClass Validation config class.
     * @param predicate Validation config predicate.
     * @param <TConfig> Validation config type.
     * @return Validator that passes when a validation config with the given {@code id} of the provided {@code configClass} exists in {@link PackedInventoryApiConfig} and {@code predicate} returns {@code true}.
     */
    public static <TConfig> InventoryViewer.Validator config(Identifier id, PackedInventoryApiConfig config, Class<TConfig> configClass, Predicate<TConfig> predicate) {
        return (player, inv, slot) -> {
            TConfig cfg = config.getValidationConfig(id, configClass);
            if (cfg == null) {
                return FailureReason.OTHER_PROBLEM;
            }
            return predicate.test(cfg) ? null : FailureReason.OTHER_PROBLEM;
        };
    }


    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has silk touch enchantment.</li>
     * </ul>
     *
     * @param block Block that a tool should be able to mine.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasSilkTouchToolFor(Block block, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, SILK_TOUCH_ENCHANTMENT, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has silk touch enchantment.</li>
     *     <li>Has at least {@code damageAmount + 1} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasSilkTouchToolFor(Block block, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, SILK_TOUCH_ENCHANTMENT, damageAmount, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has silk touch enchantment.</li>
     *     <li>Has at least {@code minRemainingDurability} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param minRemainingDurability Minimum remaining durability amount of a tool.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasSilkTouchToolFor(Block block, int minRemainingDurability, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, SILK_TOUCH_ENCHANTMENT, minRemainingDurability, damageAmount, failureReason);
    }


    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     * </ul>
     *
     * @param block Block that a tool should be able to mine.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, Map.of(), failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has at least {@code damageAmount + 1} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, Map.of(), damageAmount, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has at least {@code minRemainingDurability} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param minRemainingDurability Minimum remaining durability amount of a tool.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, int minRemainingDurability, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, Map.of(), minRemainingDurability, damageAmount, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has all the specified {@code enchantments}.</li>
     * </ul>
     *
     * @param block Block that a tool should be able to mine.
     * @param enchantments Enchantments that a tool should have.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, Collection<Enchantment> enchantments, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, enchantments.stream().collect(Collectors.toMap(x -> x, x -> 1)), failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has all the specified {@code enchantments} (or better ones).</li>
     * </ul>
     *
     * @param block Block that a tool should be able to mine.
     * @param enchantments Map of enchantments and their levels that a tool should have.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, Map<Enchantment, Integer> enchantments, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, enchantments, 0, 0, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has all the specified {@code enchantments}.</li>
     *     <li>Has at least {@code damageAmount + 1} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param enchantments Enchantments that a tool should have.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, Collection<Enchantment> enchantments, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, enchantments.stream().collect(Collectors.toMap(x -> x, x -> 1)), damageAmount, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has all the specified {@code enchantments} (or better ones).</li>
     *     <li>Has at least {@code damageAmount + 1} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param enchantments Map of enchantments and their levels that a tool should have.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, Map<Enchantment, Integer> enchantments, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, enchantments, damageAmount > 0 ? (damageAmount + 1) : 0, damageAmount, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has all the specified {@code enchantments}.</li>
     *     <li>Has at least {@code minRemainingDurability} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param enchantments Enchantments that a tool should have.
     * @param minRemainingDurability Minimum remaining durability amount of a tool.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, Collection<Enchantment> enchantments, int minRemainingDurability, int damageAmount, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasToolFor(block, enchantments.stream().collect(Collectors.toMap(x -> x, x -> 1)), minRemainingDurability, damageAmount, failureReason);
    }

    /**
     * Returns a validator that passes if an inventory contains a tool that:
     *
     * <ul>
     *     <li>Is able to mine the given {@code block}.</li>
     *     <li>Has all the specified {@code enchantments} (or better ones).</li>
     *     <li>Has at least {@code minRemainingDurability} durability left.</li>
     * </ul>
     *
     * Found tool will be damaged by {@code damageAmount}.
     *
     * @param block Block that a tool should be able to mine.
     * @param enchantments Map of enchantments and their levels that a tool should have.
     * @param minRemainingDurability Minimum remaining durability amount of a tool.
     * @param damageAmount Damage amount that should be applied to the found tool.
     * @param failureReason Failure reason returned by the validator in case a tool was not found in the inventory.
     * @return Validator that passes if a tool was found in the provided inventory.
     */
    public static InventoryViewer.Validator hasToolFor(Block block, Map<Enchantment, Integer> enchantments, int minRemainingDurability, int damageAmount, @Nullable FailureReason failureReason) {
        BlockState blockState = block.getDefaultState();
        BiPredicate<ItemStack, Inventory> hasToolPredicate = (x, i) -> x.isSuitableFor(blockState);

        if (minRemainingDurability > 0) {
            hasToolPredicate = hasToolPredicate.and((x, i) -> x.getMaxDamage() - x.getDamage() >= minRemainingDurability);
        }

        for (Map.Entry<Enchantment, Integer> enchantmentEntry : enchantments.entrySet()) {
            Enchantment enchantment = enchantmentEntry.getKey();
            int level = enchantmentEntry.getValue();
            hasToolPredicate = hasToolPredicate.and((x, i) -> EnchantmentHelper.getLevel(enchantment, x) >= level);
        }

        BiFunction<ItemStack, Inventory, ItemStack> itemStackModifier = null;
        if (damageAmount > 0) {
            itemStackModifier = (x, i) -> {
                LivingEntity miner = i instanceof PlayerInventory ? ((PlayerInventory)i).player : null;
                if (miner == null) {
                    miner = EntityUtil.getFakeServerLivingEntity();
                }
                x.damage(damageAmount, miner, e -> {
                    if (EntityUtil.isFakeEntity(e)) {
                        return;
                    }

                    EquipmentSlot slot = null;
                    for (EquipmentSlot testSlot : EquipmentSlot.values()) {
                        if (e.getEquippedStack(testSlot) == x) {
                            slot = testSlot;
                            break;
                        }
                    }

                    if (slot != null) {
                        e.sendEquipmentBreakStatus(slot);
                    }
                });
                return x;
            };
        }

        return InventoryValidators.hasItemStack(hasToolPredicate, itemStackModifier, failureReason);
    }


    /**
     * Returns a validator that passes when an inventory contains {@link ItemStack} that passes the provided {@code itemStackPredicate}.
     * @param itemStackPredicate Predicate to search {@link ItemStack}.
     * @param failureReason Failure reason returned by the validator in case {@link ItemStack} was not found in the inventory.
     * @return Validator that passes when an inventory contains {@link ItemStack} that passes the provided {@code itemStackPredicate}.
     */
    public static InventoryViewer.Validator hasItemStack(BiPredicate<ItemStack, Inventory> itemStackPredicate, @Nullable FailureReason failureReason) {
        return InventoryValidators.hasItemStack(itemStackPredicate, null, failureReason);
    }

    /**
     * Returns a validator that passes when an inventory contains {@link ItemStack} that passes the provided {@code itemStackPredicate}.
     * @param itemStackPredicate Predicate to search {@link ItemStack}.
     * @param itemStackModifier Delegate that modifies found {@link ItemStack}.
     * @param failureReason Failure reason returned by the validator in case {@link ItemStack} was not found in the inventory.
     * @return Validator that passes when an inventory contains {@link ItemStack} that passes the provided {@code itemStackPredicate}.
     */
    public static InventoryViewer.Validator hasItemStack(BiPredicate<ItemStack, Inventory> itemStackPredicate, @Nullable BiFunction<ItemStack, Inventory, ItemStack> itemStackModifier, @Nullable FailureReason failureReason) {
        if (failureReason == null) {
            failureReason = FailureReason.OTHER_PROBLEM;
        }

        FailureReason finalFailureReason = failureReason;
        return (InventoryViewer.ExtendedValidator)((__, ___, ____, inventory) -> {
            for (int i = 0; i < inventory.size(); ++i) {
                ItemStack stack = inventory.getStack(i);
                if (itemStackPredicate.test(stack, inventory)) {
                    if (itemStackModifier != null) {
                        stack = itemStackModifier.apply(stack, inventory);
                        inventory.setStack(i, stack);
                    }
                    return null;
                }
            }
            return finalFailureReason;
        });
    }


    private InventoryValidators() { }
}
