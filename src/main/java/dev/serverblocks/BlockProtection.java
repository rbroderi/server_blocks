package dev.serverblocks;

import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.StructureGrowEvent;

/** Protection for registered puzzle cells, including currently invisible cells. */
public final class BlockProtection implements Listener {
    private final Predicate<Position> registered;
    public BlockProtection(Predicate<Position> registered) { this.registered=registered; }
    private boolean owns(Block b) { return registered.test(LoadedBlocks.position(b)); }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void breakBlock(BlockBreakEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void place(BlockPlaceEvent e){if(owns(e.getBlockPlaced()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void multiPlace(BlockMultiPlaceEvent e){if(e.getReplacedBlockStates().stream().anyMatch(s->owns(s.getBlock())))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void extend(BlockPistonExtendEvent e){if(e.getBlocks().stream().anyMatch(b->owns(b)||owns(b.getRelative(e.getDirection())))||owns(e.getBlock().getRelative(e.getDirection())))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void retract(BlockPistonRetractEvent e){if(e.getBlocks().stream().anyMatch(b->owns(b)||owns(b.getRelative(e.getDirection()))||owns(b.getRelative(e.getDirection().getOppositeFace()))))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void entityExplosion(EntityExplodeEvent e){e.blockList().removeIf(this::owns);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void blockExplosion(BlockExplodeEvent e){e.blockList().removeIf(this::owns);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void fluid(BlockFromToEvent e){if(owns(e.getToBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void physics(BlockPhysicsEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void fade(BlockFadeEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void form(BlockFormEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void burn(BlockBurnEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void ignite(BlockIgniteEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void prime(TNTPrimeEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void entityChange(EntityChangeBlockEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void grow(BlockGrowEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void structure(StructureGrowEvent e){if(e.getBlocks().stream().anyMatch(s->owns(s.getBlock())))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void fertilize(BlockFertilizeEvent e){if(e.getBlocks().stream().anyMatch(s->owns(s.getBlock())))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void spread(BlockSpreadEvent e){if(owns(e.getBlock()))e.setCancelled(true);}
}
