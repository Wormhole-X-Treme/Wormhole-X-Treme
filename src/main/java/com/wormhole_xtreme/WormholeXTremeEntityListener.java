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
		if (!event.isCancelled() && (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA)))
		{
			if ( event.getEntity() instanceof Player )
			{
				Player p = (Player) event.getEntity();
				if (ConfigManager.getPortalMaterial().equals(Material.STATIONARY_LAVA))
				{
				    Location current = p.getLocation();
				    Stargate closest = Stargate.FindClosestStargate(current);
				    if(closest != null)
				    {
				        double blockdistance = Stargate.DistanceToClosestGateBlock(current, closest);
				        if (closest.Active && ((blockdistance <= closest.GateShape.woosh_depth && closest.GateShape.woosh_depth != 0) || blockdistance <= 4 ))
				        {
				            WormholeXTreme.thisPlugin.prettyLog(Level.FINE,false,"Blocked Gate: \""+ closest.Name + "\" Proximity Event: \"" + event.getCause().toString() + "\" On: \"" + p.getName() + "\" Distance: \"" + blockdistance + "\"");
				            event.setCancelled(true);
				            p.setFireTicks(0);
				        }
				        else if (!closest.Active && blockdistance <= 2)
				        {
				            WormholeXTreme.thisPlugin.prettyLog(Level.FINE,false,"Blocked Gate: \""+ closest.Name + "\" Proximity Event: \"" + event.getCause().toString() + "\" On: \"" + p.getName() + "\" Distance: \"" + blockdistance + "\"");
				            event.setCancelled(true);
				            p.setFireTicks(0);
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
	            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Blocked Creeper Explosion on Stargate: \"" + explodegate.Name + "\"" );
	            event.setCancelled(true);
	        }
	    }
	}
} 