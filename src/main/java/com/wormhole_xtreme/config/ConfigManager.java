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
package com.wormhole_xtreme.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;

import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;


// TODO: Auto-generated Javadoc
/**
 * The Class ConfigManager.
 */
public class ConfigManager 
{
	/*
	 * Set of standard text strings when a message is sent to users.
	 * Hopefully this will reduce any wrong messsages being sent. 
	 */
	/** The Constant output_strings. */
	public static final ConcurrentHashMap<StringTypes, String> output_strings = new ConcurrentHashMap<StringTypes, String>();
	
	/** The Constant configurations. */
	public static final ConcurrentHashMap<ConfigKeys, Setting> configurations = new ConcurrentHashMap<ConfigKeys, Setting>(); 
	
	/**
	 * Sets the up configs.
	 *
	 * @param pdf the new up configs
	 */
	public static void setupConfigs(PluginDescriptionFile pdf)
	{
		setupStrings();
		Configuration.loadConfiguration(pdf);
	}
	
	public static final String errorheader = "\u00A73:: \u00A75error \u00A73:: \u00A77";
	public static final String normalheader = "\u00A73:: \u00A77";
	// Used so that I don't have to retype strings over and over again.
	/**
	 * Setup strings.
	 */
	private static void setupStrings() 
	{
		output_strings.put(StringTypes.PERMISSION_NO, errorheader + "You lack the permissions to do this.");
		output_strings.put(StringTypes.TARGET_IS_SELF, errorheader + "Can't dial own gate without solar flare");
		output_strings.put(StringTypes.TARGET_INVALID, errorheader + "Invalid remote gate target.");
		output_strings.put(StringTypes.TARGET_IS_ACTIVE, errorheader + "Target gate is currently active.");
		output_strings.put(StringTypes.GATE_NOT_ACTIVE, errorheader + "No gate activated to dial.");
		output_strings.put(StringTypes.GATE_REMOTE_ACTIVE, errorheader + "Gate remotely activated.");
		output_strings.put(StringTypes.GATE_SHUTDOWN, normalheader + "Gate successfully shutdown.");
		output_strings.put(StringTypes.GATE_ACTIVATED, normalheader + "Gate successfully activated.");
		output_strings.put(StringTypes.GATE_DEACTIVATED, normalheader + "Gate successfully deactivated.");
		output_strings.put(StringTypes.GATE_DIALED, normalheader + "Gate successfully dialed.");
		output_strings.put(StringTypes.CONSTRUCT_SUCCESS, normalheader + "Gate successfully constructed.");
		output_strings.put(StringTypes.CONSTRUCT_NAME_INVALID, errorheader + "Gate name invalid: ");
		output_strings.put(StringTypes.CONSTRUCT_NAME_TOO_LONG, errorheader + "Gate name too long: ");
		output_strings.put(StringTypes.CONSTRUCT_NAME_TAKEN, errorheader + "Gate name already taken: ");
		output_strings.put(StringTypes.REQUEST_INVALID, errorheader + "Invalid Request");
		output_strings.put(StringTypes.GATE_NOT_SPECIFIED, errorheader + "No gate name specified.");
	}
	
	/**
	 * Sets the config value.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public static void setConfigValue(ConfigKeys key, Object value)
	{
		Setting s = configurations.get(key);
		if (value != null) {
			s.setValue(value);
		}
		else
		{
			//TODO SCREAM BLOODY MURDER IN LOGS ABOUT NULL VALUE
		}
	}

	/**
	 * Get portal material setting from ConfigKeys, return sane Material value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the portal material
	 */
	public static Material getPortalMaterial()
	{
		Setting pm;
		if ((pm = ConfigManager.configurations.get(ConfigKeys.PORTAL_MATERIAL)) != null) 
		{
			return pm.getMaterialValue();
		}
		else 
		{
			Material m = Material.STATIONARY_WATER;
			return m;
		}
	}
	
	/**
	 * Set portal material value in ConfigKeys.
	 *
	 * @param m the new portal material
	 */
	public static void setPortalMaterial(Material m)
	{
		ConfigManager.setConfigValue(ConfigKeys.PORTAL_MATERIAL, m);
	}
	
	/**
	 * Get Iris material setting from ConfigKeys. Return sane Material value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the iris material
	 */
	public static Material getIrisMaterial()
	{
		Setting im;
		if ((im = ConfigManager.configurations.get(ConfigKeys.IRIS_MATERIAL)) != null)
		{
			return im.getMaterialValue();
		}
		else
		{
			Material m = Material.STONE;
			return m;
		}
	}
	
	/**
	 * Set Iris material value in ConfigKeys.
	 * @param m Material to set.
	 */
	public static void setIrisMaterial(Material m)
	{
		ConfigManager.setConfigValue(ConfigKeys.IRIS_MATERIAL, m);
	}
	
	/**
	 * Get Timeout Activate setting from ConfigKeys.
	 * Return default value if key is missing or broken.
	 * @return Timeout in seconds.
	 */
	public static int getTimeoutActivate()
	{
		Setting ta;
		if ((ta = ConfigManager.configurations.get(ConfigKeys.TIMEOUT_ACTIVATE)) != null)
		{
			return ta.getIntValue();
		}
		else
		{
			int i = 30;
			return i;
		}
	}
	
	/**
	 * Set timeout activate setting in ConfigKeys.
	 *
	 * @param i Timeout in seconds.
	 */
	public static void setTimeoutActivate(int i)
	{
		ConfigManager.setConfigValue(ConfigKeys.TIMEOUT_ACTIVATE, i);
	}
	
	/**
	 * Get Timeout Shutdown setting from ConfigKeys.
	 * Return default value if key is missing or broken.
	 * @return Timeout in seconds.
	 */
	public static int getTimeoutShutdown()
	{
		Setting ts;
		if ((ts = ConfigManager.configurations.get(ConfigKeys.TIMEOUT_SHUTDOWN)) != null)
		{
			return ts.getIntValue();
		}
		else
		{
			int i = 38;
			return i;
		}
	}
	
	/**
	 * Set timeout shutdown setting in ConfigKeys.
	 *
	 * @param i the new timeout shutdown
	 */
	public static void setTimeoutShutdown(int i)
	{
		ConfigManager.setConfigValue(ConfigKeys.TIMEOUT_SHUTDOWN,i);
	}
	
	/**
	 * Get Portal Woosh setting from ConfigKeys. Return sane boolean value.
	 * @return Woosh enabled?
	 */
	public static boolean getPortalWoosh()
	{
		Setting pw;
		if ((pw = ConfigManager.configurations.get(ConfigKeys.PORTAL_WOOSH)) != null)
		{
			return pw.getBooleanValue();
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * Placeholder:
	 * Get Stargate Material settings from ConfigKeys. Return sane Material value.
	 * Return default if setting is missing or broken.
	 *
	 * @return the stargate material
	 */
	public static Material getStargateMaterial()
	{
		return Material.OBSIDIAN;
	}
	
	/**
	 * Placeholder:
	 * Get Stargate Material settings from ConfigKeys. Return sane Material value.
	 * Return default if setting is missing or broken.
	 *
	 * @return the activate material
	 */
	public static Material getActivateMaterial()
	{
		return Material.GLOWSTONE;
	}
	
	/**
	 * Get iConomy Op Excempt settings from ConfigKeys. Return sane boolean value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the iconomy ops excempt
	 */
	public static boolean getIconomyOpsExcempt()
	{
		Setting ioe;
		if ((ioe = ConfigManager.configurations.get(ConfigKeys.ICONOMY_OPS_EXEMPT)) != null)
		{
			return ioe.getBooleanValue();
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * Get iConomy wormhole use cost settings from ConfigKeys. Return sane integer value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the iconomy wormhole use cost
	 */
	public static double getIconomyWormholeUseCost()
	{
		Setting iwuc;
		if ((iwuc = ConfigManager.configurations.get(ConfigKeys.ICONOMY_WORMHOLE_USE_COST)) != null)
		{
			return iwuc.getDoubleValue();
		}
		else 
		{
			double i = 0.0;
			return i;
		}
	}
	
	/**
	 * Get iConomy wormhole owner percent settings from ConfigKeys. Return sane double value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the iconomy wormhole owner percent
	 */
	public static double getIconomyWormholeOwnerPercent()
	{
		Setting iwop;
		if ((iwop = ConfigManager.configurations.get(ConfigKeys.ICONOMY_WORMHOLE_OWNER_PERCENT)) != null)
		{
			return iwop.getDoubleValue();
		}
		else
		{
			double d = 0.0;
			return d;
		}
	}
	
	/**
	 * Get iConomy wormhole build cost settings from ConfigKeys. Return sane int value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the iconomy wormhole build cost
	 */
	public static double getIconomyWormholeBuildCost()
	{
		Setting iwbc;
		if ((iwbc = ConfigManager.configurations.get(ConfigKeys.ICONOMY_WORMHOLE_BUILD_COST)) != null) 
		{
			return iwbc.getDoubleValue();
		}
		else
		{
			double i = 0.0;
			return i;
		}
	}
	
	/**
	 * Get Built in permissions enabled settings from ConfigKeys. Return sane boolean value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the built in permissions enabled
	 */
	public static boolean getBuiltInPermissionsEnabled()
	{
		Setting bipe;
		if ((bipe = ConfigManager.configurations.get(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED)) != null)
		{
			return bipe.getBooleanValue();
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Get Built in default permission level settings from ConfigKeys. Return sane PermissionLevel.
	 * Return default value if key is missing or broken.
	 *
	 * @return the built in default permission level
	 */
	public static PermissionLevel getBuiltInDefaultPermissionLevel()
	{
		Setting bidpl;
		if ((bidpl = ConfigManager.configurations.get(ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL)) != null)
		{
			return bidpl.getPermissionLevel();
		}
		else
		{
			return PermissionLevel.WORMHOLE_USE_PERMISSION;
		}
	}
	
	/**
	 * Get Log Level setting from ConfigKeys. Return sane Level value.
	 * Return default value if key is missing or broken.
	 *
	 * @return the log level
	 */
	public static Level getLogLevel()
	{
		Setting ll;
		if ((ll = ConfigManager.configurations.get(ConfigKeys.LOG_LEVEL)) != null)
		{
			return ll.getLevel();
		}
		else
		{
			return Level.INFO;
		}
	}
	
	/*
	 * Get Built in permissions enabled settings from ConfigKeys. Return sane boolean value.
	 * Return default value if key is missing or broken.
	 */
	/**
	 * Gets the wormhole use is teleport.
	 *
	 * @return the wormhole use is teleport
	 */
	public static boolean getWormholeUseIsTeleport()
	{
		Setting bipe;
		if ((bipe = ConfigManager.configurations.get(ConfigKeys.WORMHOLE_USE_IS_TELEPORT)) != null)
		{
			return bipe.getBooleanValue();
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Gets the simple permissions.
	 *
	 * @return the simple permissions
	 */
	public static boolean getSimplePermissions()
	{
	    Setting sp;
	    if ((sp = ConfigManager.configurations.get(ConfigKeys.SIMPLE_PERMISSIONS)) != null)
	    {
	        return sp.getBooleanValue();
	    }
	    else
	    {
	        return false;
	    }
	}
	
	public static void setSimplePermissions(boolean b)
	{
	    ConfigManager.setConfigValue(ConfigKeys.SIMPLE_PERMISSIONS, b);
	}
	
	/**
	 * The Enum StringTypes.
	 */
	public enum StringTypes
	{
		
		/** The PERMISSIO_NO. */
		PERMISSION_NO,
		
		/** The TARGET_IS_SELF. */
		TARGET_IS_SELF,
		
		/** The TARGET_INVALID. */
		TARGET_INVALID,
		
		/** The TARGET_IS_ACTIVE. */
		TARGET_IS_ACTIVE,
		
		/** The GATE_NOT_ACTIVE. */
		GATE_NOT_ACTIVE,
		
		/** The GAT e_ remot e_ active. */
		GATE_REMOTE_ACTIVE,
		
		/** The GAT e_ shutdown. */
		GATE_SHUTDOWN,
		
		/** The GAT e_ activated. */
		GATE_ACTIVATED,
		
		/** The GAT e_ deactivated. */
		GATE_DEACTIVATED,
		
		/** The GAT e_ dialed. */
		GATE_DIALED,
		
		/** The CONSTRUC t_ success. */
		CONSTRUCT_SUCCESS,
		
		/** The CONSTRUC t_ nam e_ invalid. */
		CONSTRUCT_NAME_INVALID,
		
		/** The CONSTRUC t_ nam e_ to o_ long. */
		CONSTRUCT_NAME_TOO_LONG,
		
		/** The CONSTRUC t_ nam e_ taken. */
		CONSTRUCT_NAME_TAKEN,
		
		/** The REQUES t_ invalid. */
		REQUEST_INVALID,
		
		GATE_NOT_SPECIFIED
	}

	/**
	 * The Enum ConfigKeys.
	 */
	public enum ConfigKeys
	{
		
		/** The BUIL t_ i n_ permission s_ enabled. */
		BUILT_IN_PERMISSIONS_ENABLED,
		
		/** The BUIL t_ i n_ defaul t_ permissio n_ level. */
		BUILT_IN_DEFAULT_PERMISSION_LEVEL,
		
		/** The SIMPL e_ permissions. */
		SIMPLE_PERMISSIONS,
		
		/** The WORMHOL e_ us e_ i s_ teleport. */
		WORMHOLE_USE_IS_TELEPORT,
		
		/** The TIMEOU t_ activate. */
		TIMEOUT_ACTIVATE,
		
		/** The TIMEOU t_ shutdown. */
		TIMEOUT_SHUTDOWN,
		
		/** The PORTA l_ material. */
		PORTAL_MATERIAL,
		
		/** The PORTA l_ woosh. */
		PORTAL_WOOSH,
		
		/** The IRI s_ material. */
		IRIS_MATERIAL,
		
		/** The ICONOM y_ wormhol e_ us e_ cost. */
		ICONOMY_WORMHOLE_USE_COST,
		
		/** The ICONOM y_ wormhol e_ buil d_ cost. */
		ICONOMY_WORMHOLE_BUILD_COST,
		
		/** The ICONOM y_ op s_ exempt. */
		ICONOMY_OPS_EXEMPT, 
		
		/** The ICONOM y_ wormhol e_ owne r_ percent. */
		ICONOMY_WORMHOLE_OWNER_PERCENT,
		
		/** The LO g_ level. */
		LOG_LEVEL
	}
}