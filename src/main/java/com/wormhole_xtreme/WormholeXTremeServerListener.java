/*
 *   Wormhole X-Treme Plugin for Bukkit
 *   Copyright (C) 2011  Ben Echols
 *                       Dean Bailey
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wormhole_xtreme;

import java.util.logging.Level;

import me.taylorkelly.help.Help;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.wormhole_xtreme.config.ConfigManager;

// TODO: Auto-generated Javadoc
/**
 * WormholeXTreme Server Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */
public class WormholeXTremeServerListener extends ServerListener 
{
	
	/**
	 * Instantiates a new wormhole x treme server listener.
	 *
	 * @param this_plugin the this_plugin
	 */
	public WormholeXTremeServerListener(Plugin this_plugin)
	{
		
	}
	
    /* (non-Javadoc)
     * @see org.bukkit.event.server.ServerListener#onPluginEnabled(org.bukkit.event.server.PluginEvent)
     */
    @Override
    public void onPluginEnabled(PluginEvent event) 
    {
        if(event.getPlugin().getDescription().getName().equals("iConomy")) 
        {
    		if (WormholeXTreme.Iconomy == null)
    		{
    		    Plugin p = event.getPlugin();
    		    String v = event.getPlugin().getDescription().getVersion();
    		    this.checkIconomyVersion(v);
    		    try
    		    {
    		        WormholeXTreme.Iconomy = (iConomy)p;
	                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy version " + v);
    		    }
    		    catch ( Exception e)
    		    {
    		        WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to attach to iConomy: " + e.getMessage());
    		    }
    		}

        }
        else if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
    		
    		if (WormholeXTreme.Permissions == null) 
    		{
    		    Plugin p = event.getPlugin();
    		    String v = event.getPlugin().getDescription().getVersion();
    		    this.checkPermissionsVersion(v);
    		    try
        	    {
        	        WormholeXTreme.Permissions = ((Permissions)p).getHandler();
	                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to Permissions version " + v);
	                if (ConfigManager.getSimplePermissions())
	                {
	                    WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Simple Permissions Enabled");
	                }
	                else
	                {
	                    WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Complex Permissions Enabled");
	                }
        	    }
        	    catch ( Exception e)
        	    {
        		    WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to attach to Permissions: " + e.getMessage());
        	    }
    		}
        }
        else if (event.getPlugin().getDescription().getName().equals("Help"))
        {
            if (WormholeXTreme.Help == null)
            {
                Plugin p = event.getPlugin();
                String v = event.getPlugin().getDescription().getVersion();
                this.checkHelpVersion(v);
                try 
                {
                    WormholeXTreme.Help = (Help)p;
                    WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to Help version" + v);
                }
                catch (Exception e)
                {
                    WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to attach to Help: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Check permissions version.
     *
     * @param version the version
     */
    public void checkPermissionsVersion(String version)
    {
        if ( !version.equals("2.4") && !version.startsWith("2.5"))
        {
        	WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of Permissions. Recommended is 2.5.4" );
        }
       
    }
    
    /**
     * Check iconomy version.
     *
     * @param version the version
     */
    public void checkIconomyVersion(String version)
    {
        if ( !version.equals("4.0") && !version.equals("4.1") && !version.startsWith("4.2") && !version.startsWith("4.3") && 
            !version.startsWith("4.4") && !version.startsWith("4.5"))
        {
        	WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of iConomy. Recommended is 4.5" );
        }
       
    }
    
    /**
     * Check help version.
     *
     * @param version the version
     */
    public void checkHelpVersion(String version)
    {
        if (!version.equals("0.2"))
        {
            WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of Help. Recommended is 0.2" );
        }
    }
    
    /* (non-Javadoc)
     * @see org.bukkit.event.server.ServerListener#onPluginDisabled(org.bukkit.event.server.PluginEvent)
     */
    @Override
    public void onPluginDisabled(PluginEvent event)
    {
        if(event.getPlugin().getDescription().getName().equals("iConomy"))
        {
            if (!(WormholeXTreme.Iconomy == null))
            {
                String v = event.getPlugin().getDescription().getVersion();
                WormholeXTreme.Iconomy = null;
                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from iConomy version " + v);
            }
        }
        if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
            if (!(WormholeXTreme.Permissions == null))
            {
                String v = event.getPlugin().getDescription().getVersion();
                WormholeXTreme.Permissions = null;
                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from Permissions version " + v);
            }
        }
        if(event.getPlugin().getDescription().getName().equals("Help"))
        {
            if (!(WormholeXTreme.Help == null))
            {
                String v = event.getPlugin().getDescription().getVersion();
                WormholeXTreme.Help = null;
                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from Help version " + v);
            }
        }
    }
}
