package me.shark0822.tradeAndBalance.util;

import me.shark0822.tradeAndBalance.shop.type.TradeType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiUtil {
    private static final int[] BLACK_GLASS_SLOTS = {
            0,1,2,3,4,5,6,7,8,
            18,19,20,21,22,23,24,25,26,
            36,37,38,39,40,41,42,43,44,
            46,47,48,49,50,51,52};

    public static final ItemStack BLACK_GLASS = ItemUtil.createItem(Material.BLACK_STAINED_GLASS_PANE, Component.empty());
    public static final ItemStack PREV_BTN = ItemUtil.createItem(Material.PAPER, TextUtil.format("이전 페이지"));
    public static final ItemStack NEXT_BTN = ItemUtil.createItem(Material.PAPER, TextUtil.format("다음 페이지"));
    public static final ItemStack UNDO_BTN = ItemUtil.createItem(Material.PAPER, TextUtil.format("되돌리기"));
    public static final ItemStack REDO_BTN = ItemUtil.createItem(Material.PAPER, TextUtil.format("다시 실행"));
    public static ItemStack createTradeModeButton(TradeType mode) {
        Material material = Material.PAPER;
        Component name;
        switch(mode) {
            case BUY -> {
                name = TextUtil.format("&GREEN구매 모드");
            }
            case SELL -> {
                name = TextUtil.format("&RED판매 모드");
            }
            case BOTH -> {
                name = TextUtil.format("&YELLOW구매/판매 모드");
            }
            default -> {
                name = Component.text("알 수 없음");
            }
        }
        return ItemUtil.createItem(material, name, List.of(TextUtil.format("&7클릭하여 모드 변경")));
    }

    public static void fillDefault(Inventory inventory, int... exclude) {
        Set<Integer> excludeSet = Arrays.stream(exclude).boxed().collect(Collectors.toSet());
        inventory.setItem(45, PREV_BTN);
        inventory.setItem(53, NEXT_BTN);
        for (int slot : BLACK_GLASS_SLOTS) {
            if (excludeSet.contains(slot)) continue;
            inventory.setItem(slot, BLACK_GLASS);
        }
    }
}
