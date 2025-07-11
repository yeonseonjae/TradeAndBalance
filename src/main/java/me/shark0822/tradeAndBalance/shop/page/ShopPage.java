package me.shark0822.tradeAndBalance.shop.page;

import me.shark0822.tradeAndBalance.shop.ShopItem;

import java.util.ArrayList;
import java.util.List;

public class ShopPage {
    private final List<ShopItem> items;

    public ShopPage() {
        this.items = new ArrayList<>();
    }

    public boolean addItem(ShopItem item) {
        return items.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public ShopItem getItem(int index) {
        if (index < 0 || index >= items.size()) return null;
        return items.get(index);
    }

    public void setItem(int index, ShopItem shopItem) {
        if (index >= 0 && index < items.size()) {
            items.set(index, shopItem);
            System.out.println("[DEBUG] Set item at index: " + index + ", buyPrice=" + shopItem.getBuyPrice() + ", sellPrice=" + shopItem.getSellPrice() + ", limitType=" + shopItem.getLimitType() + ", limitAmount=" + shopItem.getLimitAmount());
        } else {
            System.out.println("[DEBUG] Invalid set item index: " + index + ", size: " + items.size());
        }
    }

    public List<ShopItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getSize() {
        return items.size();
    }

    public boolean isEmpty() {
        if (items == null) {
            return true; // 리스트가 null이면 빈 것으로 간주
        }
        return items.isEmpty() || items.stream().allMatch(item -> item == null || item.getOriginalItem() == null);
    }

    public void clear() {
        items.clear();
    }
}
