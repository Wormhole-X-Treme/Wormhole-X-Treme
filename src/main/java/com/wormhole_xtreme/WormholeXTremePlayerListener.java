package com.wormhole_xtreme; 

import java.util.logging.Level;


import org.bukkit.Location; 
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener; 
import org.bukkit.event.player.PlayerMoveEvent; 

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
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

		if( ((st = StargateManager.getGateFromBlock( ch )) == null ))
		{
			return;
		}

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
				boolean exempt = ConfigManager.getIconomyOpsExcempt();
				if ( !exempt || !p.isOp() )
				{
					double cost = ConfigManager.getIconomyWormholeUseCost();
					if (cost != 0.0) 
					{
						Account player_account = iConomy.getBank().getAccount(p.getName());
						double balance = player_account.getBalance();
					    if ( balance >= cost )
					    {
						    player_account.subtract(cost);
						    player_account.save();
						    p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to use wormhole." );
						    double owner_percent = ConfigManager.getIconomyWormholeOwnerPercent();
						
						    if ( owner_percent != 0.0 && st.Owner != null )
						    {
							    if ( st.Owner != null && iConomy.getBank().hasAccount(st.Owner))
							    {
								    Account own_acc = iConomy.getBank().getAccount(st.Owner);
								    own_acc.add(cost * owner_percent);
								    own_acc.save();
							    }
						    }
					    }
					    else
					    {
						    p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to use - requires: " + cost);
						    target = st.TeleportLocation;
					    }
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
				wxt.prettyLog(Level.INFO,false, p.getDisplayName() + " used wormhole: " + st.Name + " to go to: " + st.Target.Name);
			
			if ( ConfigManager.getTimeoutShutdown() == 0 )
			{
				st.ShutdownStargate();
			}
		}
		else if ( st != null )
			wxt.prettyLog(Level.FINE, false, "Player entered gate but wasn't active or didn't have a target.");
	}
} 
 