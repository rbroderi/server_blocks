package dev.serverblocks;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PackReadinessTest {
    @Test void viewersAreIndependentAndStackedPacksDoNotUndoEachOther(){
        var state=new PackReadiness();UUID viewer=UUID.randomUUID(),other=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID();
        assertFalse(state.ready(viewer));state.loaded(viewer,a);state.loaded(viewer,b);
        assertTrue(state.ready(viewer));assertFalse(state.ready(other));
        state.removed(viewer,a);assertTrue(state.ready(viewer));state.removed(viewer,b);assertFalse(state.ready(viewer));
    }
    @Test void duplicateAcknowledgementsAndDisconnectDoNotLeakReadiness(){
        var state=new PackReadiness();UUID viewer=UUID.randomUUID(),pack=UUID.randomUUID();
        state.loaded(viewer,pack);state.loaded(viewer,pack);state.removed(viewer,pack);assertFalse(state.ready(viewer));
        state.loaded(viewer,pack);state.forget(viewer);assertFalse(state.ready(viewer));state.loaded(viewer,pack);state.clear();assertFalse(state.ready(viewer));
    }
}
