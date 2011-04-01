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

import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;


 
// TODO: Auto-generated Javadoc
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
	 * @param player the player
	 * @param stargate the stargate
	 * @param block the block
	 * @return true, if successful
	 */
	private static boolean handleBlockBreak(Player player, Stargate stargate, Block block)
	{
	    final Stargate s = stargate;
	    final Player p = player;
	    final Block b = block;
	    final boolean allowed = WXPermissions.checkWXPermissions(p, s, PermissionType.DAMAGE);
	    if (allowed)
	    {
	        if ( !WorldUtils.isSameBlock(s.ActivationBlock, b) )
	        {
	            if ( s.TeleportSignBlock != null && WorldUtils.isSameBlock(s.TeleportSignBlock, b) )
	            {
	                p.sendMessage("Destroyed DHD Sign. You will be unable to change dialing target from this gate.");
	                p.sendMessage("You can rebuild it later.");
	                s.TeleportSign = null;
	            } 
	            else if (b.getType().equals(ConfigManager.getIrisMaterial()))
	            {
	                return true;
	            } 
	            else
	            {
	                if (s.Active) 
	                {
	                    s.DeActivateStargate();
	                    s.FillGateInterior(Material.AIR);
	                }
	                if (s.LitGate) 
	                {
	                    s.UnLightStargate();
	                    s.StopActivationTimer(p);
	                    StargateManager.RemoveActivatedStargate(p);
	                }
	                s.ResetTeleportSign();
	                s.SetupGateSign(false);
	                if (!s.IrisDeactivationCode.equals(""))
	                {
	                    s.SetupIrisLever(false);
	                }
	                StargateManager.RemoveStargate(stargate);
	                p.sendMessage("Stargate Destroyed: " + s.Name);
	            }
	        }
	        else
	        {
	            p.sendMessage("Destroyed DHD. You will be unable to dial out from this gate.");
	            p.sendMessage("You can rebuild it later.");
	        }
	        return false;
	    }
	    else
	    {
	        if (p != null)
	        {
	            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + p.getName() + " denied block destroy on: " + s.Name);
	        }
	    }
	    return true;
	}
	
	/**
	 * Handle block damage.
	 *
	 * @param player the player
	 * @param stargate the stargate
	 * @return true, if successful
	 */
	public static boolean handleBlockDamage(Player player, Stargate stargate)
	{
	    final Stargate s = stargate;
	    final Player p = player;
	    final boolean allowed = WXPermissions.checkWXPermissions(p, s, PermissionType.DAMAGE );
	    if (allowed)
	    {
	        return false;
	    }
	    else
	    {
	        WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + p.getName() + " denied damage on: " + s.Name);
	        return true;
	    }
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockBreak(org.bukkit.event.block.BlockBreakEvent)
	 */
	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
	    if (!event.isCancelled())
	    {
	        final Block block = event.getBlock();
	        final Stargate stargate = StargateManager.getGateFromBlock(block);
	        final Player player = event.getPlayer();
	        if (stargate != null && handleBlockBreak(player,stargate,block))
	        {
	            event.setCancelled(true);
	        }
	    }
	}
	
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockBurn(org.bukkit.event.block.BlockBurnEvent)
	 */
	@Override
	public void onBlockBurn(BlockBurnEvent event)
	{
	    if (!event.isCancelled())
	    {
	        if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA))
	        {
	            final Location current = event.getBlock().getLocation();
	            final Stargate closest = Stargate.FindClosestStargate(current);
	            if ( closest != null && (closest.Active || closest.RecentActive))
	            {
	                final double blockDistanceSquared = Stargate.distanceSquaredToClosestGateBlock(current, closest);
	                if ((blockDistanceSquared <= closest.GateShape.woosh_depth_squared && closest.GateShape.woosh_depth != 0) || blockDistanceSquared <= 25 ) 
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.Name + "\" Proximity Block Burn Distance Squared: \"" + blockDistanceSquared + "\"");
	                    event.setCancelled(true);
	                }
	            }
	        }
	    }
	}
	
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockDamage(org.bukkit.event.block.BlockDamageEvent)
	 */
	@Override
	public void onBlockDamage(BlockDamageEvent event)
	{
	    if (!event.isCancelled())
	    {
	        final Block block = event.getBlock();
	        final Stargate stargate = StargateManager.getGateFromBlock(block);
	        final Player player = event.getPlayer();
	        
	        if (stargate != null && handleBlockDamage(player,stargate))
	        {
	            event.setCancelled(true);
	        }
	    }
	}


	
	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockFlow(org.bukkit.event.block.BlockFromToEvent)
	 */
	@Override
    public void onBlockFromTo(BlockFromToEvent event)
	{
	    if (!event.isCancelled())
	    {
	        final Stargate stargate = StargateManager.getGateFromBlock(event.getToBlock());
	        if (stargate != null || StargateManager.isBlockInGate(event.getBlock()))
	        {
	            event.setCancelled(true);
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent)
	 */
	@Override
	public void onBlockIgnite(BlockIgniteEvent event)
	{
	    if (!event.isCancelled())
	    {
	        if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA))
	        {
	            final Location current = event.getBlock().getLocation();
	            final Stargate closest = Stargate.FindClosestStargate(current);
	            if ( closest != null && (closest.Active || closest.RecentActive))
	            {
	                final double blockDistanceSquared = Stargate.distanceSquaredToClosestGateBlock(current, closest);
	                if ((blockDistanceSquared <= closest.GateShape.woosh_depth_squared && closest.GateShape.woosh_depth != 0) || blockDistanceSquared <= 25 ) 
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.Name + "\" Block Type: \"" + event.getBlock().getType().toString() + "\" Proximity Block Ignite: \"" + event.getCause().toString() + "\" Distance Squared: \"" + blockDistanceSquared + "\"");
	                    event.setCancelled(true);
	                }
	            }
	        }
	    }
	}




	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent)
	 */
	@Override
    public void onBlockPhysics(BlockPhysicsEvent event)
	{
	    if (!event.isCancelled())
	    {
	        final Block block = event.getBlock();
	        if ( StargateManager.isBlockInGate(block)) 
	        {
				event.setCancelled(true);
	        }
	    }
	}
} 
