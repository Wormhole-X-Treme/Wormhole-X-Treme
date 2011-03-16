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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Vehicle Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremeVehicleListener extends VehicleListener 
{ 
	
	/** The wxt. */
	private WormholeXTreme wxt = null;
	//private final Stargates plugin;
	/**
	 * Instantiates a new wormhole x treme vehicle listener.
	 *
	 * @param instance the instance
	 */
	public WormholeXTremeVehicleListener(WormholeXTreme instance) 
	{ 
		//plugin = instance; 
		wxt = instance;
	} 
	
	/** The nospeed. */
	private static Vector nospeed = new Vector();
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.vehicle.VehicleListener#onVehicleMove(org.bukkit.event.vehicle.VehicleMoveEvent)
	 */
	@Override
    public void onVehicleMove(VehicleMoveEvent event)
	{
		Location l = event.getTo();
		Block ch = l.getWorld().getBlockAt( l.getBlockX(), l.getBlockY(), l.getBlockZ());
		if ( ch.getType() == ConfigManager.getPortalMaterial() )
		{
			// This means that the cart is in a stargate that is active.
			Stargate st = StargateManager.getGateFromBlock( ch );
			
			if ( st != null &&  st.Active && st.Target != null )
			{
				Location target = st.Target.TeleportLocation;
				Vehicle veh = event.getVehicle();
				Vector v = veh.getVelocity();
				veh.setVelocity(nospeed);
				Entity e = veh.getPassenger();
				if ( e != null )
				{
					veh.setPassenger(e);
					if ( e instanceof Player )
					{
						Player p = (Player)e;
						if ( p.getVehicle() != null )
						{
							wxt.prettyLog(Level.WARNING,false,"Player shouldn't be in this cart.");
						}
						else
						{
							e.teleportTo(target);
						}
					}
				}
				
				double speed = v.length();
				Vector new_speed = new Vector();
				if ( st.Target.Facing == BlockFace.NORTH )
					new_speed.setX(-1);
				else if ( st.Target.Facing == BlockFace.SOUTH )
					new_speed.setX(1);
				else if ( st.Target.Facing == BlockFace.EAST )
					new_speed.setZ(-1);
				else if ( st.Target.Facing == BlockFace.WEST )
					new_speed.setZ(1);
				// As we all know stargates accelerate matter.
				new_speed.multiply(speed * 5);
				
				veh.teleportTo(target);
				veh.setVelocity(new_speed);
				/*if ( e != null)
					veh.setPassenger(e);*/
				if (ConfigManager.getTimeoutShutdown() == 0)
				{
					st.ShutdownStargate();
				}
			}
		}
	}
}