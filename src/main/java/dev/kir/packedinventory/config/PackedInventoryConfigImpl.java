package dev.kir.packedinventory.config;

import dev.kir.packedinventory.api.v1.config.GenericTooltipConfig;
import dev.kir.packedinventory.api.v1.config.GenericValidationConfig;
import dev.kir.packedinventory.api.v1.config.PackedInventoryConfigEntryHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class PackedInventoryConfigImpl implements PackedInventoryConfig {
    private static final PackedInventoryConfigImpl INSTANCE = new PackedInventoryConfigImpl();

    private final GenericValidationConfig defaultValidationConfig = new GenericValidationConfig();
    private final GenericTooltipConfig defaultTooltipConfig = new GenericTooltipConfig();
    private final Map<ConfigSection, Map<Identifier, PackedInventoryConfigEntryHolder<?>>> sections = new HashMap<>();

    private PackedInventoryConfigImpl() { }

    public static PackedInventoryConfigImpl getInstance() {
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
        Map<Identifier, PackedInventoryConfigEntryHolder<?>> sectionMap = this.sections.get(section);
        if (sectionMap == null) {
            return null;
        }

        PackedInventoryConfigEntryHolder<?> holder = sectionMap.get(id);
        if (holder == null) {
            return null;
        }

        Object entry = holder.getConfigEntry();
        if (entryClass.isInstance(entry)) {
            return (T)entry;
        }
        return null;
    }

    @Override
    public <T> void register(Identifier id, ConfigSection section, PackedInventoryConfigEntryHolder<T> configEntryHolder) {
        this.sections.computeIfAbsent(section, s -> new HashMap<>()).put(id, configEntryHolder);
    }
}
