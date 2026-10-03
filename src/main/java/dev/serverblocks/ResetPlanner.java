package dev.serverblocks;

import java.util.*;
import java.util.function.Predicate;

/** Validate the complete move set before touching the world; swaps are legal. */
public final class ResetPlanner {
    private ResetPlanner() {}
    public static boolean canReset(Map<Position, Position> moves, Predicate<Position> destinationAvailable) {
        Set<Position> destinations = new HashSet<>();
        for (Position target : moves.values()) {
            if (target == null) return false;
            if (!destinations.add(target)) return false;
            if (!moves.containsKey(target) && !destinationAvailable.test(target)) return false;
        }
        return true;
    }
}
