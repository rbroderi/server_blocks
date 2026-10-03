package dev.serverblocks;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

/** NONE preserves the model's block-space geometry without inventory/fixed transforms. */
public final class ModelDisplays {
    private ModelDisplays(){}
    public static ItemDisplay spawn(Location center,ItemStack item,NamespacedKey tag,boolean visibleByDefault){
        return center.getWorld().spawn(center,ItemDisplay.class,d->{
            DisplayBoxes.tag(d,tag);d.setBrightness(null);d.setVisibleByDefault(visibleByDefault);
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);d.setItemStack(item);
        });
    }
}
