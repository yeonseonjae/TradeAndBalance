package me.shark0822.tradeAndBalance;

import me.shark0822.tradeAndBalance.command.TnbCommand;
import me.shark0822.tradeAndBalance.listener.EditorListener;
import me.shark0822.tradeAndBalance.listener.ItemSettingsListener;
import me.shark0822.tradeAndBalance.listener.ShopListener;
import me.shark0822.tradeAndBalance.shop.ShopManager;
import me.shark0822.tradeAndBalance.util.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TradeAndBalance extends JavaPlugin {
    private ShopManager shopManager;
    private DataManager dataManager;
    private ItemSettingsListener itemSettingsListener; // 단일 인스턴스 관리

    @Override
    public void onEnable() {
        shopManager = new ShopManager();
        dataManager = new DataManager(getDataFolder());
        itemSettingsListener = new ItemSettingsListener(shopManager); // 단일 인스턴스 초기화
        dataManager.loadShops(shopManager);

        getCommand("tradeandbalance").setExecutor(new TnbCommand(shopManager, dataManager));

        getServer().getPluginManager().registerEvents(new ShopListener(shopManager), this);
        getServer().getPluginManager().registerEvents(new EditorListener(shopManager), this);
        getServer().getPluginManager().registerEvents(itemSettingsListener, this);
    }

    @Override
    public void onDisable() {
        dataManager.saveShops(shopManager);
        shopManager.clearAll();
    }

    public ItemSettingsListener getItemSettingsListener() {
        return itemSettingsListener;
    }
}