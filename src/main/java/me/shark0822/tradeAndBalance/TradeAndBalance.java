package me.shark0822.tradeAndBalance;

import me.shark0822.tradeAndBalance.command.TnbCommand;
import me.shark0822.tradeAndBalance.listener.ShopEditorListener;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.util.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TradeAndBalance extends JavaPlugin {

    private ShopManager shopManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        this.shopManager = new ShopManager();
        this.dataManager = new DataManager(getDataFolder());

        dataManager.loadShops(shopManager);

        if (getCommand("tradeandbalance") != null) getCommand("tradeandbalance").setExecutor(new TnbCommand(shopManager, dataManager));
        getServer().getPluginManager().registerEvents(new ShopEditorListener(shopManager, dataManager), this);
    }

    @Override
    public void onDisable() {
        dataManager.saveShops(shopManager);
    }
}
