package dev.gugli.coinflip;

import dev.gugli.coinflip.commands.CoinflipCommand;
import dev.gugli.coinflip.gui.CoinflipGUI;
import dev.gugli.coinflip.gui.GUITracker;
import dev.gugli.coinflip.listeners.GUIListener;
import dev.gugli.coinflip.managers.CoinflipManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Coinflip extends JavaPlugin {

    private Economy economy;
    private CoinflipManager coinflipManager;
    private CoinflipGUI coinflipGUI;
    private GUITracker guiTracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getDataFolder().mkdirs();

        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling Coinflip.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        guiTracker = new GUITracker();
        coinflipManager = new CoinflipManager(this, economy);
        coinflipGUI = new CoinflipGUI(this, coinflipManager);

        getServer().getPluginManager().registerEvents(new GUIListener(this, coinflipManager), this);

        CoinflipCommand cfCmd = new CoinflipCommand(this, coinflipManager);
        getCommand("coinflip").setExecutor(cfCmd);
        getCommand("coinflip").setTabCompleter(cfCmd);

        getLogger().info("Coinflip enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Coinflip disabled.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public CoinflipManager getCoinflipManager() { return coinflipManager; }
    public CoinflipGUI getCoinflipGUI() { return coinflipGUI; }
    public GUITracker getGuiTracker() { return guiTracker; }
}
