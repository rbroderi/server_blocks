package dev.serverblocks;

import org.bukkit.plugin.java.JavaPlugin;

/** Shared API provider. Consumer plugins own their registries and entity managers. */
public final class ServerBlocksPlugin extends JavaPlugin {
    @Override public void onEnable() {
        getLogger().info("Server Blocks API ready; custom-block plugins share this installation.");
    }
}
