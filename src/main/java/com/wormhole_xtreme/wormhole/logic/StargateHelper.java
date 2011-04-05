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
package com.wormhole_xtreme.wormhole.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable.ActionToTake;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.model.StargateNetwork;
import com.wormhole_xtreme.wormhole.model.StargateShape;
import com.wormhole_xtreme.wormhole.utils.DataUtils;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;


// TODO: Auto-generated Javadoc
/**
 * The Class StargateHelper.
 */
public class StargateHelper 
{
	
	/** The Constant shapes. */
	private static final ConcurrentHashMap<String, StargateShape> shapes = new ConcurrentHashMap<String, StargateShape>();
	
	/** The Constant StargateSaveVersion. */
	public static final byte StargateSaveVersion = 5;
	
	/** The Empty block. */
	private static byte[] emptyBlock = { 0,0,0,0,0,0,0,0,0,0,0,0 };
	
	/**
	 * Stargateto binary.
	 *
	 * @param s the s
	 * @return the byte[]
	 */
	public static byte[] stargatetoBinary(Stargate s)
	{
		byte[] utfFaceBytes;
		byte[] utfIdcBytes;
		try
		{
			utfFaceBytes = s.facing.toString().getBytes("UTF8");
			utfIdcBytes = s.irisDeactivationCode.getBytes("UTF8");
		}
		catch ( Exception e)
		{
			WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Unable to store gate in DB, byte encoding failed: " + e.getMessage());
			byte[] b = null;
			return b;
		}

		int numBlocks = 4;
		int numLocations = 1;
		int locationSize = 32;
		int blockSize = 12;
		// Version, isSignPowered, Active, IrisActive, LitGate
		int numBytesWithVersion = 5;
		// string size ints 2 + block count ints 2 + sign index int + num light blocks = 6 ints
		int numInts = 6;
		int numLongs = 2;
		
		// Size of all the basic sizes we know
		int size = numBytesWithVersion + (numInts * 4) + (numLongs * 8) + (numBlocks * blockSize) + (numLocations * locationSize);
		// Size of the gate blocks
		size += (s.blocks.size() * blockSize ) + (s.waterBlocks.size() * blockSize ) + s.lightBlocks.size() * blockSize;
		// Size of the strings.
		size += utfFaceBytes.length + utfIdcBytes.length;
		
		ByteBuffer dataArr = ByteBuffer.allocate(size);
		
		dataArr.put(StargateSaveVersion);
		dataArr.put(DataUtils.blockToBytes(s.activationBlock));
		
		if ( s.irisActivationBlock != null )
			dataArr.put(DataUtils.blockToBytes(s.irisActivationBlock));
		else
			dataArr.put(emptyBlock);

		if ( s.nameBlockHolder != null )
			dataArr.put(DataUtils.blockToBytes(s.nameBlockHolder));
		else
			dataArr.put(emptyBlock);
		
		dataArr.put(DataUtils.locationToBytes(s.teleportLocation));

		if ( s.isSignPowered )
		{
			dataArr.put((byte)1);
			dataArr.put(DataUtils.blockToBytes(s.teleportSignBlock));

			// SignIndex
			dataArr.putInt(s.signIndex);
			
			// SignTarget
			if ( s.signTarget != null )
				dataArr.putLong(s.signTarget.gateId);
			else
				dataArr.putLong(-1);
		}
		else
		{
			dataArr.put((byte)0);
			dataArr.put(emptyBlock);
			dataArr.putInt(-1);
			dataArr.putLong(-1);
		}
		
		if ( s.active && s.target != null)
		{
			dataArr.put((byte)1);
			dataArr.putLong(s.target.gateId);
		}
		else
		{
			dataArr.put((byte)0);
			dataArr.putLong(-1);
		}

		
		dataArr.putInt(utfFaceBytes.length);
		dataArr.put(utfFaceBytes);

		dataArr.putInt(utfIdcBytes.length);
		dataArr.put(utfIdcBytes);

		if ( s.irisActive )
			dataArr.put((byte)1);
		else
			dataArr.put((byte)0);

		if ( s.litGate )
			dataArr.put((byte)1);
		else
			dataArr.put((byte)0);
		
		dataArr.putInt(s.blocks.size());
		for ( int i = 0; i < s.blocks.size(); i++ )
			dataArr.put(DataUtils.blockLocationToBytes(s.blocks.get(i)));
		
		dataArr.putInt(s.waterBlocks.size());
		for ( int i = 0; i < s.waterBlocks.size(); i++ )
			dataArr.put(DataUtils.blockLocationToBytes(s.waterBlocks.get(i)));

		dataArr.putInt(s.lightBlocks.size());
		for ( int i = 0; i < s.lightBlocks.size(); i++ )
			dataArr.put(DataUtils.blockLocationToBytes(s.lightBlocks.get(i)));
		
		return dataArr.array();
	} 
	
	/**
	 * * This method takes in a button/lever and a facing and returns a completed stargate.
	 * If the gate does not match the format for a gate it returns null.
	 *
	 * @param buttonBlock the button_block
	 * @param facing the facing
	 * @return s If successful returns completed gate, null otherwise
	 */
	public static Stargate checkStargate(Block buttonBlock, BlockFace facing )
	{
		final Set<String> keys = shapes.keySet();
		Stargate s = null; 
			
		for( String key : keys )
		{
		    final StargateShape shape = shapes.get(key);
		    if (shape != null)
		    {
		        s = checkStargate(buttonBlock, facing, shape, false);
		    }
			if ( s != null )
			{
				return s;
			}
		}
		
		return s;
	}
	
	/**
	 * This method takes in the DHD pressed and a shape. This method will create a stargate of the specified shape and return it.
	 *
	 * @param buttonBlock the button_block
	 * @param facing the facing
	 * @param shape the shape
	 * @return checkStargate(button_block, facing, shape, true)
	 */
	public static Stargate checkStargate(Block buttonBlock, BlockFace facing, StargateShape shape )
	{
		return checkStargate(buttonBlock, facing, shape, true);
	}
	
	/**
	 * Check stargate.
	 *
	 * @param buttonBlock the button_block
	 * @param facing the facing
	 * @param shape the shape
	 * @param create the create
	 * @return the stargate
	 */
	public static Stargate checkStargate(Block buttonBlock, BlockFace facing, StargateShape shape, boolean create )
	{
		BlockFace opposite = WorldUtils.getInverseDirection(facing);
		Block holdingBlock = buttonBlock.getFace(opposite);
		
		if ( isStargateMaterial(holdingBlock, shape) )
		{
			//System.out.println("");
			// Probably a stargate, lets start checking!
			Stargate tempGate = new Stargate();
			tempGate.myWorld = buttonBlock.getWorld();
			tempGate.name = "";
			tempGate.activationBlock = buttonBlock;
			tempGate.facing = facing;
			tempGate.blocks.add( buttonBlock.getLocation() );
			tempGate.gateShape = shape;
			if ( !isStargateMaterial( holdingBlock.getRelative(BlockFace.DOWN), tempGate.gateShape ) )
			{
				return null;
			}

			Block possibleSignHolder = holdingBlock.getRelative( WorldUtils.getPerpendicularRightDirection(opposite) ); 
			if ( isStargateMaterial( possibleSignHolder, tempGate.gateShape) )
			{
				// This might be a public gate with activation method of sign instead of name.
				Block signBlock = possibleSignHolder.getRelative(tempGate.facing);
				if ( signBlock.getType() == Material.WALL_SIGN )
				{
					tempGate.isSignPowered = true;
					tempGate.teleportSignBlock = signBlock;
					tempGate.teleportSign = (Sign) signBlock.getState();
					tempGate.blocks.add( signBlock.getLocation() );
					
					String name = tempGate.teleportSign.getLine(0);
					Stargate posDupe = StargateManager.getStargate(name);
					if ( posDupe != null )
					{
						tempGate.name = "";
						return tempGate;
					}
					
					if ( name.length() > 2 )
					{
						tempGate.name = name;
					}
				}
			}
			
			
			int[] facingVector = { 0,0,0 };
			
			World w = buttonBlock.getWorld();
			// Now we start calculaing the values for the blocks that need to be the stargate material.
			
			if ( facing == BlockFace.NORTH )
				facingVector[0] = 1;
			else if ( facing == BlockFace.SOUTH )
				facingVector[0] = -1;
			else if ( facing == BlockFace.EAST )
				facingVector[2] = 1;
			else if ( facing == BlockFace.WEST )
				facingVector[2] = -1;
			else if ( facing == BlockFace.UP )
				facingVector[1] = -1;
			else if ( facing == BlockFace.DOWN )
				facingVector[1] = 1;

			int[] directionVector = { 0,0,0 };
			int[] startingPosition = { 0,0,0 };
			
			// Calculate the cross product
			directionVector[0] = facingVector[1]*shape.referenceVector[2] - facingVector[2]*shape.referenceVector[1];
			directionVector[1] = facingVector[2]*shape.referenceVector[0] - facingVector[0]*shape.referenceVector[2];
			directionVector[2] = facingVector[0]*shape.referenceVector[1] - facingVector[1]*shape.referenceVector[0];

			// This is the 0,0,0 the block at the ground against the far side of the stargate
			startingPosition[0] = buttonBlock.getX() + facingVector[0] * shape.toGateCorner[2] + directionVector[0] * shape.toGateCorner[0];
			startingPosition[1] = buttonBlock.getY() + shape.toGateCorner[1]; 
			startingPosition[2] = buttonBlock.getZ() + facingVector[2] * shape.toGateCorner[2] + directionVector[2] * shape.toGateCorner[0];
			
			for ( int i = 0; i < shape.stargatePositions.length; i++)
			{
				int[] bVect = shape.stargatePositions[i];
				
				int[] blockLocation = {bVect[2] * directionVector[0] * -1, bVect[1], bVect[2] * directionVector[2] * -1};
				
				Block maybeBlock = w.getBlockAt(blockLocation[0] + startingPosition[0], blockLocation[1] + startingPosition[1], blockLocation[2] + startingPosition[2]);
				if ( create )
					maybeBlock.setType( tempGate.gateShape.stargateMaterial );
					
				if ( isStargateMaterial(maybeBlock, tempGate.gateShape) )
				{
					tempGate.blocks.add( maybeBlock.getLocation() );
					for ( int j = 0; j < shape.lightPositions.length; j++ )
					{
						if ( shape.lightPositions[j] == i)
						{
							tempGate.lightBlocks.add(maybeBlock.getLocation());
						}
					}
				}
				else
				{
					if ( tempGate.network != null )
					{
						tempGate.network.gateList.remove(tempGate);
						if (tempGate.isSignPowered)
						{
						    tempGate.network.signGateList.remove(tempGate);
						}
					}
					return null;
				}
			}

			// Set the name sign location.
			if ( shape.signPosition != null )
			{
				int[] signLocationArray = {shape.signPosition[2] * directionVector[0] * -1, shape.signPosition[1], shape.signPosition[2] * directionVector[2] * -1};
				Block nameBlock = w.getBlockAt(signLocationArray[0] + startingPosition[0], signLocationArray[1] + startingPosition[1], signLocationArray[2] + startingPosition[2]);
				tempGate.nameBlockHolder = nameBlock;
			}
			// Now set teleport in location
			int[] teleportLocArray = {shape.enterPosition[2] * directionVector[0] * -1, shape.enterPosition[1], shape.enterPosition[2] * directionVector[2] * -1};
			Block teleBlock = w.getBlockAt(teleportLocArray[0] + startingPosition[0], teleportLocArray[1] + startingPosition[1], teleportLocArray[2] + startingPosition[2]);
			// First go forward one
			Block bLoc = teleBlock.getRelative(facing);
			// Now go up until we hit air or water.
			while ( bLoc.getType() != Material.AIR && bLoc.getType() != Material.WATER)
			{
				bLoc = bLoc.getRelative(BlockFace.UP);
			}
			Location teleLoc = bLoc.getLocation();
			// Make sure the guy faces the right way out of the portal.
			teleLoc.setYaw( WorldUtils.getDegreesFromBlockFace(facing));
			teleLoc.setPitch(0);
			// Put him in the middle of the block instead of a corner.
			// Players are 1.65 blocks tall, so we go up .66 more up :-p
			teleLoc.setX(teleLoc.getX() + 0.5);
			teleLoc.setY(teleLoc.getY() + 0.66);
			teleLoc.setZ(teleLoc.getZ() + 0.5);
			tempGate.teleportLocation = teleLoc;
			
			for ( int[] bVect : shape.waterPositions)
			{
				int[] blockLocation = {bVect[2] * directionVector[0] * -1, bVect[1], bVect[2] * directionVector[2] * -1};
				
				Block maybeBlock = w.getBlockAt(blockLocation[0] + startingPosition[0], blockLocation[1] + startingPosition[1], blockLocation[2] + startingPosition[2]);
				if ( maybeBlock.getType() == Material.AIR )
					tempGate.waterBlocks.add( maybeBlock.getLocation() );
				else
				{
					if ( tempGate.network != null )
						tempGate.network.gateList.remove(tempGate);
					
					return null;
				}
			}
			
			// Moved this here so that it only creates the sign if the gate is correctly built.
			if ( tempGate.name != null && tempGate.name.length() > 0 )
			{
				String network_name = "Public";
				
				if ( tempGate.teleportSign != null && !tempGate.teleportSign.getLine(1).equals("") )
				{
					// We have a specific network
					network_name = tempGate.teleportSign.getLine(1);
				}
				StargateNetwork	net = StargateManager.getStargateNetwork(network_name);
				if ( net == null )
					net = StargateManager.addStargateNetwork(network_name);
				StargateManager.addGateToNetwork(tempGate, network_name);

				tempGate.network = net;
				tempGate.signIndex = -1;
				WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(tempGate,ActionToTake.SIGNCLICK));
				// tempGate.teleportSignClicked();
			}
			
			return tempGate; 
		}

		return null;
	}
	
	
	/**
	 * Checks if is stargate material.
	 *
	 * @param b the b
	 * @return true, if is stargate material
	 */
	private static boolean isStargateMaterial(Block b, StargateShape s)
	{
		return b.getType() == s.stargateMaterial;
	}	

	/**
	 * Parses the versioned data.
	 *
	 * @param gate_data the gate_data
	 * @param w the w
	 * @param name the name
	 * @param network the network
	 * @return the stargate
	 */
	public static Stargate parseVersionedData(byte[] gate_data, World w, String name, StargateNetwork network)
	{
		Stargate s = new Stargate();
		s.name = name;
		s.network = network;
		ByteBuffer byteBuff = ByteBuffer.wrap(gate_data);

		// First get version byte
		s.loadedVersion = byteBuff.get();
		s.myWorld = w;

		if ( s.loadedVersion == 2 )
		{
			byte[] locArray = new byte[32];
			byte[] blocArray = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byteBuff.get(blocArray);
			s.activationBlock = DataUtils.blockFromBytes(blocArray, w);
			WorldUtils.checkChunkLoad(s.activationBlock);
			
			byteBuff.get(blocArray); 
			s.irisActivationBlock = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(blocArray); 
			s.nameBlockHolder = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(locArray);
			s.teleportLocation = DataUtils.locationFromBytes(locArray, w);
			
			s.isSignPowered = DataUtils.byteToBoolean(byteBuff.get());// index++;
			
			byteBuff.get(blocArray);
			if ( s.isSignPowered  )
			{
				s.teleportSignBlock = DataUtils.blockFromBytes(blocArray, w);
				
				if ( w.isChunkLoaded(s.teleportSignBlock.getChunk()))
				{
					try
					{
						s.teleportSign = (Sign)s.teleportSignBlock.getState();
						s.tryClickTeleportSign(s.teleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.name + " and will be unable to dial out.");
					}
				}
			}
			
			int facingSize = byteBuff.getInt();
			byte[] strBytes = new byte[facingSize];
			byteBuff.get(strBytes);
			String faceStr = new String(strBytes);
			s.facing = BlockFace.valueOf(faceStr);
			
			s.teleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.facing));
			s.teleportLocation.setPitch(0);

			int idcLen = byteBuff.getInt();
			byte[] idcBytes = new byte[idcLen];
			byteBuff.get(idcBytes);
			s.irisDeactivationCode = new String(idcBytes);
			
			s.irisActive = DataUtils.byteToBoolean(byteBuff.get()); // index++;
			
			int numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.blocks.add( bl.getLocation() );
			}
			
			numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.waterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		else if ( s.loadedVersion == 3)
		{
			byte[] locArray = new byte[32];
			byte[] blocArray = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byteBuff.get(blocArray);
			s.activationBlock = DataUtils.blockFromBytes(blocArray, w);
			WorldUtils.checkChunkLoad(s.activationBlock);
			
			byteBuff.get(blocArray); 
			s.irisActivationBlock = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(blocArray); 
			s.nameBlockHolder = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(locArray);
			s.teleportLocation = DataUtils.locationFromBytes(locArray, w);
			
			s.isSignPowered = DataUtils.byteToBoolean(byteBuff.get());
			
			byteBuff.get(blocArray);
			s.signIndex = byteBuff.getInt();
			s.tempSignTarget = byteBuff.getInt();
			if ( s.isSignPowered  )
			{
				s.teleportSignBlock = DataUtils.blockFromBytes(blocArray, w);
					
				if ( w.isChunkLoaded(s.teleportSignBlock.getChunk()))
				{
					try
					{
						s.teleportSign = (Sign)s.teleportSignBlock.getState();
						s.tryClickTeleportSign(s.teleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.name + " and will be unable to change dial target.");
					}
				}
			}

			
			s.active = DataUtils.byteToBoolean(byteBuff.get());
			s.tempTargetId = byteBuff.getInt();
			
			int facingSize = byteBuff.getInt();
			byte[] strBytes = new byte[facingSize];
			byteBuff.get(strBytes);
			String faceStr = new String(strBytes);
			s.facing = BlockFace.valueOf(faceStr);
			
			s.teleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.facing));
			s.teleportLocation.setPitch(0);

			int idcLen = byteBuff.getInt();
			byte[] idcBytes = new byte[idcLen];
			byteBuff.get(idcBytes);
			s.irisDeactivationCode = new String(idcBytes);
			
			s.irisActive = DataUtils.byteToBoolean(byteBuff.get()); // index++;
			
			int numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.blocks.add( bl.getLocation() );
			}
			
			numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.waterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		else if ( s.loadedVersion == 4 )
		{
			byte[] locArray = new byte[32];
			byte[] blocArray = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byteBuff.get(blocArray);
			s.activationBlock = DataUtils.blockFromBytes(blocArray, w);
			WorldUtils.checkChunkLoad(s.activationBlock);
			
			byteBuff.get(blocArray); 
			s.irisActivationBlock = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(blocArray); 
			s.nameBlockHolder = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(locArray);
			s.teleportLocation = DataUtils.locationFromBytes(locArray, w);
			
			s.isSignPowered = DataUtils.byteToBoolean(byteBuff.get());
			
			byteBuff.get(blocArray);
			s.signIndex = byteBuff.getInt();
			s.tempSignTarget = byteBuff.getLong();
			if ( s.isSignPowered  )
			{
				s.teleportSignBlock = DataUtils.blockFromBytes(blocArray, w);
				
				if ( w.isChunkLoaded(s.teleportSignBlock.getChunk()))
				{
					try
					{
						s.teleportSign = (Sign)s.teleportSignBlock.getState();
						s.tryClickTeleportSign(s.teleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.name + " and will be unable to change dial target.");
					}
				}
			}

			
			s.active = DataUtils.byteToBoolean(byteBuff.get());
			s.tempTargetId = byteBuff.getLong();
			
			int facingSize = byteBuff.getInt();
			byte[] strBytes = new byte[facingSize];
			byteBuff.get(strBytes);
			String faceStr = new String(strBytes);
			s.facing = BlockFace.valueOf(faceStr);
			
			s.teleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.facing));
			s.teleportLocation.setPitch(0);

			int idcLen = byteBuff.getInt();
			byte[] idc_bytes = new byte[idcLen];
			byteBuff.get(idc_bytes);
			s.irisDeactivationCode = new String(idc_bytes);
			
			s.irisActive = DataUtils.byteToBoolean(byteBuff.get()); // index++;
			s.irisDefaultActive = s.irisActive;
			int numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.blocks.add( bl.getLocation() );
			}
			
			numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.waterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		else if ( s.loadedVersion == 5 )
		{
			byte[] locArray = new byte[32];
			byte[] blocArray = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byteBuff.get(blocArray);
			s.activationBlock = DataUtils.blockFromBytes(blocArray, w);
			WorldUtils.checkChunkLoad(s.activationBlock);
			
			byteBuff.get(blocArray); 
			s.irisActivationBlock = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(blocArray); 
			s.nameBlockHolder = DataUtils.blockFromBytes(blocArray, w);
			
			byteBuff.get(locArray);
			s.teleportLocation = DataUtils.locationFromBytes(locArray, w);
			
			s.isSignPowered = DataUtils.byteToBoolean(byteBuff.get());
			
			byteBuff.get(blocArray);
			s.signIndex = byteBuff.getInt();
			s.tempSignTarget = byteBuff.getLong();
			if ( s.isSignPowered  )
			{
				s.teleportSignBlock = DataUtils.blockFromBytes(blocArray, w);
				
				if ( w.isChunkLoaded(s.teleportSignBlock.getChunk()))
				{
					try
					{
						s.teleportSign = (Sign)s.teleportSignBlock.getState();
						s.tryClickTeleportSign(s.teleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.name + " and will be unable to change dial target.");
					}
				}
			}

			
			s.active = DataUtils.byteToBoolean(byteBuff.get());
			s.tempTargetId = byteBuff.getLong();
			
			int facingSize = byteBuff.getInt();
			byte[] strBytes = new byte[facingSize];
			byteBuff.get(strBytes);
			String faceStr = new String(strBytes);
			s.facing = BlockFace.valueOf(faceStr);
			
			s.teleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.facing));
			s.teleportLocation.setPitch(0);

			int idcLen = byteBuff.getInt();
			byte[] idcBytes = new byte[idcLen];
			byteBuff.get(idcBytes);
			s.irisDeactivationCode = new String(idcBytes);
			
			s.irisActive = DataUtils.byteToBoolean(byteBuff.get());
			s.irisDefaultActive = s.irisActive;
			s.litGate = DataUtils.byteToBoolean(byteBuff.get());
			
			int numBlocks = byteBuff.getInt();
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.blocks.add( bl.getLocation() );
			}
			
			numBlocks = byteBuff.getInt();
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.waterBlocks.add( bl.getLocation() );
			}
			
			numBlocks = byteBuff.getInt();
			for ( int i = 0; i < numBlocks; i++ )
			{
				byteBuff.get(blocArray);
				Block bl = DataUtils.blockFromBytes(blocArray, w);
				s.lightBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		return null;
	}

	/**
	 * Returns a shape based on name.
	 *
	 * @param name Name of stargate shape
	 * @return The shape associated with that name. Null if not in list.
	 */
	public static StargateShape getShape(String name)
	{
		if ( shapes.containsKey(name) )
			return shapes.get(name);

		return null;
	}
	
	/**
	 * Load shapes.
	 */
	public static void loadShapes()
	{
		final File directory = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "GateShapes" + File.separator);
		if (!directory.exists()) 
		{
			
			try 
			{
				directory.mkdir();
			} 
			catch (Exception e) 
			{
				WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Unable to make directory: " + e.getMessage());
			}
			BufferedReader br = null;
			BufferedWriter bw = null;
			try 
			{
				final File standardShapeFile = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "GateShapes" + File.separator + "Standard.shape");
		    	InputStream is = WormholeXTreme.class.getResourceAsStream("/GateShapes/Standard.shape");
		    	br = new BufferedReader(new InputStreamReader(is));
		    	bw = new BufferedWriter(new FileWriter(standardShapeFile));
		    	
				for (String s = ""; (s = br.readLine()) != null; ) 
				{
					bw.write(s);
					bw.write("\n");
				}
				
				br.close();
				bw.close();
				is.close();
			} 
			catch (IOException e) 
			{
				WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Unable to create files: " + e.getMessage());
			}
			catch (NullPointerException e)
			{
			    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Unable to create files: " + e.getMessage());
			}
			finally
			{
			    try 
			    {
                    br.close();
                }
                catch (IOException e) 
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
                }
			    try 
			    {
                    bw.close();
                }
                catch (IOException e) 
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
                }
			}
		}
		
		final File[] shapeFiles = directory.listFiles();
		for ( File fi : shapeFiles )
		{
		    if ( fi.getName().contains(".shape") )
		    {
		        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Loading shape file: \"" + (String)fi.getName() + "\"");
		        BufferedReader bufferedReader = null;
		        try
		        {
		            ArrayList<String> fileLines = new ArrayList<String>();
		            bufferedReader = new BufferedReader(new FileReader(fi));
		            for (String s = ""; (s = bufferedReader.readLine()) != null; ) 
		            {
		                fileLines.add(s);
		            }
		            bufferedReader.close();

		            StargateShape shape = new StargateShape(fileLines.toArray(new String[fileLines.size()]));

		            if ( shapes.containsKey(shape.shapeName) )
		            {
		                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Shape File: " + fi.getName() + " contains shape name: " + shape.shapeName + " which already exists. This shape will be unavailable.");
		            }
		            else
		            {
		                shapes.put(shape.shapeName, shape);
		            }
		        }
		        catch (FileNotFoundException e) 
		        {
		            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
		        }
		        catch (IOException e)
		        {
		            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
		        }
		        finally
		        {
		            try 
		            {
		                if (bufferedReader != null)
		                {
		                    bufferedReader.close();
		                }
		            }
		            catch (IOException e) 
		            {
		                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
		            }
		        }
		        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Completed loading shape file: \"" + (String)fi.getName() + "\"");
		    }
		}

		if ( shapes.size() == 0 )
		{
		    shapes.put( "Standard", new StargateShape());
		}
	}
}
