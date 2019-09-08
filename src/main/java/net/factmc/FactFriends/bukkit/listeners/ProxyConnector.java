package net.factmc.FactFriends.bukkit.listeners;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import net.factmc.FactFriends.bukkit.Main;
import net.factmc.FactFriends.bukkit.gui.FriendsGUI;
import net.factmc.FactFriends.bukkit.gui.RequestsGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class ProxyConnector implements PluginMessageListener {
	
	public void parseResponse(String msg) {
		//DEBUG=Bukkit.getPlayer(UUID.fromString("116bf6d7-fc1d-4296-96ad-3a289a61a454")).sendMessage("Received: " + msg);
		
		String[] request = msg.split("[.]");
		Player player = Bukkit.getPlayer(UUID.fromString(request[0]));
		
		if (request[1].equalsIgnoreCase("list")) {
			
			FriendsGUI.open(player, convertList(request));
			
		}
		
		else if (request[1].equalsIgnoreCase("requests")) {
			
			RequestsGUI.open(player, convertRequests(request));
			
		}
		
		
		else if (request[1].equalsIgnoreCase("open-requests")) {
			
			RequestsGUI.request(player);
			
		}
		
		else if (request[1].equalsIgnoreCase("open-main")) {
			
			FriendsGUI.request(player);
			
		}
		
		
		else if (request[1].equalsIgnoreCase("received")) {
			
			TextComponent tc = new TextComponent(ChatColor.GRAY + "here");
			tc.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/friends requests"));
			
			player.spigot().sendMessage(new TextComponent(ChatColor.BLUE + "Click "), tc,
					new TextComponent(ChatColor.BLUE + " to open the requests menu"));
			
			/*String json = "[\"\",{\"text\":\"Click \",\"color\":\"blue\"},"
					+ "{\"text\":\"here\",\"color\":\"gray\",\"underlined\":true,\"clickEvent\":"
					+ "{\"action\":\"run_command\",\"value\":\"/friends requests\"}},"
					+ "{\"text\":\" to open the requests menu\",\"color\":\"blue\",\"underlined\":false}]";*/
			
		}
		
	}
	
	public List<String[]> convertList(String[] request) {
		
		if (!request[1].equalsIgnoreCase("list")) {
			return null;
		}
		
		List<String[]> list = new ArrayList<String[]>();
		for (int i = 2; i < request.length; i++) {
			
			list.add(request[i].split(","));
			
		}
		
		return list;
		
	}
	
	public List<List<String>> convertRequests(String[] request) {
		
		if (!request[1].equalsIgnoreCase("requests")) {
			return null;
		}
		
		List<String> incoming = new ArrayList<String>();
		List<String> outgoing = new ArrayList<String>();
		
		for (String next : request[2].split(",")) {
			incoming.add(next);
		}
		for (String next : request[3].split(",")) {
			outgoing.add(next);
		}
		
		
		List<List<String>> list = new ArrayList<List<String>>();
		list.add(incoming);
		list.add(outgoing);
		return list;
		
	}
	
	
	public static void getList(Player player) {
		sendMessage(player.getUniqueId().toString() + ".send.list", player);
	}
	public static void getRequests(Player player) {
		sendMessage(player.getUniqueId().toString() + ".send.requests", player);
	}
	public static void jumpTo(Player player, UUID to) {
		sendMessage(player.getUniqueId().toString() + ".send.jump." + to.toString(), player);
	}
	
	public static void add(Player player, String addName) {
		sendMessage(player.getUniqueId().toString() + ".send.add." + addName, player);
	}
	public static void remove(Player player, UUID remove) {
		sendMessage(player.getUniqueId().toString() + ".send.remove." + remove.toString(), player);
	}
	public static void removeAll(Player player) {
		sendMessage(player.getUniqueId().toString() + ".send.remove-all", player);
	}
	
	public static void accept(Player player, UUID accept) {
		sendMessage(player.getUniqueId().toString() + ".accept." + accept.toString(), player);
	}
	public static void deny(Player player, UUID deny) {
		sendMessage(player.getUniqueId().toString() + ".deny." + deny.toString(), player);
	}
	public static void cancel(Player player, UUID cancel) {
		sendMessage(player.getUniqueId().toString() + ".cancel." + cancel.toString(), player);
	}
	
	
	
	// PLUGIN MESSAGES
	public static boolean sendMessage(String msg, Player player) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
 
        player.sendPluginMessage(Main.getPlugin(), "factfriends:system", stream.toByteArray());
        //DEBUG=Bukkit.getPlayer(UUID.fromString("116bf6d7-fc1d-4296-96ad-3a289a61a454")).sendMessage("Sent: " + msg);
        return true;
    }
 
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("factfriends:system")) {
            return;
        }
   
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
			parseResponse(in.readUTF());
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
	
}