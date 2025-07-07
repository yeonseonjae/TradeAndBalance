package me.shark0822.tradeAndBalance.command;

import me.shark0822.tradeAndBalance.gui.EditorGUI;
import me.shark0822.tradeAndBalance.gui.ShopGUI;
import me.shark0822.tradeAndBalance.shop.Shop;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.shop.type.TradeType;
import me.shark0822.tradeAndBalance.util.DataManager;
import me.shark0822.tradeAndBalance.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TnbCommand implements CommandExecutor {

    private final ShopManager shopManager;
    private final DataManager dataManager;

    public TnbCommand(ShopManager shopManager, DataManager dataManager) {
        this.shopManager = shopManager;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(TextUtil.format("&RED/" + command.getName() + " <create | open | link | modify> ..."));
            return true;
        }

        String sub = strings[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(commandSender, strings);
            case "link" -> handleLink(commandSender, strings);
            case "open" -> handleOpen(commandSender, strings);
            case "modify" -> handleModify(commandSender, strings);
            default -> commandSender.sendMessage(TextUtil.format("&RED알 수 없는 하위 명령어입니다."));
        }

        return true;
    }

    public void handleCreate(CommandSender sender, String[] strings) {
        if (strings.length < 4) {
            sender.sendMessage(TextUtil.format("&RED사용법: /shop create <shopID> <shopName> <buy/sell/both>"));
            return;
        }

        String shopID = strings[1];
        String rawName = strings[2];
        String tradeMode = strings[3].toUpperCase();

        TradeType tradeType;
        try {
            tradeType = TradeType.valueOf(tradeMode);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(TextUtil.format("&RED거래 방식은 buy, sell, both 중 하나여야 합니다."));
            return;
        }

        if (shopManager.hasShop(shopID)) {
            sender.sendMessage(TextUtil.format("&RED이미 존재하는 shopID입니다."));
            return;
        }

        Component shopNameComponent = TextUtil.format(rawName);
        String nameText = TextUtil.componentToLegacy(shopNameComponent);

        Shop shop = new Shop(shopID, nameText, tradeType);

        shopManager.registerShop(shopID, shop);
        dataManager.saveShops(shopManager);

        sender.sendMessage(TextUtil.format("&GREEN상점 '&YELLOW" + nameText + "&GREEN'이 생성되었습니다."));
    }

    public void handleLink(CommandSender commandSender, String[] strings) {

    }

    public void handleOpen(CommandSender sender, String[] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.format("&RED플레이어만 사용할 수 있습니다."));
            return;
        }

        if (strings.length < 2) {
            sender.sendMessage(TextUtil.format("&RED사용법: /shop open <shopID> [player|admin]"));
            return;
        }

        String shopID = strings[1];
        Shop shop = shopManager.getShop(shopID);

        if (shop == null) {
            sender.sendMessage(TextUtil.format("&RED해당 ID의 상점이 존재하지 않습니다."));
            return;
        }

        Player targetPlayer = null;
        boolean adminMode = false;

        if (strings.length == 2) { // /shop open <shopID>
            targetPlayer = player;
        } else if (strings.length == 3) {
            String arg2 = strings[2];

            if (arg2.equalsIgnoreCase("admin")) { // /shop open <shopID> admin
                adminMode = true;
                targetPlayer = player;
            } else { // /shop open <shopID> [player]
                Player p = Bukkit.getPlayerExact(arg2);
                if (p == null) {
                    sender.sendMessage(TextUtil.format("&RED플레이어 '" + arg2 + "' 를 찾을 수 없습니다."));
                    return;
                }
                targetPlayer = p;
            }
        } else if (strings.length == 4) { // /shop open <shopID> <player> admin
            Player p = Bukkit.getPlayerExact(strings[2]);
            if (p == null) {
                sender.sendMessage(TextUtil.format("&RED플레이어 '" + strings[2] + "' 를 찾을 수 없습니다."));
                return;
            }
            targetPlayer = p;

            if (strings[3].equalsIgnoreCase("admin")) {
                adminMode = true;
            } else {
                sender.sendMessage(TextUtil.format("&RED잘못된 옵션입니다. [admin] 만 허용됩니다."));
                return;
            }
        } else {
            sender.sendMessage(TextUtil.format("&RED잘못된 명령어 형식입니다."));
            return;
        }

        if (adminMode) {
            EditorGUI.open(targetPlayer, shop);
        } else {
            ShopGUI.open(targetPlayer, shop);
        }

        String message = "&GREEN상점 &YELLOW" + shop.getShopName() +
                (adminMode ? "&GREEN의 관리자 페이지를 " : "&GREEN을(를)") + (targetPlayer.equals(sender) ? "열었습니다." : targetPlayer.getName() + " 님에게 열어주었습니다.");

        sender.sendMessage(TextUtil.format(message));

    }

    public void handleModify(CommandSender commandSender, String[] strings) {

    }
}
