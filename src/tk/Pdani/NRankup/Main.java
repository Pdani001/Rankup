package tk.Pdani.NRankup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import tk.Pdani.NRankup.Listener.InventoryListener;
import tk.Pdani.NRankup.Listener.PlayerCommand;
import tk.Pdani.NRankup.Listener.PlayerJoin;
import tk.Pdani.NRankup.managers.GuiManager;
import tk.Pdani.NRankup.managers.RankManager;

@SuppressWarnings("unused")
public class Main extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	public String prefix = "[Rankup] ";
	private static Permission perms = null;
	private static Economy econ = null;
	public HashMap<Integer, String> ranks = new HashMap<Integer, String>();
	public String defaultRank = null;
	private RankManager rm;
	private GuiManager gui;
	public static JavaPlugin instance = null;
	private static Main main = null;
	
	public void onEnable(){
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		instance = this;
		main = this;
		
		String version = this.getDescription().getVersion();
		
		reloadMessages();
		
		if(!getConfig().isSet("messages-now-in") || getConfig().isConfigurationSection("messages")){
			getLogger().log(Level.INFO, "Deleted messages section from config.");
			getConfig().set("messages",null);
			getConfig().set("messages-now-in","messages.properties");
			saveConfig();
		}
		
		this.rm = new RankManager(this);
		this.gui = new GuiManager(rm);
		
		String name = this.getDescription().getName();
		
		rm.reloadRanks();
		
		if(!setupEconomy()){
			getLogger().log(Level.SEVERE, "Disabling plugin, due to missing dependency: Vault");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupPermissions();
		
		if(defaultRank == null || defaultRank.isEmpty()){
			getLogger().log(Level.WARNING, "Disabling plugin, due to missing default rank!");
			getLogger().log(Level.WARNING, "If this is your first time loading the plugin, edit the config then restart/reload the server!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		rm.reloadAllPlayerRanks();
		
		this.getCommand("rankup").setExecutor(new PlayerCommand(this, rm, gui));
		getServer().getPluginManager().registerEvents(new PlayerJoin(rm), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(gui, rm), this);
		
		prefix = Messages.getString("prefix","[Rankup]") + " ";
		
		String author = this.getDescription().getAuthors().get(0);
		getLogger().log(Level.INFO, ""+name+" plugin v"+version+" created by "+author);
		getLogger().log(Level.INFO, "Enabled.");
	}
	public void onDisable(){
		getLogger().log(Level.INFO, "Disabled.");
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
	
	public static void asyncTask(Runnable run){
		getScheduler().runTaskAsynchronously(instance, run);
	}
	
	public static void asyncTaskLater(Runnable run, long delay){
		getScheduler().runTaskLaterAsynchronously(instance, run, delay);
	}
	
	public static void taskLater(Runnable run, long delay){
		getScheduler().runTaskLater(instance, run, delay);
	}
	
	public static BukkitScheduler getScheduler() {
        return instance.getServer().getScheduler();
    }
	
	public static String getPrefix(){
		return main.prefix;
	}
	
	public void broadcast(String msg){
		List<Player> players = getOnlinePlayers();
		for(Player p : players){
			p.sendMessage(msg);
		}
	}
	public static void bc(String msg){
		main.broadcast(msg);
	}
	
	@SuppressWarnings("unchecked")
	public List<Player> getOnlinePlayers(){
		ArrayList<Player> players = new ArrayList<Player>();
		Collection<? extends Player> collectionList = null;
		Player[] playerList = null;
		boolean isOldClass = false;
		try {
		    if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class) {
		    	collectionList = ((Collection<? extends Player>)Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
		    } else {
		    	playerList = ((Player[])Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
		    	isOldClass = true;
		    }
		} catch (Exception ex) {
			ex.printStackTrace(); // will probably never print
			return players;
		}
		if(isOldClass){
			for(Player player : playerList){
				players.add(player);
			}
		} else {
			for(Player player : collectionList){
				players.add(player);
			}
		}
		return players;
	}
	
	public static void updateMsg(File f, InputStream is){
		Properties props = new Properties();
		try {
			InputStream is2 = new FileInputStream(f);
			if(is2 != is){
				String v = Messages.getDefString("version");
				instance.getLogger().log(Level.INFO, "Saved updated messages.properties");
				File oldF = new File(instance.getDataFolder()+"/messages.properties");
				File newF = new File(instance.getDataFolder()+"/messages.oldv"+v+".properties");
				if(newF.exists())
					newF.delete();
				if(oldF.renameTo(newF)) {
					oldF.delete();
					main.saveResource("messages.properties",f);
				}
			}
			is2.close();
		} catch (Exception e) {
			e.printStackTrace(); // Will probably never print
		}
		InputStream fis = null;
		try {
			fis = new FileInputStream(f);
			props.load(fis);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Messages.setProps(props);
	}
	public static void reloadMessages(){
		InputStream is = instance.getResource("messages.properties");
		Properties defprops = new Properties();
		Properties props = new Properties();
		try {
			if(is == null) {
				instance.getLogger().log(Level.SEVERE, "The messages.properties file was not loaded from the jar!");
				defprops = null;
			} else {
				defprops.load(is);
			}
		} catch (IOException e) {
			e.printStackTrace();
			defprops = null;
		}
		Messages.setDefProps(defprops);
		String version = Messages.getDefString("version");
		File f = new File(instance.getDataFolder(),"messages.properties");
		if(!f.exists()) {
			instance.getLogger().log(Level.INFO, "Created new messages.properties file");
			main.saveResource("messages.properties",f);
		} else {
			InputStream fis = null;
			try {
				fis = new FileInputStream(f);
				props.load(fis);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Messages.setProps(props);
			String cv = Messages.getString("version", false);
			if((cv == null || !cv.equals(version))){
				updateMsg(f,is);
			}
		}
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
