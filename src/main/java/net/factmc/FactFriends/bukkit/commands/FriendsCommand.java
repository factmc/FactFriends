package net.factmc.FactFriends.bukkit.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactFriends.bukkit.Main;
import net.factmc.FactFriends.bukkit.gui.FriendsGUI;
import net.factmc.FactFriends.bukkit.gui.RequestsGUI;
import net.factmc.FactFriends.bukkit.listeners.ProxyConnector;

public class FriendsCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("friends")) {
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				
				if (args.length > 0) {
					
					if (args[0].equalsIgnoreCase("requests")) {
						RequestsGUI.request(player);
						return true;
					}
					
					else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
						
						if (args.length < 2) {
							sender.sendMessage(Main.prefix + ChatColor.RED + "You must specifiy a player name");
							return false;
						}
						
						if (args[0].equalsIgnoreCase("add")) {
							ProxyConnector.add(player, args[1]);
							return true;
						}
						
						else if (args[0].equalsIgnoreCase("remove")) {
							
							UUID remove = FactSQLConnector.getUUID(args[1]);
							if (remove == null) {
								sender.sendMessage(Main.prefix + ChatColor.RED + "You are not friends with that player");
								return false;
							}
							else {
								ProxyConnector.remove(player, remove);
								return true;
							}
							
						}
						
					}
					
					else if (args[0].equalsIgnoreCase("remove")) {
						
						if (args.length < 2) {
							sender.sendMessage(Main.prefix + ChatColor.RED + "You must specifiy a player name");
							return false;
						}
						
						
						
					}
					
				}
				
				FriendsGUI.request(player);
				return true;

			}
			
			else {
				
				sender.sendMessage(ChatColor.RED + "Only players can use that command");
				return false;
				
			}
			
		}
		
		return false;
	}
	
}