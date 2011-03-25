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
        
        /** The USE. */
        USE
    }
    
    /**
     * Check wx permissions.
     *
     * @param player the player
     * @param stargate the stargate
     * @param permissiontype the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(Player player, Stargate stargate, PermissionType permissiontype)
    {
        if (player.isOp())
        {
            return true;
        }
        else if (WormholeXTreme.permissions != null)
        {
            if (permissiontype == PermissionType.DAMAGE)
            {
                return checkRemovePermission(player,stargate);
            }
            else if (permissiontype == PermissionType.SIGN)
            {
                return checkSignPermission(player,stargate);
            }
            else if (permissiontype == PermissionType.BUILD)
            {
                return checkBuildPermission(player,stargate);
            }
            else if (permissiontype == PermissionType.DIALER)
            {
                return checkDialerPermission(player,stargate);
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
            if (permissiontype == PermissionType.DAMAGE)
            {
                return checkFullPermissionBuiltIn(player,stargate);
            }
            else if (permissiontype == PermissionType.SIGN || permissiontype == PermissionType.DIALER || permissiontype == PermissionType.USE )
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
            return false;
        }
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
            return false;
        }
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
            return false;
        }
    }
    
    /**
     * Check build permission.
     *
     * @param player the player
     * @param stargate the stargate
     * @return true, if successful
     */
    private static boolean checkBuildPermission(Player player, Stargate stargate)
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
        if (PermissionsManager.getPermissionLevel(player, stargate) == PermissionLevel.WORMHOLE_FULL_PERMISSION)
        {
            return true;
        }
        else
        {
            return false;
        }
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
        PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
        if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
        {
            return true;
        }
        else
        {
            return false;
        }
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
        PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
        if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    

}

