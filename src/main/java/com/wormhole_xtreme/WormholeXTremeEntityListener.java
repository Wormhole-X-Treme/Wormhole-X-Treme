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


import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Entity Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremeEntityListener extends EntityListener 
{ 
	
	/** The wxt. */
	private WormholeXTreme wxt = null;
	//private final Stargates plugin;
	/**
	 * Instantiates a new wormhole x treme entity listener.
	 *
	 * @param instance the instance
	 */
	public WormholeXTremeEntityListener(WormholeXTreme instance) 
	{ 
		//plugin = instance;
		wxt = instance;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.entity.EntityListener#onEntityCombust(org.bukkit.event.entity.EntityCombustEvent)
	 */
	@Override
    public void onEntityCombust(EntityCombustEvent event)
	{
		if ( event.getEntity() instanceof Player )
		{
			Player p = (Player)event.getEntity();
			Block standing_block = p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
			if ( StargateManager.isBlockInGate(standing_block) )
			{
				wxt.prettyLog(Level.FINE,false,"Stopping combust on: " + p.getDisplayName() );
				event.setCancelled(true);
			}

		}
	}
	

	/* (non-Javadoc)
	 * @see org.bukkit.event.entity.EntityListener#onEntityDamage(org.bukkit.event.entity.EntityDamageEvent)
	 */
	@Override
    public void onEntityDamage(EntityDamageEvent event)
	{
		if (event instanceof EntityDamageByBlockEvent) {
			Block b = ((EntityDamageByBlockEvent)event).getDamager();
			if ( event.getEntity() instanceof Player && StargateManager.isBlockInGate(b) )
			{
				Player p = (Player) event.getEntity();
				if ( WormholeXTreme.Permissions != null && !ConfigManager.getSimplePermissions())
				{
					if (WormholeXTreme.Permissions.has(p, "wormhole.use.dialer") || WormholeXTreme.Permissions.has(p, "wormhole.use.sign"))
					{
						event.setCancelled(true);
					}
				}
				else if (WormholeXTreme.Permissions != null && ConfigManager.getSimplePermissions())
				{
				    if (WormholeXTreme.Permissions.has(p, "wormhole.simple.use"))
				    {
				        event.setCancelled(true);
				    }
				}
				else
				{
					wxt.prettyLog(Level.FINE,false,"Stopping dmg from:" + event.getCause());
					PermissionLevel lvl = PermissionsManager.getPermissionLevel((Player)event.getEntity(), null);
					if ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION)
						event.setCancelled(true);
				}
			}
		}
		if ( event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK) || event.getCause().equals(DamageCause.LAVA) || event.getCause().equals(DamageCause.DROWNING) || event.getCause().equals(DamageCause.DROWNING) )
		{
			if ( event.getEntity() instanceof Player )
			{
				Player p = (Player) event.getEntity();
				Block standing_block = p.getWorld().getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
				if ( StargateManager.isBlockInGate(standing_block) )
				{
					wxt.prettyLog(Level.FINE,false,"Stopping event: " + event.getCause());
					event.setCancelled(true);
				}
			}
		}
	}	
} 