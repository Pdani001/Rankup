package tk.Pdani.NRankup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;

public class Messages {
	private JavaPlugin plugin;
	private Main main;
	private InputStream msgStream = null;
	public Messages(JavaPlugin plugin, Main main, InputStream msgStream) {
		this.plugin = plugin;
		this.msgStream = msgStream;
		this.main = main;
	}

	public String getString(String key, String def) {
		Properties props = new Properties();
		
		try {
			if(msgStream == null) {
				main.log.severe(main.debug_prefix+"The messages.properties file was not loaded from the jar!");
				return def;
			}
			props.load(msgStream);
		} catch (IOException e) {
			e.printStackTrace();
			return def;
		}
		return props.getProperty(key);
	}
	
	public Properties getProps(String lang){
		Properties props = new Properties();
		InputStream is = null;
		try {
			if(lang == null){
				File f = null;
				f = new File(plugin.getDataFolder()+"/messages.properties");
				if(!f.exists() || f.isDirectory()) {
					if(msgStream == null) {
						main.log.severe(main.debug_prefix+"The messages.properties file was not loaded from the jar!");
						return null;
					}
					props.load(msgStream);
				} else {
					is = new FileInputStream(f);
			        props.load(is);
				}
			} else {
				File f = null;
		    	f = new File(plugin.getDataFolder()+"/messages-"+lang+".properties");
		        if(!f.exists() || f.isDirectory()) {
		        	return getProps(null);
		        }
		        is = new FileInputStream(f);
		        props.load(is);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return props;
	}
	
	public String getString(String key, String def, String lang){
		Properties props = new Properties();
	    InputStream is = null;

	    try 
	    {
	    	File f = null;
	    	if(lang != null){
	    		f = new File(plugin.getDataFolder()+"/messages-"+lang+".properties");
	    	} else {
	    		f = new File(plugin.getDataFolder()+"/messages.properties");
	    	}
	        if(!f.exists() || f.isDirectory()) {
	        	return getString(key, def);
	        }
	        is = new FileInputStream(f);
	        props.load(is);
	        return props.getProperty(key);
	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }

	    return def;
	}
}
