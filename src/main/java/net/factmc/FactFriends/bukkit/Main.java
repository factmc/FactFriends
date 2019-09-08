package net.factmc.FactFriends.bukkit;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.factmc.FactFriends.bukkit.commands.FriendsCommand;
import net.factmc.FactFriends.bukkit.gui.FriendsGUI;
import net.factmc.FactFriends.bukkit.gui.RequestsGUI;
import net.factmc.FactFriends.bukkit.listeners.ProxyConnector;

public class Main extends JavaPlugin implements Listener {
	
	public static JavaPlugin plugin;
	public static String prefix = ChatColor.DARK_GRAY + "["
			+ ChatColor.DARK_PURPLE + ChatColor.BOLD + "Friends"
			+ ChatColor.DARK_GRAY + "] ";
	
    @Override
    public void onEnable() {
    	plugin = this;
    	
    	registerEvents();
    	registerCommands();
    	
    	// Register Plugin Messages
    	this.getServer().getMessenger().registerIncomingPluginChannel(this, "factfriends:system", new ProxyConnector());
    	this.getServer().getMessenger().registerOutgoingPluginChannel(this, "factfriends:system");
    }
    
    @Override
    public void onDisable() {
    	plugin = null;
    }
    
    public void registerEvents() {
    	
    	Bukkit.getPluginManager().registerEvents(new FriendsGUI(), this);
    	Bukkit.getPluginManager().registerEvents(new RequestsGUI(), this);
    	
    }
    
    public void registerCommands() {
    	plugin.getCommand("friends").setExecutor(new FriendsCommand());
    	plugin.getCommand("friends").setAliases(Collections.singletonList("f"));
    }
    
    public static JavaPlugin getPlugin() {
        return plugin;
    }
    
}