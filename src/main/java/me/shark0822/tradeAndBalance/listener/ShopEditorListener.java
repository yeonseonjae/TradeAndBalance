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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ShopEditorListener implements Listener {
    private final ShopManager shopManager;
    private final DataManager dataManager;

    private static final int[] ITEM_SLOTS = {
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
            event.setCancelled(true);
            // 아이템 슬롯 클릭
            if (Arrays.stream(ITEM_SLOTS).anyMatch(s -> s == slot)) {
                ItemStack currentItem = event.getCurrentItem();
                ItemStack cursorItem = event.getCursor();
                int itemIndex = Arrays.stream(ITEM_SLOTS).boxed().toList().indexOf(slot);

                if (currentItem != null && !currentItem.equals(GuiUtil.BLACK_GLASS) && (cursorItem == null || cursorItem.getType() == Material.AIR)) {
                    // 아이템 제거: 클릭한 슬롯에 아이템이 있고, 커서가 비어 있음
                    if (itemIndex >= 0 && itemIndex < currentPage.getItems().size()) {
                        currentPage.getItems().remove(itemIndex);
                        System.out.println("[DEBUG] Removed item from slot " + slot + ", index " + itemIndex + ", clickType=" + event.getClick());
                        EditorGUI.update(player, shop);
                        player.sendMessage(TextUtil.format("&GREEN아이템이 제거되었습니다."));
                    }
                } else if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    // 아이템 추가: 커서에 아이템이 있음 (일반 좌클릭, 우클릭 등)
                    if (itemIndex >= 0) {
                        ShopItem newShopItem = new ShopItem(cursorItem.clone(), 0.0, LimitType.NONE, 0);
                        if (itemIndex < currentPage.getItems().size()) {
                            currentPage.getItems().set(itemIndex, newShopItem);
                        } else {
                            currentPage.getItems().add(newShopItem);
                        }
                        System.out.println("[DEBUG] Added item to slot " + slot + ", index " + itemIndex + ", clickType=" + event.getClick());
                        EditorGUI.update(player, shop);
                        player.sendMessage(TextUtil.format("&GREEN아이템이 추가되었습니다."));
                    }
                }
                return;
            }

            switch (slot) {
                case 45: // 이전 페이지
                    shop.prevPage();
                    EditorGUI.update(player, shop);
                    break;
                case 53: // 다음 페이지
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
                    TradeType nextType = TradeType.values()[(shop.getTradeType().ordinal() + 1) % TradeType.values().length];
                    shop.setTradeType(nextType);
                    EditorGUI.update(player, shop);
                    break;
                default:
                    break;
            }
        } else {
            // 하단 인벤토리(플레이어 인벤토리) 클릭
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                    (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
                // 쉬프트 클릭으로 상단 인벤토리로 아이템 이동
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    // 빈 아이템 슬롯 찾기
                    int targetSlot = -1;
                    int itemIndex = -1;
                    for (int i = 0; i < ITEM_SLOTS.length; i++) {
                        int slotIndex = ITEM_SLOTS[i];
                        ItemStack itemInSlot = event.getInventory().getItem(slotIndex);
                        if (itemInSlot == null || itemInSlot.equals(GuiUtil.BLACK_GLASS)) {
                            targetSlot = slotIndex;
                            itemIndex = i;
                            break;
                        }
                    }

                    if (targetSlot != -1 && itemIndex != -1) {
                        // 아이템 추가
                        ShopItem newShopItem = new ShopItem(clickedItem.clone(), 0.0, LimitType.NONE, 0);
                        if (itemIndex < currentPage.getItems().size()) {
                            currentPage.getItems().set(itemIndex, newShopItem);
                        } else {
                            currentPage.getItems().add(newShopItem);
                        }
                        EditorGUI.update(player, shop);
                        player.sendMessage(TextUtil.format("&GREEN아이템이 추가되었습니다."));
                    } else {
                        System.out.println("[DEBUG] No empty item slot found for shift-click at slot " + slot);
                        player.sendMessage(TextUtil.format("&c빈 슬롯이 없습니다."));
                    }
                    event.setCancelled(true); // 상단 인벤토리 변경 방지
                }
            } else {
                // 기타 하단 인벤토리 클릭: 허용
                System.out.println("[DEBUG] Clicked player inventory at slot " + slot + ", action allowed, clickType=" + event.getClick());
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
            shop.removeEmptyPages();
            dataManager.saveShops(shopManager);
            player.sendMessage(TextUtil.format("&GREEN상점이 저장되었습니다."));
        }
    }
}