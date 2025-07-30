package me.shark0822.tradeAndBalance.gui;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.util.GuiUtil;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class ShopGUI {
    private final ShopManager shopManager;
    private final Shop shop;
    private final Inventory inventory;

    public ShopGUI(ShopManager shopManager, Shop shop) {
        this.shopManager = shopManager;
        this.shop = shop;
        this.inventory = Bukkit.createInventory(null, 54, TextUtil.format(shop.getShopName())); // 54 슬롯 인벤토리 생성
        updateInventory();
    }

    // 인벤토리 업데이트
    private void updateInventory() {
        inventory.clear(); // 인벤토리 초기화
        GuiUtil.fillDefault(inventory, shop, ShopManager.getValidSlots()); // 기본 레이아웃 채우기
        for (Map.Entry<Integer, ShopItem> entry : shop.getCurrentPage().getItems().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getDisplayItem(false, shop.getTradeType())); // 아이템 표시
        }
    }

    // GUI 열기
    public void open(Player player) {
        updateInventory();
        player.openInventory(inventory); // 플레이어에게 인벤토리 표시
    }

    // 구매 처리
    public void handlePurchase(Player player, int slot, int amount) {
        shopManager.purchaseItem(player, shop.getShopID(), shop.getCurrentPage().getIndex(), slot, amount);
        updateInventory();
    }

    // 판매 처리
    public void handleSell(Player player, int slot, int amount) {
        shopManager.sellItem(player, shop.getShopID(), shop.getCurrentPage().getIndex(), slot, amount);
        updateInventory();
    }

    // 페이지 이동
    public void nextPage() {
        shop.nextPage();
        updateInventory();
    }

    public void prevPage() {
        shop.prevPage();
        updateInventory();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Shop getShop() {
        return this.shop;
    }
}
