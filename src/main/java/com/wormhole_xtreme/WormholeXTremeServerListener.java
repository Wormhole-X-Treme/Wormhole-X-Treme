package com.wormhole_xtreme;

import java.util.logging.Level;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

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
            WormholeXTreme.Iconomy = (iConomy)event.getPlugin();
            String v = event.getPlugin().getDescription().getVersion();
            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy version " + v);
        }
        if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
            WormholeXTreme.Permissions = (PermissionHandler)event.getPlugin();
            String v = event.getPlugin().getDescription().getVersion();
            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to Permissions version " + v);
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
