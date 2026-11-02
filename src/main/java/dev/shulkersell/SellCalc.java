package dev.shulkersell;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.api.objects.SellPrices;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Works out what a set of items in the sell menu is worth.
 * Rules:
 *  - A shulker box that has items inside: only the items inside are sold, the box is kept.
 *  - A shulker box that is empty: the box itself is sold.
 *  - Everything else is sold as normal.
 * Prices, permissions, limits and dynamic pricing all come from EconomyShopGUI.
 * Nothing passed in is modified, the caller gets the leftover items back in Result.
 */
final class SellCalc {

    record Result(SellPrices prices, ItemStack[] remaining) {}

    private SellCalc() {}

    static Result compute(Player player, ItemStack[] gui) {
        int n = gui.length;
        ItemStack[] out = new ItemStack[n];
        ItemStack[][] shulkerContents = new ItemStack[n][];

        List<ItemStack> candidates = new ArrayList<>();
        List<int[]> refs = new ArrayList<>(); // {guiSlot, contentSlot}, contentSlot -1 means the item itself

        for (int i = 0; i < n; i++) {
            ItemStack item = gui[i];
            if (item == null || item.getType().isAir()) continue;
            out[i] = item.clone();

            if (isShulker(item)) {
                ItemStack[] contents = readContents(item);
                if (contents != null && hasAny(contents)) {
                    shulkerContents[i] = contents;
                    for (int c = 0; c < contents.length; c++) {
                        if (contents[c] == null) continue;
                        candidates.add(contents[c].clone());
                        refs.add(new int[]{i, c});
                    }
                    continue; // only the contents are sold
                }
                // empty shulker: falls through and is sold as an item
            }
            candidates.add(item.clone());
            refs.add(new int[]{i, -1});
        }

        ItemStack[] arr = candidates.toArray(new ItemStack[0]);
        SellPrices prices = EconomyShopGUIHook.getCutSellPrices(player, arr, true);

        for (int k = 0; k < arr.length; k++) {
            int[] ref = refs.get(k);
            ItemStack left = arr[k]; // null when fully sold, reduced when only partly sold
            if (ref[1] == -1) {
                out[ref[0]] = left;
            } else {
                shulkerContents[ref[0]][ref[1]] = left;
            }
        }
        for (int i = 0; i < n; i++) {
            if (shulkerContents[i] != null && out[i] != null) {
                out[i] = withContents(out[i], shulkerContents[i]);
            }
        }
        return new Result(prices, out);
    }

    private static boolean isShulker(ItemStack item) {
        return Tag.SHULKER_BOXES.isTagged(item.getType());
    }

    private static boolean hasAny(ItemStack[] contents) {
        for (ItemStack c : contents) {
            if (c != null) return true;
        }
        return false;
    }

    private static ItemStack[] readContents(ItemStack shulker) {
        ItemMeta meta = shulker.getItemMeta();
        if (!(meta instanceof BlockStateMeta bsm)) return null;
        if (!(bsm.getBlockState() instanceof ShulkerBox box)) return null;
        ItemStack[] raw = box.getInventory().getContents();
        ItemStack[] copy = new ItemStack[raw.length];
        for (int i = 0; i < raw.length; i++) {
            ItemStack r = raw[i];
            copy[i] = (r == null || r.getType().isAir()) ? null : r.clone();
        }
        return copy;
    }

    private static ItemStack withContents(ItemStack shulker, ItemStack[] contents) {
        ItemStack result = shulker.clone();
        ItemMeta meta = result.getItemMeta();
        if (!(meta instanceof BlockStateMeta bsm)) return result;
        if (!(bsm.getBlockState() instanceof ShulkerBox box)) return result;
        box.getInventory().setContents(contents);
        bsm.setBlockState(box);
        result.setItemMeta(bsm);
        return result;
    }
}
