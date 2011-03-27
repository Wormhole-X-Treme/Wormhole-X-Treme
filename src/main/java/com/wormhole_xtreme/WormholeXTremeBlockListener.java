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
package com.wormhole_xtreme; 
 

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material; 
import org.bukkit.entity.Player; 

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener; 
import org.bukkit.event.block.BlockPhysicsEvent;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.permissions.WXPermissions;
import com.wormhole_xtreme.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.utils.WorldUtils;


 
// TODO: Auto-generated Javadoc
/**
 * WormholeXTreme Block Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremeBlockListener extends BlockListener
{
	//private final Stargates plugin;
	
	/**
	 * Instantiates a new wormhole x treme block listener.
	 *
	 * @param plugin the plugin
	 */
	public WormholeXTremeBlockListener(final WormholeXTreme plugin)
	{
		//this.plugin = plugin;
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
	            Location current = event.getBlock().getLocation();
	            Stargate closest = Stargate.FindClosestStargate(current);
	            if ( closest != null && (closest.Active || closest.RecentActive))
	            {
	                double blockDistanceSquared = Stargate.distanceSquaredToClosestGateBlock(current, closest);
	                if ((blockDistanceSquared <= closest.GateShape.woosh_depth_squared && closest.GateShape.woosh_depth != 0) || blockDistanceSquared <= 25 ) 
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.Name + "\" Proximity Block Ignite: \"" + event.getCause().toString() + "\" Distance Squared: \"" + blockDistanceSquared + "\"");
	                    event.setCancelled(true);
	                }
	            }
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
	            Location current = event.getBlock().getLocation();
	            Stargate closest = Stargate.FindClosestStargate(current);
	            if ( closest != null && (closest.Active || closest.RecentActive))
	            {
	                double blockDistanceSquared = Stargate.distanceSquaredToClosestGateBlock(current, closest);
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
	 * @see org.bukkit.event.block.BlockListener#onBlockFlow(org.bukkit.event.block.BlockFromToEvent)
	 */
	@Override
    public void onBlockFromTo(BlockFromToEvent event)
	{
	    if (!event.isCancelled())
	    {
	        if ( StargateManager.isBlockInGate(event.getBlock()) )
	        {
	            event.setCancelled(true);
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
	        Stargate stargate = StargateManager.getGateFromBlock(event.getBlock());
	        if (stargate != null)
	        {
	            boolean allowed = false;
	            Player player = null;
	            if (event.getPlayer() != null)
	            {
	                player = event.getPlayer();
	                allowed = WXPermissions.checkWXPermissions(player, stargate, PermissionType.DAMAGE );
	            }
	            if (!allowed)
	            {
	                if (player != null)
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied damage on: " + stargate.Name);
	                }
	                event.setCancelled(true);
	            }
	        }
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
	        Stargate stargate = StargateManager.getGateFromBlock(event.getBlock());
	        if (stargate != null)
	        {
	            boolean allowed = false;
	            Player player = null;
	            if (event.getPlayer() != null )
	            {
	                player = event.getPlayer();
	                allowed = WXPermissions.checkWXPermissions(player, stargate, PermissionType.DAMAGE);
	            }
	            if (allowed)
	            {
	                if ( !WorldUtils.isSameBlock(stargate.ActivationBlock, event.getBlock()) )
	                {
	                    if ( stargate.TeleportSignBlock != null && WorldUtils.isSameBlock(stargate.TeleportSignBlock, event.getBlock()) )
	                    {
	                        player.sendMessage("Destroyed DHD Sign. You will be unable to change dialing target from this gate.");
	                        player.sendMessage("You can rebuild it later.");
	                        stargate.TeleportSign = null;
	                    } 
	                    else if (event.getBlock().getType().equals(ConfigManager.getIrisMaterial()))
	                    {
	                        event.setCancelled(true);
	                    } 
	                    else
	                    {
	                        if (stargate.Active) 
	                        {
	                            stargate.DeActivateStargate();
	                            stargate.FillGateInterior(Material.AIR);
	                        }
	                        if (stargate.LitGate) 
	                        {
	                            stargate.UnLightStargate();
	                            stargate.StopActivationTimer(player);
	                            StargateManager.RemoveActivatedStargate(player);
	                        }
	                        stargate.ResetTeleportSign();
	                        stargate.SetupGateSign(false);
	                        if (!stargate.IrisDeactivationCode.equals(""))
	                        {
	                            stargate.SetupIrisLever(false);
	                        }
	                        StargateManager.RemoveStargate(stargate);
	                        player.sendMessage("Stargate Destroyed: " + stargate.Name);
	                    }
	                }
	                else
	                {
	                    player.sendMessage("Destroyed DHD. You will be unable to dial out from this gate.");
	                    player.sendMessage("You can rebuild it later.");
	                }
	            }
	            else
	            {
	                if (player != null)
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Player: " + player.getName() + " denied block destroy on: " + stargate.Name);
	                }
	                event.setCancelled(true);
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
	        if ( StargateManager.isBlockInGate(event.getBlock())) 
	        {
				event.setCancelled(true);
	        }
	    }
	}
} 
