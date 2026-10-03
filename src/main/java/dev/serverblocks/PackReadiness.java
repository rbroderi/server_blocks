package dev.serverblocks;
import java.util.*;

/** Per-viewer acknowledgement of stacked server packs. */
public final class PackReadiness {
    private final Map<UUID,Set<UUID>> loaded=new HashMap<>();
    public void loaded(UUID viewer,UUID pack){loaded.computeIfAbsent(viewer,k->new HashSet<>()).add(pack);}
    public void removed(UUID viewer,UUID pack){var packs=loaded.get(viewer);if(packs!=null){packs.remove(pack);if(packs.isEmpty())loaded.remove(viewer);}}
    public boolean ready(UUID viewer){return !loaded.getOrDefault(viewer,Set.of()).isEmpty();}
    public void forget(UUID viewer){loaded.remove(viewer);}
    public void clear(){loaded.clear();}
}
