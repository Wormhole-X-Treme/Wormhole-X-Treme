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
package com.wormhole_xtreme.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.model.StargateNetwork;
import com.wormhole_xtreme.model.StargateShape;
import com.wormhole_xtreme.utils.DataUtils;
import com.wormhole_xtreme.utils.WorldUtils;


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
	private static byte[] EmptyBlock = { 0,0,0,0,0,0,0,0,0,0,0,0 };
	
	/**
	 * Stargateto binary.
	 *
	 * @param s the s
	 * @return the byte[]
	 */
	public static byte[] stargatetoBinary(Stargate s)
	{
		byte[] utf_face_bytes;
		byte[] utf_idc_bytes;
		try
		{
			utf_face_bytes = s.Facing.toString().getBytes("UTF8");
			utf_idc_bytes = s.IrisDeactivationCode.getBytes("UTF8");
		}
		catch ( Exception e)
		{
			WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE,false,"Unable to store gate in DB, byte encoding failed: " + e.getMessage());
			return null;
		}

		int num_blocks = 4;
		int num_locations = 1;
		int location_size = 32;
		int block_size = 12;
		// Version, isSignPowered, Active, IrisActive, LitGate
		int num_bytes_with_version = 5;
		// string size ints 2 + block count ints 2 + sign index int + num light blocks = 6 ints
		int num_ints = 6;
		int num_longs = 2;
		
		// Size of all the basic sizes we know
		int size = num_bytes_with_version + (num_ints * 4) + (num_longs * 8) + (num_blocks * block_size) + (num_locations * location_size);
		// Size of the gate blocks
		size += (s.Blocks.size() * block_size ) + (s.WaterBlocks.size() * block_size ) + s.LightBlocks.size() * block_size;
		// Size of the strings.
		size += utf_face_bytes.length + utf_idc_bytes.length;
		
		ByteBuffer data_arr = ByteBuffer.allocate(size);
		
		data_arr.put(StargateSaveVersion);
		data_arr.put(DataUtils.BlockToBytes(s.ActivationBlock));
		
		if ( s.IrisActivationBlock != null )
			data_arr.put(DataUtils.BlockToBytes(s.IrisActivationBlock));
		else
			data_arr.put(EmptyBlock);

		if ( s.NameBlockHolder != null )
			data_arr.put(DataUtils.BlockToBytes(s.NameBlockHolder));
		else
			data_arr.put(EmptyBlock);
		
		data_arr.put(DataUtils.LocationToBytes(s.TeleportLocation));

		if ( s.IsSignPowered )
		{
			data_arr.put((byte)1);
			data_arr.put(DataUtils.BlockToBytes(s.TeleportSignBlock));

			// SignIndex
			data_arr.putInt(s.SignIndex);
			
			// SignTarget
			if ( s.SignTarget != null )
				data_arr.putLong(s.SignTarget.GateId);
			else
				data_arr.putLong(-1);
		}
		else
		{
			data_arr.put((byte)0);
			data_arr.put(EmptyBlock);
			data_arr.putInt(-1);
			data_arr.putLong(-1);
		}
		
		if ( s.Active && s.Target != null)
		{
			data_arr.put((byte)1);
			data_arr.putLong(s.Target.GateId);
		}
		else
		{
			data_arr.put((byte)0);
			data_arr.putLong(-1);
		}

		
		data_arr.putInt(utf_face_bytes.length);
		data_arr.put(utf_face_bytes);

		data_arr.putInt(utf_idc_bytes.length);
		data_arr.put(utf_idc_bytes);

		if ( s.IrisActive )
			data_arr.put((byte)1);
		else
			data_arr.put((byte)0);

		if ( s.LitGate )
			data_arr.put((byte)1);
		else
			data_arr.put((byte)0);
		
		data_arr.putInt(s.Blocks.size());
		for ( int i = 0; i < s.Blocks.size(); i++ )
			data_arr.put(DataUtils.BlockLocationToBytes(s.Blocks.get(i)));
		
		data_arr.putInt(s.WaterBlocks.size());
		for ( int i = 0; i < s.WaterBlocks.size(); i++ )
			data_arr.put(DataUtils.BlockLocationToBytes(s.WaterBlocks.get(i)));

		data_arr.putInt(s.LightBlocks.size());
		for ( int i = 0; i < s.LightBlocks.size(); i++ )
			data_arr.put(DataUtils.BlockLocationToBytes(s.LightBlocks.get(i)));
		
		return data_arr.array();
	} 
	
	/**
	 * * This method takes in a button/lever and a facing and returns a completed stargate.
	 * If the gate does not match the format for a gate it returns null.
	 *
	 * @param button_block the button_block
	 * @param facing the facing
	 * @return s If successful returns completed gate, null otherwise
	 */
	public static Stargate checkStargate(Block button_block, BlockFace facing )
	{
		Set<String> keys = shapes.keySet();
		Stargate s = null; 
			
		for( String key : keys )
		{
			s = checkStargate(button_block, facing, shapes.get(key), false);
			
			if ( s != null )
				return s;
		}
		
		return s;
	}
	
	/**
	 * This method takes in the DHD pressed and a shape. This method will create a stargate of the specified shape and return it.
	 *
	 * @param button_block the button_block
	 * @param facing the facing
	 * @param shape the shape
	 * @return checkStargate(button_block, facing, shape, true)
	 */
	public static Stargate checkStargate(Block button_block, BlockFace facing, StargateShape shape )
	{
		return checkStargate(button_block, facing, shape, true);
	}
	
	/**
	 * Check stargate.
	 *
	 * @param button_block the button_block
	 * @param facing the facing
	 * @param shape the shape
	 * @param create the create
	 * @return the stargate
	 */
	public static Stargate checkStargate(Block button_block, BlockFace facing, StargateShape shape, boolean create )
	{
		BlockFace opposite = WorldUtils.getInverseDirection(facing);
		Block holding_block = button_block.getFace(opposite);
		
		if ( isStargateMaterial(holding_block) )
		{
			//System.out.println("");
			// Probably a stargate, lets start checking!
			Stargate temp_gate = new Stargate();
			temp_gate.MyWorld = button_block.getWorld();
			temp_gate.Name = "";
			temp_gate.ActivationBlock = button_block;
			temp_gate.Facing = facing;
			temp_gate.Blocks.add( button_block.getLocation() );
			temp_gate.GateShape = shape;
			if ( !isStargateMaterial( holding_block.getRelative(BlockFace.DOWN) ) )
			{
				return null;
			}

			Block possible_sign_holder = holding_block.getRelative( WorldUtils.getPerpendicularRightDirection(opposite) ); 
			if ( isStargateMaterial( possible_sign_holder) )
			{
				// This might be a public gate with activation method of sign instead of name.
				Block sign_block = possible_sign_holder.getRelative(temp_gate.Facing);
				if ( sign_block.getType() == Material.WALL_SIGN )
				{
					temp_gate.IsSignPowered = true;
					temp_gate.TeleportSignBlock = sign_block;
					temp_gate.TeleportSign = (Sign) sign_block.getState();
					temp_gate.Blocks.add( sign_block.getLocation() );
					
					String name = temp_gate.TeleportSign.getLine(0);
					Stargate pos_dupe = StargateManager.GetStargate(name);
					if ( pos_dupe != null )
					{
						temp_gate.Name = "";
						return temp_gate;
					}
					
					if ( name.length() > 2 )
					{
						temp_gate.Name = name;
					}
				}
			}
			
			
			int[] facing_vector = { 0,0,0 };
			
			World w = button_block.getWorld();
			// Now we start calculaing the values for the blocks that need to be the stargate material.
			
			if ( facing == BlockFace.NORTH )
				facing_vector[0] = 1;
			else if ( facing == BlockFace.SOUTH )
				facing_vector[0] = -1;
			else if ( facing == BlockFace.EAST )
				facing_vector[2] = 1;
			else if ( facing == BlockFace.WEST )
				facing_vector[2] = -1;
			else if ( facing == BlockFace.UP )
				facing_vector[1] = -1;
			else if ( facing == BlockFace.DOWN )
				facing_vector[1] = 1;

			int[] direction_vector = { 0,0,0 };
			int[] starting_position = { 0,0,0 };
			
			// Calculate the cross product
			direction_vector[0] = facing_vector[1]*shape.reference_vector[2] - facing_vector[2]*shape.reference_vector[1];
			direction_vector[1] = facing_vector[2]*shape.reference_vector[0] - facing_vector[0]*shape.reference_vector[2];
			direction_vector[2] = facing_vector[0]*shape.reference_vector[1] - facing_vector[1]*shape.reference_vector[0];

			// This is the 0,0,0 the block at the ground against the far side of the stargate
			starting_position[0] = button_block.getX() + facing_vector[0] * shape.to_gate_corner[2] + direction_vector[0] * shape.to_gate_corner[0];
			starting_position[1] = button_block.getY() + shape.to_gate_corner[1]; 
			starting_position[2] = button_block.getZ() + facing_vector[2] * shape.to_gate_corner[2] + direction_vector[2] * shape.to_gate_corner[0];
			
			for ( int i = 0; i < shape.stargate_positions.length; i++)
			{
				int[] b_vect = shape.stargate_positions[i];
				
				int[] block_location = {b_vect[2] * direction_vector[0] * -1, b_vect[1], b_vect[2] * direction_vector[2] * -1};
				
				Block maybe_block = w.getBlockAt(block_location[0] + starting_position[0], block_location[1] + starting_position[1], block_location[2] + starting_position[2]);
				if ( create )
					maybe_block.setType(ConfigManager.getStargateMaterial());
					
				if ( isStargateMaterial(maybe_block) )
				{
					temp_gate.Blocks.add( maybe_block.getLocation() );
					for ( int j = 0; j < shape.light_positions.length; j++ )
					{
						if ( shape.light_positions[j] == i)
						{
							temp_gate.LightBlocks.add(maybe_block.getLocation());
						}
					}
				}
				else
				{
					if ( temp_gate.Network != null )
						temp_gate.Network.gate_list.remove(temp_gate);
					
					return null;
				}
			}

			// Set the name sign location.
			if ( shape.sign_position != null )
			{
				int[] sign_location_array = {shape.sign_position[2] * direction_vector[0] * -1, shape.sign_position[1], shape.sign_position[2] * direction_vector[2] * -1};
				Block name_block = w.getBlockAt(sign_location_array[0] + starting_position[0], sign_location_array[1] + starting_position[1], sign_location_array[2] + starting_position[2]);
				temp_gate.NameBlockHolder = name_block;
			}
			// Now set teleport in location
			int[] teleport_loc_array = {shape.enter_position[2] * direction_vector[0] * -1, shape.enter_position[1], shape.enter_position[2] * direction_vector[2] * -1};
			Block tele_block = w.getBlockAt(teleport_loc_array[0] + starting_position[0], teleport_loc_array[1] + starting_position[1], teleport_loc_array[2] + starting_position[2]);
			// First go forward one
			Block b_loc = tele_block.getRelative(facing);
			// Now go up until we hit air or water.
			while ( b_loc.getType() != Material.AIR && b_loc.getType() != Material.WATER)
			{
				b_loc = b_loc.getRelative(BlockFace.UP);
			}
			Location tele_loc = b_loc.getLocation();
			// Make sure the guy faces the right way out of the portal.
			tele_loc.setYaw( WorldUtils.getDegreesFromBlockFace(facing));
			tele_loc.setPitch(0);
			// Put him in the middle of the block instead of a corner.
			// Players are 1.65 blocks tall, so we go up .66 more up :-p
			tele_loc.setX(tele_loc.getX() + 0.5);
			tele_loc.setY(tele_loc.getY() + 0.66);
			tele_loc.setZ(tele_loc.getZ() + 0.5);
			temp_gate.TeleportLocation = tele_loc;
			
			for ( int[] b_vect : shape.water_positions)
			{
				int[] block_location = {b_vect[2] * direction_vector[0] * -1, b_vect[1], b_vect[2] * direction_vector[2] * -1};
				
				Block maybe_block = w.getBlockAt(block_location[0] + starting_position[0], block_location[1] + starting_position[1], block_location[2] + starting_position[2]);
				if ( maybe_block.getType() == Material.AIR )
					temp_gate.WaterBlocks.add( maybe_block.getLocation() );
				else
				{
					if ( temp_gate.Network != null )
						temp_gate.Network.gate_list.remove(temp_gate);
					
					return null;
				}
			}
			
			// Moved this here so that it only creates the sign if the gate is correctly built.
			if ( temp_gate.Name != null && temp_gate.Name.length() > 0 )
			{
				String network_name = "Public";
				
				if ( temp_gate.TeleportSign != null && !temp_gate.TeleportSign.getLine(1).equals("") )
				{
					// We have a specific network
					network_name = temp_gate.TeleportSign.getLine(1);
				}
				StargateNetwork	net = StargateManager.GetStargateNetwork(network_name);
				if ( net == null )
					net = StargateManager.AddStargateNetwork(network_name);
				StargateManager.AddGateToNetwork(temp_gate, network_name);

				temp_gate.Network = net;
				temp_gate.SignIndex = -1;
				temp_gate.TeleportSignClicked();
			}
			
			return temp_gate; 
		}

		return null;
	}
	
	
	/**
	 * Checks if is stargate material.
	 *
	 * @param b the b
	 * @return true, if is stargate material
	 */
	private static boolean isStargateMaterial(Block b)
	{
		return b.getType() == ConfigManager.getStargateMaterial();
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
		s.Name = name;
		s.Network = network;
		ByteBuffer byte_buff = ByteBuffer.wrap(gate_data);

		// First get version byte
		s.LoadedVersion = byte_buff.get();
		s.MyWorld = w;
		if ( s.LoadedVersion == 1 )
		{
			byte[] loc_array = new byte[32];
			byte[] bloc_array = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byte_buff.get(bloc_array);
			s.ActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);

			byte_buff.get(bloc_array); 
			s.IrisActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(bloc_array); 
			s.NameBlockHolder = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(loc_array);
			s.TeleportLocation = DataUtils.LocationFromBytes(loc_array, w);
			
			s.IsSignPowered = DataUtils.byteToBoolean(byte_buff.get());// index++;
			
			byte_buff.get(bloc_array);
			if ( s.IsSignPowered  )
			{
				s.TeleportSignBlock = DataUtils.BlockFromBytes(bloc_array, w);
				
				if ( w.isChunkLoaded(s.TeleportSignBlock.getChunk()))
				{
					try
					{
						s.TeleportSign = (Sign)s.TeleportSignBlock.getState();
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.thisPlugin.prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.Name + " and will be unable to dial out.");
					}
				}
			}
			
			int facing_size = byte_buff.getInt();
			byte[] str_bytes = new byte[facing_size];
			//System.arraycopy(gate_data, index, str_bytes, 0, facing_size); index += facing_size;
			byte_buff.get(str_bytes);
			String face_str = new String(str_bytes);
			s.Facing = BlockFace.valueOf(face_str);
			
			s.TeleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.Facing));
			s.TeleportLocation.setPitch(0);

			if ( s.TeleportLocation.getX() - ((int)s.TeleportLocation.getX()) == 0.0 )
			{
				// This means the data hasn't been corrected yet.
				s.TeleportLocation.setX(s.TeleportLocation.getX() + 0.5);
				s.TeleportLocation.setZ(s.TeleportLocation.getZ() + 0.5);
				s.TeleportLocation.setY(s.TeleportLocation.getY() + 0.1);
			}
			
			int idc_len = byte_buff.getInt();
			byte[] idc_bytes = new byte[idc_len];
			byte_buff.get(idc_bytes);
			s.IrisDeactivationCode = new String(idc_bytes);
			
			s.IrisActive = DataUtils.byteToBoolean(byte_buff.get()); // index++;
			
			WorldUtils.checkChunkLoad(s.ActivationBlock);
			
			int num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			Material m = ConfigManager.getPortalMaterial();
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				if ( bl.getType() != m )
				{
					s.Blocks.add( bl.getLocation() );
				}
			}
			
			num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.WaterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		if ( s.LoadedVersion == 2 )
		{
			byte[] loc_array = new byte[32];
			byte[] bloc_array = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byte_buff.get(bloc_array);
			s.ActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			WorldUtils.checkChunkLoad(s.ActivationBlock);
			
			byte_buff.get(bloc_array); 
			s.IrisActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(bloc_array); 
			s.NameBlockHolder = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(loc_array);
			s.TeleportLocation = DataUtils.LocationFromBytes(loc_array, w);
			
			s.IsSignPowered = DataUtils.byteToBoolean(byte_buff.get());// index++;
			
			byte_buff.get(bloc_array);
			if ( s.IsSignPowered  )
			{
				s.TeleportSignBlock = DataUtils.BlockFromBytes(bloc_array, w);
				
				if ( w.isChunkLoaded(s.TeleportSignBlock.getChunk()))
				{
					try
					{
						s.TeleportSign = (Sign)s.TeleportSignBlock.getState();
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.thisPlugin.prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.Name + " and will be unable to dial out.");
					}
				}
			}
			
			int facing_size = byte_buff.getInt();
			byte[] str_bytes = new byte[facing_size];
			byte_buff.get(str_bytes);
			String face_str = new String(str_bytes);
			s.Facing = BlockFace.valueOf(face_str);
			
			s.TeleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.Facing));
			s.TeleportLocation.setPitch(0);

			int idc_len = byte_buff.getInt();
			byte[] idc_bytes = new byte[idc_len];
			byte_buff.get(idc_bytes);
			s.IrisDeactivationCode = new String(idc_bytes);
			
			s.IrisActive = DataUtils.byteToBoolean(byte_buff.get()); // index++;
			
			int num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.Blocks.add( bl.getLocation() );
			}
			
			num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.WaterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		else if ( s.LoadedVersion == 3)
		{
			byte[] loc_array = new byte[32];
			byte[] bloc_array = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byte_buff.get(bloc_array);
			s.ActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			WorldUtils.checkChunkLoad(s.ActivationBlock);
			
			byte_buff.get(bloc_array); 
			s.IrisActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(bloc_array); 
			s.NameBlockHolder = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(loc_array);
			s.TeleportLocation = DataUtils.LocationFromBytes(loc_array, w);
			
			s.IsSignPowered = DataUtils.byteToBoolean(byte_buff.get());
			
			byte_buff.get(bloc_array);
			s.SignIndex = byte_buff.getInt();
			s.temp_sign_target = byte_buff.getInt();
			if ( s.IsSignPowered  )
			{
				s.TeleportSignBlock = DataUtils.BlockFromBytes(bloc_array, w);
					
				if ( w.isChunkLoaded(s.TeleportSignBlock.getChunk()))
				{
					try
					{
						s.TeleportSign = (Sign)s.TeleportSignBlock.getState();
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.thisPlugin.prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.Name + " and will be unable to change dial target.");
					}
				}
			}

			
			s.Active = DataUtils.byteToBoolean(byte_buff.get());
			s.temp_target_id = byte_buff.getInt();
			
			int facing_size = byte_buff.getInt();
			byte[] str_bytes = new byte[facing_size];
			byte_buff.get(str_bytes);
			String face_str = new String(str_bytes);
			s.Facing = BlockFace.valueOf(face_str);
			
			s.TeleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.Facing));
			s.TeleportLocation.setPitch(0);

			int idc_len = byte_buff.getInt();
			byte[] idc_bytes = new byte[idc_len];
			byte_buff.get(idc_bytes);
			s.IrisDeactivationCode = new String(idc_bytes);
			
			s.IrisActive = DataUtils.byteToBoolean(byte_buff.get()); // index++;
			
			int num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.Blocks.add( bl.getLocation() );
			}
			
			num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.WaterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		else if ( s.LoadedVersion == 4 )
		{
			byte[] loc_array = new byte[32];
			byte[] bloc_array = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byte_buff.get(bloc_array);
			s.ActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			WorldUtils.checkChunkLoad(s.ActivationBlock);
			
			byte_buff.get(bloc_array); 
			s.IrisActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(bloc_array); 
			s.NameBlockHolder = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(loc_array);
			s.TeleportLocation = DataUtils.LocationFromBytes(loc_array, w);
			
			s.IsSignPowered = DataUtils.byteToBoolean(byte_buff.get());
			
			byte_buff.get(bloc_array);
			s.SignIndex = byte_buff.getInt();
			s.temp_sign_target = byte_buff.getLong();
			if ( s.IsSignPowered  )
			{
				s.TeleportSignBlock = DataUtils.BlockFromBytes(bloc_array, w);
				
				if ( w.isChunkLoaded(s.TeleportSignBlock.getChunk()))
				{
					try
					{
						s.TeleportSign = (Sign)s.TeleportSignBlock.getState();
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.thisPlugin.prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.Name + " and will be unable to change dial target.");
					}
				}
			}

			
			s.Active = DataUtils.byteToBoolean(byte_buff.get());
			s.temp_target_id = byte_buff.getLong();
			
			int facing_size = byte_buff.getInt();
			byte[] str_bytes = new byte[facing_size];
			byte_buff.get(str_bytes);
			String face_str = new String(str_bytes);
			s.Facing = BlockFace.valueOf(face_str);
			
			s.TeleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.Facing));
			s.TeleportLocation.setPitch(0);

			int idc_len = byte_buff.getInt();
			byte[] idc_bytes = new byte[idc_len];
			byte_buff.get(idc_bytes);
			s.IrisDeactivationCode = new String(idc_bytes);
			
			s.IrisActive = DataUtils.byteToBoolean(byte_buff.get()); // index++;
			s.IrisDefaultActive = s.IrisActive;
			int num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.Blocks.add( bl.getLocation() );
			}
			
			num_blocks = byte_buff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.WaterBlocks.add( bl.getLocation() );
			}
			
			return s;
		}
		else if ( s.LoadedVersion == 5 )
		{
			byte[] loc_array = new byte[32];
			byte[] bloc_array = new byte[12];
			// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
			//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

			byte_buff.get(bloc_array);
			s.ActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			WorldUtils.checkChunkLoad(s.ActivationBlock);
			
			byte_buff.get(bloc_array); 
			s.IrisActivationBlock = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(bloc_array); 
			s.NameBlockHolder = DataUtils.BlockFromBytes(bloc_array, w);
			
			byte_buff.get(loc_array);
			s.TeleportLocation = DataUtils.LocationFromBytes(loc_array, w);
			
			s.IsSignPowered = DataUtils.byteToBoolean(byte_buff.get());
			
			byte_buff.get(bloc_array);
			s.SignIndex = byte_buff.getInt();
			s.temp_sign_target = byte_buff.getLong();
			if ( s.IsSignPowered  )
			{
				s.TeleportSignBlock = DataUtils.BlockFromBytes(bloc_array, w);
				
				if ( w.isChunkLoaded(s.TeleportSignBlock.getChunk()))
				{
					try
					{
						s.TeleportSign = (Sign)s.TeleportSignBlock.getState();
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
					catch (Exception e)
					{
						WormholeXTreme.thisPlugin.prettyLog(Level.WARNING,false,"Unable to get sign for stargate: " + s.Name + " and will be unable to change dial target.");
					}
				}
			}

			
			s.Active = DataUtils.byteToBoolean(byte_buff.get());
			s.temp_target_id = byte_buff.getLong();
			
			int facing_size = byte_buff.getInt();
			byte[] str_bytes = new byte[facing_size];
			byte_buff.get(str_bytes);
			String face_str = new String(str_bytes);
			s.Facing = BlockFace.valueOf(face_str);
			
			s.TeleportLocation.setYaw( WorldUtils.getDegreesFromBlockFace(s.Facing));
			s.TeleportLocation.setPitch(0);

			int idc_len = byte_buff.getInt();
			byte[] idc_bytes = new byte[idc_len];
			byte_buff.get(idc_bytes);
			s.IrisDeactivationCode = new String(idc_bytes);
			
			s.IrisActive = DataUtils.byteToBoolean(byte_buff.get());
			s.IrisDefaultActive = s.IrisActive;
			s.LitGate = DataUtils.byteToBoolean(byte_buff.get());
			
			int num_blocks = byte_buff.getInt();
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.Blocks.add( bl.getLocation() );
			}
			
			num_blocks = byte_buff.getInt();
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.WaterBlocks.add( bl.getLocation() );
			}
			
			num_blocks = byte_buff.getInt();
			for ( int i = 0; i < num_blocks; i++ )
			{
				byte_buff.get(bloc_array);
				Block bl = DataUtils.BlockFromBytes(bloc_array, w);
				s.LightBlocks.add( bl.getLocation() );
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
		File directory = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "GateShapes" + File.separator);
		if (!directory.exists()) 
		{
			
			try 
			{
				directory.mkdir();
			} 
			catch (Exception e) 
			{
				WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE,false,"Unable to make directory: " + e.getMessage());
			}
			try 
			{
				File standard_shape_file = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "GateShapes" + File.separator + "Standard.shape");
		    	InputStream is = WormholeXTreme.class.getResourceAsStream("/GateShapes/Standard.shape");
		    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    	BufferedWriter bw = new BufferedWriter(new FileWriter(standard_shape_file));
		    	
				for (String s = ""; (s = br.readLine()) != null; ) 
				{
					bw.write(s);
					bw.write("\n");
				}
				
				br.close();
				bw.close();
				is.close();
			} 
			catch (Exception e) 
			{
				WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE,false,"Unable to create files: " + e.getMessage());
			}
		}
		
		File[] shape_files = directory.listFiles();
			for ( File fi : shape_files )
			{
				if ( fi.getName().contains(".shape") )
				{
					WormholeXTreme.thisPlugin.prettyLog(Level.CONFIG, false, "Loading shape file: \"" + (String)fi.getName() + "\"");
					try
					{
						ArrayList<String> file_lines = new ArrayList<String>();
						BufferedReader bufferedreader = new BufferedReader(new FileReader(fi));
						for (String s = ""; (s = bufferedreader.readLine()) != null; ) 
						{
							file_lines.add(s);
						}
						bufferedreader.close();

						StargateShape shape = new StargateShape(file_lines.toArray(new String[]{}));
						shapes.put(shape.shapeName, shape);
					}
					catch (Exception e) 
					{
						WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
					}
					WormholeXTreme.thisPlugin.prettyLog(Level.CONFIG, false, "Completed loading shape file: \"" + (String)fi.getName() + "\"");
				}
			}
			
			if ( shapes.size() == 0 )
			{
				shapes.put( "Standard", new StargateShape());
			}
	}
}
