package dev.serverblocks;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/** No server process: strict proxies fail any unexpected world operation. */
class LoadedBlocksTest {
    private static final UUID WORLD=UUID.randomUUID();
    private static World current;
    private int reads, chunkX, chunkZ;
    private boolean loaded;
    private Block block;
    private Block at(Position p){return LoadedBlocks.at(p,id->WORLD.equals(id)?current:null);}
    @BeforeEach void world(){
        reads=0;loaded=false;
        block=(Block)Proxy.newProxyInstance(Block.class.getClassLoader(),new Class<?>[]{Block.class},(p,m,a)->{throw new AssertionError("Unexpected block operation");});
        current=(World)Proxy.newProxyInstance(World.class.getClassLoader(),new Class<?>[]{World.class},(p,m,a)->switch(m.getName()){
            case "getMinHeight" -> -64;
            case "getMaxHeight" -> 320;
            case "isChunkLoaded" -> {chunkX=(int)a[0];chunkZ=(int)a[1];yield loaded;}
            case "getBlockAt" -> {reads++;yield block;}
            default -> throw new AssertionError("Unexpected world operation: "+m.getName());
        });
    }
    @Test void missingWorldInvalidHeightAndUnloadedChunkNeverReadTerrain(){
        assertNull(at(new Position(UUID.randomUUID(),0,0,0)));
        assertNull(at(new Position(WORLD,0,-65,0)));
        assertNull(at(new Position(WORLD,0,320,0)));
        assertNull(at(new Position(WORLD,-17,-64,-1)));
        assertEquals(-2,chunkX);assertEquals(-1,chunkZ);assertEquals(0,reads);
    }
    @Test void validHeightBoundsInLoadedChunksReturnExactlyOneRead(){
        loaded=true;
        assertSame(block,at(new Position(WORLD,0,-64,0)));
        assertSame(block,at(new Position(WORLD,0,319,0)));
        assertEquals(2,reads);
    }
}
