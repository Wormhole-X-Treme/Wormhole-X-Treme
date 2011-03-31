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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.wormhole.event.StargateMinecartTeleportEvent;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.wormhole.utils.TeleportUtils;

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
	    if (event.getVehicle() instanceof Minecart)
	    {
	        handleStargateMinecartTeleportEvent(event);
	    }
	}
	
	private static boolean handleStargateMinecartTeleportEvent(VehicleMoveEvent event)
	{
	    final Location l = event.getTo();
	    final Block ch = l.getWorld().getBlockAt( l.getBlockX(), l.getBlockY(), l.getBlockZ());
	    //if ( ch.getType() == ConfigManager.getPortalMaterial() )
	    //{
	    // This means that the cart is in a stargate that is active.
	    final Stargate st = StargateManager.getGateFromBlock( ch );

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
	        final Minecart veh = (Minecart)event.getVehicle();
	        Vector v = veh.getVelocity();
	        veh.setVelocity(nospeed);
	        final Entity e = veh.getPassenger();
	        if ( e != null )
	        {
	            if ( e instanceof Player )
	            {
	                final Player p = (Player)e;
	                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Minecart Player in gate:" + st.Name + " gate Active: " + st.Active + " Target Gate: " + st.Target.Name + " Network: " + gatenetwork );
	                if (ConfigManager.getWormholeUseIsTeleport() && ((st.IsSignPowered && !WXPermissions.checkWXPermissions(p, st, PermissionType.SIGN)) ||
	                    (!st.IsSignPowered && !WXPermissions.checkWXPermissions(p, st, PermissionType.DIALER))))
	                {
	                    p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
	                    return false;
	                }
	                if ( st.Target.IrisActive )
	                {
	                    p.sendMessage(ConfigManager.errorheader + "Remote Iris is locked!");
	                    veh.teleport(st.TeleportLocation);
	                    if (ConfigManager.getTimeoutShutdown() == 0)
	                    {
	                        st.ShutdownStargate();
	                    }
	                    return false;
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
	            target = TeleportUtils.findSafeTeleportFromStargate(st);
	            veh.teleport(target);
	            veh.setVelocity(new_speed);
	        }
	        else 
	        {
	            if (target != st.TeleportLocation)
	            {
	                target = TeleportUtils.findSafeTeleportFromStargate(st.Target);
	                st.Target.TeleportLocation = target;
	            }
	            if (e != null)
	            {
	                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Removing player from cart and doing some teleport hackery");
	                veh.eject();
	                veh.remove();
	                final Minecart newveh = target.getWorld().spawnMinecart(target);
	                final Event teleportevent = new StargateMinecartTeleportEvent(veh, newveh);
	                WormholeXTreme.thisPlugin.getServer().getPluginManager().callEvent(teleportevent);
	                e.teleport(target);
	                final Vector newnew_speed = new_speed;
	                WormholeXTreme.scheduler.scheduleSyncDelayedTask(WormholeXTreme.thisPlugin, new Runnable() {
	                    public void run()
	                    {
	                        newveh.setPassenger(e);
	                        newveh.setVelocity(newnew_speed);
	                    }
	                }, 5);
	            }
	            else
	            {
	                veh.teleport(target);
	                veh.setVelocity(new_speed);
	            } 
	        }

	        if (ConfigManager.getTimeoutShutdown() == 0)
	        {
	            st.ShutdownStargate();
	        }
	        return true;
	    }
	    //}
	    return false;
	}
}
