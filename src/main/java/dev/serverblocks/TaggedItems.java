package dev.serverblocks;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Uses the consumer's existing key, so adoption does not invalidate previously crafted items. */
public final class TaggedItems {
    private TaggedItems() {}
    public static String kind(ItemStack item, NamespacedKey key) {
        return item == null || !item.hasItemMeta() ? null : item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
    public static void tag(ItemStack item, NamespacedKey key, String kind) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, kind); item.setItemMeta(meta);
    }
}
