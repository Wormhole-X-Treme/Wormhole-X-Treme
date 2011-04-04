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
package com.wormhole_xtreme.wormhole.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable.ActionToTake;

// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Stargate Manager.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class StargateManager 
{
	// A list of all blocks contained by all stargates. Makes for easy indexing when a player is trying
	// to enter a gate or if water is trying to flow out, also will contain the stone buttons used to activate.
	/** The all_gate_blocks. */
	private static ConcurrentHashMap<Location, Stargate> allGateBlocks = new ConcurrentHashMap<Location, Stargate>();
	// List of All stargates indexed by name. Useful for dialing and such
	/** The stargate_list. */
	private static ConcurrentHashMap<String, Stargate> stargateList = new ConcurrentHashMap<String, Stargate>();
	// List of stargates built but not named. Indexed by the player that built it.
	/** The incomplete_stargates. */
	private static ConcurrentHashMap<Player, Stargate> incompleteStargates = new ConcurrentHashMap<Player, Stargate>();
	// List of stargates that have been activated but not yet dialed. Only used for gates without public use sign.
	/** The activated_stargates. */
	private static ConcurrentHashMap<Player, Stargate> activatedStargates = new ConcurrentHashMap<Player, Stargate>();
	// List of networks indexed by their name
	/** The stargate_networks. */
	private static ConcurrentHashMap<String, StargateNetwork> stargateNetworks = new ConcurrentHashMap<String, StargateNetwork>();
	// List of players ready to build a stargate, with the shape they are trying to build.
	/** The player_builders. */
	private static ConcurrentHashMap<Player, StargateShape> playerBuilders = new ConcurrentHashMap<Player, StargateShape>();
	
	// List of blocks that are part of an active animation. Only use this to make sure water doesn't flow everywhere.
	/** The Constant opening_animation_blocks. */
	public static final ConcurrentHashMap<Location, Block> openingAnimationBlocks = new ConcurrentHashMap<Location, Block>();

	/**
	 * Adds the given stargate to the list of stargates. Also adds all its blocks to big block index.
	 * @param s The Stargate you want added.
	 */
	public static void addStargate(Stargate s)
	{
		stargateList.put(s.name, s);
		for ( Location b : s.blocks)
		{
			allGateBlocks.put(b, s);
		}
		for ( Location b : s.waterBlocks)
		{
			allGateBlocks.put(b, s);
		}
	}
	
	/**
	 * This method adds an index mapping block location to stargate.
	 * NOTE: This method does not verify that the block is part of the gate,
	 * so it may not persist and won't be removed by removing the stargate. This can cause a gate to stay in memory!!!
	 *
	 * @param b the b
	 * @param s the s
	 */
	public static void addBlockIndex(Block b, Stargate s)
	{
		if ( b != null && s != null)
			allGateBlocks.put(b.getLocation(), s);
	}

	/**
	 * This method removes an index mapping block location to stargate.
	 * NOTE: This method does not verify that the block has actually been removed from a gate
	 * so it may not persist and can be readded when server is restarted.
	 *
	 * @param b the b
	 */
	public static void removeBlockIndex(Block b)
	{
		if ( b != null)
			allGateBlocks.remove(b.getLocation());
	}
	
	/**
	 * Gets a stargate based on the name passed in. Returns null if there is no gate by that name.
	 * @param name String name of the Stargate you want returned.
	 * @return Stargate requested. Null if no stargate by that name.
	 */
	public static Stargate getStargate(String name)
	{
		if ( stargateList.containsKey(name) )
			return stargateList.get(name);
		else
			return null;
	}

	/**
	 * Get all gates.
	 * This is more expensive than some other methods so it probably shouldn't be called a lot.
	 * 
	 * @return the array list
	 */
	public static ArrayList<Stargate> getAllGates()
	{
		ArrayList<Stargate> gates = new ArrayList<Stargate>();
		
		Enumeration<Stargate> keys = stargateList.elements();
		
		while ( keys.hasMoreElements() )
			gates.add(keys.nextElement());
		
		return gates;
	}
	
	/**
	 * Removes the stargate from the list of stargates.
	 * Also removes all block from this gate from the big list of all blocks.
	 * @param s The gate you want removed.
	 */
	public static void removeStargate(Stargate s)
	{
		stargateList.remove(s.name);
		StargateDBManager.removeStargateFromSQL(s);
		if ( s.network != null )
		{
			synchronized (s.network.gateLock)
			{
				s.network.gateList.remove(s);
				
				for ( Stargate s2 : s.network.gateList)
				{
					if ( s2.signTarget != null && s2.signTarget.gateId == s.gateId && s2.isSignPowered)
					{
						s2.signTarget = null;
						if ( s.network.gateList.size() > 1 )
						{
							s2.signIndex = 0;
							WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(s2,ActionToTake.SIGNCLICK));
							// s2.teleportSignClicked();
						}
					}
				}
			}
		}

		for ( Location b : s.blocks )
		{
			allGateBlocks.remove(b);
		}

		for ( Location b : s.waterBlocks )
		{
			allGateBlocks.remove(b);
		}
	}
	
	/**
	 * This method adds a stargate that has been activated but not dialed by a player.
	 * @param p The player who has activated the gate
	 * @param s The gate the player has activated.
	 */
	public static void addActivatedStargate(Player p, Stargate s)
	{
		// s.ActivateStargate();
		activatedStargates.put(p, s);
	}
	
	/**
	 * Returns the stargate that has been activated by that player. 
	 * Returns null if that player has not activated a gate.
	 * @param p The player
	 * @return Stargate that the player has activated. Null if no active gate.
	 */
	public static Stargate removeActivatedStargate(Player p)
	{
		Stargate s = activatedStargates.remove(p);
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
	public static void addIncompleteStargate(Player p, Stargate s)
	{
		incompleteStargates.put(p, s);
	}
	
	/**
	 * Removes an incomplete stargate from the list.
	 * @param p The player who created the gate.
	 */
	public static void removeIncompleteStargate(Player p)
	{
		incompleteStargates.remove(p);
	}

	/**
	 * Complete stargate.
	 *
	 * @param p the p
	 * @param name the name
	 * @param idc the idc
	 * @param network the network
	 * @return true, if successful
	 */
	public static boolean completeStargate(Player p, String name, String idc, String network)
	{
		Stargate complete = incompleteStargates.remove(p);
		
		if ( complete != null )
		{
			if ( WormholeXTreme.getIconomy() != null )
			{
				boolean exempt = ConfigManager.getIconomyOpsExcempt();
				if ( !exempt || !p.isOp() )
				{
					Account playerAccount = iConomy.getBank().getAccount(p.getName());
					double balance = playerAccount.getBalance();
					double cost = ConfigManager.getIconomyWormholeBuildCost();
					if ( balance >= cost)
					{
						playerAccount.subtract(cost);
//						player_account.save();
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
				StargateNetwork	net = StargateManager.getStargateNetwork(network);
				if ( net == null )
					net = StargateManager.addStargateNetwork(network);
				StargateManager.addGateToNetwork(complete, network);
				complete.network = net;
			}
			
			complete.owner = p.getName();
			complete.completeGate(name, idc);
			WormholeXTreme.getThisPlugin().prettyLog(Level.INFO,false,"Player: " + p.getDisplayName() + " completed a wormhole: " + complete.name);
			addStargate(complete);
			StargateDBManager.stargateToSQL(complete);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Complete stargate.
	 *
	 * @param p the p
	 * @param s the s
	 * @return true, if successful
	 */
	public static boolean completeStargate(Player p, Stargate s)
	{
		Stargate posDupe = StargateManager.getStargate(s.name);
		if ( posDupe == null )
		{
			if ( WormholeXTreme.getIconomy() != null )
			{
				boolean exempt = ConfigManager.getIconomyOpsExcempt();
				if ( !exempt || !p.isOp() )
				{
					Account playerAccount = iConomy.getBank().getAccount(p.getName());
					double balance = playerAccount.getBalance();
					double cost = ConfigManager.getIconomyWormholeBuildCost();
					if ( balance >= cost)
					{
						playerAccount.subtract(cost);
//						player_account.save();
						p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to build a wormhole." );
					}
					else
					{
						p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to build - requires: " + cost);
						return false;
					}
				}
			}
			
			s.owner = p.getName();			
			s.completeGate(s.name, "");
			WormholeXTreme.getThisPlugin().prettyLog(Level.INFO,false,"Player: " + p.getDisplayName() + " completed a wormhole: " + s.name);
			addStargate(s);
			StargateDBManager.stargateToSQL(s);
			return true;
		}

		return false;
	}
	
	/**
	 * Gets the gate from block.
	 *
	 * @param b the b
	 * @return the gate from block
	 */
	public static Stargate getGateFromBlock(Block b)
	{
		if ( b == null )
			return null;
		
		if ( allGateBlocks.containsKey(b.getLocation()))
		{
			return allGateBlocks.get(b.getLocation());
		}
	
		return null;
	}

	// If block is a "gate" block this returns true.
	// This is useful to stop damage from being applied from an underpriveledged user.
	// Also used to stop flow of water, and prevent portal physics
	/**
	 * Checks if is block in gate.
	 *
	 * @param b the b
	 * @return true, if is block in gate
	 */
	public static boolean isBlockInGate(Block b)
	{
		if ( b == null )
			return false;

		return allGateBlocks.containsKey(b.getLocation()) || openingAnimationBlocks.containsKey(b.getLocation());
	}
	
	// Network functions
	/**
	 * Adds the stargate network.
	 *
	 * @param name the name
	 * @return the stargate network
	 */
	public static StargateNetwork addStargateNetwork(String name)
	{
		if ( !stargateNetworks.containsKey(name))
		{
			StargateNetwork sn = new StargateNetwork();
			sn.netName = name;
			stargateNetworks.put(name, sn);
			return sn;
		}
		else
			return stargateNetworks.get(name);
	}

	/**
	 * Gets the stargate network.
	 *
	 * @param name the name
	 * @return the stargate network
	 */
	public static StargateNetwork getStargateNetwork(String name)
	{
		if ( stargateNetworks.containsKey(name))
		{
			return stargateNetworks.get(name);
		}
		else
			return null;		
	}
	
	/**
	 * Adds the gate to network.
	 *
	 * @param gate the gate
	 * @param network the network
	 */
	public static void addGateToNetwork(Stargate gate, String network)
	{
		if ( !stargateNetworks.containsKey(network))
		{
			addStargateNetwork(network);
		}
		
		StargateNetwork net;
		if ((net = stargateNetworks.get(network)) != null)
		{
			synchronized (net.gateLock)
			{
				net.gateList.add(gate);
			}
		}
	}

	/**
	 * Adds the player builder shape.
	 *
	 * @param p the p
	 * @param shape the shape
	 */
	public static void addPlayerBuilderShape(Player p, StargateShape shape)
	{
		playerBuilders.put(p, shape);
	}
	
	/**
	 * Gets the player builder shape.
	 *
	 * @param p the p
	 * @return the stargate shape
	 */
	public static StargateShape getPlayerBuilderShape(Player p)
	{
		if ( playerBuilders.containsKey(p) )
			return playerBuilders.remove(p);
		else 
			return null;
	}
	
	   /**
     * Gets the square of the distance between self and target
     * which saves the costly call to {@link Math#sqrt(double)}.
     *
     * @param self Location of the local object.
     * @param target Location of the target object.
     * @return square of distance to target object from local object.
     */
    public static double getSquaredDistance(Location self, Location target)
    {
            double distance = Double.MAX_VALUE;
            if (self != null && target != null)
            {
               distance = Math.pow(self.getX() - target.getX(), 2) +
                          Math.pow(self.getY() - target.getY(), 2) +
                          Math.pow(self.getZ() - target.getZ(), 2);            
            }
            return distance;   
    }
    
    /**
     * Find the closest stargate.
     *
     * @param self Location of the local object.
     * @return The closest stargate to the local object.
     */
    public static Stargate findClosestStargate(Location self)
    {
        Stargate stargate = null;
        if (self != null)
        {
            ArrayList<Stargate> gates = StargateManager.getAllGates();
            double man = Double.MAX_VALUE;
            for (Stargate s : gates)
            {
                Location t = s.teleportLocation;
                double distance = getSquaredDistance(self, t);
                if (distance < man)
                {
                    man = distance;
                    stargate = s;
                }
            }
        }
        return stargate;
    }
    
    /**
     * Distance to closest stargate block.
     *
     * @param self Location of the local object.
     * @param stargate Stargate to check blocks for distance.
     * @return square of distance to the closest stargate block.
     */
    public static double distanceSquaredToClosestGateBlock(Location self, Stargate stargate)
    {
        double distance = Double.MAX_VALUE;
        if (stargate != null && self != null)
        {
            ArrayList<Location> gateblocks = stargate.blocks;
            for (Location l : gateblocks)
            {
                final double blockdistance = getSquaredDistance(self,l);
                if (blockdistance < distance)
                {
                    distance = blockdistance;
                }
            }
        }
        return distance;
    }
}