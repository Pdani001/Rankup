package tk.Pdani.NRankup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RankManager {
	private Main main;
	@SuppressWarnings("unused")
	private Messages msg;
	public RankManager (Main main, Messages msg) {
		this.main = main;
		this.msg = msg;
	}
	public void reloadRanks() {
		main.ranks.clear();
		main.defaultRank = null;
		HashMap<Integer, String> asd = new HashMap<Integer, String>();
		for(String value : main.getConfig().getConfigurationSection("ranks").getKeys(false)){
			int key = main.getConfig().getInt("ranks."+value+".priority", -1);
			if(key != 0) {
				if(key != -1)asd.put(key, value);
			} else {
				if(main.defaultRank != null){
					main.log.warning(main.debug_prefix+"There is more than one default rank in the config!");
				} else {
					main.defaultRank = value;
					asd.put(key, value);
				}
			}
		}
		
		Map<Integer, String> map = new TreeMap<Integer, String>(asd); 
		Set<?> set2 = map.entrySet();
		Iterator<?> iterator2 = set2.iterator();
		while(iterator2.hasNext()) {
			 @SuppressWarnings("rawtypes")
			Map.Entry me2 = (Map.Entry)iterator2.next();
			 main.ranks.put(Integer.parseInt(me2.getKey().toString()), me2.getValue().toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void reloadAllPlayerRanks() {
		ConfigurationSection configSection = main.getConfig().getConfigurationSection("players");
		if(configSection == null){
			return;
		}
		Collection<? extends Player> collectionList = null;
		Player[] playerList = null;
		boolean isOldClass = false;
		try {
		    if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class) {
		    	collectionList = ((Collection<? extends Player>)Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
		    } else {
		    	playerList = ((Player[])Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
		    	isOldClass = true;
		    }
		} catch (Exception ex) {
			ex.printStackTrace(); // will probably never print
			return;
		}
		if(isOldClass){
			for(Player player : playerList){
				reloadPlayerRank(player);
			}
		} else {
			for(Player player : collectionList){
				reloadPlayerRank(player);
			}
		}
	}
	
	public void reloadPlayerRank(OfflinePlayer player) {
		boolean playerRankFound = false;
		String name = player.getName();
		boolean hasRank = main.getConfig().isConfigurationSection("players."+name+"");
		String rank = main.getConfig().getString("players."+name+".rank",null);
		if(!hasRank){
			String defaultRankName = main.getConfig().getString("ranks."+main.defaultRank+".name");
			Main.getPermissions().playerAddGroup(null, player, defaultRankName);
			main.getConfig().set("players."+name+".rank", defaultRankName);
			main.saveConfig();
			return;
		}
		if(rank == null)return;
		
		playerRankFound = isRank(rank);
		if(!playerRankFound){
			String defaultRankName = main.getConfig().getString("ranks."+main.defaultRank+".name");
			Main.getPermissions().playerRemoveGroup(null, player, rank);
			Main.getPermissions().playerAddGroup(null, player, defaultRankName);
		} else {
			String[] groups = Main.getPermissions().getPlayerGroups(null,player);
			List<String> groupList = Arrays.asList(groups);
			if(!groupList.contains(rank)){
				Main.getPermissions().playerAddGroup(null, player, rank);
			}
			NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(main.ranks);
			for (Map.Entry<Integer, String> e : mp.entrySet()) {
				String tname = main.getConfig().getString("ranks."+e.getValue()+".name");
				if(groupList.contains(tname) && !tname.equals(rank)){
					Main.getPermissions().playerRemoveGroup(null, player, tname);
				}
			}
		}
	}
	
	public void reloadPlayerRank(Player player) {
		reloadPlayerRank(main.getServer().getOfflinePlayer(player.getUniqueId()));
	}
	
	public String getRankId(String rank) {
		if(rank == null) return null;
		
		String nextRankToReturn = null;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(main.ranks);
		for (Map.Entry<Integer, String> e : mp.entrySet()) {
			String tname = main.getConfig().getString("ranks."+e.getValue()+".name");
			if(tname.equals(rank)){
				nextRankToReturn = e.getValue();
				break;
			}
		}
		return nextRankToReturn;
	}
	
	public String getNextRank(String rank) {
		if(rank == null) rank = main.getConfig().getString("ranks."+main.defaultRank+".name");
		
		String nextRankToReturn = null;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(main.ranks);
		for (Map.Entry<Integer, String> e : mp.entrySet()) {
			String tname = main.getConfig().getString("ranks."+e.getValue()+".name");
			if(tname.equals(rank)){
				Map.Entry<Integer, String> next = mp.higherEntry(e.getKey());
				if(next != null){
					nextRankToReturn = next.getValue();
				}
				break;
			}
		}
		return nextRankToReturn;
	}
	
	public String getPrevRank(String rank) {
		if(rank == null) return null;
		
		String nextRankToReturn = null;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(main.ranks);
		for (Map.Entry<Integer, String> e : mp.entrySet()) {
			String tname = main.getConfig().getString("ranks."+e.getValue()+".name");
			if(tname.equals(rank)){
				Map.Entry<Integer, String> prev = mp.lowerEntry(e.getKey());
				nextRankToReturn = prev.getValue();
				break;
			}
		}
		return nextRankToReturn;
	}
	
	public boolean isPreviousRank(String playerRank, String checkedRank){
		if(playerRank == null) return false;
		
		ArrayList<String> p = new ArrayList<String>();
		
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(main.ranks);
		
		for (Map.Entry<Integer, String> e : mp.entrySet()) {
			String cr = main.getConfig().getString("ranks."+e.getValue()+".name");
			if(!cr.equals(playerRank)){
				p.add(cr);
			} else {
				break;
			}
		}
		return p.contains(checkedRank);
	}
	
	public boolean isRankExists(String rank){
		return main.getConfig().isConfigurationSection("ranks."+rank);
	}
	
	public String getPlayerRank(Player player){
		String playerName = player.getName();
		return getPlayerRank(playerName);
	}
	
	public String getPlayerRank(OfflinePlayer player){
		String playerName = player.getName();
		return getPlayerRank(playerName);
	}
	
	public String getPlayerRank(String playerName){
		String currentRank = main.getConfig().getString("players."+playerName+".rank",null);
		return currentRank;
	}
	
	public boolean isRank(String rank){
		boolean value = false;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(main.ranks);
		for (Map.Entry<Integer, String> e : mp.entrySet()) {
			String name = main.getConfig().getString("ranks."+e.getValue()+".name");
			if(rank.equals(name)){
				value = true;
				break;
			}
		}
		return value;
	}
	
	public void setRankWeb(OfflinePlayer player, String rank, CommandSender sender){
		String target = player.getName();
		if(!main.getConfig().isConfigurationSection("players."+target)){
			sender.sendMessage("error:UnknownPlayer");
			return;
		}
		if(isRank(rank)){
			String rankName = rank;
			OfflinePlayer otp = player;
			String other_rank = "";
			other_rank = getPlayerRank(otp);
			if(other_rank == null){
				Main.getPermissions().playerAddGroup(null, otp, rankName);
			} else {
				Main.getPermissions().playerRemoveGroup(null, otp, other_rank);
				Main.getPermissions().playerAddGroup(null, otp, rankName);
			}
			main.getConfig().set("players."+target+".rank", rankName);
			main.saveConfig();
			sender.sendMessage("success");
			reloadPlayerRank(otp);
		} else {
			sender.sendMessage("error:UnknownRank");
		}
	}
	
	public void setRank(OfflinePlayer player, String rank, CommandSender sender){
		String target = player.getName();
		if(!main.getConfig().isConfigurationSection("players."+target)){
			sender.sendMessage("§c"+main.getConfig().getString("messages.player_not_found","The specified player doesn't exist OR does not have a rank!"));
			return;
		}
		if(isRank(rank)){
			String rankName = rank;
			OfflinePlayer otp = player;
			String other_rank = "";
			other_rank = getPlayerRank(otp);
			if(other_rank == null){
				Main.getPermissions().playerAddGroup(null, otp, rankName);
			} else {
				Main.getPermissions().playerRemoveGroup(null, otp, other_rank);
				Main.getPermissions().playerAddGroup(null, otp, rankName);
			}
			main.getConfig().set("players."+target+".rank", rankName);
			main.saveConfig();
			String rankId = getRankId(rankName);
			String rankDisplayName = main.color(main.getConfig().getString("ranks."+rankId+".display"));
			String rank_changed = main.getConfig().getString("messages.rank_changed_other","&c{player}'s &arank successfully changed to &f{rank}&a!");
			rank_changed = rank_changed.replace("{rank}", rankDisplayName);
			rank_changed = rank_changed.replace("{player}", target);
			rank_changed = main.color(rank_changed);
			sender.sendMessage(rank_changed);
			reloadPlayerRank(otp);
		} else {
			sender.sendMessage("§c"+main.getConfig().getString("messages.rank_not_found","This rank doesn't exist!"));
		}
	}
	
	public void setRank(Player player, String rank, CommandSender sender){
		OfflinePlayer op = main.getServer().getOfflinePlayer(player.getUniqueId());
		setRank(op,rank,sender);
	}
	
	public void setRankWeb(Player player, String rank, CommandSender sender){
		OfflinePlayer op = main.getServer().getOfflinePlayer(player.getUniqueId());
		setRankWeb(op,rank,sender);
	}
}
