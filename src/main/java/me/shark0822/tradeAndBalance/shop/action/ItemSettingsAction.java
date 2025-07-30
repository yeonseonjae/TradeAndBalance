package me.shark0822.tradeAndBalance.shop.action;

import me.shark0822.tradeAndBalance.gui.ItemSettingsGUI;
import me.shark0822.tradeAndBalance.shop.type.LimitType;
import org.bukkit.Bukkit;

public class ItemSettingsAction implements EditorAction {
    private final ItemSettingsGUI gui;
    private final int oldBuyPrice, newBuyPrice;
    private final int oldSellPrice, newSellPrice;
    private final LimitType oldLimitType, newLimitType;
    private final int oldLimitAmount, newLimitAmount;

    public ItemSettingsAction(ItemSettingsGUI gui, int oldBuyPrice, int newBuyPrice, int oldSellPrice, int newSellPrice,
                              LimitType oldLimitType, LimitType newLimitType, int oldLimitAmount, int newLimitAmount) {
        this.gui = gui;
        this.oldBuyPrice = oldBuyPrice;
        this.newBuyPrice = newBuyPrice;
        this.oldSellPrice = oldSellPrice;
        this.newSellPrice = newSellPrice;
        this.oldLimitType = oldLimitType;
        this.newLimitType = newLimitType;
        this.oldLimitAmount = oldLimitAmount;
        this.newLimitAmount = newLimitAmount;
    }

    @Override
    public void undo() {
        gui.setTempBuyPrice(oldBuyPrice);
        gui.setTempSellPrice(oldSellPrice);
        gui.setTempLimitType(oldLimitType);
        gui.setTempLimitAmount(oldLimitAmount);
        gui.updateInventory();
        Bukkit.getLogger().info("[ItemSettingsAction] Undo 실행 - 구매가격: " + oldBuyPrice + ", 판매가격: " + oldSellPrice);
    }

    @Override
    public void redo() {
        gui.setTempBuyPrice(newBuyPrice);
        gui.setTempSellPrice(newSellPrice);
        gui.setTempLimitType(newLimitType);
        gui.setTempLimitAmount(newLimitAmount);
        gui.updateInventory();
        Bukkit.getLogger().info("[ItemSettingsAction] Redo 실행 - 구매가격: " + newBuyPrice + ", 판매가격: " + newSellPrice);
    }

    public int getOldBuyPrice() {
        return oldBuyPrice;
    }

    public int getNewBuyPrice() {
        return newBuyPrice;
    }

    public int getOldSellPrice() {
        return oldSellPrice;
    }

    public int getNewSellPrice() {
        return newSellPrice;
    }

    public LimitType getOldLimitType() {
        return oldLimitType;
    }

    public LimitType getNewLimitType() {
        return newLimitType;
    }

    public int getOldLimitAmount() {
        return oldLimitAmount;
    }

    public int getNewLimitAmount() {
        return newLimitAmount;
    }
}
