package tk.Pdani.NRankup;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import tk.Pdani.NRankup.Listener.PlayerCommand;
import tk.Pdani.NRankup.Listener.PlayerJoin;

public class Main extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	public String prefix = "[Rankup] ";
	public String debug_prefix = "[Rankup] ";
	private static Permission perms = null;
	private static Economy econ = null;
	public HashMap<Integer, String> ranks = new HashMap<Integer, String>();
	public String defaultRank = null;
	private RankManager rm;
	
	public void onEnable(){
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		this.rm = new RankManager(this);
		
		rm.reloadRanks();
		
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
		
		rm.reloadAllPlayerRanks();
		
		this.getCommand("rankup").setExecutor(new PlayerCommand(this, rm));
		getServer().getPluginManager().registerEvents(new PlayerJoin(rm), this);
		
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
	
	public static Economy getEconomy() {
		return econ;
	}
	
	public static Permission getPermissions() {
		return perms;
	}
	
	public String color(String string){
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
}
