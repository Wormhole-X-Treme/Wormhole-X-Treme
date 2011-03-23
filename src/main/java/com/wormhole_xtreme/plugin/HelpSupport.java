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

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;

import me.taylorkelly.help.Help;

// TODO: Auto-generated Javadoc
/**
 * The Class HelpPlugin.
 *
 * @author alron
 */
public class HelpSupport {
    
    public HelpSupport(WormholeXTreme wormholeXTreme)
    {
        
    }
    /**
     * Setup help.
     */
    public void setupHelp()
    {
        if (WormholeXTreme.Help == null)
        {
            Plugin helptest = WormholeXTreme.ThisPlugin.getServer().getPluginManager().getPlugin("Help");
            if (helptest != null)
            {
                String version = helptest.getDescription().getVersion();
                checkHelpVersion(version);
                try 
                {
                    WormholeXTreme.Help = ((Help)helptest);
                    WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to Help version " + version);
                }
                catch (Exception e)
                {
                    WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Failed to get cast to Help: " + e.getMessage() );
                }
            }
            else
            {
                WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Help Plugin not yet available - there will be no Help integration until loaded.");
            }
        }
    }
    public void disableHelp()
    {
        if (!(WormholeXTreme.Help == null))
        {
            WormholeXTreme.Help = null;
            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Detached from Help.");
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
    
    /**
     * Register help commands.
     */
    public void registerHelpCommands()
    {
        if (WormholeXTreme.Help != null)
        {
            final String[] cp = new String[] { "wormhole.use.sign",
                                                         "wormhole.use.dialer",
                                                         "wormhole.use.compass",
                                                         "wormhole.remove.own",
                                                         "wormhole.remove.all",
                                                         "wormhole.build",
                                                         "wormhole.config",
                                                         "wormhole.list",
                                                         "wormhole.go"};
            
            final String[] sp = new String[] { "wormhole.simple.use",
                                                            "wormhole.simple.build",
                                                            "wormhole.simple.config",
                                                            "wormhole.simple.remove"};
            
            
            String dial;
            String wxidc;
            String[] wxforce;
            String wxcompass;
            String wxcomplete;
            String[] wxremove;
            String[] wxlist;
            String wxgo;
            String wxbuild;
            String wormhole;
            if (WormholeXTreme.Permissions != null)
            {
                if (ConfigManager.getSimplePermissions())
                {
                    dial = sp[0];
                    wxidc = sp[2];
                    wxforce = new String[] { sp[2],sp[3]};
                    wxcompass = sp[0];
                    wxcomplete = sp[1];
                    wxremove = new String[] { sp[3] };
                    wxlist = new String[] { sp[0],sp[2] };
                    wxgo = sp[2];
                    wxbuild = sp[2];
                    wormhole = sp[2];
                } 
                else 
                {
                    dial = cp[1];
                    wxidc = cp[6];
                    wxforce = new String[] { cp[4],cp[6]};
                    wxcompass = cp[2];
                    wxcomplete = cp[5];
                    wxremove = new String[] { cp[3],cp[4] };
                    wxlist = new String[] { cp[6],cp[7] };
                    wxgo = cp[8];
                    wxbuild = cp[6];
                    wormhole = cp[6];
                }
            }
            else 
            {
                dial = "";
                wxidc = "OP";
                wxforce = new String[] { "OP" };
                wxcompass = "OP";
                wxcomplete = "OP";
                wxremove = new String[] {""};
                wxlist = new String[] {"OP"};
                wxgo = "OP";
                wxbuild = "OP";
                wormhole = "OP";
            }
          
            WormholeXTreme.Help.registerCommand("dial [stargate] <idc>","Dial [stargate] and optionally unlock <idc>" , WormholeXTreme.ThisPlugin, true, dial);
            WormholeXTreme.Help.registerCommand("wxidc [stargate] <idc|-clear>", "Display [stargate] idc, optionally set <idc> or <-clear> idc", WormholeXTreme.ThisPlugin, wxidc);
            WormholeXTreme.Help.registerCommand("wxforce [close|drop]", "Forcefully [close] all gates or [drop] all iris", WormholeXTreme.ThisPlugin, wxforce);
            WormholeXTreme.Help.registerCommand("wxcompass", "Point compass at nearest Stargate", WormholeXTreme.ThisPlugin, wxcompass);
            WormholeXTreme.Help.registerCommand("wxcomplete [stargate] <idc=[idc]> <net=[net]>", "Complete [stargate] construction, optional [idc] and [net]", WormholeXTreme.ThisPlugin, true, wxcomplete);
            WormholeXTreme.Help.registerCommand("wxremove [stargate] <-all>", "Remove a [stargate], optionally destroy <-all> its blocks", WormholeXTreme.ThisPlugin, wxremove);
            WormholeXTreme.Help.registerCommand("wxlist", "List all stargates", WormholeXTreme.ThisPlugin, wxlist);
            WormholeXTreme.Help.registerCommand("wxgo [stargate]", "Teleport to [stargate]", WormholeXTreme.ThisPlugin, wxgo);
            WormholeXTreme.Help.registerCommand("wxbuild [gateshape]", "Automaticially build a stargate in the specified [gateshape]", WormholeXTreme.ThisPlugin, wxbuild);
            WormholeXTreme.Help.registerCommand("wormhole","Wormhole administration and configuration command", WormholeXTreme.ThisPlugin, true, wormhole);
            WormholeXTreme.Help.registerCommand("wormhole owner [stargate] <owner>","Display owner of [stargate], optionally change <owner>", WormholeXTreme.ThisPlugin, wormhole);
            WormholeXTreme.Help.registerCommand("wormhole portalmaterial <material>","Display portal material, optionally change <material>", WormholeXTreme.ThisPlugin, wormhole);
            WormholeXTreme.Help.registerCommand("wormhole irismaterial <material>","Display iris material, optionally change <material>", WormholeXTreme.ThisPlugin, wormhole);
            WormholeXTreme.Help.registerCommand("wormhole shutdown_timeout <timeout>","Display shutdown timeout, optionally change <timeout>", WormholeXTreme.ThisPlugin, wormhole);
            WormholeXTreme.Help.registerCommand("wormhole activate_timeout <timeout>","Display activation timeout, optionally change <timeout>", WormholeXTreme.ThisPlugin, wormhole);
            WormholeXTreme.Help.registerCommand("wormhole simple <boolean>","Display simple permissions status, optionally enable/disable via <boolean>", WormholeXTreme.ThisPlugin, wormhole);
        }
    }
}
