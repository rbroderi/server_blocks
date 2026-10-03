package dev.serverblocks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

public final class LoadedBlocks {
    private LoadedBlocks() {}
    public static Position position(Block block) { return new Position(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ()); }
    /** Never synchronously generates or loads a chunk. */
    public static Block at(Position p) {
        return at(p, Bukkit::getWorld);
    }
    static Block at(Position p, java.util.function.Function<java.util.UUID, World> worlds) {
        World w = worlds.apply(p.world());
        return w == null || p.y() < w.getMinHeight() || p.y() >= w.getMaxHeight() || !w.isChunkLoaded(p.x() >> 4, p.z() >> 4)
                ? null : w.getBlockAt(p.x(), p.y(), p.z());
    }
    public static BoundingBox box(Position p) { return new BoundingBox(p.x(), p.y(), p.z(), p.x()+1, p.y()+1, p.z()+1); }
    public static boolean livingOccupant(Position p) {
        Block b = at(p);
        return b == null || b.getWorld().getNearbyEntities(box(p), e -> e instanceof org.bukkit.entity.LivingEntity && !EntityHitboxes.internal(e)).stream()
                .anyMatch(e -> e.getBoundingBox().overlaps(box(p)));
    }
}
