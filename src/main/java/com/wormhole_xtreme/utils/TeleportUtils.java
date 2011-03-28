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
package com.wormhole_xtreme.utils;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.model.Stargate;

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
    public static Location FindSafeTeleportFromStargate(Stargate stargate)
    {
        Location location = stargate.TeleportLocation;
        double tlyaxis = Math.floor(stargate.TeleportLocation.getY());
        double abyaxis = Math.floor(stargate.ActivationBlock.getY());
        if (tlyaxis != abyaxis && (tlyaxis >= abyaxis - 3.0 || tlyaxis <= abyaxis + 6.0))
        {
            location.setY(abyaxis - 1.0);
        }
        else
        {
            location.setY(tlyaxis);
        }
        
        location = findSafeUp(findSafeDown(location, 3),6);
        return location;
    }
    
    /**
     * Find safe standing location downwards.
     *
     * @param location The unsafe location.
     * @param max The max depth.
     * @return The safe location if one can be found within max depth, otherwise returns initial unsafe location.
     */
    private static Location findSafeDown(Location location, int max)
    {
        if (location != null && max <= 32)
        {
            Block block = location.getBlock();
            Material material = block.getType();
            int i = 0;
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "down=0 material=" + material);
            while (i < max && (material != Material.AIR && material != Material.WATER && material != Material.RAILS))
            {
                block = block.getFace(BlockFace.DOWN);
                material = block.getType();
                i++;
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "down="+ i + " material=" + material);
            }
            if (i < max)
            {
                Block secondblock = block;
                while (i < max && (material == Material.AIR || material == Material.WATER || material == Material.RAILS))
                {
                    block = secondblock;
                    secondblock = secondblock.getFace(BlockFace.DOWN);
                    material = secondblock.getType();
                    i++;
                    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "down="+ i + " material=" + material);
                }
                i=i - 1;
            }
            if (i != max && i != 0)
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Unafe location: " + location);
                location.setY(Math.floor(location.getY()) - i);
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Safe location Down: " + location);
            }
        }
        return location;
    }
    
    /**
     * Find safe standing location upwards.
     *
     * @param location The unsafe location.
     * @param max The max height.
     * @return The safe location if one can be found within max height, otherwise returns initial unsafe location.
     */
    private static Location findSafeUp(Location location, int max)
    {
        if (location != null && max <= 32)
        {
            Block block = location.getBlock();
            Material material = block.getType();
            int i = 0;
            WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "up=0 material=" + material);
            while (i < max && (material != Material.AIR && material != Material.WATER && material != Material.RAILS))
            {
                block = block.getFace(BlockFace.UP);
                material = block.getType();
                i++;
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "up="+ i + " material=" + material);
            }
            if (i != max && i != 0)
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Unafe location: " + location);
                location.setY(location.getY() + i);
                WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Safe location Up: " + location);
            }
            else
            {
                location.setY(location.getY());
            }
        }
        return location;
    }
}
