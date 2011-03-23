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

// TODO: Auto-generated Javadoc
/**
 * The Class IConomySupport.
 *
 * @author alron
 */
public class IConomySupport {
    
    /**
     * Instantiates a new i conomy support.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public IConomySupport(WormholeXTreme wormholeXTreme)
    {
    
    }
    
    /**
     * Setup iconomy.
     */
    public void setupIconomy() 
    {
        Plugin test = WormholeXTreme.ThisPlugin.getServer().getPluginManager().getPlugin("iConomy");

        if(WormholeXTreme.Iconomy == null) 
        {
            if(test != null) 
            {
                String v = test.getDescription().getVersion();
                checkIconomyVersion(v);
                try
                {
                    WormholeXTreme.Iconomy = ((iConomy)test);
                    WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy version " + v);
                }
                catch ( Exception e)
                {
                    WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to get cast to iConomy.");
                }
            } 
            else 
            {
                WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "iConomy Plugin not yet available - there will be no iConomy integration until loaded.");
                //this.getServer().getPluginManager().disablePlugin(this);
            }
        }
    }
    
    /**
     * Disable iconomy.
     */
    public void disableIconomy()
    {
            if (!(WormholeXTreme.Iconomy == null))
            {
                WormholeXTreme.Iconomy = null;
                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from iConomy.");
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
}
