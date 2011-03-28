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

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.permissions.WXPermissions;
import com.wormhole_xtreme.permissions.WXPermissions.PermissionType;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Vehicle Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremeVehicleListener extends VehicleListener 
{ 
	

	/**
	 * Instantiates a new wormhole x treme vehicle listener.
	 *
	 * @param instance the instance
	 */
	public WormholeXTremeVehicleListener(WormholeXTreme instance) 
	{ 
		//plugin = instance; 
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
		//if ( ch.getType() == ConfigManager.getPortalMaterial() )
		//{
			// This means that the cart is in a stargate that is active.
			Stargate st = StargateManager.getGateFromBlock( ch );
			
			if ( st != null &&  st.Active && st.Target != null )
			{
			    String gatenetwork;
			    if (st.Network != null)
			    {
			        gatenetwork = st.Network.netName;
			    }
			    else
			    {
			        gatenetwork = "Public";
			    }
				Location target = st.Target.TeleportLocation;
				WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, target.toString());
				Vehicle veh = event.getVehicle();
				Vector v = veh.getVelocity();
				veh.setVelocity(nospeed);
				Entity e = veh.getPassenger();
				if ( e != null )
				{
					// veh.setPassenger(e);
					if ( e instanceof Player )
					{
						Player p = (Player)e;
						WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Minecart Player in gate:" + st.Name + " gate Active: " + st.Active + " Target Gate: " + st.Target.Name + " Network: " + gatenetwork );
			            if (ConfigManager.getWormholeUseIsTeleport() && ((st.IsSignPowered && !WXPermissions.checkWXPermissions(p, st, PermissionType.SIGN)) ||
			                (!st.IsSignPowered && !WXPermissions.checkWXPermissions(p, st, PermissionType.DIALER))))
			            {
			                p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
			                return;
			            }
			            if ( st.Target.IrisActive )
			            {
			                p.sendMessage(ConfigManager.errorheader + "Remote Iris is locked!");
			                veh.teleport(st.TeleportLocation);
			                if (ConfigManager.getTimeoutShutdown() == 0)
			                {
			                    st.ShutdownStargate();
			                }
			                return;
			            }
			            if ( WormholeXTreme.iconomy != null )
			            {
			                double cost = ConfigManager.getIconomyWormholeUseCost();
			                boolean charge = true;
			                if ((ConfigManager.getIconomyOpsExcempt() && p.isOp()) || (st.Owner != null && st.Owner.equals(p.getName())))
			                {
			                    charge = false;
			                }
			                if (charge && cost > 0.0)
			                {
			                    Account player_account = iConomy.getBank().getAccount(p.getName());
			                    double balance = player_account.getBalance();
			                    String currency = iConomy.getBank().getCurrency();
			                    if ( balance >= cost )
			                    {
			                        player_account.subtract(cost);
			                        //  player_account.save();
			                        p.sendMessage(ConfigManager.normalheader + "Wormhole Use \u00A7F- \u00A72" + cost + " \u00A77" + currency );
			                        //p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to use wormhole." );
			                        double owner_percent = ConfigManager.getIconomyWormholeOwnerPercent();

			                        if ( owner_percent != 0.0 && st.Owner != null )
			                        {
			                            if ( st.Owner != null && iConomy.getBank().hasAccount(st.Owner))
			                            {
			                                Account own_acc = iConomy.getBank().getAccount(st.Owner);
			                                own_acc.add(cost * owner_percent);
			                                // own_acc.save();
			                            }
			                        }
			                    }
			                    else
			                    {
			                        p.sendMessage(ConfigManager.errorheader + "Not enough " + currency  + "! - Requires: \u00A72" + cost + " \u00A77- Available: \u00A74" + player_account.getBalance() + " \u00A77" + currency);
			                        //p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to use - requires: " + cost);
			                        target = st.TeleportLocation;
			                    }
			                }
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
				if (st.Target.IrisActive)
				{
				    veh.teleport(st.TeleportLocation);
				}
				else 
				{
				    if (target.getWorld() != st.TeleportLocation.getWorld())
				    {
				        if (e != null)
				        {
				            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Not same world, removing player from cart and doing some teleport hackery");
				            veh.eject();
				            target.getWorld().loadChunk(target.getBlockX(), target.getBlockZ(), false);
				            veh.teleport(target);
				            e.teleport(target);
				            if (e instanceof Player && !veh.setPassenger(e))
				            {
				                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Unable to set \"" + ((Player) e).getName() + "\" as passenger of minecart");
				            }
				        }
				        else
				        {
				            // WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Not same world, empty minecarts not allowed.");
				            veh.teleport(target);
				        }
				    }
				    else 
				    {
				        veh.teleport(target);
				    }
				    veh.setVelocity(new_speed);
				}

				if (ConfigManager.getTimeoutShutdown() == 0)
				{
					st.ShutdownStargate();
				}
			}
		//}
	}
}