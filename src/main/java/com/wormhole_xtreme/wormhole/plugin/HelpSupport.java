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

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;

import me.taylorkelly.help.Help;

// TODO: Auto-generated Javadoc
/**
 * The Class HelpPlugin.
 *
 * @author alron
 */
public class HelpSupport {
    
    /**
     * Setup help.
     */
    public static void enableHelp()
    {
        if (WormholeXTreme.getHelp() == null && !ConfigManager.getHelpSupportDisable())
        {
            final Plugin helptest = WormholeXTreme.getThisPlugin().getServer().getPluginManager().getPlugin("Help");
            if (helptest != null)
            {
                final String version = helptest.getDescription().getVersion();
                checkHelpVersion(version);
                try 
                {
                    WormholeXTreme.setHelp(((Help)helptest));
                    WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Attached to Help version " + version);
                }
                catch (Exception e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Failed to get cast to Help: " + e.getMessage() );
                }
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Help Plugin not yet available - there will be no Help integration until loaded.");
            }
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Help Plugin support disabled via settings.txt.");
        }
    }
    
    /**
     * Disable help.
     */
    public static void disableHelp()
    {
        if (WormholeXTreme.getHelp() != null)
        {
            WormholeXTreme.setHelp(null);
            WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Detached from Help plugin.");
        }
    }
    /**
     * Check help version.
     *
     * @param version the version
     */
    private static void checkHelpVersion(String version)
    {
        if (!version.startsWith("0.2"))
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Not a supported version of Help. Recommended is 0.2" );
        }
    }
    
    /**
     * Register help commands.
     */
    public static void registerHelpCommands()
    {
        if (WormholeXTreme.getHelp() != null && !ConfigManager.getHelpSupportDisable())
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
            if (WormholeXTreme.getPermissions() != null)
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
          
            WormholeXTreme.getHelp().registerCommand("dial [stargate] <idc>","Dial [stargate] and optionally unlock <idc>" , WormholeXTreme.getThisPlugin(), true, dial);
            WormholeXTreme.getHelp().registerCommand("wxidc [stargate] <idc|-clear>", "Display [stargate] idc, optionally set <idc> or <-clear> idc", WormholeXTreme.getThisPlugin(), wxidc);
            WormholeXTreme.getHelp().registerCommand("wxforce [close|drop]", "Forcefully [close] all gates or [drop] all iris", WormholeXTreme.getThisPlugin(), wxforce);
            WormholeXTreme.getHelp().registerCommand("wxcompass", "Point compass at nearest Stargate", WormholeXTreme.getThisPlugin(), wxcompass);
            WormholeXTreme.getHelp().registerCommand("wxcomplete [stargate] <idc=[idc]> <net=[net]>", "Complete [stargate] construction, optional [idc] and [net]", WormholeXTreme.getThisPlugin(), true, wxcomplete);
            WormholeXTreme.getHelp().registerCommand("wxremove [stargate] <-all>", "Remove a [stargate], optionally destroy <-all> its blocks", WormholeXTreme.getThisPlugin(), wxremove);
            WormholeXTreme.getHelp().registerCommand("wxlist", "List all stargates", WormholeXTreme.getThisPlugin(), wxlist);
            WormholeXTreme.getHelp().registerCommand("wxgo [stargate]", "Teleport to [stargate]", WormholeXTreme.getThisPlugin(), wxgo);
            WormholeXTreme.getHelp().registerCommand("wxbuild [gateshape]", "Automaticially build a stargate in the specified [gateshape]", WormholeXTreme.getThisPlugin(), wxbuild);
            WormholeXTreme.getHelp().registerCommand("wormhole","Wormhole administration and configuration command", WormholeXTreme.getThisPlugin(), true, wormhole);
            WormholeXTreme.getHelp().registerCommand("wormhole owner [stargate] <owner>","Display owner of [stargate], optionally change <owner>", WormholeXTreme.getThisPlugin(), wormhole);
            WormholeXTreme.getHelp().registerCommand("wormhole portalmaterial <material>","Display portal material, optionally change <material>", WormholeXTreme.getThisPlugin(), wormhole);
            WormholeXTreme.getHelp().registerCommand("wormhole irismaterial <material>","Display iris material, optionally change <material>", WormholeXTreme.getThisPlugin(), wormhole);
            WormholeXTreme.getHelp().registerCommand("wormhole shutdown_timeout <timeout>","Display shutdown timeout, optionally change <timeout>", WormholeXTreme.getThisPlugin(), wormhole);
            WormholeXTreme.getHelp().registerCommand("wormhole activate_timeout <timeout>","Display activation timeout, optionally change <timeout>", WormholeXTreme.getThisPlugin(), wormhole);
            WormholeXTreme.getHelp().registerCommand("wormhole simple <boolean>","Display simple permissions, optionally change via <boolean>", WormholeXTreme.getThisPlugin(), wormhole);
        }
    }
}
