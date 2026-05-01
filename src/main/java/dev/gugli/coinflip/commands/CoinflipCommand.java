package dev.gugli.coinflip.commands;

import dev.gugli.coinflip.Coinflip;
import dev.gugli.coinflip.managers.CoinflipManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class CoinflipCommand implements CommandExecutor, TabCompleter {

    private final Coinflip plugin;
    private final CoinflipManager manager;

    public CoinflipCommand(Coinflip plugin, CoinflipManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }

        String prefix = plugin.getConfig().getString("messages.prefix", "");

        if (!player.hasPermission("coinflip.use")) {
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            plugin.getCoinflipGUI().openMain(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && player.hasPermission("coinflip.admin")) {
            plugin.reloadConfig();
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.reload")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.invalid-amount")));
            return true;
        }

        double min = plugin.getConfig().getDouble("coinflip.min-bet", 100);
        double max = plugin.getConfig().getDouble("coinflip.max-bet", 1000000000);
        String symbol = plugin.getConfig().getString("coinflip.currency-symbol", "$");

        if (amount < min) {
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.min-bet", "")
                    .replace("{min}", symbol + min)));
            return true;
        }
        if (amount > max) {
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.max-bet", "")
                    .replace("{max}", symbol + max)));
            return true;
        }
        if (!manager.getEconomy().has(player, amount)) {
            player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.not-enough-money")));
            return true;
        }
        if (manager.hasActiveGame(player.getUniqueId())) {
            player.sendMessage(plugin.color(prefix + "&cYou already have an active coinflip!"));
            return true;
        }

        manager.createGame(player, amount);
        player.sendMessage(plugin.color(prefix + plugin.getConfig().getString("messages.bet-created", "")
                .replace("{amount}", symbol + amount)));
        plugin.getCoinflipGUI().openMain(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return List.of("100", "1000", "10000", "reload");
        return List.of();
    }
}
