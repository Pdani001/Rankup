package tk.Pdani.NRankup.Listener;

import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;
import tk.Pdani.NRankup.Main;
import tk.Pdani.NRankup.Messages;
import tk.Pdani.NRankup.managers.GuiManager;
import tk.Pdani.NRankup.managers.RankManager;

@SuppressWarnings("static-access")
public class PlayerCommand implements CommandExecutor {
	private Main main;
	private RankManager rm;
	private GuiManager gui;
	public PlayerCommand(Main main, RankManager rm, GuiManager gui) {
		this.main = main;
		this.rm = rm;
		this.gui = gui;
	}
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("rankup")){
			if(!(sender instanceof Player)){
				if(args.length == 1 && (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version"))){
					String author = main.getDescription().getAuthors().get(0);
					String version = main.getDescription().getVersion();
					String name = main.getDescription().getName();
					sender.sendMessage("§4"+name+" plugin v"+version+" created by "+author);
					return true;
				} else if(args.length == 3 && args[0].equalsIgnoreCase("set")){
					Player tp = main.getServer().getPlayer(args[2]);
					OfflinePlayer otp = main.getServer().getOfflinePlayer(args[2]);
					rm.setRank((tp == null) ? otp : tp, args[1], sender);
					return true;
				} else if(args.length == 4 && args[0].equalsIgnoreCase("set")){
					Player tp = main.getServer().getPlayer(args[2]);
					OfflinePlayer otp = main.getServer().getOfflinePlayer(args[2]);
					rm.setRankWeb((tp == null) ? otp : tp, args[1], sender);
					return true;
				}
				sender.sendMessage("§cThis command can only be used by players!");
				return true;
			}
			if(!sender.hasPermission("rankup.use")){
				if(args.length == 1 && (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version"))){
					String author = main.getDescription().getAuthors().get(0);
					String version = main.getDescription().getVersion();
					String name = main.getDescription().getName();
					sender.sendMessage("§4"+name+" plugin v"+version+" created by "+author);
					return true;
				}
				String no_perm = Messages.getString("no_perm","You don't have permission to use this command!");
				sender.sendMessage("§c"+main.prefix+no_perm);
				return true;
			}
			Player player = (Player)sender;
			@SuppressWarnings("unused")
			String group = main.getPermissions().getPrimaryGroup(player);
			String rank = rm.getPlayerRank(player);
			if(args.length == 0){
				String nextGroup = rm.getNextRank(rank);
				if(nextGroup == null){
					String already_highest = main.getConfig().getString("already_highest","You have the highest available rank!");
					sender.sendMessage("§c"+already_highest);
					return true;
				}
				String rankName = main.getConfig().getString("ranks."+nextGroup+".name");
				String rankDisplay = main.color(main.getConfig().getString("ranks."+nextGroup+".display"));
				int rankPrice = main.getConfig().getInt("ranks."+nextGroup+".price");
				String next_rank_msg = Messages.getString("next_rank","&eThe next rank is &f{rank}&e, which price is: &a${price}");
				next_rank_msg = next_rank_msg.replace("{rank}", rankDisplay);
				next_rank_msg = next_rank_msg.replace("{price}", Integer.toString(rankPrice));
				next_rank_msg = main.color(next_rank_msg);
				String next_rank_noprice_msg = Messages.getString("next_rank_noprice","&eThe next rank is &f{rank}&e!");
				next_rank_noprice_msg = next_rank_noprice_msg.replace("{rank}", rankDisplay);
				next_rank_noprice_msg = main.color(next_rank_noprice_msg);
				if(rankPrice == 0){
					sender.sendMessage(next_rank_noprice_msg);
				} else {
					sender.sendMessage(next_rank_msg);
				}
				int playerEco = (int) main.getEconomy().getBalance(player);
				if(playerEco < rankPrice){
					int difference = rankPrice - playerEco;
					String next_rank_price_msg = Messages.getString("next_rank_diff","&cYou need &a${diff} &cmore to get this rank!");
					next_rank_price_msg = next_rank_price_msg.replace("{diff}", Integer.toString(difference));
					next_rank_price_msg = main.color(next_rank_price_msg);
					sender.sendMessage(next_rank_price_msg);
				} else {
					Double worth = 0.0;
					worth = Double.parseDouble(Integer.toString(rankPrice));
					EconomyResponse er = main.getEconomy().withdrawPlayer(player,Math.abs(-worth));
					if(!er.transactionSuccess()){
						main.instance.getLogger().log(Level.SEVERE, "Economy transaction failed! Error: \""+er.errorMessage+"\"");
						String transaction_error = Messages.getString("transaction_error","An error occurred while processing your request! Please notify an Administrator!");
						sender.sendMessage("§c"+transaction_error);
						return true;
					}
					if(rank == null){
						main.getPermissions().playerAddGroup(null, player, rankName);
					} else {
						main.getPermissions().playerRemoveGroup(null, player, rank);
						main.getPermissions().playerAddGroup(null, player, rankName);
					}
					main.getConfig().set("players."+player.getName()+".rank", rankName);
					main.saveConfig();
					rm.runRankCommands(player,rankName);
					String rank_buy_success = Messages.getString("rank_buy_success","&aYou successfully bought the &f{rank} &arank!");
					rank_buy_success = rank_buy_success.replace("{rank}", rankDisplay);
					rank_buy_success = main.color(rank_buy_success);
					sender.sendMessage(rank_buy_success);
					String announce = Messages.getString("getrank.announce","&e{player} &asuccessfully bought the &f{rank} &arank!");
					announce = announce.replace("{rank}", rankDisplay);
					announce = announce.replace("{player}", player.getName());
					announce = "&a" + main.prefix + "&f" + announce;
					announce = main.color(announce);
					main.broadcast(announce);
				}
			} else if(args.length == 1){
				if(args[0].equalsIgnoreCase("status")){
					String nextGroup = rm.getNextRank(rank);
					if(nextGroup != null){
						nextGroup = main.color(main.getConfig().getString("ranks."+nextGroup+".display"));
					} else {
						nextGroup = main.color("&c---");
					}
					if(rank == null){
						rank = main.color(main.getConfig().getString("ranks."+main.defaultRank+".display"));
					} else {
						String rankId = rm.getRankId(rank);
						rank = main.color(main.getConfig().getString("ranks."+rankId+".display"));
					}
					String current_rank = Messages.getString("status.current_rank","&dCurrent rank: &f{rank}");
					current_rank = current_rank.replace("{rank}", rank);
					current_rank = main.color(current_rank);
					String next_rank = Messages.getString("status.next_rank","&dNext rank: &f{rank}");
					next_rank = next_rank.replace("{rank}", nextGroup);
					next_rank = main.color(next_rank);
					sender.sendMessage(current_rank);
					sender.sendMessage(next_rank);
				} else if(args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version")){
					String author = main.getDescription().getAuthors().get(0);
					String version = main.getDescription().getVersion();
					String name = main.getDescription().getName();
					sender.sendMessage("§4"+name+" plugin v"+version+" created by "+author);
				} else if(args[0].equalsIgnoreCase("reload")){
					if(!player.hasPermission("rankup.admin")){
						return true;
					}
					main.reloadConfig();
					main.reloadMessages();
					rm.reloadRanks();
					rm.reloadAllPlayerRanks();
					main.prefix = Messages.getString("prefix","[Rankup]") + " ";
					String reloaded = Messages.getString("reloaded","Plugin reloaded!");
					sender.sendMessage("§c"+reloaded);
				} else if(args[0].equalsIgnoreCase("guitest")){
					if(!player.hasPermission("rankup.admin")){
						return true;
					}
					gui.openInventory(player);
				}
			} else if(args.length == 2){
				if(args[0].equalsIgnoreCase("set")){
					if(!player.hasPermission("rankup.admin")){
						return true;
					}
					if(rm.isRank(args[1])){
						String rankName = args[1];
						if(rank == null){
							main.getPermissions().playerAddGroup(null, player, rankName);
						} else {
							main.getPermissions().playerRemoveGroup(null, player, rank);
							main.getPermissions().playerAddGroup(null, player, rankName);
						}
						main.getConfig().set("players."+player.getName()+".rank", rankName);
						main.saveConfig();
						String rankId = rm.getRankId(rankName);
						String rankDisplayName = main.color(main.getConfig().getString("ranks."+rankId+".display"));
						String rank_changed = Messages.getString("rank_changed","&aYour rank successfully changed to &f{rank}&a!");
						rank_changed = rank_changed.replace("{rank}", rankDisplayName);
						rank_changed = main.color(rank_changed);
						sender.sendMessage(rank_changed);
						rm.reloadPlayerRank(player);
					} else {
						sender.sendMessage("§c"+Messages.getString("rank_not_found","This rank doesn't exist!"));
					}
				} else if(args[0].equalsIgnoreCase("getrank")){
					if(rm.isRank(args[1])){
						String rankName = args[1];
						if(!rm.isRank(rankName)){
							sender.sendMessage("§c"+Messages.getString("rank_not_found","This rank doesn't exist!"));
							return true;
						}
						if(rank != null && rank.equals(rankName)){
							sender.sendMessage("§c"+Messages.getString("getrank.same_rank","You already have this rank!"));
							return true;
						}
						if(rm.isPreviousRank(rank,rankName)){
							sender.sendMessage("§c"+Messages.getString("getrank.bigger_rank","You already have a bigger rank!"));
							return true;
						}
						String nextGroup = rm.getNextRank(rank);
						if(nextGroup == null){
							String already_highest = Messages.getString("already_highest","You have the highest available rank!");
							sender.sendMessage("§c"+already_highest);
							return true;
						}
						String nextGroupId = nextGroup;
						nextGroup = main.getConfig().getString("ranks."+nextGroup+".name");
						if(!nextGroup.equals(rankName)){
							sender.sendMessage("§c"+Messages.getString("getrank.not_yet","You can't buy this rank yet."));
							return true;
						}
						
						int rankPrice = main.getConfig().getInt("ranks."+nextGroupId+".price");
						String rankDisplayName = main.color(main.getConfig().getString("ranks."+nextGroupId+".display"));
						int playerEco = (int) main.getEconomy().getBalance(player);
						if(playerEco < rankPrice){
							int difference = rankPrice - playerEco;
							String next_rank_price_msg = Messages.getString("getrank.rank_diff","&cYou need &a${diff} &cmore to get the &f{rank} &crank!");
							next_rank_price_msg = next_rank_price_msg.replace("{diff}", Integer.toString(difference));
							next_rank_price_msg = next_rank_price_msg.replace("{rank}", rankDisplayName);
							next_rank_price_msg = main.color(next_rank_price_msg);
							sender.sendMessage(next_rank_price_msg);
						} else {
							Double worth = 0.0;
							worth = Double.parseDouble(Integer.toString(rankPrice));
							EconomyResponse er = main.getEconomy().withdrawPlayer(player,Math.abs(-worth));
							if(!er.transactionSuccess()){
								main.instance.getLogger().log(Level.SEVERE, "Economy transaction failed! Error: \""+er.errorMessage+"\"");
								String transaction_error = Messages.getString("transaction_error","An error occurred while processing your request! Please notify an Administrator!");
								sender.sendMessage("§c"+transaction_error);
								return true;
							}
							if(rank == null){
								main.getPermissions().playerAddGroup(null, player, nextGroup);
							} else {
								main.getPermissions().playerRemoveGroup(null, player, rank);
								main.getPermissions().playerAddGroup(null, player, nextGroup);
							}
							main.getConfig().set("players."+player.getName()+".rank", nextGroup);
							main.saveConfig();
							rm.runRankCommands(player,rankName);
							String rank_buy_success = Messages.getString("rank_buy_success","&aYou successfully bought the &f{rank} &arank!");
							rank_buy_success = rank_buy_success.replace("{rank}", rankDisplayName);
							rank_buy_success = main.color(rank_buy_success);
							sender.sendMessage(rank_buy_success);
							String announce = Messages.getString("getrank.announce","&e{player} &asuccessfully bought the &f{rank} &arank!");
							announce = announce.replace("{rank}", rankDisplayName);
							announce = announce.replace("{player}", player.getName());
							announce = "&a" + main.prefix + "&f" + announce;
							announce = main.color(announce);
							main.broadcast(announce);
						}
					} else {
						sender.sendMessage("§c"+Messages.getString("rank_not_found","This rank doesn't exist!"));
					}
				} else if(args[0].equalsIgnoreCase("status")){
					String target = args[1];
					if(!main.getConfig().isConfigurationSection("players."+target)){
						sender.sendMessage("§c"+Messages.getString("player_not_found","The specified player doesn't exist OR does not have a rank!"));
						return true;
					}
					String targetRank = main.getConfig().getString("players."+target+".rank");
					String rid = rm.getRankId(targetRank);
					String rankDisplayName = main.color(main.getConfig().getString("ranks."+rid+".display"));
					String other_player_rank = Messages.getString("other_player_rank","&e{player}&d's current rank: &f{rank}&d!");
					other_player_rank = other_player_rank.replace("{player}", args[1]);
					other_player_rank = other_player_rank.replace("{rank}", rankDisplayName);
					other_player_rank = main.color(other_player_rank);
					sender.sendMessage(other_player_rank);
				}
			} else if(args.length == 3){
				if(args[0].equalsIgnoreCase("set")){
					if(!player.hasPermission("rankup.admin")){
						return true;
					}
					Player tp = main.getServer().getPlayer(args[2]);
					OfflinePlayer otp = main.getServer().getOfflinePlayer(args[2]);
					rm.setRank((tp == null) ? otp : tp, args[1], sender);
				}
			}
		}
		return true;
	}
}
