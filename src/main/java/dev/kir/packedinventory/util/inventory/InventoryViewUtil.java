package dev.kir.packedinventory.util.inventory;

import com.mojang.datafixers.util.Either;
import dev.kir.packedinventory.api.v1.FailureReason;
import dev.kir.packedinventory.api.v1.inventory.InventoryValidationFailureHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewHandlerRegistry;
import dev.kir.packedinventory.api.v1.inventory.InventoryViewerRegistry;
import dev.kir.packedinventory.inventory.CombinedInventory;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public final class InventoryViewUtil {
    public static boolean isNonEmptyView(Inventory inventory, int slot, PlayerEntity player) {
        InventoryViewerRegistry registry = InventoryViewerRegistry.getInstance();
        if (!registry.hasView(inventory, slot, player)) {
            return false;
        }

        Optional<Inventory> view = registry.forceView(inventory, slot, player);
        return view.isPresent() && view.get().size() > 0;
    }

    public static boolean handleView(Inventory inventory, int slot, PlayerEntity player) {
        return InventoryViewUtil.handleView(inventory, slot, player, view -> InventoryViewUtil.handleView(view, inventory, slot, player));
    }

    public static boolean handleView(Inventory inventory, int slot, PlayerEntity player, Consumer<Inventory> viewHandler) {
        return InventoryViewUtil.handleView(inventory, slot, player, viewHandler, failure -> InventoryViewUtil.handleFailure(failure, inventory, slot, player));
    }

    public static boolean handleView(Inventory inventory, int slot, PlayerEntity player, Consumer<Inventory> viewHandler, Consumer<FailureReason> failureHandler) {
        Optional<Either<Inventory, FailureReason>> view = InventoryViewUtil.getView(inventory, slot, player);
        if (view.isEmpty()) {
            return false;
        }

        view.get().ifLeft(viewHandler).ifRight(failureHandler);
        return true;
    }

    public static boolean handleView(Inventory view, Inventory parentInventory, int slot, PlayerEntity player) {
        return InventoryViewHandlerRegistry.getInstance().handle(view, parentInventory, slot, player);
    }

    public static boolean handleViews(Inventory inventory, IntList slots, PlayerEntity player) {
        return InventoryViewUtil.handleViews(inventory, slots, player, (view, slot) -> {
            InventoryViewUtil.handleView(view, inventory, slot, player);
            return true;
        });
    }

    public static boolean handleViews(Inventory inventory, IntList slots, PlayerEntity player, BiPredicate<Inventory, Integer> viewHandler) {
        return InventoryViewUtil.handleViews(inventory, slots, player, viewHandler, (failure, slot) -> InventoryViewUtil.handleFailure(failure, inventory, slot, player));
    }

    public static boolean handleViews(Inventory inventory, IntList slots, PlayerEntity player, BiPredicate<Inventory, Integer> viewHandler, BiConsumer<FailureReason, Integer> failureHandler) {
        boolean allViewsAreHandled = true;
        int lastSlot = -1;
        FailureReason lastFailure = null;

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.getInt(i);
            Optional<Either<Inventory, FailureReason>> optionalView = InventoryViewUtil.getView(inventory, slot, player);
            if (optionalView.isEmpty()) {
                continue;
            }

            Either<Inventory, FailureReason> view = optionalView.get();
            if (view.left().isPresent()) {
                if (viewHandler.test(view.left().get(), slot)) {
                    return true;
                }

                lastFailure = null;
                continue;
            }

            if (view.right().isPresent()) {
                lastSlot = slot;
                lastFailure = view.right().get();
            }

            allViewsAreHandled = false;
        }

        if (lastFailure != null) {
            failureHandler.accept(lastFailure, lastSlot);
        }

        return allViewsAreHandled;
    }

    public static boolean handleFailure(FailureReason failureReason, Inventory parentInventory, int slot, PlayerEntity player) {
        return InventoryValidationFailureHandlerRegistry.getInstance().handle(failureReason, parentInventory, slot, player);
    }

    public static boolean transferViews(Inventory inventory, IntList fromSlots, IntList toSlots, PlayerEntity player) {
        if (fromSlots.size() < toSlots.size()) {
            return InventoryViewUtil.transferFromViews(inventory, fromSlots, toSlots, player) || InventoryViewUtil.transferToViews(inventory, fromSlots, toSlots, player);
        } else {
            return InventoryViewUtil.transferToViews(inventory, fromSlots, toSlots, player) || InventoryViewUtil.transferFromViews(inventory, fromSlots, toSlots, player);
        }
    }

    private static boolean transferFromViews(Inventory inventory, IntList fromSlots, IntList toSlots, PlayerEntity player) {
        for (IntListIterator iterator = fromSlots.iterator(); iterator.hasNext(); ) {
            int fromSlot = iterator.nextInt();
            Inventory fromView = InventoryViewUtil.tryGetView(inventory, fromSlot, player).orElse(null);
            if (fromView == null || !fromView.canPlayerUse(player)) {
                continue;
            }

            if (InventoryViewUtil.transferFromView(fromView, inventory, toSlots, player)) {
                iterator.remove();
            }
        }
        return fromSlots.isEmpty();
    }

    private static boolean transferFromView(Inventory from, Inventory to, IntList toSlots, PlayerEntity player) {
        for (IntListIterator iterator = toSlots.iterator(); iterator.hasNext(); ) {
            if (!from.canPlayerUse(player)) {
                return false;
            }

            int toSlot = iterator.nextInt();
            if (InventoryUtil.transfer(from, -1, to, toSlot)) {
                iterator.remove();
            }
        }

        return toSlots.isEmpty();
    }

    private static boolean transferToViews(Inventory inventory, IntList fromSlots, IntList toSlots, PlayerEntity player) {
        for (IntListIterator iterator = toSlots.iterator(); iterator.hasNext(); ) {
            int toSlot = iterator.nextInt();
            Inventory toView = InventoryViewUtil.tryGetView(inventory, toSlot, player).orElse(null);
            if (toView == null || !toView.canPlayerUse(player)) {
                continue;
            }

            if (InventoryViewUtil.transferToView(inventory, fromSlots, toView, player)) {
                return true;
            }
        }
        return false;
    }

    private static boolean transferToView(Inventory from, IntList fromSlots, Inventory to, PlayerEntity player) {
        for (IntListIterator iterator = fromSlots.iterator(); iterator.hasNext(); ) {
            if (!from.canPlayerUse(player)) {
                return false;
            }

            int fromSlot = iterator.nextInt();
            if (InventoryUtil.transfer(from, fromSlot, to, -1)) {
                iterator.remove();
            }
        }

        return fromSlots.isEmpty();
    }

    public static boolean dropViews(Inventory inventory, IntList slots, PlayerEntity player) {
        return InventoryViewUtil.handleViews(inventory, slots, player, (view, slot) -> {
            InventoryUtil.drop(view, player);
            return false;
        });
    }

    public static Optional<Inventory> tryGetView(Inventory inventory, int slot, PlayerEntity player) {
        return InventoryViewUtil.getView(inventory, slot, player).flatMap(x -> x.left());
    }

    public static Optional<Either<Inventory, FailureReason>> getView(Inventory inventory, int slot, PlayerEntity player) {
        InventoryViewerRegistry registry = InventoryViewerRegistry.getInstance();
        Optional<Either<Inventory, FailureReason>> view = registry.view(inventory, slot, player);
        if (view.isEmpty()) {
            return view;
        }

        boolean hasInventory = view.get().left().isPresent();
        if (hasInventory) {
            return view;
        }

        LinkedHashSet<Inventory> availableInventories = new LinkedHashSet<>();
        availableInventories.add(inventory);
        for (Slot screenHandlerSlot : player.currentScreenHandler.slots) {
            availableInventories.add(screenHandlerSlot.inventory);
        }

        if (availableInventories.size() == 1) {
            return view;
        }

        return registry.view(CombinedInventory.of(availableInventories), slot, player).or(() -> view);
    }

    private InventoryViewUtil() { }
}