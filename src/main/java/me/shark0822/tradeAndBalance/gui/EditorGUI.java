package me.shark0822.tradeAndBalance.gui;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.util.GuiUtil;
import me.shark0822.tradeAndBalance.util.ItemUtil;
import me.shark0822.tradeAndBalance.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditorGUI {
    public static final int[] ITEM_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    public static final ItemStack UNDO_BTN = ItemUtil.createItem(Material.PAPER, TextUtil.format("&WHITE실행 취소"), List.of(TextUtil.format("&GRAY이전 동작을 취소합니다")));
    public static final ItemStack REDO_BTN = ItemUtil.createItem(Material.PAPER, TextUtil.format("&WHITE다시 실행"), List.of(TextUtil.format("&GRAY취소된 동작을 다시 실행합니다")));
    public static final ItemStack CANCEL_BTN = ItemUtil.createItem(Material.RED_CONCRETE, TextUtil.format("&RED취소"), List.of(TextUtil.format("&GRAY아이템 등록을 취소합니다")));
    public static final ItemStack CONFIRM_BTN = ItemUtil.createItem(Material.LIME_CONCRETE, TextUtil.format("&GREEN확인"), List.of(TextUtil.format("&GRAY아이템을 등록합니다")));

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
            inv.setItem(ITEM_SLOTS[i], items.get(i).getDisplayItem(true, shop.getTradeType()));
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
            inv.setItem(itemSlots[i], items.get(i).getDisplayItem(true, shop.getTradeType()));
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
                ShopItem newShopItem = new ShopItem(item.clone(), 0, 0, LimitType.NONE, 0);
                page.addItem(newShopItem);
                System.out.println("[DEBUG] Synced item to slot " + ITEM_SLOTS[i] + ", index " + i);
            }
        }
    }

    public static void openItemSettings(Player player, Map<String, Object> state) {
        ItemStack item = (ItemStack) state.get("item");
        if (item == null || item.getType() == Material.AIR || item.equals(GuiUtil.BLACK_GLASS)) {
            return;
        }

        Inventory settingsInv = Bukkit.createInventory(player, 27, TextUtil.format("&8아이템 설정"));
        int[] blankSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 23, 24};
        for (int blankSlot : blankSlots) {
            settingsInv.setItem(blankSlot, GuiUtil.BLACK_GLASS);
        }

        LimitType limitType = (LimitType) state.getOrDefault("limitType", LimitType.NONE);
        int buyPrice = (int) state.getOrDefault("buyPrice", 0);
        int sellPrice = (int) state.getOrDefault("sellPrice", 0);
        int limitAmount = (int) state.getOrDefault("limitAmount", 0);

        ItemStack buyPriceItem = ItemUtil.createItem(Material.GOLD_INGOT, TextUtil.format("&GREEN구매 가격 설정"), List.of(TextUtil.format("&GRAY클릭하여 가격을 입력하세요 (현재 : " + state.getOrDefault("buyPrice", 0) + ")")));
        ItemStack sellPriceItem = ItemUtil.createItem(Material.EMERALD, TextUtil.format("&RED판매 가격 설정"), List.of(TextUtil.format("&GRAY클릭하여 가격을 입력하세요 (현재 : " + state.getOrDefault("sellPrice", 0) + ")")));
        ItemStack limitAmountItem = ItemUtil.createItem(Material.HOPPER, TextUtil.format("&GOLD제한 수량 설정"), List.of(TextUtil.format("&GRAY클릭하여 제한 수량을 입력하세요 (현재 : " + state.getOrDefault("limitAmount", 0) + ")")));

        ItemStack displayItem = item.clone();
        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            // 원본 아이템의 사용자 정의 lore 유지
            if (meta.hasLore()) {
                lore.addAll(meta.lore());
            }
            // 가격 및 제한 정보 추가 (ShopItem.getEditorLore와 유사)
            lore.add(TextUtil.format("&GOLD[ 가격 정보 ]"));
            lore.add(TextUtil.format("&WHITE→ 구매 : " + buyPrice + "원"));
            lore.add(TextUtil.format("&WHITE→ 판매 : " + sellPrice + "원"));
            lore.add(TextUtil.format("")); // 빈 줄
            lore.add(TextUtil.format("&GOLD[ 수량 제한 ]"));
            lore.add(TextUtil.format("&WHITE→ 제한 : " + switch (limitType) {
                case GLOBAL -> "&BLUE플레이어 전체";
                case PERSONAL -> "&YELLOW플레이어 개인";
                default -> "&GREEN제한 없음";
            }));
            lore.add(TextUtil.format("&WHITE→ 수량 : " + limitAmount));
            meta.lore(lore);
            displayItem.setItemMeta(meta);
        }

        settingsInv.setItem(10, buyPriceItem);
        settingsInv.setItem(12, sellPriceItem);
        settingsInv.setItem(14, createLimitTypeIcon(limitType));
        settingsInv.setItem(16, limitAmountItem);
        settingsInv.setItem(18, UNDO_BTN);
        settingsInv.setItem(19, REDO_BTN);
        settingsInv.setItem(22, displayItem);
        settingsInv.setItem(25, CANCEL_BTN);
        settingsInv.setItem(26, CONFIRM_BTN);

        player.openInventory(settingsInv);
    }

    public static ItemStack createLimitTypeIcon(LimitType limitType) {
        Material material;
        Component name;
        switch(limitType) {
            case NONE -> {
                material = Material.LIME_STAINED_GLASS_PANE;
                name = TextUtil.format("&GREEN제한 없음");
            }
            case GLOBAL -> {
                material = Material.BLUE_STAINED_GLASS_PANE;
                name = TextUtil.format("&BLUE플레이어 전체");
            }
            case PERSONAL -> {
                material = Material.YELLOW_STAINED_GLASS_PANE;
                name = TextUtil.format("&YELLOW플레이어 개인");
            }
            default -> {
                material = Material.GREEN_STAINED_GLASS_PANE;
                name = Component.text("&GRAY알 수 없음");
            }
        }
        return ItemUtil.createItem(material, name, List.of(TextUtil.format("&7클릭하여 모드 변경")));
    }
}
