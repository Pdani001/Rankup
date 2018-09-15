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

	private String getString(String key, String def) {
		if(defProps == null)
			return def;
		return defProps.getProperty(key,def);
	}
	
	public Properties getDefaults(){
		return defProps;
	}
	
	public void setDefaults(Properties props){
		this.defProps = props;
	}
	
	/**
	 * Gets the message from the properties file
	 * @param key Message key from properties file
	 * @param def Default message if key doesn't exists
	 * @param lang Language code or {@code null}
	 * @return Message
	 */
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
	        	if(lang != null){
	        		InputStream is2 = plugin.getResource("messages-"+lang+".properties");
	        		if(is2 == null){
	        			return getString(key, def, null);
	        		}
	        		props.load(is2);
	    	        return props.getProperty(key,def);
	        	} else {
	        		return getString(key, def);
	        	}
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
