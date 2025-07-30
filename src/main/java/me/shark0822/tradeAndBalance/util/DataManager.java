package me.shark0822.tradeAndBalance.util;

import me.shark0822.tradeAndBalance.gui.EditorGUI;
import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopItem;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.ShopPage;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import org.bukkit.Material;
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
        if (!dataFolder.exists()) dataFolder.mkdirs(); // 디렉토리 생성
        this.shopFile = new File(dataFolder, "shops.yml");
        if (!shopFile.exists()) {
            try {
                shopFile.createNewFile(); // 파일 생성
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.shopConfig = YamlConfiguration.loadConfiguration(shopFile); // YAML 파일 로드
    }

    // 상점 데이터 저장
    public void saveShops(ShopManager shopManager) {
        shopConfig.set("shops", null); // 기존 데이터 초기화
        for (Shop shop : shopManager.getAllShops()) {
            String path = "shops." + shop.getShopID();
            shopConfig.set(path + ".shopName", shop.getShopName());
            shopConfig.set(path + ".tradeType", shop.getTradeType().name());
            UUID mobUUID = shop.getLinkedEntityUUID();
            shopConfig.set(path + ".linkedMobUUID", mobUUID == null ? null : mobUUID.toString());

            ConfigurationSection pagesSection = shopConfig.createSection(path + ".pages");
            int pageIndex = 0;
            for (ShopPage page : shop.getPages().getAll()) {
                ConfigurationSection pageSection = pagesSection.createSection(String.valueOf(pageIndex));
                for (Map.Entry<Integer, ShopItem> entry : page.getItems().entrySet()) {
                    String itemPath = String.valueOf(entry.getKey());
                    ShopItem item = entry.getValue();
                    pageSection.set(itemPath + ".item", item.getOriginalItem().serialize()); // ItemStack 직렬화
                    pageSection.set(itemPath + ".buyPrice", item.getBuyPrice());
                    pageSection.set(itemPath + ".sellPrice", item.getSellPrice());
                    pageSection.set(itemPath + ".limitType", item.getLimitType().name());
                    pageSection.set(itemPath + ".limitAmount", item.getLimitAmount());
                }
                pageIndex++;
            }
        }
        try {
            shopConfig.save(shopFile); // YAML 파일 저장
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 상점 데이터 로드
    public void loadShops(ShopManager shopManager) {
        ConfigurationSection shopSection = shopConfig.getConfigurationSection("shops");
        if (shopSection == null) return;

        for (String shopID : shopSection.getKeys(false)) {
            String path = "shops." + shopID;
            String shopName = shopConfig.getString(path + ".shopName", "Unknown");
            String tradeTypeStr = shopConfig.getString(path + ".tradeType", "BOTH");
            TradeType tradeType;
            try {
                tradeType = TradeType.valueOf(tradeTypeStr); // 문자열을 Enum으로 변환
            } catch (IllegalArgumentException e) {
                tradeType = TradeType.BOTH;
            }
            String uuidString = shopConfig.getString(path + ".linkedMobUUID");
            UUID mobUUID = uuidString != null && !uuidString.equalsIgnoreCase("null") ? UUID.fromString(uuidString) : null;

            Shop shop = new Shop(shopID, shopName, tradeType);
            shop.setLinkedEntityUUID(mobUUID);
            shop.getPages().getAll().clear(); // 기존 페이지 초기화

            ConfigurationSection pagesSection = shopConfig.getConfigurationSection(path + ".pages");
            if (pagesSection != null) {
                List<String> pageKeys = new ArrayList<>(pagesSection.getKeys(false));
                pageKeys.sort(Comparator.comparingInt(Integer::parseInt)); // 페이지 인덱스 정렬
                for (String pageKey : pageKeys) {
                    ShopPage page = new ShopPage(Integer.parseInt(pageKey));
                    ConfigurationSection pageSection = pagesSection.getConfigurationSection(pageKey);
                    if (pageSection != null) {
                        for (String slotKey : pageSection.getKeys(false)) {
                            try {
                                int slot = Integer.parseInt(slotKey);
                                if (Arrays.stream(EditorGUI.ITEM_SLOTS).noneMatch(s -> s == slot)) continue;

                                ConfigurationSection itemSection = pageSection.getConfigurationSection(slotKey);
                                if (itemSection == null) continue;

                                Map<String, Object> itemData = itemSection.getConfigurationSection("item").getValues(false);
                                ItemStack itemStack;
                                try {
                                    itemStack = ItemStack.deserialize(itemData); // ItemStack 역직렬화
                                } catch (Exception e) {
                                    String itemId = (String) itemData.get("id");
                                    Material material = Material.matchMaterial(itemId);
                                    if (material != null) {
                                        itemStack = new ItemStack(material, ((Number) itemData.get("count")).intValue());
                                    } else {
                                        continue;
                                    }
                                }
                                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                                int buyPrice = itemSection.getInt("buyPrice", 0);
                                int sellPrice = itemSection.getInt("sellPrice", 0);
                                String limitTypeStr = itemSection.getString("limitType", "NONE");
                                LimitType limitType;
                                try {
                                    limitType = LimitType.valueOf(limitTypeStr);
                                } catch (IllegalArgumentException e) {
                                    limitType = LimitType.NONE;
                                }
                                int limitAmount = itemSection.getInt("limitAmount", 0);

                                ShopItem shopItem = new ShopItem(itemStack, buyPrice, sellPrice, limitType, limitAmount);
                                page.addItem(slot, shopItem);
                            } catch (NumberFormatException e) {
                                // 잘못된 슬롯 번호 무시
                            }
                        }
                    }
                    shop.addPage(page);
                }
            }
            if (shop.getPages().isEmpty()) {
                shop.addPage(new ShopPage(0));
            }
            shopManager.createShop(shopID, shopName, tradeType);
            shopManager.getShop(shopID).setLinkedEntityUUID(mobUUID);
        }
    }
}