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
    private final static ConcurrentHashMap<Location, Stargate> allGateBlocks = new ConcurrentHashMap<Location, Stargate>();
    // List of All stargates indexed by name. Useful for dialing and such
    /** The stargate_list. */
    private final static ConcurrentHashMap<String, Stargate> stargateList = new ConcurrentHashMap<String, Stargate>();
    // List of stargates built but not named. Indexed by the player that built it.
    /** The incomplete_stargates. */
    private final static ConcurrentHashMap<Player, Stargate> incompleteStargates = new ConcurrentHashMap<Player, Stargate>();
    // List of stargates that have been activated but not yet dialed. Only used for gates without public use sign.
    /** The activated_stargates. */
    private final static ConcurrentHashMap<Player, Stargate> activatedStargates = new ConcurrentHashMap<Player, Stargate>();
    // List of networks indexed by their name
    /** The stargate_networks. */
    private final static ConcurrentHashMap<String, StargateNetwork> stargateNetworks = new ConcurrentHashMap<String, StargateNetwork>();
    // List of players ready to build a stargate, with the shape they are trying to build.
    /** The player_builders. */
    private final static ConcurrentHashMap<Player, StargateShape> playerBuilders = new ConcurrentHashMap<Player, StargateShape>();

    // List of blocks that are part of an active animation. Only use this to make sure water doesn't flow everywhere.
    /** The Constant opening_animation_blocks. */
    protected static final ConcurrentHashMap<Location, Block> openingAnimationBlocks = new ConcurrentHashMap<Location, Block>();

    /**
     * This method adds a stargate that has been activated but not dialed by a player.
     * 
     * @param p
     *            The player who has activated the gate
     * @param s
     *            The gate the player has activated.
     */
    public static void addActivatedStargate(final Player p, final Stargate s)
    {
        // s.ActivateStargate();
        activatedStargates.put(p, s);
    }

    /**
     * This method adds an index mapping block location to stargate.
     * NOTE: This method does not verify that the block is part of the gate,
     * so it may not persist and won't be removed by removing the stargate. This can cause a gate to stay in memory!!!
     * 
     * @param b
     *            the b
     * @param s
     *            the s
     */
    public static void addBlockIndex(final Block b, final Stargate s)
    {
        if ((b != null) && (s != null))
        {
            allGateBlocks.put(b.getLocation(), s);
        }
    }

    /**
     * Adds the gate to network.
     * 
     * @param gate
     *            the gate
     * @param network
     *            the network
     */
    public static void addGateToNetwork(final Stargate gate, final String network)
    {
        if ( !stargateNetworks.containsKey(network))
        {
            addStargateNetwork(network);
        }

        StargateNetwork net;
        if ((net = stargateNetworks.get(network)) != null)
        {
            synchronized (net.getNetworkGateLock())
            {
                net.getNetworkGateList().add(gate);
                if (gate.isGateSignPowered())
                {
                    net.getNetworkSignGateList().add(gate);
                }
            }
        }
    }

    /**
     * Adds a gate indexed by the player that hasn't yet been named and completed.
     * 
     * @param p
     *            The player
     * @param s
     *            The Stargate
     */
    public static void addIncompleteStargate(final Player p, final Stargate s)
    {
        incompleteStargates.put(p, s);
    }

    /**
     * Adds the player builder shape.
     * 
     * @param p
     *            the p
     * @param shape
     *            the shape
     */
    public static void addPlayerBuilderShape(final Player p, final StargateShape shape)
    {
        playerBuilders.put(p, shape);
    }

    /**
     * Adds the given stargate to the list of stargates. Also adds all its blocks to big block index.
     * 
     * @param s
     *            The Stargate you want added.
     */
    protected static void addStargate(final Stargate s)
    {
        stargateList.put(s.getGateName(), s);
        for (final Location b : s.getGateStructureBlocks())
        {
            allGateBlocks.put(b, s);
        }
        for (final Location b : s.getGatePortalBlocks())
        {
            allGateBlocks.put(b, s);
        }
    }

    // Network functions
    /**
     * Adds the stargate network.
     * 
     * @param name
     *            the name
     * @return the stargate network
     */
    public static StargateNetwork addStargateNetwork(final String name)
    {
        if ( !stargateNetworks.containsKey(name))
        {
            final StargateNetwork sn = new StargateNetwork();
            sn.setNetworkName(name);
            stargateNetworks.put(name, sn);
            return sn;
        }
        else
        {
            return stargateNetworks.get(name);
        }
    }

    /**
     * Complete stargate.
     * 
     * @param p
     *            the p
     * @param s
     *            the s
     * @return true, if successful
     */
    public static boolean completeStargate(final Player p, final Stargate s)
    {
        final Stargate posDupe = StargateManager.getStargate(s.getGateName());
        if (posDupe == null)
        {
            if (WormholeXTreme.getIconomy() != null)
            {
                final boolean exempt = ConfigManager.getIconomyOpsExcempt();
                if ( !exempt || !p.isOp())
                {
                    final Account playerAccount = iConomy.getBank().getAccount(p.getName());
                    final double balance = playerAccount.getBalance();
                    final double cost = ConfigManager.getIconomyWormholeBuildCost();
                    if (balance >= cost)
                    {
                        playerAccount.subtract(cost);
//						player_account.save();
                        p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to build a wormhole.");
                    }
                    else
                    {
                        p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to build - requires: " + cost);
                        return false;
                    }
                }
            }

            s.setGateOwner(p.getName());
            s.completeGate(s.getGateName(), "");
            WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Player: " + p.getName() + " completed a wormhole: " + s.getGateName());
            addStargate(s);
            StargateDBManager.stargateToSQL(s);
            return true;
        }

        return false;
    }

    /**
     * Complete stargate.
     * 
     * @param p
     *            the p
     * @param name
     *            the name
     * @param idc
     *            the idc
     * @param network
     *            the network
     * @return true, if successful
     */
    public static boolean completeStargate(final Player p, final String name, final String idc, final String network)
    {
        final Stargate complete = incompleteStargates.remove(p);

        if (complete != null)
        {
            if (WormholeXTreme.getIconomy() != null)
            {
                final boolean exempt = ConfigManager.getIconomyOpsExcempt();
                if ( !exempt || !p.isOp())
                {
                    final Account playerAccount = iConomy.getBank().getAccount(p.getName());
                    final double balance = playerAccount.getBalance();
                    final double cost = ConfigManager.getIconomyWormholeBuildCost();
                    if (balance >= cost)
                    {
                        playerAccount.subtract(cost);
//						player_account.save();
                        p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to build a wormhole.");
                    }
                    else
                    {
                        p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to build - requires: " + cost);
                        return false;
                    }
                }
            }

            if ( !network.equals(""))
            {
                StargateNetwork net = StargateManager.getStargateNetwork(network);
                if (net == null)
                {
                    net = StargateManager.addStargateNetwork(network);
                }
                StargateManager.addGateToNetwork(complete, network);
                complete.setGateNetwork(net);
            }

            complete.setGateOwner(p.getName());
            complete.completeGate(name, idc);
            WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Player: " + p.getName() + " completed a wormhole: " + complete.getGateName());
            addStargate(complete);
            StargateDBManager.stargateToSQL(complete);
            return true;
        }

        return false;
    }

    /**
     * Distance to closest stargate block.
     * 
     * @param self
     *            Location of the local object.
     * @param stargate
     *            Stargate to check blocks for distance.
     * @return square of distance to the closest stargate block.
     */
    public static double distanceSquaredToClosestGateBlock(final Location self, final Stargate stargate)
    {
        double distance = Double.MAX_VALUE;
        if ((stargate != null) && (self != null))
        {
            final ArrayList<Location> gateblocks = stargate.getGateStructureBlocks();
            for (final Location l : gateblocks)
            {
                final double blockdistance = getSquaredDistance(self, l);
                if (blockdistance < distance)
                {
                    distance = blockdistance;
                }
            }
        }
        return distance;
    }

    /**
     * Find the closest stargate.
     * 
     * @param self
     *            Location of the local object.
     * @return The closest stargate to the local object.
     */
    public static Stargate findClosestStargate(final Location self)
    {
        Stargate stargate = null;
        if (self != null)
        {
            final ArrayList<Stargate> gates = StargateManager.getAllGates();
            double man = Double.MAX_VALUE;
            for (final Stargate s : gates)
            {
                final Location t = s.getGatePlayerTeleportLocation();
                final double distance = getSquaredDistance(self, t);
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
     * Get all gates.
     * This is more expensive than some other methods so it probably shouldn't be called a lot.
     * 
     * @return the array list
     */
    public static ArrayList<Stargate> getAllGates()
    {
        final ArrayList<Stargate> gates = new ArrayList<Stargate>();

        final Enumeration<Stargate> keys = stargateList.elements();

        while (keys.hasMoreElements())
        {
            gates.add(keys.nextElement());
        }

        return gates;
    }

    /**
     * Gets the gate from block.
     * 
     * @param b
     *            the b
     * @return the gate from block
     */
    public static Stargate getGateFromBlock(final Block b)
    {
        if (allGateBlocks.containsKey(b.getLocation()))
        {
            return allGateBlocks.get(b.getLocation());
        }

        return null;
    }

    /**
     * Gets the player builder shape.
     * 
     * @param p
     *            the p
     * @return the stargate shape
     */
    public static StargateShape getPlayerBuilderShape(final Player p)
    {
        if (playerBuilders.containsKey(p))
        {
            return playerBuilders.remove(p);
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the square of the distance between self and target
     * which saves the costly call to {@link Math#sqrt(double)}.
     * 
     * @param self
     *            Location of the local object.
     * @param target
     *            Location of the target object.
     * @return square of distance to target object from local object.
     */
    private static double getSquaredDistance(final Location self, final Location target)
    {
        double distance = Double.MAX_VALUE;
        if ((self != null) && (target != null))
        {
            distance = Math.pow(self.getX() - target.getX(), 2) + Math.pow(self.getY() - target.getY(), 2) + Math.pow(self.getZ() - target.getZ(), 2);
        }
        return distance;
    }

    /**
     * Gets a stargate based on the name passed in. Returns null if there is no gate by that name.
     * 
     * @param name
     *            String name of the Stargate you want returned.
     * @return Stargate requested. Null if no stargate by that name.
     */
    public static Stargate getStargate(final String name)
    {
        if (stargateList.containsKey(name))
        {
            return stargateList.get(name);
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the stargate network.
     * 
     * @param name
     *            the name
     * @return the stargate network
     */
    public static StargateNetwork getStargateNetwork(final String name)
    {
        if (stargateNetworks.containsKey(name))
        {
            return stargateNetworks.get(name);
        }
        else
        {
            return null;
        }
    }

    // If block is a "gate" block this returns true.
    // This is useful to stop damage from being applied from an underpriveledged user.
    // Also used to stop flow of water, and prevent portal physics
    /**
     * Checks if is block in gate.
     * 
     * @param b
     *            the b
     * @return true, if is block in gate
     */
    public static boolean isBlockInGate(final Block b)
    {
        return allGateBlocks.containsKey(b.getLocation()) || openingAnimationBlocks.containsKey(b.getLocation());  
    }

    /**
     * Returns the stargate that has been activated by that player.
     * Returns null if that player has not activated a gate.
     * 
     * @param p
     *            The player
     * @return Stargate that the player has activated. Null if no active gate.
     */
    public static Stargate removeActivatedStargate(final Player p)
    {
        final Stargate s = activatedStargates.remove(p);
        //	if ( s != null )
        //		s.DeActivateStargate();
        return s;
    }

    /**
     * This method removes an index mapping block location to stargate.
     * NOTE: This method does not verify that the block has actually been removed from a gate
     * so it may not persist and can be readded when server is restarted.
     * 
     * @param b
     *            the b
     */
    public static void removeBlockIndex(final Block b)
    {
        if (b != null)
        {
            allGateBlocks.remove(b.getLocation());
        }
    }

    /**
     * Removes an incomplete stargate from the list.
     * 
     * @param p
     *            The player who created the gate.
     */
    public static void removeIncompleteStargate(final Player p)
    {
        incompleteStargates.remove(p);
    }

    /**
     * Removes the stargate from the list of stargates.
     * Also removes all block from this gate from the big list of all blocks.
     * 
     * @param s
     *            The gate you want removed.
     */
    public static void removeStargate(final Stargate s)
    {
        stargateList.remove(s.getGateName());
        StargateDBManager.removeStargateFromSQL(s);
        if (s.getGateNetwork() != null)
        {
            synchronized (s.getGateNetwork().getNetworkGateLock())
            {
                s.getGateNetwork().getNetworkGateList().remove(s);
                if (s.isGateSignPowered())
                {
                    s.getGateNetwork().getNetworkSignGateList().remove(s);
                }

                for (final Stargate s2 : s.getGateNetwork().getNetworkSignGateList())
                {
                    if ((s2.getGateSignTarget() != null) && (s2.getGateSignTarget().getGateId() == s.getGateId()) && s2.isGateSignPowered())
                    {
                        s2.setGateSignTarget(null);
                        if (s.getGateNetwork().getNetworkSignGateList().size() > 1)
                        {
                            s2.setGateSignIndex(0);
                            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(s2, ActionToTake.SIGNCLICK));
                            // s2.teleportSignClicked();
                        }
                    }
                }
            }
        }

        for (final Location b : s.getGateStructureBlocks())
        {
            allGateBlocks.remove(b);
        }

        for (final Location b : s.getGatePortalBlocks())
        {
            allGateBlocks.remove(b);
        }
    }
}