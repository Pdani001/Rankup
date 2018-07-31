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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@SuppressWarnings("static-access")
public class RankManager {
	private Main main;
	public RankManager (Main main) {
		this.main = main;
	}
	public void reloadRanks() {
		main.ranks.clear();
		main.defaultRank = null;
		HashMap<Integer, String> asd = new HashMap<Integer, String>();
		for(String value : main.getConfig().getConfigurationSection("ranks").getKeys(false)){
			int key = main.getConfig().getInt("ranks."+value+".priority", -1);
			if(key != 0) {asd.put(key, value);} else {main.defaultRank = value;asd.put(key, value);}
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
	
	public void reloadAllPlayerRanks() {
		ConfigurationSection configSection = main.getConfig().getConfigurationSection("players");
		if(configSection == null){
			return;
		}
		Set<String> players = configSection.getKeys(false);
		for(String name : players){
			boolean playerRankFound = false;
			String rank = main.getConfig().getString("players."+name+".rank");
			Player player = main.getServer().getPlayer(name);
			if(player != null){
				playerRankFound = isRank(rank);
				if(!playerRankFound){
					String defaultRankName = main.getConfig().getString("ranks."+main.defaultRank+".name");
					main.getPermissions().playerRemoveGroup(null, player, rank);
					main.getPermissions().playerAddGroup(null, player, defaultRankName);
				}
			}
		}
		Collection<? extends Player> onlinePlayers = main.getServer().getOnlinePlayers();
		if(onlinePlayers.size() > 0){
			for(Player player : onlinePlayers){
				reloadPlayerRank(player);
			}
		}
	}
	
	public void reloadPlayerRank(Player player) {
		boolean playerRankFound = false;
		String name = player.getName();
		boolean hasRank = main.getConfig().isConfigurationSection("players."+name+".rank");
		String rank = main.getConfig().getString("players."+name+".rank",null);
		if(!hasRank){
			String defaultRankName = main.getConfig().getString("ranks."+main.defaultRank+".name");
			main.getPermissions().playerAddGroup(null, player, defaultRankName);
			main.getConfig().set("players."+name+".rank", defaultRankName);
			main.saveConfig();
			return;
		}
		if(rank == null)return;
		
		playerRankFound = isRank(rank);
		if(!playerRankFound){
			String defaultRankName = main.getConfig().getString("ranks."+main.defaultRank+".name");
			main.getPermissions().playerRemoveGroup(null, player, rank);
			main.getPermissions().playerAddGroup(null, player, defaultRankName);
		} else {
			String[] groups = main.getPermissions().getPlayerGroups(player);
			List<String> groupList = Arrays.asList(groups);
			if(!groupList.contains(rank)){
				main.getPermissions().playerAddGroup(null, player, rank);
			}
		}
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
}