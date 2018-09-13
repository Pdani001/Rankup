package tk.Pdani.NRankup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;

public class Messages {
	private static JavaPlugin plugin;
	private Messages(JavaPlugin plugin) {
		Messages.plugin = plugin;
	}

	public static String getString(String key, String def) {
		Properties props = new Properties();
		InputStream is = null;
		is = Main.class.getResourceAsStream("messages.properties");
		try {
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return props.getProperty(key);
	}
	
	public static String getString(String key, String def, String lang){
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

	    return null;
	}
}
