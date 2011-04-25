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
package com.wormhole_xtreme.wormhole;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;

/**
 * WormholeXTreme Block Listener.
 * 
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */
public class WormholeXTremeBlockListener extends BlockListener
{
    /**
     * Handle block break.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @param block
     *            the block
     * @return true, if successful
     */
    private static boolean handleBlockBreak(final Player player, final Stargate stargate, final Block block)
    {
        final boolean allowed = WXPermissions.checkWXPermissions(player, stargate, PermissionType.DAMAGE);
        if (allowed)
        {
            if ( !WorldUtils.isSameBlock(stargate.activationBlock, block))
            {
                if ((stargate.teleportSignBlock != null) && WorldUtils.isSameBlock(stargate.teleportSignBlock, block))
                {
                    player.sendMessage("Destroyed DHD Sign. You will be unable to change dialing target from this gate.");
                    player.sendMessage("You can rebuild it later.");
                    stargate.teleportSign = null;
                }
                else if (block.getType().equals(stargate.gateShape.irisMaterial))
                {
                    return true;
                }
                else
                {
                    if (stargate.active)
                    {
                        stargate.deActivateStargate();
                        stargate.fillGateInterior(Material.AIR);
                    }
                    if (stargate.litGate)
                    {
                        stargate.unLightStargate();
                        stargate.stopActivationTimer();
                        StargateManager.removeActivatedStargate(player);
                    }
                    stargate.resetTeleportSign();
                    stargate.setupGateSign(false);
                    if ( !stargate.irisDeactivationCode.equals(""))
                    {
                        stargate.setupIrisLever(false);
                    }
                    StargateManager.removeStargate(stargate);
                    player.sendMessage("Stargate Destroyed: " + stargate.name);
                }
            }
            else
            {
                player.sendMessage("Destroyed DHD. You will be unable to dial out from this gate.");
                player.sendMessage("You can rebuild it later.");
            }
            return false;
        }
        else
        {
            if (player != null)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied block destroy on: " + stargate.name);
            }
        }
        return true;
    }

    /**
     * Handle block damage.
     * 
     * @param player
     *            the player
     * @param stargate
     *            the stargate
     * @return true, if successful
     */
    public static boolean handleBlockDamage(final Player player, final Stargate stargate)
    {
        final boolean allowed = WXPermissions.checkWXPermissions(player, stargate, PermissionType.DAMAGE);
        if (allowed)
        {
            return false;
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied damage on: " + stargate.name);
            return true;
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockBreak(org.bukkit.event.block.BlockBreakEvent)
     */
    @Override
    public void onBlockBreak(final BlockBreakEvent event)
    {
        if ( !event.isCancelled())
        {
            final Block block = event.getBlock();
            final Stargate stargate = StargateManager.getGateFromBlock(block);
            final Player player = event.getPlayer();
            if ((stargate != null) && handleBlockBreak(player, stargate, block))
            {
                event.setCancelled(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockBurn(org.bukkit.event.block.BlockBurnEvent)
     */
    @Override
    public void onBlockBurn(final BlockBurnEvent event)
    {
        if ( !event.isCancelled())
        {
            //if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA))
            //{
            final Location current = event.getBlock().getLocation();
            final Stargate closest = StargateManager.findClosestStargate(current);
            //TODO This is bad, very bad for performance!
            if ((closest != null) && (closest.active || closest.recentActive) && (closest.gateShape.portalMaterial == Material.STATIONARY_LAVA))
            {
                final double blockDistanceSquared = StargateManager.distanceSquaredToClosestGateBlock(current, closest);
                if (((blockDistanceSquared <= closest.gateShape.wooshDepthSquared) && (closest.gateShape.wooshDepth != 0)) || (blockDistanceSquared <= 25))
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.name + "\" Proximity Block Burn Distance Squared: \"" + blockDistanceSquared + "\"");
                    event.setCancelled(true);
                }
            }
            //}
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockDamage(org.bukkit.event.block.BlockDamageEvent)
     */
    @Override
    public void onBlockDamage(final BlockDamageEvent event)
    {
        if ( !event.isCancelled())
        {
            final Block block = event.getBlock();
            final Stargate stargate = StargateManager.getGateFromBlock(block);
            final Player player = event.getPlayer();

            if ((stargate != null) && handleBlockDamage(player, stargate))
            {
                event.setCancelled(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockFlow(org.bukkit.event.block.BlockFromToEvent)
     */
    @Override
    public void onBlockFromTo(final BlockFromToEvent event)
    {
        if ( !event.isCancelled())
        {
            final Stargate stargate = StargateManager.getGateFromBlock(event.getToBlock());
            if ((stargate != null) || StargateManager.isBlockInGate(event.getBlock()))
            {
                event.setCancelled(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent)
     */
    @Override
    public void onBlockIgnite(final BlockIgniteEvent event)
    {
        if ( !event.isCancelled())
        {
            //if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA))
            //{
            final Location current = event.getBlock().getLocation();
            final Stargate closest = StargateManager.findClosestStargate(current);
            if ((closest != null) && (closest.active || closest.recentActive) && (closest.gateShape.portalMaterial == Material.STATIONARY_LAVA))
            {
                final double blockDistanceSquared = StargateManager.distanceSquaredToClosestGateBlock(current, closest);
                if (((blockDistanceSquared <= closest.gateShape.wooshDepthSquared) && (closest.gateShape.wooshDepth != 0)) || (blockDistanceSquared <= 25))
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.name + "\" Block Type: \"" + event.getBlock().getType().toString() + "\" Proximity Block Ignite: \"" + event.getCause().toString() + "\" Distance Squared: \"" + blockDistanceSquared + "\"");
                    event.setCancelled(true);
                }
            }
            //}
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent)
     */
    @Override
    public void onBlockPhysics(final BlockPhysicsEvent event)
    {
        if ( !event.isCancelled())
        {
            final Block block = event.getBlock();
            if (StargateManager.isBlockInGate(block))
            {
                event.setCancelled(true);
            }
        }
    }
}
