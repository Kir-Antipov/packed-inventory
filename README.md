![Logo](media/logo.png)

# Packed Inventory
[![GitHub tag](https://img.shields.io/github/v/tag/Kir-Antipov/packed-inventory.svg?cacheSeconds=3600&sort=date)](https://github.com/Kir-Antipov/packed-inventory/releases/latest)
[![GitHub build status](https://img.shields.io/github/workflow/status/Kir-Antipov/packed-inventory/build-artifacts/1.17.x/dev?cacheSeconds=3600)](https://github.com/Kir-Antipov/packed-inventory/actions/workflows/build-artifacts.yml)
[![Modrinth](https://img.shields.io/badge/dynamic/json?color=5da545&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/packed-inventory&style=flat&cacheSeconds=3600&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMSAxMSIgd2lkdGg9IjE0LjY2NyIgaGVpZ2h0PSIxNC42NjciICB4bWxuczp2PSJodHRwczovL3ZlY3RhLmlvL25hbm8iPjxkZWZzPjxjbGlwUGF0aCBpZD0iQSI+PHBhdGggZD0iTTAgMGgxMXYxMUgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjxnIGNsaXAtcGF0aD0idXJsKCNBKSI+PHBhdGggZD0iTTEuMzA5IDcuODU3YTQuNjQgNC42NCAwIDAgMS0uNDYxLTEuMDYzSDBDLjU5MSA5LjIwNiAyLjc5NiAxMSA1LjQyMiAxMWMxLjk4MSAwIDMuNzIyLTEuMDIgNC43MTEtMi41NTZoMGwtLjc1LS4zNDVjLS44NTQgMS4yNjEtMi4zMSAyLjA5Mi0zLjk2MSAyLjA5MmE0Ljc4IDQuNzggMCAwIDEtMy4wMDUtMS4wNTVsMS44MDktMS40NzQuOTg0Ljg0NyAxLjkwNS0xLjAwM0w4LjE3NCA1LjgybC0uMzg0LS43ODYtMS4xMTYuNjM1LS41MTYuNjk0LS42MjYuMjM2LS44NzMtLjM4N2gwbC0uMjEzLS45MS4zNTUtLjU2Ljc4Ny0uMzcuODQ1LS45NTktLjcwMi0uNTEtMS44NzQuNzEzLTEuMzYyIDEuNjUxLjY0NSAxLjA5OC0xLjgzMSAxLjQ5MnptOS42MTQtMS40NEE1LjQ0IDUuNDQgMCAwIDAgMTEgNS41QzExIDIuNDY0IDguNTAxIDAgNS40MjIgMCAyLjc5NiAwIC41OTEgMS43OTQgMCA0LjIwNmguODQ4QzEuNDE5IDIuMjQ1IDMuMjUyLjgwOSA1LjQyMi44MDljMi42MjYgMCA0Ljc1OCAyLjEwMiA0Ljc1OCA0LjY5MSAwIC4xOS0uMDEyLjM3Ni0uMDM0LjU2bC43NzcuMzU3aDB6IiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGZpbGw9IiM1ZGE0MjYiLz48L2c+PC9zdmc+)](https://modrinth.com/mod/packed-inventory)
[![CurseForge](https://img.shields.io/badge/dynamic/json?color=%23f16436&label=CurseForge&query=title&url=https%3A%2F%2Fapi.cfwidget.com%2F641196)](https://www.curseforge.com/minecraft/mc-mods/packed-inventory)
[![GitHub license](https://img.shields.io/github/license/Kir-Antipov/packed-inventory.svg?cacheSeconds=36000)](https://github.com/Kir-Antipov/packed-inventory#readme)

A Minecraft mod that attempts to mitigate the ever-growing Inventory Problem by making it possible to manage the contents of shulker boxes, ender chests, etc. right inside your inventory.

Oh, also we have fancy tooltips for shulker boxes, ender chests, maps, and more!

----

## Features

 - Manage inventory of shulker boxes without ever placing them
 - Manage inventory of your ender chest without ever placing it
 - Vanilla-like tooltips for shulker boxes
 - Vanilla-like tooltips for ender chests
 - Vanilla-like tooltips for filled maps
 - Vanilla-like tooltips for NBT-containing items acquired in Creative via `Ctrl + MMB` *(e.g., chests, barrels, furnaces, hoppers, dispensers, droppers, etc.)*
 - An extensive and simple API that helps other modders implement these features for their blocks and items

Here are some examples for you:

 - Container tooltip
 - Colored container tooltip
 - Compact container tooltip
 - Filled map tooltip

![Preview](media/preview.png)

More screenshots can be seen [here](media/).

----

## Key Binds

Key binds can be configured just like vanilla ones:

 - Go to `Options...`
 - Select `Controls...`
 - Then choose `Key Binds...`
 - Scroll down to the `Packed Inventory` section

| Name | Description | Default |
| ---- | ----------- | ------- |
| `Open edit screen` | Opens the edit screen for the selected item *(the one located in the active hotbar slot, if the inventory screen is not open; otherwise, the one the mouse is hovering over)* | `k` |
| `Invert tooltip visibility (hold)` | Inverts tooltip visibility while pressed | `Left Shift` |
| `Invert tooltip compact mode (hold)` | Inverts tooltip compact mode while pressed | `c` |

It is ok for these key binds to interfere with others, because they are applied when your inventory screen is opened, where other key binds do not work.

----

## Config

If you have [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config) installed, you can customize the behavior of the mod. A config is usually located at `./config/packed-inventory.json` and by default looks like this:

```json
{
  "defaultTooltipConfig": {
    "showWhenEmpty": false,
    "rows": -1,
    "columns": -1,
    "usePredefinedColor": false,
    "enable": true,
    "compact": false
  },
  "tooltips": {
    "minecraft:shulker_box": {
      "showWhenEmpty": false,
      "rows": -1,
      "columns": -1,
      "usePredefinedColor": false,
      "enable": true,
      "compact": false
    },
    "minecraft:ender_chest": {
      "syncInterval": 5000,
      "showWhenEmpty": false,
      "rows": -1,
      "columns": -1,
      "usePredefinedColor": false,
      "enable": true,
      "compact": false
    },
    "minecraft:filled_map": {
      "size": 128,
      "enable": false,
      "compact": false
    }
  },
  "defaultValidationConfig": {
    "suppressValidationInCreative": true,
    "requiresPlayerOnGround": true,
    "enable": true
  },
  "validation": {
    "minecraft:shulker_box": {
      "suppressValidationInCreative": true,
      "requiresPlayerOnGround": true,
      "enable": true
    },
    "minecraft:ender_chest": {
      "requiresSilkTouch": true,
      "suppressValidationInCreative": true,
      "requiresPlayerOnGround": true,
      "enable": true
    }
  }
}
```

| Name | Description | Side | Default value |
| ---- | ----------- | ---- |------------- |
| `defaultTooltipConfig` | Default tooltip config | `client` | `N/A` |
| `tooltips` | All registered tooltip configs | `client` | `N/A` |
| `tooltip.enable` | Indicates whether a tooltip should be enabled by default or not | `client` | `true` |
| `tooltip.compact` | Indicates whether tooltip compact mode should be enabled by default or not | `client` | `false` |
| `tooltip.showWhenEmpty` | Indicates whether a tooltip should be shown when its content is empty or not | `client` | `false` |
| `tooltip.rows` | Specifies the number of rows used to display tooltip content | `client` | `-1` |
| `tooltip.columns` | Specifies the number of columns used to display tooltip content | `client` | `-1` |
| `tooltip.usePredefinedColor` | Indicates whether a tooltip should use item color or not | `client` | `false` |
| `tooltip.color` | Specifies default tooltip color in case `usePredefinedColor` is set to `false`, or item color cannot be automatically determined | `client` | `false` |
| `tooltip.syncInterval` | Determines how often synchronization should occur | `client` | `5000` |
| `tooltip.size` | Specifies size of a tooltip *(mostly used by the `filled_map` tooltip)* | `client` | `128` |
| `defaultValidationConfig` | Default validation config | `server` | `N/A` |
| `validation` | All registered validation configs | `server` | `N/A` |
| `validation.enable` | Indicates whether an action associated with this validation rule should be allowed at all or not | `server` | `true` |
| `validation.suppressValidationInCreative` | Indicates whether validation should be suppressed for creative players | `server` | `true` |
| `validation.requiresPlayerOnGround` | Indicates whether a player should be on the ground in order to proceed | `server` | `true` |
| `validation.requiresSilkTouch` | Indicates whether a player should have a tool enchanted with silk touch enchantment in order to proceed | `server` | `true` |

You can edit any of these values directly in the config file or via [ModMenu](https://www.curseforge.com/minecraft/mc-mods/modmenu).

----

## Development

### Adding the API to your buildscript

In order to use the API, you will need to add `Packed Inventory` as a dependency to your build script:

`build.gradle`:

```gradle
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:packed-inventory:${project.packed_inventory_version}"
}
```

`gradle.properties`:

```properties
packed_inventory_version=/* version (e.g., 0.1.0+1.18.2) */
```

You can find the current version of the API at the [Latest Release](https://github.com/Kir-Antipov/packed-inventory/releases/latest) page.

### Entrypoints

The API provides 2 new entrypoints that you can declare in your mod's metadata:

`fabric.mod.json`:
```json
{
  "entrypoints": {
    "packed-inventory": [
      "a.b.c.MyMod"
    ],
    "packed-inventory-client": [
      "a.b.c.MyClientMod"
    ]
  }
}
```

 - `packed-inventory` - will be run in any environment. Your initializer should implement `PackedInventoryInitializer`
 - `packed-inventory-client` - will be run second and only on the client side. Your initializer should implement `PackedInventoryClientInitializer`

### Inventory Views

Meet inventory views! An inventory view is an editable inventory representation of an item *(e.g., ender chest inventory for ender chest item, shulker box contents for shulker box item, etc.)*. This is the core concept that allows us to edit item inventories without ever interacting with their block forms.

Let's register a simple inventory viewer *(the thing that provides inventory views)* for the ender chest item:

```java
public class MyMod implements PackedInventoryInitializer {
    @Override
    public void registerInventoryViewers(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            // We need to "extract" an inventory from the given context.
            // In this case it's pretty easy, because ender chests have a shared inventory,
            // which can be retrieved from the player themself.
            (inventory, slot, player) -> player.getEnderChestInventory(),

            // This could be a predicate -
            // `(inventory, slot, player) -> inventory.getStack(slot).isOf(Items.ENDER_CHEST)`,
            // but this form is preferred
            Items.ENDER_CHEST
        );
    }
}
```

And that's it! You should be able to open the game, hover over an ender chest item stack in your inventory, press `k`, and it will cause the inventory edit screen to pop up.

As for the next step, let's make our inventory viewer non-cheaty, so in order to edit the ender chest inventory a player should:

 - Be on the ground
 - Have a pickaxe enchanted with silk touch enchantment
 - Or be in the Creative mode

 To achieve this, we will use the `InventoryValidators` class, that contains some useful predefined inventory validators, but keep in mind, that you can always create a new one from scratch.

```java
public class MyMod implements PackedInventoryInitializer {
    @Override
    public void registerInventoryViewers(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            (inventory, slot, player) -> player.getEnderChestInventory(),
            // isInCreative || isOnGround && hasSilkTouch
            InventoryValidators.IS_IN_CREATIVE.or(
                InventoryValidators.IS_ON_GROUND.and(InventoryValidators.HAS_PICKAXE_WITH_SILK_TOUCH)
            ),
            Items.ENDER_CHEST
        );
    }
}
```

We're getting there! Now you will see a friendly error message when you try to open an ender chest in the midair and/or without a pickaxe enchanted with silk touch enchantment, but the moment you switch to the Creative mode, all restrictions will disappear.

One last thing to do is to make our inventory viewer configurable. After all, we don't have the `config` parameter for nothing ;)

```java
public class MyMod implements PackedInventoryInitializer {
    @Override
    public void registerInventoryViewers(InventoryViewerRegistry registry, PackedInventoryApiConfig config) {
        // Let's register a validation config entry for the ender chest item.
        // The API has these predefined configs (but you can always create a new one by yourself):
        // - ValidationConfig — can be enabled/disabled
        // - GenericValidationConfig — extends ValidationConfig, defines rules for being on the ground and suppressing validation for creative players
        // - SilkTouchableGenericValidationConfig — extends GenericValidationConfig, defines rule for having a silk touch tool in the inventory
        config.registerValidationConfig(Items.ENDER_CHEST, SilkTouchableGenericValidationConfig::new);

        // Inventory viewer may be completely disabled by the user in the config
        var isEnabled = InventoryValidators.config(Items.ENDER_CHEST, config, ValidationConfig.DEFAULT, ValidationConfig::isEnabled);

        // Suppress validation for creative players only if the config says so
        var isInCreative = InventoryValidators.config(Items.ENDER_CHEST, config, GenericValidationConfig.DEFAULT, GenericValidationConfig::isSuppressedInCreative).and(InventoryValidators.IS_IN_CREATIVE);

        // Require the player to be on the ground only if the config says so
        var isOnGround = InventoryValidators.config(Items.ENDER_CHEST, config, GenericValidationConfig.DEFAULT, x -> !x.requiresPlayerOnGround()).or(InventoryValidators.IS_ON_GROUND);

        // Require the player to have a silk touch in their inventory only if the config says so
        var hasSilkTouch = InventoryValidators.config(Items.ENDER_CHEST, config, SilkTouchableGenericValidationConfig.DEFAULT, x -> !x.requiresSilkTouch()).or(InventoryValidators.HAS_PICKAXE_WITH_SILK_TOUCH);

        registry.register(
            (inventory, slot, player) -> player.getEnderChestInventory(),
            // isEnabled && (isInCreative || isOnGround && hasSilkTouch)
            isEnabled.and(isInCreative.or(isOnGround.and(hasSilkTouch))),
            Items.ENDER_CHEST
        );
    }
}
```

This way all restrictions can be configured via `ModMenu`, or `Packed Inventory`'s config file. Perfect, absolutely perfect, you've fully integrated an item with `Packed Inventory`, congratulations!

#### Inventory View Handlers

Ok, we've learned about inventory views, but where did that inventory edit screen come from? This is `InventoryViewHandler`'s job.

Most of the time you should be ok if your inventory:

 - Implements `NamedScreenHandlerFactory`, or
 - Implements `ScreenHandlerFactory`, or
 - Has a conventional size that can be processed by a `GenericContainerScreenHandler` *(i.e., 9, 18, 27, 36, 45, or 54)*

But, just as usual, you can register an `InventoryViewHandler` of your own:

```java
public class MyMod implements PackedInventoryInitializer {
    @Override
    public void registerInventoryViewHandlers(InventoryViewHandlerRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            (inventoryView, parentInventory, slot, player) -> {
                var name = parentInventory.getStack(slot).getName();
                var playerInventory = player.getInventory();
                var namedFactory = new SimpleNamedScreenHandlerFactory((sId, inv, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, sId, playerInventory, inventoryView, 3), name);

                player.openHandledScreen(namedFactory);
            },
            Items.ENDER_CHEST
        );
    }
}
```

Note, that you are **not** limited to just opening a handled screen, you can literally do whatever you want with the provided inventory view.

#### Inventory Validation Failure Handlers

*These names just keep getting longer and longer.* So, we can customize inventory view handling, but what about failure handling? Of course, you can tweak it too.

By default, errors are sent to the corresponding player's action bar, but you can change this behavior:

```java
public class MyMod implements PackedInventoryInitializer {
    @Override
    public void registerInventoryValidationFailureHandlers(InventoryValidationFailureHandlerRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            (failureReason, inventory, slot, player) -> {
                var text = failureReason.toText();
                if (text != null) {
                    player.sendMessage(text, false);
                }
            },
            Items.ENDER_CHEST
        );
    }
}
```

### Tooltips

What is good inventory management without descriptive tooltips? Let's make one for ender chests via `TooltipProvider`!

```java
// Note, that tooltips are completely client-side,
// so we use `PackedInventoryClientInitializer` instead of `PackedInventoryInitializer` here
public class MyClientMod implements PackedInventoryClientInitializer {
    @Override
    public void registerTooltipProviders(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            TooltipProvider.builder()
                .tooltipText((stack, context) -> List.of(Text.of("This is an ender chest")))
                .build()
            Items.ENDER_CHEST
        );
    }
}
```

This way ender chest's tooltip will be replaced with "This is an ender chest" text. Well, this is not helpful. The problem with ender chests is the client does not have any information about their contents. So, is this a dead end? Of course not. We are gonna use some *magic*:

```java
public class MyClientMod implements PackedInventoryClientInitializer {
    @Override
    public void registerTooltipProviders(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            TooltipProvider.builder()
                .useSyncData(GenericContainerTooltipSyncData::of)
                .tooltipData((stack, context, syncData) -> GenericContainerTooltipData.of(syncData.getInventory()))
                .build()
            Items.ENDER_CHEST
        );
    }
}
```

After specifying that our `TooltipProvider` needs `TooltipSyncData` *(in this case I use predefined `GenericContainerTooltipSyncData`, because we only care about ender chest's inventory)*, we are able to use it in the consequential calls to `tooltipData`, `tooltipText`, etc. We will talk about `TooltipSyncData` a little bit later, at the moment just pretend that everything just works.

Note, that we override ender chest's `TooltipData` with `GenericContainerTooltipData`. In case you are not familiar with vanilla treatment of these things, if an item has a `TooltipData`, Minecraft will look for a corresponding `TooltipComponent`, which allows you to render everything you want inside of a tooltip. `GenericContainerTooltipData` is provided by the API, so you don't need to worry about supplying the game with a tooltip component for it, but if you want to use custom tooltip data, you need to register a tooltip component manually via Fabric API like so:

```java
TooltipComponentCallback.EVENT.register(tooltipData -> {
    if (tooltipData instanceof MyTooltipData) {
        return new MyTooltipComponent((MyTooltipData)tooltipData);
    }
    return null;
});
```

Fine, now we have a fancy tooltip that shows us contents of the ender chest. Once again, it's time to make everything configurable:

```java
public class MyClientMod implements PackedInventoryClientInitializer {
    @Override
    public void registerTooltipProviders(TooltipProviderRegistry registry, PackedInventoryApiConfig config) {
        // Let's register a tooltip config entry for the ender chest item.
        // The API has these predefined configs (but you can always create a new one by yourself):
        // - TooltipConfig — can be enabled/disabled, compact mode can be switched on or off
        // - GenericTooltipConfig — extends TooltipConfig, defines properties commonly used by containers
        // - GenericSyncedTooltipConfig — extends GenericTooltipConfig, defines `syncInterval` property used by tooltip providers that require synchronization data
        config.registerTooltipConfig(Items.ENDER_CHEST, GenericSyncedTooltipConfig::new);

        registry.register(
            TooltipProvider.builder()
                // Tooltip can be enabled/disabled in the config or by the key bind (`Left Shift` by default)
                .when((stack, context) -> config.getTooltipConfigOrDefault(stack.getItem()).isEnabled())
                .useSyncData(GenericContainerTooltipSyncData::of)
                .tooltipData((stack, context, syncData) -> {
                    var cfg = config.getTooltipConfigOrDefault(stack.getItem());
                    var inventory = syncData.getInventory();
                    if (!cfg.shouldShowWhenEmpty() && inventory.isEmpty()) {
                        return null;
                    }

                    var color = cfg.usePredefinedColor() ? DyeColor.GRAY : cfg.color();
                    if (cfg.isCompact()) {
                        return GenericContainerTooltipData.ofZipped(inventory, cfg.rows(), cfg.columns(), color);
                    } else {
                        return GenericContainerTooltipData.of(inventory, cfg.rows(), cfg.columns(), color);
                    }
                })
                // Make synchronization interval for this TooltipProvider configurable
                .syncInterval(stack -> config.getTooltipConfigOrDefault(stack.getItem(), GenericSyncedTooltipConfig.DEFAULT).syncInterval())
                .build(),
            Items.ENDER_CHEST
        );
    }
}
```

#### Tooltip Sync Data Providers

As it was said before, in order to make the example above work, we need some information from the server. That's where `TooltipSyncData` and `TooltipSyncDataProvider`s come in play.

We are already using `GenericContainerTooltipSyncData` in our ender chest's tooltip provider, so let's get back to the server and teach it how to handle it:

```java
public class MyMod implements PackedInventoryInitializer {
    @Override
    public void registerTooltipSyncDataProviders(TooltipSyncDataProviderRegistry registry, PackedInventoryApiConfig config) {
        registry.register(
            // On the server side, we need to create the same TooltipSyncData,
            // and fill it with contents that are not presented at the client.
            // Everything else will be handled by Packed Inventory automatically.
            (stack, player) -> GenericContainerTooltipSyncData.of(player.getEnderChestInventory()),
            Items.ENDER_CHEST
        );
    }
}
```

Just a few lines of code and everything works as expected.

----

## Installation

Requirements:

 - Minecraft `1.17.x`
 - Fabric Loader `>=0.11.3`

You can download the mod from:

 - [GitHub Releases](https://github.com/Kir-Antipov/packed-inventory/releases/latest) <sup><sub>(recommended)</sub></sup>
 - [Modrinth](https://modrinth.com/mod/packed-inventory)
 - [CurseForge](https://www.curseforge.com/minecraft/mc-mods/packed-inventory)
 - [GitHub Actions](https://github.com/Kir-Antipov/packed-inventory/actions/workflows/build-artifacts.yml) *(these builds may be unstable, but they represent the actual state of the development)*

## Building from sources

Requirements:

 - JDK `16`

### Linux/MacOS

```cmd
git clone https://github.com/Kir-Antipov/packed-inventory.git
cd packed-inventory

chmod +x ./gradlew
./gradlew build
cd build/libs
```
### Windows

```cmd
git clone https://github.com/Kir-Antipov/packed-inventory.git
cd packed-inventory

gradlew build
cd build/libs
```
