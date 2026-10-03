package dev.serverblocks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ModelKeyTest {
    @Test void namespacedModelsMapToModernDefinitionFiles(){var key=ModelKey.parse("zblocks:push/blue_active");assertEquals("assets/zblocks/items/push/blue_active.json",key.definitionPath());assertEquals("zblocks:push/blue_active",key.toString());}
    @Test void unsafeAndAmbiguousNamesAreRejected(){for(String key:new String[]{"push","A:b","a:../b","a:/b","a:b/","a:","a:b:c","a:b x"})assertThrows(IllegalArgumentException.class,()->ModelKey.parse(key));}
}
