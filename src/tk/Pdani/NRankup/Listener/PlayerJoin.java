package tk.Pdani.NRankup.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import tk.Pdani.NRankup.RankManager;

public class PlayerJoin implements Listener {
	private RankManager rm;
	public PlayerJoin(RankManager rm){
		this.rm = rm;
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		rm.reloadPlayerRank(event.getPlayer());
	}
}
