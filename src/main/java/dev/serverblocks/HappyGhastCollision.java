package dev.serverblocks;

import java.lang.reflect.Method;
import org.bukkit.entity.HappyGhast;

/** Narrow 26.3 adapter: public Bukkit has no happy-ghast stay-still setter yet.
 * Keep both native collision and its client metadata synchronized. No copied Nexo code.
 */
final class HappyGhastCollision {
    private final Method still;
    private Method handle;
    HappyGhastCollision() {
        this(nativeType());
    }
    private static Class<?> nativeType(){
        try{return Class.forName("net.minecraft.world.entity.animal.happyghast.HappyGhast");}
        catch(ClassNotFoundException e){throw new IllegalStateException("Happy-ghast collision adapter requires supported Purpur 26.3 mappings",e);}
    }
    HappyGhastCollision(Class<?> type) {
        try {
            still=type.getDeclaredMethod("setServerStillTimeout",int.class);
            if(!still.trySetAccessible())throw new IllegalStateException("Cannot access happy-ghast collision state");
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Happy-ghast collision adapter requires supported Purpur 26.3 mappings",e);}
    }
    void keepSolid(Object entity) {
        try {
            if(handle==null)handle=entity.getClass().getMethod("getHandle");
            still.invoke(handle.invoke(entity),200);
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot synchronize happy-ghast collision",e);}
    }
}
