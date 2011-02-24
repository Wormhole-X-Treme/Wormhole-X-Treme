package com.wormhole_xtreme.utils;

import java.nio.ByteBuffer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *  WormholeXTreme DataUtils
 *  @author Ben Echols (Lologarithm) 
 */
public class DataUtils 
{

	public static byte[] LocationToBytes(Location l)
	{
		ByteBuffer b = ByteBuffer.allocate(32);
		b.putDouble(l.getX());
		b.putDouble(l.getY());
		b.putDouble(l.getZ());
		b.putFloat(l.getPitch());
		b.putFloat(l.getYaw());

		return b.array();
	}
	
	public static byte[] BlockToBytes(Block b)
	{
		ByteBuffer bb = ByteBuffer.allocate(12);
		
		bb.putInt(b.getX());
		bb.putInt(b.getY());
		bb.putInt(b.getZ());
		
		return bb.array();
	}

	public static byte[] BlockLocationToBytes(Location l)
	{
		ByteBuffer bb = ByteBuffer.allocate(12);
		
		bb.putInt(l.getBlockX());
		bb.putInt(l.getBlockY());
		bb.putInt(l.getBlockZ());
		
		return bb.array();
	}

	
	public static Block BlockFromBytes(byte[] bytes, World w)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);
		return w.getBlockAt( b.getInt(), b.getInt(), b.getInt() );
	}
	
	public static Location LocationFromBytes(byte[] bytes, World w)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);
		return new Location(w, b.getDouble(), b.getDouble(), b.getDouble(), b.getFloat(), b.getFloat());
	}
	
	public static final byte[] intToByteArray(int value) 
	{
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	
	public static final int byteArrayToInt(byte [] b, int index) 
	{
        return (b[index] << 24)
                + ((b[index + 1] & 0xFF) << 16)
                + ((b[index + 2] & 0xFF) << 8)
                + (b[index + 3] & 0xFF);
	}
	
	public static final boolean byteToBoolean(byte b)
	{
		if ( b >= 1 )
			return true;
		else
			return false;
	}
}