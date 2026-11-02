package dev.shulkersell;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.api.objects.SellPrices;
import me.gypopo.economyshopgui.providers.EconomyProvider;
import me.gypopo.economyshopgui.util.EcoType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class SellGui implements InventoryHolder {

    static final int SIZE = 54;
    static final int ITEM_SLOTS = 45; // slots 0-44 hold items, the last row is controls
    static final int BUTTON = 49;

    private final ShulkerSellPlugin plugin;
    private final Player player;
    private final Inventory inv;
    private boolean done;
    private boolean refreshQueued;

    SellGui(ShulkerSellPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inv = Bukkit.createInventory(this, SIZE, Component.text("Sell Items"));
        ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
        for (int i = ITEM_SLOTS; i < SIZE; i++) {
            inv.setItem(i, filler);
        }
        refreshButton(null);
    }

    void open() {
        player.openInventory(inv);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    private ItemStack[] collect() {
        ItemStack[] items = new ItemStack[ITEM_SLOTS];
        for (int i = 0; i < ITEM_SLOTS; i++) {
            items[i] = inv.getItem(i);
        }
        return items;
    }

    void scheduleRefresh() {
        if (refreshQueued || done) return;
        refreshQueued = true;
        Bukkit.getScheduler().runTask(plugin, () -> {
            refreshQueued = false;
            if (!done) {
                refreshButton(SellCalc.compute(player, collect()).prices());
            }
        });
    }

    private void refreshButton(SellPrices prices) {
        List<Component> lore = new ArrayList<>();
        if (prices == null || prices.isEmpty()) {
            lore.add(line("Nothing sellable yet", NamedTextColor.GRAY));
        } else {
            int count = 0;
            for (int amount : prices.getItems().values()) count += amount;
            lore.add(line("Items: " + count, NamedTextColor.GRAY));
            lore.add(line("Value: " + formatPrices(prices), NamedTextColor.GREEN));
        }
        lore.add(Component.empty());
        lore.add(line("Shulkers: the items inside are sold,", NamedTextColor.DARK_GRAY));
        lore.add(line("the box comes back to you.", NamedTextColor.DARK_GRAY));
        lore.add(line("Empty shulkers are sold themselves.", NamedTextColor.DARK_GRAY));
        inv.setItem(BUTTON, named(Material.EMERALD, line("Click to sell", NamedTextColor.GREEN), lore));
    }

    void sell() {
        if (done) return;
        SellCalc.Result res = SellCalc.compute(player, collect());
        SellPrices sp = res.prices();
        if (sp.isEmpty()) {
            player.sendMessage(line("Nothing in here can be sold.", NamedTextColor.RED));
            return;
        }
        int count = 0;
        for (int amount : sp.getItems().values()) count += amount;
        String total = formatPrices(sp);

        sp.updateLimits();
        for (Map.Entry<EcoType, Double> e : sp.getPrices().entrySet()) {
            EconomyProvider provider = EconomyShopGUIHook.getEcon(e.getKey());
            if (provider != null && e.getValue() > 0) {
                provider.depositBalance(player, e.getValue());
            }
        }

        ItemStack[] left = res.remaining();
        for (int i = 0; i < ITEM_SLOTS; i++) {
            inv.setItem(i, left[i]);
        }
        done = true;
        player.sendMessage(line("Sold " + count + " items for " + total + ".", NamedTextColor.GREEN));
        Bukkit.getScheduler().runTask(plugin, player::closeInventory);
    }

    /** Gives back everything still sitting in the item slots. */
    void returnItems() {
        for (int i = 0; i < ITEM_SLOTS; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            inv.setItem(i, null);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            for (ItemStack extra : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), extra);
            }
        }
    }

    private static String formatPrices(SellPrices prices) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<EcoType, Double> e : prices.getPrices().entrySet()) {
            EconomyProvider provider = EconomyShopGUIHook.getEcon(e.getKey());
            String text = provider != null ? provider.formatPrice(e.getValue()) : String.valueOf(e.getValue());
            parts.add(text.replaceAll("[§&][0-9a-fk-orA-FK-OR]", ""));
        }
        return String.join(", ", parts);
    }

    private static Component line(String text, NamedTextColor color) {
        return Component.text(text, color);
    }

    private static ItemStack named(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        List<Component> plain = new ArrayList<>();
        for (Component c : lore) plain.add(c.decoration(TextDecoration.ITALIC, false));
        meta.lore(plain);
        item.setItemMeta(meta);
        return item;
    }
}
