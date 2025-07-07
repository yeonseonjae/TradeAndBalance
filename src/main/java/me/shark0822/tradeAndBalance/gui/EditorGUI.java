package me.shark0822.tradeAndBalance.gui;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.util.GuiUtil;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class EditorGUI {
    public static void open(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(null, 54, TextUtil.format("&8[편집] " + shop.getShopName()));

        GuiUtil.fillDefault(inv, 48, 49, 50);

        inv.setItem(48, GuiUtil.UNDO_BTN);
        inv.setItem(49, GuiUtil.createTradeModeButton(shop.getTradeType()));
        inv.setItem(50, GuiUtil.REDO_BTN);

        ShopPage page = shop.getCurrentPage();
        List<ShopItem> items = page.getItems();

        int[] itemSlots = {
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                27,28,29,30,31,32,33,34,35
        };

        for (int i = 0; i < Math.min(items.size(), itemSlots.length); i++) {
            inv.setItem(itemSlots[i], items.get(i).getItemStack());
        }

        player.openInventory(inv);
    }
}
