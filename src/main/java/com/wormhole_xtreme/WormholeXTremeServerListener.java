package com.wormhole_xtreme;

import java.util.logging.Level;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;

public class WormholeXTremeServerListener extends ServerListener 
{
	public WormholeXTremeServerListener(Plugin this_plugin)
	{
		
	}
	
    @Override
    public void onPluginEnabled(PluginEvent event) 
    {
        if(event.getPlugin().getDescription().getName().equals("iConomy")) 
        {
    		String v = event.getPlugin().getDescription().getVersion();
    		this.CheckIconomyVersion(v);
    		
        	try
        	{
	            WormholeXTreme.Iconomy = (iConomy)event.getPlugin();
	            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy version " + v);
	    	}
	    	catch ( Exception e)
	    	{
	    		WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to attach to iConomy: " + e.getMessage());
	    	}
        }
        if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
    		String v = event.getPlugin().getDescription().getVersion();
    		this.CheckPermissionsVersion(v);

        	try
        	{
	            WormholeXTreme.Permissions = (PermissionHandler)event.getPlugin();
	            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to Permissions version " + v);
        	}
        	catch ( Exception e)
        	{
        		WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to attach to Permissions: " + e.getMessage());
        	}
        }
    }
    
    public void CheckPermissionsVersion(String version)
    {
        if ( !version.equals("2.4") && !version.equals("2.5") )
        {
        	WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of Permissions. Recommended is 2.5" );
        }
       
    }
    
    public void CheckIconomyVersion(String version)
    {
        if ( !version.equals("4.0") && !version.equals("4.1") )
        {
        	WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of iConomy. Recommended is 4.1" );
        }
       
    }
    
    @Override
    public void onPluginDisabled(PluginEvent event)
    {
        if(event.getPlugin().getDescription().getName().equals("iConomy"))
        {
            String v = event.getPlugin().getDescription().getVersion();
            WormholeXTreme.Iconomy = null;
            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from iConomy version " + v);
        }
        if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
            String v = event.getPlugin().getDescription().getVersion();
            WormholeXTreme.Permissions = null;
            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from Permissions version " + v);
        }
    }
}
