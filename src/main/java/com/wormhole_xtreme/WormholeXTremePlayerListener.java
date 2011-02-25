package com.wormhole_xtreme; 

import java.util.logging.Level;


import org.bukkit.Location; 
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener; 
import org.bukkit.event.player.PlayerMoveEvent; 

import com.nijikokun.bukkit.iConomy.iConomy;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;


/** 
 * WormholeXtreme Player Listener 
 * @author Ben Echols (Lologarithm)
 */ 
public class WormholeXTremePlayerListener extends PlayerListener 
{ 
	private WormholeXTreme wxt = null;
	//private ConcurrentHashMap<String, Integer> PlayerCompassOn = new ConcurrentHashMap<String, Integer>(); 
	//private final WormholeXTreme plugin;
	public WormholeXTremePlayerListener(WormholeXTreme instance) 
	{ 
		//plugin = instance; 
		wxt = instance;
	} 
 

//	private void PrintHelpFile(Player player) 
//	{
//		player.sendMessage("Commands are: help, remove <name>, material <material>, irismaterial <material>, perms/perm, active_timeout <time>, shutdown_timeout <time>, owner <gate_name> <optional_set_owner>");
//	}

	@Override
    public void onPlayerMove(PlayerMoveEvent event)
	{
		Player p = event.getPlayer();
		Location l = event.getTo();
		Block ch = l.getWorld().getBlockAt( l.getBlockX(), l.getBlockY(), l.getBlockZ());
		Stargate st = null;
		if ( ch.getType().equals(ConfigManager.getPortalMaterial()) && ((st = StargateManager.getGateFromBlock( ch )) != null ))
		{
		    wxt.prettyLog(Level.FINE, false, p.getName() + " entered portal material.");

			if ( st != null && st.Active && st.Target != null )
			{
				wxt.prettyLog(Level.FINE, false, "Player in gate:" + st.Name + " gate Active: " + st.Active + " Target Gate: " + st.Target.Name);
				if ( st.Target.IrisActive )
				{
					p.sendMessage("Remote Iris is active - unable to teleport!");
					event.setFrom(st.TeleportLocation);
					event.setTo(st.TeleportLocation);
					p.teleportTo(st.TeleportLocation);
					return;
				}
			
				Location target = st.Target.TeleportLocation;
				if ( WormholeXTreme.Iconomy != null )
				{
					boolean excempt = ConfigManager.getIconomyOpsExcempt();
					if ( !excempt || !p.isOp() )
						{
							double balance = iConomy.db.getBalance(p.getName());
							double cost = ConfigManager.getIconomyWormholeUseCost();
							if ( balance >= cost)
							{
								iConomy.db.setBalance(p.getName(), balance - cost);
								p.sendMessage("You were charged " + cost + " " + iConomy.currency + " to use wormhole." );
								double owner_percent = ConfigManager.getIconomyWormholeOwnerPercent();
								if ( owner_percent != 0.0 && st.Owner != null )
								{
									double owner_balance = iConomy.db.getBalance(st.Owner);
									iConomy.db.setBalance(st.Owner, owner_balance + (double)(cost * owner_percent));
								}
							}
							else
							{
								p.sendMessage("Not enough " + iConomy.currency + " to use - requires: " + cost);
								target = st.TeleportLocation;
							}
						}
					}
					//Block target_block = target.getWorld().getBlockAt(target.getBlockX(), target.getBlockY(), target.getBlockZ());
					//while ( target_block.getType() != Material.AIR && target_block.getType() != Material.WATER  )
					//{
					//	target_block = target_block.getFace(BlockFace.UP);
					//	target.setY(target.getY() + 1.0);
					//}
				
				event.setFrom(target);
				event.setTo(target);
				p.teleportTo(target);
				event.setCancelled(true);
				if ( target == st.Target.TeleportLocation )
					wxt.prettyLog(Level.INFO,false, p.getDisplayName() + " used a wormhole to go to: " + st.Target.Name);
				
				if ( ConfigManager.getTimeoutShutdown() == 0 )
				{
					st.ShutdownStargate();
				}
			}
			else if ( st != null )
				wxt.prettyLog(Level.FINE, false, "Player entered gate but wasn't active or didn't have a target.");
		}
	}
} 
 