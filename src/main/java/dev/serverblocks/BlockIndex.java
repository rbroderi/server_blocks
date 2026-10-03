package dev.serverblocks;

import java.util.*;
import java.util.function.Function;

/** Per-consumer identity index; materials are never used as custom block IDs. Main-thread confined. */
public final class BlockIndex<T> {
    private final Map<Position, T> entries = new LinkedHashMap<>();
    private final Function<T, Position> position;
    public BlockIndex(Function<T, Position> position) { this.position = Objects.requireNonNull(position); }
    public T at(Position p) { return entries.get(p); }
    public boolean contains(Position p) { return entries.containsKey(p); }
    public List<T> snapshot() { return List.copyOf(entries.values()); }
    public int size() { return entries.size(); }
    public void add(T entry) {
        Position p = Objects.requireNonNull(position.apply(entry));
        if (entries.putIfAbsent(p, entry) != null) throw new IllegalArgumentException("Already registered: " + p);
    }
    public void remove(T entry) { entries.remove(position.apply(entry), entry); }
    /** Caller updates the object's coordinate only after a successful move. */
    public void move(T entry, Position from, Position to) {
        if (entries.get(from) != entry) throw new IllegalArgumentException("Stale source");
        if (from.equals(to)) return;
        if (entries.containsKey(to)) throw new IllegalArgumentException("Occupied target");
        entries.remove(from); entries.put(to, entry);
    }
    public List<T> within(Position center, int horizontal, int up, int down) {
        return entries.entrySet().stream().filter(e -> e.getKey().within(center, horizontal, up, down)).map(Map.Entry::getValue).toList();
    }
}
