package dev.serverblocks;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HitboxGeometryTest {
    @Test void cableRunsUseOverlappingCubesWithoutExpandingTheirOuterBounds(){
        var box=new HitboxGeometry.Box(.375,0,.625,.25,.25,.375);
        var cubes=HitboxGeometry.collision(List.of(box));assertEquals(2,cubes.size());
        assertEquals(.625,cubes.getFirst().z());assertEquals(.75,cubes.getLast().z());
        for(var c:cubes){assertEquals(.25,c.size());assertEquals(.375,c.x());assertEquals(0,c.y());}
        assertEquals(1,cubes.getLast().z()+cubes.getLast().size());
    }
    @Test void thinDetailsExpandSymmetricallyAndDuplicateCubesAreDeduplicated(){
        var box=new HitboxGeometry.Box(.625,0,.375,.125,.25,.25);
        var cubes=HitboxGeometry.collision(List.of(box,box));assertEquals(1,cubes.size());
        assertEquals(.5625,cubes.getFirst().x());assertEquals(.25,cubes.getFirst().size());
    }
    @Test void squareInteractionsCoverRectanglesExactly(){
        var boxes=HitboxGeometry.interaction(List.of(new HitboxGeometry.Box(0,0,0,.75,.125,.25)));
        assertEquals(3,boxes.size());assertEquals(.5,boxes.getLast().x());
        for(var b:boxes){assertEquals(.25,b.width());assertEquals(.25,b.depth());assertEquals(.125,b.height());}
    }
    @Test void rejectsInvalidDimensionsAndEntityExplosions(){
        assertThrows(IllegalArgumentException.class,()->new HitboxGeometry.Box(Double.NaN,0,0,1,1,1));
        assertThrows(IllegalArgumentException.class,()->new HitboxGeometry.Box(0,0,0,0,1,1));
        assertThrows(IllegalArgumentException.class,()->HitboxGeometry.collision(List.of(new HitboxGeometry.Box(0,0,0,4,.25,4))));
    }
}
