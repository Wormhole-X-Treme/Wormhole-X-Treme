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

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.wormhole_xtreme.plugin.HelpSupport;
import com.wormhole_xtreme.plugin.IConomySupport;
import com.wormhole_xtreme.plugin.PermissionsSupport;

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
	private final HelpSupport helpSupport = new HelpSupport(WormholeXTreme.thisPlugin);
	private final IConomySupport iconomySupport = new IConomySupport(WormholeXTreme.thisPlugin);
	private final PermissionsSupport permissionsSupport = new PermissionsSupport(WormholeXTreme.thisPlugin);
	
    /* (non-Javadoc)
     * @see org.bukkit.event.server.ServerListener#onPluginEnabled(org.bukkit.event.server.PluginEvent)
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event) 
    {
        if(event.getPlugin().getDescription().getName().equals("iConomy")) 
        {
            iconomySupport.setupIconomy();
        }
        else if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
    		permissionsSupport.setupPermissions();
        }
        else if (event.getPlugin().getDescription().getName().equals("Help"))
        {
            helpSupport.setupHelp();
        }
    }
    

    
    /* (non-Javadoc)
     * @see org.bukkit.event.server.ServerListener#onPluginDisabled(org.bukkit.event.server.PluginEvent)
     */
    @Override
    public void onPluginDisable(PluginDisableEvent event)
    {
        if(event.getPlugin().getDescription().getName().equals("iConomy"))
        {
            iconomySupport.disableIconomy();
        }
        if(event.getPlugin().getDescription().getName().equals("Permissions"))
        {
            permissionsSupport.disablePermissions();
        }
        if(event.getPlugin().getDescription().getName().equals("Help"))
        {
            helpSupport.disableHelp();
        }
    }
}
