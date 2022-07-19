package dev.kir.packedinventory.api.v1.config;

/**
 * Base class for validation rules.
 */
public class ValidationConfig {
    /**
     * Default instance of {@link ValidationConfig}.
     */
    public static final ValidationConfig DEFAULT = new ValidationConfig();

    /**
     * Indicates whether an action associated with this validation rule should be allowed at all or not.
     */
    protected boolean enable;

    /**
     * Constructs default {@link ValidationConfig} instance.
     */
    public ValidationConfig() {
        this(true);
    }

    /**
     * Constructs new {@link ValidationConfig} instance.
     *
     * @param enabled Indicates whether an action associated with this validation rule should be allowed at all or not.
     */
    public ValidationConfig(boolean enabled) {
        this.enable = enabled;
    }

    /**
     * @return Flag that indicates whether an action associated with this validation rule should be allowed at all or not.
     */
    public boolean isEnabled() {
        return this.enable;
    }
}
