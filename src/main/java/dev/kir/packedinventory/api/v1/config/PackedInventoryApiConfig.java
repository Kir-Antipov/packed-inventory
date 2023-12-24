package dev.kir.packedinventory.api.v1.config;

import dev.kir.packedinventory.PackedInventory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Shared Packed Inventory config that may (and should) be used by other mods.
 */
public interface PackedInventoryApiConfig {
    /**
     * @return {@link PackedInventoryApiConfig} instance.
     */
    static PackedInventoryApiConfig getInstance() {
        return PackedInventory.getConfig();
    }

    /**
     * @return Default validation config.
     */
    GenericValidationConfig getDefaultValidationConfig();

    /**
     * Retrieves an entry of the given class with the id of the provided item from the validation config section, if any; otherwise {@code null}.
     * @param item Item associated with the entry.
     * @param validationConfigClass Entry type.
     * @param <T> Entry type.
     * @return Entry of the given class with the id of the provided item from the validation config section, if any; otherwise {@code null}.
     */
    default <T> @Nullable T getValidationConfig(Item item, Class<? extends T> validationConfigClass) {
        return this.getValidationConfig(Registries.ITEM.getId(item), validationConfigClass);
    }

    /**
     * Retrieves an entry of the same class as defaultValidationConfig's one with the id of the provided item from the validation config section, if any; otherwise defaultValidationConfig.
     * @param item Item associated with the entry.
     * @param defaultValidationConfig Default validation config.
     * @param <T> Entry type.
     * @return Entry of the same class as defaultValidationConfig's one with the id of the provided item from the validation config section, if any; otherwise defaultValidationConfig.
     */
    default <T> T getValidationConfigOrDefault(Item item, T defaultValidationConfig) {
        return this.getValidationConfigOrDefault(Registries.ITEM.getId(item), defaultValidationConfig);
    }

    /**
     * Retrieves an entry of {@link GenericValidationConfig} class with the id of the provided item from the validation config section, if any; otherwise {@link this#getDefaultValidationConfig()}.
     * @param item Item associated with the entry.
     * @return Entry of {@link GenericValidationConfig} class with the id of the provided item from the validation config section, if any; otherwise {@link this#getDefaultValidationConfig()}.
     */
    default GenericValidationConfig getValidationConfigOrDefault(Item item) {
        return this.getValidationConfigOrDefault(Registries.ITEM.getId(item), this.getDefaultValidationConfig());
    }

    /**
     * Retrieves an entry of the given class with the provided id from the validation config section, if any; otherwise {@code null}.
     * @param id Entry id.
     * @param validationConfigClass Entry type.
     * @param <T> Entry type.
     * @return Entry of the given class with the provided id from the validation config section, if any; otherwise {@code null}.
     */
    default <T> @Nullable T getValidationConfig(Identifier id, Class<? extends T> validationConfigClass) {
        return this.get(id, ConfigSection.VALIDATION, validationConfigClass);
    }

    /**
     * Retrieves an entry of the same class as defaultValidationConfig's one with the provided id from the validation config section, if any; otherwise defaultValidationConfig.
     * @param id Entry id.
     * @param defaultValidationConfig Default validation config.
     * @param <T> Entry type.
     * @return Entry of the same class as defaultValidationConfig's one with the provided id from the validation config section, if any; otherwise defaultValidationConfig.
     */
    default <T> T getValidationConfigOrDefault(Identifier id, T defaultValidationConfig) {
        return this.getOrDefault(id, ConfigSection.VALIDATION, defaultValidationConfig);
    }

    /**
     * Retrieves an entry of {@link GenericValidationConfig} class with the provided id from the validation config section, if any; otherwise {@link this#getDefaultValidationConfig()}.
     * @param id Entry id.
     * @return Entry of {@link GenericValidationConfig} class with the provided id from the validation config section, if any; otherwise {@link this#getDefaultValidationConfig()}.
     */
    default GenericValidationConfig getValidationConfigOrDefault(Identifier id) {
        return this.getOrDefault(id, ConfigSection.VALIDATION, this.getDefaultValidationConfig());
    }

    /**
     * @return Default tooltip config.
     */
    GenericTooltipConfig getDefaultTooltipConfig();

    /**
     * Retrieves an entry of the given class with the id of the provided item from the tooltip config section, if any; otherwise {@code null}.
     * @param item Item associated with the entry.
     * @param tooltipConfigClass Entry type.
     * @param <T> Entry type.
     * @return Entry of the given class with the id of the provided item from the tooltip config section, if any; otherwise {@code null}.
     */
    default <T> @Nullable T getTooltipConfig(Item item, Class<? extends T> tooltipConfigClass) {
        return this.getTooltipConfig(Registries.ITEM.getId(item), tooltipConfigClass);
    }

    /**
     * Retrieves an entry of the same class as defaultTooltipConfig's one with the id of the provided item from the tooltip config section, if any; otherwise defaultTooltipConfig.
     * @param item Item associated with the entry.
     * @param defaultTooltipConfig Default tooltip config.
     * @param <T> Entry type.
     * @return Entry of the same class as defaultTooltipConfig's one with the id of the provided item from the tooltip config section, if any; otherwise defaultTooltipConfig.
     */
    default <T> T getTooltipConfigOrDefault(Item item, T defaultTooltipConfig) {
        return this.getTooltipConfigOrDefault(Registries.ITEM.getId(item), defaultTooltipConfig);
    }

    /**
     * Retrieves an entry of {@link GenericTooltipConfig} class with the id of the provided item from the tooltip config section, if any; otherwise {@link this#getDefaultTooltipConfig()}.
     * @param item Item associated with the entry.
     * @return Entry of {@link GenericTooltipConfig} class with the id of the provided item from the tooltip config section, if any; otherwise {@link this#getDefaultTooltipConfig()}.
     */
    default GenericTooltipConfig getTooltipConfigOrDefault(Item item) {
        return this.getTooltipConfigOrDefault(Registries.ITEM.getId(item), this.getDefaultTooltipConfig());
    }

    /**
     * Retrieves an entry of the given class with the provided id from the tooltip config section, if any; otherwise {@code null}.
     * @param id Entry id.
     * @param tooltipConfigClass Entry type.
     * @param <T> Entry type.
     * @return Entry of the given class with the provided id from the tooltip config section, if any; otherwise {@code null}.
     */
    default <T> @Nullable T getTooltipConfig(Identifier id, Class<? extends T> tooltipConfigClass) {
        return this.get(id, ConfigSection.TOOLTIPS, tooltipConfigClass);
    }

    /**
     * Retrieves an entry of the same class as defaultTooltipConfig's one with the provided id from the tooltip config section, if any; otherwise defaultTooltipConfig.
     * @param id Entry id.
     * @param defaultTooltipConfig Default tooltip config.
     * @param <T> Entry type.
     * @return Entry of the same class as defaultTooltipConfig's one with the provided id from the tooltip config section, if any; otherwise defaultTooltipConfig.
     */
    default <T> T getTooltipConfigOrDefault(Identifier id, T defaultTooltipConfig) {
        return this.getOrDefault(id, ConfigSection.TOOLTIPS, defaultTooltipConfig);
    }

    /**
     * Retrieves an entry of {@link GenericTooltipConfig} class with the provided id from the tooltip config section, if any; otherwise {@link this#getDefaultTooltipConfig()}.
     * @param id Entry id.
     * @return Entry of {@link GenericTooltipConfig} class with the provided id from the tooltip config section, if any; otherwise {@link this#getDefaultTooltipConfig()}.
     */
    default GenericTooltipConfig getTooltipConfigOrDefault(Identifier id) {
        return this.getOrDefault(id, ConfigSection.TOOLTIPS, this.getDefaultTooltipConfig());
    }

    /**
     * Retrieves an entry of the same class as defaultValue's one with the id of the provided item from the specified config section, if any; otherwise defaultValue.
     * @param item Item associated with the entry.
     * @param section Target config section.
     * @param defaultValue Default value.
     * @param <T> Entry type.
     * @return Entry of the same class as defaultValue's one with the id of the provided item from the specified config section, if any; otherwise defaultValue.
     */
    default <T> T getOrDefault(Item item, ConfigSection section, T defaultValue) {
        return this.getOrDefault(Registries.ITEM.getId(item), section, defaultValue);
    }

    /**
     * Retrieves an entry of the same class as defaultValue's one with the provided id from the specified config section, if any; otherwise defaultValue.
     * @param id Entry id.
     * @param section Target config section.
     * @param defaultValue Default value.
     * @param <T> Entry type.
     * @return Entry of the same class as defaultValue's one with the provided id from the specified config section, if any; otherwise defaultValue.
     */
    @SuppressWarnings("unchecked")
    default <T> T getOrDefault(Identifier id, ConfigSection section, T defaultValue) {
        T entry = this.get(id, section, (Class<? extends T>)defaultValue.getClass());
        return entry == null ? defaultValue : entry;
    }

    /**
     * Retrieves an entry of the given class with the id of the provided item from the specified config section, if any; otherwise {@code null}.
     * @param item Item associated with the entry.
     * @param section Target config section.
     * @param entryClass Entry type.
     * @param <T> Entry type.
     * @return Entry of the given class with the id of the provided item from the specified config section, if any; otherwise {@code null}.
     */
    default <T> @Nullable T get(Item item, ConfigSection section, Class<? extends T> entryClass) {
        return this.get(Registries.ITEM.getId(item), section, entryClass);
    }

    /**
     * Retrieves an entry of the given class with the provided id from the specified config section, if any; otherwise {@code null}.
     * @param id Entry id.
     * @param section Target config section.
     * @param entryClass Entry type.
     * @param <T> Entry type.
     * @return Entry of the given class with the provided id from the specified config section, if any; otherwise {@code null}.
     */
    <T> @Nullable T get(Identifier id, ConfigSection section, Class<? extends T> entryClass);


    /**
     * Registers an entry within the validation config section with the id of the given item.
     *
     * @param item Item associated with the entry.
     * @param configEntrySupplier Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerValidationConfig(Item item, Supplier<T> configEntrySupplier) {
        this.register(item, ConfigSection.VALIDATION, configEntrySupplier);
    }

    /**
     * Registers an entry within the validation config section with the id of the given item.
     *
     * @param item Item associated with the entry.
     * @param configEntryHolder Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerValidationConfig(Item item, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        this.register(item, ConfigSection.VALIDATION, configEntryHolder);
    }

    /**
     * Registers an entry within the validation config section with the given id.
     *
     * @param id Entry id.
     * @param configEntrySupplier Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerValidationConfig(Identifier id, Supplier<T> configEntrySupplier) {
        this.register(id, ConfigSection.VALIDATION, configEntrySupplier);
    }

    /**
     * Registers an entry within the validation config section with the given id.
     *
     * @param id Entry id.
     * @param configEntryHolder Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerValidationConfig(Identifier id, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        this.register(id, ConfigSection.VALIDATION, configEntryHolder);
    }


    /**
     * Registers an entry within the tooltip config section with the id of the given item.
     *
     * @param item Item associated with the entry.
     * @param configEntrySupplier Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerTooltipConfig(Item item, Supplier<T> configEntrySupplier) {
        this.register(item, ConfigSection.TOOLTIPS, configEntrySupplier);
    }

    /**
     * Registers an entry within the tooltip config section with the id of the given item.
     *
     * @param item Item associated with the entry.
     * @param configEntryHolder Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerTooltipConfig(Item item, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        this.register(item, ConfigSection.TOOLTIPS, configEntryHolder);
    }

    /**
     * Registers an entry within the tooltip config section with the given id.
     *
     * @param id Entry id.
     * @param configEntrySupplier Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerTooltipConfig(Identifier id, Supplier<T> configEntrySupplier) {
        this.register(id, ConfigSection.TOOLTIPS, configEntrySupplier);
    }

    /**
     * Registers an entry within the tooltip config section with the given id.
     *
     * @param id Entry id.
     * @param configEntryHolder Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void registerTooltipConfig(Identifier id, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        this.register(id, ConfigSection.TOOLTIPS, configEntryHolder);
    }


    /**
     * Registers an entry within the target config section with the id of the given item.
     *
     * @param item Item associated with the entry.
     * @param section Target config section.
     * @param configEntrySupplier Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void register(Item item, ConfigSection section, Supplier<T> configEntrySupplier) {
        this.register(item, section, PackedInventoryConfigEntryHolder.gson(configEntrySupplier));
    }

    /**
     * Registers an entry within the target config section with the id of the given item.
     *
     * @param item Item associated with the entry.
     * @param section Target config section.
     * @param configEntryHolder Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void register(Item item, ConfigSection section, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        this.register(Registries.ITEM.getId(item), section, configEntryHolder);
    }

    /**
     * Registers an entry within the target config section with the given id.
     *
     * @param id Entry id.
     * @param section Target config section.
     * @param configEntrySupplier Entry to be registered.
     * @param <T> Entry type.
     */
    default <T> void register(Identifier id, ConfigSection section, Supplier<T> configEntrySupplier) {
        this.register(id, section, PackedInventoryConfigEntryHolder.gson(configEntrySupplier));
    }

    /**
     * Registers an entry within the target config section with the given id.
     *
     * @param id Entry id.
     * @param section Target config section.
     * @param configEntryHolder Entry to be registered.
     * @param <T> Entry type.
     */
    <T> void register(Identifier id, ConfigSection section, PackedInventoryConfigEntryHolder<T> configEntryHolder);


    /**
     * Describes predefined config sections.
     */
    enum ConfigSection {
        /**
         * Validation config section.
         */
        VALIDATION,

        /**
         * Tooltip config section.
         */
        TOOLTIPS
    }
}
