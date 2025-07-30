package me.shark0822.tradeAndBalance.listener;

import me.shark0822.tradeAndBalance.TradeAndBalance;
import me.shark0822.tradeAndBalance.gui.EditorGUI;
import me.shark0822.tradeAndBalance.gui.ItemSettingsGUI;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.ShopPage;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EditorListener implements Listener {
    private final ShopManager shopManager;

    public EditorListener(ShopManager shopManager) {
        this.shopManager = shopManager;

        // 디버그 메시지: 리스너 초기화
        Bukkit.getLogger().info("[EditorListener] EditorListener 초기화 완료");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        EditorGUI editorGUI = shopManager.getEditorGUI(player);

        if (editorGUI == null || !event.getInventory().equals(editorGUI.getInventory())) {
            return;
        }

        int slot = event.getRawSlot();
        boolean isValidSlot = Arrays.stream(EditorGUI.ITEM_SLOTS).anyMatch(s -> s == slot);
        boolean isBlockedSlot = Arrays.stream(EditorGUI.BLOCKED_SLOTS).anyMatch(s -> s == slot);

        // 디버그 메시지: 클릭 이벤트 기본 정보
        Bukkit.getLogger().info("[EditorListener] 클릭 이벤트 - 플레이어: " + player.getName() +
                ", 슬롯: " + slot +
                ", 클릭 타입: " + event.getClick() +
                ", 액션: " + event.getAction() +
                ", 유효 슬롯: " + isValidSlot +
                ", 차단 슬롯: " + isBlockedSlot +
                ", 인벤토리 액션: " + event.getAction());

        if (isValidSlot || isBlockedSlot || slot == 45 || slot == 48 || slot == 49 || slot == 50 || slot == 53) {
            event.setCancelled(true);

            if (isBlockedSlot) {
                Bukkit.getLogger().info("[EditorListener] 차단된 슬롯 클릭 - 아무 동작 안 함: " + slot);
                return;
            }

            if (slot == 45) { // 이전 페이지
                Bukkit.getLogger().info("[EditorListener] 이전 페이지 버튼 클릭 - " + player.getName());
                editorGUI.prevPage();

            } else if (slot == 53) { // 다음 페이지
                Bukkit.getLogger().info("[EditorListener] 다음 페이지 버튼 클릭 - " + player.getName());

                if (editorGUI.getShop().nextIsNone()) {
                    Bukkit.getLogger().info("[EditorListener] 새 페이지 생성 - 페이지 인덱스: " + editorGUI.getShop().getNextPageIndex());
                    editorGUI.getShop().addPage(new ShopPage(editorGUI.getShop().getNextPageIndex()));
                }
                editorGUI.nextPage();

            } else if (slot == 48) { // Undo
                Bukkit.getLogger().info("[EditorListener] Undo 버튼 클릭 - " + player.getName());
                if (shopManager.canUndoEditor()) {
                    shopManager.undoEditor();
                    player.sendMessage(TextUtil.format("&a실행 취소가 완료되었습니다."));
                } else {
                    player.sendMessage(TextUtil.format("&c실행 취소할 작업이 없습니다."));
                }

            } else if (slot == 50) { // Redo
                Bukkit.getLogger().info("[EditorListener] Redo 버튼 클릭 - " + player.getName());
                if (shopManager.canRedoEditor()) {
                    shopManager.redoEditor();
                    player.sendMessage(TextUtil.format("&a다시 실행이 완료되었습니다."));
                } else {
                    player.sendMessage(TextUtil.format("&c다시 실행할 작업이 없습니다."));
                }
            } else if (slot == 49) { // 거래 모드 변경
                Bukkit.getLogger().info("[EditorListener] 거래 모드 변경 버튼 클릭 - " + player.getName());
                editorGUI.toggleTradeMode();

            } else if (isValidSlot) {
                // 아이템 등록 (LEFT 클릭, 커서에 아이템)
                if (event.getClick() == ClickType.LEFT &&
                        event.getCursor() != null && !event.getCursor().getType().isAir()) {
                    Bukkit.getLogger().info("[EditorListener] 아이템 등록 시도 - 슬롯: " + slot +
                            ", 커서 아이템: " + event.getCursor().getType());

                    ShopItem shopItem = editorGUI.addItem(slot, event.getCursor().clone());
                    if (shopItem != null) {
                        Bukkit.getLogger().info("[EditorListener] 아이템 등록 후 ItemSettingsGUI 열기 - " + player.getName());

                        ItemSettingsGUI itemSettingsGUI = new ItemSettingsGUI(shopManager, shopItem, editorGUI);
                        ItemSettingsListener.setItemSettingsGUI(player, itemSettingsGUI);
                        itemSettingsGUI.open(player);

                        Bukkit.getLogger().info("[EditorListener] ItemSettingsGUI 열기 완료 - " + player.getName());
                    } else {
                        Bukkit.getLogger().warning("[EditorListener] 아이템 등록 실패 - " + player.getName());
                    }
                }

                // 아이템 설정 (Shift + Left Click)
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    Bukkit.getLogger().info("[EditorListener] 기존 아이템 설정 열기 시도 - 슬롯: " + slot);

                    ShopItem item = editorGUI.getShop().getCurrentPage().getItem(slot);
                    if (item != null) {
                        Bukkit.getLogger().info("[EditorListener] 기존 아이템 ItemSettingsGUI 열기 - " + player.getName());

                        ItemSettingsGUI itemSettingsGUI = new ItemSettingsGUI(shopManager, item, editorGUI);
                        ItemSettingsListener.setItemSettingsGUI(player, itemSettingsGUI);
                        itemSettingsGUI.open(player);

                        Bukkit.getLogger().info("[EditorListener] 기존 아이템 ItemSettingsGUI 열기 완료 - " + player.getName());
                    } else {
                        Bukkit.getLogger().info("[EditorListener] 설정할 아이템이 없음 - 슬롯: " + slot);
                    }
                }

                // 아이템 삭제 (Shift + Right Click)
                if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    Bukkit.getLogger().info("[EditorListener] 아이템 삭제 시도 - 슬롯: " + slot);

                    ShopItem item = editorGUI.getShop().getCurrentPage().getItem(slot);
                    if (item != null) {
                        editorGUI.removeItem(slot);
                        // 뒷슬롯에 아이템이 있는지 확인
                        boolean hasLaterItems = false;
                        for (int validSlot : EditorGUI.ITEM_SLOTS) {
                            if (validSlot > slot && editorGUI.getShop().getCurrentPage().getItem(validSlot) != null) {
                                hasLaterItems = true;
                                break;
                            }
                        }
                        // 다른 페이지에도 아이템이 있는지 확인
                        for (ShopPage page : editorGUI.getShop().getPages().getAll()) {
                            if (page.getIndex() > editorGUI.getShop().getCurrentPage().getIndex() && !page.isEmpty()) {
                                hasLaterItems = true;
                                break;
                            }
                        }
                        if (hasLaterItems) {
                            Bukkit.getLogger().info("[EditorListener] 뒷슬롯 또는 뒷페이지에 아이템 존재, 재정렬 수행 - " + player.getName());
                            editorGUI.saveAndRearrange();
                        } else {
                            Bukkit.getLogger().info("[EditorListener] 뒷슬롯 또는 뒷페이지에 아이템 없음, 재정렬 생략 - " + player.getName());
                        }
                        Bukkit.getLogger().info("[EditorListener] 아이템 삭제 완료 - 슬롯: " + slot +
                                ", 제거된 아이템: " + item.getOriginalItem().getType() +
                                ", custom_name: " + (item.getOriginalItem().getItemMeta() != null ? item.getOriginalItem().getItemMeta().displayName() : "none"));
                    } else {
                        Bukkit.getLogger().info("[EditorListener] 삭제할 아이템이 없음 - 슬롯: " + slot);
                    }
                }
            }
        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
            // 플레이어 인벤토리에서 쉬프트 클릭으로 아이템 등록
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType().isAir()) {
                Bukkit.getLogger().info("[EditorListener] 쉬프트 클릭으로 등록 시도 - 아이템이 null이거나 공기");
                return;
            }

            // 첫 번째 빈 슬롯 찾기
            int targetSlot = -1;
            for (int validSlot : EditorGUI.ITEM_SLOTS) {
                if (editorGUI.getShop().getCurrentPage().getItem(validSlot) == null) {
                    targetSlot = validSlot;
                    break;
                }
            }

            if (targetSlot == -1) {
                player.sendMessage(TextUtil.format("&c빈 슬롯이 없습니다."));
                Bukkit.getLogger().info("[EditorListener] 쉬프트 클릭 등록 실패 - 빈 슬롯 없음");
                return;
            }

            Bukkit.getLogger().info("[EditorListener] 쉬프트 클릭으로 아이템 등록 시도 - 슬롯: " + targetSlot +
                    ", 아이템: " + item.getType());

            ShopItem shopItem = editorGUI.addItem(targetSlot, item.clone());
            if (shopItem != null) {
                Bukkit.getLogger().info("[EditorListener] 쉬프트 클릭으로 아이템 등록 후 ItemSettingsGUI 열기 - " + player.getName());

                ItemSettingsGUI itemSettingsGUI = new ItemSettingsGUI(shopManager, shopItem, editorGUI);
                itemSettingsGUI.open(player);
                ItemSettingsListener.setItemSettingsGUI(player, itemSettingsGUI);

                Bukkit.getLogger().info("[EditorListener] ItemSettingsGUI 열기 완료 - " + player.getName());
            } else {
                Bukkit.getLogger().warning("[EditorListener] 쉬프트 클릭으로 아이템 등록 실패 - " + player.getName());
            }
        } else {
            Bukkit.getLogger().info("[EditorListener] 처리되지 않은 슬롯 클릭 - 슬롯: " + slot + ", 플레이어: " + player.getName());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        EditorGUI editorGUI = shopManager.getEditorGUI(player);

        if (editorGUI == null || !event.getInventory().equals(editorGUI.getInventory())) {
            return;
        }

        boolean affectsValidSlot = event.getInventorySlots().stream()
                .anyMatch(slot -> Arrays.stream(EditorGUI.ITEM_SLOTS).anyMatch(s -> s == slot));

        // 디버그 메시지: 드래그 이벤트
        Bukkit.getLogger().info("[EditorListener] 드래그 이벤트 - 플레이어: " + player.getName() +
                ", 영향받는 슬롯: " + event.getInventorySlots() +
                ", 유효 슬롯 영향: " + affectsValidSlot);

        if (affectsValidSlot) {
            event.setCancelled(true);
            Bukkit.getLogger().info("[EditorListener] 유효 슬롯 드래그 차단 - 아이템 추가 안 함");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        EditorGUI editorGUI = shopManager.getEditorGUI(player);

        if (editorGUI == null || !event.getInventory().equals(editorGUI.getInventory())) {
            return;
        }

        // ItemSettingsGUI가 열려있는지 확인
        ItemSettingsListener listener = TradeAndBalance.getPlugin(TradeAndBalance.class).getItemSettingsListener();
        if (listener.hasItemSettingsGUI(player)) {
            Bukkit.getLogger().info("[EditorListener] ItemSettingsGUI 편집 중이므로 상점 잠금 유지 - " + player.getName());
            return; // ItemSettingsGUI 편집 중이면 unlockShop 하지 않음
        }

        Bukkit.getLogger().info("[EditorListener] EditorGUI 완전 종료 - 상점 잠금 해제");
        shopManager.closeEditorGUI(player);
    }
}