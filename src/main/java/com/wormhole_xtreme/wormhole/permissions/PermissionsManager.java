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
package com.wormhole_xtreme.wormhole.permissions;

import java.util.concurrent.ConcurrentHashMap;


import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateDBManager;


/**
 * WormholeXtreme Built in Permissions Manager.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class PermissionsManager 
{

    /** The player_general_permission. */
    private static ConcurrentHashMap<String, PermissionLevel> player_general_permission = new ConcurrentHashMap<String, PermissionLevel>();

    //private static HashMap<String, PermissionLevel> group_general_permission = new HashMap<String, PermissionLevel>();
    //private static Object group_lock = new Object();

    /**
     * Gets the permission level.
     *
     * @param p the p
     * @param s the s
     * @return the permission level
     */
    public static PermissionLevel getPermissionLevel( Player p, Stargate s)
    {
        if ( !ConfigManager.getBuiltInPermissionsEnabled() )
        {
            return PermissionLevel.WORMHOLE_FULL_PERMISSION;
        }

        // 1. Check for individual network rights
        if ( s != null )
        {

        }
        // 2. Check for individual general rights
        PermissionLevel lvl =  getIndividualPermissionLevel(p.getName());
        if ( lvl != PermissionLevel.NO_PERMISSION_SET )
            return lvl;
        // 3. Check for group network rights
        if ( s != null )
        {

        }

        // 4. Check for group general rights

        // 5. Check for default network rights
        if ( s != null )
        {

        }

        // 5. Check for default general rights
        if ( p.isOp() )
        {
            return PermissionLevel.WORMHOLE_FULL_PERMISSION;
        }
        else
        {
            return ConfigManager.getBuiltInDefaultPermissionLevel();
        }
    }

    /**
     * Sets the individual permission level.
     *
     * @param player the player
     * @param lvl the lvl
     */
    public static void setIndividualPermissionLevel( String player, PermissionLevel lvl )
    {
        String pl_lower = player.toLowerCase();
        player_general_permission.put(pl_lower, lvl);
        StargateDBManager.storeIndividualPermissionInDB(pl_lower, lvl);
    }

    /**
     * Gets the individual permission level.
     *
     * @param player the player
     * @return the permission level
     */
    public static PermissionLevel getIndividualPermissionLevel( String player )
    {
        String pl_lower = player.toLowerCase();
        if (player_general_permission.containsKey(pl_lower))
            return player_general_permission.get(pl_lower);
        else
            return PermissionLevel.NO_PERMISSION_SET;
    }

    /**
     * Load permissions.
     */
    public static void loadPermissions()
    {
        player_general_permission = StargateDBManager.getAllIndividualPermissions();
        // StargateDBManager.GetAllGroupPermissions();
    }

    /**
     * The Enum PermissionLevel.
     */
    public enum PermissionLevel
    {

        /** The N o_ permissio n_ set. */
        NO_PERMISSION_SET,

        /** The WORMHOL e_ ful l_ permission. */
        WORMHOLE_FULL_PERMISSION,

        /** The WORMHOL e_ creat e_ permission. */
        WORMHOLE_CREATE_PERMISSION,

        /** The WORMHOL e_ us e_ permission. */
        WORMHOLE_USE_PERMISSION,

        /** The WORMHOL e_ n o_ permission. */
        WORMHOLE_NO_PERMISSION
    }
    // 0         1     2        3
    // 0         1     2        3
    // /stargate perms indiv    <USERNAME>     <OPTIONAL_SET> (else its a get)
    // /stargate perms group    <GROUPNAME>    <OPTIONAL_SET> (else its a get)
    // /stargate perms default <OPTIONAL_SET> (else a get)
    // /stargate perms active  <OPTIONAL_SET> (else a get)
    /**
     * Handle permission request.
     *
     * @param p the p
     * @param message_parts the message_parts
     */
    public static void handlePermissionRequest(Player p, String[] message_parts) 
    {
        if ( p.isOp() )
        {
            if ( message_parts.length > 2 )
            {
                if ( message_parts[2].equalsIgnoreCase("active") )
                {
                    if ( message_parts.length == 4)
                    {
                        try
                        {
                            boolean active = Boolean.parseBoolean(message_parts[3]);
                            ConfigManager.setConfigValue(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, active);
                        }
                        catch ( Exception e)
                        {
                            p.sendMessage("Invalid format - only true and false allowed.");
                        }
                    }
                    p.sendMessage("Permissions active is: " + ConfigManager.getBuiltInPermissionsEnabled() );
                }
                else if ( message_parts[2].equalsIgnoreCase("indiv") )
                {
                    if ( message_parts.length == 5)
                    {
                        try
                        {
                            PermissionsManager.setIndividualPermissionLevel(message_parts[3].toLowerCase(), PermissionsManager.PermissionLevel.valueOf(message_parts[4]));
                        }
                        catch ( Exception e)
                        {
                            p.sendMessage("Invalid format - /wormhole perms indiv <username> <perm>.");
                            p.sendMessage("Valid Permission Levels: ");
                            for( PermissionsManager.PermissionLevel level : PermissionsManager.PermissionLevel.values() )
                                p.sendMessage(" " + level.toString());
                        }
                    }

                    p.sendMessage("Permissions for " + message_parts[3] + ": " + PermissionsManager.getIndividualPermissionLevel(message_parts[3].toLowerCase()));
                }	
                else if ( message_parts[2].equalsIgnoreCase("default") )
                {
                    if ( message_parts.length == 4)
                    {
                        try
                        {
                            ConfigManager.setConfigValue(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, PermissionLevel.valueOf(message_parts[3]) );
                            p.sendMessage("Default Permission is now: " + ConfigManager.getBuiltInDefaultPermissionLevel());
                        }
                        catch ( Exception e)
                        {
                            p.sendMessage("Invalid format - /wormhole perms default <perm>");
                            p.sendMessage("Valid Permission Levels: ");
                            for( PermissionsManager.PermissionLevel level : PermissionsManager.PermissionLevel.values() )
                                p.sendMessage(" " + level.toString());
                        }
                    }
                }
            }
            else
            {
                // /stargate perms indiv    <USERNAME>     <OPTIONAL_SET> (else its a get)
                // /stargate perms group    <GROUPNAME>    <OPTIONAL_SET> (else its a get)
                // /stargate perms default <OPTIONAL_SET> (else a get)
                // /stargate perms active  <OPTIONAL_SET> (else a get)
                p.sendMessage("/wormhole perms indiv    <USERNAME>     <OPTIONAL_SET>");
                //p.sendMessage("/stargate perms indiv    <USERNAME>     <OPTIONAL_SET>");
                p.sendMessage("/wormhole perms default <OPTIONAL_SET>");
                p.sendMessage("/wormhole perms active  <OPTIONAL_SET> (else a get)");
            }
        }
        else
            p.sendMessage("Unable to set permissions unless you are OP. Try \"op <name>\"");
    }
}
