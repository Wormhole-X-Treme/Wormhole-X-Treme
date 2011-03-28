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
package com.wormhole_xtreme.permissions;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;


// TODO: Auto-generated Javadoc
/**
 * The Class WXPermissions.
 *
 * @author alron
 */
public class WXPermissions {
    
    /**
     * The Enum PermissionType.
     */
    public static enum PermissionType
    {
        
        /** The DAMAGE. */
        DAMAGE,
        
        /** The SIGN. */
        SIGN,
        
        /** The DIALER. */
        DIALER,
        
        /** The BUILD. */
        BUILD,
        
        /** The REMOVE. */
        REMOVE,
        
        /** The USE. */
        USE,
        
        /** The LIST. */
        LIST,
        
        /** The CONFIG. */
        CONFIG,
        
        /** The GO. */
        GO,
        
        /** The COMPASS. */
        COMPASS
    }
    
    /**
     * Check wx permissions.
     *
     * @param player the player
     * @param permissiontype the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(Player player, PermissionType permissiontype)
    {
        return checkWXPermissions(player,null,null,permissiontype);
    }
    
    /**
     * Check wx permissions.
     *
     * @param player the player
     * @param network the network
     * @param permissiontype the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(Player player, String network, PermissionType permissiontype)
    {
        return checkWXPermissions(player,null,network,permissiontype);
    }
    
    /**
     * Check wx permisssions.
     *
     * @param player the player
     * @param stargate the stargate
     * @param permissionstype the permissionstype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(Player player, Stargate stargate, PermissionType permissionstype)
    {
        return checkWXPermissions(player,stargate,null,permissionstype);
    }
    
    /**
     * Check wx permissions.
     *
     * @param player the player
     * @param stargate the stargate
     * @param network the network
     * @param permissiontype the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(Player player, Stargate stargate, String network, PermissionType permissiontype)
    {
        if (player == null)
        {
            return false;
        }
        if (player.isOp())
        {
            return true;
        }
        else if (WormholeXTreme.permissions != null)
        {
            if (permissiontype == PermissionType.DAMAGE || permissiontype == PermissionType.REMOVE )
            {
                if (checkRemovePermission(player,stargate) || checkConfigPermission(player))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (permissiontype == PermissionType.SIGN)
            {
                return checkSignPermission(player,stargate);
            }
            else if (permissiontype == PermissionType.BUILD)
            {
                return checkBuildPermission(player,stargate,network);
            }
            else if (permissiontype == PermissionType.DIALER)
            {
                return checkDialerPermission(player,stargate);
            }
            else if (permissiontype == PermissionType.LIST)
            {
                return checkListPermission(player);
            }
            else if (permissiontype == PermissionType.CONFIG)
            {
                return checkConfigPermission(player);
            }
            else if (permissiontype == PermissionType.GO)
            {
                return checkGoPermission(player);
            }
            else if (permissiontype == PermissionType.COMPASS)
            {
                return checkCompassPermission(player);
            }
            else if (permissiontype == PermissionType.USE)
            {
                if (checkDialerPermission(player,stargate) || checkSignPermission(player,stargate))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            if (permissiontype == PermissionType.DAMAGE || permissiontype == PermissionType.REMOVE || permissiontype == PermissionType.CONFIG ||
                permissiontype == PermissionType.GO )
            {
                return checkFullPermissionBuiltIn(player,stargate);
            }
            else if (permissiontype == PermissionType.SIGN || permissiontype == PermissionType.DIALER || permissiontype == PermissionType.USE || 
                permissiontype == PermissionType.LIST || permissiontype == PermissionType.COMPASS )
            {
                return checkAnyPermissionBuiltIn(player,stargate);
            }
            else if (permissiontype == PermissionType.BUILD)
            {
                return checkBuildPermissionBuiltIn(player,stargate);
            }
        }
        return false;
    }

    /**
     * Check remove permission.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkRemovePermission(Player player, Stargate stargate)
    {
        if (stargate != null)
        {
            if (!ConfigManager.getSimplePermissions() && (WormholeXTreme.permissions.has(player, "wormhole.remove.all") ||
                (stargate.Owner != null && stargate.Owner.equals(player.getName()) && WormholeXTreme.permissions.has(player, "wormhole.remove.own") )))
            {
                
                return true;
            }
            else if (ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.simple.remove"))
            {
                return true;
            }
            else
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied Remove on: " + stargate.Name);
            }
        }
        return false;
    }

    /**
     * Check sign permission.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkSignPermission(Player player, Stargate stargate)
    {
        if (stargate != null)
        {
            String gatenet;
            if (stargate.Network != null )
            {
                gatenet = stargate.Network.netName;
            }
            else
            {
                gatenet = "Public";
            }
            if ( ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.simple.use"))
            {
                return true;
            }
            else if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.use.sign") && (gatenet.equals("Public") ||
                (!gatenet.equals("Public") && WormholeXTreme.permissions.has(player, "wormhole.network.use." + gatenet))))
            {
                return true;
            }
            else
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied sign permission on: " + stargate.Name );
            }
        }
        return false;
    }
    
    /**
     * Check dialer permission.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkDialerPermission(Player player, Stargate stargate)
    {
        if (stargate != null)
        {
            String gatenet;
            if (stargate.Network != null )
            {
                gatenet = stargate.Network.netName;
            }
            else
            {
                gatenet = "Public";
            }
            if ( ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.simple.use"))
            {
                return true;
            }
            else if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.use.dialer") && (gatenet.equals("Public") ||
                (!gatenet.equals("Public") && WormholeXTreme.permissions.has(player, "wormhole.network.use." + gatenet))))
            {
                return true;
            }
            else
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied dialer permission on: " + stargate.Name );
            }
        }
        return false;
    }
    
    /**
     * Check build permission.
     *
     * @param player the player
     * @param stargate the stargate
     * @param network the network
     * @return true, if successful
     */
    private static boolean checkBuildPermission(Player player, Stargate stargate, String network)
    {
        if (stargate != null || network != null)
        {
            String gatenet;
            if (stargate != null)
            {
                if (stargate.Network != null )
                {
                    gatenet = stargate.Network.netName;
                }
                else
                {
                    gatenet = "Public";
                }
            }
            else
            {
                if (network != null)
                {
                    gatenet = network;
                }
                else
                {
                    gatenet = "Public";
                }
            }
            if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.build") && (gatenet.equals("Public") ||
                (!gatenet.equals("Public") && WormholeXTreme.permissions.has(player, "wormhole.network.build." + gatenet))))
            {
                return true;
            }
            else if ( ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.simple.build"))
            {
                return true;
            }
            else
            {
                if (stargate != null)
                {
                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied config permission on: " + stargate.Name);
                }
                else
                {
                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied config permission.");
                }
            }
        }
        return false;
    }
    
    /**
     * Check config permission.
     *
     * @param player the player
     * @return true, if successful
     */
    private static boolean checkConfigPermission(Player player)
    {
        if ((ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.simple.config")) ||
            (!ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.config")))
        {
            return true;
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied config permission.");
            return false;
        }
    }
    
    /**
     * Check list permission.
     *
     * @param player the player
     * @return true, if successful
     */
    private static boolean checkListPermission(Player player)
    {
        if (ConfigManager.getSimplePermissions() && (WormholeXTreme.permissions.has(player, "wormhole.simple.config") || WormholeXTreme.permissions.has(player, "wormhole.simple.use"))) 
        {
            return true;
        }
        else if (!ConfigManager.getSimplePermissions() && (WormholeXTreme.permissions.has(player, "wormhole.config")) || (WormholeXTreme.permissions.has(player, "wormhole.list")))
        {
            return true;
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied list permission.");
            return false;
        }
    }
    
    /**
     * Check compass permission.
     *
     * @param player the player
     * @return true, if successful
     */
    private static boolean checkCompassPermission(Player player)
    {
        if ((ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.use")) ||
            (!ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.use.compass")))
        {
            return true;
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied compass permission.");
            return false;
        }
    }
    /**
     * Check go permission.
     *
     * @param player the player
     * @return true, if successful
     */
    private static boolean checkGoPermission(Player player)
    {
        if ((!ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.go")) || 
            (ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.config")))
        {
            return true;
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied go permission.");
            return false;
        }
    }
    /**
     * Check full permission built in.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkFullPermissionBuiltIn(Player player, Stargate stargate)
    {
        if (stargate != null)
        {
            if (PermissionsManager.getPermissionLevel(player, stargate) == PermissionLevel.WORMHOLE_FULL_PERMISSION)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check build permission built in.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkBuildPermissionBuiltIn(Player player, Stargate stargate)
    {
        if (stargate != null)
        {
            PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
            if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
            {
                return true;
            }
        }
        return false;
    }   
    
    /**
     * Check any permission built in.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkAnyPermissionBuiltIn(Player player, Stargate stargate)
    {
        if (stargate != null)
        {
            PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
            if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
            {
                return true;
            }
        }
        return false;
    }
    

}

