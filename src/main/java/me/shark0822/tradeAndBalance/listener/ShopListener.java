package me.shark0822.tradeAndBalance.listener;

import me.shark0822.tradeAndBalance.gui.ShopGUI;
import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Arrays;
import java.util.UUID;

public class ShopListener implements Listener {
    private final ShopManager shopManager;

    public ShopListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ShopGUI shopGUI = shopManager.getShopGUI(player);
        if (shopGUI == null || !event.getInventory().equals(shopGUI.getInventory())) return;
        int slot = event.getRawSlot();

        if (slot < 54) {
            event.setCancelled(true);
            if (slot == 45) { // 이전 페이지
                shopGUI.prevPage();
            } else if (slot == 53) { // 다음 페이지
                shopGUI.nextPage();
            } else if (Arrays.stream(ShopManager.getValidSlots()).anyMatch(s -> s == slot)) {
                TradeType tradeType = shopGUI.getShop().getTradeType();

                switch (tradeType) {
                    case BUY -> {
                        if (event.isLeftClick()) {
                            if (event.getClick().isShiftClick()) {
                                // 일괄 구매
                                player.sendMessage(TextUtil.format("&c일괄 구매 미구현"));
                            } else {
                                shopGUI.handlePurchase(player, slot, 1);
                            }
                        }
                    }
                    case SELL -> {
                        if (event.isLeftClick()) {
                            if (event.getClick().isShiftClick()) {
                                // 일괄 판매
                                player.sendMessage(TextUtil.format("&c일괄 판매 미구현"));
                            } else {
                                shopGUI.handleSell(player, slot, 1);
                            }
                        }
                    }
                    case BOTH -> {
                        if (event.getClick().isShiftClick()) {
                            if (event.isLeftClick()) {
                                // 일괄 구매
                                player.sendMessage(TextUtil.format("&c일괄 구매 미구현"));
                            } else if (event.isRightClick()) {
                                // 일괄 판매
                                player.sendMessage(TextUtil.format("&c일괄 판매 미구현"));
                            }
                        } else {
                            if (event.isLeftClick()) {
                                shopGUI.handlePurchase(player, slot, 1);
                            } else if (event.isRightClick()) {
                                shopGUI.handleSell(player, slot, 1);
                            }
                        }
                    }
                }

                if (event.isLeftClick()) {
                    shopGUI.handlePurchase(player, slot, 1);
                } else if (event.isRightClick()) {
                    shopGUI.handleSell(player, slot, 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        UUID entityUUID = event.getRightClicked().getUniqueId();
        Shop shop = shopManager.getShopByMobUUID(entityUUID);
        if (shop == null) return;
        event.setCancelled(true); // 엔티티 상호작용 취소
        shopManager.openShopGUI(player, shop); // 상점 GUI 열기
    }
}