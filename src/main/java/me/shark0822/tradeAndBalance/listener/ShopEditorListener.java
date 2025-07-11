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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ShopEditorListener implements Listener {
    private final ShopManager shopManager;
    private final DataManager dataManager;
    private final Map<Player, Map<String, Object>> settingStates = new HashMap<>();
    private final Logger logger;
    private final JavaPlugin plugin;

    public static final int[] ITEM_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    public ShopEditorListener(ShopManager shopManager, DataManager dataManager, JavaPlugin plugin) {
        this.shopManager = shopManager;
        this.dataManager = dataManager;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = TextUtil.componentToLegacy(event.getView().title());
        if (title == null) {
            logger.info("[DEBUG] Title is null for player " + player.getName());
            return;
        }

        Inventory inv = event.getClickedInventory();
        if (inv == null) {
            logger.info("[DEBUG] Inventory is null for player " + player.getName());
            return;
        }

        if (title.contains("[편집]")) {
            handleEditInventoryClick(event, player, title, inv);
        } else if (title.contains("아이템 설정")) {
            handleItemSettingsClick(event, player, title, inv);
        }
    }

    private void handleEditInventoryClick(InventoryClickEvent event, Player player, String title, Inventory inv) {
        Shop shop = shopManager.getShopByName(title);
        if (shop == null) {
            logger.info("[DEBUG] Shop not found for title: " + title);
            return;
        }

        int slot = event.getRawSlot();
        ShopPage currentPage = shop.getCurrentPage();

        if (slot < 54 && slot != -999) {
            event.setCancelled(true);
            if (Arrays.stream(ITEM_SLOTS).anyMatch(s -> s == slot)) {
                ItemStack currentItem = event.getCurrentItem();
                ItemStack cursorItem = event.getCursor();
                int itemIndex = Arrays.stream(ITEM_SLOTS).boxed().toList().indexOf(slot);

                if (currentItem != null && !currentItem.equals(GuiUtil.BLACK_GLASS) &&
                        (cursorItem == null || cursorItem.getType() == Material.AIR) &&
                        event.getClick() == ClickType.LEFT) {
                    // 좌클릭: 설정창 열기
                    if (itemIndex >= 0 && itemIndex < currentPage.getItems().size()) {
                        ShopItem shopItem = currentPage.getItem(itemIndex);
                        Map<String, Object> state = new HashMap<>();
                        state.put("shop", shop);
                        state.put("item", shopItem.getOriginalItem());
                        state.put("slot", slot);
                        if (shopItem != null) {
                            state.put("buyPrice", shopItem.getBuyPrice());
                            state.put("sellPrice", shopItem.getSellPrice());
                            state.put("limitType", shopItem.getLimitType());
                            state.put("limitAmount", shopItem.getLimitAmount());
                            logger.info("[DEBUG] Loaded ShopItem data for slot " + slot + ": buyPrice=" + shopItem.getBuyPrice() + ", sellPrice=" + shopItem.getSellPrice() + ", limitType=" + shopItem.getLimitType() + ", limitAmount=" + shopItem.getLimitAmount());
                        } else {
                            state.put("buyPrice", 0);
                            state.put("sellPrice", 0);
                            state.put("limitType", LimitType.NONE);
                            state.put("limitAmount", 0);
                            logger.info("[DEBUG] No ShopItem found at index " + itemIndex + " for slot " + slot + ", using default values");
                        }
                        settingStates.put(player, state);
                        logger.info("[DEBUG] Stored state for player " + player.getName() + ": " + state);
                        EditorGUI.openItemSettings(player, state);
                        logger.info("[DEBUG] Opened item settings for slot " + slot + ", index " + itemIndex);
                    } else {
                        logger.info("[DEBUG] Invalid item index " + itemIndex + " for slot " + slot + ", size=" + currentPage.getItems().size());
                        player.sendMessage(TextUtil.format("&c아이템 데이터를 찾을 수 없습니다."));
                    }
                } else if (currentItem != null && !currentItem.equals(GuiUtil.BLACK_GLASS) &&
                        event.getClick() == ClickType.RIGHT) {
                    // 우클릭: 아이템 제거
                    if (itemIndex >= 0 && itemIndex < currentPage.getItems().size()) {
                        currentPage.removeItem(itemIndex);
                        logger.info("[DEBUG] Removed item from slot " + slot + ", index " + itemIndex);
                        EditorGUI.update(player, shop);
                        player.sendMessage(TextUtil.format("&a아이템이 제거되었습니다."));
                    }
                } else if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    // 커서에 아이템이 있을 때 추가
                    Map<String, Object> state = new HashMap<>();
                    state.put("shop", shop);
                    state.put("item", cursorItem.clone());
                    state.put("slot", slot);
                    state.put("buyPrice", 0);
                    state.put("sellPrice", 0);
                    state.put("limitType", LimitType.NONE);
                    state.put("limitAmount", 0);
                    settingStates.put(player, state);
                    logger.info("[DEBUG] Stored state for new item for player " + player.getName() + ": " + state);
                    EditorGUI.openItemSettings(player, state);
                    logger.info("[DEBUG] Opened item settings for new item at slot " + slot + ", index " + itemIndex);
                }
                return;
            }

            switch (slot) {
                case 45: // 이전 페이지
                    shop.prevPage();
                    EditorGUI.update(player, shop);
                    logger.info("[DEBUG] Moved to previous page, currentIndex=" + shop.getCurrentPageNode().getIndex());
                    break;
                case 53: // 다음 페이지
                    shop.addNextPage();
                    EditorGUI.update(player, shop);
                    logger.info("[DEBUG] Moved to next page, currentIndex=" + shop.getCurrentPageNode().getIndex());
                    break;
                case 48: // Undo
                    player.sendMessage(TextUtil.format("&c미구현"));
                    logger.info("[DEBUG] Undo clicked (unimplemented)");
                    break;
                case 50: // Redo
                    player.sendMessage(TextUtil.format("&c미구현"));
                    logger.info("[DEBUG] Redo clicked (unimplemented)");
                    break;
                case 49: // TradeType 변경
                    TradeType nextType = TradeType.values()[(shop.getTradeType().ordinal() + 1) % TradeType.values().length];
                    shop.setTradeType(nextType);
                    EditorGUI.update(player, shop);
                    logger.info("[DEBUG] Changed TradeType to " + nextType);
                    break;
                default:
                    logger.info("[DEBUG] Unhandled slot " + slot + " in edit inventory for player " + player.getName());
                    break;
            }
        } else {
            // 하단 인벤토리(플레이어 인벤토리) 클릭
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                    (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    int targetSlot = -1;
                    int itemIndex = -1;
                    for (int i = 0; i < ITEM_SLOTS.length; i++) {
                        int slotIndex = ITEM_SLOTS[i];
                        ItemStack itemInSlot = inv.getItem(slotIndex);
                        if (itemInSlot == null || itemInSlot.equals(GuiUtil.BLACK_GLASS)) {
                            targetSlot = slotIndex;
                            itemIndex = i;
                            break;
                        }
                    }

                    if (targetSlot != -1 && itemIndex != -1) {
                        Map<String, Object> state = new HashMap<>();
                        state.put("shop", shop);
                        state.put("item", clickedItem.clone());
                        state.put("slot", targetSlot);
                        state.put("buyPrice", 0);
                        state.put("sellPrice", 0);
                        state.put("limitType", LimitType.NONE);
                        state.put("limitAmount", 0);
                        settingStates.put(player, state);
                        logger.info("[DEBUG] Stored state for shift-click for player " + player.getName() + ": " + state);
                        EditorGUI.openItemSettings(player, state);
                        logger.info("[DEBUG] Opened item settings for shift-click at slot " + targetSlot + ", index " + itemIndex);
                    } else {
                        player.sendMessage(TextUtil.format("&c빈 슬롯이 없습니다."));
                        logger.info("[DEBUG] No empty slot found for shift-click for player " + player.getName());
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    private void handleItemSettingsClick(InventoryClickEvent event, Player player, String title, Inventory inv) {
        int slot = event.getRawSlot();
        logger.info("[DEBUG] Clicked slot in settings inventory: " + slot + " for player " + player.getName());
        player.sendMessage("[DEBUG] Clicked slot: " + slot);
        if (slot >= 27) {
            logger.info("[DEBUG] Clicked player inventory (slot >= 27), ignoring for player " + player.getName());
            player.sendMessage("[DEBUG] 하단 인벤토리 클릭, 무시됨");
            return;
        }

        event.setCancelled(true);
        Map<String, Object> state = settingStates.getOrDefault(player, new HashMap<>());
        if (state.isEmpty()) {
            logger.info("[DEBUG] State is empty for player " + player.getName());
            player.sendMessage(TextUtil.format("&c설정 상태가 없습니다."));
            Shop shop = shopManager.getShopByName(title.replace("아이템 설정", "[편집]"));
            if (shop != null) {
                EditorGUI.open(player, shop);
            } else {
                logger.info("[DEBUG] Failed to find shop for title: " + title.replace("아이템 설정", "[편집]"));
                player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
            }
            return;
        }

        Shop shop = (Shop) state.get("shop");
        if (shop == null) {
            logger.info("[DEBUG] Shop is null for player " + player.getName());
            player.sendMessage(TextUtil.format("&c상점 데이터를 찾을 수 없습니다."));
            Shop fallbackShop = shopManager.getShopByName(title.replace("아이템 설정", "[편집]"));
            if (fallbackShop != null) {
                EditorGUI.open(player, fallbackShop);
            }
            return;
        }

        TradeType tradeType = shop.getTradeType();
        switch (slot) {
            case 10: // 구매 가격 설정
                if (tradeType == TradeType.SELL) {
                    player.sendMessage(TextUtil.format("&c이 상점은 판매 전용입니다. 구매 가격을 설정할 수 없습니다."));
                    logger.info("[DEBUG] Blocked buyPrice setting due to TradeType=SELL for player " + player.getName());
                    break;
                }
                player.closeInventory();
                player.sendMessage(TextUtil.format("&6구매 가격을 채팅으로 입력하세요 (정수, 예: 100)"));
                state.put("setting", "buyPrice");
                settingStates.put(player, state);
                logger.info("[DEBUG] Initiated buyPrice setting for player " + player.getName() + ", state: " + state);
                break;
            case 12: // 판매 가격 설정
                if (tradeType == TradeType.BUY) {
                    player.sendMessage(TextUtil.format("&c이 상점은 구매 전용입니다. 판매 가격을 설정할 수 없습니다."));
                    logger.info("[DEBUG] Blocked sellPrice setting due to TradeType=BUY for player " + player.getName());
                    break;
                }
                player.closeInventory();
                player.sendMessage(TextUtil.format("&6판매 가격을 채팅으로 입력하세요 (정수, 예: 50)"));
                state.put("setting", "sellPrice");
                settingStates.put(player, state);
                logger.info("[DEBUG] Initiated sellPrice setting for player " + player.getName() + ", state: " + state);
                break;
            case 14: // 제한 유형
                LimitType currentLimitType = (LimitType) state.getOrDefault("limitType", LimitType.NONE);
                LimitType nextLimitType = switch (currentLimitType) {
                    case NONE -> LimitType.GLOBAL;
                    case GLOBAL -> LimitType.PERSONAL;
                    case PERSONAL -> LimitType.NONE;
                };
                state.put("limitType", nextLimitType);
                settingStates.put(player, state);
                EditorGUI.openItemSettings(player, state);
                logger.info("[DEBUG] Changed limit type to " + nextLimitType + " for player " + player.getName());
                break;
            case 16: // 제한 수량
                currentLimitType = (LimitType) state.getOrDefault("limitType", LimitType.NONE);
                if (currentLimitType == LimitType.NONE) {
                    player.sendMessage(TextUtil.format("&c수량 제한이 걸려있지 않습니다"));
                    logger.info("[DEBUG] Blocked limit amount setting due to limitType=NONE for player " + player.getName());
                    break;
                }
                player.closeInventory();
                player.sendMessage(TextUtil.format("&c제한 수량을 채팅으로 입력하세요 (정수, 예: 10)"));
                state.put("setting", "limitAmount");
                settingStates.put(player, state);
                logger.info("[DEBUG] Initiated limit amount setting for player " + player.getName() + ", state: " + state);
                break;
            case 18: // Undo
                player.sendMessage(TextUtil.format("&c미구현"));
                logger.info("[DEBUG] Undo clicked (unimplemented) for player " + player.getName());
                break;
            case 19: // Redo
                player.sendMessage(TextUtil.format("&c미구현"));
                logger.info("[DEBUG] Redo clicked (unimplemented) for player " + player.getName());
                break;
            case 25: // 취소
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.closeInventory();
                        settingStates.remove(player);
                        EditorGUI.open(player, shop);
                        player.sendMessage(TextUtil.format("&c아이템 등록이 취소되었습니다."));
                        logger.info("[DEBUG] Cancelled item registration for player " + player.getName());
                        logger.info("[DEBUG] Removed setting state for player " + player.getName());
                    }
                }.runTask(plugin);
                break;
            case 26: // 확인
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            ItemStack item = (ItemStack) state.get("item");
                            if (item != null && item.getType() != Material.AIR) {
                                int buyPrice = (int) state.getOrDefault("buyPrice", 0);
                                int sellPrice = (int) state.getOrDefault("sellPrice", 0);
                                LimitType limitType = (LimitType) state.getOrDefault("limitType", LimitType.NONE);
                                int limitAmount = (int) state.getOrDefault("limitAmount", 0);
                                // TradeType에 따라 가격 유효성 검사
                                if (tradeType == TradeType.BUY && buyPrice <= 0) {
                                    player.sendMessage(TextUtil.format("&c구매 전용 상점은 구매 가격을 설정해야 합니다."));
                                    logger.info("[DEBUG] Invalid buyPrice for TradeType=BUY for player " + player.getName());
                                    return;
                                }
                                if (tradeType == TradeType.SELL && sellPrice <= 0) {
                                    player.sendMessage(TextUtil.format("&c판매 전용 상점은 판매 가격을 설정해야 합니다."));
                                    logger.info("[DEBUG] Invalid sellPrice for TradeType=SELL for player " + player.getName());
                                    return;
                                }
                                if (tradeType == TradeType.BOTH && (buyPrice <= 0 || sellPrice <= 0)) {
                                    player.sendMessage(TextUtil.format("&c동시 거래 상점은 구매 및 판매 가격을 모두 설정해야 합니다."));
                                    logger.info("[DEBUG] Invalid prices for TradeType=BOTH for player " + player.getName());
                                    return;
                                }
                                ShopItem newShopItem = new ShopItem(item.clone(), buyPrice, sellPrice, limitType, limitAmount);
                                int itemIndex = Arrays.stream(ITEM_SLOTS).boxed().toList().indexOf((int) state.get("slot"));
                                logger.info("[DEBUG] Attempting to register item: slot=" + state.get("slot") + ", index=" + itemIndex + ", itemsSize=" + shop.getCurrentPage().getItems().size());
                                if (itemIndex >= 0) {
                                    if (itemIndex < shop.getCurrentPage().getItems().size()) {
                                        shop.getCurrentPage().setItem(itemIndex, newShopItem);
                                    } else {
                                        shop.getCurrentPage().addItem(newShopItem);
                                    }
                                    logger.info("[DEBUG] Registered item: slot=" + state.get("slot") + ", buyPrice=" + buyPrice + ", sellPrice=" + sellPrice + ", limitType=" + limitType + ", limitAmount=" + limitAmount + " for player " + player.getName());
                                    player.sendMessage(TextUtil.format("&a아이템이 등록되었습니다."));
                                } else {
                                    logger.info("[DEBUG] Invalid item index for slot " + state.get("slot") + " for player " + player.getName());
                                    player.sendMessage(TextUtil.format("&c잘못된 슬롯 인덱스입니다."));
                                }
                            } else {
                                logger.info("[DEBUG] Invalid item in state for player " + player.getName());
                                player.sendMessage(TextUtil.format("&c유효하지 않은 아이템입니다."));
                            }
                        } catch (Exception e) {
                            logger.severe("[ERROR] Failed to register item: " + e.getMessage());
                            player.sendMessage(TextUtil.format("&c아이템 등록에 실패했습니다: " + e.getMessage()));
                        } finally {
                            settingStates.remove(player);
                            logger.info("[DEBUG] Removed setting state for player " + player.getName());
                            player.closeInventory();
                            EditorGUI.open(player, shop);
                        }
                    }
                }.runTask(plugin);
                break;
            default:
                logger.info("[DEBUG] Unhandled slot " + slot + " in settings inventory for player " + player.getName());
                player.sendMessage("[DEBUG] 처리되지 않은 슬롯: " + slot);
                break;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = TextUtil.componentToLegacy(event.getView().title());
        if (title == null || !title.contains("[편집]")) return;

        Shop shop = shopManager.getShopByName(title);
        if (shop == null) {
            logger.info("[DEBUG] Shop not found for title: " + title);
            return;
        }

        ShopPage currentPage = shop.getCurrentPage();

        boolean updated = false;
        for (Integer slot : event.getRawSlots()) {
            if (slot < 54 && Arrays.stream(ITEM_SLOTS).anyMatch(s -> s == slot)) {
                event.setCancelled(true);
                int itemIndex = Arrays.stream(ITEM_SLOTS).boxed().toList().indexOf(slot);
                ItemStack newItem = event.getNewItems().get(slot);

                if (newItem != null && newItem.getType() != Material.AIR && itemIndex >= 0) {
                    Map<String, Object> state = new HashMap<>();
                    state.put("shop", shop);
                    state.put("item", newItem.clone());
                    state.put("slot", slot);
                    state.put("buyPrice", 0);
                    state.put("sellPrice", 0);
                    state.put("limitType", LimitType.NONE);
                    state.put("limitAmount", 0);
                    settingStates.put(player, state);
                    logger.info("[DEBUG] Stored state for drag for player " + player.getName() + ": " + state);
                    EditorGUI.openItemSettings(player, state);
                    logger.info("[DEBUG] Opened item settings for drag at slot " + slot + ", index=" + itemIndex + ", amount=" + newItem.getAmount());
                    updated = true;
                }
            }
        }
        if (updated) {
            EditorGUI.update(player, shop);
            player.sendMessage(TextUtil.format("&a아이템 설정창이 열렸습니다."));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = TextUtil.componentToLegacy(event.getView().title());
        if (title == null) {
            logger.info("[DEBUG] Closed inventory with null title for player " + player.getName());
            return;
        }

        if (title.contains("[편집]")) {
            Shop shop = shopManager.getShopByName(title);
            if (shop != null) {
                shop.removeEmptyPages();
                dataManager.saveShops(shopManager);
                player.sendMessage(TextUtil.format("&a상점이 저장되었습니다."));
                logger.info("[DEBUG] Saved shop data for player " + player.getName());
                logger.info("[DEBUG] Shop items: " + shop.getCurrentPage().getItems());
            } else {
                logger.info("[DEBUG] Shop not found for title: " + title);
            }
        } else {
            logger.info("[DEBUG] Closed inventory with title: " + title + " for player " + player.getName());
            if (settingStates.containsKey(player)) {
                logger.info("[DEBUG] Preserved setting state for player " + player.getName() + ": " + settingStates.get(player));
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Map<String, Object> state = settingStates.get(player);
        if (state == null || !state.containsKey("setting")) return;

        String setting = (String) state.get("setting");
        String message = event.getMessage().trim();
        event.setCancelled(true);

        // 취소 입력 처리
        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("취소")) {
            String cancelMessage;
            String debugMessage;
            switch (setting) {
                case "buyPrice":
                    cancelMessage = "&c구매 가격 설정이 취소되었습니다.";
                    debugMessage = "Cancelled buyPrice setting for player " + player.getName();
                    break;
                case "sellPrice":
                    cancelMessage = "&c판매 가격 설정이 취소되었습니다.";
                    debugMessage = "Cancelled sellPrice setting for player " + player.getName();
                    break;
                case "limitAmount":
                    cancelMessage = "&c제한 수량 설정이 취소되었습니다.";
                    debugMessage = "Cancelled limitAmount setting for player " + player.getName();
                    break;
                default:
                    cancelMessage = "&c설정이 취소되었습니다.";
                    debugMessage = "Cancelled unknown setting for player " + player.getName();
                    break;
            }
            state.remove("setting");
            settingStates.put(player, state);

            final String finalCancelMessage = cancelMessage;
            final String finalDebugMessage = debugMessage;
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.closeInventory();
                    EditorGUI.openItemSettings(player, state);
                    player.sendMessage(TextUtil.format(finalCancelMessage));
                    logger.info("[DEBUG] " + finalDebugMessage + ", returning to item settings");
                }
            }.runTask(plugin);
            return;
        }

        try {
            if (setting.equals("buyPrice")) {
                int buyPrice = Integer.parseInt(message);
                if (buyPrice < 0) {
                    player.sendMessage(TextUtil.format("&c구매 가격은 0 이상이어야 합니다."));
                    logger.info("[DEBUG] Invalid buyPrice " + message + " for player " + player.getName());
                    return;
                }
                state.put("buyPrice", buyPrice);
                player.sendMessage(TextUtil.format("&a구매 가격이 " + buyPrice + "으로 설정되었습니다."));
                logger.info("[DEBUG] Set buyPrice to " + buyPrice + " for player " + player.getName());
            } else if (setting.equals("sellPrice")) {
                int sellPrice = Integer.parseInt(message);
                if (sellPrice < 0) {
                    player.sendMessage(TextUtil.format("&c판매 가격은 0 이상이어야 합니다."));
                    logger.info("[DEBUG] Invalid sellPrice " + message + " for player " + player.getName());
                    return;
                }
                state.put("sellPrice", sellPrice);
                player.sendMessage(TextUtil.format("&a판매 가격이 " + sellPrice + "으로 설정되었습니다."));
                logger.info("[DEBUG] Set sellPrice to " + sellPrice + " for player " + player.getName());
            } else if (setting.equals("limitAmount")) {
                int limitAmount = Integer.parseInt(message);
                if (limitAmount < 0) {
                    player.sendMessage(TextUtil.format("&c제한 수량은 0 이상이어야 합니다."));
                    logger.info("[DEBUG] Invalid limit amount " + message + " for player " + player.getName());
                    return;
                }
                state.put("limitAmount", limitAmount);
                player.sendMessage(TextUtil.format("&a제한 수량이 " + limitAmount + "으로 설정되었습니다."));
                logger.info("[DEBUG] Set limit amount to " + limitAmount + " for player " + player.getName());
            }
            state.remove("setting");
            settingStates.put(player, state);
            logger.info("[DEBUG] Updated state for player " + player.getName() + ": " + state);
            new BukkitRunnable() {
                @Override
                public void run() {
                    EditorGUI.openItemSettings(player, state);
                }
            }.runTask(plugin);
        } catch (NumberFormatException e) {
            player.sendMessage(TextUtil.format("&c유효한 숫자를 입력하세요."));
            logger.info("[DEBUG] Invalid number format: " + message + " for player " + player.getName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    EditorGUI.openItemSettings(player, state);
                }
            }.runTask(plugin);
        }
    }
}