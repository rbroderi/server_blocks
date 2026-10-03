package dev.serverblocks;

import java.util.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/** Main-thread, consumer-owned interaction boxes and invisible happy-ghast collision cubes.
 * Gameplay owns the backing block, protection checks, drops and persistence.
 */
public final class EntityHitboxes implements Listener,AutoCloseable {
    public enum Action { USE, ATTACK }
    public record Click(String id,Position position,Player player,EquipmentSlot hand,Action action,org.bukkit.block.BlockFace face) {}
    @FunctionalInterface public interface Handler { void clicked(Click click); }
    private static final NamespacedKey MARKER=new NamespacedKey("serverblocks","hitbox");
    private final JavaPlugin plugin;
    private final NamespacedKey owner;
    private final Handler handler;
    private final java.util.function.Consumer<RuntimeException> failureHandler;
    private final HappyGhastCollision ghastCollision;
    private final Map<String,Entry> entries=new HashMap<>();
    private final Map<UUID,String> entityIds=new HashMap<>();
    private final Map<UUID,Integer> lastClick=new HashMap<>();
    private final BukkitTask task;
    private boolean closed;
    private static final class Entry {
        final String id; final Position position; final List<HitboxGeometry.Box> interaction;
        final List<HitboxGeometry.Cube> collision; final Map<Entity,Location> entities=new LinkedHashMap<>();
        Entry(String id,Position p,List<HitboxGeometry.Box> interaction,List<HitboxGeometry.Cube> collision){this.id=id;position=p;this.interaction=interaction;this.collision=collision;}
    }
    public EntityHitboxes(JavaPlugin plugin,String name,Handler handler) {
        this(plugin,name,handler,ex->{throw ex;});
    }
    /** Recovery must restore consumer-owned backing blocks if collision becomes unavailable. */
    public EntityHitboxes(JavaPlugin plugin,String name,Handler handler,java.util.function.Consumer<RuntimeException> failureHandler) {
        this.plugin=plugin;owner=new NamespacedKey(plugin,name);this.handler=Objects.requireNonNull(handler);this.failureHandler=Objects.requireNonNull(failureHandler);ghastCollision=new HappyGhastCollision();
        Bukkit.getPluginManager().registerEvents(this,plugin);
        task=Bukkit.getScheduler().runTaskTimer(plugin,this::repair,1,10);
    }
    public static boolean internal(Entity e){return e.getPersistentDataContainer().has(MARKER,PersistentDataType.BYTE);}
    public Position target(Player player,double range){
        Location eye=player.getEyeLocation();var hit=player.getWorld().rayTraceEntities(eye,eye.getDirection(),range,0,e->entry(e)!=null);
        if(hit==null||occluded(eye,hit.getHitPosition()))return null;
        Entry e=entry(hit.getHitEntity());return e==null?null:e.position;
    }
    private boolean occluded(Location eye,org.bukkit.util.Vector hit){
        double distance=eye.toVector().distance(hit);
        // Bukkit block ray tracing may read unloaded terrain. Reject the ray first
        // if any traversed cell is unloaded, rather than triggering a chunk load.
        for(double d=0;d<distance+.25;d+=.25){Location sample=eye.clone().add(eye.getDirection().multiply(Math.min(d,distance)));
            if(LoadedBlocks.at(new Position(eye.getWorld().getUID(),sample.getBlockX(),sample.getBlockY(),sample.getBlockZ()))==null)return true;
        }
        var block=eye.getWorld().rayTraceBlocks(eye,eye.getDirection(),distance,FluidCollisionMode.NEVER,true);
        return block!=null&&eye.toVector().distance(block.getHitPosition())<distance-1e-5;
    }
    public void upsert(String id,Position p,List<HitboxGeometry.Box> clickable,List<HitboxGeometry.Box> solid) {
        if(closed)throw new IllegalStateException("Hitboxes closed");
        var interaction=HitboxGeometry.interaction(clickable);var collision=HitboxGeometry.collision(solid);
        Entry old=entries.get(id);
        if(old!=null&&old.position.equals(p)&&old.interaction.equals(interaction)&&old.collision.equals(collision)){ensure(old);return;}
        remove(id);Entry e=new Entry(id,p,interaction,collision);entries.put(id,e);
        try{ensure(e);}catch(RuntimeException ex){remove(id);throw ex;}
    }
    public void remove(String id){Entry e=entries.remove(id);if(e!=null)clear(e);}
    private void clear(Entry e){for(Entity entity:e.entities.keySet()){entityIds.remove(entity.getUniqueId());entity.remove();}e.entities.clear();}
    private void tag(Entity entity){entity.setPersistent(false);entity.setInvulnerable(true);entity.setSilent(true);entity.setGravity(false);
        entity.getPersistentDataContainer().set(MARKER,PersistentDataType.BYTE,(byte)1);
        entity.getPersistentDataContainer().set(owner,PersistentDataType.BYTE,(byte)1);
    }
    private Location at(Entry e,double x,double y,double z){return new Location(Bukkit.getWorld(e.position.world()),e.position.x()+x,e.position.y()+y,e.position.z()+z);}
    private void track(Entry e,Entity entity,Location anchor){if(!entity.isValid()){entity.remove();throw new IllegalStateException("Hitbox entity spawn was rejected");}e.entities.put(entity,anchor);entityIds.put(entity.getUniqueId(),e.id);}
    private void ensure(Entry e) {
        if(LoadedBlocks.at(e.position)==null){clear(e);return;}
        if(!e.entities.isEmpty()&&e.entities.keySet().stream().allMatch(Entity::isValid))return;
        clear(e);
        try {
            for(var b:e.interaction){Location l=at(e,b.x()+b.width()/2,b.y(),b.z()+b.depth()/2);
                Interaction entity=l.getWorld().spawn(l,Interaction.class,i->{tag(i);i.setInteractionWidth((float)b.width());i.setInteractionHeight((float)b.height());i.setResponsive(true);});track(e,entity,l);
            }
            for(var b:e.collision){Location l=at(e,b.x()+b.size()/2,b.y(),b.z()+b.size()/2);
                HappyGhast entity=l.getWorld().spawn(l,HappyGhast.class,g->{tag(g);g.setAdult();g.setAgeLock(true);g.setAI(false);g.setAware(false);g.setInvisible(true);g.setCollidable(true);
                    g.setRemoveWhenFarAway(false);Objects.requireNonNull(g.getAttribute(Attribute.SCALE)).setBaseValue(b.size()/4);
                });track(e,entity,l);ghastCollision.keepSolid(entity);
            }
        }catch(RuntimeException ex){clear(e);throw ex;}
    }
    private void repair(){if(closed)return;try{for(Entry e:List.copyOf(entries.values())){
        ensure(e);for(var a:e.entities.entrySet()){Entity entity=a.getKey();Location l=a.getValue();
            if(!entity.getLocation().getWorld().equals(l.getWorld())||entity.getLocation().distanceSquared(l)>1e-8)entity.teleport(l);
            entity.setVelocity(new org.bukkit.util.Vector());
            if(entity instanceof HappyGhast g)ghastCollision.keepSolid(g);
        }
    }}catch(RuntimeException ex){failureHandler.accept(ex);}}
    private Entry entry(Entity entity){String id=entityIds.get(entity.getUniqueId());return id==null?null:entries.get(id);}
    private void click(Entry e,Entity entity,Player player,EquipmentSlot hand,Action action){
        if(player.getGameMode()==GameMode.SPECTATOR||hand!=EquipmentSlot.HAND||LoadedBlocks.at(e.position)==null)return;
        int tick=Bukkit.getCurrentTick();if(Objects.equals(lastClick.get(player.getUniqueId()),tick))return;
        Location eye=player.getEyeLocation();var range=player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        var hit=entity.getBoundingBox().rayTrace(eye.toVector(),eye.getDirection(),range==null?3:range.getValue());
        if(hit==null||occluded(eye,hit.getHitPosition()))return;
        lastClick.put(player.getUniqueId(),tick);
        handler.clicked(new Click(e.id,e.position,player,hand,action,hit.getHitBlockFace()==null?player.getFacing().getOppositeFace():hit.getHitBlockFace()));
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void use(PlayerInteractEntityEvent event){
        Entry e=entry(event.getRightClicked());if(e==null)return;boolean allowed=!event.isCancelled();event.setCancelled(true);
        if(allowed)click(e,event.getRightClicked(),event.getPlayer(),event.getHand(),Action.USE);
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void useAt(PlayerInteractAtEntityEvent event){use(event);}
    @EventHandler(priority=EventPriority.HIGHEST) public void damage(EntityDamageEvent event){
        if(entry(event.getEntity())!=null)event.setCancelled(true);
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void attack(io.papermc.paper.event.player.PrePlayerAttackEntityEvent event){
        Entry e=entry(event.getAttacked());if(e==null)return;
        // Paper initially cancels attacks against non-attackable entities. Gameplay still
        // dispatches its normal block protection event before modifying the world.
        boolean allowed=!event.willAttack()||!event.isCancelled();event.setCancelled(true);
        if(allowed)click(e,event.getAttacked(),event.getPlayer(),EquipmentSlot.HAND,Action.ATTACK);
    }
    @EventHandler public void quit(PlayerQuitEvent event){lastClick.remove(event.getPlayer().getUniqueId());}
    @EventHandler(priority=EventPriority.HIGHEST) public void leash(PlayerLeashEntityEvent event){if(entry(event.getEntity())!=null)event.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST) public void mount(EntityMountEvent event){if(entry(event.getMount())!=null)event.setCancelled(true);}
    @EventHandler public void unload(ChunkUnloadEvent event){for(Entry e:entries.values())if(e.position.world().equals(event.getWorld().getUID())&&(e.position.x()>>4)==event.getChunk().getX()&&(e.position.z()>>4)==event.getChunk().getZ())clear(e);}
    @Override public void close(){if(closed)return;closed=true;task.cancel();HandlerList.unregisterAll(this);entries.values().forEach(this::clear);entries.clear();entityIds.clear();lastClick.clear();}
}
