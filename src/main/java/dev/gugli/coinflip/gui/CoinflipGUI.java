package dev.gugli.coinflip.gui;

import dev.gugli.coinflip.Coinflip;
import dev.gugli.coinflip.managers.CoinflipManager;
import dev.gugli.coinflip.models.CoinflipGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class CoinflipGUI {

    private final Coinflip plugin;
    private final CoinflipManager manager;

    public CoinflipGUI(Coinflip plugin, CoinflipManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openMain(Player player) {
        List<CoinflipGame> games = manager.getWaitingGames();
        String symbol = plugin.getConfig().getString("coinflip.currency-symbol", "$");

        Inventory inv = Bukkit.createInventory(null, 54,
                plugin.color("&8» &6&lCoinflip &8| &7" + games.size() + " active"));

        fillBorder(inv);

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        for (int i = 0; i < slots.length && i < games.size(); i++) {
            inv.setItem(slots[i], buildGameItem(games.get(i), player));
        }

        ItemStack create = new ItemStack(Material.GOLD_INGOT);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(plugin.color("&6&l+ Create Coinflip"));
        createMeta.setLore(List.of(
                plugin.color("&7Use: &f/cf <amount>"),
                plugin.color("&7Min: &f" + symbol + plugin.getConfig().getString("coinflip.min-bet")),
                plugin.color("&7Max: &f" + symbol + plugin.getConfig().getString("coinflip.max-bet"))
        ));
        createMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        createMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        create.setItemMeta(createMeta);
        inv.setItem(49, create);

        if (manager.hasActiveGame(player.getUniqueId())) {
            ItemStack cancel = new ItemStack(Material.BARRIER);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName(plugin.color("&c&lCancel My Coinflip"));
            cancelMeta.setLore(List.of(plugin.color("&7Click to cancel and get refund")));
            cancel.setItemMeta(cancelMeta);
            inv.setItem(47, cancel);
        }

        player.openInventory(inv);
        plugin.getGuiTracker().trackMain(player);
    }

    private ItemStack buildGameItem(CoinflipGame game, Player viewer) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(game.getCreatorUUID()));
        String symbol = plugin.getConfig().getString("coinflip.currency-symbol", "$");

        meta.setDisplayName(plugin.color("&f&l" + game.getCreatorName() + "'s Coinflip"));
        List<String> lore = new ArrayList<>();
        lore.add(plugin.color("&8─────────────────"));
        lore.add(plugin.color("&7Bet: &6" + symbol + game.formatAmount()));
        lore.add(plugin.color("&7Win: &a" + symbol + formatDouble(game.getAmount() * 2)));
        lore.add(plugin.color("&8─────────────────"));
        if (game.getCreatorUUID().equals(viewer.getUniqueId())) {
            lore.add(plugin.color("&cThis is your coinflip"));
        } else {
            lore.add(plugin.color("&eClick to accept!"));
        }
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    public void openAnimation(Player player, CoinflipGame game) {
        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.color("&8» &6Coinflip &8| &7Flipping..."));

        ItemStack black = makeGlass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, black);

        ItemStack heads = new ItemStack(Material.SUNFLOWER);
        ItemMeta headsMeta = heads.getItemMeta();
        headsMeta.setDisplayName(plugin.color("&e&lHeads"));
        heads.setItemMeta(headsMeta);
        inv.setItem(11, heads);

        ItemStack tails = new ItemStack(Material.IRON_INGOT);
        ItemMeta tailsMeta = tails.getItemMeta();
        tailsMeta.setDisplayName(plugin.color("&7&lTails"));
        tails.setItemMeta(tailsMeta);
        inv.setItem(15, tails);

        ItemStack vs = new ItemStack(Material.NETHER_STAR);
        ItemMeta vsMeta = vs.getItemMeta();
        vsMeta.setDisplayName(plugin.color("&6&lVS"));
        String symbol = plugin.getConfig().getString("coinflip.currency-symbol", "$");
        vsMeta.setLore(List.of(
                plugin.color("&f" + game.getCreatorName() + " &7vs &f" + game.getOpponentName()),
                plugin.color("&7Pot: &6" + symbol + formatDouble(game.getAmount() * 2))
        ));
        vs.setItemMeta(vsMeta);
        inv.setItem(13, vs);

        player.openInventory(inv);

        int[] animSlots = {11, 15};
        runAnimation(player, inv, game, animSlots, 0);
    }

    private void runAnimation(Player player, Inventory inv, CoinflipGame game, int[] slots, int tick) {
        if (tick >= plugin.getConfig().getInt("coinflip.animation-ticks", 40) - 5) return;
        if (!player.isOnline()) return;

        boolean flip = tick % 2 == 0;
        inv.setItem(slots[0], flip ? makeGlass(Material.YELLOW_STAINED_GLASS_PANE) : makeGlass(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(slots[1], flip ? makeGlass(Material.GRAY_STAINED_GLASS_PANE) : makeGlass(Material.YELLOW_STAINED_GLASS_PANE));

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                runAnimation(player, inv, game, slots, tick + 1), 2L);
    }

    public void openResult(Player player, CoinflipGame game, boolean won) {
        Inventory inv = Bukkit.createInventory(null, 27,
                plugin.color(won ? "&8» &a&lYou Won!" : "&8» &c&lYou Lost!"));

        ItemStack bg = makeGlass(won ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        String symbol = plugin.getConfig().getString("coinflip.currency-symbol", "$");
        ItemStack result = new ItemStack(won ? Material.GOLD_BLOCK : Material.COAL_BLOCK);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(plugin.color(won ? "&a&lYou Won!" : "&c&lYou Lost!"));
        meta.setLore(List.of(
                plugin.color("&8─────────────────"),
                plugin.color(won ? "&aYou won &6" + symbol + formatDouble(game.getAmount() * 2) : "&cYou lost &6" + symbol + game.formatAmount()),
                plugin.color("&7Against: &f" + (won ? game.getOpponentName() : game.getCreatorName())),
                plugin.color("&8─────────────────")
        ));
        result.setItemMeta(meta);
        inv.setItem(13, result);

        player.openInventory(inv);
        Bukkit.getScheduler().runTaskLater(plugin, (Runnable) player::closeInventory, 60L);
    }

    private void fillBorder(Inventory inv) {
        ItemStack black = makeGlass(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack gray = makeGlass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, black);
        for (int i = 45; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, gray);
        }
        for (int row = 1; row <= 3; row++) {
            inv.setItem(row * 9, gray);
            inv.setItem(row * 9 + 8, gray);
        }
        inv.setItem(36, black); inv.setItem(37, black); inv.setItem(38, black);
        inv.setItem(39, black); inv.setItem(43, black); inv.setItem(44, black);
        inv.setItem(40, gray); inv.setItem(41, gray); inv.setItem(42, gray);
    }

    private ItemStack makeGlass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private String formatDouble(double amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000);
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000);
        return String.format("%.0f", amount);
    }
}
