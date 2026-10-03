package dev.serverblocks;

import java.util.Objects;

/** Stable item_model identifier, independent of item material and numeric model data. */
public record ModelKey(String namespace, String path) {
    public ModelKey {
        Objects.requireNonNull(namespace);Objects.requireNonNull(path);
        if(!namespace.matches("[a-z0-9_.-]+")||!path.matches("[a-z0-9_/.-]+")||path.startsWith("/")||path.endsWith("/")||path.contains(".."))
            throw new IllegalArgumentException("Invalid model identifier");
    }
    public static ModelKey parse(String value){
        String[] parts=value.split(":",-1);if(parts.length!=2)throw new IllegalArgumentException("Use namespace:path");
        return new ModelKey(parts[0],parts[1]);
    }
    @Override public String toString(){return namespace+":"+path;}
    public String definitionPath(){return "assets/"+namespace+"/items/"+path+".json";}
}
