package com.wormhole_xtreme.config;

import com.wormhole_xtreme.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;



/*
 * This class is based on a class "SettingsList.java" 
 * from MinecartMania written by Afforess from Bukkit.org
 */
public class DefaultSettings 
{
	final static Setting[] config = {
		new Setting(
				ConfigKeys.TIMEOUT_ACTIVATE, 
				30, 
				"Number of seconds after a gate is activated, but before dialing before timing out.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.TIMEOUT_SHUTDOWN, 
				38, 
				"Number of seconds after a gate is dialed before automatically shutdown. With 0 timeout a gate won't shutdown until something goes through the gate.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.PORTAL_MATERIAL, 
				"STATIONARY_WATER", 
				"Material to be used when a gate is active. Values are 'STATIONARY_WATER', 'STATIONARY_LAVA', 'AIR', 'PORTAL'.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, 
				false, 
				"This should be set to true if you want the built in permissions enabled. This setting does nothing if you have Permissions plugin installed.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL, 
				PermissionLevel.WORMHOLE_USE_PERMISSION, 
				"If built in permissions are being used, this is the default level of control users (non-ops) have.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.ICONOMY_OPS_EXEMPT, 
				true, 
				"Server Ops will not be charged to use or build gates.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.ICONOMY_WORMHOLE_BUILD_COST, 
				0, 
				"Cost for users to build a wormhole.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.ICONOMY_WORMHOLE_USE_COST, 
				0, 
				"Cost for users to use a wormhole (charged on teleport, not activation.)",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.ICONOMY_WORMHOLE_OWNER_PERCENT, 
				0.0, 
				"Percent ( from 0.0 - 1.0 ) of wormhole use cost the builder of a gate is given.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.PORTAL_WOOSH, 
				true, 
				"On activate of portal should the 'woosh' animation be played.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.IRIS_MATERIAL,
				"STONE", 
				"Material to be used for the gate Iris. Values are 'IRON_BLOCK', 'STONE', 'BEDROCK', 'GLASS', and 'DIAMOND_BLOCK'.",
				"WormholeXTreme"
		),
		new Setting(
				ConfigKeys.LOG_LEVEL,
				"INFO",
				"Log level to use for minecraft logging purposes. Values are SEVERE, WARNING, INFO, CONFIG, FINE, FINER, and FINEST. In order of least to most logging output.",
				"WormholeXTreme"
		)
	};
	
}
