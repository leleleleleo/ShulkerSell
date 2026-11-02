# ShulkerSell

A `/sell` menu for Paper that uses your EconomyShopGUI prices.

- Put items in the menu and click the emerald to sell.
- Shulker box **with items inside**: only the items inside are sold, the box comes back to you.
- **Empty** shulker box: the box itself is sold.
- Items that can't be sold (no price, limit reached, no permission) are returned to you.
- Prices, sell limits and dynamic pricing all come from EconomyShopGUI.

## Build it (no software needed, uses GitHub)

1. On github.com click **New repository**, name it `ShulkerSell`, create it.
2. Upload everything from this folder, **including the hidden `.github` folder**
   (drag the whole folder contents onto the "uploading an existing file" page).
   If the `.github` folder won't upload, open the **Actions** tab, click
   "set up a workflow yourself", and paste the contents of `.github/workflows/build.yml`.
3. Open the **Actions** tab. The `Build` workflow runs by itself (or press **Run workflow**).
4. When it finishes (green check), open the run and download the **ShulkerSell** artifact.
   Unzip it, you get `ShulkerSell-1.0.0.jar`.

If the run fails (red cross), open it and send the error log to Claude.

## Install

1. Put `ShulkerSell-1.0.0.jar` in `plugins/` and restart the server.
2. Make sure Essentials is not handling `/sell` (`disabled-commands: - sell`).
3. Type `/sell`. The alias `/sellshulker` also works.
4. Players are allowed by default (permission `shulkersell.use`).
5. To stop players using the old menu, remove `economyshopgui.sellgui` and
   `economyshopgui.sellgui.*` from your `default` group. Otherwise `/sellgui` still works
   the old way.

## Notes

- Needs EconomyShopGUI (free or premium) 7.x and Paper 1.21+.
- Sales made here are not written to EconomyShopGUI's transaction log.
