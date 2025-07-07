package me.shark0822.tradeAndBalance.listener;

import me.shark0822.tradeAndBalance.gui.EditorGUI;
import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopEditorListener implements Listener {

    private final ShopManager shopManager;

    public ShopEditorListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component component = event.getView().title();
        String title = PlainTextComponentSerializer.plainText().serialize(component);
        if (title == null || !title.contains("[편집]")) return;

        event.setCancelled(true);

        Shop shop = shopManager.getShop(title);
        if (shop == null) {
            player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        switch (slot) {
            case 48: // Undo 버튼 클릭
                player.sendMessage(TextUtil.format("&eUndo 기능은 아직 구현되지 않았습니다."));
                break;
            case 49: // 모드 변경 버튼 클릭
                // TradeType 순환 변경 예시
                switch (shop.getTradeType()) {
                    case BUY -> shop.setTradeType(me.shark0822.tradeAndBalance.shop.type.TradeType.SELL);
                    case SELL -> shop.setTradeType(me.shark0822.tradeAndBalance.shop.type.TradeType.BOTH);
                    case BOTH -> shop.setTradeType(me.shark0822.tradeAndBalance.shop.type.TradeType.BUY);
                }
                player.sendMessage(TextUtil.format("&a거래 모드를 변경했습니다: " + shop.getTradeType().name()));
                EditorGUI.open(player, shop);
                break;
            case 50: // Redo 버튼 클릭
                player.sendMessage(TextUtil.format("&eRedo 기능은 아직 구현되지 않았습니다."));
                break;
            case 45: // 이전 페이지 버튼 클릭 (만약 GuiUtil에 이전 페이지 버튼이 45번 슬롯이면)
                shop.prevPage();
                EditorGUI.open(player, shop);
                break;
            case 53: // 다음 페이지 버튼 클릭 (만약 GuiUtil에 다음 페이지 버튼이 53번 슬롯이면)
                shop.nextPage();
                EditorGUI.open(player, shop);
                break;
            default:
                // TODO: 아이템 슬롯 클릭 시 편집 관련 로직 추가 (추가, 삭제, 수정 등)
                break;
        }
    }
}
