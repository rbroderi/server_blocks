package dev.serverblocks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HappyGhastCollisionTest {
    public static class NativeGhast {
        int timeout;boolean clientFlag;
        private void setServerStillTimeout(int ticks){timeout=ticks;clientFlag=ticks>0;}
    }
    public static class Wrapper {
        NativeGhast nativeGhast=new NativeGhast();public NativeGhast getHandle(){return nativeGhast;}
    }
    @Test void renewsCollisionThroughTheMetadataSynchronizingSetter(){
        var adapter=new HappyGhastCollision(NativeGhast.class);var wrapper=new Wrapper();
        adapter.keepSolid(wrapper);assertEquals(200,wrapper.nativeGhast.timeout);assertTrue(wrapper.nativeGhast.clientFlag);
        wrapper.nativeGhast.timeout=1;adapter.keepSolid(wrapper);assertEquals(200,wrapper.nativeGhast.timeout);
    }
    @Test void unsupportedMappingsFailBeforeBackingBlocksAreChanged(){assertThrows(IllegalStateException.class,()->new HappyGhastCollision(String.class));}
}
