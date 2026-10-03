package dev.serverblocks;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public final class ItemModels {
    private ItemModels(){}
    public static ItemStack apply(ItemStack item, ModelKey key){
        var meta=item.getItemMeta();meta.setItemModel(new NamespacedKey(key.namespace(),key.path()));item.setItemMeta(meta);return item;
    }
}
