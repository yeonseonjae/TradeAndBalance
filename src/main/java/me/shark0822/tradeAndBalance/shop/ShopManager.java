package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.TextUtil;

import java.util.*;

public class ShopManager {
    private final Map<String, Shop> shops = new HashMap<>();

    public boolean registerShop(String id, Shop shop) {
        if (shops.containsKey(id)) return false;
        shops.put(id, shop);
        return true;
    }

    public boolean createShop(String id, String name, TradeType type) {
        if (shops.containsKey(id)) return false;
        Shop shop = new Shop(id, name, type);
        shops.put(id, shop);
        return true;
    }

    public boolean removeShop(String id) {
        return shops.remove(id) != null;
    }

    public Shop getShop(String id) {
        return shops.get(id);
    }

    public Shop getShopByName(String title) {
        if (title == null) return null;

        if (title.startsWith("&8[편집] ")) {
            String shopName = title.substring("&8[편집] ".length());
            for (Shop shop : shops.values()) {
                if (shop.getShopName().equalsIgnoreCase(shopName)) {
                    return shop;
                }
            }
        }

        return null;
    }

    public boolean hasShop(String id) {
        return shops.containsKey(id);
    }

    public Collection<Shop> getAllShopsAsList() {
        return shops.values();
    }

    public Map<String, Shop> getAllShops() {
        return Collections.unmodifiableMap(shops);
    }

    public Shop getShopByMobUUID(UUID uuid) {
        for (Shop shop : shops.values()) {
            if (uuid.equals(shop.getLinkedEntityUUID())) {
                return shop;
            }
        }
        return null;
    }

    public int getShopCount() {
        return shops.size();
    }

    public void clearAll() {
        shops.clear();
    }
}
