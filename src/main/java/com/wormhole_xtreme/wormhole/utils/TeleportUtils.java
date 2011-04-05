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
        final Stargate s = stargate;
        Location location = s.teleportLocation;
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Unsafe location: " + location);
        final double tlyaxis = Math.floor(s.teleportLocation.getY());
        final double abyaxis = Math.floor(s.activationBlock.getY());
        if (tlyaxis != abyaxis && (tlyaxis <= abyaxis - 6.0 || tlyaxis >= abyaxis + 6.0))
        {
            location.setY(abyaxis - 1.0);
        }
        else
        {
            location.setY(tlyaxis);
        }
        
        location = findSafe(location,3,3,s);
        
        if (location != s.teleportLocation && Math.floor(location.getX()) == Math.floor(s.teleportLocation.getX()) && Math.floor(location.getZ()) == Math.floor(s.teleportLocation.getZ()))
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Unclean safe location: " + location);
            location.setPitch(s.teleportLocation.getPitch());
            location.setYaw(s.teleportLocation.getYaw());
            location.setX(s.teleportLocation.getX());
            location.setZ(s.teleportLocation.getZ());
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Cleaned safe location: " + location);
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Unclean fallback location: " + location);
            location.setPitch(s.teleportLocation.getPitch());
            location.setYaw(s.teleportLocation.getYaw());
            location.setX(Math.floor(location.getX()));
            location.setZ(Math.floor(location.getZ()));
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Cleaned fallback location: " + location);
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
        Block initialBlock = location.getBlock();
        Material initialMaterial = initialBlock.getType();
        boolean retry = true;
        Location finalLocation = null;
        Block tempBlockOne = null;
        Material tempMaterialOne = null;
        Block tempBlockTwo = null;
        Material tempMaterialTwo = null;
        int i = 0;
        while (retry)
        {
            i++;
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "iteration=" + iteration + " i=" + i);

            int secondStageDistance = 0;
            int firstStageDistance = 0;
            boolean up = false;
            boolean safe = false;
            boolean down = false;

            int secondStageDownState = 0;

            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "initial material=" + initialMaterial);
            if (initialMaterial == Material.AIR || initialMaterial == Material.WATER || initialMaterial == Material.RAILS)
            {
                // Start block is a safe block. Iterate downwards until a non-safe block is found (or max is hit)
                // if non-safe block is found immediately below safe block, iterate up once from safe block
                // to check if there is headroom. Set return value to bottom-most safe block.
                while (firstStageDistance <= max * 3 && !up)
                {
                    if (firstStageDistance == 0)
                    {
                        tempBlockOne = initialBlock;
                        tempMaterialOne = initialMaterial;
                    }
                    else 
                    {
                        tempBlockOne = initialBlock.getFace(BlockFace.DOWN,firstStageDistance);
                        tempMaterialOne = tempBlockOne.getType();
                    }
                    if (tempMaterialOne == Material.AIR || tempMaterialOne == Material.WATER || tempMaterialOne == Material.RAILS)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "1st down="+ firstStageDistance + " material=" + tempMaterialOne);
                        firstStageDistance++;
                    }
                    else
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "1st break=" + firstStageDistance );
                        up = true;
                    }
                }
            }

            // Start block is not safe. Iterate upwards until safe block is found (or max is hit). Once safe block is
            // found, iterate once more to make sure it has head room. Return bottom-most safe block.
            // If no safe block is found, start iterating downwards from initial block location. If safe block is found
            // start iterating downards until non-safe block is found.
            while (secondStageDistance <= max && !safe)
            {
                if (up)
                {
                    tempBlockOne = initialBlock.getFace(BlockFace.DOWN,firstStageDistance);
                    tempMaterialOne = tempBlockOne.getType();
                    up = false;
                    down = true;
                }
                else if (secondStageDistance == 0)
                {
                    tempBlockOne = initialBlock;
                    tempMaterialOne = initialMaterial;
                }
                else 
                {
                    if (down)
                    {
                        if (firstStageDistance - secondStageDistance > 0)
                        {
                            tempBlockOne = initialBlock.getFace(BlockFace.DOWN,firstStageDistance - secondStageDistance);
                            tempMaterialOne = tempBlockOne.getType();
                            secondStageDownState = 1;
                        } 
                        else if (firstStageDistance - secondStageDistance == 0)
                        {
                            tempBlockOne = initialBlock;
                            tempMaterialOne = initialMaterial;
                            secondStageDownState = 2;
                        }
                        else
                        {
                            tempBlockOne = initialBlock.getFace(BlockFace.UP,secondStageDistance - firstStageDistance);
                            tempMaterialOne = tempBlockOne.getType();
                            secondStageDownState = 3;
                        }
                    }
                    else
                    {
                        tempBlockOne = initialBlock.getFace(BlockFace.UP,secondStageDistance);
                        tempMaterialOne = tempBlockOne.getType();
                    }
                }
                if (tempMaterialOne != Material.WATER && tempMaterialOne != Material.RAILS && tempMaterialOne != Material.AIR)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "2nd up="+ secondStageDistance + " material=" + tempMaterialOne);
                    secondStageDistance++;
                }
                else
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "2nd break=" + secondStageDistance );
                    safe = true;
                } 
            }
            if (safe)
            {
                if (down)
                {
                    if (secondStageDownState == 1)
                    {
                        tempBlockOne = initialBlock.getFace(BlockFace.DOWN,(firstStageDistance - secondStageDistance) - 1);
                        tempMaterialOne = tempBlockOne.getType();
                        tempBlockTwo = tempBlockOne.getFace(BlockFace.DOWN, 2);
                        tempMaterialTwo = tempBlockTwo.getType();
                        if ((tempMaterialOne == Material.AIR || tempMaterialOne == Material.WATER) && (tempMaterialTwo != Material.AIR && tempMaterialTwo != Material.WATER))
                        {
                            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Returning down based safe location, downstate: " + secondStageDownState);
                            finalLocation = initialBlock.getFace(BlockFace.DOWN,firstStageDistance - secondStageDistance).getLocation();
                            retry = false;
                        }
                        else
                        {
                            initialBlock = tempBlockOne;
                            initialMaterial = tempMaterialOne;
                        }
                    }
                    else if (secondStageDownState == 2)
                    {
                        tempBlockOne = initialBlock.getFace(BlockFace.UP,1);
                        tempMaterialOne = tempBlockOne.getType();
                        tempBlockTwo = tempBlockOne.getFace(BlockFace.DOWN, 2);
                        tempMaterialTwo = tempBlockTwo.getType();
                        if ((tempMaterialOne == Material.AIR || tempMaterialOne == Material.WATER) && (tempMaterialTwo != Material.AIR && tempMaterialTwo != Material.WATER))
                        {
                            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Returning down based safe location, downstate: " + secondStageDownState);
                            finalLocation = initialBlock.getLocation();
                            retry = false;
                        }
                        else
                        {
                            initialBlock = tempBlockOne;
                            initialMaterial = tempMaterialOne;
                        }
                    }
                    else if (secondStageDownState == 3)
                    {
                        tempBlockOne = initialBlock.getFace(BlockFace.UP, (secondStageDistance - firstStageDistance) + 1);
                        tempMaterialOne = tempBlockOne.getType();
                        tempBlockTwo = tempBlockOne.getFace(BlockFace.DOWN, 2);
                        tempMaterialTwo = tempBlockTwo.getType();
                        if ((tempMaterialOne == Material.AIR || tempMaterialOne == Material.WATER) && (tempMaterialTwo != Material.AIR && tempMaterialTwo != Material.WATER))
                        {
                            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Returning down based safe location, downstate: " + secondStageDownState);
                            finalLocation = initialBlock.getFace(BlockFace.UP, secondStageDistance - firstStageDistance).getLocation();
                            retry = false;
                        }
                        else
                        {
                            initialBlock = tempBlockOne;
                            initialMaterial = tempMaterialOne;
                        }
                    }
                }
                else
                {
                    tempBlockOne = initialBlock.getFace(BlockFace.UP, secondStageDistance + 1);
                    tempMaterialOne = tempBlockOne.getType();
                    tempBlockTwo = tempBlockOne.getFace(BlockFace.DOWN, 2);
                    tempMaterialTwo = tempBlockTwo.getType();
                    if ((tempMaterialOne == Material.AIR || tempMaterialOne == Material.WATER) && (tempMaterialTwo != Material.AIR && tempMaterialTwo != Material.WATER))
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Returning up based safe location." );
                        finalLocation = initialBlock.getFace(BlockFace.UP,secondStageDistance).getLocation();
                        retry = false;
                    }
                    else
                    {
                        initialBlock = tempBlockOne;
                        initialMaterial = tempMaterialOne;
                    }
                }
            }
            else
            {
                initialBlock = tempBlockOne;
                initialMaterial = tempMaterialOne;
            }
            if (i == iteration || iteration == 0)
            {
                retry = false;
            }
        }
        if (finalLocation != null)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Safe location returned.");
            return finalLocation;
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "No Safe location found, returned front of DHD activation block.");
            return stargate.activationBlock.getLocation();
        }

    }
}
