package dev.gugli.coinflip.listeners;

import dev.gugli.coinflip.Coinflip;
import dev.gugli.coinflip.managers.CoinflipManager;
import dev.gugli.coinflip.models.CoinflipGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class GUIListener implements Listener {

    private final Coinflip plugin;
    private final CoinflipManager coinflipManager;

    public GUIListener(Coinflip plugin, CoinflipManager coinflipManager) {
        this.plugin = plugin;
        this.coinflipManager = coinflipManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();

        if (title.contains("Coinflip")) {
            e.setCancelled(true);
            handleCoinflipGUI(player, e.getRawSlot(), title);
        }
    }

    private void handleCoinflipGUI(Player player, int slot, String title) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        if (title.contains("Flipping") || title.contains("Won") || title.contains("Lost")) return;

        if (slot == 49) {
            player.closeInventory();
            player.sendMessage(plugin.color(prefix + "&7Use &f/cf <amount> &7to create a coinflip!"));
            return;
        }

        if (slot == 47 && coinflipManager.hasActiveGame(player.getUniqueId())) {
            coinflipManager.cancelGame(player);
            plugin.getCoinflipGUI().openMain(player);
            return;
        }

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        List<CoinflipGame> games = coinflipManager.getWaitingGames();
        for (int i = 0; i < slots.length && i < games.size(); i++) {
            if (slot == slots[i]) {
                CoinflipGame game = games.get(i);
                if (!game.getCreatorUUID().equals(player.getUniqueId())) {
                    coinflipManager.joinGame(player, game.getId());
                }
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            plugin.getGuiTracker().remove(player);
        }
    }
}
