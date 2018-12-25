package tk.Pdani.NRankup.managers;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tk.Pdani.NRankup.Main;

public class GuiManager {
	private RankManager rm;
	private HashMap<Player,Inventory> invs = new HashMap<Player,Inventory>();
	public GuiManager(RankManager rm){
		this.rm = rm;
	}
	public void openInventory(Player target){
		if(getInventory(target) != null)
			return;
		Inventory myInv = Main.instance.getServer().createInventory(null, 9, "RankUp Panel");
		ItemStack i1 = new ItemStack(Material.DIRT, 1);
		ItemMeta meta = i1.getItemMeta();
		meta.setDisplayName("Fõd");
		i1.setItemMeta(meta);
		myInv.setItem(0, i1);
		myInv.setItem(8, new ItemStack(Material.GOLD_BLOCK, 1));
		invs.put(target, myInv);
		target.openInventory(myInv);
	}
	public void closedInventory(Player target){
		invs.remove(target);
	}
	public Inventory getInventory(Player target){
		return invs.get(target);
	}
}
