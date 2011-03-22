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


import java.util.ArrayList;
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

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;



// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Entity Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremeEntityListener extends EntityListener 
{ 

	//private final Stargates plugin;
	/**
	 * Instantiates a new wormhole x treme entity listener.
	 *
	 * @param instance the instance
	 */
	public WormholeXTremeEntityListener(WormholeXTreme instance) 
	{ 
		//plugin = instance;
	}
	    
	/* (non-Javadoc)
	 * @see org.bukkit.event.entity.EntityListener#onEntityDamage(org.bukkit.event.entity.EntityDamageEvent)
	 */
	@Override
    public void onEntityDamage(EntityDamageEvent event)
	{
		if (!event.isCancelled() && (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA) || event.getCause().equals(DamageCause.DROWNING)))
		{
			if ( event.getEntity() instanceof Player )
			{
				Player p = (Player) event.getEntity();
				// Block standing_block = p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
				if ( StargateManager.isBlockInGate(p.getLocation().getBlock()) )
				{
					if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA) && (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA)))
					{
					    WormholeXTreme.ThisPlugin.prettyLog(Level.FINE,false,"Stopping event: " + event.getCause() + " on: " + p.getName());
					    event.setCancelled(true);
					    p.setFireTicks(0);
					}
					else if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_WATER) && event.getCause().equals(DamageCause.DROWNING))
					{
					    WormholeXTreme.ThisPlugin.prettyLog(Level.FINE,false,"Stopping event: " + event.getCause() + " on: " + p.getName());
					    event.setCancelled(true);
					}
				}
				else
				{
				    if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA) && (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA)))
				    {
				        Location current = p.getLocation();
				        ArrayList<Stargate> gates = StargateManager.GetAllGates();
				        double man = Double.MAX_VALUE;
				        Stargate closest = null;
	        
				        for(Stargate s : gates)
				        {
				            Location t = s.TeleportLocation;
				            double distance = Math.sqrt( Math.pow(current.getX() - t.getX(), 2) + 
				                                         Math.pow(current.getY() - t.getY(), 2) +
				                                         Math.pow(current.getZ() - t.getZ(), 2) );
				            if(distance < man)
				            {
				                man = distance;
				                closest = s;
				            }
				        }
	        
				        if(closest != null && man < 50 && closest.Active)
				        {
				           ArrayList<Location> gateblocks = closest.Blocks;
				           double blockdistance = Double.MAX_VALUE;
				           for (Location l : gateblocks )
				           {
				               double distance = Math.sqrt( Math.pow(current.getX() - l.getX(), 2) +
				                                            Math.pow(current.getY() - l.getY(), 2) + 
				                                            Math.pow(current.getZ() - l.getZ(), 2));
				               if (distance < blockdistance)
				               {
				                   blockdistance = distance;
				               }
				           }
				           if (blockdistance <= 3 && blockdistance != 0 )
				           {
				               WormholeXTreme.ThisPlugin.prettyLog(Level.FINE,false,"Stopping "+ closest.Name + " proximity event: " + event.getCause() + " on: " + p.getName());
				               event.setCancelled(true);
				               p.setFireTicks(0);
				           }
				        }
				    }
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.entity.EntityListener#onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent)
	 */
	@Override
	public void onEntityExplode(EntityExplodeEvent event)
	{
	    List<Block> explodeblocks = event.blockList();
	    for ( int i = 0; i < explodeblocks.size(); i++)
	    {
	        if (StargateManager.isBlockInGate(explodeblocks.get(i)))
	        {
	            Stargate explodegate = StargateManager.getGateFromBlock(explodeblocks.get(i));
	            WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Stopping Creeper Explosion on Stargate: " + explodegate.Name );
	            event.setCancelled(true);
	        }
	    }
	}
} 