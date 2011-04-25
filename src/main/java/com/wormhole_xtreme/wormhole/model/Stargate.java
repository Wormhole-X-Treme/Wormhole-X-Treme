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
import org.bukkit.material.MaterialData;

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
    public byte loadedVersion = -1;
    /** The Gate id. */
    public long gateId = -1;
    /**
     * Name of this gate, used to index and target.
     */
    public String name = "";
    /** Name of person who made the gate. */
    public String owner = null;
    /**
     * Network gate is connected to.
     */
    public StargateNetwork network;
    /** Is activated through sign destination?. */
    public boolean isSignPowered;
    /**
     * The gateshape that this gate uses.
     * This affects woosh depth and later materials
     */
    public StargateShape gateShape;
    /** The My world. */
    public World myWorld;
    /** The Iris deactivation code. */
    public String irisDeactivationCode = "";
    /** Is the iris Active?. */
    public boolean irisActive = false;
    /** The iris default setting. */
    public boolean irisDefaultActive = false;
    /** Is this stargate already active? Can be active remotely and have no target of its own. */
    public boolean active = false;
    /** The Recent active. */
    public boolean recentActive = false;
    // Is this stargate already lit up? 
    /** The Lit gate. */
    public boolean litGate = false;
    // Stargate that is the target of this gate.
    /** The Target. */
    public Stargate target = null;
    // temp int id of target starget
    /** The temp_target_id. */
    public long tempTargetId = -1;
    /** Location of the Button/Lever that activates this gate. */
    public Block activationBlock;
    /** Location of the Button/Lever that activates the iris. */
    public Block irisActivationBlock;
    /** The Name block holder. Where we place the stargate name sign. */
    public Block nameBlockHolder;
    /**
     * Block that toggle the activation state of the gate
     * if nearby redstone is activated.
     */
    public Block redstoneActivationBlock;
    /**
     * Block that will toggle sign target when redstone nearby
     * is activated.
     */
    public Block redstoneDialChangeBlock;
    /** The Teleport sign block. */
    public Block teleportSignBlock;
    // Sign to choose teleport target from (optional)
    /** The Teleport sign. */
    public Sign teleportSign;
    /** The location to teleport to. */
    public Location teleportLocation;
    /** The direction that the stargate faces. */
    public BlockFace facing;
    /** The current target on the sign, only used if IsSignPowered is true. */
    public Stargate signTarget;
    /** Temp target id to store when loading gates. */
    public long tempSignTarget = -1;
    /** The network index the sign is pointing at. */
    public int signIndex = 0;

    /** List of all blocks contained in this stargate, including buttons and levers. */
    public ArrayList<Location> stargateBlocks = new ArrayList<Location>();
    /** List of all blocks that that are part of the "portal". */
    public ArrayList<Location> portalBlocks = new ArrayList<Location>();
    /** List of all blocks that turn on when gate is active. */
    public ArrayList<ArrayList<Location>> lightBlocks = new ArrayList<ArrayList<Location>>();
    /** List of all blocks that woosh in order when gate is active. */
    public ArrayList<ArrayList<Location>> wooshBlocks = new ArrayList<ArrayList<Location>>();

    // Used to track active scheduled tasks.
    /** The Activate task id. */
    private int activateTaskId;

    /** The Shutdown task id. */
    private int shutdownTaskId;

    /** The After shutdown task id. */
    private int afterShutdownTaskId;

    /** The animation_step. */
    int animationStep = 1;

    /*public Stargate(World w, String name, StargateNetwork network, byte[] gate_data)
    {
    	this.Name = name;
    	this.Network = network;
    	ParseVersionedData(gate_data, w);
    }*/

    /** The animation removing. */
    boolean animationRemoving = false;

    /** The Animated blocks. */
    ArrayList<Block> animatedBlocks = new ArrayList<Block>();
    /** The current_lighting_iteration. */
    int current_lighting_iteration = 0;
    /** The gate_order. */
    final private HashMap<Integer, Stargate> signGateOrder = new HashMap<Integer, Stargate>();

    /**
     * Instantiates a new stargate.
     */
    public Stargate()
    {

    }

    /**
     * Activate stargate.
     */
    public void activateStargate()
    {
        active = true;
    }

    /**
     * After activate stargate.
     */
    public void afterActivateStargate()
    {
        recentActive = true;
    }

    /**
     * After shutdown of stargate, spawn off task to set RecentActive = false;
     * This way we can depend on RecentActive for gate fire/lava protection.
     */
    public void afterShutdown()
    {
        if (afterShutdownTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(afterShutdownTaskId);
        }
        final int timeout = 60;
        afterShutdownTaskId = WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.AFTERSHUTDOWN), timeout);
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" AfterShutdownTaskID \"" + afterShutdownTaskId + "\" created.");
        if (afterShutdownTaskId == -1)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Failed to schdule wormhole after shutdown, received task id of -1.");
            deRecentActivateStargate();
        }
    }

    /**
     * After shutdown stargate.
     */
    public void afterShutdownStargate()
    {
        if (afterShutdownTaskId > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" AfterShutdownTaskID \"" + afterShutdownTaskId + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(afterShutdownTaskId);
            afterShutdownTaskId = -1;
        }
        deRecentActivateStargate();
    }

    /**
     * Animate opening.
     */
    public void animateOpening()
    {
        final Material wooshMaterial = gateShape.portalMaterial;
        if ((wooshBlocks != null) && (wooshBlocks.size() != 0))
        {
            final ArrayList<Location> wooshBlockStep = wooshBlocks.get(animationStep);
            if ( !animationRemoving)
            {
                if (wooshBlockStep != null)
                {
                    for (final Location l : wooshBlockStep)
                    {
                        final Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        animatedBlocks.add(b);
                        StargateManager.openingAnimationBlocks.put(l, b);
                        b.setType(wooshMaterial);
                    }

                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, name + " Woosh Adding: " + animationStep + " Woosh Block Size: " + wooshBlockStep.size());
                }

                if (wooshBlocks.size() == animationStep + 1)
                {
                    animationRemoving = true;
                }
                else
                {
                    animationStep++;
                }
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), gateShape.wooshTicks);

            }
            else
            {
                // remove in reverse order, if block is not a portal block!
                if (wooshBlockStep != null)
                {

                    for (final Location l : wooshBlockStep)
                    {
                        final Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        StargateManager.openingAnimationBlocks.remove(l, b);
                        animatedBlocks.remove(b);
                        b.setType(Material.AIR);
                    }
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, name + " Woosh Removing: " + animationStep + " Woosh Block Size: " + wooshBlockStep.size());
                }

                // If this is the last step to animate, we now add all the portal blocks in.
                if (animationStep == 1)
                {
                    animationRemoving = false;
                    if (litGate && active)
                    {
                        fillGateInterior(gateShape.portalMaterial);
                    }
                }
                else
                {
                    animationStep--;
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), gateShape.wooshTicks);
                }
            }
        }
        else
        {
            if (active)
            {
                fillGateInterior(gateShape.portalMaterial);
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
    public void completeGate(final String name, final String idc)
    {
        this.name = name;

        // 1. Setup Name Sign
        if (nameBlockHolder != null)
        {
            setupGateSign(true);
        }
        // 2. Set up Iris stuff
        setIrisDeactivationCode(idc);
    }

    /**
     * De activate stargate.
     */
    public void deActivateStargate()
    {
        active = false;
    }

    /**
     * Delete gate blocks.
     */
    public void deleteGateBlocks()
    {
        for (final Location bc : stargateBlocks)
        {
            final Block b = myWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(Material.AIR);
        }
    }

    /**
     * Delete portal blocks.
     */
    public void deletePortalBlocks()
    {
        for (final Location bc : portalBlocks)
        {
            final Block b = myWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(Material.AIR);
        }
    }

    /**
     * Delete teleport sign.
     */
    public void deleteTeleportSign()
    {
        if ((teleportSignBlock != null) && (teleportSign != null))
        {
            final Block teleportSign = teleportSignBlock.getFace(facing);
            teleportSign.setType(Material.AIR);
        }
    }

    /**
     * De recent activate stargate.
     */
    public void deRecentActivateStargate()
    {
        recentActive = false;
    }

    /**
     * Dial button lever state.
     * Same as calling {@link #dialButtonLeverState(boolean)} with boolean true.
     */
    public void dialButtonLeverState()
    {
        this.dialButtonLeverState(false);
    }

    /**
     * Set the dial button and lever block state based on gate activation status.
     * 
     * @param regenerate
     *            true, to replace missing activation lever.
     */
    public void dialButtonLeverState(final boolean regenerate)
    {
        if (activationBlock != null)
        {
            Material material = activationBlock.getType();
            if (regenerate)
            {
                if ((material != Material.LEVER) && (material != Material.STONE_BUTTON))
                {
                    activationBlock.setType(Material.LEVER);
                    switch (facing)
                    {
                        case SOUTH :
                            activationBlock.setData((byte) 0x01);
                            break;
                        case NORTH :
                            activationBlock.setData((byte) 0x02);
                            break;
                        case WEST :
                            activationBlock.setData((byte) 0x03);
                            break;
                        case EAST :
                            activationBlock.setData((byte) 0x04);
                            break;
                        default :
                            break;
                    }
                }
                material = activationBlock.getType();
            }
            if ((material == Material.LEVER) || (material == Material.STONE_BUTTON))
            {
                int state = activationBlock.getData();
                if (material == Material.STONE_BUTTON)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Automaticially replaced Button on gate \"" + name + "\" with Lever.");
                    activationBlock.setType(Material.LEVER);
                    activationBlock.setData((byte) state);
                }
                if (active)
                {
                    if ((state <= 4) && (state != 0))
                    {
                        state = state + 8;
                    }
                }
                else
                {
                    if ((state <= 12) && (state >= 9))
                    {
                        state = state - 8;
                    }
                }

                activationBlock.setData((byte) state);
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Dial Button Lever Gate: \"" + name + "\" Material: \"" + material.toString() + "\" State: \"" + state + "\"");
                }
            }
        }
    }

    /**
     * This method activates the current stargate as if it had just been dialed.
     * This includes filling the event horizon, canceling any other shutdown events,
     * scheduling the shutdown time and scheduling the WOOSH if enabled.
     * Failed task schedules will cause gate to not activate, fill, or animate.
     */
    public void dialStargate()
    {
        if (WormholeXTreme.getWorldHandler() != null)
        {
            WormholeXTreme.getWorldHandler().addStickyChunk(teleportLocation.getBlock().getChunk(), "WormholeXTreme");
        }
        if (shutdownTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(shutdownTaskId);
        }
        if (afterShutdownTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(afterShutdownTaskId);
        }

        final int timeout = ConfigManager.getTimeoutShutdown() * 20;
        if (timeout > 0)
        {
            shutdownTaskId = WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.SHUTDOWN), timeout);
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" ShutdownTaskID \"" + shutdownTaskId + "\" created.");
            if (shutdownTaskId == -1)
            {
                shutdownStargate();
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Failed to schdule wormhole shutdown timeout: " + timeout + " Received task id of -1. Wormhole forced closed NOW.");
            }
        }

        if ((shutdownTaskId > 0) || (timeout == 0))
        {
            if ( !active)
            {
                activateStargate();
                this.dialButtonLeverState();
                deRecentActivateStargate();
            }
            if ( !litGate)
            {
                // This function lights, wooshes, and then adds portal material
                lightStargate();
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
     *            the target
     * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate
     *         shutdowns.
     */
    public boolean dialStargate(final Stargate target)
    {
        if (activateTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(activateTaskId);
        }

        if ( !target.litGate)
        {
            WorldUtils.scheduleChunkLoad(target.activationBlock);
            this.target = target;
            this.dialStargate();
            target.dialStargate();
            if ((active) && (this.target.active))
            {
                return true;
            }
            else if ((active) && ( !this.target.active))
            {
                this.shutdownStargate();
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
            }
            else if (( !active) && (target.active))
            {
                target.shutdownStargate();
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
        for (final Location bc : portalBlocks)
        {
            final Block b = myWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(m);
        }
    }

    /**
     * This method takes in a remote stargate and dials it if it is not active.
     * 
     * @param target
     *            the target
     * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate
     *         shutdowns.
     */
    public boolean forceDialStargate(final Stargate target)
    {
        if (activateTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(activateTaskId);
        }

        //if ( !target.LitGate )
        //{
        WorldUtils.scheduleChunkLoad(target.activationBlock);
        this.target = target;
        this.dialStargate();
        target.dialStargate();
        if ((active) && (target.active))
        {
            return true;
        }
        else if ((active) && ( !target.active))
        {
            this.shutdownStargate();
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
        }
        else if (( !active) && (target.active))
        {
            target.shutdownStargate();
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
        }
        //}

        return false;
    }

    /**
     * Light stargate.
     */
    public void lightStargate()
    {
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Lighting up Order: " + current_lighting_iteration);
        litGate = true;
        current_lighting_iteration++;
        // Light up blocks
        if (lightBlocks != null)
        {
            if (lightBlocks.get(current_lighting_iteration) != null)
            {
                for (final Location l : lightBlocks.get(current_lighting_iteration))
                {
                    final Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                    b.setType(gateShape.activeMaterial);
                }
            }

            if (current_lighting_iteration >= lightBlocks.size() - 1)
            {
                // Reset back to start
                current_lighting_iteration = 0;
                if (active)
                {
                    // Start up animation for woosh now!
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH));
                }
            }
            else
            {
                // Keep lighting
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.LIGHTUP), gateShape.lightTicks);
            }
        }

    }

    /**
     * Reset teleport sign.
     */
    public void resetTeleportSign()
    {
        if ((teleportSignBlock != null) && (teleportSign != null))
        {
            teleportSign.setLine(0, name);
            if (network != null)
            {
                teleportSign.setLine(1, network.netName);
            }
            else
            {
                teleportSign.setLine(1, "");
            }
            teleportSign.setLine(2, "");
            teleportSign.setLine(3, "");
            teleportSign.setData(teleportSign.getData());
            teleportSign.update();
        }

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
            irisDeactivationCode = idc;
            setupIrisLever(true);
        }
        else
        {
            this.toggleIrisActive(false);
            setupIrisLever(false);
            irisDeactivationCode = "";
        }
    }

    /**
     * Setup or remove gate name sign.
     * 
     * @param create
     *            true to create, false to destroy
     */
    public void setupGateSign(final boolean create)
    {
        if (nameBlockHolder != null)
        {
            if (create)
            {
                final Block nameSign = nameBlockHolder.getFace(facing);
                nameSign.setType(Material.WALL_SIGN);
                switch (facing)
                {
                    case NORTH :
                        nameSign.setData((byte) 0x04);
                        break;
                    case SOUTH :
                        nameSign.setData((byte) 0x05);
                        break;
                    case EAST :
                        nameSign.setData((byte) 0x02);
                        break;
                    case WEST :
                        nameSign.setData((byte) 0x03);
                        break;
                    default :
                        break;
                }
                nameSign.getState().setData(new MaterialData(Material.WALL_SIGN));
                final Sign sign = (Sign) nameSign.getState();
                sign.setLine(0, "-" + name + "-");

                if (network != null)
                {
                    sign.setLine(1, "N:" + network.netName);
                }

                if (owner != null)
                {
                    sign.setLine(2, "O:" + owner);
                }
                sign.update();
            }
            else
            {
                Block nameSign;
                if ((nameSign = nameBlockHolder.getFace(facing)) != null)
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
            Block iris_block = irisActivationBlock;
            if (iris_block == null)
            {
                iris_block = activationBlock.getFace(BlockFace.DOWN);
                irisActivationBlock = iris_block;
            }
            stargateBlocks.add(irisActivationBlock.getLocation());

            irisActivationBlock.setType(Material.LEVER);
            switch (facing)
            {
                case SOUTH :
                    irisActivationBlock.setData((byte) 0x01);
                    break;
                case NORTH :
                    irisActivationBlock.setData((byte) 0x02);
                    break;
                case WEST :
                    irisActivationBlock.setData((byte) 0x03);
                    break;
                case EAST :
                    irisActivationBlock.setData((byte) 0x04);
                    break;
                default :
                    break;
            }
        }
        else
        {
            if (irisActivationBlock != null)
            {
                stargateBlocks.remove(irisActivationBlock.getLocation());
                irisActivationBlock.setType(Material.AIR);
            }
        }

    }

    /**
     * Shutdown stargate.
     * This is the same as calling ShutdownStargate(false)
     */
    public void shutdownStargate()
    {
        this.shutdownStargate(true);
    }

    /**
     * Shutdown stargate.
     * 
     * @param timer
     *            true if we want to spawn after shutdown timer.
     */
    public void shutdownStargate(final boolean timer)
    {
        if (shutdownTaskId > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" ShutdownTaskID \"" + shutdownTaskId + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(shutdownTaskId);
            shutdownTaskId = -1;
        }

        if (target != null)
        {
            target.shutdownStargate();
        }

        target = null;
        if (timer)
        {
            afterActivateStargate();
        }
        deActivateStargate();

        unLightStargate();
        this.dialButtonLeverState();
        // Only set back to air if iris isn't on.
        // If the iris should be on, we will make it that way.
        if (irisDefaultActive)
        {
            toggleIrisActive(irisDefaultActive);
        }
        else if ( !irisActive)
        {
            fillGateInterior(Material.AIR);
        }

        if (timer)
        {
            afterShutdown();
        }
        WorldUtils.scheduleChunkUnload(teleportLocation.getBlock());
    }

    /**
     * Start activation timer.
     * 
     * @param p
     *            the p
     */
    public void startActivationTimer(final Player p)
    {
        if (activateTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(activateTaskId);
        }

        final int timeout = ConfigManager.getTimeoutActivate() * 20;
        activateTaskId = WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, p, ActionToTake.DEACTIVATE), timeout);
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" ActivateTaskID \"" + activateTaskId + "\" created.");
    }

    /**
     * Stop activation timer.
     * 
     */
    public void stopActivationTimer()
    {
        if (activateTaskId > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" ActivateTaskID \"" + activateTaskId + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(activateTaskId);
            activateTaskId = -1;
        }
    }

    // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
    //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

    /**
     * Teleport sign clicked.
     */
    public void teleportSignClicked()
    {
        synchronized (network.gateLock)
        {
            if (signIndex == -1)
            {
                teleportSign.setLine(0, "-" + name + "-");
                signIndex++;
            }
            if ((network.signGateList.size() == 0) || (network.signGateList.size() == 1))
            {
                teleportSign.setLine(1, "");
                teleportSign.setLine(2, "No Other Gates");
                teleportSign.setLine(3, "");
                signTarget = null;
                return;
            }

            if (signIndex >= network.signGateList.size())
            {
                signIndex = 0;
            }

            if (network.signGateList.get(signIndex).name.equals(name))
            {
                signIndex++;
                if (signIndex == network.signGateList.size())
                {
                    signIndex = 0;
                }
            }

            if (network.signGateList.size() == 2)
            {
                signGateOrder.clear();
                signGateOrder.put(Integer.valueOf(2), network.signGateList.get(signIndex));

                teleportSign.setLine(1, "");
                teleportSign.setLine(2, ">" + signGateOrder.get(Integer.valueOf(2)).name + "<");
                teleportSign.setLine(3, "");
                signTarget = network.signGateList.get(signIndex);
            }
            else if (network.signGateList.size() == 3)
            {
                signGateOrder.clear();
                int orderIndex = 1;
                //SignIndex++;
                while (signGateOrder.size() < 2)
                {
                    if (signIndex >= network.signGateList.size())
                    {
                        signIndex = 0;
                    }

                    if (network.signGateList.get(signIndex).name.equals(name))
                    {
                        signIndex++;
                        if (signIndex == network.signGateList.size())
                        {
                            signIndex = 0;
                        }
                    }

                    signGateOrder.put(Integer.valueOf(orderIndex), network.signGateList.get(signIndex));
                    orderIndex++;
                    if (orderIndex == 4)
                    {
                        orderIndex = 1;
                    }
                    signIndex++;
                }

                teleportSign.setLine(1, signGateOrder.get(Integer.valueOf(1)).name);
                teleportSign.setLine(2, ">" + signGateOrder.get(Integer.valueOf(2)).name + "<");
                teleportSign.setLine(3, "");

                signTarget = signGateOrder.get(Integer.valueOf(2));
                signIndex = network.signGateList.indexOf(signGateOrder.get(Integer.valueOf(2)));
            }
            else
            {
                signGateOrder.clear();
                int orderIndex = 1;
                while (signGateOrder.size() < 3)
                {
                    if (signIndex == network.signGateList.size())
                    {
                        signIndex = 0;
                    }

                    if (network.signGateList.get(signIndex).name.equals(name))
                    {
                        signIndex++;
                        if (signIndex == network.signGateList.size())
                        {
                            signIndex = 0;
                        }
                    }

                    signGateOrder.put(Integer.valueOf(orderIndex), network.signGateList.get(signIndex));
                    orderIndex++;

                    signIndex++;
                }

                teleportSign.setLine(1, signGateOrder.get(Integer.valueOf(3)).name);
                teleportSign.setLine(2, ">" + signGateOrder.get(Integer.valueOf(2)).name + "<");
                teleportSign.setLine(3, signGateOrder.get(Integer.valueOf(1)).name);

                signTarget = signGateOrder.get(Integer.valueOf(2));
                signIndex = network.signGateList.indexOf(signGateOrder.get(Integer.valueOf(2)));
            }
        }

        teleportSign.setData(teleportSign.getData());
        teleportSign.update(true);
    }

    /**
     * Timeout stargate.
     * 
     * @param p
     *            the p
     */
    public void timeoutStargate(final Player p)
    {
        if (activateTaskId > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + name + "\" ActivateTaskID \"" + activateTaskId + "\" timed out.");
            activateTaskId = -1;
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
            if (irisDefaultActive)
            {
                toggleIrisActive(irisDefaultActive);
            }
            if (litGate)
            {
                s.unLightStargate();
            }

            if (p != null)
            {
                p.sendMessage("Gate: " + name + " timed out and deactivated.");
            }
        }
    }

    /**
     * Toggle the iris state.
     */
    public void toggleIrisActive()
    {
        irisActive = !irisActive;
        this.toggleIrisActive(irisActive);
    }

    /**
     * This method sets the iris state and toggles the iris lever.
     * Smart enough to know if the gate is active and set the proper
     * material in its interior.
     * 
     * @param irisactive
     *            true for iris on, false for off.
     */
    public void toggleIrisActive(final boolean irisactive)
    {
        irisActive = irisactive;
        int leverstate = irisActivationBlock.getData();
        if (irisActive)
        {
            if ((leverstate <= 4) && (leverstate != 0))
            {
                leverstate = leverstate + 8;
            }
            fillGateInterior(gateShape.irisMaterial);
        }
        else
        {
            if ((leverstate <= 12) && (leverstate >= 9))
            {
                leverstate = leverstate - 8;
            }
            if (active)
            {
                fillGateInterior(gateShape.portalMaterial);
            }
            else
            {
                fillGateInterior(Material.AIR);
            }
        }
        if ((irisActivationBlock != null) && (irisActivationBlock.getType() == Material.LEVER))
        {
            irisActivationBlock.setData((byte) leverstate);
        }
    }

    /**
     * This method should only be called when the Iris lever is hit.
     * This toggles the current state of the Iris and then sets that state to be the default.
     */
    public void toggleIrisDefault()
    {
        toggleIrisActive();
        irisDefaultActive = irisActive;
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
        if ((teleportSign == null) && (teleportSignBlock != null))
        {
            if (teleportSignBlock.getType() == Material.WALL_SIGN)
            {
                signIndex = -1;
                teleportSign = (Sign) teleportSignBlock.getState();
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, player, ActionToTake.SIGNCLICK));
            }
        }
        else if (WorldUtils.isSameBlock(clicked, teleportSignBlock))
        {
            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, player, ActionToTake.SIGNCLICK));
            return true;
        }

        return false;
    }

    /**
     * Un light stargate.
     */
    public void unLightStargate()
    {
        litGate = false;

        // Remove Light Up Blocks
        if (lightBlocks != null)
        {
            for (int i = 0; i < lightBlocks.size(); i++)
            {
                if (lightBlocks.get(i) != null)
                {
                    for (final Location l : lightBlocks.get(i))
                    {
                        final Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        b.setType(gateShape.stargateMaterial);
                    }
                }
            }
        }
    }

}