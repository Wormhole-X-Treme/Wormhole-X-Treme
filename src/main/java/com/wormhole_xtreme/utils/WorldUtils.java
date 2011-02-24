package com.wormhole_xtreme.utils;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *  WormholeXTreme WorldUtils
 *  @author Ben Echols (Lologarithm) 
 */
public class WorldUtils 
{
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

	public static boolean isSameBlock(Block b1, Block b2)
	{
		if ( b1 == null || b2 == null )
			return false;
		
		return b1.getX() == b2.getX() && b1.getY() == b2.getY() &&	b1.getZ() == b2.getZ();
	}

	public static byte signFacingDataFromBlockFace(BlockFace bf)
	{
		switch ( bf )
		{
		case NORTH:
			return (byte)4;
		case SOUTH:
			return (byte)5;
		case EAST:
			return (byte)2;
		case WEST:
			return (byte)3;
		}
		
		return (byte)0;
	}
	
	public static byte leverFacingDataFromBlockFace(BlockFace bf)
	{
		switch ( bf )
		{
		case NORTH:
			return (byte)3;
		case SOUTH:
			return (byte)1;
		case EAST:
			return (byte)0;
		case WEST:
			return (byte)2;
		}
		
		return (byte)0;
	}

	public static void checkChunkLoad(Block b) 
	{
		World w = b.getWorld();
		Chunk c = b.getChunk();
		
		if ( w.isChunkLoaded(c) )
			w.loadChunk(c);
	}
}
