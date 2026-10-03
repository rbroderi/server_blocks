package dev.serverblocks;

import java.nio.file.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AtomicYamlTest {
    @TempDir Path folder;
    @Test void savesReplaceWholeDocumentWithoutLeftoverTemps()throws Exception{
        Path file=folder.resolve("state.yml");YamlConfiguration y=new YamlConfiguration();y.set("old",123);AtomicYaml.save(y,file);
        y=new YamlConfiguration();y.set("new",456);AtomicYaml.save(y,file);
        YamlConfiguration loaded=AtomicYaml.load(file);assertFalse(loaded.contains("old"));assertEquals(456,loaded.getInt("new"));
        try(var files=Files.list(folder)){assertEquals(1,files.count());}
    }
    @Test void corruptYamlFailsAndRetainsOriginalFile()throws Exception{
        Path file=folder.resolve("state.yml");String corrupt="broken: [\n";Files.writeString(file,corrupt);
        assertThrows(InvalidConfigurationException.class,()->AtomicYaml.load(file));assertEquals(corrupt,Files.readString(file));
    }
}
