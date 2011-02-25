package com.wormhole_xtreme.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;

import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;


public class ConfigManager 
{
	/*
	 * Set of standard text strings when a message is sent to users.
	 * Hopefully this will reduce any wrong messsages being sent. 
	 */
	public static final ConcurrentHashMap<StringTypes, String> output_strings = new ConcurrentHashMap<StringTypes, String>();
	
	public static final ConcurrentHashMap<ConfigKeys, Setting> configurations = new ConcurrentHashMap<ConfigKeys, Setting>(); 
	
	public static void setupConfigs(PluginDescriptionFile pdf)
	{
		setupStrings();
		Configuration.loadConfiguration(pdf);
	}

	// Used so that I don't have to retype strings over and over again.
	private static void setupStrings() 
	{
		output_strings.put(StringTypes.PERMISSION_NO, "You do not have permission to do this.");
		output_strings.put(StringTypes.TARGET_IS_SELF, "Can't dial back to your own gate (without a solar flare).");
		output_strings.put(StringTypes.TARGET_INVALID, "Invalid target to dial.");
		output_strings.put(StringTypes.TARGET_IS_ACTIVE, "Target gate is currently active.");
		output_strings.put(StringTypes.GATE_NOT_ACTIVE, "No gate activated to dial.");
		output_strings.put(StringTypes.GATE_REMOTE_ACTIVE, "Gate remotely activated.");
		output_strings.put(StringTypes.GATE_SHUTDOWN, "Gate successfully shutdown.");
		output_strings.put(StringTypes.GATE_ACTIVATED, "Gate successfully activated.");
		output_strings.put(StringTypes.GATE_DEACTIVATED, "Gate successfully deactivated.");
		output_strings.put(StringTypes.GATE_DIALED, "Gate successfully dialed.");
		output_strings.put(StringTypes.CONSTRUCT_SUCCESS, "Gate successfully constructed.");
		output_strings.put(StringTypes.CONSTRUCT_NAME_INVALID, "Gate name invalid.");
		output_strings.put(StringTypes.CONSTRUCT_NAME_TOO_LONG, "Gate name too long.");
		output_strings.put(StringTypes.REQUEST_INVALID, "Invalid Request.");
	}
	
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
	 */
	public static void setPortalMaterial(Material m)
	{
		ConfigManager.setConfigValue(ConfigKeys.PORTAL_MATERIAL, m);
	}
	
	/**
	 * Get Iris material setting from ConfigKeys. Return sane Material value.
	 * Return default value if key is missing or broken.
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
	 * Set timeout activate setting in ConfigKeys
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
	 */
	public static Material getStargateMaterial()
	{
		return Material.OBSIDIAN;
	}
	
	/**
	 * Placeholder:
	 * Get Stargate Material settings from ConfigKeys. Return sane Material value.
	 * Return default if setting is missing or broken.
	 */
	public static Material getActivateMaterial()
	{
		return Material.GLOWSTONE;
	}
	
	/*
	 * Get iConomy Op Excempt settings from ConfigKeys. Return sane boolean value.
	 * Return default value if key is missing or broken.
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
	
	/*
	 * Get iConomy wormhole use cost settings from ConfigKeys. Return sane integer value.
	 * Return default value if key is missing or broken.
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
	
	/*
	 * Get iConomy wormhole owner percent settings from ConfigKeys. Return sane double value.
	 * Return default value if key is missing or broken.
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
	
	/*
	 * Get iConomy wormhole build cost settings from ConfigKeys. Return sane int value.
	 * Return default value if key is missing or broken.
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
	
	/*
	 * Get Built in permissions enabled settings from ConfigKeys. Return sane boolean value.
	 * Return default value if key is missing or broken.
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
	
	/*
	 * Get Built in default permission level settings from ConfigKeys. Return sane PermissionLevel.
	 * Return default value if key is missing or broken.
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
	
	/*
	 * Get Log Level setting from ConfigKeys. Return sane Level value.
	 * Return default value if key is missing or broken.
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
	public enum StringTypes
	{
		PERMISSION_NO,
		TARGET_IS_SELF,
		TARGET_INVALID,
		TARGET_IS_ACTIVE,
		GATE_NOT_ACTIVE,
		GATE_REMOTE_ACTIVE,
		GATE_SHUTDOWN,
		GATE_ACTIVATED,
		GATE_DEACTIVATED,
		GATE_DIALED,
		CONSTRUCT_SUCCESS,
		CONSTRUCT_NAME_INVALID,
		CONSTRUCT_NAME_TOO_LONG,
		CONSTRUCT_NAME_TAKEN,
		REQUEST_INVALID
	}

	public enum ConfigKeys
	{
		BUILT_IN_PERMISSIONS_ENABLED,
		BUILT_IN_DEFAULT_PERMISSION_LEVEL,
		TIMEOUT_ACTIVATE,
		TIMEOUT_SHUTDOWN,
		PORTAL_MATERIAL,
		PORTAL_WOOSH,
		IRIS_MATERIAL,
		ICONOMY_WORMHOLE_USE_COST,
		ICONOMY_WORMHOLE_BUILD_COST,
		ICONOMY_OPS_EXEMPT, 
		ICONOMY_WORMHOLE_OWNER_PERCENT,
		LOG_LEVEL
	}
}