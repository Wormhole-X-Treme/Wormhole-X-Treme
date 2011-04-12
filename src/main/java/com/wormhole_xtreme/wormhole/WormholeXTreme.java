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

import java.util.ArrayList;
import java.util.HashMap; 
import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.entity.Player; 
import org.bukkit.event.Event; 
import org.bukkit.event.Event.Priority; 
import org.bukkit.plugin.PluginManager; 
import org.bukkit.plugin.java.JavaPlugin; 
import org.bukkit.scheduler.BukkitScheduler;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;

import com.wormhole_xtreme.wormhole.command.*;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.config.Configuration;
import com.wormhole_xtreme.wormhole.logic.StargateHelper;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateDBManager;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager;
import com.wormhole_xtreme.wormhole.plugin.HelpSupport;
import com.wormhole_xtreme.wormhole.plugin.IConomySupport;
import com.wormhole_xtreme.wormhole.plugin.PermissionsSupport;
import com.wormhole_xtreme.wormhole.utils.DBUpdateUtil;

import me.taylorkelly.help.Help;

/**
 * WormholeXtreme for Bukkit.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTreme extends JavaPlugin
{

    /** The player listener. */
    private static final WormholeXTremePlayerListener playerListener = new WormholeXTremePlayerListener();
    /** The block listener. */
    private static final WormholeXTremeBlockListener blockListener = new WormholeXTremeBlockListener();
    /** The vehicle listener. */
    private static final WormholeXTremeVehicleListener vehicleListener = new WormholeXTremeVehicleListener();
    /** The entity listener. */
    private static final WormholeXTremeEntityListener entityListener = new WormholeXTremeEntityListener();
    /** The server listener. */
    private static final WormholeXTremeServerListener serverListener = new WormholeXTremeServerListener();
    /** The server listener. */
    private static final WormholeXTremeRedstoneListener redstoneListener = new WormholeXTremeRedstoneListener();

    /** The debugees. */
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();


    /** The Permissions. */
    private static PermissionHandler permissions = null;

    /** The Iconomy. */
    private static iConomy iconomy = null;

    /** The Help. */
    private static Help help = null;

    /** The Scheduler. */
    private static BukkitScheduler scheduler = null;

    /** The This plugin. */
    private static WormholeXTreme thisPlugin = null;

    /** The log. */
    private static Logger log = null;

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
     */
    @Override
    public void onLoad()
    {
        setThisPlugin(this);
        setLog(getThisPlugin().getServer().getLogger()); 
        setScheduler(getThisPlugin().getServer().getScheduler());

        prettyLog(Level.INFO,true,getThisPlugin().getDescription().getAuthors().toString() + "Load Beginning." );
        // Load our config files and set logging level right away.
        ConfigManager.setupConfigs(getThisPlugin().getDescription());
        WormholeXTreme.setPrettyLogLevel(ConfigManager.getLogLevel());
        // Make sure DB is up to date with latest SCHEMA
        DBUpdateUtil.updateDB();
        // Load our shapes, stargates, and internal permissions.
        StargateHelper.loadShapes();	 
        StargateDBManager.loadStargates(getThisPlugin().getServer());
        PermissionsManager.loadPermissions();
        prettyLog(Level.INFO,true, "Load Completed.");
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    @Override
    public void onEnable()
    { 
        prettyLog(Level.INFO,true,"Enable Beginning.");
        // Try and attach to Permissions and iConomy and Help
        try
        {
            PermissionsSupport.enablePermissions();
            IConomySupport.enableIconomy();
            HelpSupport.enableHelp();
        }
        catch ( Exception e)
        {
            prettyLog(Level.WARNING,false, "Caught Exception while trying to load support plugins." + e.getMessage());
        }
        // Register our events and commands
        registerEvents();
        registerCommands();
        HelpSupport.registerHelpCommands();
        prettyLog(Level.INFO, true, "Enable Completed.");
    }

    /**
     * Register commands.
     */
    private static void registerCommands()
    {
        final WormholeXTreme tp = getThisPlugin();
        tp.getCommand("wxforce").setExecutor(new Force());
        tp.getCommand("wxidc").setExecutor(new WXIDC());
        tp.getCommand("wxcompass").setExecutor(new Compass());
        tp.getCommand("wxcomplete").setExecutor(new Complete());
        tp.getCommand("wxremove").setExecutor(new WXRemove());
        tp.getCommand("wxlist").setExecutor(new WXList());
        tp.getCommand("wxgo").setExecutor(new Go());
        tp.getCommand("dial").setExecutor(new Dial());
        tp.getCommand("wxbuild").setExecutor(new Build());
        tp.getCommand("wormhole").setExecutor(new Wormhole());
    }

    /**
     * Register events.
     */
    private static void registerEvents() 
    {
        final WormholeXTreme tp = getThisPlugin();
        final PluginManager pm = tp.getServer().getPluginManager();
        //Listen for Interact, Physics, Break, Flow, and RightClick events. Pass to blockListener
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Highest, tp);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, tp);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Highest, tp);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.High, tp);
        pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.High, tp);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.High, tp);

        // To handle teleporting when walking into a gate.
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.High, tp);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.High, tp);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, playerListener, Priority.High, tp);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, playerListener, Priority.High, tp);

        pm.registerEvent(Event.Type.REDSTONE_CHANGE, redstoneListener, Priority.Normal, tp);
        // Handle removing player data
        // pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        // Handle minecarts going through portal
        pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.High, tp);
        pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.High, tp);
        // Handle player walking through the lava.
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.High, tp);
        // Handle Creeper explosions damaging Gate components.
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.High, tp);
        // Listen for enable events.
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, tp);
        // Listen for disable events.
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, tp);
    }


    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    @Override
    public void onDisable() 
    {  
        try
        {
            Configuration.writeFile(this.getDescription());
            final ArrayList<Stargate> gates = StargateManager.getAllGates();
            // Store all our gates
            for ( Stargate gate : gates )
            {
                gate.shutdownStargate(false);
                StargateDBManager.stargateToSQL(gate);
            }

            StargateDBManager.shutdown();
            prettyLog(Level.INFO, true, "Successfully shutdown.");
        }
        catch ( Exception e)
        {
            prettyLog(Level.SEVERE,false,"Caught exception while shutting down: " + e.getMessage());
            e.printStackTrace();
        }
    } 


    /**
     * Checks if is debugging.
     *
     * @param player the player
     * @return true, if is debugging
     */
    public boolean isDebugging(final Player player) 
    { 
        return debugees.containsKey(player) && debugees.get(player).booleanValue();
    }

    /**
     * Sets the debugging.
     *
     * @param player the player
     * @param value the value
     */
    public void setDebugging(final Player player, final boolean value) 
    {
        debugees.put(player, Boolean.valueOf(value)); 
    }

    /**
     * 
     * prettyLog: A quick and dirty way to make log output clean, unified, and with versioning as needed.
     * 
     * @param severity Level of severity in the form of INFO, WARNING, SEVERE, etc.
     * @param version true causes version display in log entries.
     * @param message to prettyLog.
     * 
     */
    public void prettyLog(Level severity, boolean version, String message) 
    {
        final String prettyName = (String)("[" + getThisPlugin().getDescription().getName() + "]");
        final String prettyVersion = (String)("[v" + getThisPlugin().getDescription().getVersion() + "]");
        String prettyLogLine = prettyName;
        if (version)
        {
            prettyLogLine += prettyVersion;
            getLogger().log(severity,prettyLogLine + message);
        } 
        else
        {
            getLogger().log(severity,prettyLogLine + message);
        }
    }

    /**
     * Sets the pretty log level.
     *
     * @param level the new pretty log level
     */
    public static void setPrettyLogLevel(Level level)
    {
        getLogger().setLevel(level);
        getThisPlugin().prettyLog(Level.CONFIG, false, "Logging set to: " + level );
    }

    /**
     * Sets the this plugin.
     *
     * @param thisPlugin the new this plugin
     */
    private static void setThisPlugin(WormholeXTreme thisPlugin)
    {
        WormholeXTreme.thisPlugin = thisPlugin;
    }

    /**
     * Gets the this plugin.
     *
     * @return the this plugin
     */
    public static WormholeXTreme getThisPlugin()
    {
        return thisPlugin;
    }

    /**
     * Sets the scheduler.
     *
     * @param scheduler the new scheduler
     */
    private static void setScheduler(BukkitScheduler scheduler)
    {
        WormholeXTreme.scheduler = scheduler;
    }

    /**
     * Gets the scheduler.
     *
     * @return the scheduler
     */
    public static BukkitScheduler getScheduler()
    {
        return scheduler;
    }

    /**
     * Sets the log.
     *
     * @param log the new log
     */
    private static void setLog(Logger log)
    {
        WormholeXTreme.log = log;
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    private static Logger getLogger()
    {
        return log;
    }

    /**
     * Sets the iconomy.
     *
     * @param iconomy the new iconomy
     */
    public static void setIconomy(iConomy iconomy)
    {
        WormholeXTreme.iconomy = iconomy;
    }

    /**
     * Gets the iconomy.
     *
     * @return the iconomy
     */
    public static iConomy getIconomy()
    {
        return iconomy;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions the new permissions
     */
    public static void setPermissions(PermissionHandler permissions)
    {
        WormholeXTreme.permissions = permissions;
    }

    /**
     * Gets the permissions.
     *
     * @return the permissions
     */
    public static PermissionHandler getPermissions()
    {
        return permissions;
    }

    /**
     * Sets the help.
     *
     * @param help the new help
     */
    public static void setHelp(Help help)
    {
        WormholeXTreme.help = help;
    }

    /**
     * Gets the help.
     *
     * @return the help
     */
    public static Help getHelp() {
        return help;
    }
} 

