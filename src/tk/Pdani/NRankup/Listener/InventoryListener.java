package tk.Pdani.NRankup.Listener;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.milkbowl.vault.economy.EconomyResponse;
import tk.Pdani.NRankup.Main;
import tk.Pdani.NRankup.Messages;
import tk.Pdani.NRankup.managers.GuiManager;
import tk.Pdani.NRankup.managers.RankManager;

public class InventoryListener implements Listener {
	private GuiManager gui;
	private RankManager rm;
	public InventoryListener(GuiManager gui, RankManager rm){
		this.gui = gui;
		this.rm = rm;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); // The player that clicked the item
		ItemStack clicked = event.getCurrentItem(); // The item that was clicked
		int slot = event.getRawSlot();
		if(slot == -999) {
			return;
		}
		Inventory inventory = event.getInventory(); // The inventory that was clicked in
		Inventory myInv = gui.getInventory(player);
		if(myInv == null) {
			return;
		}
		if (inventory.getName().equals(myInv.getName())) {
			if(slot > myInv.getSize()-1){
				return;
			}
			event.setCancelled(true);
			if(clicked.getType() == Material.AIR)
				return;
			if(slot == 8){
				player.closeInventory();
				getNextRank(player);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Player player = (Player) event.getPlayer(); // The player that closed the inventory
		Inventory myInv = gui.getInventory(player);
		if(myInv == null)
			return;
		gui.closedInventory(player);
	}
	
	private void getNextRank(Player player){
		String rank = rm.getPlayerRank(player);
		String nextGroup = rm.getNextRank(rank);
		if(nextGroup == null){
			String already_highest = Messages.getString("already_highest","You have the highest available rank!");
			player.sendMessage("§c"+already_highest);
			return;
		}
		String nextGroupId = nextGroup;
		nextGroup = Main.instance.getConfig().getString("ranks."+nextGroup+".name");
		int rankPrice = Main.instance.getConfig().getInt("ranks."+nextGroupId+".price");
		String rankDisplayName = color(Main.instance.getConfig().getString("ranks."+nextGroupId+".display"));
		int playerEco = (int) Main.getEconomy().getBalance(player);
		if(playerEco < rankPrice){
			int difference = rankPrice - playerEco;
			String next_rank_price_msg = Messages.getString("getrank.rank_diff","&cYou need &a${diff} &cmore to get the &f{rank} &crank!");
			next_rank_price_msg = next_rank_price_msg.replace("{diff}", Integer.toString(difference));
			next_rank_price_msg = next_rank_price_msg.replace("{rank}", rankDisplayName);
			next_rank_price_msg = color(next_rank_price_msg);
			player.sendMessage(next_rank_price_msg);
		} else {
			Double worth = 0.0;
			worth = Double.parseDouble(Integer.toString(rankPrice));
			EconomyResponse er = Main.getEconomy().withdrawPlayer(player,Math.abs(-worth));
			if(!er.transactionSuccess()){
				Main.instance.getLogger().log(Level.SEVERE, "Economy transaction failed! Error: \""+er.errorMessage+"\"");
				String transaction_error = Messages.getString("transaction_error","An error occurred while processing your request! Please notify an Administrator!");
				player.sendMessage("§c"+transaction_error);
				return;
			}
			if(rank == null){
				Main.getPermissions().playerAddGroup(null, player, nextGroup);
			} else {
				Main.getPermissions().playerRemoveGroup(null, player, rank);
				Main.getPermissions().playerAddGroup(null, player, nextGroup);
			}
			Main.instance.getConfig().set("players."+player.getName()+".rank", nextGroup);
			Main.instance.saveConfig();
			rm.runRankCommands(player,nextGroup);
			String rank_buy_success = Messages.getString("rank_buy_success","&aYou successfully bought the &f{rank} &arank!");
			rank_buy_success = rank_buy_success.replace("{rank}", rankDisplayName);
			rank_buy_success = color(rank_buy_success);
			player.sendMessage(rank_buy_success);
			String announce = Messages.getString("getrank.announce","&e{player} &asuccessfully bought the &f{rank} &arank!");
			announce = announce.replace("{rank}", rankDisplayName);
			announce = announce.replace("{player}", player.getName());
			announce = "&a" + Main.getPrefix() + "&f" + announce;
			announce = color(announce);
			Main.bc(announce);
		}
	}
	
	private String color(String string){
		return ChatColor.translateAlternateColorCodes('&', string);
	}
}
