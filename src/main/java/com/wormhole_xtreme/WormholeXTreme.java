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
 
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.logging.Logger;
import java.util.logging.Level;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player; 
import org.bukkit.event.Event; 
import org.bukkit.event.Event.Priority; 
import org.bukkit.plugin.PluginDescriptionFile; 
import org.bukkit.plugin.PluginManager; 
import org.bukkit.plugin.java.JavaPlugin; 


import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.Configuration;
import com.wormhole_xtreme.logic.StargateHelper;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateDBManager;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.utils.DBUpdateUtil;
import com.wormhole_xtreme.command.*;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme for Bukkit.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTreme extends JavaPlugin
{

	/** The player listener. */
	private final WormholeXTremePlayerListener playerListener = new WormholeXTremePlayerListener(this);
	
	/** The block listener. */
	private final WormholeXTremeBlockListener blockListener = new WormholeXTremeBlockListener(this);
	
	/** The vehicle listener. */
	private final WormholeXTremeVehicleListener vehicleListener = new WormholeXTremeVehicleListener(this);
	//private final WormholeXTremeEntityListener entityListener = new WormholeXTremeEntityListener(this);
	/** The server listener. */
	private final WormholeXTremeServerListener serverListener = new WormholeXTremeServerListener(this);
	
	/** The debugees. */
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

	
	/** The Permissions. */
	public static volatile PermissionHandler Permissions = null;
	
	/** The Iconomy. */
	public static volatile iConomy Iconomy = null;
	
	/** The Scheduler. */
	public static BukkitScheduler Scheduler = null;
	
	/** The This plugin. */
	public static WormholeXTreme ThisPlugin = null;
	 
	/** The log. */
	private static Logger log;
 
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
    public void onEnable()
	{ 
		log = this.getServer().getLogger();
		ThisPlugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		prettyLog(Level.INFO,true, pdfFile.getAuthors() + "Load Beginning." );
		
		// Register our events 
		registerEvents();
		
		ConfigManager.setupConfigs(pdfFile);

		// Make sure DB is up to date with latest SCHEMA
		DBUpdateUtil.updateDB();
		
		this.setPrettyLogLevel(ConfigManager.getLogLevel());
		
		StargateHelper.loadShapes();
		Scheduler = getServer().getScheduler();
		
		try
		{
			setupPermissions();
			setupIconomy();
		}
		catch ( Exception e)
		{
			
		}

		StargateDBManager.LoadStargates(getServer());
		PermissionsManager.LoadPermissions();
		prettyLog(Level.INFO, true, "Load Completed.");
		
		this.registerCommands();
	}
	
	/**
	 * Register commands.
	 */
	private void registerCommands()
	{
	    getCommand("wxforce").setExecutor(new WXForce(this));
		getCommand("wxidc").setExecutor(new WXIDC(this));
		getCommand("wxcompass").setExecutor(new WXCompass(this));
		getCommand("wxcomplete").setExecutor(new WXComplete(this));
		getCommand("wxremove").setExecutor(new WXRemove(this));
		getCommand("wxlist").setExecutor(new WXList(this));
		getCommand("wxgo").setExecutor(new WXGo(this));
		getCommand("dial").setExecutor(new Dial(this));
	}
    /**
     * Register events.
     */
    private void registerEvents() 
    {
		PluginManager pm = getServer().getPluginManager(); 
		
		//Listen for Interact, Physics, Break, Flow, and RightClick evebts. Pass to blockListener
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.High, this);
		
		// To handle teleporting when walking into a gate.
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.High, this);
		// Handle removing player data
		// pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

		// Handle minecarts going through portal
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.High, this);
		// Handle player walking through the lava.
		//pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.High, this);
		
		// Listen for enable events.
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
		// Listen for disable events.
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
	}

	/**
	 * Setup permissions.
	 */
	public void setupPermissions() 
    {
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

    	if(Permissions == null) 
    	{
    	    if(test != null)
    	    {
    	    	String v = test.getDescription().getVersion();
    	    	serverListener.checkPermissionsVersion(v);
    	    	try
    	    	{
    	    		Permissions = ((Permissions)test).getHandler();
    	            WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to Permissions version " + v);
    	    	}
    	    	catch ( Exception e)
    	    	{
    	    		prettyLog(Level.WARNING, false, "Failed to get Permissions Handler. Defaulting to built-in permissions.");
    	    	}
    	    } 
    	    else 
    	    {
    			prettyLog(Level.WARNING, false, "Permission Plugin not yet available. Defaulting to built-in permissions until Permissions is loaded.");
    	    }
    	}
    }
	
    /**
     * Setup iconomy.
     */
    public void setupIconomy() 
    {
    	Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");


    	if(Iconomy == null) 
    	{
    		if(test != null) 
    	    {
        		String v = test.getDescription().getVersion();
        		serverListener.checkIconomyVersion(v);
        		try
        		{
	    	    	Iconomy = ((iConomy)test);
	                WormholeXTreme.ThisPlugin.prettyLog(Level.INFO, false, "Attached to iConomy version " + v);
        		}
        		catch ( Exception e)
        		{
        			prettyLog(Level.WARNING, false, "Failed to get cast to iConomy. Defaulting to built-in permissions.");
        		}
    	    } 
    	    else 
    	    {
    			prettyLog(Level.WARNING, false, "iConomy Plugin not yet available - there will be no iConomy integration until loaded.");
    	    	//this.getServer().getPluginManager().disablePlugin(this);
    	    }
    	}
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
			ArrayList<Stargate> gates = StargateManager.GetAllGates();
	
			// Store all our gates
			for ( Stargate gate : gates )
			{
				gate.ShutdownStargate();
				StargateDBManager.StargateToSQL(gate);
			}
			
			StargateDBManager.Shutdown();

			prettyLog(Level.INFO, true, "Successfully shutdown.");
		}
		catch ( Exception e)
		{
			prettyLog(Level.SEVERE,false,"Caught exception while shutting down: " + e.getMessage());
			e.printStackTrace();
		}
	} 

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		String commandName = command.getName().toLowerCase();
		if (commandName.equals("wormhole"))
		{
			return WormholeXTremeCommand.commandWormhole(sender, args);
		}
		else if (commandName.equals("wxbuild"))
		{
		    return WormholeXTremeCommand.commandBuildGate(sender, args);
		}
		return false;
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
	 * prettyLog: A quick and dirty way to make log output clean, unified, and versioning as needed.
	 * 
	 * @param severity Level of severity in the form of INFO, WARNING, SEVERE, etc.
	 * @param version true causes version display in log entries.
	 * @param message to prettyLog.
	 * 
	 */
	public void prettyLog(Level severity, boolean version, String message) 
	{
		final String prettyName = (String)("[" + this.getDescription().getName() + "]");
		final String prettyVersion = (String)("[v" + this.getDescription().getVersion() + "]");
		String prettyLogLine = prettyName;
		if (version)
		{
			prettyLogLine += prettyVersion;
			log.log(severity,prettyLogLine + message);
		} 
		else
		{
			log.log(severity,prettyLogLine + message);
		}
	}
	
	/**
	 * Sets the pretty log level.
	 *
	 * @param level the new pretty log level
	 */
	public void setPrettyLogLevel(Level level)
	{
		log.setLevel(level);
		this.prettyLog(Level.CONFIG, false, "Logging set to: " + level );
	}
} 
 
