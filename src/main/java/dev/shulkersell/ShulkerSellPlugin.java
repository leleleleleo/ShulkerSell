package dev.shulkersell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShulkerSellPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        boolean hasShop = getServer().getPluginManager().getPlugin("EconomyShopGUI") != null
                || getServer().getPluginManager().getPlugin("EconomyShopGUI-Premium") != null;
        if (!hasShop) {
            getLogger().severe("EconomyShopGUI was not found. ShulkerSell needs it for prices, disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new SellListener(), this);
        if (getCommand("sell") != null) {
            getCommand("sell").setExecutor(this);
        }
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder(false) instanceof SellGui) {
                p.closeInventory();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }
        new SellGui(this, player).open();
        return true;
    }
}
