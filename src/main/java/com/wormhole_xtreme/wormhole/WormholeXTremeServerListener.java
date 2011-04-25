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
package com.wormhole_xtreme.wormhole;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.plugin.HelpSupport;
import com.wormhole_xtreme.wormhole.plugin.IConomySupport;
import com.wormhole_xtreme.wormhole.plugin.PermissionsSupport;
import com.wormhole_xtreme.wormhole.plugin.WormholeWorldsSupport;

/**
 * WormholeXTreme Server Listener.
 * 
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */
public class WormholeXTremeServerListener extends ServerListener
{

    /* (non-Javadoc)
     * @see org.bukkit.event.server.ServerListener#onPluginDisabled(org.bukkit.event.server.PluginEvent)
     */
    @Override
    public void onPluginDisable(final PluginDisableEvent event)
    {
        if (event.getPlugin().getDescription().getName().equals("iConomy") && !ConfigManager.getIconomySupportDisable())
        {
            IConomySupport.disableIconomy();
        }
        if (event.getPlugin().getDescription().getName().equals("Permissions") && !ConfigManager.getPermissionsSupportDisable())
        {
            PermissionsSupport.disablePermissions();
        }
        if (event.getPlugin().getDescription().getName().equals("Help") && !ConfigManager.getHelpSupportDisable())
        {
            HelpSupport.disableHelp();
        }
        else if (event.getPlugin().getDescription().getName().equals("WormholeXTremeWorlds") && ConfigManager.isWormholeWorldsSupportEnabled())
        {
            WormholeWorldsSupport.disableWormholeWorlds();
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.server.ServerListener#onPluginEnabled(org.bukkit.event.server.PluginEvent)
     */
    @Override
    public void onPluginEnable(final PluginEnableEvent event)
    {
        if (event.getPlugin().getDescription().getName().equals("iConomy") && !ConfigManager.getIconomySupportDisable())
        {
            IConomySupport.enableIconomy();
        }
        else if (event.getPlugin().getDescription().getName().equals("Permissions") && !ConfigManager.getPermissionsSupportDisable())
        {
            PermissionsSupport.enablePermissions();
        }
        else if (event.getPlugin().getDescription().getName().equals("Help") && !ConfigManager.getHelpSupportDisable())
        {
            HelpSupport.enableHelp();
        }
        else if (event.getPlugin().getDescription().getName().equals("WormholeXTremeWorlds") && ConfigManager.isWormholeWorldsSupportEnabled())
        {
            WormholeWorldsSupport.enableWormholeWorlds();
        }
    }
}
