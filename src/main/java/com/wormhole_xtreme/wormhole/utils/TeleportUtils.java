/**
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
package com.wormhole_xtreme.wormhole.utils;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.model.Stargate;

// TODO: Auto-generated Javadoc
/**
 * The Class TeleportUtils.
 *
 * @author alron
 */
public class TeleportUtils {

    /**
     * Find safe teleport location from stargate.
     * Will default to default teleport location associated with stargate if unable.
     *
     * @param stargate The unsafe stargate to find a location from.
     * @return The safe stading location (probably)
     */
    public static Location findSafeTeleportFromStargate(Stargate stargate)
    {
        Location location = stargate.TeleportLocation;
        WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Unsafe location: " + location);
        double tlyaxis = Math.floor(stargate.TeleportLocation.getY());
        double abyaxis = Math.floor(stargate.ActivationBlock.getY());
        if (tlyaxis != abyaxis && (tlyaxis <= abyaxis - 6.0 || tlyaxis >= abyaxis + 6.0))
        {
            location.setY(abyaxis - 1.0);
        }
        else
        {
            location.setY(tlyaxis);
        }
        
        location = findSafe(location,3,3,stargate);
        
        if (location != stargate.TeleportLocation && Math.floor(location.getX()) == Math.floor(stargate.TeleportLocation.getX()) && Math.floor(location.getZ()) == Math.floor(stargate.TeleportLocation.getZ()))
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Unclean safe location: " + location);
            location.setPitch(stargate.TeleportLocation.getPitch());
            location.setYaw(stargate.TeleportLocation.getYaw());
            location.setX(stargate.TeleportLocation.getX());
            location.setZ(stargate.TeleportLocation.getZ());
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Cleaned safe location: " + location);
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Unclean fallback location: " + location);
            location.setPitch(stargate.TeleportLocation.getPitch());
            location.setYaw(stargate.TeleportLocation.getYaw());
            location.setX(Math.floor(location.getX()));
            location.setZ(Math.floor(location.getZ()));
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Cleaned fallback location: " + location);
        }
        return location;
    }
    

    /**
     * Find safe.
     *
     * @param location The initial location to start our checks on.
     * @param max The max block distance to run our checks, per iteration.
     * @param iteration The number of iterations we will run the safe check.
     * @param stargate The destination stargate.
     * @return The safe location, if no safe location is found, we use the top of the DHD Activation Block.
     */
    private static Location findSafe(Location location, int max, int iteration, Stargate stargate)
    {
        Block initialblock = location.getBlock();
        Material initialmaterial = initialblock.getType();
        boolean retry = true;
        Location finallocation = null;
        int i = 0;
        while (retry)
        {
            i++;
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "iteration=" + iteration + " i=" + i);
            Block tempblock = null;
            Material tempmaterial = null;
            int secondstagedistance = 0;
            int firststagedistance = 0;
            boolean up = false;
            boolean safe = false;
            boolean down = false;

            int secondstagedownstate = 0;

            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "initial material=" + initialmaterial);
            if (initialmaterial == Material.AIR || initialmaterial == Material.WATER || initialmaterial == Material.RAILS)
            {
                // Start block is a safe block. Iterate downwards until a non-safe block is found (or max is hit)
                // if non-safe block is found immediately below safe block, iterate up once from safe block
                // to check if there is headroom. Set return value to bottom-most safe block.
                while (firststagedistance <= max && !up)
                {
                    if (firststagedistance == 0)
                    {
                        tempblock = initialblock;
                        tempmaterial = initialmaterial;
                    }
                    else 
                    {
                        tempblock = initialblock.getFace(BlockFace.DOWN,firststagedistance);
                        tempmaterial = tempblock.getType();
                    }
                    if (tempmaterial == Material.AIR || tempmaterial == Material.WATER || tempmaterial == Material.RAILS)
                    {
                        WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "1st down="+ firststagedistance + " material=" + tempmaterial);
                        firststagedistance++;
                    }
                    else
                    {
                        WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "1st break=" + firststagedistance );
                        up = true;
                    }
                }
            }

            // Start block is not safe. Iterate upwards until safe block is found (or max is hit). Once safe block is
            // found, iterate once more to make sure it has head room. Return bottom-most safe block.
            // If no safe block is found, start iterating downwards from initial block location. If safe block is found
            // start iterating downards until non-safe block is found.
            while (secondstagedistance <= max && !safe)
            {
                if (up)
                {
                    tempblock = initialblock.getFace(BlockFace.DOWN,firststagedistance);
                    tempmaterial = tempblock.getType();
                    up = false;
                    down = true;
                }
                else if (secondstagedistance == 0)
                {
                    tempblock = initialblock;
                    tempmaterial = initialmaterial;
                }
                else 
                {
                    if (down)
                    {
                        if (firststagedistance - secondstagedistance > 0)
                        {
                            tempblock = initialblock.getFace(BlockFace.DOWN,firststagedistance - secondstagedistance);
                            tempmaterial = tempblock.getType();
                            secondstagedownstate = 1;
                        } 
                        else if (firststagedistance - secondstagedistance == 0)
                        {
                            tempblock = initialblock;
                            tempmaterial = initialmaterial;
                            secondstagedownstate = 2;
                        }
                        else
                        {
                            tempblock = initialblock.getFace(BlockFace.UP,secondstagedistance - firststagedistance);
                            tempmaterial = tempblock.getType();
                            secondstagedownstate = 3;
                        }
                    }
                    else
                    {
                        tempblock = initialblock.getFace(BlockFace.UP,secondstagedistance);
                        tempmaterial = tempblock.getType();
                    }
                }
                if (tempmaterial != Material.WATER && tempmaterial != Material.RAILS && tempmaterial != Material.AIR)
                {
                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "2nd up="+ secondstagedistance + " material=" + tempmaterial);
                    secondstagedistance++;
                }
                else
                {
                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "2nd break=" + secondstagedistance );
                    safe = true;
                } 
            }
            if (safe)
            {
                if (down)
                {
                    if (secondstagedownstate == 1)
                    {
                        tempblock = initialblock.getFace(BlockFace.DOWN,(firststagedistance - secondstagedistance) - 1);
                        tempmaterial = tempblock.getType();
                        if (tempmaterial == Material.AIR || tempmaterial == Material.WATER)
                        {
                            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Returning down based safe location, downstate: " + secondstagedownstate);
                            finallocation = initialblock.getFace(BlockFace.DOWN,firststagedistance - secondstagedistance).getLocation();
                            retry = false;
                        }
                        else
                        {
                            initialblock = tempblock;
                            initialmaterial = tempmaterial;
                        }
                    }
                    else if (secondstagedownstate == 2)
                    {
                        tempblock = initialblock.getFace(BlockFace.UP,1);
                        tempmaterial = tempblock.getType();
                        if (tempmaterial == Material.AIR || tempmaterial == Material.WATER)
                        {
                            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Returning down based safe location, downstate: " + secondstagedownstate);
                            finallocation = initialblock.getLocation();
                            retry = false;
                        }
                        else
                        {
                            initialblock = tempblock;
                            initialmaterial = tempmaterial;
                        }
                    }
                    else if (secondstagedownstate == 3)
                    {
                        tempblock = initialblock.getFace(BlockFace.UP, (secondstagedistance - firststagedistance) + 1);
                        tempmaterial = tempblock.getType();
                        if (tempmaterial == Material.AIR || tempmaterial == Material.WATER)
                        {
                            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Returning down based safe location, downstate: " + secondstagedownstate);
                            finallocation = initialblock.getFace(BlockFace.UP, secondstagedistance - firststagedistance).getLocation();
                            retry = false;
                        }
                        else
                        {
                            initialblock = tempblock;
                            initialmaterial = tempmaterial;
                        }
                    }
                }
                else
                {
                    tempblock = initialblock.getFace(BlockFace.UP, secondstagedistance + 1);
                    tempmaterial = tempblock.getType();
                    if (tempmaterial == Material.AIR || tempmaterial == Material.WATER)
                    {
                        WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Returning up based safe location." );
                        finallocation = initialblock.getFace(BlockFace.UP,secondstagedistance).getLocation();
                        retry = false;
                    }
                    else
                    {
                        initialblock = tempblock;
                        initialmaterial = tempmaterial;
                    }
                }
            }
            else
            {
                initialblock = tempblock;
                initialmaterial = tempmaterial;
            }
            if (i == iteration || iteration == 0)
            {
                retry = false;
            }
        }
        if (finallocation != null)
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Safe location returned.");
            return finallocation;
        }
        else
        {
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "No Safe location found, returned front of DHD activation block.");
            return stargate.ActivationBlock.getLocation();
        }

    }
}
