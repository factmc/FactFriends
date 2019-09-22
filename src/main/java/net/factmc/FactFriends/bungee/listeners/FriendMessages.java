package net.factmc.FactFriends.bungee.listeners;

import java.util.UUID;

import net.factmc.FactFriends.Data;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FriendMessages implements Listener {
	
	@EventHandler
	public void onLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		
		sendMsgToFriends(player.getUniqueId(),
				new TextComponent(Data.PREFIX + ChatColor.BLUE + player.getName() + " joined the network"));
		
	}
	
	@EventHandler
	public void onLogout(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		
		sendMsgToFriends(player.getUniqueId(),
				new TextComponent(Data.PREFIX + ChatColor.BLUE + player.getName() + " left the network"));
		
	}
	
	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		String server = event.getPlayer().getServer().getInfo().getName();
		
		sendMsgToFriends(player.getUniqueId(),
				new TextComponent(Data.PREFIX + ChatColor.BLUE + player.getName() + " joined the " + server + " server"));
		
	}
	
	public void sendMsgToFriends(UUID uuid, BaseComponent msg) {
		for (UUID friend : Data.getFriends(uuid)) {
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(friend);
			if (player != null) player.sendMessage(msg);
		}
	}
	
}