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
package com.wormhole_xtreme.wormhole.utils;

import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.wormhole_xtreme.wormhole.WormholeXTreme;

// TODO: Auto-generated Javadoc
/**
 * WormholeXTreme WorldUtils.
 *
 * @author Ben Echols (Lologarithm)
 */
public class WorldUtils 
{
	
	/**
	 * Gets the inverse direction.
	 *
	 * @param bf the bf
	 * @return the inverse direction
	 */
	public static BlockFace getInverseDirection(BlockFace bf)
	{
		switch ( bf )
		{
			case NORTH:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.NORTH;
			case EAST:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.EAST;
			case NORTH_EAST:
				return BlockFace.SOUTH_WEST;
			case SOUTH_WEST:
				return BlockFace.NORTH_EAST;
			case NORTH_WEST:
				return BlockFace.SOUTH_EAST;
			case SOUTH_EAST:
				return BlockFace.NORTH_WEST;
			case UP:
				return BlockFace.DOWN;
			case DOWN:
				return BlockFace.UP;
			default:
				return bf;
		}
	}
	
	/**
	 * Gets the perpendicular right direction.
	 *
	 * @param bf the bf
	 * @return the perpendicular right direction
	 */
	public static BlockFace getPerpendicularRightDirection(BlockFace bf)
	{
		switch ( bf )
		{
			case NORTH:
				return BlockFace.EAST;
			case SOUTH:
				return BlockFace.WEST;
			case EAST:
				return BlockFace.SOUTH;
			case WEST:
				return BlockFace.NORTH;
			case NORTH_EAST:
				return BlockFace.SOUTH_EAST;
			case SOUTH_WEST:
				return BlockFace.NORTH_WEST;
			case NORTH_WEST:
				return BlockFace.NORTH_EAST;
			case SOUTH_EAST:
				return BlockFace.SOUTH_WEST;
			case UP:
				return BlockFace.EAST;
			case DOWN:
				return BlockFace.WEST;
			default:
				return bf;
		}
	}

	/**
	 * Gets the degrees from block face.
	 *
	 * @param bf the bf
	 * @return the degrees from block face
	 */
	public static Float getDegreesFromBlockFace(BlockFace bf)
	{
		if ( bf == BlockFace.NORTH )
			return (float) 90;
		else if ( bf == BlockFace.EAST )
			return (float) 180;
		else if ( bf == BlockFace.SOUTH )
			return (float) 270;
		else if ( bf == BlockFace.WEST )
			return (float) 0;
		
		return (float) 0; 
	}

	/**
	 * Checks if is same block.
	 *
	 * @param b1 the b1
	 * @param b2 the b2
	 * @return true, if is same block
	 */
	public static boolean isSameBlock(Block b1, Block b2)
	{
		if ( b1 == null || b2 == null )
			return false;
		
		return b1.getX() == b2.getX() && b1.getY() == b2.getY() &&	b1.getZ() == b2.getZ();
	}

//	/**
//	 * Sign facing data from block face.
//	 *
//	 * @param bf the bf
//	 * @return the byte
//	 */
//	public static byte signFacingDataFromBlockFace(BlockFace bf)
//	{
//		switch ( bf )
//		{
//		case NORTH:
//			return (byte)4;
//		case SOUTH:
//			return (byte)5;
//		case EAST:
//			return (byte)2;
//		case WEST:
//			return (byte)3;
//		}
//		
//		return (byte)0;
//	}
//	
//	/**
//	 * Lever facing data from block face.
//	 *
//	 * @param bf the bf
//	 * @return the byte
//	 */
//	public static byte leverFacingDataFromBlockFace(BlockFace bf)
//	{
//		switch ( bf )
//		{
//		case NORTH:
//			return (byte)3;
//		case SOUTH:
//			return (byte)1;
//		case EAST:
//			return (byte)0;
//		case WEST:
//			return (byte)2;
//		}
//		
//		return (byte)0;
//	}

	/**
	 * Check chunk load.
	 *
	 * @param b the b
	 */
	public static void checkChunkLoad(Block b) 
	{
		World w = b.getWorld();
		Chunk c = b.getChunk();
		
		if ( !w.isChunkLoaded(c) )
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Loading chunk: " + c.toString() + " on: " + w.toString());
			w.loadChunk(c);
		}
	}
}
