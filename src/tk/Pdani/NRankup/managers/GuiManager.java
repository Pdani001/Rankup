package tk.Pdani.NRankup.managers;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tk.Pdani.NRankup.Main;
import tk.Pdani.NRankup.Messages;

public class GuiManager {
	private RankManager rm;
	private HashMap<Player,Inventory> invs = new HashMap<Player,Inventory>();
	public GuiManager(RankManager rm){
		this.rm = rm;
	}
	public void openInventory(Player target){
		if(getInventory(target) != null)
			return;
		String rank = rm.getPlayerRank(target);
		String nextGroup = rm.getNextRank(rank);
		String rankId = rm.getRankId(rank);
		rank = color(Main.instance.getConfig().getString("ranks."+rankId+".display"));
		int playerEco = (int) Main.getEconomy().getBalance(target);
		boolean highest = (nextGroup == null);
		String already_highest_text = "§c"+Messages.getString("already_highest","You have the highest available rank!");
		String nextGroupDisplay;
		int rankPrice = (nextGroup != null) ? Main.instance.getConfig().getInt("ranks."+nextGroup+".price") : -1;
		int difference;
		if(rankPrice > playerEco){
			difference = (rankPrice - playerEco);
		} else {
			difference = 0;
		}
		if(nextGroup == null)
			nextGroupDisplay = already_highest_text;
		else
			nextGroupDisplay = color(Main.instance.getConfig().getString("ranks."+nextGroup+".display"));
		
		Inventory myInv = Main.instance.getServer().createInventory(null, 9, color(Messages.getString("menu.title","&c###")));
		
		/* CURRENT RANK */
		Material m1 = Material.valueOf(Messages.getString("menu.current_rank.item","IRON_HELMET").toUpperCase());
		if(m1 == null)
			m1 = Material.STONE;
		ItemStack cr = new ItemStack(m1, 1);
		ItemMeta crm = cr.getItemMeta();
		ArrayList<String> crl = new ArrayList<String>();
		if(!Messages.getString("menu.current_rank.lore","&c###").equals(""))
			crl.add(
					color(Messages.getString("menu.current_rank.lore","&c###")
							.replace("{rank}", rank)
							)
					);
		crm.setDisplayName(
				color(Messages.getString("menu.current_rank.name","&c###")
						.replace("{rank}", rank)
						)
				);
		crm.setLore(crl);
		cr.setItemMeta(crm);
		if(!highest)
			myInv.setItem(0, cr);
		else
			myInv.setItem(2, cr);
		
		/* NEXT RANK */
		Material m2 = Material.valueOf(Messages.getString("menu.next_rank.item","DIAMOND_HELMET").toUpperCase());
		if(m2 == null)
			m2 = Material.STONE;
		ItemStack nr = new ItemStack(m2, 1);
		ItemMeta nrm = nr.getItemMeta();
		ArrayList<String> nrl = new ArrayList<String>();
		if(!highest){
			if(!Messages.getString("menu.next_rank.lore","&c###").equals(""))
				nrl.add(
						color(Messages.getString("menu.next_rank.lore","&c###")
								.replace("{rank}", nextGroupDisplay)
								)
						);
		} else {
			if(!Messages.getString("menu.next_rank.lore_highest","&c###").equals(""))
				nrl.add(
						color(Messages.getString("menu.next_rank.lore_highest","&c###")
								.replace("{rank}", nextGroupDisplay)
								)
						);
		}
		nrm.setDisplayName(
				color(Messages.getString("menu.next_rank.name","&c###")
						.replace("{rank}", nextGroupDisplay)
						)
				);
		nrm.setLore(nrl);
		nr.setItemMeta(nrm);
		if(!highest)
			myInv.setItem(3, nr);
		else
			myInv.setItem(6, nr);
		
		/* NEXT RANK DIFF */
		Material m3 = Material.valueOf(Messages.getString("menu.next_rank_diff.item","EMERALD").toUpperCase());
		if(m3 == null)
			m3 = Material.STONE;
		ItemStack nrd = new ItemStack(m3, 1);
		ItemMeta nrdm = nrd.getItemMeta();
		ArrayList<String> nrdl = new ArrayList<String>();
		if(!Messages.getString("menu.next_rank_diff.lore","&c###").equals(""))
			nrdl.add(
					color(Messages.getString("menu.next_rank_diff.lore","&c###")
							.replace("{rank}", nextGroupDisplay)
							.replace("{diff}", Integer.toString(difference))
							)
					);
		
		nrdm.setDisplayName(
				color(Messages.getString("menu.next_rank_diff.name","&c###")
						.replace("{rank}", nextGroupDisplay)
						.replace("{diff}", Integer.toString(difference))
						)
				);
		nrdm.setLore(nrdl);
		nrd.setItemMeta(nrdm);
		if(!highest)
			myInv.setItem(4, nrd);
		
		/* RANKUP */
		Material m4 = Material.valueOf(Messages.getString("menu.rankup.item","GOLD_INGOT").toUpperCase());
		if(m4 == null)
			m4 = Material.STONE;
		ItemStack rankup = new ItemStack(m4, 1);
		ItemMeta rankupmeta = rankup.getItemMeta();
		rankupmeta.setDisplayName(color(Messages.getString("menu.rankup","&c###")));
		rankup.setItemMeta(rankupmeta);
		if(!highest)
			myInv.setItem(8, rankup);
		
		invs.put(target, myInv);
		target.openInventory(myInv);
	}
	public void closedInventory(Player target){
		invs.remove(target);
	}
	public Inventory getInventory(Player target){
		return invs.get(target);
	}
	
	private String color(String string){
		return ChatColor.translateAlternateColorCodes('&', string);
	}
}
