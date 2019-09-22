package net.factmc.FactFriends.bungee.listeners;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FriendStatus implements Listener {
	
	public static void sendStatusList(List<String> list, ProxiedPlayer player) {
		
		try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				DataOutputStream dos = new DataOutputStream(baos)
			) {
			
			dos.writeUTF(player.getUniqueId().toString());
			for (String status : list) {
				dos.writeUTF(status);
			}
	        
	        player.getServer().getInfo().sendData("factfriends:status", baos.toByteArray());
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@EventHandler
	public void onPluginMessageReceived(PluginMessageEvent event) {
		if (!event.getTag().equals("factfriends:status")) return;
		
		try (
				ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
				DataInputStream dis = new DataInputStream(bais);
			) {
			
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(dis.readUTF()));
			List<UUID> list = new ArrayList<UUID>();
			try {
				while (true) {
					list.add(UUID.fromString(dis.readUTF()));
				}
			} catch (EOFException ignore) {};
			
			if (player != null) {
				List<String> statusList = new ArrayList<String>();
				for (UUID uuid : list) {
					ProxiedPlayer p = ProxyServer.getInstance().getPlayer(uuid);
					if (p != null) statusList.add(player.getServer().getInfo().getName());
					else statusList.add("offline");
				}
				sendStatusList(statusList, player);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}