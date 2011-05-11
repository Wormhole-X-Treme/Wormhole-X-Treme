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

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable.ActionToTake;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;

/**
 * The Class StargateRestrictions.
 * 
 * @author alron
 */
public class StargateRestrictions
{

    /**
     * The Enum RestrictionGroup.
     */
    private static enum RestrictionGroup
    {

        /** The cooldown group 1 */
        CD_GROUP_ONE(ConfigManager.getUseCooldownGroupOne()),

        /** The cooldown group 2 */
        CD_GROUP_TWO(ConfigManager.getUseCooldownGroupTwo()),

        /** The cooldown group 3 */
        CD_GROUP_THREE(ConfigManager.getUseCooldownGroupThree()),
        BR_GROUP_ONE(ConfigManager.getBuildRestrictionGroupOne()),
        BR_GROUP_TWO(ConfigManager.getBuildRestrictionGroupTwo()),
        BR_GROUP_THREE(ConfigManager.getBuildRestrictionGroupThree());

        /** The restriction group node. */
        private final long restrictionGroupNode;

        /**
         * Instantiates a new restriction group.
         * 
         * @param restrictionGroupNode
         *            the restriction group node
         */
        private RestrictionGroup(final long restrictionGroupNode)
        {
            this.restrictionGroupNode = restrictionGroupNode;
        }

        /**
         * Gets the group value.
         * 
         * @return the group value
         */
        public long getGroupValue()
        {
            return restrictionGroupNode;
        }
    }

    /** The Constant playerUseCooldownStart. */
    private static final ConcurrentHashMap<Player, Long> playerUseCooldownStart = new ConcurrentHashMap<Player, Long>();

    /** The Constant playerUseCooldownGroup. */
    private static final ConcurrentHashMap<Player, RestrictionGroup> playerUseCooldownGroup = new ConcurrentHashMap<Player, RestrictionGroup>();

    /**
     * Adds the player use cooldown.
     * 
     * @param player
     *            the player
     */
    public static void addPlayerUseCooldown(final Player player)
    {
        RestrictionGroup cooldownGroup = null;
        if (WXPermissions.checkWXPermissions(player, PermissionType.USE_COOLDOWN_GROUP_ONE))
        {
            cooldownGroup = RestrictionGroup.CD_GROUP_ONE;
        }
        else if (WXPermissions.checkWXPermissions(player, PermissionType.USE_COOLDOWN_GROUP_TWO))
        {
            cooldownGroup = RestrictionGroup.CD_GROUP_TWO;
        }
        else if (WXPermissions.checkWXPermissions(player, PermissionType.USE_COOLDOWN_GROUP_THREE))
        {
            cooldownGroup = RestrictionGroup.CD_GROUP_THREE;
        }
        if (cooldownGroup != null)
        {
            getPlayerUseCooldownStart().put(player, System.nanoTime());
            getPlayerUseCooldownGroup().put(player, cooldownGroup);
            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(player, ActionToTake.COOLDOWN_REMOVE), cooldownGroup.getGroupValue() * 20);
        }
    }

    /**
     * Check player use cooldown remaining.
     * 
     * @param player
     *            the player
     * @return the int
     */
    public static long checkPlayerUseCooldownRemaining(final Player player)
    {
        if (getPlayerUseCooldownStart().containsKey(player) && getPlayerUseCooldownGroup().containsKey(player))
        {
            final long startTime = getPlayerUseCooldownStart().get(player);
            final long currentTime = System.nanoTime();
            final long elapsedTime = (currentTime - startTime) / 1000000000;
            return (getPlayerUseCooldownGroup().get(player).getGroupValue() >= elapsedTime)
                ? getPlayerUseCooldownGroup().get(player).getGroupValue() - elapsedTime
                : removePlayerUseCooldown(player);
        }
        return -1;
    }

    /**
     * Gets the player use cooldown group.
     * 
     * @return the player use cooldown group
     */
    private static ConcurrentHashMap<Player, RestrictionGroup> getPlayerUseCooldownGroup()
    {
        return playerUseCooldownGroup;
    }

    /**
     * Gets the player use cooldown list.
     * 
     * @return the player use cooldown list
     */
    private static ConcurrentHashMap<Player, Long> getPlayerUseCooldownStart()
    {
        return playerUseCooldownStart;
    }

    /**
     * Checks if is player build restricted.
     * 
     * @param player
     *            the player
     * @return true, if is player build restricted
     */
    public static boolean isPlayerBuildRestricted(final Player player)
    {
        if (ConfigManager.isBuildRestrictionEnabled())
        {
            RestrictionGroup restrictionGroup = null;
            if (WXPermissions.checkWXPermissions(player, PermissionType.BUILD_RESTRICTION_GROUP_ONE))
            {
                restrictionGroup = RestrictionGroup.BR_GROUP_ONE;
            }
            else if (WXPermissions.checkWXPermissions(player, PermissionType.BUILD_RESTRICTION_GROUP_TWO))
            {
                restrictionGroup = RestrictionGroup.BR_GROUP_TWO;
            }
            else if (WXPermissions.checkWXPermissions(player, PermissionType.BUILD_RESTRICTION_GROUP_THREE))
            {
                restrictionGroup = RestrictionGroup.BR_GROUP_THREE;
            }
            int gateCount = 0;
            for (final Stargate stargate : StargateManager.getAllGates())
            {
                if ((stargate.getGateOwner() != null) && stargate.getGateOwner().equalsIgnoreCase(player.getName()))
                {
                    gateCount++;
                }
            }
            return (restrictionGroup != null) && (gateCount != 0) && (gateCount >= restrictionGroup.getGroupValue());
        }
        return false;
    }

    /**
     * Checks if is player use cooldown.
     * 
     * @param player
     *            the player
     * @return true, if is player use cooldown
     */
    public static boolean isPlayerUseCooldown(final Player player)
    {
        return (getPlayerUseCooldownStart().containsKey(player) && getPlayerUseCooldownGroup().containsKey(player));
    }

    /**
     * Removes the player use cooldown.
     * 
     * @param player
     *            the player
     */
    public static int removePlayerUseCooldown(final Player player)
    {
        if (getPlayerUseCooldownStart().containsKey(player))
        {
            getPlayerUseCooldownStart().remove(player);
        }
        if (getPlayerUseCooldownGroup().containsKey(player))
        {
            getPlayerUseCooldownGroup().remove(player);
        }
        return 0;
    }
}
