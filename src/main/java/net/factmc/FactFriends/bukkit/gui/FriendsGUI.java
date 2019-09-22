package net.factmc.FactFriends.bukkit.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactFriends.Data;
import net.factmc.FactFriends.bukkit.Main;

public class FriendsGUI implements Listener, PluginMessageListener {
	
	public static final NamespacedKey TYPE_KEY = new NamespacedKey(Main.getPlugin(), "type");
	public static final NamespacedKey DATA_KEY = new NamespacedKey(Main.getPlugin(), "data");
	public static final NamespacedKey WARP_KEY = new NamespacedKey(Main.getPlugin(), "warp");
	
	private static boolean loaded = false;
	final public static int SIZE = 45;
	final public static String TITLE = ChatColor.DARK_PURPLE + "Friends";
	
	public static void open(Player player) {
		
		Inventory gui = player.getServer().createInventory(player, SIZE, TITLE);
		
		ItemStack panes = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
		for (int i = 9; i < 18; i++) {
			gui.setItem(i, panes);
		}
		
		ItemStack removeAll = InventoryControl.getItemStack(Material.BARRIER, ChatColor.RED + "Remove All Friends",
				ChatColor.GRAY + "Caution! This is irreversible!");
		gui.setItem(0, InventoryControl.addPersistentData(removeAll, TYPE_KEY, "REMOVE_ALL"));
		
		ItemStack head = InventoryControl.getHead(player, ChatColor.LIGHT_PURPLE + player.getName() + "'s Friends",
				ChatColor.GRAY + "Click here to view your active requests");
		gui.setItem(4, InventoryControl.addPersistentData(head, TYPE_KEY, "REQUESTS"));
		
		ItemStack add = InventoryControl.getItemStack(Material.COMPARATOR, ChatColor.GREEN + "Add Friend");
		gui.setItem(8, InventoryControl.addPersistentData(add, TYPE_KEY, "ADD"));
		
		
		List<UUID> friends = Data.getFriends(player.getUniqueId());
		requestStatusList(friends, player, gui.getSize() - 18);
		for (int i = 0; i < friends.size(); i++) {
			if (i + 18 >= gui.getSize()) break;
			
			UUID uuid = friends.get(i);
			String name = FactSQL.getInstance().getName(uuid);
			
			ItemStack skull = InventoryControl.getHead(name, name, getStatusMsg("offline"),
					ChatColor.BLUE + "Left click to join", ChatColor.RED + "Right click to remove");
			skull = InventoryControl.addPersistentData(skull, new NamespacedKey[] {TYPE_KEY, DATA_KEY, WARP_KEY},
					new String[] {"FRIEND", uuid.toString(), "offline"});
			gui.setItem(i + 18, skull);
			
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
			
			String type = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING);
			if (type == null) return;
			
			if (type.equals("REMOVE_ALL")) {
				Data.removeAllFriends(player.getUniqueId());
				return;
			}
			
			else if (type.equals("ADD")) {
				addingFriend.add(player);
				player.closeInventory();
				player.sendMessage(Data.PREFIX + ChatColor.BLUE + "Enter a name in chat: " + ChatColor.GRAY + "(Use \"-cancel\" to cancel)");
				return;
			}
			
			else if (type.equals("REQUESTS")) {
				RequestsGUI.open(player);
				return;
			}
			
			else if (type.equals("FRIEND")) {
				
				UUID friend = UUID.fromString(
						event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(DATA_KEY, PersistentDataType.STRING));
				
				if (event.getClick() == ClickType.RIGHT) {
					Data.removeFriend(player.getUniqueId(), friend);
					open(player);
					return;
				}
				
				else {
					String server = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(WARP_KEY, PersistentDataType.STRING);
					if (!server.equals("offline")) {
						connect(player, server);
						return;
					}
				}
				
			}
			
		}
		
	}
	
	public static void requestStatusList(List<UUID> friends, Player player, int limit) {
		
		try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				DataOutputStream dos = new DataOutputStream(baos)
			) {
			
			dos.writeUTF(player.getUniqueId().toString());
			for (int i = 0; i < friends.size(); i++) {
				if (i >= limit) break;
				dos.writeUTF(friends.get(i).toString());
			}
	        
	        player.sendPluginMessage(Main.getPlugin(), "factfriends:status", baos.toByteArray());
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message) {
		if (!channel.equals("factfriends:status")) return;
				
		try (
				ByteArrayInputStream bais = new ByteArrayInputStream(message);
				DataInputStream dis = new DataInputStream(bais);
			) {
			
			Player player = Bukkit.getPlayer(UUID.fromString(dis.readUTF()));
			if (player != null) {
				InventoryView iv = player.getOpenInventory();
				
				if (iv.getType() != InventoryType.CRAFTING) {
					if (iv.getType() == InventoryType.CHEST && iv.getTopInventory().getHolder() instanceof Player
							&& iv.getTitle().equalsIgnoreCase(TITLE)) {
						
						List<String> statusList = new ArrayList<String>();
						try {
							while (true) {
								statusList.add(dis.readUTF());
							}
						} catch (EOFException ignore) {};
						
						Inventory gui = iv.getTopInventory();
						for (int i = 0; i < statusList.size(); i++) {
							if (i + 18 >= gui.getSize()) break;
							
							ItemStack item = gui.getItem(i + 18);
							ItemMeta meta = item.getItemMeta();
							meta.getPersistentDataContainer().set(WARP_KEY, PersistentDataType.STRING, statusList.get(i));
							List<String> lore = meta.getLore();
							lore.set(0, getStatusMsg(statusList.get(i)));
							meta.setLore(lore); item.setItemMeta(meta);
							gui.setItem(i + 18, item);
							
						}
						
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		};
		
	}
	
	
	public static String getStatusMsg(String status) {
		
		if (status.equals("offline")) {	
			
			return ChatColor.GRAY + "Offline";
			
		}
		
		else {
			
			String end = "Unknown";
			
			switch (status) {
			
			case "hub":
				end = ChatColor.DARK_PURPLE + "Hub"; break;
				
			case "creative":
				end = ChatColor.DARK_GREEN + "Creative"; break;
				
			case "survival":
				end = ChatColor.DARK_RED + "Survival"; break;
				
			case "skyblock":
				end = ChatColor.DARK_BLUE + "Skyblock"; break;
				
			case "tardiss":
				end = ChatColor.DARK_BLUE + "TARDIS"; break;
				
			}
			
			return ChatColor.GRAY + "Online on " + end;
			
		}
		
	}
	
	public static void connect(Player player, String server) {
		try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
				DataOutputStream dos = new DataOutputStream(baos)
			){
			
	        dos.writeUTF("Connect");
	        dos.writeUTF(server);
	        player.sendPluginMessage(Main.getPlugin(), "BungeeCord", baos.toByteArray());
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	
	private List<Player> addingFriend = new ArrayList<Player>();
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onFriendAdding(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (addingFriend.contains(player)) {
			event.setCancelled(true);
			String msg = event.getMessage();
			
			if (!msg.equalsIgnoreCase("-cancel")) {
				
				UUID friend = FactSQL.getInstance().getUUID(msg);
				if (friend == null) {
					player.sendMessage(Data.PREFIX + ChatColor.RED + "That player has never joined the network");
				}
				
				else {
					Data.addRequest(player.getUniqueId(), friend);
					new BukkitRunnable() {
						@Override
						public void run() {
							RequestsGUI.open(player);
						}
					}.runTask(Main.getPlugin());
				}
				
			}
			else new BukkitRunnable() {
				@Override
				public void run() {
					open(player);
				}
			}.runTask(Main.getPlugin());
			
			addingFriend.remove(player);
		}
		
	}
	
}