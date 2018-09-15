package tk.Pdani.NRankup;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
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
	public String debug_prefix = "";
	private static Permission perms = null;
	private static Economy econ = null;
	public HashMap<Integer, String> ranks = new HashMap<Integer, String>();
	public String defaultRank = null;
	private RankManager rm;
	private Messages msg;
	
	public void onEnable(){
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("messages.properties");
		
		this.msg = new Messages(this, this, is);
		this.rm = new RankManager(this, msg);
		
		String name = this.getDescription().getName();
		this.debug_prefix = "["+ name + "] ";
		
		rm.reloadRanks();
		
		if(!setupEconomy()){
			log.severe(debug_prefix+"Disabling plugin, due to missing dependency: Vault");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		
		if(defaultRank == null || defaultRank.isEmpty()){
			log.warning(debug_prefix+"Disabling plugin, due to missing default rank!");
			log.warning(debug_prefix+"If this is your first time loading the plugin, edit the config then restart/reload the server!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		rm.reloadAllPlayerRanks();
		
		this.getCommand("rankup").setExecutor(new PlayerCommand(this, rm, msg));
		getServer().getPluginManager().registerEvents(new PlayerJoin(rm), this);
		
		prefix = msg.getString("prefix","[Rankup]",null) + " ";
		
		Properties props = msg.getProps(null);
		log.info(debug_prefix+"Properties size: "+props.size());
		
		String author = this.getDescription().getAuthors().get(0);
		String version = this.getDescription().getVersion();
		log.info(debug_prefix+""+name+" plugin v"+version+" created by "+author);
		log.info(debug_prefix+"Enabled.");
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
