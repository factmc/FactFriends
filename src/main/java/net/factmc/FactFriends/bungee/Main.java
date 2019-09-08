package net.factmc.FactFriends.bungee;

import java.util.ArrayList;
import java.util.List;

import net.factmc.FactFriends.bungee.listeners.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin implements Listener {
	
	public static Plugin plugin;
	public static String prefix = ChatColor.DARK_GRAY + "["
				+ ChatColor.DARK_PURPLE + ChatColor.BOLD + "Friends"
				+ ChatColor.DARK_GRAY + "] ";
	
    @Override
    public void onEnable() {
    	plugin = this;
    	registerEvents();
    	//registerCommands();
    	
    	// Register Plugin Messages
    	this.getProxy().registerChannel("factfriends:system");
    	this.getProxy().getPluginManager().registerListener(this, new ServerConnector());
    	
    }
    
    @Override
    public void onDisable() {
    	plugin.getProxy().getScheduler().cancel(plugin);
    	plugin.getLogger().info("Cancelled Tasks");
    	
    	plugin = null;
    }
    
    public static void registerEvents() {
    	List<Listener> listeners = new ArrayList<Listener>();
    	listeners.add(new FriendMessages());
    	
        for (Listener listener : listeners) {
            plugin.getProxy().getPluginManager().registerListener(plugin, listener);
        }
    }
    
    public static void registerCommands() {
    	//plugin.getProxy().getPluginManager().registerCommand(plugin, new FriendCommand());
    }
    
    public static Plugin getPlugin() {
        return plugin;
    }
    
    
    
    public static boolean send(ProxiedPlayer player, Server server) {
    	if (player.getServer() == server) {
    		return false;
    	}
    	
    	player.connect(server.getInfo());
    	return true;
    }
    
}