package dev.serverblocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Decorative geometry only; consumers own collision and display lifecycle. */
public final class DisplayBoxes {
    private DisplayBoxes() {}
    public static void tag(Display display, NamespacedKey key) {
        display.setPersistent(false); display.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
        display.setBrightness(new Display.Brightness(15,15)); display.setViewRange(1.5f);
    }
    public static BlockDisplay spawn(Location location, double x, double y, double z, Material material, NamespacedKey key) {
        return location.getWorld().spawn(location, BlockDisplay.class, d -> {
            tag(d,key); d.setBlock(material.createBlockData());
            d.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f((float)x,(float)y,(float)z),new Quaternionf()));
        });
    }
}
