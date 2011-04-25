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
    // Used to parse
    /** The Loaded version. */
    public byte loadedVersion = -1;

    /** The Gate id. */
    public long gateId = -1;
    /** 
     *  Name of this gate, used to index and target.
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
    // Iris passcode (IDC_
    /** The Iris deactivation code. */
    public String irisDeactivationCode = "";
    // Is Iris active?
    /** The Iris active. */
    public boolean irisActive = false;
    // Default Iris setting
    /** The Iris default active. */
    public boolean irisDefaultActive = false;
    // Is this stargate already active? Can be active remotely and have no target of its own.
    /** The Active. */
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
    // Block to place stargate name
    /** The Name block holder. */
    public Block nameBlockHolder;
    /** Block that toggle the activation state of the gate
     * if nearby redstone is activated. 
     */
    public Block redstoneActivationBlock;
    /** Block that will toggle sign target when redstone nearby 
     * is activated.
     */
    public Block redstoneDialChangeBlock;

    /** The Teleport sign block. */
    public Block teleportSignBlock;
    // Sign to choose teleport target from (optional)
    /** The Teleport sign. */
    public Sign teleportSign;
    // Block to teleport from;
    /** The Teleport location. */
    public Location teleportLocation;

    // Direction that the stargate faces.
    /** The Facing. */
    public BlockFace facing;

    // The current target on the sign, only used if IsSignPowered is true
    /** The Sign target. */
    public Stargate signTarget;
    // Temp target id to store when loading gates.
    /** The temp_sign_target. */
    public long tempSignTarget = -1;
    // Index in network the sign is pointing at
    /** The Sign index. */
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

    /**
     * Instantiates a new stargate.
     */
    public Stargate()
    {

    }

    /*public Stargate(World w, String name, StargateNetwork network, byte[] gate_data)
	{
		this.Name = name;
		this.Network = network;
		ParseVersionedData(gate_data, w);
	}*/


    /**
     * Fill gate interior.
     *
     * @param m the m
     */
    public void fillGateInterior(Material m)
    {
        for( Location bc : this.portalBlocks )
        {
            Block b = myWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(m);
        }
    }

    /** The animation_step. */
    int animationStep = 1;
    boolean animationRemoving = false;
    /** The Animated blocks. */
    ArrayList<Block> animatedBlocks = new ArrayList<Block>();

    /**
     * Animate opening.
     */
    public void animateOpening()
    {
        final Material wooshMaterial = this.gateShape.portalMaterial;
        if (this.wooshBlocks != null && this.wooshBlocks.size() != 0) 
        {
            ArrayList<Location> wooshBlockStep = this.wooshBlocks.get(animationStep);
            if ( !animationRemoving )
            {
                if (  wooshBlockStep != null )
                {
                    for ( Location l : wooshBlockStep )
                    {
                        Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        animatedBlocks.add(b);
                        StargateManager.openingAnimationBlocks.put(l, b);
                        b.setType(wooshMaterial);
                    }

                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, this.name + " Woosh Adding: " + animationStep + " Woosh Block Size: " + wooshBlockStep.size() );
                }

                if ( this.wooshBlocks.size() == animationStep + 1)
                {
                    this.animationRemoving = true;
                } 
                else 
                {
                    animationStep++;
                }
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), this.gateShape.wooshTicks);

            }
            else
            {
                // remove in reverse order, if block is not a portal block!
                if ( wooshBlockStep != null)
                {

                    for ( Location l : wooshBlockStep )
                    {
                        Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        StargateManager.openingAnimationBlocks.remove(l, b);
                        animatedBlocks.remove(b);
                        b.setType(Material.AIR);
                    }
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, this.name + " Woosh Removing: " + animationStep + " Woosh Block Size: " + wooshBlockStep.size() );
                }
                
                // If this is the last step to animate, we now add all the portal blocks in.
                if ( animationStep == 1 )
                {
                    this.animationRemoving = false;
                    if (this.litGate && this.active) {
                        this.fillGateInterior(this.gateShape.portalMaterial);
                    }
                }
                else
                {
                    animationStep--;
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), this.gateShape.wooshTicks);
                }
            }
        }
        else 
        {
            if (this.active) {
                this.fillGateInterior(this.gateShape.portalMaterial);
            }
        }
    }

    /**
     * Activate stargate.
     */
    public void activateStargate() 
    {
        this.active = true;
    }

    /**
     * After activate stargate.
     */
    public void afterActivateStargate()
    {
        this.recentActive = true;
    }

    /** The current_lighting_iteration. */
    int current_lighting_iteration = 0;
    /**
     * Light stargate.
     */
    public void lightStargate()
    {
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Lighting up Order: " + this.current_lighting_iteration );
        this.litGate = true;
        this.current_lighting_iteration++;
        // Light up blocks
        if ( this.lightBlocks != null  )
        {
            if ( this.lightBlocks.get(this.current_lighting_iteration) != null )
            {
                for ( Location l : this.lightBlocks.get(this.current_lighting_iteration) )
                {
                    Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()); 
                    b.setType(this.gateShape.activeMaterial);
                }
            }

            if ( current_lighting_iteration >= lightBlocks.size() - 1)
            {
            	// Reset back to start
                this.current_lighting_iteration = 0;
                if (this.active)
                {
                    // Start up animation for woosh now!
                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH));
                }
            }
            else
            {
                // Keep lighting
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.LIGHTUP), this.gateShape.lightTicks);
            }
        }

    }

    /**
     * Start activation timer.
     *
     * @param p the p
     */
    public void startActivationTimer(Player p)
    {
        if ( this.activateTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(this.activateTaskId);
        }

        int timeout = ConfigManager.getTimeoutActivate() * 20;
        this.activateTaskId = WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, p, ActionToTake.DEACTIVATE), timeout);
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \""+ this.name + "\" ActivateTaskID \"" + this.activateTaskId + "\" created.");
    }

    /**
     * Stop activation timer.
     *
     * @param p the p
     */
    public void stopActivationTimer()
    {
        if ( this.activateTaskId > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \""+ this.name + "\" ActivateTaskID \"" + this.activateTaskId + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(this.activateTaskId);
            this.activateTaskId = -1;
        }
    }

    /**
     * Timeout stargate.
     *
     * @param p the p
     */
    public void timeoutStargate(Player p)
    {
        if ( this.activateTaskId > 0 )
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \""+ this.name + "\" ActivateTaskID \"" + this.activateTaskId + "\" timed out.");
            this.activateTaskId = -1;
        }
        // Deactivate if player still hasn't picked a target.
        Stargate s = null;
        if ( p != null)
            s = StargateManager.removeActivatedStargate(p);
        else
            s = this;

        // Only send a message if the gate was still in the remotely activated gates list.
        if ( s != null)
        {
            // Make sure to reset iris if it should be on.
            if ( this.irisDefaultActive ) 
            {
                toggleIrisActive(irisDefaultActive);
            }
            if ( this.litGate )
            {
                s.unLightStargate();
            }

            if ( p != null )
                p.sendMessage("Gate: " + this.name + " timed out and deactivated.");
        }
    }

    /**
     * De activate stargate.
     */
    public void deActivateStargate()
    {
        this.active = false;
    }

    /**
     * De recent activate stargate.
     */
    public void deRecentActivateStargate()
    {
        this.recentActive = false;
    }

    /**
     * Un light stargate.
     */
    public void unLightStargate()
    {
        this.litGate = false;

        // Remove Light Up Blocks
        if ( lightBlocks != null )
        {
            for ( int i = 0; i < lightBlocks.size(); i++ )
            {
                if ( lightBlocks.get(i) != null )
                {
                    for (  Location l : lightBlocks.get(i))
                    {
                        Block b = myWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()); 
                        b.setType(this.gateShape.stargateMaterial);
                    }
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
        if (WormholeXTreme.getWorldHandler() != null) {
            WormholeXTreme.getWorldHandler().addStickyChunk(this.teleportLocation.getBlock().getChunk(), "WormholeXTreme");
        }
        if ( this.shutdownTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(this.shutdownTaskId);
        }
        if (this.afterShutdownTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(this.afterShutdownTaskId);
        }

        int timeout = ConfigManager.getTimeoutShutdown() * 20;
        if ( timeout > 0 )
        {
            this.shutdownTaskId = WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.SHUTDOWN), timeout);
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + this.name + "\" ShutdownTaskID \"" + this.shutdownTaskId + "\" created." );
            if (this.shutdownTaskId == -1 ) 
            { 
                shutdownStargate();
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Failed to schdule wormhole shutdown timeout: " + timeout + " Received task id of -1. Wormhole forced closed NOW.");
            }
        }

        if ((this.shutdownTaskId > 0) || ( timeout == 0 ))
        {
            if ( !this.active ) 
            {
                this.activateStargate();
                this.dialButtonLeverState();
                this.deRecentActivateStargate();
            }
            if ( !this.litGate)
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
     * After shutdown of stargate, spawn off task to set RecentActive = false;
     * This way we can depend on RecentActive for gate fire/lava protection.
     */
    public void afterShutdown()
    {
        if (this.afterShutdownTaskId > 0)
        {
            WormholeXTreme.getScheduler().cancelTask(this.afterShutdownTaskId);
        }
        int timeout = 60;
        this.afterShutdownTaskId = WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this,ActionToTake.AFTERSHUTDOWN), timeout);
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + this.name + "\" AfterShutdownTaskID \"" + this.afterShutdownTaskId + "\" created." );
        if (this.afterShutdownTaskId == -1)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Failed to schdule wormhole after shutdown, received task id of -1.");
            this.deRecentActivateStargate();
        }
    }
    /**
     * This method takes in a remote stargate and dials it if it is not active.
     *
     * @param target the target
     * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate shutdowns.
     */
    public boolean dialStargate(Stargate target)
    {
        if ( this.activateTaskId > 0 )
        {
            WormholeXTreme.getScheduler().cancelTask(activateTaskId);
        }

        if ( !target.litGate )
        {
            WorldUtils.scheduleChunkLoad(target.activationBlock);
            this.target = target;
            this.dialStargate();
            target.dialStargate();
            if ((this.active) && (this.target.active)) 
            {
                return true;
            } 
            else if ((this.active) && (!this.target.active))
            {
                this.shutdownStargate();
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
            } 
            else if ((!this.active) && (target.active))
            {
                target.shutdownStargate();
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING,false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
            }
        }

        return false;
    }

    /**
     * This method takes in a remote stargate and dials it if it is not active.
     *
     * @param target the target
     * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate shutdowns.
     */
    public boolean forceDialStargate(Stargate target)
    {
        if ( this.activateTaskId > 0 )
        {
            WormholeXTreme.getScheduler().cancelTask(activateTaskId);
        }

        //if ( !target.LitGate )
        //{
        WorldUtils.scheduleChunkLoad(target.activationBlock);
        this.target = target;
        this.dialStargate();
        target.dialStargate();
        if ((this.active) && (target.active)) 
        {
            return true;
        } 
        else if ((this.active) && (!target.active))
        {
            this.shutdownStargate();
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
        } 
        else if ((!this.active) && (target.active))
        {
            target.shutdownStargate();
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING,false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
        }
        //}

        return false;
    }

    /**
     * Shutdown stargate.
     *
     * @param timer true if we want to spawn after shutdown timer.
     */
    public void shutdownStargate(boolean timer)
    {
        if ( this.shutdownTaskId > 0 )
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + this.name + "\" ShutdownTaskID \"" + this.shutdownTaskId + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(this.shutdownTaskId);
            this.shutdownTaskId = -1;
        }

        if ( this.target != null )
        {
            this.target.shutdownStargate();
        }

        this.target = null;
        if (timer)
        {
            this.afterActivateStargate();
        }		
        this.deActivateStargate();

        this.unLightStargate();
        this.dialButtonLeverState();
        // Only set back to air if iris isn't on.
        // If the iris should be on, we will make it that way.
        if ( this.irisDefaultActive )
        {
            toggleIrisActive(irisDefaultActive);
        }
        else if ( !this.irisActive )
        {
            this.fillGateInterior(Material.AIR);
        }

        if (timer)
        {
            this.afterShutdown();
        }
        WorldUtils.scheduleChunkUnload(this.teleportLocation.getBlock());
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
     * After shutdown stargate.
     */
    public void afterShutdownStargate()
    {
        if (this.afterShutdownTaskId > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Wormhole \"" + this.name + "\" AfterShutdownTaskID \"" + this.afterShutdownTaskId + "\" cancelled.");
            WormholeXTreme.getScheduler().cancelTask(this.afterShutdownTaskId);
            this.afterShutdownTaskId = -1;
        }
        this.deRecentActivateStargate();
    }

    /**
     * Complete gate.
     *
     * @param name the name
     * @param idc the idc
     */
    public void completeGate(String name, String idc)
    {
        this.name = name;

        // 1. Setup Name Sign
        if ( this.nameBlockHolder != null )
        {
            this.setupGateSign(true);
        }
        // 2. Set up Iris stuff
        setIrisDeactivationCode(idc);
    }


    /**
     * Setup or remove gate name sign.
     *
     * @param create true to create, false to destroy
     */
    public void setupGateSign(boolean create)
    {
        if (this.nameBlockHolder != null)
        {
            if (create)
            {
                Block nameSign = this.nameBlockHolder.getFace(facing);
                nameSign.setType(Material.WALL_SIGN);
                switch ( facing )
                {
                    case NORTH:
                        nameSign.setData((byte)0x04);
                        break;
                    case SOUTH:
                        nameSign.setData((byte)0x05);
                        break;
                    case EAST:
                        nameSign.setData((byte)0x02);
                        break;
                    case WEST:
                        nameSign.setData((byte)0x03);
                        break;
                    default:
                        break;
                }
                nameSign.getState().setData(new MaterialData(Material.WALL_SIGN));
                Sign sign = (Sign)nameSign.getState();
                sign.setLine(0, "-" + this.name + "-");

                if ( this.network != null )
                {
                    sign.setLine(1, "N:" + this.network.netName);
                }

                if ( this.owner != null )
                {
                    sign.setLine(2, "O:" + this.owner);
                }
                sign.update();
            }
            else
            {
                Block nameSign;
                if (( nameSign = this.nameBlockHolder.getFace(facing)) != null)
                {
                    nameSign.setType(Material.AIR);
                }
            }
        }
    }


    /**
     * Setup or remove IRIS control lever.
     *
     * @param create true for create, false for destroy.
     */
    public void setupIrisLever(boolean create)
    {
        if ( create )
        {
            Block iris_block = this.irisActivationBlock;
            if ( iris_block == null )
            {
                iris_block = this.activationBlock.getFace(BlockFace.DOWN);
                this.irisActivationBlock = iris_block;
            }
            this.stargateBlocks.add(irisActivationBlock.getLocation());

            this.irisActivationBlock.setType(Material.LEVER);
            switch (facing)
            {
                case SOUTH:
                    this.irisActivationBlock.setData((byte)0x01);
                    break;
                case NORTH:
                    this.irisActivationBlock.setData((byte)0x02);
                    break;
                case WEST:
                    this.irisActivationBlock.setData((byte)0x03);
                    break;
                case EAST:
                    this.irisActivationBlock.setData((byte)0x04);
                    break;
                default:
                    break;
            }
        }
        else
        {
            if ( this.irisActivationBlock != null )
            {
                stargateBlocks.remove(this.irisActivationBlock.getLocation());
                this.irisActivationBlock.setType(Material.AIR);
            }
        }

    }

    /**
     * Sets the iris deactivation code.
     *
     * @param idc the idc
     */
    public void setIrisDeactivationCode ( String idc )
    {
        // If empty string make sure to make lever area air instead of lever.
        if ( idc != null && !idc.equals(""))
        {
            this.irisDeactivationCode = idc;
            this.setupIrisLever(true);
        }
        else
        {
            this.toggleIrisActive(false);
            this.setupIrisLever(false);
            this.irisDeactivationCode = "";
        }
    }

    /**
     * This method should only be called when the Iris lever is hit.
     * This toggles the current state of the Iris and then sets that state to be the default.
     */
    public void toggleIrisDefault()
    {
        toggleIrisActive();
        this.irisDefaultActive = this.irisActive;
    }
    /**
     * Toggle the iris state. 
     */
    public void toggleIrisActive()
    {
        this.irisActive = !this.irisActive;
        this.toggleIrisActive(this.irisActive);
    }
    /**
     * This method sets the iris state and toggles the iris lever.
     * Smart enough to know if the gate is active and set the proper
     * material in its interior.
     *
     * @param irisactive true for iris on, false for off.
     */
    public void toggleIrisActive(boolean irisactive)
    {
        this.irisActive = irisactive;
        int leverstate = (int)this.irisActivationBlock.getData();
        if ( this.irisActive )
        {
            if (leverstate <= 4 && leverstate != 0)
            {
                leverstate = leverstate + 8;
            }
            this.fillGateInterior(this.gateShape.irisMaterial);
        }
        else
        {
            if (leverstate <= 12 && leverstate >= 9)
            {
                leverstate = leverstate - 8;
            }
            if (this.active)
            {
                this.fillGateInterior(this.gateShape.portalMaterial);
            }
            else
            {
                this.fillGateInterior(Material.AIR);
            }
        }
        if (this.irisActivationBlock != null && this.irisActivationBlock.getType() == Material.LEVER )
        {
            this.irisActivationBlock.setData((byte)leverstate);
        }
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
     * @param regenerate true, to replace missing activation lever.
     */
    public void dialButtonLeverState(boolean regenerate)
    {
        if (this.activationBlock != null)
        {
            Material material = this.activationBlock.getType();
            if (regenerate)
            {
                if (material != Material.LEVER && material != Material.STONE_BUTTON)
                {
                    this.activationBlock.setType(Material.LEVER);
                    switch (facing)
                    {
                        case SOUTH:
                            this.activationBlock.setData((byte)0x01);
                            break;
                        case NORTH:
                            this.activationBlock.setData((byte)0x02);
                            break;
                        case WEST:
                            this.activationBlock.setData((byte)0x03);
                            break;
                        case EAST:
                            this.activationBlock.setData((byte)0x04);
                            break;   
                        default:
                            break;
                    }
                }
                material = this.activationBlock.getType();
            }
            if (material == Material.LEVER || material == Material.STONE_BUTTON)
            {
                int state = (int)this.activationBlock.getData();
                if (material == Material.STONE_BUTTON)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Automaticially replaced Button on gate \"" + this.name + "\" with Lever.");
                    this.activationBlock.setType(Material.LEVER);
                    this.activationBlock.setData((byte)state);
                }
                if (this.active)
                {
                    if (state <= 4 && state != 0)
                    {
                        state = state + 8;
                    }
                }
                else
                {
                    if (state <= 12 && state >= 9)
                    {
                        state = state - 8;
                    }
                }

                this.activationBlock.setData((byte)state);
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Dial Button Lever Gate: \"" + this.name + "\" Material: \"" + material.toString() + "\" State: \"" + state + "\"");
                }
            }
        }
    }

    // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
    //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks


    /**
     * Try click teleport sign. This is the same as {@link Stargate#tryClickTeleportSign(Block, Player)} with Player set to null.
     *
     * @param clicked the clicked
     * @return true, if successful
     */
    public boolean tryClickTeleportSign(Block clicked)
    {
        return tryClickTeleportSign(clicked, null);
    }

    /**
     * Try click teleport sign.
     *
     * @param clicked the clicked
     * @param player the player
     * @return true, if successful
     */
    public boolean tryClickTeleportSign(Block clicked, Player player) 
    {
        if ( teleportSign == null && teleportSignBlock != null )
        {
            if ( teleportSignBlock.getType() == Material.WALL_SIGN )
            {
                this.signIndex = -1;
                teleportSign = (Sign)teleportSignBlock.getState();
                WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, player, ActionToTake.SIGNCLICK));
            }
        }
        else if ( WorldUtils.isSameBlock(clicked, teleportSignBlock) )
        {
            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(this, player, ActionToTake.SIGNCLICK));
            return true;
        }

        return false;
    }

    /** The gate_order. */
    final private HashMap<Integer, Stargate> signGateOrder = new HashMap<Integer,Stargate>();

    /**
     * Teleport sign clicked.
     */
    public void teleportSignClicked()
    {
        synchronized ( network.gateLock )
        {
            if ( this.signIndex == -1 )
            {
                this.teleportSign.setLine(0, "-" + this.name + "-");
                this.signIndex++;
            }
            if ( this.network.signGateList.size() == 0 || this.network.signGateList.size() == 1)
            {
                this.teleportSign.setLine(1, "");
                this.teleportSign.setLine(2, "No Other Gates");
                this.teleportSign.setLine(3, "");
                this.signTarget = null;
                return;
            }

            if ( signIndex >= this.network.signGateList.size() )
                signIndex = 0;

            if ( this.network.signGateList.get(signIndex).name.equals(this.name) )
            {
                signIndex++;
                if ( signIndex == this.network.signGateList.size() )
                    signIndex = 0;
            }

            if ( this.network.signGateList.size() == 2 )
            {
                signGateOrder.clear();
                signGateOrder.put(Integer.valueOf(2), this.network.signGateList.get(signIndex));


                this.teleportSign.setLine(1, "");
                this.teleportSign.setLine(2, ">" + signGateOrder.get(Integer.valueOf(2)).name + "<");
                this.teleportSign.setLine(3, "");
                this.signTarget = this.network.signGateList.get(signIndex);
            }
            else if ( this.network.signGateList.size() == 3 )
            {
                signGateOrder.clear();
                int orderIndex = 1;
                //SignIndex++;
                while ( signGateOrder.size() < 2)
                {
                    if ( signIndex >= this.network.signGateList.size() )
                        signIndex = 0;

                    if ( this.network.signGateList.get(signIndex).name.equals(this.name) )
                    {
                        signIndex++;
                        if ( signIndex == this.network.signGateList.size() )
                            signIndex = 0;
                    }


                    signGateOrder.put(Integer.valueOf(orderIndex), this.network.signGateList.get(signIndex));
                    orderIndex++;
                    if ( orderIndex == 4)
                        orderIndex = 1;
                    signIndex++;
                }

                this.teleportSign.setLine(1, signGateOrder.get(Integer.valueOf(1)).name);
                this.teleportSign.setLine(2, ">" + signGateOrder.get(Integer.valueOf(2)).name + "<");
                this.teleportSign.setLine(3, "");

                this.signTarget = signGateOrder.get(Integer.valueOf(2));
                this.signIndex = network.signGateList.indexOf(signGateOrder.get(Integer.valueOf(2)));
            }		
            else
            {
                signGateOrder.clear();
                int orderIndex = 1;
                while ( signGateOrder.size() < 3)
                {
                    if ( signIndex == this.network.signGateList.size() )
                        signIndex = 0;

                    if ( this.network.signGateList.get(signIndex).name.equals(this.name) )
                    {
                        signIndex++;
                        if ( signIndex == this.network.signGateList.size() )
                            signIndex = 0;
                    }

                    signGateOrder.put(Integer.valueOf(orderIndex), this.network.signGateList.get(signIndex));
                    orderIndex++;

                    signIndex++;
                }

                this.teleportSign.setLine(1, signGateOrder.get(Integer.valueOf(3)).name);
                this.teleportSign.setLine(2, ">" + signGateOrder.get(Integer.valueOf(2)).name + "<");
                this.teleportSign.setLine(3, signGateOrder.get(Integer.valueOf(1)).name);

                this.signTarget = signGateOrder.get(Integer.valueOf(2));
                this.signIndex = network.signGateList.indexOf(signGateOrder.get(Integer.valueOf(2)));
            }
        }

        this.teleportSign.setData(this.teleportSign.getData());		
        this.teleportSign.update(true);
    }

    /**
     * Delete gate blocks.
     */
    public void deleteGateBlocks()
    {
        for( Location bc : this.stargateBlocks )
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
        for( Location bc : this.portalBlocks )
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
        if (this.teleportSignBlock != null && this.teleportSign != null)
        {
            final Block teleportSign = this.teleportSignBlock.getFace(facing);
            teleportSign.setType(Material.AIR);
        }
    }

    /**
     * Reset teleport sign.
     */
    public void resetTeleportSign()
    {
        if ( this.teleportSignBlock != null && this.teleportSign != null)
        {
            this.teleportSign.setLine(0, this.name );
            if (this.network != null)
            {
                this.teleportSign.setLine(1, this.network.netName );
            }
            else 
            {
                this.teleportSign.setLine(1, "");
            }
            this.teleportSign.setLine(2, "");
            this.teleportSign.setLine(3, "");
            this.teleportSign.setData(this.teleportSign.getData());
            this.teleportSign.update();
        }

    }


}