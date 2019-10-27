package net.factmc.FactFriends.bukkit;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.BukkitMain;
import net.factmc.FactFriends.Data;
import net.factmc.FactFriends.bukkit.gui.FriendsGUI;
import net.factmc.FactFriends.bukkit.gui.RequestsGUI;

public class FriendsCommand implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("friends")) {
			
			if (sender instanceof Player) {
				Player player = (Player) sender;
				
				if (args.length > 0) {
					
					if (args[0].equalsIgnoreCase("requests")) {
						RequestsGUI.open(player);
						return true;
					}
					
					else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
						
						if (args.length < 2) {
							sender.sendMessage(Data.PREFIX + ChatColor.RED + "You must specifiy a player name");
							return false;
						}
						
						UUID friend = FactSQL.getInstance().getUUID(args[1]);
						if (friend == null) {
							sender.sendMessage(Data.PREFIX + ChatColor.RED + "That player has never joined the network");
							return false;
						}
						
						if (args[0].equalsIgnoreCase("add")) {
							
							if (Data.hasRequest(player.getUniqueId(), friend)) {
								sender.sendMessage(Data.PREFIX + ChatColor.RED + "You have already sent a friend request to "
										+ FactSQL.getInstance().getName(friend));
								return false;
							}
							
							else if (Data.areFriends(player.getUniqueId(), friend)) {
								sender.sendMessage(Data.PREFIX + ChatColor.RED + "You are already friends with "
										+ FactSQL.getInstance().getName(friend));
								return false;
							}
							
							else if (Data.addRequest(player.getUniqueId(), friend)) {
								sender.sendMessage(Data.PREFIX + ChatColor.BLUE + "A friend request has been sent to "
										+ FactSQL.getInstance().getName(friend));
								return true;
							}
							
							else {
								sender.sendMessage(Data.PREFIX + ChatColor.RED + "An error occured");
								return false;
							}
							
						}
						
						else if (args[0].equalsIgnoreCase("remove")) {
							
							if (!Data.areFriends(player.getUniqueId(), friend)) {
								sender.sendMessage(Data.PREFIX + ChatColor.RED + "You are not friends with "
										+ FactSQL.getInstance().getName(friend));
								return false;
							}
							
							else if (Data.removeFriend(player.getUniqueId(), friend)) {
								sender.sendMessage(Data.PREFIX + ChatColor.BLUE + "You are no longer friends with "
										+ FactSQL.getInstance().getName(friend));
								return true;
							}
							
							else {
								sender.sendMessage(Data.PREFIX + ChatColor.RED + "An error occured");
								return false;
							}
							
						}
						
					}
					
				}
				
				FriendsGUI.open(player);
				return true;

			}
			
			else {
				sender.sendMessage(ChatColor.RED + "Only players can use that command");
				return false;
			}
			
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("friends")) {
			
			if (args.length < 2) return CoreUtils.filter(CoreUtils.toList("add", "remove"), args[0]);
			
			else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
				
				if (args.length == 2) return CoreUtils.filter(BukkitMain.toList(Bukkit.getOnlinePlayers()), args[1]);
				
			}
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}
	
}