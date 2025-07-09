package me.shark0822.tradeAndBalance.listener;

import me.shark0822.tradeAndBalance.gui.EditorGUI;
import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.DataManager;
import me.shark0822.tradeAndBalance.util.GuiUtil;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ShopEditorListener implements Listener {
    private final ShopManager shopManager;
    private final DataManager dataManager;

    public static final int[] ITEM_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    public ShopEditorListener(ShopManager shopManager, DataManager dataManager) {
        this.shopManager = shopManager;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = TextUtil.componentToLegacy(event.getView().title());
        if (title == null || !title.contains("[편집]")) return;

        Shop shop = shopManager.getShopByName(title);
        if (shop == null) return;

        int slot = event.getRawSlot();
        ShopPage currentPage = shop.getCurrentPage();

        if (slot < 54) {
            if (Arrays.stream(ITEM_SLOTS).noneMatch(s -> s == slot)) {
                event.setCancelled(true);
            }

            switch (slot) {
                case 45: // 이전 페이지
                    EditorGUI.syncItems(player, shop);
                    shop.prevPage();
                    EditorGUI.update(player, shop);
                    break;
                case 53: // 다음 페이지
                    EditorGUI.syncItems(player, shop);
                    shop.addNextPage();
                    EditorGUI.update(player, shop);
                    break;
                case 48: // Undo
                    player.sendMessage(TextUtil.format("&c미구현"));
                    break;
                case 50: // Redo
                    player.sendMessage(TextUtil.format("&c미구현"));
                    break;
                case 49: // TradeType 변경
                    EditorGUI.syncItems(player, shop);
                    TradeType nextType = TradeType.values()[(shop.getTradeType().ordinal() + 1) % TradeType.values().length];
                    shop.setTradeType(nextType);
                    EditorGUI.update(player, shop);
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = TextUtil.componentToLegacy(event.getView().title());
        if (title == null || !title.contains("[편집]")) return;

        Shop shop = shopManager.getShopByName(title);
        if (shop != null) {
            EditorGUI.syncItems(player, shop);
            shop.removeEmptyPages();
            dataManager.saveShops(shopManager);
            player.sendMessage(TextUtil.format("&GREEN상점이 저장되었습니다."));
        }
    }
}