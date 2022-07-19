package dev.kir.packedinventory.api.v1.config;

/**
 * A {@link ValidationConfig} extension that should suit most validation rules.
 */
public class GenericValidationConfig extends ValidationConfig {
    /**
     * Default instance of {@link GenericValidationConfig}.
     */
    public static final GenericValidationConfig DEFAULT = new GenericValidationConfig();

    /**
     * Indicates whether validation should be suppressed for creative players.
     */
    protected boolean suppressValidationInCreative;
    /**
     * Indicates whether a player should be on the ground in order to proceed.
     */
    protected boolean requiresPlayerOnGround;

    /**
     * Constructs default {@link GenericValidationConfig} instance.
     */
    public GenericValidationConfig() {
        this(true, true, true);
    }

    /**
     * Constructs new {@link GenericValidationConfig} instance.
     *
     * @param enabled Determines whether an action associated with this validation rule should be allowed at all or not.
     * @param suppressValidationInCreative Indicates whether validation should be suppressed for creative players.
     * @param requiresPlayerOnGround Indicates whether a player should be on the ground in order to proceed.
     */
    public GenericValidationConfig(boolean enabled, boolean suppressValidationInCreative, boolean requiresPlayerOnGround) {
        super(enabled);
        this.suppressValidationInCreative = suppressValidationInCreative;
        this.requiresPlayerOnGround = requiresPlayerOnGround;
    }

    /**
     * @return Flag that indicates whether validation should be suppressed for creative players.
     */
    public boolean isSuppressedInCreative() {
        return this.suppressValidationInCreative;
    }

    /**
     * @return Flag that indicates whether a player should be on the ground in order to proceed.
     */
    public boolean requiresPlayerOnGround() {
        return this.requiresPlayerOnGround;
    }
}
