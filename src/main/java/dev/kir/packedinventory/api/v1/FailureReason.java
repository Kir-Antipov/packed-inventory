package dev.kir.packedinventory.api.v1;

import dev.kir.packedinventory.PackedInventory;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Describes a failure reason of some operation.
 */
@FunctionalInterface
public interface FailureReason {
    /**
     * Generic {@link FailureReason} with no description.
     */
    FailureReason OTHER_PROBLEM = () -> null;
    /**
     * The item stack should consist of only one item in order to proceed.
     */
    FailureReason STACK_CONSISTS_OF_MORE_THAN_ONE_ITEM = FailureReason.translate("event." + PackedInventory.MOD_ID + ".failure.stack_consists_of_more_than_one_item");
    /**
     * Player should be on the ground in order to proceed.
     */
    FailureReason PLAYER_IS_NOT_ON_THE_GROUND = FailureReason.translate("event." + PackedInventory.MOD_ID + ".failure.player_is_not_on_the_ground");
    /**
     * Player should be in creative mode in order to proceed.
     */
    FailureReason PLAYER_IS_NOT_IN_THE_CREATIVE_MODE = FailureReason.translate("event." + PackedInventory.MOD_ID + ".failure.player_is_not_in_the_creative_mode");
    /**
     * Player needs a pickaxe enchanted with silk touch enchantment in order to proceed.
     */
    FailureReason PLAYER_NEEDS_PICKAXE_ENCHANTED_WITH_SILK_TOUCH_ENCHANTMENT = FailureReason.translate("event." + PackedInventory.MOD_ID + ".failure.player_needs_pickaxe_enchanted_with_silk_touch_enchantment");

    /**
     * @return Text that explains the cause of the failure.
     */
    @Nullable Text toText();

    /**
     * Creates new {@link FailureReason} with the given description.
     * @param description Failure description.
     * @return New {@link FailureReason} instance that uses the given description.
     */
    static FailureReason create(@Nullable Text description) {
        return description == null ? OTHER_PROBLEM : () -> description;
    }

    /**
     * Creates new {@link FailureReason} with the given literal description.
     * @param text Failure description.
     * @return New {@link FailureReason} instance that uses the given literal description.
     */
    static FailureReason literal(String text) {
        return FailureReason.create(Text.of(text));
    }

    /**
     * Creates new {@link FailureReason} with the given translatable description.
     * @param key Failure description's translation key.
     * @return New {@link FailureReason} instance that uses the given translatable description.
     */
    static FailureReason translate(String key) {
        return FailureReason.create(Text.translatable(key));
    }
}
