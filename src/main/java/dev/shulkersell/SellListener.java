package dev.shulkersell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

final class SellListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof SellGui gui)) return;

        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedInventory() == top) {
            int slot = e.getRawSlot();
            if (slot >= SellGui.ITEM_SLOTS) {
                e.setCancelled(true);
                if (slot == SellGui.BUTTON && e.getWhoClicked() instanceof Player) {
                    gui.sell();
                }
                return;
            }
        }
        gui.scheduleRefresh();
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof SellGui gui)) return;
        for (int slot : e.getRawSlots()) {
            if (slot >= SellGui.ITEM_SLOTS && slot < SellGui.SIZE) {
                e.setCancelled(true);
                return;
            }
        }
        gui.scheduleRefresh();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder(false) instanceof SellGui gui) {
            gui.returnItems();
        }
    }
}
