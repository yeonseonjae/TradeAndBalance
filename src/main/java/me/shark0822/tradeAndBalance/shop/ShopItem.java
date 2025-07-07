package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.type.LimitType;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final ItemStack itemStack;
    private final double price;
    private final LimitType limitType;
    private final int limitAmount;

    public ShopItem(ItemStack itemStack, double price, LimitType limitType, int limitAmount) {
        this.itemStack = itemStack;
        this.price = price;
        this.limitType = limitType;
        this.limitAmount = limitAmount;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getPrice() {
        return price;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public int getLimitAmount() {
        return limitAmount;
    }

    public boolean isLimited() {
        return limitType != LimitType.NONE;
    }

    public boolean isGlobalLimit() {
        return limitType == LimitType.GLOBAL;
    }

    public boolean isPersonalLimit() {
        return limitType == LimitType.PERSONAL;
    }

    @Override
    public String toString() {
        return "ShopItem{" +
                "item=" + itemStack.getType() +
                ", price=" + price +
                ", limitType=" + limitType +
                ", limitAmount=" + limitAmount +
                '}';
    }
}
