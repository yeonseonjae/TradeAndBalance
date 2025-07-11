package me.shark0822.tradeAndBalance.shop;

import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.*;

public class ShopManager {
    private final Map<String, Shop> shops = new HashMap<>();
    private final Map<ShopItem, Integer> globalLimits = new HashMap<>();
    private final Map<ShopItem, Map<UUID, Integer>> personalLimits = new HashMap<>();

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

    public int getRemainingLimit(Player player, ShopItem item) {
        if (item.getLimitType() == LimitType.GLOBAL) {
            return globalLimits.getOrDefault(item, item.getLimitAmount());
        } else if (item.getLimitType() == LimitType.PERSONAL) {
            Map<UUID, Integer> playerLimits = personalLimits.computeIfAbsent(item, k -> new HashMap<>());
            return playerLimits.getOrDefault(player.getUniqueId(), item.getLimitAmount());
        }
        return item.getLimitAmount();
    }

    public void updateRemainingLimit(Player player, ShopItem item, int newLimit) {
        if (item.getLimitType() == LimitType.GLOBAL) {
            globalLimits.put(item, Math.max(0, newLimit));
        } else if (item.getLimitType() == LimitType.PERSONAL) {
            Map<UUID, Integer> playerLimits = personalLimits.computeIfAbsent(item, k -> new HashMap<>());
            playerLimits.put(player.getUniqueId(), Math.max(0, newLimit));
        }
    }

    public boolean purchaseItem(Player player, ShopItem item, int amount) {
        if (item.getLimitType() != LimitType.NONE) {
            int remainingLimit = getRemainingLimit(player, item);
            if (remainingLimit < amount) {
                player.sendMessage(TextUtil.format("&c제한 수량을 초과했습니다."));
                return false;
            }
            updateRemainingLimit(player, item, remainingLimit - amount);
        }
        // 실제 구매 로직 추가 (돈 차감, 아이템 지급)
        player.sendMessage(TextUtil.format("&a" + amount + "개 구매했습니다."));
        return true;
    }

    public boolean sellItem(Player player, ShopItem item, int amount) {
        if (item.getLimitType() != LimitType.NONE) {
            int remainingLimit = getRemainingLimit(player, item);
            if (remainingLimit < amount) {
                player.sendMessage(TextUtil.format("&c제한 수량을 초과했습니다."));
                return false;
            }
            updateRemainingLimit(player, item, remainingLimit - amount);
        }
        //실제 판매 로직 추가 (돈 지급, 아이템 제거)
        player.sendMessage(TextUtil.format("&a" + amount + "개 판매했습니다."));
        return true;
    }

    public void resetLimits() {
        globalLimits.clear();
        personalLimits.clear();
    }
}
