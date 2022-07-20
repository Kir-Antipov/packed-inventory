package dev.kir.packedinventory.compat.cloth;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.kir.packedinventory.PackedInventory;
import dev.kir.packedinventory.api.v1.config.GenericTooltipConfig;
import dev.kir.packedinventory.api.v1.config.GenericValidationConfig;
import dev.kir.packedinventory.api.v1.config.PackedInventoryConfigEntryHolder;
import dev.kir.packedinventory.config.PackedInventoryConfig;
import dev.kir.packedinventory.util.collection.DefaultedMap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Config(name = PackedInventory.MOD_ID)
public class PackedInventoryClothConfig implements PackedInventoryConfig, ConfigData {
    @ConfigEntry.Category("tooltips")
    private static final PackedInventoryClothConfig INSTANCE = AutoConfig.register(PackedInventoryClothConfig.class, GsonConfigSerializer::new).getConfig();

    @ConfigEntry.Category("tooltips")
    @ConfigEntry.Gui.CollapsibleObject
    protected GenericTooltipConfig defaultTooltipConfig = new GenericTooltipConfig();

    @ConfigEntry.Category("tooltips")
    @ConfigEntry.Gui.Excluded
    protected transient Map<Identifier, PackedInventoryConfigEntryHolder<?>> tooltipEntries = new LinkedHashMap<>();

    @ConfigEntry.Category("tooltips")
    @ConfigEntry.Gui.Excluded
    protected Map<String, JsonElement> tooltips = new LinkedHashMap<>();

    @ConfigEntry.Category("tooltips")
    protected transient DefaultedMap<Identifier, Object> tooltipData = DefaultedMap.wrap(new LinkedHashMap<>(), () -> new Identifier("item"), GenericTooltipConfig::new);

    @ConfigEntry.Category("validation")
    @ConfigEntry.Gui.CollapsibleObject
    protected GenericValidationConfig defaultValidationConfig = new GenericValidationConfig();

    @ConfigEntry.Category("validation")
    @ConfigEntry.Gui.Excluded
    protected transient Map<Identifier, PackedInventoryConfigEntryHolder<?>> validationEntries = new LinkedHashMap<>();

    @ConfigEntry.Category("validation")
    @ConfigEntry.Gui.Excluded
    protected Map<String, JsonElement> validation = new LinkedHashMap<>();

    @ConfigEntry.Category("validation")
    protected transient DefaultedMap<Identifier, Object> validationData = DefaultedMap.wrap(new LinkedHashMap<>(), () -> new Identifier("item"), GenericValidationConfig::new);

    @ConfigEntry.Category("validation")
    @ConfigEntry.Gui.Excluded
    private final transient Map<ConfigSection, ConfigSectionData> configSections = Map.of(
        ConfigSection.TOOLTIPS, new ConfigSectionData(() -> this.tooltips, () -> this.tooltipEntries, () -> this.tooltipData),
        ConfigSection.VALIDATION, new ConfigSectionData(() -> this.validation, () -> this.validationEntries, () -> this.validationData)
    );

    private PackedInventoryClothConfig() { }

    public static PackedInventoryConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public GenericValidationConfig getDefaultValidationConfig() {
        return this.defaultValidationConfig;
    }

    @Override
    public GenericTooltipConfig getDefaultTooltipConfig() {
        return this.defaultTooltipConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(Identifier id, ConfigSection section, Class<? extends T> entryClass) {
        ConfigSectionData configSectionData = this.configSections.get(section);
        if (configSectionData == null) {
            return null;
        }

        Object entry = configSectionData.getValues().get(id);
        if (entry == null && !configSectionData.getEntries().containsKey(id) && configSectionData.getSerializedValues().containsKey(id.toString())) {
            this.register(id, section, PackedInventoryConfigEntryHolder.gson(() -> Utils.constructUnsafely(entryClass)));
            entry = configSectionData.getValues().get(id);
        }

        if (entryClass.isInstance(entry)) {
            return (T)entry;
        }
        return null;
    }

    @Override
    public <T> void register(Identifier id, ConfigSection section, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        ConfigSectionData configSectionData = this.configSections.get(section);
        if (configSectionData == null) {
            return;
        }

        configSectionData.getEntries().put(id, configEntryHolder);
        JsonElement serializedValue = configSectionData.getSerializedValues().get(id.toString());
        if (serializedValue != null) {
            configEntryHolder.fromJson(serializedValue.toString());
        }
        configSectionData.getValues().put(id, configEntryHolder.getConfigEntry());
    }

    @SuppressWarnings("unchecked")
    private void onSave() {
        for (ConfigSection section : ConfigSection.values()) {
            ConfigSectionData configSectionData = this.configSections.get(section);
            if (configSectionData == null) {
                continue;
            }

            configSectionData.getSerializedValues().clear();
            for (Map.Entry<Identifier, Object> entry : configSectionData.getValues().entrySet()) {
                PackedInventoryConfigEntryHolder<Object> configHolder = (PackedInventoryConfigEntryHolder<Object>)configSectionData.getEntries().get(entry.getKey());
                if (configHolder == null && entry.getValue() != null) {
                    configHolder = PackedInventoryConfigEntryHolder.gson(entry::getValue);
                }
                if (configHolder == null) {
                    continue;
                }

                configHolder.setConfigEntry(entry.getValue());
                configSectionData.getSerializedValues().put(entry.getKey().toString(), new JsonParser().parse(configHolder.toJson()));
            }
        }
    }

    private static class ConfigSectionData {
        private final Supplier<Map<String, JsonElement>> serializedValuesSupplier;
        private final Supplier<Map<Identifier, PackedInventoryConfigEntryHolder<?>>> entriesSupplier;
        private final Supplier<DefaultedMap<Identifier, Object>> valuesSupplier;

        public ConfigSectionData(Supplier<Map<String, JsonElement>> serializedValuesSupplier, Supplier<Map<Identifier, PackedInventoryConfigEntryHolder<?>>> entriesSupplier, Supplier<DefaultedMap<Identifier, Object>> valuesSupplier) {
            this.serializedValuesSupplier = serializedValuesSupplier;
            this.entriesSupplier = entriesSupplier;
            this.valuesSupplier = valuesSupplier;
        }

        public Map<String, JsonElement> getSerializedValues() {
            return this.serializedValuesSupplier.get();
        }

        public Map<Identifier, PackedInventoryConfigEntryHolder<?>> getEntries() {
            return this.entriesSupplier.get();
        }

        public DefaultedMap<Identifier, Object> getValues() {
            return this.valuesSupplier.get();
        }
    }

    static {
        AutoConfig.getConfigHolder(PackedInventoryClothConfig.class).registerSaveListener((h, c) -> { c.onSave(); return ActionResult.PASS; });
    }
}
