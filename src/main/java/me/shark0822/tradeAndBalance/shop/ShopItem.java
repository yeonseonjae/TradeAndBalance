package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopItem {
    private final ItemStack originalItem;
    private int buyPrice;
    private int sellPrice;
    private LimitType limitType;
    private int limitAmount;
    private int remainingLimitAmount;

    public ShopItem(ItemStack item, int buyPrice, int sellPrice, LimitType limitType, int limitAmount) {
        this.originalItem = item.clone();
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.limitType = limitType;
        this.limitAmount = limitAmount;
        this.remainingLimitAmount = limitAmount;
    }

    public List<Component> getEditorLore() {
        List<Component> lore = new ArrayList<>();
        ItemMeta meta = originalItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            lore.addAll(meta.lore()); // 원본의 사용자 정의 lore 추가
        }
        lore.add(TextUtil.format("")); // 빈 줄
        lore.add(TextUtil.format("&GOLD[ 가격 정보 ]"));
        lore.add(TextUtil.format("&WHITE→ 구매 : " + buyPrice + "원"));
        lore.add(TextUtil.format("&WHITE→ 판매 : " + sellPrice + "원"));
        lore.add(TextUtil.format("")); // 빈 줄
        lore.add(TextUtil.format("&GOLD[ 수량 제한 ]"));
        lore.add(TextUtil.format("&WHITE→ 제한 : " + switch (limitType) {
            case GLOBAL -> "&BLUE플레이어 전체";
            case PERSONAL -> "&YELLOW플레이어 개인";
            default -> "&c없음";
        }));
        lore.add(TextUtil.format("&WHITE→ 수량 : (" + remainingLimitAmount + "/" + limitAmount + ")"));
        lore.add(TextUtil.format("")); // 빈 줄
        lore.add(TextUtil.format("&WHITE등록 해제하려면 우클릭하세요"));
        lore.add(TextUtil.format("&WHITE수정하려면 좌클릭하세요"));
        return lore;
    }

    public List<Component> getUserLore(TradeType tradeType) {
        List<Component> lore = new ArrayList<>();
        ItemMeta meta = originalItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            lore.addAll(meta.lore());
        }

        lore.add(TextUtil.format("")); // 빈 줄
        lore.add(TextUtil.format("&GOLD[ 가격 정보 ]"));
        if (tradeType == TradeType.BUY || tradeType == TradeType.BOTH) {
            lore.add(TextUtil.format("&WHITE→ 구매 : " + buyPrice + "원"));
        }
        if (tradeType == TradeType.SELL || tradeType == TradeType.BOTH) {
            lore.add(TextUtil.format("&WHITE→ 판매 : " + sellPrice + "원"));
        }
        // 제한 정보 추가
        if (limitType != LimitType.NONE) {
            lore.add(TextUtil.format("")); // 빈 줄
            lore.add(TextUtil.format("&GOLD[ 수량 제한 ]"));
            lore.add(TextUtil.format("&WHITE→ 제한 : " + switch (limitType) {
                case GLOBAL -> "&BLUE플레이어 전체";
                case PERSONAL -> "&YELLOW플레이어 개인";
                default -> "&c없음";
            }));
            lore.add(TextUtil.format("&WHITE→ 수량 : (" + remainingLimitAmount + "/" + limitAmount + ")"));
        }

        if (tradeType == TradeType.BUY || tradeType == TradeType.BOTH) {
            lore.add(TextUtil.format("")); // 빈 줄
            lore.add(TextUtil.format("&GOLD[ 구매 방법 ]"));
            lore.add(TextUtil.format("&WHITE→ 좌클릭 시 1개 구매"));
            lore.add(TextUtil.format("&WHITE→ 쉬프트 + 좌클릭시 일괄 구매"));
        }
        if (tradeType == TradeType.SELL || tradeType == TradeType.BOTH) {
            lore.add(TextUtil.format("")); // 빈 줄
            lore.add(TextUtil.format("&GOLD[ 판매 방법 ]"));
            lore.add(TextUtil.format("&WHITE→ 우클릭 시 1개 판매"));
            lore.add(TextUtil.format("&WHITE→ 쉬프트 + 우클릭시 일괄 판매"));
        }
        return lore;
    }

    public ItemStack getDisplayItem(boolean isEditor, TradeType tradeType) {
        ItemStack displayItem = getOriginalItem();
        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            meta.lore(isEditor ? getEditorLore() : getUserLore(tradeType));
            displayItem.setItemMeta(meta);
        }
        return displayItem;
    }

    public ItemStack getOriginalItem() {
        return originalItem.clone();
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(int price) {
        this.buyPrice = price;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int price) {
        this.sellPrice = price;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }

    public int getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(int limitAmount) {
        this.limitAmount = limitAmount;
    }

    public int getRemainingLimitAmount() {
        return remainingLimitAmount;
    }

    public void setRemainingLimitAmount(int remainingLimitAmount) {
        this.remainingLimitAmount = Math.max(0, remainingLimitAmount); // 음수 방지
    }
}