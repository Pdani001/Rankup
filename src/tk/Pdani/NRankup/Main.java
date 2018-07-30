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
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin  implements Listener, CommandExecutor {
	Logger log = Logger.getLogger("Minecraft");
	String prefix = "[Rankup] ";
	String debug_prefix = "[Rankup] ";
	private static Permission perms = null;
	private static Economy econ = null;
	HashMap<Integer, String> ranks = new HashMap<Integer, String>();
	String defaultRank = null;
	
	public void onEnable(){
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		reloadRanks();
		
		if(!setupEconomy()){
			log.severe(debug_prefix+"Disabling plugin, due to missing dependency: Vault");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		
		if(defaultRank == null || defaultRank.isEmpty()){
			log.warning(debug_prefix+"It looks like, this is the first start of the plugin. Take your time, edit the config.yml, then restart/reload the server!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		reloadAllPlayerRanks();
		
		getServer().getPluginManager().registerEvents(this, this);
		
		String author = this.getDescription().getAuthors().get(0);
		String version = this.getDescription().getVersion();
		String name = this.getDescription().getName();
		log.info(debug_prefix+""+name+" plugin v"+version+" created by "+author);
		log.info(debug_prefix+"Enabled.");
		
		prefix = getConfig().getString("messages.prefix","[Rankup]") + " ";
	}
	public void onDisable(){
		log.info(debug_prefix+"Disabled.");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		reloadPlayerRank(event.getPlayer());
	}
	
	public void reloadRanks() {
		ranks.clear();
		defaultRank = null;
		HashMap<Integer, String> asd = new HashMap<Integer, String>();
		for(String value : getConfig().getConfigurationSection("ranks").getKeys(false)){
			int key = getConfig().getInt("ranks."+value+".priority", -1);
			if(key != 0) {asd.put(key, value);} else {defaultRank = value;asd.put(key, value);}
		}
		
		Map<Integer, String> map = new TreeMap<Integer, String>(asd); 
        Set<?> set2 = map.entrySet();
        Iterator<?> iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
             @SuppressWarnings("rawtypes")
			Map.Entry me2 = (Map.Entry)iterator2.next();
             ranks.put(Integer.parseInt(me2.getKey().toString()), me2.getValue().toString());
        }
	}
	
	public void reloadAllPlayerRanks() {
		ConfigurationSection configSection = getConfig().getConfigurationSection("players");
		if(configSection == null){
			return;
		}
		Set<String> players = configSection.getKeys(false);
		for(String name : players){
			boolean playerRankFound = false;
			String rank = getConfig().getString("players."+name+".rank");
			Player player = getServer().getPlayer(name);
			if(player != null){
				playerRankFound = isRank(rank);
			    if(!playerRankFound){
			    	String defaultRankName = getConfig().getString("ranks."+defaultRank+".name");
			    	getPermissions().playerRemoveGroup(null, player, rank);
					getPermissions().playerAddGroup(null, player, defaultRankName);
			    }
			}
		}
		Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
		if(onlinePlayers.size() > 0){
			for(Player player : onlinePlayers){
				reloadPlayerRank(player);
			}
		}
	}
	
	public void reloadPlayerRank(Player player) {
		boolean playerRankFound = false;
		String name = player.getName();
		boolean hasRank = getConfig().isConfigurationSection("players."+name+".rank");
		String rank = getConfig().getString("players."+name+".rank",null);
		if(!hasRank){
			String defaultRankName = getConfig().getString("ranks."+defaultRank+".name");
			getPermissions().playerAddGroup(null, player, defaultRankName);
			getConfig().set("players."+name+".rank", defaultRankName);
			saveConfig();
			return;
		}
		if(rank == null)return;
		
		playerRankFound = isRank(rank);
	    if(!playerRankFound){
	    	String defaultRankName = getConfig().getString("ranks."+defaultRank+".name");
	    	getPermissions().playerRemoveGroup(null, player, rank);
			getPermissions().playerAddGroup(null, player, defaultRankName);
	    } else {
	    	String[] groups = getPermissions().getPlayerGroups(player);
		    List<String> groupList = Arrays.asList(groups);
		    if(!groupList.contains(rank)){
		    	getPermissions().playerAddGroup(null, player, rank);
		    }
	    }
	}
	
	public static Economy getEconomy() {
        return econ;
    }
	
	public static Permission getPermissions() {
        return perms;
    }
	
	public String getRankId(String rank) {
		if(rank == null) return null;
		
		String nextRankToReturn = null;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(ranks);
	    for (Map.Entry<Integer, String> e : mp.entrySet()) {
	    	String tname = getConfig().getString("ranks."+e.getValue()+".name");
	        if(tname.equals(rank)){
	        	nextRankToReturn = e.getValue();
	        	break;
	        }
	    }
	    return nextRankToReturn;
	}
	
	public String getNextRank(String rank) {
		if(rank == null) rank = getConfig().getString("ranks."+defaultRank+".name");
		
		String nextRankToReturn = null;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(ranks);
	    for (Map.Entry<Integer, String> e : mp.entrySet()) {
	    	String tname = getConfig().getString("ranks."+e.getValue()+".name");
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
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(ranks);
	    for (Map.Entry<Integer, String> e : mp.entrySet()) {
	    	String tname = getConfig().getString("ranks."+e.getValue()+".name");
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
		
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(ranks);
		
	    for (Map.Entry<Integer, String> e : mp.entrySet()) {
	    	String cr = getConfig().getString("ranks."+e.getValue()+".name");
	        if(!cr.equals(playerRank)){
	        	p.add(cr);
	        } else {
	        	break;
	        }
	    }
		return p.contains(checkedRank);
	}
	
	public boolean isRankExists(String rank){
		return getConfig().isConfigurationSection("ranks."+rank);
	}
	
	public String getPlayerRank(Player player){
		String playerName = player.getName();
		String currentRank = getConfig().getString("players."+playerName+".rank",null);
		return currentRank;
	}
	
	public boolean isRank(String rank){
		boolean value = false;
		NavigableMap<Integer, String> mp = new TreeMap<Integer, String>(ranks);
		for (Map.Entry<Integer, String> e : mp.entrySet()) {
			String name = getConfig().getString("ranks."+e.getValue()+".name");
	        if(rank.equals(name)){
	        	value = true;
	        	break;
	        }
	    }
		return value;
	}
	
	public String color(String string){
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("rankup")){
			if(!(sender instanceof Player)){
				if(args.length == 1 && (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version"))){
					String author = this.getDescription().getAuthors().get(0);
					String version = this.getDescription().getVersion();
					String name = this.getDescription().getName();
					sender.sendMessage("§4"+name+" plugin v"+version+" created by "+author);
					return true;
				}
	    		sender.sendMessage("§c"+debug_prefix+"This command can only be used by players!");
				return true;
			}
			if(!sender.hasPermission("rankup.use")){
				if(args.length == 1 && (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version"))){
					String author = this.getDescription().getAuthors().get(0);
					String version = this.getDescription().getVersion();
					String name = this.getDescription().getName();
					sender.sendMessage("§4"+name+" plugin v"+version+" created by "+author);
					return true;
				}
				String no_perm = getConfig().getString("messages.no_perm","You don't have permission to use this command!");
				sender.sendMessage("§c"+prefix+no_perm);
				return true;
			}
			Player player = (Player)sender;
			@SuppressWarnings("unused")
			String group = getPermissions().getPrimaryGroup(player);
			String rank = getPlayerRank(player);
			if(args.length == 0){
				String nextGroup = getNextRank(rank);
				if(nextGroup == null){
					String already_highest = getConfig().getString("messages.already_highest","You have the highest available rank!");
					sender.sendMessage("§c"+already_highest);
					return true;
				}
				String rankName = getConfig().getString("ranks."+nextGroup+".name");
				String rankDisplay = color(getConfig().getString("ranks."+nextGroup+".display"));
				int rankPrice = getConfig().getInt("ranks."+nextGroup+".price");
				String next_rank_msg = getConfig().getString("messages.next_rank","&eThe next rank is &f{rank}&e, which price is: &a${price}");
				next_rank_msg = next_rank_msg.replace("{rank}", rankDisplay);
				next_rank_msg = next_rank_msg.replace("{price}", Integer.toString(rankPrice));
				next_rank_msg = color(next_rank_msg);
				String next_rank_noprice_msg = getConfig().getString("messages.next_rank_noprice","&eThe next rank is &f{rank}&e!");
				next_rank_noprice_msg = next_rank_noprice_msg.replace("{rank}", rankDisplay);
				next_rank_noprice_msg = color(next_rank_noprice_msg);
				if(rankPrice == 0){
					sender.sendMessage(next_rank_noprice_msg);
				} else {
					sender.sendMessage(next_rank_msg);
				}
				int playerEco = (int) getEconomy().getBalance(player);
				if(playerEco < rankPrice){
					int difference = rankPrice - playerEco;
					String next_rank_price_msg = getConfig().getString("messages.next_rank_diff","&cYou need &a${diff} &cmore to get this rank!");
					next_rank_price_msg = next_rank_price_msg.replace("{diff}", Integer.toString(difference));
					next_rank_price_msg = color(next_rank_price_msg);
					sender.sendMessage(next_rank_price_msg);
				} else {
					Double worth = 0.0;
					worth = Double.parseDouble(Integer.toString(rankPrice));
					EconomyResponse er = getEconomy().withdrawPlayer(player,Math.abs(-worth));
					if(!er.transactionSuccess()){
						log.severe(debug_prefix+"Economy transaction failed! Error: \""+er.errorMessage+"\"");
						String transaction_error = getConfig().getString("messages.transaction_error","An error occurred while processing your request! Please notify an Administrator!");
						sender.sendMessage("§c"+transaction_error);
						return true;
					}
					if(rank == null){
						getPermissions().playerAddGroup(null, player, rankName);
					} else {
						getPermissions().playerRemoveGroup(null, player, rank);
						getPermissions().playerAddGroup(null, player, rankName);
					}
					getConfig().set("players."+player.getName()+".rank", rankName);
					saveConfig();
					String rank_buy_success = getConfig().getString("messages.rank_buy_success","&aYou successfully bought the &f{rank} &arank!");
					rank_buy_success = rank_buy_success.replace("{rank}", rankDisplay);
					rank_buy_success = color(rank_buy_success);
					sender.sendMessage(rank_buy_success);
				}
			} else if(args.length == 1){
				if(args[0].equalsIgnoreCase("status")){
					String nextGroup = getNextRank(rank);
					if(nextGroup != null){
						nextGroup = color(getConfig().getString("ranks."+nextGroup+".display"));
					} else {
						nextGroup = color("&c---");
					}
					if(rank == null){
						rank = color(getConfig().getString("ranks."+defaultRank+".display"));
					} else {
						String rankId = getRankId(rank);
						rank = color(getConfig().getString("ranks."+rankId+".display"));
					}
					String current_rank = getConfig().getString("messages.status.current_rank","&dCurrent rank: &f{rank}");
					current_rank = current_rank.replace("{rank}", rank);
					current_rank = color(current_rank);
					String next_rank = getConfig().getString("messages.status.next_rank","&dNext rank: &f{rank}");
					next_rank = next_rank.replace("{rank}", nextGroup);
					next_rank = color(next_rank);
					sender.sendMessage(current_rank);
					sender.sendMessage(next_rank);
				} else if(args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version")){
					String author = this.getDescription().getAuthors().get(0);
					String version = this.getDescription().getVersion();
					String name = this.getDescription().getName();
					sender.sendMessage("§4"+name+" plugin v"+version+" created by "+author);
				} else if(args[0].equalsIgnoreCase("reload")){
					if(!player.hasPermission("rankup.admin")){
						return true;
					}
					reloadConfig();
					reloadRanks();
					reloadAllPlayerRanks();
					prefix = getConfig().getString("messages.prefix","[Rankup]") + " ";
					String reloaded = getConfig().getString("messages.reloaded","Plugin reloaded!");
					sender.sendMessage("§c"+reloaded);
				}
			} else if(args.length == 2){
				if(args[0].equalsIgnoreCase("set")){
					if(!player.hasPermission("rankup.admin")){
						return true;
					}
					if(isRank(args[1])){
						String rankName = args[1];
						if(rank == null){
							getPermissions().playerAddGroup(null, player, rankName);
						} else {
							getPermissions().playerRemoveGroup(null, player, rank);
							getPermissions().playerAddGroup(null, player, rankName);
						}
						getConfig().set("players."+player.getName()+".rank", rankName);
						saveConfig();
						String rankId = getRankId(rankName);
						String rankDisplayName = color(getConfig().getString("ranks."+rankId+".display"));
						String rank_changed = getConfig().getString("messages.rank_changed","&aYour rank successfully changed to &f{rank}&a!");
						rank_changed = rank_changed.replace("{rank}", rankDisplayName);
						rank_changed = color(rank_changed);
						sender.sendMessage(rank_changed);
					} else {
						sender.sendMessage("§c"+getConfig().getString("messages.rank_not_found","This rank doesn't exist!"));
					}
				} else if(args[0].equalsIgnoreCase("getrank")){
					if(isRank(args[1])){
						String rankName = args[1];
						if(!isRank(rankName)){
							sender.sendMessage("§c"+getConfig().getString("messages.rank_not_found","This rank doesn't exist!"));
							return true;
						}
						if(rank != null && rank.equals(rankName)){
							sender.sendMessage("§c"+getConfig().getString("messages.getrank.same_rank","You already have this rank!"));
							return true;
						}
						if(isPreviousRank(rank,rankName)){
							sender.sendMessage("§c"+getConfig().getString("messages.getrank.bigger_rank","You already have a bigger rank!"));
							return true;
						}
						String nextGroup = getNextRank(rank);
						if(nextGroup == null){
							String already_highest = getConfig().getString("messages.already_highest","You have the highest available rank!");
							sender.sendMessage("§c"+already_highest);
							return true;
						}
						String nextGroupId = nextGroup;
						nextGroup = getConfig().getString("ranks."+nextGroup+".name");
						if(!nextGroup.equals(rankName)){
							sender.sendMessage("§c"+getConfig().getString("messages.getrank.not_yet","You can't buy this rank yet."));
							return true;
						}
						
						int rankPrice = getConfig().getInt("ranks."+nextGroupId+".price");
						String rankDisplayName = color(getConfig().getString("ranks."+nextGroupId+".display"));
						int playerEco = (int) getEconomy().getBalance(player);
						if(playerEco < rankPrice){
							int difference = rankPrice - playerEco;
							String next_rank_price_msg = getConfig().getString("messages.getrank.rank_diff","&cYou need &a${diff} &cmore to get the &f{rank} &crank!");
							next_rank_price_msg = next_rank_price_msg.replace("{diff}", Integer.toString(difference));
							next_rank_price_msg = next_rank_price_msg.replace("{rank}", rankDisplayName);
							next_rank_price_msg = color(next_rank_price_msg);
							sender.sendMessage(next_rank_price_msg);
						} else {
							Double worth = 0.0;
							worth = Double.parseDouble(Integer.toString(rankPrice));
							EconomyResponse er = getEconomy().withdrawPlayer(player,Math.abs(-worth));
							if(!er.transactionSuccess()){
								log.severe(debug_prefix+"Economy transaction failed! Error: \""+er.errorMessage+"\"");
								String transaction_error = getConfig().getString("messages.transaction_error","An error occurred while processing your request! Please notify an Administrator!");
								sender.sendMessage("§c"+transaction_error);
								return true;
							}
							if(rank == null){
								getPermissions().playerAddGroup(null, player, nextGroup);
							} else {
								getPermissions().playerRemoveGroup(null, player, rank);
								getPermissions().playerAddGroup(null, player, nextGroup);
							}
							getConfig().set("players."+player.getName()+".rank", nextGroup);
							saveConfig();
							String rank_buy_success = getConfig().getString("messages.rank_buy_success","&aYou successfully bought the &f{rank} &arank!");
							rank_buy_success = rank_buy_success.replace("{rank}", rankDisplayName);
							rank_buy_success = color(rank_buy_success);
							sender.sendMessage(rank_buy_success);
						}
					} else {
						sender.sendMessage("§c"+getConfig().getString("messages.rank_not_found","This rank doesn't exist!"));
					}
				} else if(args[0].equalsIgnoreCase("status")){
					String target = args[1];
					if(!getConfig().isConfigurationSection("players."+target)){
						sender.sendMessage("§c"+getConfig().getString("messages.player_not_found","The specified player doesn\'t exist OR does not have a rank!"));
						return true;
					}
					String targetRank = getConfig().getString("players."+target+".rank");
					String rid = getRankId(targetRank);
					String rankDisplayName = color(getConfig().getString("ranks."+rid+".display"));
					String other_player_rank = getConfig().getString("messages.other_player_rank","&e{player}&d's current rank: &f{rank}&d!");
					other_player_rank = other_player_rank.replace("{player}", args[1]);
					other_player_rank = other_player_rank.replace("{rank}", rankDisplayName);
					other_player_rank = color(other_player_rank);
					sender.sendMessage(other_player_rank);
				}
			}
			
		}
		return true;
    }
}
