package me.shark0822.tradeAndBalance.shop.page;

import me.shark0822.tradeAndBalance.shop.ShopItem;

import java.util.ArrayList;
import java.util.Collections;
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

    public List<ShopItem> getItems() {
        return Collections.unmodifiableList(items); // 외부에서 변경 못하게
    }

    public int getSize() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
