package net.factmc.FactFriends.bungee;

import java.util.UUID;

import net.factmc.FactFriends.bungee.listeners.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin implements Listener {
	
	public static Plugin plugin;
	
    @Override
    public void onEnable() {
    	plugin = this;
    	
    	registerPluginMessages();
    	registerEvents();
    	
    }
    
    @Override
    public void onDisable() {
    	plugin = null;
    }
    
    public void registerPluginMessages() {
    	plugin.getProxy().registerChannel("factfriends:status");
    	plugin.getProxy().getPluginManager().registerListener(plugin, new FriendStatus());
    }
    
    public void registerEvents() {
    	plugin.getProxy().getPluginManager().registerListener(plugin, new FriendMessages());	
    }
    
    public void registerCommands() {
    	//plugin.getProxy().getPluginManager().registerCommand(plugin, new FriendCommand());
    }
    
    public static Plugin getPlugin() {
        return plugin;
    }
    
    
    public static void sendMessage(UUID uuid, String message) {
		
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
		if (player != null) {
			player.sendMessage(new net.md_5.bungee.api.chat.TextComponent(message));
		}
		
	}
    
}