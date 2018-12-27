package tk.Pdani.NRankup.Listener;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tk.Pdani.NRankup.Main;
import tk.Pdani.NRankup.managers.GuiManager;

public class InventoryListener implements Listener {
	GuiManager gui;
	public InventoryListener(GuiManager gui){
		this.gui = gui;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); // The player that clicked the item
		ItemStack clicked = event.getCurrentItem(); // The item that was clicked
		int slot = event.getRawSlot();
		Inventory inventory = event.getInventory(); // The inventory that was clicked in
		Inventory myInv = gui.getInventory(player);
		if(myInv == null)
			return;
		if (inventory.getName().equals(myInv.getName())) {
			event.setCancelled(true);
			Main.instance.getLogger().log(Level.INFO,"Slot: "+slot); // DEBUG (lol)
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
}
