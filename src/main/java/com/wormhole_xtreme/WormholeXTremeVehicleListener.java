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


/** 
 * WormholeXtreme Vehicle Listener 
 * @author Ben Echols (Lologarithm) 
 */ 
public class WormholeXTremeVehicleListener extends VehicleListener 
{ 
	private WormholeXTreme wxt = null;
	//private final Stargates plugin;
	public WormholeXTremeVehicleListener(WormholeXTreme instance) 
	{ 
		//plugin = instance; 
		wxt = instance;
	} 
	
	private static Vector nospeed = new Vector();
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