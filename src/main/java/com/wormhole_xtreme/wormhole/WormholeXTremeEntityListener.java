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

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;

/**
 * WormholeXtreme Entity Listener.
 * 
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */
class WormholeXTremeEntityListener extends EntityListener
{

    /**
     * Handle entity explode event.
     * 
     * @param explodeBlocks
     *            the explode blocks
     * @return true, if successful
     */
    private static boolean handleEntityExplodeEvent(final List<Block> explodeBlocks)
    {
        final List<Block> eb = explodeBlocks;
        for (int i = 0; i < eb.size(); i++)
        {
            if (StargateManager.isBlockInGate(eb.get(i)))
            {
                final Stargate s = StargateManager.getGateFromBlock(eb.get(i));
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Blocked Creeper Explosion on Stargate: \"" + s.getGateName() + "\"");
                return true;
            }
        }
        return false;
    }

    /**
     * Handle Player damage event.
     * 
     * @param event
     *            the event
     * @return true, if successful
     */
    private static boolean handlePlayerDamageEvent(final EntityDamageEvent event)
    {
        final Player p = (Player) event.getEntity();
        final Location current = p.getLocation();
        final Stargate closest = StargateManager.findClosestStargate(current);
        if ((closest != null) && (((closest.isGateCustom() ? closest.getGateCustomPortalMaterial()
            : closest.getGateShape() != null ? closest.getGateShape().getShapePortalMaterial()
                : Material.STATIONARY_WATER) == Material.STATIONARY_LAVA) || ((closest.getGateTarget() != null) && ((closest.getGateTarget().isGateCustom()
            ? closest.getGateTarget().getGateCustomPortalMaterial() : closest.getGateTarget().getGateShape() != null
                ? closest.getGateTarget().getGateShape().getShapePortalMaterial() : Material.STATIONARY_WATER) == Material.STATIONARY_LAVA))))
        {
            final double blockDistanceSquared = StargateManager.distanceSquaredToClosestGateBlock(current, closest);
            if ((closest.isGateActive() || closest.isGateRecentlyActive()) && (((blockDistanceSquared <= (closest.isGateCustom()
                ? closest.getGateCustomWooshDepthSquared() : closest.getGateShape() != null
                    ? closest.getGateShape().getShapeWooshDepthSquared() : 0)) && ((closest.isGateCustom()
                ? closest.getGateCustomWooshDepth() : closest.getGateShape() != null
                    ? closest.getGateShape().getShapeWooshDepth() : 0) != 0)) || (blockDistanceSquared <= 16)))
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.getGateName() + "\" Proximity Event: \"" + event.getCause().toString() + "\" On: \"" + p.getName() + "\" Distance Squared: \"" + blockDistanceSquared + "\"");
                p.setFireTicks(0);
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.entity.EntityListener#onEntityDamage(org.bukkit.event.entity.EntityDamageEvent)
     */
    @Override
    public void onEntityDamage(final EntityDamageEvent event)
    {
        if ( !event.isCancelled() && (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA)))
        {
            if (event.getEntity() instanceof Player)
            {
                if (handlePlayerDamageEvent(event))
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.entity.EntityListener#onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent)
     */
    @Override
    public void onEntityExplode(final EntityExplodeEvent event)
    {
        if ( !event.isCancelled())
        {
            final List<Block> explodeBlocks = event.blockList();
            if (handleEntityExplodeEvent(explodeBlocks))
            {
                event.setCancelled(true);
            }
        }
    }
}