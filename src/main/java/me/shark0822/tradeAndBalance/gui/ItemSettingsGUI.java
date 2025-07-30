package me.shark0822.tradeAndBalance.gui;

import me.shark0822.tradeAndBalance.TradeAndBalance;
import me.shark0822.tradeAndBalance.listener.ItemSettingsListener;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.ShopManager;
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
import java.util.Objects;

public class ItemSettingsGUI {
    private final ShopManager shopManager;
    private final ShopItem item;
    private final EditorGUI editorGUI;
    private final Inventory inventory;

    private final int originalBuyPrice;
    private final int originalSellPrice;
    private final LimitType originalLimitType;
    private final int originalLimitAmount;

    private int tempBuyPrice;
    private int tempSellPrice;
    private LimitType tempLimitType;
    private int tempLimitAmount;

    private static final int[] BLANK_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 20, 21, 23, 24};
    private static final ItemStack CANCEL_BTN = ItemUtil.createItem(Material.BARRIER, TextUtil.format("&c취소"));
    private static final ItemStack CONFIRM_BTN = ItemUtil.createItem(Material.EMERALD, TextUtil.format("&a확인"));

    public ItemSettingsGUI(ShopManager shopManager, ShopItem item, EditorGUI editorGUI) {
        this.shopManager = shopManager;
        this.item = item;
        this.editorGUI = editorGUI;

        this.originalBuyPrice = item.getBuyPrice();
        this.originalSellPrice = item.getSellPrice();
        this.originalLimitType = item.getLimitType();
        this.originalLimitAmount = item.getLimitAmount();

        this.tempBuyPrice = originalBuyPrice;
        this.tempSellPrice = originalSellPrice;
        this.tempLimitType = originalLimitType;
        this.tempLimitAmount = originalLimitAmount;

        this.inventory = Bukkit.createInventory(null, 27, TextUtil.format("&8아이템 설정"));
        updateInventory();
    }

    public void updateInventory() {
        inventory.clear();
        for (int slot : BLANK_SLOTS) {
            inventory.setItem(slot, GuiUtil.BLACK_GLASS);
        }

        ItemStack buyPriceItem = ItemUtil.createItem(Material.GOLD_INGOT, TextUtil.format("&a구매 가격 설정"),
                List.of(TextUtil.format("&7클릭하여 가격을 입력하세요"),
                        TextUtil.format("&7현재: " + tempBuyPrice + "원"),
                        (tempBuyPrice != originalBuyPrice) ? TextUtil.format("&e(변경됨: " + originalBuyPrice + " → " + tempBuyPrice + ")") : TextUtil.format("&7원본: " + originalBuyPrice + "원")));

        ItemStack sellPriceItem = ItemUtil.createItem(Material.EMERALD, TextUtil.format("&c판매 가격 설정"),
                List.of(TextUtil.format("&7클릭하여 가격을 입력하세요"),
                        TextUtil.format("&7현재: " + tempSellPrice + "원"),
                        (tempSellPrice != originalSellPrice) ? TextUtil.format("&e(변경됨: " + originalSellPrice + " → " + tempSellPrice + ")") : TextUtil.format("&7원본: " + originalSellPrice + "원")));

        ItemStack limitAmountItem = ItemUtil.createItem(Material.HOPPER, TextUtil.format("&e제한 수량 설정"),
                List.of(TextUtil.format("&7클릭하여 제한 수량을 입력하세요"),
                        TextUtil.format("&7현재: " + tempLimitAmount),
                        (tempLimitAmount != originalLimitAmount) ? TextUtil.format("&e(변경됨: " + originalLimitAmount + " → " + tempLimitAmount + ")") : TextUtil.format("&7원본: " + originalLimitAmount)));

        ItemStack limitTypeItem = createLimitTypeIcon(tempLimitType);

        ItemStack displayItem = item.getOriginalItem().clone();
        updateDisplayItemLore(displayItem);

        inventory.setItem(10, buyPriceItem);
        inventory.setItem(12, sellPriceItem);
        inventory.setItem(14, limitTypeItem);
        inventory.setItem(16, limitAmountItem);
        inventory.setItem(18, GuiUtil.UNDO_BTN);
        inventory.setItem(19, GuiUtil.REDO_BTN);
        inventory.setItem(22, displayItem);
        inventory.setItem(25, CANCEL_BTN);
        inventory.setItem(26, CONFIRM_BTN);
    }

    private void updateDisplayItemLore(ItemStack displayItem) {
        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore.addAll(Objects.requireNonNull(meta.lore()));
                lore.add(TextUtil.format(""));
            }

            lore.add(TextUtil.format("&6[ 가격 정보 ]"));
            lore.add(TextUtil.format("&f→ 구매: " + tempBuyPrice + "원" + (tempBuyPrice != originalBuyPrice ? " &e(변경됨)" : "")));
            lore.add(TextUtil.format("&f→ 판매: " + tempSellPrice + "원" + (tempSellPrice != originalSellPrice ? " &e(변경됨)" : "")));
            lore.add(TextUtil.format(""));
            lore.add(TextUtil.format("&6[ 수량 제한 ]"));
            lore.add(TextUtil.format("&f→ 제한: " + switch (tempLimitType) {
                case GLOBAL -> "&b플레이어 전체";
                case PERSONAL -> "&e플레이어 개인";
                default -> "&a제한 없음";
            } + (tempLimitType != originalLimitType ? " &e(변경됨)" : "")));
            lore.add(TextUtil.format("&f→ 수량: " + tempLimitAmount + (tempLimitAmount != originalLimitAmount ? " &e(변경됨)" : "")));

            if (hasChanges()) {
                lore.add(TextUtil.format(""));
                lore.add(TextUtil.format("&e변경 사항이 있습니다"));
                lore.add(TextUtil.format("&7확인을 눌러 저장하세요"));
            }

            meta.lore(lore);
            displayItem.setItemMeta(meta);
        }
    }

    public void open(Player player) {
        updateInventory();
        player.openInventory(inventory);
    }

    public void startSetting(Player player, String setting) {
        player.closeInventory();
        player.sendMessage(TextUtil.format("&a" + setting + " 값을 입력하세요:"));
        player.sendMessage(TextUtil.format("&7현재 값: " + getCurrentValue(setting)));
        player.sendMessage(TextUtil.format("&7취소하려면 'cancel'을 입력하세요"));
    }

    private String getCurrentValue(String setting) {
        return switch (setting) {
            case "구매 가격" -> String.valueOf(tempBuyPrice);
            case "판매 가격" -> String.valueOf(tempSellPrice);
            case "제한 수량" -> String.valueOf(tempLimitAmount);
            default -> "";
        };
    }

    public void applySetting(String setting, String value) {
        if ("cancel".equalsIgnoreCase(value.trim())) {
            return;
        }

        try {
            switch (setting) {
                case "구매 가격" -> {
                    int newPrice = Integer.parseInt(value);
                    if (newPrice < 0) throw new NumberFormatException();
                    shopManager.recordItemSettingsAction(this, tempBuyPrice, newPrice, tempSellPrice, tempSellPrice,
                            tempLimitType, tempLimitType, tempLimitAmount, tempLimitAmount);
                    tempBuyPrice = newPrice;
                }
                case "판매 가격" -> {
                    int newPrice = Integer.parseInt(value);
                    if (newPrice < 0) throw new NumberFormatException();
                    shopManager.recordItemSettingsAction(this, tempBuyPrice, tempBuyPrice, tempSellPrice, newPrice,
                            tempLimitType, tempLimitType, tempLimitAmount, tempLimitAmount);
                    tempSellPrice = newPrice;
                }
                case "제한 수량" -> {
                    int newAmount = Integer.parseInt(value);
                    if (newAmount < 0) throw new NumberFormatException();
                    shopManager.recordItemSettingsAction(this, tempBuyPrice, tempBuyPrice, tempSellPrice, tempSellPrice,
                            tempLimitType, tempLimitType, tempLimitAmount, newAmount);
                    tempLimitAmount = newAmount;
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 숫자 입력입니다.");
        }
    }

    public void confirm(Player player) {
        Bukkit.getLogger().info("[ItemSettingsGUI] 확인 처리 시작 - " + player.getName() +
                ", 변경사항 있음: " + hasChanges());

        if (hasChanges()) {
            Bukkit.getLogger().info("[ItemSettingsGUI] 변경사항 적용 - 구매가격: " + originalBuyPrice + "→" + tempBuyPrice +
                    ", 판매가격: " + originalSellPrice + "→" + tempSellPrice +
                    ", 제한타입: " + originalLimitType + "→" + tempLimitType +
                    ", 제한수량: " + originalLimitAmount + "→" + tempLimitAmount);

            item.setBuyPrice(tempBuyPrice);
            item.setSellPrice(tempSellPrice);
            item.setLimitType(tempLimitType);
            item.setLimitAmount(tempLimitAmount);

            player.sendMessage(TextUtil.format("&a아이템 설정이 저장되었습니다."));
            Bukkit.getLogger().info("[ItemSettingsGUI] 변경사항 적용 완료 - " + player.getName());
        }

        // ItemSettingsGUI 매핑 제거
        ItemSettingsListener listener = TradeAndBalance.getPlugin(TradeAndBalance.class).getItemSettingsListener();
        listener.removeItemSettingsGUI(player);
        Bukkit.getLogger().info("[ItemSettingsGUI] ItemSettingsGUI 매핑 제거 완료 - " + player.getName());

        // EditorGUI를 shopManager에 다시 등록 (핵심!)
        shopManager.setEditorGUI(player, editorGUI);
        Bukkit.getLogger().info("[ItemSettingsGUI] EditorGUI 재등록 완료 - " + player.getName());

        player.closeInventory();
        editorGUI.saveAndRearrange();
        editorGUI.open(player);

        Bukkit.getLogger().info("[ItemSettingsGUI] 확인 처리 완료 - " + player.getName());
    }

    public void cancel(Player player) {
        Bukkit.getLogger().info("[ItemSettingsGUI] 취소 처리 시작 - " + player.getName() +
                ", 변경사항 있음: " + hasChanges());

        if (hasChanges()) {
            player.sendMessage(TextUtil.format("&c변경 사항이 취소되었습니다."));
        }

        // ItemSettingsGUI 매핑 제거
        ItemSettingsListener listener = TradeAndBalance.getPlugin(TradeAndBalance.class).getItemSettingsListener();
        listener.removeItemSettingsGUI(player);
        Bukkit.getLogger().info("[ItemSettingsGUI] ItemSettingsGUI 매핑 제거 완료 - " + player.getName());

        // EditorGUI를 shopManager에 다시 등록 (핵심!)
        shopManager.setEditorGUI(player, editorGUI);
        Bukkit.getLogger().info("[ItemSettingsGUI] EditorGUI 재등록 완료 - " + player.getName());

        player.closeInventory();
        editorGUI.open(player);

        Bukkit.getLogger().info("[ItemSettingsGUI] 취소 처리 완료 - " + player.getName());
    }

    public void toggleLimitType() {
        LimitType newLimitType = switch (tempLimitType) {
            case NONE -> LimitType.GLOBAL;
            case GLOBAL -> LimitType.PERSONAL;
            case PERSONAL -> LimitType.NONE;
        };
        shopManager.recordItemSettingsAction(this, tempBuyPrice, tempBuyPrice, tempSellPrice, tempSellPrice,
                tempLimitType, newLimitType, tempLimitAmount, tempLimitAmount);
        tempLimitType = newLimitType;
        updateInventory();
    }

    private boolean hasChanges() {
        return tempBuyPrice != originalBuyPrice ||
                tempSellPrice != originalSellPrice ||
                tempLimitType != originalLimitType ||
                tempLimitAmount != originalLimitAmount;
    }

    private ItemStack createLimitTypeIcon(LimitType limitType) {
        Material material;
        String displayName;
        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.format("&7클릭하여 모드 변경"));

        switch (limitType) {
            case GLOBAL -> {
                displayName = "&9플레이어 전체";
                material = Material.BLUE_STAINED_GLASS_PANE;
                lore.add(TextUtil.format("&7모든 플레이어가 공유하는 제한"));
            }
            case PERSONAL -> {
                displayName = "&e플레이어 개인";
                material = Material.YELLOW_STAINED_GLASS_PANE;
                lore.add(TextUtil.format("&7각 플레이어별 개별 제한"));
            }
            default -> {
                displayName = "&a제한 없음";
                material = Material.LIME_STAINED_GLASS_PANE;
                lore.add(TextUtil.format("&7수량 제한 없음"));
            }
        }

        if (limitType != originalLimitType) {
            lore.add(TextUtil.format("&e(변경됨: " + getLimitTypeDisplayName(originalLimitType) + " → " + getLimitTypeDisplayName(limitType) + ")"));
        }

        return ItemUtil.createItem(material, TextUtil.format(displayName), lore);
    }

    private String getLimitTypeDisplayName(LimitType limitType) {
        return switch (limitType) {
            case GLOBAL -> "플레이어 전체";
            case PERSONAL -> "플레이어 개인";
            default -> "제한 없음";
        };
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public ShopItem getItem() {
        return this.item;
    }

    public void setTempBuyPrice(int tempBuyPrice) {
        this.tempBuyPrice = tempBuyPrice;
    }

    public void setTempSellPrice(int tempSellPrice) {
        this.tempSellPrice = tempSellPrice;
    }

    public void setTempLimitType(LimitType tempLimitType) {
        this.tempLimitType = tempLimitType;
    }

    public void setTempLimitAmount(int tempLimitAmount) {
        this.tempLimitAmount = tempLimitAmount;
    }
}