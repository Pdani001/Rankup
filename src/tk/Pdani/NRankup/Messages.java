package tk.Pdani.NRankup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;

public class Messages {
	private JavaPlugin plugin;
	private Properties defProps = null;
	public Messages(JavaPlugin plugin, Properties defProps) {
		this.plugin = plugin;
		this.defProps = defProps;
	}

	public String getString(String key, String def) {
		if(defProps == null)
			return def;
		return defProps.getProperty(key,def);
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
	        return props.getProperty(key,def);
	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }

	    return def;
	}
}
