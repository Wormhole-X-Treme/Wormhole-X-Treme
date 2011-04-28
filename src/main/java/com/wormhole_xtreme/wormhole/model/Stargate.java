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
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable.ActionToTake;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;

/**
 * WormholeXtreme Stargate Class/Instance.
 * 
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 * 
 */
public class Stargate
{

    /** The Loaded version, used to determine what version of parser to use. */
    private byte loadedVersion = -1;

    /** The Gate id. */
    private long gateId = -1;
    /** Name of this gate, used to index and target. */
    private String gateName = "";
    /** Name of person who made the gate. */
    private String gateOwner = null;
    /** Network gate is connected to. */
    private StargateNetwork gateNetwork;
    /**
     * The gateshape that this gate uses.
     * This affects woosh depth and later materials
     */
    private StargateShape gateShape;
    /** The world this stargate is associated with. */
    private World gateWorld;
    /** Is this stargate already active? Can be active remotely and have no target of its own. */
    private boolean gateActive = false;

    /** Has this stargate been recently active?. */
    private boolean gateRecentlyActive = false;
    /** The direction that the stargate faces. */
    private BlockFace gateFacing;

    /** Is the stargate already lit up?. */
    private boolean gateLit = false;
    /** Is activated through sign destination?. */
    private boolean gateSignPowered;
    /** The stargate that is being targeted by this gate. */
    private Stargate gateTarget = null;
    /** The current target on the sign, only used if gateSignPowered is true. */
    private Stargate gateSignTarget;
    /** Temp target id to store when loading gates. */
    private long gateTempSignTarget = -1;
    /** The network index the sign is pointing at. */
    private int gateSignIndex = 0;
    /** The temporary target stargate id. */
    private long gateTempTargetId = -1;
    /** The Iris deactivation code. */
    private String gateIrisDeactivationCode = "";
    /** Is the iris Active?. */
    private boolean gateIrisActive = false;
    /** The iris default setting. */
    private boolean gateIrisDefaultActive = false;
    /** The Teleport sign, used for selection of stargate target. */
    private Sign gateTeleportSign;
    /** The location to teleport players to. */
    private Location gatePlayerTeleportLocation;
    /** The location to teleport minecarts to. */
    private Location gateMinecartTeleportLocation;
    /** Location of the Button/Lever that activates this gate. */
    private Block gateActivationBlock;
    /** Location of the Button/Lever that activates the iris. */
    private Block gateIrisActivationBlock;
    /** The Teleport sign block. */
    private Block gateTeleportSignBlock;
    /** Block that toggle the activation state of the gate if nearby redstone is activated. */
    private Block gateRedstoneActivationBlock;
    /** Block that will toggle sign target when redstone nearby is activated. */
    private Block gateRedstoneDialChangeBlock;
    /** The Name block holder. Where we place the stargate name sign. */
    private Block gateNameBlockHolder;
    /** The gate activate scheduler task id. */
    private int gateActivateTaskId;
    /** The gate shutdown scheduler task id. */
    private int gateShutdownTaskId;
    /** The gate after shutdown scheduler task id. */
    private int gateAfterShutdownTaskId;
    /** The animation_step. */
    private int gateAnimationStep = 1;
    /** The animation removing. */
    private boolean gateAnimationRemoving = false;
    /** The current_lighting_iteration. */
    private int gateLightingCurrentIteration = 0;
    /** List of all blocks contained in this stargate, including buttons and levers. */
    private final ArrayList<Location> gateStructureBlocks = new ArrayList<Location>();
    /** List of all blocks that that are part of the "portal". */
    private final ArrayList<Location> gatePortalBlocks = new ArrayList<Location>();
    /** List of all blocks that turn on when gate is active. */
    private final ArrayList<ArrayList<Location>> gateLightBlocks = new ArrayList<ArrayList<Location>>();
    /** List of all blocks that woosh in order when gate is active. */
    private final ArrayList<ArrayList<Location>> gateWooshBlocks = new ArrayList<ArrayList<Location>>();
    /** The Animated blocks. */
    private final ArrayList<Block> gateAnimatedBlocks = new ArrayList<Block>();
    /** The gate_order. */
    private final HashMap<Integer, Stargate> gateSignOrder = new HashMap<Integer, Stargate>();

    /**
     * Instantiates a new stargate.
     */
    public Stargate()
    {

    }

    /**
     * Animate opening.
     */
    public void animateOpening()
    {
        final Material wooshMaterial = getGateShape().getShapePortalMaterial();
        if ((getGateWooshBlocks() != null) && (getGateWooshBlocks().size() > 0))
        {
            final ArrayList<Location> wooshBlockStep = getGateWooshBlocks().get(getGateAnimationStep());
            if ( !isGateAnimationRemoving())
            {
                if (wooshBlockStep != null)
                {
                    for (final Location l : wooshBlockStep)
                    {
                        final Block b = getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        getGateAnimatedBlocks().add(b);
                        StargateManager.openingAnimationBlocks.put(l, b);
                        b.setType(wooshMaterial);
                    }

                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, getGateName() + " Woosh Adding: " + getGateAnimationStep() + " Woosh Block Size: " + wooshBlockStep.size());
                }

                if (getGateWooshBlocks().size() == getGateAnimationStep() + 1)
                {
                    setGateAnimationRemoving(true);
                }
                else
                {
                    setGateAnimationStep(getGateAnimationStep() + 1);
                }
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), getGateShape().getShapeWooshTicks());
            }
            else
            {
                // remove in reverse order, if block is not a portal block!
                if (wooshBlockStep != null)
                {

                    for (final Location l : wooshBlockStep)
                    {
                        final Block b = getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        StargateManager.openingAnimationBlocks.remove(l, b);
                        getGateAnimatedBlocks().remove(b);
                        b.setType(Material.AIR);
                    }
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, getGateName() + " Woosh Removing: " + getGateAnimationStep() + " Woosh Block Size: " + wooshBlockStep.size());
                }

                // If this is the last step to animate, we now add all the portal blocks in.
                if (getGateAnimationStep() == 1)
                {
                    setGateAnimationRemoving(false);
                    if (isGateLit() && isGateActive())
                    {
                        fillGateInterior(getGateShape().getShapePortalMaterial());
                    }
                }
                else
                {
                    setGateAnimationStep(getGateAnimationStep() - 1);
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), getGateShape().getShapeWooshTicks());
                }
            }
        }
        else
        {
            if (isGateActive())
            {
                fillGateInterior(getGateShape().getShapePortalMaterial());
            }
        }
    }

    /**
     * Complete gate.
     * 
     * @param name
     *            the name
     * @param idc
     *            the idc
     */
    void completeGate(final String name, final String idc)
    {
        setGateName(name);

        // 1. Setup Name Sign
        if (getGateNameBlockHolder() != null)
        {
            setupGateSign(true);
        }
        // 2. Set up Iris stuff
        setIrisDeactivationCode(idc);
    }

    /**
     * Delete gate blocks.
     */
    public void deleteGateBlocks()
    {
        for (final Location bc : getGateStructureBlocks())
        {
            final Block b = getGateWorld().getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(Material.AIR);
        }
    }

    /**
     * Delete portal blocks.
     */
    public void deletePortalBlocks()
    {
        for (final Location bc : getGatePortalBlocks())
        {
            final Block b = getGateWorld().getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(Material.AIR);
        }
    }

    /**
     * Delete teleport sign.
     */
    public void deleteTeleportSign()
    {
        if ((getGateTeleportSignBlock() != null) && (getGateTeleportSign() != null))
        {
            final Block teleportSign = getGateTeleportSignBlock().getFace(getGateFacing());
            teleportSign.setType(Material.AIR);
        }
    }

    /**
     * This method activates the current stargate as if it had just been dialed.
     * This includes filling the event horizon, canceling any other shutdown events,
     * scheduling the shutdown time and scheduling the WOOSH if enabled.
     * Failed task schedules will cause gate to not activate, fill, or animate.
     */
    private void dialStargate()
    {
        if (WormholeXTreme.getWorldHandler() != null)
        {
            WormholeXTreme.getWorldHandler().addStickyChunk(getGatePlayerTeleportLocation().getBlock().getChunk(), "WormholeXTreme");
        }
        if (getGateShutdownTaskId() > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(getGateShutdownTaskId());
        }
        if (getGateAfterShutdownTaskId() > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(getGateAfterShutdownTaskId());
        }

        final int timeout = ConfigManager.getTimeoutShutdown() * 20;
        if (timeout > 0)
        {
            setGateShutdownTaskId(WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.SHUTDOWN), timeout));
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ShutdownTaskID \"" + getGateShutdownTaskId() + "\" created.");
            if (getGateShutdownTaskId() == -1)
            {
                shutdownStargate(true);
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Failed to schdule wormhole shutdown timeout: " + timeout + " Received task id of -1. Wormhole forced closed NOW.");
            }
        }

        if ((getGateShutdownTaskId() > 0) || (timeout == 0))
        {
            if ( !isGateActive())
            {
                setGateActive(true);
                toggleDialLeverState(false);
                setGateRecentlyActive(false);
            }
            if ( !isGateLit())
            {
                // This function lights, wooshes, and then adds portal material
                lightStargate(true);
            }
            else
            {
                // Just skip top woosh if already lit (/dial gate)
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH));
            }
            // Show water if you are dialing out OR if the iris isn't active
            /*if ( this.target != null || !this.irisActive )
            {
                this.fillGateInterior(this.gateShape.portalMaterial);

                if ( this.gateShape.wooshDepth > 0 )
                {
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH));
                }
            }*/
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "No wormhole. No visual events.");
        }
    }

    /**
     * This method takes in a remote stargate and dials it if it is not active.
     * 
     * @param target
     *            the target stargate
     * @param force
     *            true to force dial the stargate, false to properly check if target gate is not active.
     * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate
     *         shutdowns.
     */
    public boolean dialStargate(final Stargate target, final boolean force)
    {
        if (getGateActivateTaskId() > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(getGateActivateTaskId());
        }

        if ( !target.isGateLit() || force)
        {
            WorldUtils.scheduleChunkLoad(target.getGateActivationBlock());
            setGateTarget(target);
            dialStargate();
            getGateTarget().dialStargate();
            if ((isGateActive()) && (getGateTarget().isGateActive()))
            {
                return true;
            }
            else if ((isGateActive()) && ( !getGateTarget().isGateActive()))
            {
                shutdownStargate(true);
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
            }
            else if (( !isGateActive()) && (getGateTarget().isGateActive()))
            {
                target.shutdownStargate(true);
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
            }
        }

        return false;
    }

    /**
     * Fill gate interior.
     * 
     * @param m
     *            the m
     */
    public void fillGateInterior(final Material m)
    {
        for (final Location bc : getGatePortalBlocks())
        {
            final Block b = getGateWorld().getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(m);
        }
    }

    /**
     * Gets the gate activate task id.
     * 
     * @return the gate activate task id
     */
    private int getGateActivateTaskId()
    {
        return gateActivateTaskId;
    }

    /**
     * Gets the gate activation block.
     * 
     * @return the gate activation block
     */
    public Block getGateActivationBlock()
    {
        return gateActivationBlock;
    }

    /**
     * Gets the gate after shutdown task id.
     * 
     * @return the gate after shutdown task id
     */
    private int getGateAfterShutdownTaskId()
    {
        return gateAfterShutdownTaskId;
    }

    /**
     * Gets the gate animated blocks.
     * 
     * @return the gate animated blocks
     */
    private ArrayList<Block> getGateAnimatedBlocks()
    {
        return gateAnimatedBlocks;
    }

    /**
     * Gets the gate animation step.
     * 
     * @return the gate animation step
     */
    private int getGateAnimationStep()
    {
        return gateAnimationStep;
    }

    /**
     * Gets the gate facing.
     * 
     * @return the gate facing
     */
    public BlockFace getGateFacing()
    {
        return gateFacing;
    }

    /**
     * Gets the gate id.
     * 
     * @return the gate id
     */
    public long getGateId()
    {
        return gateId;
    }

    /**
     * Gets the gate iris activation block.
     * 
     * @return the gate iris activation block
     */
    public Block getGateIrisActivationBlock()
    {
        return gateIrisActivationBlock;
    }

    /**
     * Gets the gate iris deactivation code.
     * 
     * @return the gate iris deactivation code
     */
    public String getGateIrisDeactivationCode()
    {
        return gateIrisDeactivationCode;
    }

    /**
     * Gets the gate light blocks.
     * 
     * @return the gate light blocks
     */
    public ArrayList<ArrayList<Location>> getGateLightBlocks()
    {
        return gateLightBlocks;
    }

    /**
     * Gets the gate lighting current iteration.
     * 
     * @return the gate lighting current iteration
     */
    private int getGateLightingCurrentIteration()
    {
        return gateLightingCurrentIteration;
    }

    /**
     * Gets the gate minecart teleport location.
     * 
     * @return the gate minecart teleport location
     */
    public Location getGateMinecartTeleportLocation()
    {
        return gateMinecartTeleportLocation;
    }

    /**
     * Gets the gate name.
     * 
     * @return the gate name
     */
    public String getGateName()
    {
        return gateName;
    }

    /**
     * Gets the gate name block holder.
     * 
     * @return the gate name block holder
     */
    public Block getGateNameBlockHolder()
    {
        return gateNameBlockHolder;
    }

    /**
     * Gets the gate network.
     * 
     * @return the gate network
     */
    public StargateNetwork getGateNetwork()
    {
        return gateNetwork;
    }

    /**
     * Gets the gate owner.
     * 
     * @return the gate owner
     */
    public String getGateOwner()
    {
        return gateOwner;
    }

    /**
     * Gets the gate teleport location.
     * 
     * @return the gate teleport location
     */
    public Location getGatePlayerTeleportLocation()
    {
        return gatePlayerTeleportLocation;
    }

    /**
     * Gets the gate portal blocks.
     * 
     * @return the gate portal blocks
     */
    public ArrayList<Location> getGatePortalBlocks()
    {
        return gatePortalBlocks;
    }

    /**
     * Gets the gate redstone activation block.
     * 
     * @return the gate redstone activation block
     */
    public Block getGateRedstoneActivationBlock()
    {
        return gateRedstoneActivationBlock;
    }

    /**
     * Gets the gate redstone dial change block.
     * 
     * @return the gate redstone dial change block
     */
    public Block getGateRedstoneDialChangeBlock()
    {
        return gateRedstoneDialChangeBlock;
    }

    /**
     * Gets the gate shape.
     * 
     * @return the gate shape
     */
    public StargateShape getGateShape()
    {
        return gateShape;
    }

    /**
     * Gets the gate shutdown task id.
     * 
     * @return the gate shutdown task id
     */
    private int getGateShutdownTaskId()
    {
        return gateShutdownTaskId;
    }

    /**
     * Gets the gate sign index.
     * 
     * @return the gate sign index
     */
    public int getGateSignIndex()
    {
        return gateSignIndex;
    }

    /**
     * Gets the gate sign order.
     * 
     * @return the gate sign order
     */
    private HashMap<Integer, Stargate> getGateSignOrder()
    {
        return gateSignOrder;
    }

    /**
     * Gets the gate sign target.
     * 
     * @return the gate sign target
     */
    public Stargate getGateSignTarget()
    {
        return gateSignTarget;
    }

    /**
     * Gets the gate structure blocks.
     * 
     * @return the gate structure blocks
     */
    public ArrayList<Location> getGateStructureBlocks()
    {
        return gateStructureBlocks;
    }

    /**
     * Gets the gate target.
     * 
     * @return the gate target
     */
    public Stargate getGateTarget()
    {
        return gateTarget;
    }

    /**
     * Gets the gate teleport sign.
     * 
     * @return the gate teleport sign
     */
    public Sign getGateTeleportSign()
    {
        return gateTeleportSign;
    }

    /**
     * Gets the gate teleport sign block.
     * 
     * @return the gate teleport sign block
     */
    public Block getGateTeleportSignBlock()
    {
        return gateTeleportSignBlock;
    }

    /**
     * Gets the gate temp sign target.
     * 
     * @return the gate temp sign target
     */
    long getGateTempSignTarget()
    {
        return gateTempSignTarget;
    }

    /**
     * Gets the gate temp target id.
     * 
     * @return the gate temp target id
     */
    long getGateTempTargetId()
    {
        return gateTempTargetId;
    }

    /**
     * Gets the gate woosh blocks.
     * 
     * @return the gate woosh blocks
     */
    public ArrayList<ArrayList<Location>> getGateWooshBlocks()
    {
        return gateWooshBlocks;
    }

    /**
     * Gets the gate world.
     * 
     * @return the gate world
     */
    public World getGateWorld()
    {
        return gateWorld;
    }

    /**
     * Gets the loaded version.
     * 
     * @return the loaded version
     */
    public byte getLoadedVersion()
    {
        return loadedVersion;
    }

    /**
     * Checks if is gate active.
     * 
     * @return true, if is gate active
     */
    public boolean isGateActive()
    {
        return gateActive;
    }

    /**
     * Checks if is gate animation removing.
     * 
     * @return true, if is gate animation removing
     */
    private boolean isGateAnimationRemoving()
    {
        return gateAnimationRemoving;
    }

    /**
     * Checks if is gate iris active.
     * 
     * @return true, if is gate iris active
     */
    public boolean isGateIrisActive()
    {
        return gateIrisActive;
    }

    /**
     * Checks if is gate iris default active.
     * 
     * @return true, if is gate iris default active
     */
    private boolean isGateIrisDefaultActive()
    {
        return gateIrisDefaultActive;
    }

    /**
     * Checks if is gate lit.
     * 
     * @return true, if is gate lit
     */
    public boolean isGateLit()
    {
        return gateLit;
    }

    /**
     * Checks if is gate recently active.
     * 
     * @return true, if is gate recently active
     */
    public boolean isGateRecentlyActive()
    {
        return gateRecentlyActive;
    }

    /**
     * Checks if is gate sign powered.
     * 
     * @return true, if is gate sign powered
     */
    public boolean isGateSignPowered()
    {
        return gateSignPowered;
    }

    /**
     * Light or darken stargate and kick off woosh animation on active stargates.
     * 
     * @param on
     *            true to light, false to darken.
     */
    public void lightStargate(final boolean on)
    {
        if (on)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Lighting up Order: " + getGateLightingCurrentIteration());
            if (getGateLightingCurrentIteration() == 0)
            {
                setGateLit(true);
            }
            else if ( !isGateLit())
            {
                lightStargate(false);
                setGateLightingCurrentIteration(0);
                return;
            }
            setGateLightingCurrentIteration(getGateLightingCurrentIteration() + 1);
            // Light up blocks
            if (getGateLightBlocks() != null)
            {
                if (getGateLightBlocks().get(getGateLightingCurrentIteration()) != null)
                {
                    for (final Location l : getGateLightBlocks().get(getGateLightingCurrentIteration()))
                    {
                        final Block b = getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        b.setType(getGateShape().getShapeActiveMaterial());
                    }
                }

                if (getGateLightingCurrentIteration() >= getGateLightBlocks().size() - 1)
                {
                    // Reset back to start
                    setGateLightingCurrentIteration(0);
                    if (isGateActive())
                    {
                        // Start up animation for woosh now!
                        WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH));
                    }
                }
                else
                {
                    // Keep lighting
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.LIGHTUP), getGateShape().getShapeLightTicks());
                }
            }
        }
        else
        {
            setGateLit(false);
            // Remove Light Up Blocks
            if (getGateLightBlocks() != null)
            {
                for (int i = 0; i < getGateLightBlocks().size(); i++)
                {
                    if (getGateLightBlocks().get(i) != null)
                    {
                        for (final Location l : getGateLightBlocks().get(i))
                        {
                            final Block b = getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                            b.setType(getGateShape().getShapeStructureMaterial());
                        }
                    }
                }
            }
        }
    }

    /**
     * Reset teleport sign.
     */
    public void resetTeleportSign()
    {
        if ((getGateTeleportSignBlock() != null) && (getGateTeleportSign() != null))
        {
            getGateTeleportSign().setLine(0, getGateName());
            if (getGateNetwork() != null)
            {
                getGateTeleportSign().setLine(1, getGateNetwork().getNetworkName());
            }
            else
            {
                getGateTeleportSign().setLine(1, "");
            }
            getGateTeleportSign().setLine(2, "");
            getGateTeleportSign().setLine(3, "");
            getGateTeleportSign().update();
        }

    }

    /**
     * Sets the gate activate task id.
     * 
     * @param gateActivateTaskId
     *            the new gate activate task id
     */
    private void setGateActivateTaskId(final int gateActivateTaskId)
    {
        this.gateActivateTaskId = gateActivateTaskId;
    }

    /**
     * Sets the gate activation block.
     * 
     * @param gateActivationBlock
     *            the new gate activation block
     */
    public void setGateActivationBlock(final Block gateActivationBlock)
    {
        this.gateActivationBlock = gateActivationBlock;
    }

    /**
     * Sets the gate active.
     * 
     * @param gateActive
     *            the new gate active
     */
    public void setGateActive(final boolean gateActive)
    {
        this.gateActive = gateActive;
    }

    /**
     * Sets the gate after shutdown task id.
     * 
     * @param gateAfterShutdownTaskId
     *            the new gate after shutdown task id
     */
    private void setGateAfterShutdownTaskId(final int gateAfterShutdownTaskId)
    {
        this.gateAfterShutdownTaskId = gateAfterShutdownTaskId;
    }

    /**
     * Sets the gate animation removing.
     * 
     * @param gateAnimationRemoving
     *            the new gate animation removing
     */
    private void setGateAnimationRemoving(final boolean gateAnimationRemoving)
    {
        this.gateAnimationRemoving = gateAnimationRemoving;
    }

    /**
     * Sets the gate animation step.
     * 
     * @param gateAnimationStep
     *            the new gate animation step
     */
    private void setGateAnimationStep(final int gateAnimationStep)
    {
        this.gateAnimationStep = gateAnimationStep;
    }

    /**
     * Sets the gate facing.
     * 
     * @param gateFacing
     *            the new gate facing
     */
    public void setGateFacing(final BlockFace gateFacing)
    {
        this.gateFacing = gateFacing;
    }

    /**
     * Sets the gate id.
     * 
     * @param gateId
     *            the new gate id
     */
    void setGateId(final long gateId)
    {
        this.gateId = gateId;
    }

    /**
     * Sets the gate iris activation block.
     * 
     * @param gateIrisActivationBlock
     *            the new gate iris activation block
     */
    public void setGateIrisActivationBlock(final Block gateIrisActivationBlock)
    {
        this.gateIrisActivationBlock = gateIrisActivationBlock;
    }

    /**
     * Sets the gate iris active.
     * 
     * @param gateIrisActive
     *            the new gate iris active
     */
    public void setGateIrisActive(final boolean gateIrisActive)
    {
        this.gateIrisActive = gateIrisActive;
    }

    /**
     * Sets the gate iris deactivation code.
     * 
     * @param gateIrisDeactivationCode
     *            the new gate iris deactivation code
     */
    public void setGateIrisDeactivationCode(final String gateIrisDeactivationCode)
    {
        this.gateIrisDeactivationCode = gateIrisDeactivationCode;
    }

    /**
     * Sets the gate iris default active.
     * 
     * @param gateIrisDefaultActive
     *            the new gate iris default active
     */
    public void setGateIrisDefaultActive(final boolean gateIrisDefaultActive)
    {
        this.gateIrisDefaultActive = gateIrisDefaultActive;
    }

    /**
     * Sets the gate lighting current iteration.
     * 
     * @param gateLightingCurrentIteration
     *            the new gate lighting current iteration
     */
    private void setGateLightingCurrentIteration(final int gateLightingCurrentIteration)
    {
        this.gateLightingCurrentIteration = gateLightingCurrentIteration;
    }

    /**
     * Sets the gate lit.
     * 
     * @param gateLit
     *            the new gate lit
     */
    public void setGateLit(final boolean gateLit)
    {
        this.gateLit = gateLit;
    }

    /**
     * Sets the gate minecart teleport location.
     * 
     * @param gateMinecartTeleportLocation
     *            the new gate minecart teleport location
     */
    public void setGateMinecartTeleportLocation(final Location gateMinecartTeleportLocation)
    {
        this.gateMinecartTeleportLocation = gateMinecartTeleportLocation;
    }

    /**
     * Sets the gate name.
     * 
     * @param gateName
     *            the new gate name
     */
    public void setGateName(final String gateName)
    {
        this.gateName = gateName;
    }

    /**
     * Sets the gate name block holder.
     * 
     * @param gateNameBlockHolder
     *            the new gate name block holder
     */
    public void setGateNameBlockHolder(final Block gateNameBlockHolder)
    {
        this.gateNameBlockHolder = gateNameBlockHolder;
    }

    /**
     * Sets the gate network.
     * 
     * @param gateNetwork
     *            the new gate network
     */
    public void setGateNetwork(final StargateNetwork gateNetwork)
    {
        this.gateNetwork = gateNetwork;
    }

    /**
     * Sets the gate owner.
     * 
     * @param gateOwner
     *            the new gate owner
     */
    public void setGateOwner(final String gateOwner)
    {
        this.gateOwner = gateOwner;
    }

    /**
     * Sets the gate teleport location.
     * 
     * @param gatePlayerTeleportLocation
     *            the new gate player teleport location
     */
    public void setGatePlayerTeleportLocation(final Location gatePlayerTeleportLocation)
    {
        this.gatePlayerTeleportLocation = gatePlayerTeleportLocation;
    }

    /**
     * Sets the gate recently active.
     * 
     * @param gateRecentlyActive
     *            the new gate recently active
     */
    private void setGateRecentlyActive(final boolean gateRecentlyActive)
    {
        this.gateRecentlyActive = gateRecentlyActive;
    }

    /**
     * Sets the gate redstone activation block.
     * 
     * @param gateRedstoneActivationBlock
     *            the new gate redstone activation block
     */
    public void setGateRedstoneActivationBlock(final Block gateRedstoneActivationBlock)
    {
        this.gateRedstoneActivationBlock = gateRedstoneActivationBlock;
    }

    /**
     * Sets the gate redstone dial change block.
     * 
     * @param gateRedstoneDialChangeBlock
     *            the new gate redstone dial change block
     */
    public void setGateRedstoneDialChangeBlock(final Block gateRedstoneDialChangeBlock)
    {
        this.gateRedstoneDialChangeBlock = gateRedstoneDialChangeBlock;
    }

    /**
     * Sets the gate shape.
     * 
     * @param gateShape
     *            the new gate shape
     */
    public void setGateShape(final StargateShape gateShape)
    {
        this.gateShape = gateShape;
    }

    /**
     * Sets the gate shutdown task id.
     * 
     * @param gateShutdownTaskId
     *            the new gate shutdown task id
     */
    private void setGateShutdownTaskId(final int gateShutdownTaskId)
    {
        this.gateShutdownTaskId = gateShutdownTaskId;
    }

    /**
     * Sets the gate sign index.
     * 
     * @param gateSignIndex
     *            the new gate sign index
     */
    public void setGateSignIndex(final int gateSignIndex)
    {
        this.gateSignIndex = gateSignIndex;
    }

    /**
     * Sets the gate sign powered.
     * 
     * @param gateSignPowered
     *            the new gate sign powered
     */
    public void setGateSignPowered(final boolean gateSignPowered)
    {
        this.gateSignPowered = gateSignPowered;
    }

    /**
     * Sets the gate sign target.
     * 
     * @param gateSignTarget
     *            the new gate sign target
     */
    protected void setGateSignTarget(final Stargate gateSignTarget)
    {
        this.gateSignTarget = gateSignTarget;
    }

    /**
     * Sets the gate target.
     * 
     * @param gateTarget
     *            the new gate target
     */
    private void setGateTarget(final Stargate gateTarget)
    {
        this.gateTarget = gateTarget;
    }

    /**
     * Sets the gate teleport sign.
     * 
     * @param gateTeleportSign
     *            the new gate teleport sign
     */
    public void setGateTeleportSign(final Sign gateTeleportSign)
    {
        this.gateTeleportSign = gateTeleportSign;
    }

    /**
     * Sets the gate teleport sign block.
     * 
     * @param gateTeleportSignBlock
     *            the new gate teleport sign block
     */
    public void setGateTeleportSignBlock(final Block gateTeleportSignBlock)
    {
        this.gateTeleportSignBlock = gateTeleportSignBlock;
    }

    /**
     * Sets the gate temp sign target.
     * 
     * @param gateTempSignTarget
     *            the new gate temp sign target
     */
    public void setGateTempSignTarget(final long gateTempSignTarget)
    {
        this.gateTempSignTarget = gateTempSignTarget;
    }

    /**
     * Sets the gate temp target id.
     * 
     * @param gateTempTargetId
     *            the new gate temp target id
     */
    public void setGateTempTargetId(final long gateTempTargetId)
    {
        this.gateTempTargetId = gateTempTargetId;
    }

    /**
     * Sets the gate world.
     * 
     * @param gateWorld
     *            the new gate world
     */
    public void setGateWorld(final World gateWorld)
    {
        this.gateWorld = gateWorld;
    }

    /**
     * Sets the iris deactivation code.
     * 
     * @param idc
     *            the idc
     */
    public void setIrisDeactivationCode(final String idc)
    {
        // If empty string make sure to make lever area air instead of lever.
        if ((idc != null) && !idc.equals(""))
        {
            setGateIrisDeactivationCode(idc);
            setupIrisLever(true);
        }
        else
        {
            setIrisState(false);
            setupIrisLever(false);
            setGateIrisDeactivationCode("");
        }
    }

    /**
     * This method sets the iris state and toggles the iris lever.
     * Smart enough to know if the gate is active and set the proper
     * material in its interior.
     * 
     * @param irisactive
     *            true for iris on, false for off.
     */
    private void setIrisState(final boolean irisactive)
    {
        setGateIrisActive(irisactive);
        fillGateInterior(isGateIrisActive() ? getGateShape().getShapeIrisMaterial() : isGateActive()
            ? getGateShape().getShapePortalMaterial() : Material.AIR);
        if ((getGateIrisActivationBlock() != null) && (getGateIrisActivationBlock().getType() == Material.LEVER))
        {
            getGateIrisActivationBlock().setData(WorldUtils.getLeverToggleByte(getGateIrisActivationBlock().getData(), isGateIrisActive()));
        }
    }

    /**
     * Sets the loaded version.
     * 
     * @param loadedVersion
     *            the new loaded version
     */
    public void setLoadedVersion(final byte loadedVersion)
    {
        this.loadedVersion = loadedVersion;
    }

    /**
     * Setup or remove gate name sign.
     * 
     * @param create
     *            true to create, false to destroy
     */
    public void setupGateSign(final boolean create)
    {
        if (getGateNameBlockHolder() != null)
        {
            if (create)
            {
                final Block nameSign = getGateNameBlockHolder().getFace(getGateFacing());
                nameSign.setType(Material.WALL_SIGN);
                nameSign.setData(WorldUtils.getSignFacingByteFromBlockFace(getGateFacing()));
                // nameSign.getState().setData(new org.bukkit.material.Sign(Material.WALL_SIGN));
                final Sign sign = (Sign) nameSign.getState();
                sign.setLine(0, "-" + getGateName() + "-");

                if (getGateNetwork() != null)
                {
                    sign.setLine(1, "N:" + getGateNetwork().getNetworkName());
                }

                if (getGateOwner() != null)
                {
                    sign.setLine(2, "O:" + getGateOwner());
                }
                sign.update();
            }
            else
            {
                Block nameSign;
                if ((nameSign = getGateNameBlockHolder().getFace(getGateFacing())) != null)
                {
                    nameSign.setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Setup or remove IRIS control lever.
     * 
     * @param create
     *            true for create, false for destroy.
     */
    public void setupIrisLever(final boolean create)
    {
        if (create)
        {
            Block iris_block = getGateIrisActivationBlock();
            if (iris_block == null)
            {
                iris_block = getGateActivationBlock().getFace(BlockFace.DOWN);
                setGateIrisActivationBlock(iris_block);
            }
            getGateStructureBlocks().add(getGateIrisActivationBlock().getLocation());

            getGateIrisActivationBlock().setType(Material.LEVER);
            getGateIrisActivationBlock().setData(WorldUtils.getLeverFacingByteFromBlockFace(getGateFacing()));
        }
        else
        {
            if (getGateIrisActivationBlock() != null)
            {
                getGateStructureBlocks().remove(getGateIrisActivationBlock().getLocation());
                getGateIrisActivationBlock().setType(Material.AIR);
            }
        }

    }

    /**
     * Shutdown stargate.
     * 
     * @param timer
     *            true if we want to spawn after shutdown timer.
     */
    public void shutdownStargate(final boolean timer)
    {
        if (getGateShutdownTaskId() > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ShutdownTaskID \"" + getGateShutdownTaskId() + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(getGateShutdownTaskId());
            setGateShutdownTaskId( -1);
        }

        if (getGateTarget() != null)
        {
            getGateTarget().shutdownStargate(true);
        }

        setGateTarget(null);
        if (timer)
        {
            setGateRecentlyActive(true);
        }
        setGateActive(false);

        lightStargate(false);
        toggleDialLeverState(false);
        // Only set back to air if iris isn't on.
        // If the iris should be on, we will make it that way.
        if (isGateIrisDefaultActive())
        {
            setIrisState(isGateIrisDefaultActive());
        }
        else if ( !isGateIrisActive())
        {
            fillGateInterior(Material.AIR);
        }

        if (timer)
        {
            startAfterShutdownTimer();
        }
        WorldUtils.scheduleChunkUnload(getGatePlayerTeleportLocation().getBlock());
    }

    /**
     * Start activation timer.
     * 
     * @param p
     *            the p
     */
    public void startActivationTimer(final Player p)
    {
        if (getGateActivateTaskId() > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(getGateActivateTaskId());
        }

        final int timeout = ConfigManager.getTimeoutActivate() * 20;
        setGateActivateTaskId(WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, p, ActionToTake.DEACTIVATE), timeout));
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ActivateTaskID \"" + getGateActivateTaskId() + "\" created.");
    }

    /**
     * After shutdown of stargate, spawn off task to set RecentActive = false;
     * This way we can depend on RecentActive for gate fire/lava protection.
     */
    private void startAfterShutdownTimer()
    {
        if (getGateAfterShutdownTaskId() > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(getGateAfterShutdownTaskId());
        }
        final int timeout = 60;
        setGateAfterShutdownTaskId(WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.AFTERSHUTDOWN), timeout));
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" AfterShutdownTaskID \"" + getGateAfterShutdownTaskId() + "\" created.");
        if (getGateAfterShutdownTaskId() == -1)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Failed to schdule wormhole after shutdown, received task id of -1.");
            setGateRecentlyActive(false);
        }
    }

    /**
     * Stop activation timer.
     * 
     */
    public void stopActivationTimer()
    {
        if (getGateActivateTaskId() > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ActivateTaskID \"" + getGateActivateTaskId() + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(getGateActivateTaskId());
            setGateActivateTaskId( -1);
        }
    }

    /**
     * After shutdown stargate.
     */
    public void stopAfterShutdownTimer()
    {
        if (getGateAfterShutdownTaskId() > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" AfterShutdownTaskID \"" + getGateAfterShutdownTaskId() + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(getGateAfterShutdownTaskId());
            setGateAfterShutdownTaskId( -1);
        }
        setGateRecentlyActive(false);
    }

    /**
     * Teleport sign clicked.
     */
    public void teleportSignClicked()
    {
        synchronized (getGateNetwork().getNetworkGateLock())
        {
            if (getGateSignIndex() == -1)
            {
                getGateTeleportSign().setLine(0, "-" + getGateName() + "-");
                setGateSignIndex(getGateSignIndex() + 1);
            }
            if ((getGateNetwork().getNetworkSignGateList().size() == 0) || (getGateNetwork().getNetworkSignGateList().size() == 1))
            {
                getGateTeleportSign().setLine(1, "");
                getGateTeleportSign().setLine(2, "No Other Gates");
                getGateTeleportSign().setLine(3, "");
                getGateTeleportSign().update();
                setGateSignTarget(null);
                return;
            }

            if (getGateSignIndex() >= getGateNetwork().getNetworkSignGateList().size())
            {
                setGateSignIndex(0);
            }

            if (getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()).getGateName().equals(getGateName()))
            {
                setGateSignIndex(getGateSignIndex() + 1);
                if (getGateSignIndex() == getGateNetwork().getNetworkSignGateList().size())
                {
                    setGateSignIndex(0);
                }
            }

            if (getGateNetwork().getNetworkSignGateList().size() == 2)
            {
                getGateSignOrder().clear();
                getGateSignOrder().put(Integer.valueOf(2), getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()));

                getGateTeleportSign().setLine(1, "");
                getGateTeleportSign().setLine(2, ">" + getGateSignOrder().get(Integer.valueOf(2)).getGateName() + "<");
                getGateTeleportSign().setLine(3, "");
                setGateSignTarget(getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()));
            }
            else if (getGateNetwork().getNetworkSignGateList().size() == 3)
            {
                getGateSignOrder().clear();
                int orderIndex = 1;
                //SignIndex++;
                while (getGateSignOrder().size() < 2)
                {
                    if (getGateSignIndex() >= getGateNetwork().getNetworkSignGateList().size())
                    {
                        setGateSignIndex(0);
                    }

                    if (getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()).getGateName().equals(getGateName()))
                    {
                        setGateSignIndex(getGateSignIndex() + 1);
                        if (getGateSignIndex() == getGateNetwork().getNetworkSignGateList().size())
                        {
                            setGateSignIndex(0);
                        }
                    }

                    getGateSignOrder().put(Integer.valueOf(orderIndex), getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()));
                    orderIndex++;
                    if (orderIndex == 4)
                    {
                        orderIndex = 1;
                    }
                    setGateSignIndex(getGateSignIndex() + 1);
                }

                getGateTeleportSign().setLine(1, getGateSignOrder().get(Integer.valueOf(1)).getGateName());
                getGateTeleportSign().setLine(2, ">" + getGateSignOrder().get(Integer.valueOf(2)).getGateName() + "<");
                getGateTeleportSign().setLine(3, "");

                setGateSignTarget(getGateSignOrder().get(Integer.valueOf(2)));
                setGateSignIndex(getGateNetwork().getNetworkSignGateList().indexOf(getGateSignOrder().get(Integer.valueOf(2))));
            }
            else
            {
                getGateSignOrder().clear();
                int orderIndex = 1;
                while (getGateSignOrder().size() < 3)
                {
                    if (getGateSignIndex() == getGateNetwork().getNetworkSignGateList().size())
                    {
                        setGateSignIndex(0);
                    }

                    if (getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()).getGateName().equals(getGateName()))
                    {
                        setGateSignIndex(getGateSignIndex() + 1);
                        if (getGateSignIndex() == getGateNetwork().getNetworkSignGateList().size())
                        {
                            setGateSignIndex(0);
                        }
                    }

                    getGateSignOrder().put(Integer.valueOf(orderIndex), getGateNetwork().getNetworkSignGateList().get(getGateSignIndex()));
                    orderIndex++;

                    setGateSignIndex(getGateSignIndex() + 1);
                }

                getGateTeleportSign().setLine(1, getGateSignOrder().get(Integer.valueOf(3)).getGateName());
                getGateTeleportSign().setLine(2, ">" + getGateSignOrder().get(Integer.valueOf(2)).getGateName() + "<");
                getGateTeleportSign().setLine(3, getGateSignOrder().get(Integer.valueOf(1)).getGateName());

                setGateSignTarget(getGateSignOrder().get(Integer.valueOf(2)));
                setGateSignIndex(getGateNetwork().getNetworkSignGateList().indexOf(getGateSignOrder().get(Integer.valueOf(2))));
            }
        }

        // getGateTeleportSign().setData(getGateTeleportSign().getData());
        getGateTeleportSign().update(true);
    }

    /**
     * Timeout stargate.
     * 
     * @param p
     *            the p
     */
    public void timeoutStargate(final Player p)
    {
        if (getGateActivateTaskId() > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ActivateTaskID \"" + getGateActivateTaskId() + "\" timed out.");
            setGateActivateTaskId( -1);
        }
        // Deactivate if player still hasn't picked a target.
        Stargate s = null;
        if (p != null)
        {
            s = StargateManager.removeActivatedStargate(p);
        }
        else
        {
            s = this;
        }

        // Only send a message if the gate was still in the remotely activated gates list.
        if (s != null)
        {
            // Make sure to reset iris if it should be on.
            if (isGateIrisDefaultActive())
            {
                setIrisState(isGateIrisDefaultActive());
            }
            if (isGateLit())
            {
                s.lightStargate(false);
            }

            if (p != null)
            {
                p.sendMessage("Gate: " + getGateName() + " timed out and deactivated.");
            }
        }
    }

    /**
     * Set the dial button and lever block state based on gate activation status.
     * 
     * @param regenerate
     *            true, to replace missing activation lever.
     */
    public void toggleDialLeverState(final boolean regenerate)
    {
        if (getGateActivationBlock() != null)
        {
            Material material = getGateActivationBlock().getType();
            if (regenerate)
            {
                getGateActivationBlock().setType(Material.LEVER);
                getGateActivationBlock().setData(WorldUtils.getLeverFacingByteFromBlockFace(getGateFacing()));
                material = getGateActivationBlock().getType();
            }
            final byte leverState = getGateActivationBlock().getData();
            switch (material)
            {
                case STONE_BUTTON :
                    getGateActivationBlock().setType(Material.LEVER);
                    getGateActivationBlock().setData(leverState);
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Automaticially replaced Button on gate \"" + getGateName() + "\" with Lever.");
                    getGateActivationBlock().setData(WorldUtils.getLeverToggleByte(leverState, isGateActive()));
                    break;
                case LEVER :
                    getGateActivationBlock().setData(WorldUtils.getLeverToggleByte(leverState, isGateActive()));
                    break;
                default :
                    break;
            }
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Dial Button Lever Gate: \"" + getGateName() + "\" Material: \"" + material.toString() + "\" State: \"" + leverState + "\"");
        }
    }

    /**
     * Toggle the iris state.
     * 
     * @param setDefault
     *            true to set the toggled state as the default state.
     */
    public void toggleIrisActive(final boolean setDefault)
    {
        setGateIrisActive( !isGateIrisActive());
        setIrisState(isGateIrisActive());
        if (setDefault)
        {
            setGateIrisDefaultActive(isGateIrisActive());
        }
    }

    /**
     * Try click teleport sign. This is the same as {@link Stargate#tryClickTeleportSign(Block, Player)} with Player set
     * to null.
     * 
     * @param clicked
     *            the clicked
     * @return true, if successful
     */
    public boolean tryClickTeleportSign(final Block clicked)
    {
        return tryClickTeleportSign(clicked, null);
    }

    /**
     * Try click teleport sign.
     * 
     * @param clicked
     *            the clicked
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean tryClickTeleportSign(final Block clicked, final Player player)
    {
        if ((getGateTeleportSign() == null) && (getGateTeleportSignBlock() != null))
        {
            if (getGateTeleportSignBlock().getType() == Material.WALL_SIGN)
            {
                setGateSignIndex( -1);
                setGateTeleportSign((Sign) getGateTeleportSignBlock().getState());
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, player, ActionToTake.SIGNCLICK));
            }
        }
        else if (WorldUtils.isSameBlock(clicked, getGateTeleportSignBlock()))
        {
            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, player, ActionToTake.SIGNCLICK));
            return true;
        }

        return false;
    }
}