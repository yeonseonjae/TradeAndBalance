package me.shark0822.tradeAndBalance.command;

import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.DataManager;
import me.shark0822.tradeAndBalance.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TnbCommand implements CommandExecutor {
    private final ShopManager shopManager;
    private final DataManager dataManager;

    public TnbCommand(ShopManager shopManager, DataManager dataManager) {
        this.shopManager = shopManager;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.format("&c플레이어만 사용할 수 있습니다."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(TextUtil.format("&c사용법: /tradeandbalance <create|open|remove|link> ..."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 4) {
                    player.sendMessage(TextUtil.format("&c사용법: /tradeandbalance create <id> <name> <buy|sell|both>"));
                    return true;
                }
                String id = args[1];
                String name = args[2];
                TradeType type;
                try {
                    type = TradeType.valueOf(args[3].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(TextUtil.format("&c잘못된 거래 유형입니다."));
                    return true;
                }
                if (shopManager.createShop(id, name, type)) {
                    player.sendMessage(TextUtil.format("&a상점 '" + name + "&a'이(가) 생성되었습니다."));
                    dataManager.saveShops(shopManager);
                } else {
                    player.sendMessage(TextUtil.format("&c이미 존재하는 상점 ID입니다."));
                }
            }
            case "open" -> {
                if (args.length < 2) {
                    player.sendMessage(TextUtil.format("&c사용법: /tradeandbalance open <id> [admin]"));
                    return true;
                }
                Shop shop = shopManager.getShop(args[1]);
                if (shop == null) {
                    player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
                    return true;
                }
                if (args.length > 2 && args[2].equalsIgnoreCase("admin")) {
                    shopManager.openEditorGUI(player, shop);
                } else {
                    shopManager.openShopGUI(player, shop);
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    player.sendMessage(TextUtil.format("&c사용법: /tradeandbalance remove <id>"));
                    return true;
                }
                if (shopManager.removeShop(args[1])) {
                    player.sendMessage(TextUtil.format("&a상점이 삭제되었습니다."));
                    dataManager.saveShops(shopManager);
                } else {
                    player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
                }
            }
            case "link" -> {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.format("&c사용법: /tradeandbalance link <id> <uuid>"));
                    return true;
                }
                Shop shop = shopManager.getShop(args[1]);
                if (shop == null) {
                    player.sendMessage(TextUtil.format("&c상점을 찾을 수 없습니다."));
                    return true;
                }
                if (shopManager.isShopLocked(shop.getShopID())) {
                    player.sendMessage(TextUtil.format("&c이 상점은 현재 편집 중입니다."));
                    return true;
                }
                try {
                    UUID uuid = UUID.fromString(args[2]);
                    shop.setLinkedEntityUUID(uuid);
                    player.sendMessage(TextUtil.format("&a상점이 UUID " + uuid + "에 연결되었습니다."));
                    dataManager.saveShops(shopManager);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(TextUtil.format("&c잘못된 UUID 형식입니다."));
                }
            }
        }
        return true;
    }
}