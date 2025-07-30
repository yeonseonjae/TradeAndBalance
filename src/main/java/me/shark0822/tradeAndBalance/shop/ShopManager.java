package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.gui.EditorGUI;
import me.shark0822.tradeAndBalance.gui.ItemSettingsGUI;
import me.shark0822.tradeAndBalance.gui.ShopGUI;
import me.shark0822.tradeAndBalance.shop.action.EditorAction;
import me.shark0822.tradeAndBalance.shop.action.ItemSettingsAction;
import me.shark0822.tradeAndBalance.shop.action.ShopEditAction;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.structure.ActionStack;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ShopManager {
    private final Map<String, Shop> shops;
    private final Map<ShopItem, Integer> globalLimits;
    private final Map<ShopItem, Map<UUID, Integer>> personalLimits;

    // 컨텍스트별 액션 스택 분리
    private final ActionStack<EditorAction> editorActionStack; // EditorGUI 전용
    private final ActionStack<EditorAction> editorRedoStack;
    private final ActionStack<EditorAction> itemSettingsActionStack; // ItemSettingsGUI 전용
    private final ActionStack<EditorAction> itemSettingsRedoStack;

    private final Map<UUID, ShopGUI> playerShopGUIs;
    private final Map<UUID, EditorGUI> playerEditorGUIs;
    private final Map<String, UUID> lockedShops;
    private static final int[] VALID_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17, 27, 28, 29, 30, 31, 32, 33, 34, 35};

    public ShopManager() {
        shops = new HashMap<>();
        globalLimits = new HashMap<>();
        personalLimits = new HashMap<>();

        // 스택 초기화
        editorActionStack = new ActionStack<>();
        editorRedoStack = new ActionStack<>();
        itemSettingsActionStack = new ActionStack<>();
        itemSettingsRedoStack = new ActionStack<>();

        playerShopGUIs = new HashMap<>();
        playerEditorGUIs = new HashMap<>();
        lockedShops = new HashMap<>();
    }

    // 상점 생성
    public boolean createShop(String id, String name, TradeType type) {
        if (shops.containsKey(id)) return false;
        Shop shop = new Shop(id, name, type);
        shops.put(id, shop);
        return true;
    }

    // 상점 제거
    public boolean removeShop(String id) {
        unlockShop(id); // 상점 제거 전 잠금 해제
        return shops.remove(id) != null;
    }

    // 상점 조회
    public Shop getShop(String id) {
        return shops.get(id);
    }

    // 몹 UUID로 상점 조회
    public Shop getShopByMobUUID(UUID uuid) {
        for (Shop shop : shops.values()) {
            if (uuid.equals(shop.getLinkedEntityUUID())) {
                return shop;
            }
        }
        return null;
    }

    // 모든 상점 반환
    public Collection<Shop> getAllShops() {
        return shops.values();
    }

    // 상점 수 반환
    public int getShopCount() {
        return shops.size();
    }

    // 모든 데이터 초기화
    public void clearAll() {
        shops.clear();
        globalLimits.clear();
        personalLimits.clear();

        // 모든 스택 초기화
        editorActionStack.clear();
        editorRedoStack.clear();
        itemSettingsActionStack.clear();
        itemSettingsRedoStack.clear();

        playerShopGUIs.clear();
        playerEditorGUIs.clear();
        lockedShops.clear();
    }

    // 남은 수량 제한 조회
    public int getRemainingLimit(Player player, ShopItem item) {
        if (item.getLimitType() == LimitType.GLOBAL) {
            return globalLimits.getOrDefault(item, item.getLimitAmount());
        } else if (item.getLimitType() == LimitType.PERSONAL) {
            Map<UUID, Integer> playerLimits = personalLimits.computeIfAbsent(item, k -> new HashMap<>());
            return playerLimits.getOrDefault(player.getUniqueId(), item.getLimitAmount());
        }
        return item.getLimitAmount();
    }

    // 수량 제한 업데이트
    public void updateRemainingLimit(Player player, ShopItem item, int newLimit) {
        if (item.getLimitType() == LimitType.GLOBAL) {
            globalLimits.put(item, Math.max(0, newLimit));
        } else if (item.getLimitType() == LimitType.PERSONAL) {
            Map<UUID, Integer> playerLimits = personalLimits.computeIfAbsent(item, k -> new HashMap<>());
            playerLimits.put(player.getUniqueId(), Math.max(0, newLimit));
        }
    }

    // 아이템 구매
    public boolean purchaseItem(Player player, String shopId, int pageIndex, int slot, int amount) {
        if (isShopLocked(shopId)) {
            player.sendMessage(TextUtil.format("&c이 상점은 현재 편집 중입니다."));
            return false;
        }
        Shop shop = shops.get(shopId);
        if (shop == null) {
            player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
            return false;
        }
        ShopPage page = shop.getPages().get(pageIndex);
        if (page == null) {
            player.sendMessage(TextUtil.format("&c페이지를 찾을 수 없습니다."));
            return false;
        }
        ShopItem item = page.getItem(slot);
        if (item == null) {
            player.sendMessage(TextUtil.format("&c해당 슬롯에 아이템이 없습니다."));
            return false;
        }
        if (shop.getTradeType() == TradeType.SELL) {
            player.sendMessage(TextUtil.format("&c이 상점은 판매 전용입니다."));
            return false;
        }
        if (item.getBuyPrice() <= 0) {
            player.sendMessage(TextUtil.format("&c이 아이템은 구매할 수 없습니다."));
            return false;
        }
        if (item.getLimitType() != LimitType.NONE) {
            int remainingLimit = getRemainingLimit(player, item);
            if (remainingLimit < amount) {
                player.sendMessage(TextUtil.format("&c제한 수량을 초과했습니다. (남은 수량: " + remainingLimit + ")"));
                return false;
            }
            updateRemainingLimit(player, item, remainingLimit - amount);
        }
        player.sendMessage(TextUtil.format("&a" + item.getOriginalItem().getType().name() + " " + amount + "개를 구매했습니다."));
        return true;
    }

    // 아이템 판매
    public boolean sellItem(Player player, String shopId, int pageIndex, int slot, int amount) {
        if (isShopLocked(shopId)) {
            player.sendMessage(TextUtil.format("&c이 상점은 현재 편집 중입니다."));
            return false;
        }
        Shop shop = shops.get(shopId);
        if (shop == null) {
            player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
            return false;
        }
        ShopPage page = shop.getPages().get(pageIndex);
        if (page == null) {
            player.sendMessage(TextUtil.format("&c페이지를 찾을 수 없습니다."));
            return false;
        }
        ShopItem item = page.getItem(slot);
        if (item == null) {
            player.sendMessage(TextUtil.format("&c해당 슬롯에 아이템이 없습니다."));
            return false;
        }
        if (shop.getTradeType() == TradeType.BUY) {
            player.sendMessage(TextUtil.format("&c이 상점은 구매 전용입니다."));
            return false;
        }
        if (item.getSellPrice() <= 0) {
            player.sendMessage(TextUtil.format("&c이 아이템은 판매할 수 없습니다."));
            return false;
        }
        if (item.getLimitType() != LimitType.NONE) {
            int remainingLimit = getRemainingLimit(player, item);
            if (remainingLimit < amount) {
                player.sendMessage(TextUtil.format("&c제한 수량을 초과했습니다. (남은 수량: " + remainingLimit + ")"));
                return false;
            }
            updateRemainingLimit(player, item, remainingLimit - amount);
        }
        player.sendMessage(TextUtil.format("&a" + item.getOriginalItem().getType().name() + " " + amount + "개를 판매했습니다."));
        return true;
    }

    // EditorGUI 전용 편집 동작 기록
    public void recordEditorAction(Shop shop, ShopPage page, ShopPage beforeState, EditorGUI editorGUI) {
        int pageIndex = page.getIndex();
        ShopPage currentState = page.clone();

        editorActionStack.push(new ShopEditAction(shop, pageIndex, beforeState, currentState, editorGUI));
        editorRedoStack.clear(); // 새 액션 시 Redo 스택 초기화

        Bukkit.getLogger().info("[ShopManager] EditorGUI 액션 기록 - 페이지: " + pageIndex +
                ", 변경 전 아이템 수: " + beforeState.getItems().size() +
                ", 변경 후 아이템 수: " + currentState.getItems().size());
    }

    // ItemSettingsGUI 전용 아이템 설정 동작 기록
    public void recordItemSettingsAction(ItemSettingsGUI itemSettingsGUI, int oldBuyPrice, int newBuyPrice, int oldSellPrice, int newSellPrice,
                                         LimitType oldLimitType, LimitType newLimitType, int oldLimitAmount, int newLimitAmount) {
        itemSettingsActionStack.push(new ItemSettingsAction(itemSettingsGUI, oldBuyPrice, newBuyPrice, oldSellPrice, newSellPrice,
                oldLimitType, newLimitType, oldLimitAmount, newLimitAmount));
        itemSettingsRedoStack.clear(); // 새 액션 시 Redo 스택 초기화

        Bukkit.getLogger().info("[ShopManager] ItemSettings 액션 기록 - 아이템: " + itemSettingsGUI.getItem().getOriginalItem().getType() +
                ", 구매가격: " + oldBuyPrice + "→" + newBuyPrice +
                ", 판매가격: " + oldSellPrice + "→" + newSellPrice);
    }

    // EditorGUI Undo 동작
    public void undoEditor() {
        if (!editorActionStack.isEmpty()) {
            EditorAction action = editorActionStack.pop();
            action.undo();
            editorRedoStack.push(action);

            Bukkit.getLogger().info("[ShopManager] EditorGUI Undo 실행 완료 - 스택 크기: " + editorActionStack.size());
        } else {
            Bukkit.getLogger().info("[ShopManager] EditorGUI Undo 실행 불가 - 빈 스택");
        }
    }

    // EditorGUI Redo 동작
    public void redoEditor() {
        if (!editorRedoStack.isEmpty()) {
            EditorAction action = editorRedoStack.pop();
            action.redo();
            editorActionStack.push(action);

            Bukkit.getLogger().info("[ShopManager] EditorGUI Redo 실행 완료 - 스택 크기: " + editorRedoStack.size());
        } else {
            Bukkit.getLogger().info("[ShopManager] EditorGUI Redo 실행 불가 - 빈 스택");
        }
    }

    // ItemSettingsGUI Undo 동작
    public void undoItemSettings() {
        if (!itemSettingsActionStack.isEmpty()) {
            EditorAction action = itemSettingsActionStack.pop();
            action.undo();
            itemSettingsRedoStack.push(action);

            Bukkit.getLogger().info("[ShopManager] ItemSettings Undo 실행 완료 - 스택 크기: " + itemSettingsActionStack.size());
        } else {
            Bukkit.getLogger().info("[ShopManager] ItemSettings Undo 실행 불가 - 빈 스택");
        }
    }

    // ItemSettingsGUI Redo 동작
    public void redoItemSettings() {
        if (!itemSettingsRedoStack.isEmpty()) {
            EditorAction action = itemSettingsRedoStack.pop();
            action.redo();
            itemSettingsActionStack.push(action);

            Bukkit.getLogger().info("[ShopManager] ItemSettings Redo 실행 완료 - 스택 크기: " + itemSettingsRedoStack.size());
        } else {
            Bukkit.getLogger().info("[ShopManager] ItemSettings Redo 실행 불가 - 빈 스택");
        }
    }

    // EditorGUI 상태 확인 메서드들
    public boolean canUndoEditor() {
        return !editorActionStack.isEmpty();
    }

    public boolean canRedoEditor() {
        return !editorRedoStack.isEmpty();
    }

    // ItemSettingsGUI 상태 확인 메서드들
    public boolean canUndoItemSettings() {
        return !itemSettingsActionStack.isEmpty();
    }

    public boolean canRedoItemSettings() {
        return !itemSettingsRedoStack.isEmpty();
    }

    // 유효한 슬롯 반환
    public static int[] getValidSlots() {
        return VALID_SLOTS.clone();
    }

    // ShopGUI 열기
    public void openShopGUI(Player player, Shop shop) {
        if (isShopLocked(shop.getShopID())) {
            player.sendMessage(TextUtil.format("&c이 상점은 현재 편집 중입니다."));
            Bukkit.getLogger().info("[ShopManager] " + player.getName() + "(이)가 잠금된 상점 " + shop.getShopID() + "에 접근 시도함");
            return;
        }
        ShopGUI gui = new ShopGUI(this, shop);
        playerShopGUIs.put(player.getUniqueId(), gui);
        gui.open(player);
        Bukkit.getLogger().info("[ShopManager] " + player.getName() + "(이)가 상점 " + shop.getShopID() + "에 접근함");
    }

    // EditorGUI 열기
    public void openEditorGUI(Player player, Shop shop) {
        if (isShopLocked(shop.getShopID())) {
            player.sendMessage(TextUtil.format("&c이 상점은 현재 편집 중입니다."));
            Bukkit.getLogger().info("[ShopManager] 관리자 " + player.getName() + "(이)가 잠금된 상점 " + shop.getShopID() + "에 접근 시도함");
            return;
        }
        lockShop(shop.getShopID(), player.getUniqueId()); // 상점 잠금
        Bukkit.getLogger().info("[ShopManager] 상점 " + shop.getShopID() + "에 잠금 설정됨 ");
        EditorGUI gui = new EditorGUI(this, shop);
        playerEditorGUIs.put(player.getUniqueId(), gui);
        gui.open(player);
        Bukkit.getLogger().info("[ShopManager] 관리자 " + player.getName() + "(이)가 상점 " + shop.getShopID() + "에 접근함");
    }

    // 플레이어의 ShopGUI 조회
    public ShopGUI getShopGUI(Player player) {
        return playerShopGUIs.get(player.getUniqueId());
    }

    public void setShopGUI(Player player, ShopGUI shopGUI) {
        playerShopGUIs.put(player.getUniqueId(), shopGUI);
    }

    // 플레이어의 EditorGUI 조회
    public EditorGUI getEditorGUI(Player player) {
        return playerEditorGUIs.get(player.getUniqueId());
    }

    public void setEditorGUI(Player player, EditorGUI editorGUI) {
        playerEditorGUIs.put(player.getUniqueId(), editorGUI);
    }

    // GUI 닫기 처리
    public void closeEditorGUI(Player player) {
        EditorGUI editorGUI = playerEditorGUIs.remove(player.getUniqueId());
        if (editorGUI != null) {
            unlockShop(editorGUI.getShop().getShopID()); // 상점 잠금 해제
            editorGUI.saveAndRearrange(); // 재정렬
        }
    }

    // 상점 잠금 여부 확인
    public boolean isShopLocked(String shopId) {
        return lockedShops.containsKey(shopId); // HashMap.containsKey: 잠금 상태 확인
    }

    // 상점 잠금
    public void lockShop(String shopId, UUID playerUUID) {
        lockedShops.put(shopId, playerUUID); // HashMap.put: 상점 잠금
    }

    // 상점 잠금 해제
    public void unlockShop(String shopId) {
        lockedShops.remove(shopId); // HashMap.remove: 상점 잠금 해제
    }
}