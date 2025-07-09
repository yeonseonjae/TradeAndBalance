package me.shark0822.tradeAndBalance.gui;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.util.GuiUtil;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EditorGUI {
    public static final int[] ITEM_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    public static void open(Player player, Shop shop) {
        // 현재 페이지가 없으면 새 페이지 생성
        if (shop.getCurrentPageNode() == null) {
            shop.addPage(new ShopPage());
        }

        Inventory inv = Bukkit.createInventory(player, 54, TextUtil.format("&8[편집] " + shop.getShopName()));

        GuiUtil.fillDefault(inv, shop, 48, 49, 50);

        inv.setItem(48, GuiUtil.UNDO_BTN);
        inv.setItem(49, GuiUtil.createTradeModeButton(shop.getTradeType()));
        inv.setItem(50, GuiUtil.REDO_BTN);

        ShopPage page = shop.getCurrentPage();
        List<ShopItem> items = page.getItems();

        for (int i = 0; i < Math.min(items.size(), ITEM_SLOTS.length); i++) {
            inv.setItem(ITEM_SLOTS[i], items.get(i).getItemStack());
        }

        player.openInventory(inv);
    }

    public static void update(Player player, Shop shop) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv == null) {
            open(player, shop); // 인벤토리가 없으면 새로 열기
            return;
        }

        // 기존 인벤토리 갱신
        GuiUtil.fillDefault(inv, shop, 48, 49, 50);

        inv.setItem(48, GuiUtil.UNDO_BTN);
        inv.setItem(49, GuiUtil.createTradeModeButton(shop.getTradeType()));
        inv.setItem(50, GuiUtil.REDO_BTN);

        ShopPage page = shop.getCurrentPage();
        List<ShopItem> items = page.getItems();

        int[] itemSlots = {
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                27, 28, 29, 30, 31, 32, 33, 34, 35
        };

        // 아이템 슬롯 초기화
        for (int slot : itemSlots) {
            inv.setItem(slot, null);
        }

        // 현재 페이지 아이템 표시
        for (int i = 0; i < Math.min(items.size(), itemSlots.length); i++) {
            inv.setItem(itemSlots[i], items.get(i).getItemStack());
        }
    }

    public static void syncItems(Player player, Shop shop) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv == null) {
            return;
        }

        ShopPage page = shop.getCurrentPage();
        page.clear();

        // 인벤토리 슬롯의 아이템을 ShopPage에 동기화
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            ItemStack item = inv.getItem(ITEM_SLOTS[i]);
            if (item != null && item.getType() != Material.AIR && !item.equals(GuiUtil.BLACK_GLASS)) {
                ShopItem newShopItem = new ShopItem(item.clone(), 0.0, LimitType.NONE, 0);
                page.addItem(newShopItem);
                System.out.println("[DEBUG] Synced item to slot " + ITEM_SLOTS[i] + ", index " + i);
            }
        }
    }
}
