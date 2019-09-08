package net.factmc.FactFriends.bukkit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.factmc.FactFriends.bukkit.listeners.ProxyConnector;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactFriends.bukkit.Main;

public class FriendsGUI implements Listener {
	
	private static boolean loaded = false;
	final public static int SIZE = 45;
	final public static String TITLE = ChatColor.DARK_PURPLE + "Friends";
	
	public static void request(Player player) {
		ProxyConnector.getList(player);
	}
	
	public static void open(Player player, List<String[]> friendList) {
		
		Inventory gui = player.getServer().createInventory(player, SIZE, TITLE);
		
		ItemStack panes = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
		for (int i = 9; i < 18; i++) {
			gui.setItem(i, panes);
		}
		
		ItemStack removeAll = getStack(Material.BARRIER, ChatColor.RED + "Remove All Friends",
				ChatColor.GRAY + "Caution! This is irreversible!");
		gui.setItem(0, removeAll);
		
		ItemStack head = getSkull(player.getUniqueId(), ChatColor.LIGHT_PURPLE + player.getName() + "'s Friends",
				ChatColor.GRAY + "Click here to view your active requests");
		gui.setItem(4, head);
		
		ItemStack add = getStack(Material.COMPARATOR, ChatColor.GREEN + "Add Friend");
		gui.setItem(8, add);
		
		
		for (int i = 0; i < friendList.size(); i++) {
			if (i+18 >= SIZE) break;
			
			String[] info = friendList.get(i);
			UUID uuid = UUID.fromString(info[0]);
			
			ItemStack friendSkull = getSkull(uuid, ChatColor.RESET + info[1], getStatusMsg(info[2]),
					ChatColor.BLUE + "Left click to join", ChatColor.RED + "Right click to remove");
			gui.setItem(i+18, friendSkull);
			
		}
		
		
		loaded = true;
		player.openInventory(gui);
		
	}
	
	public static String getStatusMsg(String status) {
		
		if (status.equalsIgnoreCase("offline")) {	
			
			return ChatColor.GRAY + "Offline";
			
		}
		
		else {
			
			String start = ChatColor.GRAY + "Online on ";
			String end = "";
			
			if (status.equalsIgnoreCase("hub")) {
				end = ChatColor.DARK_PURPLE + "Hub";
			}
			else if (status.equalsIgnoreCase("creative")) {
				end = ChatColor.DARK_GREEN + "Creative";
			}
			else if (status.equalsIgnoreCase("survival")) {
				end = ChatColor.DARK_RED + "Survival";
			}
			else if (status.equalsIgnoreCase("skyblock")) {
				end = ChatColor.DARK_BLUE + "Skyblock";
			}
			else if (status.equalsIgnoreCase("tardiss")) {
				end = ChatColor.DARK_BLUE + "TARDIS";
			}
			else end = "Unknown";
			
			return start + end;
			
		}
		
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
			
			if (name.equalsIgnoreCase(ChatColor.RED + "Remove All Friends")) {
				
				ProxyConnector.removeAll(player);
				
			}
			
			else if (name.equalsIgnoreCase(ChatColor.GREEN + "Add Friend")) {
				
				player.closeInventory();
				player.sendMessage(Main.prefix + ChatColor.BLUE + "Enter a name in chat: " + ChatColor.GRAY + "(Use \"=cancel\" to cancel)");
				addingFriend.add(player);
				
			}
			
			else if (name.equalsIgnoreCase(ChatColor.LIGHT_PURPLE + player.getName() + "'s Friends")) {
				
				RequestsGUI.request(player);
				
			}
			
			else if (lore.size() > 1) {
				
				//UUID uuid = UUID.fromString(ChatColor.stripColor(lore.get(4)));
				UUID uuid = ((SkullMeta)clicked.getItemMeta()).getOwningPlayer().getUniqueId();
				
				if (event.getAction() == InventoryAction.PICKUP_HALF) {
					
					// Remove friend
					ProxyConnector.remove(player, uuid);
					
				}
				
				else {
					
					// Jump to friend
					ProxyConnector.jumpTo(player, uuid);
					
				}
				
			}
			
		}
			
			
		
	}
	
	
	
	public static ItemStack getStack(Material material, String name, String... lore) {
		
		ItemStack stack = new ItemStack(material);
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		
		List<String> fLore = new ArrayList<String>();
		for (String line : lore) {
			fLore.add(line);
		}
		meta.setLore(fLore);
		
		stack.setItemMeta(meta);
		return stack;
		
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getSkull(UUID head, String name, String... lore) {
		
		ItemStack stack = getStack(Material.PLAYER_HEAD, name, lore);
		
		SkullMeta meta = (SkullMeta) stack.getItemMeta();
		meta.setOwner(FactSQLConnector.getName(head));
		
		stack.setItemMeta(meta);
		return stack;
		
	}
	
	
	
	private List<Player> addingFriend = new ArrayList<Player>();
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onFriendAdding(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		boolean remove = false;
		for (final Player p : addingFriend) {
			
			if (p == player) {
				event.setCancelled(true);
				String msg = event.getMessage();
				
				if (!msg.equalsIgnoreCase("=cancel")) {
					
					ProxyConnector.add(player, msg);
					
				}
				
				else player.sendMessage(ChatColor.RED + "Cancelled");
				
				remove = true;
				break;
			}
			
		}
		
		if (remove) addingFriend.remove(player);
		
	}
	
}