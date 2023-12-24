package dev.kir.packedinventory.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.kir.packedinventory.compat.cloth.PackedInventoryClothConfig;
import dev.kir.packedinventory.util.OptionalDyeColor;
import dev.kir.packedinventory.util.collection.DefaultedMap;
import dev.kir.packedinventory.util.collection.KeyValuePair;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked", "UnstableApiUsage"})
@Environment(EnvType.CLIENT)
public final class ModMenuCompat implements ModMenuApi {
    private static final boolean IS_CLOTH_LOADED = FabricLoader.getInstance().isModLoaded("cloth-config");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!IS_CLOTH_LOADED) {
            return parent -> null;
        }

        return parent -> AutoConfig.getConfigScreen(PackedInventoryClothConfig.class, parent).get();
    }

    private static void registerGuiProviders() {
        if (!IS_CLOTH_LOADED) {
            return;
        }

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        GuiRegistry registry = AutoConfig.getGuiRegistry(PackedInventoryClothConfig.class);

        registry.registerPredicateProvider((i18n, field, config, defaults, registry1) -> Collections.emptyList(), field -> Modifier.isStatic(field.getModifiers()));

        registry.registerTypeProvider(ModMenuCompat::getChildren, Object.class);

        registry.registerAnnotationProvider(
            ModMenuCompat::getChildren,
            field -> !field.getType().isPrimitive(),
            ConfigEntry.Gui.TransitiveObject.class
        );

        registry.registerAnnotationProvider(
            (i18n, field, config, defaults, guiProvider) -> Collections.singletonList(
                entryBuilder.startSubCategory(Text.translatable(i18n), getChildren(i18n, field, config, defaults, guiProvider))
                    .setExpanded(field.getAnnotation(ConfigEntry.Gui.CollapsibleObject.class).startExpanded())
                    .build()
            ),
            field -> !field.getType().isPrimitive(),
            ConfigEntry.Gui.CollapsibleObject.class
        );

        registry.registerTypeProvider(
            (i18n, field, config, defaults, registry1) -> Collections.singletonList(
                entryBuilder
                    .startTextField(Text.translatable(i18n), Utils.getUnsafely(field, config).toString())
                    .setErrorSupplier(s -> Optional.ofNullable(Identifier.tryParse(s) == null ? Text.translatable("text.cloth-config.error.not_valid_identifier") : null))
                    .setDefaultValue(() -> Utils.getUnsafely(field, config).toString())
                    .setSaveConsumer(s -> Utils.setUnsafely(field, config, Identifier.tryParse(s)))
                    .build()
            ),
            Identifier.class
        );

        registry.registerTypeProvider((i18n, field, config, defaults, registry1) -> {
            DefaultedMap<Object, Object> map = Utils.getUnsafely(field, config);
            String classI13n = String.format("%s.%s", i18n, "entry");

            return Collections.singletonList(
                    new NestedListListEntry<Object, MultiElementListEntry<Object>>(
                        Text.translatable(i18n),
                        map.entrySet().stream().map(x -> KeyValuePair.of(x.getKey(), x.getValue())).collect(Collectors.toCollection(ArrayList::new)),
                        false,
                        null,
                        newValue -> {
                            Map<Object, Object> newMap = ((List<KeyValuePair<Object, Object>>)(Object)newValue).stream().collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue, (a, b) -> b, LinkedHashMap::new));
                            Utils.setUnsafely(field, config, DefaultedMap.wrap(newMap, map.getDefaultEntrySupplier()));
                        },
                        () -> Utils.<DefaultedMap<Object, Object>>getUnsafely(field, defaults).entrySet().stream().map(x -> KeyValuePair.of(x.getKey(), x.getValue())).collect(Collectors.toCollection(ArrayList::new)),
                        entryBuilder.getResetButtonKey(),
                        true,
                        false,
                        (elem, nestedListListEntry) -> {
                            if (elem == null) {
                                elem = map.getDefaultEntry();
                            }
                            Map.Entry<Object, Object> defaultValue = KeyValuePair.of(((Map.Entry<Object, Object>)elem).getKey(), Utils.constructUnsafely(((Map.Entry<Object, Object>)elem).getValue().getClass()));
                            return new MultiElementListEntry<>(Text.translatable(classI13n), elem, (List)getChildren(classI13n, elem, defaultValue, registry1), true);
                        }
                    )
            );
        }, DefaultedMap.class);

        registry.registerTypeProvider(
            (i18n, field, config, defaults, guiProvider) -> {
                OptionalDyeColor[] enums = OptionalDyeColor.values();
                return Collections.singletonList(
                    entryBuilder.startSelector(
                        Text.translatable(i18n),
                        enums,
                        OptionalDyeColor.of(Utils.getUnsafely(field, config, Utils.getUnsafely(field, defaults)))
                    )
                    .setDefaultValue(() -> OptionalDyeColor.of(Utils.getUnsafely(field, defaults)))
                    .setSaveConsumer(newValue -> Utils.setUnsafely(field, config, newValue.getColor()))
                    .build()
                );
            },
            DyeColor.class
        );

        registry.registerPredicateProvider(
            (i18n, field, config, defaults, guiProvider) -> {
                List<OptionalDyeColor> enums = Arrays.asList(OptionalDyeColor.values());
                return Collections.singletonList(
                        entryBuilder.startDropdownMenu(
                            Text.translatable(i18n),
                            DropdownMenuBuilder.TopCellElementBuilder.of(
                                OptionalDyeColor.of(Utils.getUnsafely(field, config, Utils.getUnsafely(field, defaults))),
                                str -> {
                                    String s = Text.of(str).getString();
                                    for (OptionalDyeColor constant : enums) {
                                        if (constant.toString().equals(s)) {
                                            return constant;
                                        }
                                    }
                                    return null;
                                },
                                v -> Text.of(v.toString())
                            ),
                            DropdownMenuBuilder.CellCreatorBuilder.of(v -> Text.of(v.toString()))
                        )
                        .setSelections(enums)
                        .setDefaultValue(() -> OptionalDyeColor.of(Utils.getUnsafely(field, defaults)))
                        .setSaveConsumer(newValue -> Utils.setUnsafely(field, config, newValue.getColor()))
                        .build()
                );
            },
            field -> field.getType() == DyeColor.class && field.isAnnotationPresent(ConfigEntry.Gui.EnumHandler.class) && field.getAnnotation(ConfigEntry.Gui.EnumHandler.class).option() == ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN
        );
    }

    private static List<AbstractConfigListEntry> getChildren(String i18n, Field field, Object config, Object defaults, GuiRegistryAccess guiProvider) {
        return getChildren(i18n, Utils.getUnsafely(field, config), Utils.getUnsafely(field, defaults), guiProvider);
    }

    private static List<AbstractConfigListEntry> getChildren(String i18n, Object iConfig, Object iDefaults, GuiRegistryAccess guiProvider) {
        List<Field> fields = new ArrayList<>();
        Deque<Class<?>> classes = new ArrayDeque<>();
        Class<?> currentClass = iConfig.getClass();
        while (currentClass != null && currentClass != Object.class) {
            classes.push(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        for (Class<?> cls : classes) {
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
        }

        return fields.stream()
                .map(field -> {
                    String iI13n = String.format("%s.%s", i18n, field.getName());
                    return guiProvider.getAndTransform(iI13n, field, iConfig, iDefaults, guiProvider);
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    static {
        registerGuiProviders();
    }
}
