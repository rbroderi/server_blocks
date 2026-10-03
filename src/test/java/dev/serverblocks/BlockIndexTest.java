package dev.serverblocks;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlockIndexTest {
    private final UUID world=UUID.randomUUID();
    private Position pos(int x,int y,int z){return new Position(world,x,y,z);}
    static final class Entry {Position p;Entry(Position p){this.p=p;}}
    @Test void identitiesIncludeWorldAndUseValueEquality(){
        Position a=pos(-17,-64,32);assertEquals(a,Position.decode(a.encode()));
        assertNotEquals(a,new Position(UUID.randomUUID(),-17,-64,32));
        assertTrue(a.within(pos(-18,-65,33),1,1,1));assertFalse(a.within(pos(-18,-65,33),0,1,1));
        assertFalse(pos(Integer.MIN_VALUE,0,0).within(pos(Integer.MAX_VALUE,0,0),10,10,10));
    }
    @Test void moveRejectsStaleOrOccupiedPositionsWithoutLosingSource(){
        BlockIndex<Entry> index=new BlockIndex<>(e->e.p);Entry a=new Entry(pos(0,0,0)),b=new Entry(pos(1,0,0));index.add(a);index.add(b);
        assertThrows(IllegalArgumentException.class,()->index.move(a,a.p,b.p));assertSame(a,index.at(a.p));assertSame(b,index.at(b.p));
        assertThrows(IllegalArgumentException.class,()->index.move(a,pos(9,0,0),pos(2,0,0)));
        Position old=a.p,target=pos(2,0,0);index.move(a,old,target);a.p=target;
        assertNull(index.at(old));assertSame(a,index.at(target));index.remove(a);assertEquals(1,index.size());
    }
    @Test void snapshotsSurviveMutationAndRangesExcludeOtherWorlds(){
        BlockIndex<Entry> index=new BlockIndex<>(e->e.p);Entry a=new Entry(pos(0,0,0));index.add(a);
        index.add(new Entry(new Position(UUID.randomUUID(),0,0,0)));
        List<Entry> snapshot=index.snapshot();index.remove(a);assertEquals(2,snapshot.size());
        assertTrue(index.within(pos(0,0,0),100,50,50).isEmpty());
    }
    @Test void malformedCoordinatesFail(){for(String s:List.of("",world+",1,2",world+",1,2,3,4",world+",x,2,3"))assertThrows(IllegalArgumentException.class,()->Position.decode(s));}
}
