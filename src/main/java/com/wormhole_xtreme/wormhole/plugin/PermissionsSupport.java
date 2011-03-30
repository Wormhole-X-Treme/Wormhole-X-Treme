/**
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
package com.wormhole_xtreme.wormhole.plugin;

import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;

// TODO: Auto-generated Javadoc
/**
 * The Class PermissionsSupport.
 *
 * @author alron
 */
public class PermissionsSupport {
    
    /**
     * Setup permissions.
     */
    public static void enablePermissions() 
    {
        if(WormholeXTreme.permissions == null && !ConfigManager.getPermissionsSupportDisable()) 
        {
            Plugin test = WormholeXTreme.thisPlugin.getServer().getPluginManager().getPlugin("Permissions");
            if(test != null)
            {
                String v = test.getDescription().getVersion();
                checkPermissionsVersion(v);
                try
                {
                    WormholeXTreme.permissions = ((Permissions)test).getHandler();
                    WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Attached to Permissions version " + v);
                    if (ConfigManager.getSimplePermissions())
                    {
                        WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Simple Permissions Enabled");
                    }
                    else
                    {
                        WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Complex Permissions Enabled");
                    }
                }
                catch ( Exception e)
                {
                    WormholeXTreme.thisPlugin.prettyLog(Level.WARNING, false, "Failed to get Permissions Handler. Defaulting to built-in permissions.");
                }
            } 
            else 
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Permission Plugin not yet available. Defaulting to built-in permissions until Permissions is loaded.");
            }
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Permission Plugin support disabled via settings.txt.");
        }
    }
    
    /**
     * Disable permissions.
     */
    public static void disablePermissions()
    {
        if (WormholeXTreme.permissions != null)
        {
            WormholeXTreme.permissions = null;
            WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Detached from Permissions plugin.");
        }
    }
    /**
     * Check permissions version.
     *
     * @param version the version
     */
    private static void checkPermissionsVersion(String version)
    {
        if ( !version.equals("2.4") && !version.startsWith("2.5"))
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of Permissions. Recommended is 2.5.4" );
        }
       
    }
}
