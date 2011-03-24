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
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;
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
	            if ( closest != null && closest.Active)
	            {
	                double blockdistance = Stargate.DistanceToClosestGateBlock(current, closest);
	                if ((blockdistance <= closest.GateShape.woosh_depth && closest.GateShape.woosh_depth != 0) || blockdistance <= 5 ) 
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.Name + "\" Proximity Block Ignite: \"" + event.getCause().toString() + "\" Distance: \"" + blockdistance + "\"");
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
	            if ( closest != null && closest.Active)
	            {
	                double blockdistance = Stargate.DistanceToClosestGateBlock(current, closest);
	                if ((blockdistance <= closest.GateShape.woosh_depth && closest.GateShape.woosh_depth != 0) || blockdistance <= 5 ) 
	                {
	                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Blocked Gate: \"" + closest.Name + "\" Proximity Block Burn Distance: \"" + blockdistance + "\"");
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
    public void onBlockFlow(BlockFromToEvent event)
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
	            if (event instanceof Player)
	            {
	                Player player = event.getPlayer();
	                if ( player.isOp())
	                {
	                    allowed = true;
	                }
	                else if ( WormholeXTreme.permissions != null )
	                {
	                    if (!ConfigManager.getSimplePermissions() && (WormholeXTreme.permissions.has(player, "wormhole.remove.all") ||
	                        (stargate.Owner != null && stargate.Owner.equals(player.getName()) && WormholeXTreme.permissions.has(player, "wormhole.remove.own") )))
	                    {
	                        allowed = true;
	                    }
	                    else if (ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(player, "wormhole.simple.remove"))
	                    {
	                        allowed = true;
	                    }
	                }
	                else 
	                {
	                    PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, stargate);
	                    if (lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION)
	                    {
	                        allowed = true;
	                    }
	                }
	            }
	            if (!allowed)
	            {
	                event.setCancelled(true);
	            }
	        }
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.block.BlockListener#onBlockBreak(org.bukkit.event.block.BlockBreakEvent)
	 */
	@Override
	public void onBlockBreak(BlockBreakEvent e)
	{
	    Stargate s = StargateManager.getGateFromBlock(e.getBlock());
        Player p = e.getPlayer();
        if ( s != null )
        {
            boolean allowed = false;
            if ( WormholeXTreme.permissions != null && !ConfigManager.getSimplePermissions())
            {
                if ( WormholeXTreme.permissions.has(p, "wormhole.remove.all"))
                {
                    allowed = true;
                }
                else if ( s.Owner != null)  
                {
                    if (s.Owner.equals(p.getName()) && WormholeXTreme.permissions.has(p, "wormhole.remove.own"))
                    {
                        allowed = true;
                    }
                }
            }
            else if (WormholeXTreme.permissions != null && ConfigManager.getSimplePermissions())
            {
                if (WormholeXTreme.permissions.has(p, "wormhole.simple.remove"))
                {
                    allowed = true;
                }
            }
            else 
            {
                PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, s);
                if ( lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION )
                {
                    allowed = true;
                }
            }

            if ( p.isOp() || allowed )
            {
                if ( !WorldUtils.isSameBlock(s.ActivationBlock, e.getBlock()) )
                {
                    if ( s.TeleportSignBlock != null && WorldUtils.isSameBlock(s.TeleportSignBlock, e.getBlock()) )
                    {
                        p.sendMessage("Destroyed DHD Sign. You will be unable to change dialing target from this gate.");
                        p.sendMessage("You can rebuild it later.");
                        s.TeleportSign = null;
                    } 
                    else if (e.getBlock().getType().equals(ConfigManager.getIrisMaterial()))
                    {
                        e.setCancelled(true);
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
                        s.DeleteNameSign();
                        if (!s.IrisDeactivationCode.equals(""))
                        {
                            s.DeleteIrisLever();
                        }
                        StargateManager.RemoveStargate(s);
                        p.sendMessage("Stargate Destroyed: " + s.Name);
                    }
                }
                else
                {
                    p.sendMessage("Destroyed DHD. You will be unable to dial out from this gate.");
                    p.sendMessage("You can rebuild it later.");
                }
                
            }
            else
            {
                e.setCancelled(true);
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