package dev.serverblocks;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResetPlannerTest {
    final UUID world=UUID.randomUUID();
    Position p(int x){return new Position(world,x,0,0);}
    @Test void swapsAndThreeBlockCyclesNeedNoEmptyDestination(){
        assertTrue(ResetPlanner.canReset(Map.of(p(0),p(1),p(1),p(0)),q->false));
        assertTrue(ResetPlanner.canReset(Map.of(p(0),p(1),p(1),p(2),p(2),p(0)),q->false));
    }
    @Test void occupiedOriginsAndDuplicateDestinationsVetoWholeReset(){
        assertFalse(ResetPlanner.canReset(Map.of(p(0),p(3),p(1),p(4)),q->q.equals(p(3))));
        assertFalse(ResetPlanner.canReset(Map.of(p(0),p(3),p(1),p(3)),q->true));
    }
    @Test void equalCoordinatesAndEmptyMovesAreSafe(){assertTrue(ResetPlanner.canReset(Map.of(p(0),p(0)),q->false));assertTrue(ResetPlanner.canReset(Map.of(),q->false));}
}
