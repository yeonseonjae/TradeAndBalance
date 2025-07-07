package me.shark0822.tradeAndBalance.util;

import me.shark0822.tradeAndBalance.shop.*;
import me.shark0822.tradeAndBalance.shop.page.PageNode;
import me.shark0822.tradeAndBalance.shop.page.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final File shopFile;
    private final YamlConfiguration shopConfig;

    public DataManager(File dataFolder) {
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.shopFile = new File(dataFolder, "shops.yml");
        if (!shopFile.exists()) {
            try {
                shopFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    public void saveShops(ShopManager shopManager) {
        shopConfig.set("shops", null); // 초기화

        for (Map.Entry<String, Shop> entry : shopManager.getAllShops().entrySet()) {
            String shopID = entry.getKey();
            Shop shop = entry.getValue();
            String path = "shops." + shopID;

            shopConfig.set(path + ".shopName", shop.getShopName());
            shopConfig.set(path + ".tradeType", shop.getTradeType().name());

            UUID mobUUID = shop.getLinkedEntityUUID();
            shopConfig.set(path + ".linkedMobUUID", mobUUID == null ? null : mobUUID.toString());

            PageNode node = shop.getCurrentPageNode();
            if (node == null) continue;

            PageNode start = node;
            do {
                int pageIndex = node.getIndex();
                ShopPage page = node.getPage();
                for (int i = 0; i < page.getItems().size(); i++) {
                    ShopItem item = page.getItems().get(i);
                    String itemPath = path + ".pages." + pageIndex + ".items." + i;

                    shopConfig.set(itemPath + ".item", item.getItemStack());
                    shopConfig.set(itemPath + ".price", item.getPrice());
                    shopConfig.set(itemPath + ".limitType", item.getLimitType().name());
                    shopConfig.set(itemPath + ".limitAmount", item.getLimitAmount());
                }
                node = node.getNext();
            } while (node != null && node != start);
        }

        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadShops(ShopManager shopManager) {
        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shops");
        if (shopSection == null) return;

        for (String shopID : shopSection.getKeys(false)) {
            String path = "shops." + shopID;

            String shopName = shopConfig.getString(path + ".shopName", "알 수 없음");
            TradeType tradeType = TradeType.valueOf(shopConfig.getString(path + ".tradeType", "BOTH"));

            String uuidString = shopConfig.getString(path + ".linkedMobUUID");
            UUID mobUUID = (uuidString == null || uuidString.equalsIgnoreCase("null")) ? null : UUID.fromString(uuidString);

            Shop shop = new Shop(shopID, shopName, tradeType);
            shop.setLinkedEntityUUID(mobUUID);

            ConfigurationSection pageSection = shopConfig.getConfigurationSection(path + ".pages");
            if (pageSection != null) {
                // 페이지 번호 순서대로 정렬
                pageSection.getKeys(false).stream()
                        .sorted(Comparator.comparingInt(Integer::parseInt))
                        .forEach(pageKey -> {
                            ConfigurationSection itemsSection = shopConfig.getConfigurationSection(path + ".pages." + pageKey + ".items");
                            ShopPage page = new ShopPage();

                            if (itemsSection != null) {
                                for (String itemKey : itemsSection.getKeys(false)) {
                                    String itemPath = path + ".pages." + pageKey + ".items." + itemKey;

                                    ItemStack itemStack = shopConfig.getItemStack(itemPath + ".item");
                                    double price = shopConfig.getDouble(itemPath + ".price");
                                    LimitType limitType = LimitType.valueOf(shopConfig.getString(itemPath + ".limitType", "NONE"));
                                    int limitAmount = shopConfig.getInt(itemPath + ".limitAmount");

                                    ShopItem shopItem = new ShopItem(itemStack, price, limitType, limitAmount);
                                    page.addItem(shopItem);
                                }
                            }

                            shop.addPage(page);
                        });
            }

            shopManager.registerShop(shopID, shop);
        }
    }
}
