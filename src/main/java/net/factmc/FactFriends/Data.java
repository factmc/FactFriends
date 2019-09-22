package net.factmc.FactFriends;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.factmc.FactCore.FactSQL;
import net.md_5.bungee.api.ChatColor;

public class Data {
	
	public static final String PREFIX = ChatColor.DARK_GRAY + "["
			+ ChatColor.DARK_PURPLE + ChatColor.BOLD + "Friends"
			+ ChatColor.DARK_GRAY + "] ";
	
	
	public static boolean areFriends(UUID uuid, UUID friend) {
		
		long count = FactSQL.getInstance().count(FactSQL.getFriendsTable(), new String[0],
				"((`UUID`=? AND `FRIEND`=?) OR (`FRIEND`=? AND `UUID`=?)) AND `PENDING`=?",
				new Object[] {uuid.toString(), friend.toString(), uuid.toString(), friend.toString(), false});
		return count > 0;
		
	}
	
	public static boolean hasRequest(UUID uuid, UUID friend) {
		
		long count = FactSQL.getInstance().count(FactSQL.getFriendsTable(), new String[0],
				"((`UUID`=? AND `FRIEND`=?) OR (`FRIEND`=? AND `UUID`=?)) AND `PENDING`=?",
				new Object[] {uuid.toString(), friend.toString(), uuid.toString(), friend.toString(), true});
		return count > 0;
		
	}
	
	
	public static List<UUID> getFriends(UUID uuid) {
		List<UUID> list = new ArrayList<UUID>();
		
		List<Map<String, Object>> maps = FactSQL.getInstance().select(FactSQL.getFriendsTable(), new String[] {"UUID", "FRIEND"},
				"(`UUID`=? OR `FRIEND`=?) AND `PENDING`=?", new Object[] {uuid.toString(), uuid.toString(), false});
		for (Map<String, Object> map : maps) {
			UUID f = UUID.fromString(map.get("FRIEND").toString());
			UUID u = UUID.fromString(map.get("UUID").toString());
			if (f.equals(uuid)) list.add(u);
			else list.add(f);
		}
		
		return list;
	}
	
	public static boolean addFriend(UUID uuid, UUID friend) {
		if (areFriends(uuid, friend)) return false;
		
		boolean success = false;
		if (hasRequest(uuid, friend))
			success = FactSQL.getInstance().update(FactSQL.getFriendsTable(), "PENDING", false,
					"(`UUID`=? AND `FRIEND`=?) OR (`FRIEND`=? AND `UUID`=?)",
					new Object[] {uuid.toString(), friend.toString(), uuid.toString(), friend.toString()});
		
		else success = FactSQL.getInstance().insert(FactSQL.getFriendsTable(), new String[] {"UUID", "FRIEND", "PENDING"},
				new Object[] {uuid.toString(), friend.toString(), false});
		
		if (success) {
			sendMessage(uuid, PREFIX + ChatColor.BLUE +
					"You are now friends with " + FactSQL.getInstance().getName(friend));
			sendMessage(friend, PREFIX + ChatColor.BLUE +
					"You are now friends with " + FactSQL.getInstance().getName(uuid));
		}
		
		return success;
	}
	
	public static boolean removeFriend(UUID uuid, UUID friend) {
		
		if (FactSQL.getInstance().delete(FactSQL.getFriendsTable(),
				"((`UUID`=? AND `FRIEND`=?) OR (`FRIEND`=? AND `UUID`=?)) AND `PENDING`=?",
				new Object[] {uuid.toString(), friend.toString(), uuid.toString(), friend.toString(), false})) {
		
			sendMessage(friend, PREFIX + ChatColor.BLUE +
					FactSQL.getInstance().getName(uuid) + " is no longer friends with you");
			return true;
		}
		
		return false;
	}
	
	public static boolean removeAllFriends(UUID uuid) {
		
		List<UUID> friends = getFriends(uuid);
		for (UUID f : friends) {
			sendMessage(f, PREFIX + ChatColor.BLUE +
					FactSQL.getInstance().getName(uuid) + " is no longer friends with you");
		}
		
		return FactSQL.getInstance().delete(FactSQL.getFriendsTable(),
				"(`UUID`=? OR `FRIEND`=?) AND `PENDING`=?",
				new Object[] {uuid.toString(), uuid.toString(), false});
		
	}
	
	
	public static List<Map<String, Object>> getRequests(UUID uuid) {
		return FactSQL.getInstance().select(FactSQL.getFriendsTable(), new String[] {"UUID", "FRIEND"},
				"(`UUID`=? OR `FRIEND`=?) AND `PENDING`=?", new Object[] {uuid.toString(), uuid.toString(), true});
	}
	
	public static boolean addRequest(UUID uuid, UUID friend) {
		if (hasRequest(uuid, friend) || areFriends(uuid, friend)) return false;
		
		if (FactSQL.getInstance().insert(FactSQL.getFriendsTable(), new String[] {"UUID", "FRIEND", "PENDING"},
				new Object[] {uuid.toString(), friend.toString(), true})) {
		
			sendMessage(friend, PREFIX + ChatColor.BLUE +
					"You have received a friend request from " + FactSQL.getInstance().getName(uuid) + ", use "
					+ ChatColor.GRAY + "/friends requests" + ChatColor.BLUE + " to view it");
			return true;
		}
		
		return false;
	}
	
	public static boolean removeRequest(UUID uuid, UUID friend) {
		
		if (FactSQL.getInstance().delete(FactSQL.getFriendsTable(),
				"((`UUID`=? AND `FRIEND`=?) OR (`FRIEND`=? AND `UUID`=?)) AND `PENDING`=?",
				new Object[] {uuid.toString(), friend.toString(), uuid.toString(), friend.toString(), true})) {
		
//			sendMessage(friend, PREFIX + ChatColor.BLUE +
//					FactSQL.getInstance().getName(uuid) + " is no longer friends with you");
			return true;
		}
		
		return false;
	}
	
	public static boolean respondToRequest(UUID uuid, UUID friend, boolean response) {
		
		if (response == false && removeRequest(uuid, friend)) {
			
			sendMessage(uuid, PREFIX + ChatColor.BLUE +
					FactSQL.getInstance().getName(friend) + " denied your friend request");
			return true;
		}
		
		else if (response == true && addFriend(uuid, friend)) {
			return true;
		}
		
		return false;
	}
	
	
	public static void sendMessage(UUID uuid, String message) {
		
		try {
			Class.forName("org.bukkit.Bukkit");
			net.factmc.FactFriends.bukkit.Main.sendMessage(uuid, message);
		} catch (ClassNotFoundException e) {};
		
		try {
			Class.forName("net.md_5.bungee.api.ProxyServer");
			net.factmc.FactFriends.bungee.Main.sendMessage(uuid, message);
		} catch (ClassNotFoundException e) {};
		
	}
	
}