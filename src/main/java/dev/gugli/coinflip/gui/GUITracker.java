package dev.gugli.coinflip.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUITracker {

    public enum GUIType { MAIN, ANIMATION, RESULT }

    private final Map<UUID, GUIType> states = new HashMap<>();

    public void trackMain(Player player) { states.put(player.getUniqueId(), GUIType.MAIN); }
    public void trackAnimation(Player player) { states.put(player.getUniqueId(), GUIType.ANIMATION); }
    public void trackResult(Player player) { states.put(player.getUniqueId(), GUIType.RESULT); }
    public void remove(Player player) { states.remove(player.getUniqueId()); }
    public GUIType getType(Player player) { return states.get(player.getUniqueId()); }
    public boolean isInGUI(Player player) { return states.containsKey(player.getUniqueId()); }
}
