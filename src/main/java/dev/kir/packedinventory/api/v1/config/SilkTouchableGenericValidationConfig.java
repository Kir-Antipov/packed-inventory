package dev.kir.packedinventory.api.v1.config;

/**
 * A {@link GenericValidationConfig} extension that contains properties specific for silk-touchable objects.
 */
public class SilkTouchableGenericValidationConfig extends GenericValidationConfig {
    /**
     * Default instance of {@link SilkTouchableGenericValidationConfig}.
     */
    public static final SilkTouchableGenericValidationConfig DEFAULT = new SilkTouchableGenericValidationConfig();

    /**
     * Indicates whether a player should have a tool enchanted with silk touch enchantment in order to proceed.
     */
    protected boolean requiresSilkTouch;

    /**
     * Constructs default {@link SilkTouchableGenericValidationConfig} instance.
     */
    public SilkTouchableGenericValidationConfig() {
        this(true, true, true, true);
    }

    /**
     * Constructs new {@link SilkTouchableGenericValidationConfig} instance.
     *
     * @param enabled Determines whether an action associated with this validation rule should be allowed at all or not.
     * @param suppressValidationInCreative Indicates whether validation should be suppressed for creative players.
     * @param requiresPlayerOnGround Indicates whether a player should be on the ground in order to proceed.
     * @param requiresSilkTouch Indicates whether a player should have a tool enchanted with silk touch enchantment in order to proceed.
     */
    public SilkTouchableGenericValidationConfig(boolean enabled, boolean suppressValidationInCreative, boolean requiresPlayerOnGround, boolean requiresSilkTouch) {
        super(enabled, suppressValidationInCreative, requiresPlayerOnGround);
        this.requiresSilkTouch = requiresSilkTouch;
    }

    /**
     * @return Flag that indicates whether a player should have a tool enchanted with silk touch enchantment in order to proceed.
     */
    public boolean requiresSilkTouch() {
        return this.requiresSilkTouch;
    }
}
