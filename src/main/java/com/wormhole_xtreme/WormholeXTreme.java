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

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/** 
 * WormholeXtreme for Bukkit 
 * @author Ben Echols (Lologarithm) 
 */ 
public class WormholeXTreme extends JavaPlugin
{

	private final WormholeXTremePlayerListener playerListener = new WormholeXTremePlayerListener(this);
	private final WormholeXTremeBlockListener blockListener = new WormholeXTremeBlockListener(this);
	private final WormholeXTremeVehicleListener vehicleListener = new WormholeXTremeVehicleListener(this);
	//private final WormholeXTremeEntityListener entityListener = new WormholeXTremeEntityListener(this);
	private final WormholeXTremeServerListener serverListener = new WormholeXTremeServerListener(this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

	
	public static volatile PermissionHandler Permissions = null;
	public static volatile iConomy Iconomy = null;
	public static BukkitScheduler Scheduler = null;
	public static WormholeXTreme ThisPlugin = null;
	 
	private static Logger log;
 
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
	}

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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		String commandName = command.getName().toLowerCase();
		if (commandName.equals("wormhole"))
		{
			return WormholeXTremeCommand.commandWormhole(sender, args);
		}
		else if (commandName.equals("dial"))
		{
			return WormholeXTremeCommand.commandDial(sender, args);
		}
		else if (commandName.equals("wxlist"))
		{
		    return WormholeXTremeCommand.commandList(sender, args);
		}
		else if (commandName.equals("wxbuild"))
		{
		    return WormholeXTremeCommand.commandBuildGate(sender, args);
		}
		else if (commandName.equals("wxremove"))
		{
		    return WormholeXTremeCommand.commandRemoveGate(sender, args);
		}
		else if (commandName.equals("wxcompass"))
		{
		    return WormholeXTremeCommand.commandCompass(sender, args);
		}
		else if (commandName.equals("wxcomplete"))
		{
		    return WormholeXTremeCommand.commandCompleteGate(sender, args);
		}
		return false;
	}

	public boolean isDebugging(final Player player) 
	{ 
		return debugees.containsKey(player) && debugees.get(player).booleanValue();
	}
	
	public void setDebugging(final Player player, final boolean value) 
	{
		debugees.put(player, Boolean.valueOf(value)); 
	}
	
	/**
	 * 
	 * prettyLog: A quick and dirty way to make log output clean, unified, and versioning as needed.
	 * 
	 * @param Level severity in the form of INFO, WARNING, SEVERE, etc.
	 * @param Boolean version true causes version display in log entries.
	 * @param String message to prettyLog.
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
	
	public void setPrettyLogLevel(Level level)
	{
		log.setLevel(level);
		this.prettyLog(Level.CONFIG, false, "Logging set to: " + level );
	}
} 
 
