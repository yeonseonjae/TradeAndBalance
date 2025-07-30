package me.shark0822.tradeAndBalance.gui;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.GuiUtil;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EditorGUI {
    public static final int[] ITEM_SLOTS = ShopManager.getValidSlots();
    public static final int[] BLOCKED_SLOTS = { 0,1,2,3,4,5,6,7,8,18,19,20,21,22,23,24,25,26,36,37,38,39,40,41,42,43,44,46,47,51,52 };
    private final ShopManager shopManager;
    private final Shop shop;
    private final Inventory inventory;

    public EditorGUI(ShopManager shopManager, Shop shop) {
        this.shopManager = shopManager;
        this.shop = shop;
        this.inventory = Bukkit.createInventory(null, 54, TextUtil.format("[편집] " + shop.getShopName()));

        // 디버그 메시지: EditorGUI 생성
        Bukkit.getLogger().info("[EditorGUI] EditorGUI 생성됨 - 상점: " + shop.getShopName());

        updateInventory();
    }

    // 인벤토리 업데이트
    private void updateInventory() {
        Bukkit.getLogger().info("[EditorGUI] 인벤토리 업데이트 시작 - 상점: " + shop.getShopName());

        inventory.clear(); // 인벤토리 초기화
        GuiUtil.fillDefault(inventory, shop, ITEM_SLOTS);
        inventory.setItem(48, GuiUtil.UNDO_BTN); // Undo 버튼
        inventory.setItem(49, GuiUtil.createTradeModeButton(shop.getTradeType())); // 거래 모드 버튼
        inventory.setItem(50, GuiUtil.REDO_BTN); // Redo 버튼

        int itemCount = 0;
        for (Map.Entry<Integer, ShopItem> entry : shop.getCurrentPage().getItems().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getDisplayItem(true, shop.getTradeType()));
            itemCount++;
        }

        Bukkit.getLogger().info("[EditorGUI] 인벤토리 업데이트 완료 - 아이템 수: " + itemCount +
                ", 현재 페이지: " + shop.getCurrentPage().getIndex() +
                ", 거래 모드: " + shop.getTradeType());
    }

    // GUI 열기
    public void open(Player player) {
        Bukkit.getLogger().info("[EditorGUI] GUI 열기 - 플레이어: " + player.getName() +
                ", 상점: " + shop.getShopName());

        updateInventory();
        player.openInventory(inventory);

        Bukkit.getLogger().info("[EditorGUI] GUI 열기 완료 - " + player.getName());
    }

    // 아이템 추가
    public ShopItem addItem(int slot, ItemStack item) {
        Bukkit.getLogger().info("[EditorGUI] 아이템 추가 시도 - 슬롯: " + slot +
                ", 아이템: " + (item != null ? item.getType() : "null"));

        if (item == null || item.getType().isAir()) {
            Bukkit.getLogger().warning("[EditorGUI] 아이템 추가 실패 - 아이템이 null이거나 공기");
            return null;
        }

        // 변경 전 페이지 상태 저장
        ShopPage beforeState = shop.getCurrentPage().clone();

        ShopItem shopItem = new ShopItem(item, 0, 0, LimitType.NONE, 0);
        shop.getCurrentPage().addItem(slot, shopItem);

        // 편집 액션 기록 (페이지 단위)
        shopManager.recordEditorAction(shop, shop.getCurrentPage(), beforeState, this);

        Bukkit.getLogger().info("[EditorGUI] 아이템 추가 완료 - 슬롯: " + slot +
                ", 아이템: " + item.getType() +
                ", 페이지: " + shop.getCurrentPage().getIndex());

        updateInventory();
        return shopItem;
    }

    // 아이템 제거
    public void removeItem(int slot) {
        Bukkit.getLogger().info("[EditorGUI] 아이템 제거 시도 - 슬롯: " + slot);

        // 변경 전 페이지 상태 저장
        ShopPage beforeState = shop.getCurrentPage().clone();

        ShopItem item = shop.getCurrentPage().removeItem(slot);
        if (item != null) {
            // 편집 액션 기록 (페이지 단위)
            shopManager.recordEditorAction(shop, shop.getCurrentPage(), beforeState, this);

            Bukkit.getLogger().info("[EditorGUI] 아이템 제거 완료 - 슬롯: " + slot +
                    ", 제거된 아이템: " + item.getOriginalItem().getType());
        } else {
            Bukkit.getLogger().info("[EditorGUI] 아이템 제거 - 슬롯에 아이템 없음: " + slot);
        }

        updateInventory();
    }

    // 페이지 이동
    public void nextPage() {
        int oldPage = shop.getCurrentPage().getIndex();
        shop.nextPage();
        int newPage = oldPage + 1;

        Bukkit.getLogger().info("[EditorGUI] 다음 페이지로 이동 - " + oldPage + " → " + newPage);

        updateInventory();
    }

    public void prevPage() {
        int oldPage = shop.getCurrentPage().getIndex();
        shop.prevPage();
        int newPage = oldPage - 1;

        Bukkit.getLogger().info("[EditorGUI] 이전 페이지로 이동 - " + oldPage + " → " + newPage);

        updateInventory();
    }

    // 거래 모드 변경
    public void toggleTradeMode() {
        TradeType oldMode = shop.getTradeType();
        TradeType newMode = switch (oldMode) {
            case BUY -> TradeType.SELL;
            case SELL -> TradeType.BOTH;
            case BOTH -> TradeType.BUY;
        };

        shop.setTradeType(newMode);

        Bukkit.getLogger().info("[EditorGUI] 거래 모드 변경 - " + oldMode + " → " + newMode);

        updateInventory();
    }

    // 저장 및 재정렬
    public void saveAndRearrange() {
        Bukkit.getLogger().info("[EditorGUI] 저장 및 재정렬 시작 - 상점: " + shop.getShopName());

        shop.removeEmptyPagesAndRearrange(ITEM_SLOTS);
        updateInventory();

        Bukkit.getLogger().info("[EditorGUI] 저장 및 재정렬 완료 - 상점: " + shop.getShopName());
    }

    public Shop getShop() {
        return this.shop;
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}