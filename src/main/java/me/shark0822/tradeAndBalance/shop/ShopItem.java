package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.type.LimitType;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final ItemStack itemStack;
    private double price;
    private LimitType limitType;
    private int limitAmount;

    public ShopItem(ItemStack itemStack) {
        this(itemStack, 0.0, LimitType.NONE, 0);
    }

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

    public void setPrice(double price) {
        this.price = price;
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
}