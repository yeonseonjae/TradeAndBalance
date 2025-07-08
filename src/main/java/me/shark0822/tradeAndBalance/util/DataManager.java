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
import java.util.*;

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
        System.out.println("[DEBUG] Saving shops...");
        shopConfig.set("shops", null); // 초기화

        for (Map.Entry<String, Shop> entry : shopManager.getAllShops().entrySet()) {
            String shopID = entry.getKey();
            Shop shop = entry.getValue();
            String path = "shops." + shopID;

            System.out.println("[DEBUG] Saving shop: " + shopID + " (" + shop.getShopName() + ")");

            shopConfig.set(path + ".shopName", shop.getShopName());
            shopConfig.set(path + ".tradeType", shop.getTradeType().name());
            UUID mobUUID = shop.getLinkedEntityUUID();
            shopConfig.set(path + ".linkedMobUUID", mobUUID == null ? null : mobUUID.toString());

            PageNode node = shop.getHead();
            if (node == null) {
                System.out.println("[DEBUG] Skipping shop (no pages): " + shopID);
                continue;
            }

            ConfigurationSection pagesSection = shopConfig.createSection(path + ".pages");
            int pageIndex = 0;
            PageNode start = node;
            do {
                ShopPage page = node.getPage();
                System.out.println("[DEBUG] Saving page index: " + pageIndex + ", items: " + page.getItems().size());

                ConfigurationSection pageSection = pagesSection.createSection(String.valueOf(pageIndex));
                List<Map<String, Object>> items = new ArrayList<>();
                for (ShopItem item : page.getItems()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("item", item.getItemStack().serialize());
                    itemData.put("price", item.getPrice());
                    itemData.put("limitType", item.getLimitType().name());
                    itemData.put("limitAmount", item.getLimitAmount());
                    items.add(itemData);
                }
                pageSection.set("items", items);

                node = node.getNext();
                pageIndex++;
            } while (node != null && node != start);

            try {
                shopConfig.save(shopFile);
                System.out.println("[DEBUG] Shops saved successfully.");
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to save shops:");
                e.printStackTrace();
            }
        }
    }

    public void loadShops(ShopManager shopManager) {
        System.out.println("[DEBUG] Loading shops...");

        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shops");
        if (shopSection == null) {
            System.out.println("[DEBUG] No shop data found.");
            return;
        }

        for (String shopID : shopSection.getKeys(false)) {
            String path = "shops." + shopID;

            String shopName = shopConfig.getString(path + ".shopName", "Unknown");
            TradeType tradeType = TradeType.valueOf(shopConfig.getString(path + ".tradeType", "BOTH"));

            String uuidString = shopConfig.getString(path + ".linkedMobUUID");
            UUID mobUUID = (uuidString == null || uuidString.equalsIgnoreCase("null")) ? null : UUID.fromString(uuidString);

            Shop shop = new Shop(shopID, shopName, tradeType);
            shop.setLinkedEntityUUID(mobUUID);

            System.out.println("[DEBUG] Loading shop: " + shopID + " (" + shopName + ")");

            ConfigurationSection pageSection = shopConfig.getConfigurationSection(path + ".pages");
            if (pageSection != null) {
                pageSection.getKeys(false).stream()
                        .sorted(Comparator.comparingInt(Integer::parseInt))
                        .forEach(pageKey -> {
                            ConfigurationSection itemsSection = shopConfig.getConfigurationSection(path + ".pages." + pageKey + ".items");
                            ShopPage page = new ShopPage();

                            System.out.println(" - Loading page index: " + pageKey);

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

        System.out.println("[DEBUG] Shop loading completed.");
    }
}