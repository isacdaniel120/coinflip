package dev.gugli.coinflip.managers;

import dev.gugli.coinflip.Coinflip;
import dev.gugli.coinflip.models.CoinflipGame;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class CoinflipManager {

    private final Coinflip plugin;
    private final Economy economy;
    private final Map<String, CoinflipGame> games = new HashMap<>();
    private final Map<UUID, String> playerGames = new HashMap<>();

    public CoinflipManager(Coinflip plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public CoinflipGame createGame(Player creator, double amount) {
        if (!economy.has(creator, amount)) return null;
        if (playerGames.containsKey(creator.getUniqueId())) return null;

        economy.withdrawPlayer(creator, amount);
        String id = UUID.randomUUID().toString();
        CoinflipGame game = new CoinflipGame(id, creator.getUniqueId(), creator.getName(), amount);
        games.put(id, game);
        playerGames.put(creator.getUniqueId(), id);
        return game;
    }

    public void joinGame(Player opponent, String gameId) {
        CoinflipGame game = games.get(gameId);
        if (game == null || game.getState() != CoinflipGame.State.WAITING) return;
        if (game.getCreatorUUID().equals(opponent.getUniqueId())) {
            opponent.sendMessage(plugin.color(plugin.getConfig().getString("messages.prefix") +
                    plugin.getConfig().getString("messages.cant-flip-self")));
            return;
        }
        if (!economy.has(opponent, game.getAmount())) {
            opponent.sendMessage(plugin.color(plugin.getConfig().getString("messages.prefix") +
                    plugin.getConfig().getString("messages.not-enough-money")));
            return;
        }

        economy.withdrawPlayer(opponent, game.getAmount());
        game.setOpponentUUID(opponent.getUniqueId());
        game.setOpponentName(opponent.getName());
        game.setState(CoinflipGame.State.ANIMATING);
        playerGames.put(opponent.getUniqueId(), gameId);

        startAnimation(game);
    }

    private void startAnimation(CoinflipGame game) {
        int ticks = plugin.getConfig().getInt("coinflip.animation-ticks", 40);
        Player creator = Bukkit.getPlayer(game.getCreatorUUID());
        Player opponent = Bukkit.getPlayer(game.getOpponentUUID());

        if (creator != null) plugin.getCoinflipGUI().openAnimation(creator, game);
        if (opponent != null) plugin.getCoinflipGUI().openAnimation(opponent, game);

        Bukkit.getScheduler().runTaskLater(plugin, () -> finishGame(game), ticks);
    }

    private void finishGame(CoinflipGame game) {
        boolean creatorWins = new Random().nextBoolean();
        UUID winnerUUID = creatorWins ? game.getCreatorUUID() : game.getOpponentUUID();
        UUID loserUUID = creatorWins ? game.getOpponentUUID() : game.getCreatorUUID();
        String winnerName = creatorWins ? game.getCreatorName() : game.getOpponentName();
        String loserName = creatorWins ? game.getOpponentName() : game.getCreatorName();

        game.setWinnerUUID(winnerUUID);
        game.setState(CoinflipGame.State.FINISHED);

        double winnings = game.getAmount() * 2;
        economy.depositPlayer(Bukkit.getOfflinePlayer(winnerUUID), winnings);

        String symbol = plugin.getConfig().getString("coinflip.currency-symbol", "$");

        Player winner = Bukkit.getPlayer(winnerUUID);
        Player loser = Bukkit.getPlayer(loserUUID);

        if (winner != null) plugin.getCoinflipGUI().openResult(winner, game, true);
        if (loser != null) plugin.getCoinflipGUI().openResult(loser, game, false);

        if (plugin.getConfig().getBoolean("coinflip.broadcast-win", true)) {
            String broadcast = plugin.color(plugin.getConfig().getString("coinflip.broadcast-message", "")
                    .replace("{winner}", winnerName)
                    .replace("{loser}", loserName)
                    .replace("{amount}", symbol + game.formatAmount()));
            Bukkit.broadcastMessage(broadcast);
        }

        games.remove(game.getId());
        playerGames.remove(game.getCreatorUUID());
        playerGames.remove(game.getOpponentUUID());
    }

    public void cancelGame(Player creator) {
        String gameId = playerGames.get(creator.getUniqueId());
        if (gameId == null) return;
        CoinflipGame game = games.get(gameId);
        if (game == null || game.getState() != CoinflipGame.State.WAITING) return;

        economy.depositPlayer(creator, game.getAmount());
        games.remove(gameId);
        playerGames.remove(creator.getUniqueId());
        creator.sendMessage(plugin.color(plugin.getConfig().getString("messages.prefix") +
                plugin.getConfig().getString("messages.bet-cancelled")));
    }

    public boolean hasActiveGame(UUID uuid) { return playerGames.containsKey(uuid); }
    public String getPlayerGameId(UUID uuid) { return playerGames.get(uuid); }
    public List<CoinflipGame> getWaitingGames() {
        return games.values().stream()
                .filter(g -> g.getState() == CoinflipGame.State.WAITING)
                .sorted(Comparator.comparingDouble(CoinflipGame::getAmount).reversed())
                .toList();
    }
    public CoinflipGame getGame(String id) { return games.get(id); }
    public Economy getEconomy() { return economy; }
}
