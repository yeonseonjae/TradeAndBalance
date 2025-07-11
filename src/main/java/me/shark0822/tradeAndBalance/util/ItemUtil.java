package me.shark0822.tradeAndBalance.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemUtil {

    public static ItemStack createItem(Material material, Component displayName) {
        return createItem(material, displayName, null, null);
    }

    public static ItemStack createItem(Material material, Component displayName, List<Component> lore) {
        return createItem(material, displayName, lore, null);
    }

    public static ItemStack createItem(Material material, List<Component> lore) {
        return createItem(material, null, lore, null);
    }

    public static ItemStack createItem(Material material, Component displayName, NamespacedKey namespacedKey) {
        return createItem(material, displayName, null, namespacedKey);
    }

    public static ItemStack createItem(Material material, Component displayName, List<Component> lore, NamespacedKey namespacedKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) meta.displayName(displayName);
            if (lore != null) meta.lore(lore);
            if (namespacedKey != null) meta.setItemModel(namespacedKey);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isSimilar(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        return a.isSimilar(b);
    }
}
