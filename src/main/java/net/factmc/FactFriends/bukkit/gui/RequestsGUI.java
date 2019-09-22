package net.factmc.FactFriends.bukkit.gui;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactFriends.Data;

public class RequestsGUI implements Listener {
	
	private static boolean loaded = false;
	final public static int SIZE = 45;
	final public static String TITLE = ChatColor.DARK_PURPLE + "Friends";
	
	public static void open(Player player) {
		
		Inventory gui = player.getServer().createInventory(player, SIZE, TITLE);
		
		ItemStack panes = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
		for (int i = 4; i < SIZE; i += 9) {
			gui.setItem(i, panes);
		}
		
		ItemStack back = InventoryControl.getItemStack(Material.ARROW, ChatColor.LIGHT_PURPLE + "Return to friends list",
				ChatColor.GRAY + "Return to the main friends menu");
		gui.setItem(22, InventoryControl.addPersistentData(back, FriendsGUI.TYPE_KEY, "BACK"));
		
		ItemStack incomingItem = InventoryControl.getItemStack(Material.GOLDEN_CARROT, ChatColor.GREEN + "Incoming Requests");
		gui.setItem(2, incomingItem);
		
		ItemStack outgoingItem = InventoryControl.getItemStack(Material.SPECTRAL_ARROW, ChatColor.RED + "Outgoing Requests");
		gui.setItem(6, outgoingItem);
		
		
		int j = 9; int k = 14;
		List<Map<String, Object>> rows = Data.getRequests(player.getUniqueId());
		for (Map<String, Object> map : rows) {
			
			UUID uuid = UUID.fromString(map.get("UUID").toString());
			UUID friend = UUID.fromString(map.get("FRIEND").toString());
			
			if (friend.equals(player.getUniqueId())) {
				if (j >= gui.getSize()) continue;
				
				String name = FactSQL.getInstance().getName(uuid);
				ItemStack skull = InventoryControl.getHead(name, name,
						ChatColor.GREEN + "Left click to accept", ChatColor.RED + "Right click to deny");
				skull = InventoryControl.addPersistentData(skull, new NamespacedKey[] {FriendsGUI.TYPE_KEY, FriendsGUI.DATA_KEY},
						new String[] {"INCOMING", uuid.toString()});
				gui.setItem(j, skull);
				
				if ((j-3)%9 == 0) j += 6;
				else j++;
			}
			
			else {
				if (k >= gui.getSize()) continue;
				
				String name = FactSQL.getInstance().getName(friend);
				ItemStack skull = InventoryControl.getHead(name, name, ChatColor.RED + "Click to cancel");
				skull = InventoryControl.addPersistentData(skull, new NamespacedKey[] {FriendsGUI.TYPE_KEY, FriendsGUI.DATA_KEY},
						new String[] {"OUTGOING", friend.toString()});
				gui.setItem(k, skull);
				
				if ((k+1)%9 == 0) k += 6;
				else k++;
			}
			
		}
		
		loaded = true;
		player.openInventory(gui);
		
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onItemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		if (event.getInventory().getType() == InventoryType.CHEST && event.getInventory().getHolder() instanceof Player
				&& event.getView().getTitle().equalsIgnoreCase(TITLE)) {
			
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
                return;
            }
			event.setCancelled(true);
			Player player = (Player) event.getWhoClicked();
			
			String type = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(FriendsGUI.TYPE_KEY, PersistentDataType.STRING);
			if (type == null) return;
			
			if (type.equals("BACK")) {
				FriendsGUI.open(player);
				return;
			}
			
			else if (type.equals("INCOMING")) {
				
				UUID uuid = UUID.fromString(
						event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(FriendsGUI.DATA_KEY, PersistentDataType.STRING));
				
				boolean response = false;
				if (event.getClick() == ClickType.LEFT) response = true;
				
				Data.respondToRequest(uuid, player.getUniqueId(), response);
				open(player);
				return;
				
			}
			
			else if (type.equals("OUTGOING")) {
				
				UUID friend = UUID.fromString(
						event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(FriendsGUI.DATA_KEY, PersistentDataType.STRING));
				Data.removeRequest(player.getUniqueId(), friend);
				open(player);
				return;
				
			}
			
		}
		
	}
	
}