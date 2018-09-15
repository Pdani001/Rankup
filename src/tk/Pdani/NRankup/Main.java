package tk.Pdani.NRankup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import tk.Pdani.NRankup.Listener.PlayerCommand;
import tk.Pdani.NRankup.Listener.PlayerJoin;

@SuppressWarnings("unused")
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
		
		String version = this.getDescription().getVersion();
		
		InputStream is = this.getResource("messages.properties");
		Properties props = new Properties();
		try {
			if(is == null) {
				log.severe(debug_prefix+"The messages.properties file was not loaded from the jar!");
				props = null;
			} else {
				props.load(is);
			}
		} catch (IOException e) {
			e.printStackTrace();
			props = null;
		}
		File f = new File(this.getDataFolder()+"/messages.properties");
		if(!f.exists()) {
			log.info(debug_prefix+"Created new messages.properties file");
			this.saveResource("messages.properties",f);
		} else {
			String cv = getConfig().getString("version");
			if(cv == null || !cv.equals(version)){
				getConfig().set("version", version);
				saveConfig();
				try {
					InputStream is2 = new FileInputStream(f);
					if(is2 != is){
						log.info(debug_prefix+"Saved updated messages.properties");
						File oldF = new File(this.getDataFolder()+"/messages.properties");
						File newF = new File(this.getDataFolder()+"/messages.old.properties");
						if(newF.exists())
							newF.delete();
						if(oldF.renameTo(newF)) {
							oldF.delete();
							this.saveResource("messages.properties",f);
						}
					}
					is2.close();
				} catch (Exception e) {
					e.printStackTrace(); // Will probably never print
				}
			}
		}
		
		if(!getConfig().isSet("messages-now-in") || getConfig().isConfigurationSection("messages")){
			log.info(debug_prefix+"Deleted messages section from config.");
			getConfig().set("messages",null);
			getConfig().set("messages-now-in","messages.properties");
			saveConfig();
		}
		
		this.msg = new Messages(this, props);
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
		
		String author = this.getDescription().getAuthors().get(0);
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
	
	private void saveResource(String name, File outFile) {
	    try (InputStream in = this.getResource(
	            name);
	            OutputStream out = new FileOutputStream(outFile);) {

	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = in.read(bytes)) != -1) {
	            out.write(bytes, 0, read);
	        }

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
	
}
