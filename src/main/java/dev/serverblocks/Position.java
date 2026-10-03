package dev.serverblocks;

import java.util.UUID;

/** World-qualified immutable coordinate. No Bukkit objects retained across unloads. */
public record Position(UUID world, int x, int y, int z) {
    public Position offset(int dx, int dy, int dz) { return new Position(world, x + dx, y + dy, z + dz); }
    public boolean within(Position center, int horizontal, int up, int down) {
        return world.equals(center.world) && Math.abs((long) x - center.x) <= horizontal
                && Math.abs((long) z - center.z) <= horizontal
                && (long) y - center.y <= up && (long) center.y - y <= down;
    }
    public String encode() { return world + "," + x + "," + y + "," + z; }
    public static Position decode(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Invalid coordinate: " + value);
        return new Position(UUID.fromString(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}
