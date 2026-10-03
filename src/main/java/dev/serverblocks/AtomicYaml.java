package dev.serverblocks;

import java.io.IOException;
import java.nio.file.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class AtomicYaml {
    private AtomicYaml() {}
    /** Strict load: corrupt state must not become an empty registry on the next save. */
    public static YamlConfiguration load(Path file) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        if (Files.exists(file)) yaml.load(file.toFile());
        return yaml;
    }
    public static void save(YamlConfiguration yaml, Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = Files.createTempFile(file.toAbsolutePath().getParent(), file.getFileName().toString(), ".tmp");
        try {
            yaml.save(temp.toFile());
            try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
