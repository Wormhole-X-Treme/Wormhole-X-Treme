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
package com.wormhole_xtreme.plugin;

import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;

// TODO: Auto-generated Javadoc
/**
 * The Class IConomySupport.
 *
 * @author alron
 */
public class IConomySupport {
    
    /**
     * Setup iconomy.
     */
    public static void enableIconomy() 
    {
        if(WormholeXTreme.iconomy == null && !ConfigManager.getIconomySupportDisable()) 
        {
            Plugin test = WormholeXTreme.thisPlugin.getServer().getPluginManager().getPlugin("iConomy");
            if(test != null) 
            {
                String v = test.getDescription().getVersion();
                checkIconomyVersion(v);
                try
                {
                    WormholeXTreme.iconomy = ((iConomy)test);
                    WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy version " + v);
                }
                catch ( Exception e)
                {
                    WormholeXTreme.thisPlugin.prettyLog(Level.WARNING, false, "Failed to get cast to iConomy.");
                }
            } 
            else 
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "iConomy Plugin not yet available - there will be no iConomy integration until loaded.");
            }
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "iConomy Plugin support disabled via settings.txt.");
        }
    }
    
    /**
     * Disable iconomy.
     */
    public static void disableIconomy()
    {
            if (WormholeXTreme.iconomy != null)
            {
                WormholeXTreme.iconomy = null;
                WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Detached from iConomy plugin.");
            }
    }
    
    /**
     * Check iconomy version.
     *
     * @param version the version
     */
    private static void checkIconomyVersion(String version)
    {
        if ( !version.equals("4.0") && !version.equals("4.1") && !version.startsWith("4.2") && !version.startsWith("4.3") && 
            !version.startsWith("4.4") && !version.startsWith("4.5") && !version.startsWith("4.6"))
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.WARNING, false, "Not a supported version of iConomy. Recommended is 4.5" );
        }
       
    }
}
