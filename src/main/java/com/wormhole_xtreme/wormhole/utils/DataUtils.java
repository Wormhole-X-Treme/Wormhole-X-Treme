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

import java.nio.ByteBuffer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

// TODO: Auto-generated Javadoc
/**
 * WormholeXTreme DataUtils.
 *
 * @author Ben Echols (Lologarithm)
 */
public class DataUtils 
{

	/**
	 * Location to bytes.
	 *
	 * @param l the l
	 * @return the byte[]
	 */
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
	
	/**
	 * Block to bytes.
	 *
	 * @param b the b
	 * @return the byte[]
	 */
	public static byte[] BlockToBytes(Block b)
	{
		ByteBuffer bb = ByteBuffer.allocate(12);
		
		bb.putInt(b.getX());
		bb.putInt(b.getY());
		bb.putInt(b.getZ());
		
		return bb.array();
	}

	/**
	 * Block location to bytes.
	 *
	 * @param l the l
	 * @return the byte[]
	 */
	public static byte[] BlockLocationToBytes(Location l)
	{
		ByteBuffer bb = ByteBuffer.allocate(12);
		
		bb.putInt(l.getBlockX());
		bb.putInt(l.getBlockY());
		bb.putInt(l.getBlockZ());
		
		return bb.array();
	}

	
	/**
	 * Block from bytes.
	 *
	 * @param bytes the bytes
	 * @param w the w
	 * @return the block
	 */
	public static Block BlockFromBytes(byte[] bytes, World w)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);
		return w.getBlockAt( b.getInt(), b.getInt(), b.getInt() );
	}
	
	/**
	 * Location from bytes.
	 *
	 * @param bytes the bytes
	 * @param w the w
	 * @return the location
	 */
	public static Location LocationFromBytes(byte[] bytes, World w)
	{
		ByteBuffer b = ByteBuffer.wrap(bytes);
		return new Location(w, b.getDouble(), b.getDouble(), b.getDouble(), b.getFloat(), b.getFloat());
	}
	
	/**
	 * Int to byte array.
	 *
	 * @param value the value
	 * @return the byte[]
	 */
	public static final byte[] intToByteArray(int value) 
	{
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	
	/**
	 * Byte array to int.
	 *
	 * @param b the b
	 * @param index the index
	 * @return the int
	 */
	public static final int byteArrayToInt(byte [] b, int index) 
	{
        return (b[index] << 24)
                + ((b[index + 1] & 0xFF) << 16)
                + ((b[index + 2] & 0xFF) << 8)
                + (b[index + 3] & 0xFF);
	}
	
	/**
	 * Byte to boolean.
	 *
	 * @param b the b
	 * @return true, if successful
	 */
	public static final boolean byteToBoolean(byte b)
	{
		if ( b >= 1 )
			return true;
		else
			return false;
	}
}