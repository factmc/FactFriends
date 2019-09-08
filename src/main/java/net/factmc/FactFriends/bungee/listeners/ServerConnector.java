package net.factmc.FactFriends.bungee.listeners;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.factmc.FactFriends.bungee.Data;
import net.factmc.FactFriends.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerConnector implements Listener {
	
	public void parseRequest(String msg) {
		//DEBUG=Main.getPlugin().getProxy().getPlayer(UUID.fromString("116bf6d7-fc1d-4296-96ad-3a289a61a454")).sendMessage(new TextComponent("Received: " + msg));
		
		String[] request = msg.split("[.]");
		ProxiedPlayer player = Main.getPlugin().getProxy().getPlayer(UUID.fromString(request[0]));
		
		if (request[1].equalsIgnoreCase("send") && request.length > 2) {
			
			if (request[2].equalsIgnoreCase("list")) {
				
				sendMessage(getList(player), player.getServer());
				
			}
			
			else if (request[2].equalsIgnoreCase("requests")) {
				
				sendMessage(getRequestList(player), player.getServer());
				
			}
			
			else if (request[2].equalsIgnoreCase("jump")) {
				
				if (request.length > 3) {
					
					ProxiedPlayer to = Main.getPlugin().getProxy().getPlayer(UUID.fromString(request[3]));
					if (to != null && to.isConnected()) {
						
						player.connect(to.getServer().getInfo());
						
					}
					
				}
				
			}
			
			
			else if (request[2].equalsIgnoreCase("add")) {
				
				if (request.length > 3) {
					ProxiedPlayer sendTo = Main.getPlugin().getProxy().getPlayer(request[3]);
					short result = Data.addRequest(player, sendTo);
					
					String message = "";
					switch (result) {
					
					case 0:
						message = ChatColor.RED + "You have already sent a friend request to " + sendTo.getName();
						break;
					case 1:
						message = ChatColor.RED + "You have already received a friend request from " + sendTo.getName();
						break;
					case 2:
						message = ChatColor.BLUE + "A friend request has been sent to " + sendTo.getName();
						break;
					case 3:
						message = ChatColor.RED + "You can't send a friend request to yourself!";
						break;
					default:
						message = ChatColor.RED + request[3] + " is not online";
					
					}
					
					player.sendMessage(new TextComponent(Main.prefix + message));
					
					if (result < 3) {
						sendMessage(player.getUniqueId().toString() + ".open-requests", player.getServer());
					}
					
				}
				
			}
			
			else if (request[2].equalsIgnoreCase("remove")) {
				
				if (request.length > 3) {
					
					UUID remove = UUID.fromString(request[3]);
					if (Data.getFriend(remove, player.getUniqueId()) == null) {
						player.sendMessage(new TextComponent(Main.prefix + ChatColor.RED + "You are not friends with that player"));
					}
					else {
						Data.removeFriend(player.getUniqueId(), remove);
						sendMessage(player.getUniqueId().toString() + ".open-main", player.getServer());
					}
					
				}
				
			}
			
			else if (request[2].equalsIgnoreCase("remove-all")) {
				
				Data.removeAllFriends(player.getUniqueId());
				sendMessage(player.getUniqueId().toString() + ".open-main", player.getServer());
				
			}
			
		}
		
		
		else if (request.length > 2) {
			
			UUID uuid = UUID.fromString(request[2]);
			
			if (request[1].equalsIgnoreCase("accept")) {
				
				Data.respondRequest(player, uuid, true);
				
			}
			
			else if (request[1].equalsIgnoreCase("deny")) {
				
				Data.respondRequest(player, uuid, false);
				
			}
			
			else if (request[1].equalsIgnoreCase("cancel")) {
		
				Data.revokeRequest(player, uuid);
		
			}
			
			else return;
			
			sendMessage(player.getUniqueId().toString() + ".open-requests", player.getServer());
			
		}
		
	}
	
	public String getList(ProxiedPlayer player) {
		
		List<String> friendsList = Data.getFriends(player.getUniqueId());
		
		String list = player.getUniqueId().toString() + ".list";
		for (String info : friendsList) {
			
			list += "." + info;
			list += "," + find(UUID.fromString(info.split(",")[0]));
			
		}
		
		return list;
		
	}
	
	public String getRequestList(ProxiedPlayer player) {
		
		String list = player.getUniqueId().toString() + ".requests.";
		
		boolean oFirst = true;
		String outgoing = "";
		
		boolean iFirst = true;
		String incoming = "";
		
		for (ProxiedPlayer[] array : Data.requests) {
			
			if (array[0] == player) {
				
				// Outgoing request
				if (!oFirst) outgoing += ",";
				else oFirst = false;
				
				outgoing += array[1].getUniqueId();
				
			}
			
			else if (array[1] == player) {
				
				// Incoming requests
				if (!iFirst) incoming += ",";
				else iFirst = false;
				
				incoming += array[0].getUniqueId();
				
			}
			
		}
		
		if (incoming.equals("")) incoming = "null";
		if (outgoing.equals("")) outgoing = "null";
		
		list += incoming + "." + outgoing;
		return list;
		
	}
	
	public String find(UUID uuid) {
		
		ProxiedPlayer player = Main.getPlugin().getProxy().getPlayer(uuid);
		
		if (player == null) return "offline";
		else return player.getServer().getInfo().getName();
		
	}
	
	
	
	// PLUGIN MESSAGES
    public static boolean sendMessage(String msg, Server server) {
    	/*Main.getPlugin().getLogger().info("Sending Plugin Message: " + msg);
    	new Throwable().printStackTrace();//DEBUG*/
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
 
        server.sendData("factfriends:system", stream.toByteArray());
        //DEBUG=Main.getPlugin().getProxy().getPlayer(UUID.fromString("116bf6d7-fc1d-4296-96ad-3a289a61a454")).sendMessage(new TextComponent("Sent: " + msg));
        return true;
    }
 
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals("factfriends:system")) {
            return;
        }
   
        if (!(event.getSender() instanceof Server)) {
            return;
        }
 
        ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
			parseRequest(in.readUTF());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}