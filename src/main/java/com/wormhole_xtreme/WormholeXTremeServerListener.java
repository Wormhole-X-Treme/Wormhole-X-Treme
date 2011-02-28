package com.wormhole_xtreme;

import java.util.logging.Level;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;

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
            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy.");
        }
    }
}
