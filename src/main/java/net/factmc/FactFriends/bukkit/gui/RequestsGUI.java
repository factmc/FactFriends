package net.factmc.FactFriends.bukkit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.factmc.FactFriends.bukkit.listeners.ProxyConnector;

public class RequestsGUI implements Listener {
	
	private static boolean loaded = false;
	final public static int SIZE = 27;
	final public static String TITLE = ChatColor.DARK_PURPLE + "Active Friend Requests";
	
	public static void request(Player player) {
		ProxyConnector.getRequests(player);
	}
	
	public static void open(Player player, List<List<String>> requestList) {
		
		Inventory gui = player.getServer().createInventory(player, SIZE, TITLE);
		
		ItemStack panes = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
		for (int i = 4; i < SIZE; i += 9) {
			gui.setItem(i, panes);
		}
		
		ItemStack back = FriendsGUI.getStack(Material.ARROW, ChatColor.LIGHT_PURPLE + "Return to friends list",
				ChatColor.GRAY + "Return to the main friends menu");
		gui.setItem(22, back);
		
		ItemStack incomingItem = FriendsGUI.getStack(Material.GOLDEN_CARROT, ChatColor.GREEN + "Incoming Requests");
		gui.setItem(2, incomingItem);
		
		ItemStack outgoingItem = FriendsGUI.getStack(Material.SPECTRAL_ARROW, ChatColor.RED + "Outgoing Requests");
		gui.setItem(6, outgoingItem);
		
		
		// Incoming
		int j = 9;
		List<String> incoming = requestList.get(0);
		for (int i = 0; i < incoming.size(); i++) {
			if (incoming.get(i).equals("null")) break;
			
			UUID uuid = UUID.fromString(incoming.get(i));
			String name = Bukkit.getOfflinePlayer(uuid).getName();
			
			ItemStack skull = FriendsGUI.getSkull(uuid, ChatColor.RESET + name,
					ChatColor.GREEN + "Left click to accept", ChatColor.RED + "Right click to deny");
			gui.setItem(j, skull);
			
			if ((j-3)%9 == 0) j += 6;
			else j++;
			
		}
		
		// Outgoing
		int k = 14;
		List<String> outgoing = requestList.get(1);
		for (int i = 0; i < outgoing.size(); i++) {
			if (outgoing.get(i).equals("null")) break;
			
			UUID uuid = UUID.fromString(outgoing.get(i));
			String name = Bukkit.getOfflinePlayer(uuid).getName();
			
			ItemStack skull = FriendsGUI.getSkull(uuid, ChatColor.RESET + name,
					ChatColor.RED + "Click to cancel");
			gui.setItem(k, skull);
			
			if ((k+1)%9 == 0) k += 6;
			else k++;
			
		}
		
		
		loaded = true;
		player.openInventory(gui);
		
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onItemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		if (event.getView().getTitle().equalsIgnoreCase(TITLE) && event.getInventory().getSize() == SIZE) {
			
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
                return;
            }
			event.setCancelled(true);
			final Player player = (Player) event.getWhoClicked();
			
			ItemStack clicked = event.getCurrentItem();
			String name = clicked.getItemMeta().getDisplayName();
			if (name == null) return;
			List<String> lore = clicked.getItemMeta().getLore();
			if (lore == null) lore = new ArrayList<String>();
			
			
			if (name.equalsIgnoreCase(ChatColor.LIGHT_PURPLE + "Return to friends list")) {
				
				FriendsGUI.request(player);
				
			}
			
			else if (lore.size() > 0) {
				
				// Incoming
				if (lore.get(0).equalsIgnoreCase(ChatColor.GREEN + "Left click to accept")) {
					
					UUID uuid = ((SkullMeta)clicked.getItemMeta()).getOwningPlayer().getUniqueId();
					
					if (event.getAction() == InventoryAction.PICKUP_HALF) {
						
						ProxyConnector.deny(player, uuid);
						
					}
					
					else {
						
						ProxyConnector.accept(player, uuid);
						
					}
					
				}
				
				// Outgoing
				else {
					
					UUID uuid = ((SkullMeta)clicked.getItemMeta()).getOwningPlayer().getUniqueId();
					
					ProxyConnector.cancel(player, uuid);
					
				}
				
				
			}
			
		}
			
			
		
	}
	
}