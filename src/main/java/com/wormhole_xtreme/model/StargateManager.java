package com.wormhole_xtreme.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;

/** 
 * WormholeXtreme Stargate Manager 
 * @author Ben Echols (Lologarithm) 
 */ 
public class StargateManager 
{

	private static final WormholeXTreme wxt = WormholeXTreme.ThisPlugin;
	// A list of all blocks contained by all stargates. Makes for easy indexing when a player is trying
	// to enter a gate or if water is trying to flow out, also will contain the stone buttons used to activate.
	private static ConcurrentHashMap<Location, Stargate> all_gate_blocks = new ConcurrentHashMap<Location, Stargate>();
	// List of All stargates indexed by name. Useful for dialing and such
	private static ConcurrentHashMap<String, Stargate> stargate_list = new ConcurrentHashMap<String, Stargate>();
	// List of stargates built but not named. Indexed by the player that built it.
	private static ConcurrentHashMap<Player, Stargate> incomplete_stargates = new ConcurrentHashMap<Player, Stargate>();
	// List of stargates that have been activated but not yet dialed. Only used for gates without public use sign.
	private static ConcurrentHashMap<Player, Stargate> activated_stargates = new ConcurrentHashMap<Player, Stargate>();
	// List of networks indexed by their name
	private static ConcurrentHashMap<String, StargateNetwork> stargate_networks = new ConcurrentHashMap<String, StargateNetwork>();
	// List of players ready to build a stargate, with the shape they are trying to build.
	private static ConcurrentHashMap<Player, StargateShape> player_builders = new ConcurrentHashMap<Player, StargateShape>();
	
	// List of blocks that are part of an active animation. Only use this to make sure water doesn't flow everywhere.
	public static final ConcurrentHashMap<Location, Block> opening_animation_blocks = new ConcurrentHashMap<Location, Block>();

	/**
	 * Adds the given stargate to the list of stargates. Also adds all its blocks to big block index.
	 * @param s The Stargate you want added.
	 */
	public static void AddStargate(Stargate s)
	{
		stargate_list.put(s.Name, s);
		for ( Location b : s.Blocks)
		{
			all_gate_blocks.put(b, s);
		}
		for ( Location b : s.WaterBlocks)
		{
			all_gate_blocks.put(b, s);
		}
	}
	
	/**
	 * Gets a stargate based on the name passed in. Returns null if there is no gate by that name.
	 * @param name String name of the Stargate you want returned.
	 * @return Stargate requested. Null if no stargate by that name.
	 */
	public static Stargate GetStargate(String name)
	{
		if ( stargate_list.containsKey(name) )
			return stargate_list.get(name);
		else
			return null;
	}
	
	// Gets all gates
	// This is more expensive than some other methods so it probably shouldn't be called a lot.
	public static ArrayList<Stargate> GetAllGates()
	{
		ArrayList<Stargate> gates = new ArrayList<Stargate>();
		
		Enumeration<Stargate> keys = stargate_list.elements();
		
		while ( keys.hasMoreElements() )
			gates.add(keys.nextElement());
		
		return gates;
	}
	
	/**
	 * Removes the stargate from the list of stargates.
	 * Also removes all block from this gate from the big list of all blocks.
	 * @param s The gate you want removed.
	 */
	public static void RemoveStargate(Stargate s)
	{
		stargate_list.remove(s.Name);
		StargateDBManager.RemoveStargateFromSQL(s);
		if ( s.Network != null )
		{
			synchronized (s.Network.gateLock)
			{
				s.Network.gate_list.remove(s);
			}
		}

		for ( Location b : s.Blocks )
		{
			all_gate_blocks.remove(b);
		}

		for ( Location b : s.WaterBlocks )
		{
			all_gate_blocks.remove(b);
		}
	}
	
	/**
	 * This method adds a stargate that has been activated but not dialed by a player.
	 * @param p The player who has activated the gate
	 * @param s The gate the player has activated.
	 */
	public static void AddActivatedStargate(Player p, Stargate s)
	{
		// s.ActivateStargate();
		activated_stargates.put(p, s);
	}
	
	/**
	 * Returns the stargate that has been activated by that player. 
	 * Returns null if that player has not activated a gate.
	 * @param p The player
	 * @return Stargate that the player has activated. Null if no active gate.
	 */
	public static Stargate RemoveActivatedStargate(Player p)
	{
		Stargate s = activated_stargates.remove(p);
	//	if ( s != null )
	//		s.DeActivateStargate();
		return s;
	}
	
	/**
	 * Adds a gate indexed by the player that hasn't yet been named and completed.
	 * 
	 * @param p The player
	 * @param s The Stargate
	 */
	public static void AddIncompleteStargate(Player p, Stargate s)
	{
		incomplete_stargates.put(p, s);
	}
	
	/**
	 * Removes an incomplete stargate from the list.
	 * @param p The player who created the gate.
	 */
	public static void RemoveIncompleteStargate(Player p)
	{
		incomplete_stargates.remove(p);
	}

	public static boolean CompleteStargate(Player p, String name, String idc, String network)
	{
		Stargate complete = incomplete_stargates.remove(p);
		
		if ( complete != null )
		{
			if ( WormholeXTreme.Iconomy != null )
			{
				boolean exempt = ConfigManager.getIconomyOpsExcempt();
				if ( !exempt || !p.isOp() )
				{
					Account player_account = iConomy.getBank().getAccount(p.getName());
					double balance = player_account.getBalance();
					double cost = ConfigManager.getIconomyWormholeBuildCost();
					if ( balance >= cost)
					{
						player_account.subtract(cost);
						player_account.save();
						p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to build a wormhole." );
					}
					else
					{
						p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to build - requires: " + cost);
						return false;
					}
				}
			}
			
			if ( !network.equals("") )
			{
				StargateNetwork	net = StargateManager.GetStargateNetwork(network);
				if ( net == null )
					net = StargateManager.AddStargateNetwork(network);
				StargateManager.AddGateToNetwork(complete, network);
				complete.Network = net;
			}
			
			complete.Owner = p.getName();
			complete.CompleteGate(name, idc);
			wxt.prettyLog(Level.INFO,false,"Player: " + p.getDisplayName() + " completed a wormhole: " + complete.Name);
			AddStargate(complete);
			StargateDBManager.StargateToSQL(complete);
			return true;
		}
		
		return false;
	}
	
	public static boolean CompleteStargate(Player p, Stargate s)
	{
		Stargate pos_dupe = StargateManager.GetStargate(s.Name);
		if ( pos_dupe == null )
		{
			if ( WormholeXTreme.Iconomy != null )
			{
				boolean exempt = ConfigManager.getIconomyOpsExcempt();
				if ( !exempt || !p.isOp() )
				{
					Account player_account = iConomy.getBank().getAccount(p.getName());
					double balance = player_account.getBalance();
					double cost = ConfigManager.getIconomyWormholeBuildCost();
					if ( balance >= cost)
					{
						player_account.subtract(cost);
						player_account.save();
						p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to build a wormhole." );
					}
					else
					{
						p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to build - requires: " + cost);
						return false;
					}
				}
			}
			
			s.Owner = p.getName();			
			s.CompleteGate(s.Name, "");
			wxt.prettyLog(Level.INFO,false,"Player: " + p.getDisplayName() + " completed a wormhole: " + s.Name);
			AddStargate(s);
			StargateDBManager.StargateToSQL(s);
			return true;
		}

		return false;
	}
	
	public static Stargate getGateFromBlock(Block b)
	{
		if ( b == null )
			return null;
		
		if ( all_gate_blocks.containsKey(b.getLocation()))
		{
			return all_gate_blocks.get(b.getLocation());
		}
	
		return null;
	}

	// If block is a "gate" block this returns true.
	// This is useful to stop damage from being applied from an underpriveledged user.
	// Also used to stop flow of water, and prevent portal physics
	public static boolean isBlockInGate(Block b)
	{
		if ( b == null )
			return false;

		return all_gate_blocks.containsKey(b.getLocation()) || opening_animation_blocks.containsKey(b.getLocation());
	}
	
	// Network functions
	public static StargateNetwork AddStargateNetwork(String name)
	{
		if ( !stargate_networks.containsKey(name))
		{
			StargateNetwork sn = new StargateNetwork();
			sn.netName = name;
			stargate_networks.put(name, sn);
			return sn;
		}
		else
			return stargate_networks.get(name);
	}

	public static StargateNetwork GetStargateNetwork(String name)
	{
		if ( stargate_networks.containsKey(name))
		{
			return stargate_networks.get(name);
		}
		else
			return null;		
	}
	
	public static void AddGateToNetwork(Stargate gate, String network)
	{
		if ( !stargate_networks.containsKey(network))
		{
			AddStargateNetwork(network);
		}
		
		StargateNetwork net;
		if ((net = stargate_networks.get(network)) != null)
		{
			synchronized (net.gateLock)
			{
				net.gate_list.add(gate);
			}
		}
	}

	public static void AddPlayerBuilderShape(Player p, StargateShape shape)
	{
		player_builders.put(p, shape);
	}
	
	public static StargateShape GetPlayerBuilderShape(Player p)
	{
		if ( player_builders.containsKey(p) )
			return player_builders.remove(p);
		else 
			return null;
	}
}