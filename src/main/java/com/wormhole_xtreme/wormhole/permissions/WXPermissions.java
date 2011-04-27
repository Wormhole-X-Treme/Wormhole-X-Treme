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
package com.wormhole_xtreme.wormhole.permissions;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager.PermissionLevel;

/**
 * The Class WXPermissions.
 * 
 * @author alron
 */
public class WXPermissions
{

    /**
     * The Enum PermissionType.
     */
    public static enum PermissionType
    {

        /** The DAMAGE permission. */
        DAMAGE,

        /** The SIGN permission. */
        SIGN,

        /** The DIALER permission. */
        DIALER,

        /** The BUILD permission. */
        BUILD,

        /** The REMOVE permission. */
        REMOVE,

        /** The USE permission. */
        USE,

        /** The LIST permission. */
        LIST,

        /** The CONFIG permission. */
        CONFIG,

        /** The GO permission. */
        GO,

        /** The COMPASS permission. */
        COMPASS
    }

    /**
     * Check build permission.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @param network
     *            the network
     * @return true, if successful
     */
    private static boolean checkBuildPermission(final Player player, final Stargate stargate, final String network)
    {
        if ((stargate != null) || (network != null))
        {
            String gatenet;
            if (stargate != null)
            {
                if (stargate.getGateNetwork() != null)
                {
                    gatenet = stargate.getGateNetwork().getNetworkName();
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
            if (permSimpleBuild(player) || (permComplexBuild(player) && (gatenet.equals("Public") || ( !gatenet.equals("Public") && permComplexNetworkBuild(player, gatenet)))))
            {
                if (stargate != null)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed build permission on: " + stargate.getGateName());
                }
                else
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed build permission.");
                }
                return true;
            }
            else
            {
                if (stargate != null)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied config permission on: " + stargate.getGateName());
                }
                else
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied config permission.");
                }
            }
        }
        return false;
    }

    /**
     * Check compass permission.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean checkCompassPermission(final Player player)
    {
        if (permSimpleUse(player) || permComplexCompass(player))
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed compass permission.");
            return true;
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied compass permission.");
            return false;
        }
    }

    /**
     * Check config permission.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean checkConfigPermission(final Player player)
    {
        if (permSimpleConfig(player) || permComplexConfig(player))
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed config permission.");
            return true;
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied config permission.");
            return false;
        }
    }

    /**
     * Check dialer permission.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    private static boolean checkDialerPermission(final Player player, final Stargate stargate)
    {
        if (stargate != null)
        {
            String gatenet;
            if (stargate.getGateNetwork() != null)
            {
                gatenet = stargate.getGateNetwork().getNetworkName();
            }
            else
            {
                gatenet = "Public";
            }
            if (permSimpleUse(player) || (permComplexUseDialer(player) && (gatenet.equals("Public") || ( !gatenet.equals("Public") && permComplexNetworkUse(player, gatenet)))))
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed dialer permission on: " + stargate.getGateName());
                return true;
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied dialer permission on: " + stargate.getGateName());
            }
        }
        return false;
    }

    /**
     * Check go permission.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean checkGoPermission(final Player player)
    {
        if (permSimpleConfig(player) || permComplexGo(player))
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed go permission.");
            return true;
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied go permission.");
            return false;
        }
    }

    /**
     * Check list permission.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean checkListPermission(final Player player)
    {
        if (permSimpleConfig(player) || permSimpleUse(player) || permComplexConfig(player) || permComplexList(player))
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed list permission.");
            return true;
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied list permission.");
            return false;
        }
    }

    /**
     * Check remove permission.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    private static boolean checkRemovePermission(final Player player, final Stargate stargate)
    {
        if (stargate != null)
        {
            if (permComplexRemoveAll(player) || ((stargate.getGateOwner() != null) && stargate.getGateOwner().equals(player.getName()) && permComplexRemoveOwn(player)) || permSimpleRemove(player))
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed Remove on: " + stargate.getGateName());
                return true;
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied Remove on: " + stargate.getGateName());
            }
        }
        return false;
    }

    /**
     * Check sign permission.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    private static boolean checkSignPermission(final Player player, final Stargate stargate)
    {
        if (stargate != null)
        {
            String gatenet;
            if (stargate.getGateNetwork() != null)
            {
                gatenet = stargate.getGateNetwork().getNetworkName();
            }
            else
            {
                gatenet = "Public";
            }
            if (permSimpleUse(player) || (permComplexUseSign(player) && (gatenet.equals("Public") || ( !gatenet.equals("Public") && permComplexNetworkUse(player, gatenet)))))
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " allowed sign permission on: " + stargate.getGateName());
                return true;
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied sign permission on: " + stargate.getGateName());
            }
        }
        return false;
    }

    /**
     * Check wx permissions.
     * 
     * @param player
     *            the player
     * @param permissiontype
     *            the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(final Player player, final PermissionType permissiontype)
    {
        return checkWXPermissions(player, null, null, permissiontype);
    }

    /**
     * Check wx permisssions.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @param permissionstype
     *            the permissionstype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(final Player player, final Stargate stargate, final PermissionType permissionstype)
    {
        return checkWXPermissions(player, stargate, null, permissionstype);
    }

    /**
     * Check wx permissions.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @param network
     *            the network
     * @param permissiontype
     *            the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(final Player player, final Stargate stargate, final String network, final PermissionType permissiontype)
    {
        if (player == null)
        {
            return false;
        }
        if (player.isOp())
        {
            return true;
        }
        else if (WormholeXTreme.getPermissions() != null)
        {
            if ((permissiontype == PermissionType.DAMAGE) || (permissiontype == PermissionType.REMOVE))
            {
                if (checkRemovePermission(player, stargate) || checkConfigPermission(player))
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
                return checkSignPermission(player, stargate);
            }
            else if (permissiontype == PermissionType.BUILD)
            {
                return checkBuildPermission(player, stargate, network);
            }
            else if (permissiontype == PermissionType.DIALER)
            {
                return checkDialerPermission(player, stargate);
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
                if (checkDialerPermission(player, stargate) || checkSignPermission(player, stargate))
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
            if ((permissiontype == PermissionType.DAMAGE) || (permissiontype == PermissionType.REMOVE) || (permissiontype == PermissionType.CONFIG) || (permissiontype == PermissionType.GO))
            {
                return permBuiltInCheckFull(player, stargate);
            }
            else if ((permissiontype == PermissionType.SIGN) || (permissiontype == PermissionType.DIALER) || (permissiontype == PermissionType.USE) || (permissiontype == PermissionType.LIST) || (permissiontype == PermissionType.COMPASS))
            {
                return permBuiltInCheckAny(player, stargate);
            }
            else if (permissiontype == PermissionType.BUILD)
            {
                return permBuiltInCheckBuild(player, stargate);
            }
        }
        return false;
    }

    /**
     * Check wx permissions.
     * 
     * @param player
     *            the player
     * @param network
     *            the network
     * @param permissiontype
     *            the permissiontype
     * @return true, if successful
     */
    public static boolean checkWXPermissions(final Player player, final String network, final PermissionType permissiontype)
    {
        return checkWXPermissions(player, null, network, permissiontype);
    }

    /**
     * Check any permission built in.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    private static boolean permBuiltInCheckAny(final Player player, final Stargate stargate)
    {
        if (stargate != null)
        {
            final PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
            if (((lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION) || (lvl == PermissionLevel.WORMHOLE_USE_PERMISSION) || (lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check build permission built in.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    private static boolean permBuiltInCheckBuild(final Player player, final Stargate stargate)
    {
        if (stargate != null)
        {
            final PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
            if (((lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION) || (lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check full permission built in.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    private static boolean permBuiltInCheckFull(final Player player, final Stargate stargate)
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
     * Perm complex build.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexBuild(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.build"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex compass.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexCompass(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.use.compass"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex config.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexConfig(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.config"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex go.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexGo(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.go"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex list.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexList(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.list"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex network build.
     * 
     * @param player
     *            the player
     * @param network
     *            the network
     * @return true, if successful
     */
    private static boolean permComplexNetworkBuild(final Player player, final String network)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.network.build." + network))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex network use.
     * 
     * @param player
     *            the player
     * @param network
     *            the network
     * @return true, if successful
     */
    private static boolean permComplexNetworkUse(final Player player, final String network)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.network.use." + network))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex remove all.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexRemoveAll(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.remove.all"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex remove own.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexRemoveOwn(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.remove.own"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex use dialer.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexUseDialer(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.use.dialer"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm complex use sign.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permComplexUseSign(final Player player)
    {
        if ( !ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.use.sign"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm simple build.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permSimpleBuild(final Player player)
    {
        if (ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.simple.build"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm simple config.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permSimpleConfig(final Player player)
    {
        if (ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.simple.config"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm simple remove.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permSimpleRemove(final Player player)
    {
        if (ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.simple.remove"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Perm simple use.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean permSimpleUse(final Player player)
    {
        if (ConfigManager.getSimplePermissions() && WormholeXTreme.getPermissions().has(player, "wormhole.simple.use"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
