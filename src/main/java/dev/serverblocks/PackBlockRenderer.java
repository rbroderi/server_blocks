package dev.serverblocks;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;

/**
 * Display models + per-viewer invisible collision skins. The world remains owned
 * by gameplay. A successfully loaded server pack enables visuals for that viewer.
 * No upload, pack URL or automatic pack dispatch is performed here.
 */
public final class PackBlockRenderer implements Listener,AutoCloseable {
    private final JavaPlugin plugin;
    private final NamespacedKey tag;
    private final Map<String,Entry> entries=new LinkedHashMap<>();
    private final PackReadiness loadedPacks=new PackReadiness();
    private final UUID requiredPack;
    private final BukkitTask repair;
    private boolean closed;
    private static final class Entry {
        Position position; ModelKey model; Material item; BlockData mask; ItemDisplay display;
        List<HitboxGeometry.Box> fallbackShape;List<BlockDisplay> fallback=new ArrayList<>();
        Entry(Position p,ModelKey m,Material item,BlockData mask,List<HitboxGeometry.Box> shape){position=p;model=m;this.item=item;this.mask=mask.clone();fallbackShape=List.copyOf(shape);}
    }
    public PackBlockRenderer(JavaPlugin plugin,String tagName){
        this(plugin,tagName,null);
    }
    /** Optional exact ID for a manually configured merged server pack. */
    public PackBlockRenderer(JavaPlugin plugin,String tagName,UUID requiredPack){
        this.plugin=plugin;this.requiredPack=requiredPack;tag=new NamespacedKey(plugin,tagName);
        Bukkit.getPluginManager().registerEvents(this,plugin);
        // World block updates can overwrite a client skin; repair without loading chunks.
        repair=Bukkit.getScheduler().runTaskTimer(plugin,()->{
            for(Entry e:entries.values())for(Player p:Bukkit.getOnlinePlayers())if(ready(p))skin(p,e);
        },20,20);
    }
    public boolean ready(Player p){return loadedPacks.ready(p.getUniqueId());}
    public void upsert(String id,Position pos,ModelKey model,Material item,BlockData clientCollision){
        upsert(id,pos,model,item,clientCollision,List.of());
    }
    /** Optional vanilla geometry for air/light-backed objects before a server pack loads. */
    public void upsert(String id,Position pos,ModelKey model,Material item,BlockData clientCollision,List<HitboxGeometry.Box> fallbackShape){
        if(closed)throw new IllegalStateException("Renderer closed");
        if(LoadedBlocks.at(pos)==null){remove(id);return;}
        Entry e=entries.get(id);
        if(e!=null&&e.position.equals(pos)&&e.model.equals(model)&&e.item==item&&e.mask.matches(clientCollision)&&e.fallbackShape.equals(fallbackShape)&&e.display!=null&&e.display.isValid()&&e.fallback.stream().allMatch(Entity::isValid))return;
        ItemDisplay reusable=e!=null&&e.display!=null&&e.display.isValid()?e.display:null;
        if(e!=null){if(!e.position.equals(pos))restore(e.position);if(reusable==null&&e.display!=null)e.display.remove();e.fallback.forEach(Entity::remove);}
        e=new Entry(pos,model,item,clientCollision,fallbackShape);entries.put(id,e);
        World w=Bukkit.getWorld(pos.world());Location center=new Location(w,pos.x()+.5,pos.y()+.5,pos.z()+.5);
        ItemStack stack=ItemModels.apply(new ItemStack(item),model);
        if(reusable==null)e.display=ModelDisplays.spawn(center,stack,tag,false);
        else{e.display=reusable;e.display.teleport(center);e.display.setItemStack(stack);}
        for(var box:fallbackShape){var d=DisplayBoxes.spawn(new Location(w,pos.x()+box.x(),pos.y()+box.y(),pos.z()+box.z()),box.width(),box.height(),box.depth(),item,tag);d.setBrightness(null);e.fallback.add(d);}
        for(Player player:Bukkit.getOnlinePlayers())if(ready(player)){player.showEntity(plugin,e.display);e.fallback.forEach(d->player.hideEntity(plugin,d));skin(player,e);}
    }
    public void remove(String id){Entry e=entries.remove(id);if(e==null)return;if(e.display!=null)e.display.remove();e.fallback.forEach(Entity::remove);restore(e.position);}
    private boolean nearby(Player player,Position pos){
        int radius=Math.max(1,Math.min(player.getClientViewDistance(),player.getWorld().getViewDistance()));
        return player.getWorld().getUID().equals(pos.world())&&Math.abs((player.getLocation().getBlockX()>>4)-(pos.x()>>4))<=radius
            &&Math.abs((player.getLocation().getBlockZ()>>4)-(pos.z()>>4))<=radius;
    }
    private void skin(Player player,Entry e){
        if(nearby(player,e.position)&&LoadedBlocks.at(e.position)!=null)
            player.sendBlockChange(new Location(player.getWorld(),e.position.x(),e.position.y(),e.position.z()),e.mask);
    }
    private void restore(Position pos){
        var b=LoadedBlocks.at(pos);if(b==null)return;
        for(Player player:Bukkit.getOnlinePlayers())if(nearby(player,pos))player.sendBlockChange(b.getLocation(),b.getBlockData());
    }
    private void refresh(Player player){
        for(Entry e:entries.values()){
            if(ready(player)){if(e.display!=null&&e.display.isValid())player.showEntity(plugin,e.display);e.fallback.forEach(d->player.hideEntity(plugin,d));skin(player,e);}
            else{if(e.display!=null)player.hideEntity(plugin,e.display);e.fallback.forEach(d->player.showEntity(plugin,d));var b=LoadedBlocks.at(e.position);if(b!=null&&nearby(player,e.position))player.sendBlockChange(b.getLocation(),b.getBlockData());}
        }
    }
    @EventHandler public void status(PlayerResourcePackStatusEvent event){
        if(requiredPack!=null&&!requiredPack.equals(event.getID()))return;
        UUID player=event.getPlayer().getUniqueId();
        // Pending another stacked pack must not hide a previously successful one.
        switch(event.getStatus()){
            case SUCCESSFULLY_LOADED -> loadedPacks.loaded(player,event.getID());
            case DECLINED,FAILED_DOWNLOAD,FAILED_RELOAD,INVALID_URL,DISCARDED -> loadedPacks.removed(player,event.getID());
            default -> {return;}
        }
        Bukkit.getScheduler().runTask(plugin,()->{if(!closed&&event.getPlayer().isOnline())refresh(event.getPlayer());});
    }
    @EventHandler public void chunk(PlayerChunkLoadEvent event){
        if(!ready(event.getPlayer()))return;
        UUID world=event.getChunk().getWorld().getUID();int x=event.getChunk().getX(),z=event.getChunk().getZ();
        Bukkit.getScheduler().runTask(plugin,()->{if(closed||!event.getPlayer().isOnline()||!ready(event.getPlayer()))return;
            for(Entry e:entries.values())if(e.position.world().equals(world)&&(e.position.x()>>4)==x&&(e.position.z()>>4)==z)skin(event.getPlayer(),e);
        });
    }
    @EventHandler public void quit(PlayerQuitEvent event){loadedPacks.forget(event.getPlayer().getUniqueId());}
    public void sound(Position pos,String custom,Sound fallback,float volume,float pitch){
        World world=Bukkit.getWorld(pos.world());if(world==null)return;Location loc=new Location(world,pos.x()+.5,pos.y()+.5,pos.z()+.5);
        for(Player player:world.getPlayers())if(ready(player))player.playSound(loc,custom,volume,pitch);else player.playSound(loc,fallback,volume,pitch);
    }
    @Override public void close(){if(closed)return;closed=true;repair.cancel();HandlerList.unregisterAll(this);
        for(Entry e:entries.values()){if(e.display!=null)e.display.remove();e.fallback.forEach(Entity::remove);restore(e.position);}entries.clear();loadedPacks.clear();
    }
}
