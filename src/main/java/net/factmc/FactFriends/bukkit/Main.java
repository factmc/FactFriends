package net.factmc.FactFriends.bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.factmc.FactCore.FactSQL;
import net.factmc.FactFriends.bukkit.gui.FriendsGUI;
import net.factmc.FactFriends.bukkit.gui.RequestsGUI;

public class Main extends JavaPlugin implements Listener {
	
	public static JavaPlugin plugin;
	
    @Override
    public void onEnable() {
    	plugin = this;
    	
    	registerPluginMessages();
    	registerEvents();
    	registerCommands();
    	
    }
    
    @Override
    public void onDisable() {
    	plugin = null;
    }
    
    public void registerPluginMessages() {
    	Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    	Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "factfriends:status");
    	Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "factfriends:status", new FriendsGUI());
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
    
    
    public static void sendMessage(UUID uuid, String message) {
		
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				try (
						ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
						DataOutputStream dos = new DataOutputStream(baos)
					) {
					
			        dos.writeUTF("Message");
			        dos.writeUTF(FactSQL.getInstance().getName(uuid));
			        dos.writeUTF(message);
			        
			        Optional<? extends org.bukkit.entity.Player> player = Bukkit.getOnlinePlayers().stream().findAny();
			        if (player.isPresent()) player.get().sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());
			        
				} catch (IOException e){
					e.printStackTrace();
				}
				
			}
		});
		
	}
    
}