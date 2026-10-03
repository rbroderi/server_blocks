package dev.serverblocks;

import java.util.*;

/** Local axis-aligned shapes. Collision uses overlapping cubes, never rotated meshes. */
public final class HitboxGeometry {
    private HitboxGeometry() {}
    public record Box(double x,double y,double z,double width,double height,double depth) {
        public Box {
            for(double v:new double[]{x,y,z,width,height,depth})if(!Double.isFinite(v))throw new IllegalArgumentException("Non-finite hitbox");
            if(width<=0||height<=0||depth<=0||width>4||height>4||depth>4)throw new IllegalArgumentException("Hitbox dimensions must be in (0,4]");
        }
    }
    public record Cube(double x,double y,double z,double size) {}
    /** Happy-ghast scale bottoms out at 1/16; its adult body is four blocks wide. */
    public static List<Cube> collision(List<Box> boxes) {
        Set<Cube> result=new LinkedHashSet<>();
        for(Box b:boxes) {
            double size=Math.max(.25,Math.min(b.width,Math.min(b.height,b.depth)));
            for(double x:starts(b.x,b.width,size))for(double y:starts(b.y,b.height,size))for(double z:starts(b.z,b.depth,size)) {
                result.add(new Cube(x,y,z,size));
                if(result.size()>64)throw new IllegalArgumentException("More than 64 collision cubes in one shape");
            }
        }
        return List.copyOf(result);
    }
    /** Interaction entities have square horizontal footprints; overlap squares without gaps. */
    public static List<Box> interaction(List<Box> boxes) {
        Set<Box> result=new LinkedHashSet<>();
        for(Box b:boxes) {
            double size=Math.min(b.width,b.depth);
            for(double x:starts(b.x,b.width,size))for(double z:starts(b.z,b.depth,size)) {
                result.add(new Box(x,b.y,z,size,b.height,size));
                if(result.size()>64)throw new IllegalArgumentException("More than 64 interaction boxes in one shape");
            }
        }
        return List.copyOf(result);
    }
    private static List<Double> starts(double start,double length,double size) {
        if(length<size)return List.of(start+(length-size)/2);
        int count=Math.max(1,(int)Math.ceil(length/size-1e-9));
        List<Double> out=new ArrayList<>();
        for(int i=0;i<count;i++)out.add(start+Math.min(i*size,length-size));
        return out;
    }
}
