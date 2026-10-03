package dev.serverblocks;

import org.bukkit.entity.Player;
import org.bukkit.event.*;

/** Protection plugins may veto server-driven changes before any block is changed. */
public final class BlockMutationEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String owner, reason;
    private final Position from, to;
    private final Player player;
    private boolean cancelled;
    public BlockMutationEvent(String owner, String reason, Position from, Position to, Player player) {
        this.owner=owner;this.reason=reason;this.from=from;this.to=to;this.player=player;
    }
    public String owner() { return owner; }
    public String reason() { return reason; }
    public Position from() { return from; }
    public Position to() { return to; }
    public Player player() { return player; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean value) { cancelled=value; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
