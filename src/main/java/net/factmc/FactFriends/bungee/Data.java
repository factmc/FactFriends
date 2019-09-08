package net.factmc.FactFriends.bungee;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactFriends.bungee.Main;
import net.factmc.FactFriends.bungee.listeners.ServerConnector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Data {
	
	public static List<ProxiedPlayer[]> requests = new ArrayList<ProxiedPlayer[]>();
	
	public static ResultSet getTable() {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("SELECT * FROM " + FactSQLConnector.getFriendsTable());
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	
	public static UUID getFriend(UUID uuid, UUID friend) {
		
		try {
			
			ResultSet rs = getTable();
			while (rs.next()) {
				UUID player1 = UUID.fromString(rs.getString("UUID"));
				UUID player2 = UUID.fromString(rs.getString("FRIEND"));
				
				if (player1.equals(uuid) && player2.equals(friend)) {
					return player2;
				}
				if (player2.equals(uuid) && player1.equals(friend)) {
					return player1;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static List<String> getFriends(UUID uuid) {
		List<String> list = new ArrayList<String>();
		
		try {
			
			ResultSet rs = getTable();
			while (rs.next()) {
				UUID player1 = UUID.fromString(rs.getString("UUID"));
				UUID player2 = UUID.fromString(rs.getString("FRIEND"));
				
				UUID friend = null;
				if (player1.equals(uuid)) {
					friend = player2;
				}
				if (player2.equals(uuid)) {
					friend = player1;
				}
				
				if (friend != null) {
					
					list.add(friend.toString() + "," + FactSQLConnector.getName(friend));
					
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
		
	}
	
	public static boolean removeFriend(UUID uuid, UUID friend) {
		
		try {
			
			PreparedStatement delete = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("DELETE FROM " + FactSQLConnector.getFriendsTable()
							+ " WHERE `UUID`=? AND `FRIEND`=?");
			delete.setString(1, uuid.toString());
			delete.setString(2, friend.toString());
			
			delete.executeUpdate();
			
			PreparedStatement delete2 = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("DELETE FROM " + FactSQLConnector.getFriendsTable()
					+ " WHERE `UUID`=? AND `FRIEND`=?");
			delete2.setString(1, friend.toString());
			delete2.setString(2, uuid.toString());
			
			delete2.executeUpdate();
			
			msgUUID(friend, new TextComponent(Main.prefix + ChatColor.BLUE +
					FactSQLConnector.getName(uuid) + " is no longer friends with you"));
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	public static void removeAllFriends(UUID uuid) {
		
		try {
			
			ResultSet rs = getTable();
			while (rs.next()) {
				UUID player1 = UUID.fromString(rs.getString("UUID"));
				UUID player2 = UUID.fromString(rs.getString("FRIEND"));
				
				UUID friend = null;
				if (player1.equals(uuid)) {
					friend = player2;
				}
				if (player2.equals(uuid)) {
					friend = player1;
				}
				
				if (friend != null) {
					
					removeFriend(uuid, friend);
					
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean addFriend(UUID uuid, UUID friend) {
		
		try {
			
			PreparedStatement insert = FactSQLConnector.getMysql().getConnection()
				.prepareStatement("INSERT INTO " + FactSQLConnector.getFriendsTable()
				+ " (UUID,FRIEND) VALUE (?,?)");
			insert.setString(1, uuid.toString());
			insert.setString(2, friend.toString());
			
			insert.executeUpdate();
			
			msgUUID(uuid, new TextComponent(Main.prefix + ChatColor.BLUE +
					"You are now friends with " + FactSQLConnector.getName(friend)));
			msgUUID(friend, new TextComponent(Main.prefix + ChatColor.BLUE +
					"You are now friends with " + FactSQLConnector.getName(uuid)));
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static short addRequest(final ProxiedPlayer sender, ProxiedPlayer receiver) {
		if (receiver == null) return 5;
		if (receiver == sender) return 3;
		
		final ProxiedPlayer[] request = {sender, receiver};
		
		short check = checkExists(sender, receiver);
		if (check < 2) return check;
		
		requests.add(request);
		
		if (receiver.isConnected()) {
			receiver.sendMessage(new TextComponent(Main.prefix + ChatColor.BLUE
					+ "You have received a friend request from " + sender.getName()));
			
			ServerConnector.sendMessage(receiver.getUniqueId().toString() + ".received", receiver.getServer());
		}
		
		Main.getPlugin().getProxy().getScheduler().schedule(Main.getPlugin(), new Runnable() {

			public void run() {
				if (requests.remove(request)) {
					sender.sendMessage(
						new TextComponent(Main.prefix + ChatColor.BLUE + "Your friend request to "
						+ request[1].getName() + " has expired"));
				}
			}
			
		}, 10L, TimeUnit.MINUTES);
		
		return 2;
	}
	
	public static short checkExists(ProxiedPlayer sender, ProxiedPlayer receiver) {
		
		for (ProxiedPlayer[] array : requests) {
			
			if (array[0].getUniqueId().equals(sender.getUniqueId()) && array[1].getUniqueId().equals(receiver.getUniqueId())) {
				
				return 0;
				
			}
			
			else if (array[0].getUniqueId().equals(receiver.getUniqueId()) && array[1].getUniqueId().equals(sender.getUniqueId())) {
				
				return 1;
				
			}
			
		}
		
		return 2;
		
	}
	
	
	public static boolean respondRequest(ProxiedPlayer player, UUID senderUUID, boolean response) {
		ProxiedPlayer[] remove = null;
		for (ProxiedPlayer[] request : requests) {
			
			if (request[0].getUniqueId().equals(senderUUID) && request[1].getUniqueId().equals(player.getUniqueId())) {
				if (response) addFriend(request[0].getUniqueId(), request[1].getUniqueId());
				else if (request[0].isConnected()) request[0].sendMessage(
						new TextComponent(Main.prefix + ChatColor.BLUE + request[1].getName() + " denied your friend request"));
				remove = request;
				break;
			}
			
		}
		
		if (remove == null) return false;
		requests.remove(remove);
		return true;
	}
	
	public static boolean revokeRequest(ProxiedPlayer player, UUID uuid) {
		ProxiedPlayer[] remove = null;
		for (ProxiedPlayer[] request : requests) {
			
			if (request[0].getUniqueId().equals(player.getUniqueId()) && request[1].getUniqueId().equals(uuid)) {
				remove = request;
				break;
			}
			
		}
		
		if (remove == null) return false;
		requests.remove(remove);
		return true;
	}
	
	
	
	public static void msgUUID(UUID uuid, BaseComponent msg) {
		
		ProxiedPlayer p = Main.getPlugin().getProxy().getPlayer(uuid);
		if (p != null && p.isConnected()) {
			p.sendMessage(msg);
		}
		
	}
	
}