package dev.gugli.coinflip.models;

import java.util.UUID;

public class CoinflipGame {

    public enum State { WAITING, ANIMATING, FINISHED }

    private final String id;
    private final UUID creatorUUID;
    private final String creatorName;
    private final double amount;
    private UUID opponentUUID;
    private String opponentName;
    private UUID winnerUUID;
    private State state;
    private final long createdAt;

    public CoinflipGame(String id, UUID creatorUUID, String creatorName, double amount) {
        this.id = id;
        this.creatorUUID = creatorUUID;
        this.creatorName = creatorName;
        this.amount = amount;
        this.state = State.WAITING;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public UUID getCreatorUUID() { return creatorUUID; }
    public String getCreatorName() { return creatorName; }
    public double getAmount() { return amount; }
    public UUID getOpponentUUID() { return opponentUUID; }
    public void setOpponentUUID(UUID opponentUUID) { this.opponentUUID = opponentUUID; }
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public UUID getWinnerUUID() { return winnerUUID; }
    public void setWinnerUUID(UUID winnerUUID) { this.winnerUUID = winnerUUID; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getCreatedAt() { return createdAt; }

    public String formatAmount() {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000);
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000);
        return String.format("%.0f", amount);
    }
}
