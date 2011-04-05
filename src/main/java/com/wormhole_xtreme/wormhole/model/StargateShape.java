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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;

import com.wormhole_xtreme.wormhole.WormholeXTreme;



// TODO: Auto-generated Javadoc
/**
 * The Class StargateShape.
 */
public class StargateShape 
{
	
	/** The shape name. */
	public String shapeName = "Standard";
	
	/** The stargate_positions. */
	public int[][] stargatePositions = { {0,2,0}, {0,3,0}, {0,4,0}, 
		{0,1,1}, {0,5,1}, 
		{0,0,2}, {0,6,2}, 
		{0,6,3}, {0,0,3}, 
		{0,0,4}, {0,6,4}, 
		{0,5,5}, {0,1,5}, 
		{0,2,6}, {0,3,6}, {0,4,6} };
	
	/** The sign_position. */
	public int[] signPosition = {0,3,6};
	
	/** The enter_position. */
	public int[] enterPosition = {0,0,3};
	
	/** The light_positions. */
	public int[] lightPositions = {3,4,11,12};
	
	/** The water_positions. */
	public int[][] waterPositions = { {0,2,1}, {0,3,1}, {0,4,1}, 
			{0,1,2}, {0,2,2}, {0,3,2}, {0,4,2}, {0,5,2}, 
			{0,1,3}, {0,2,3}, {0,3,3}, {0,4,3}, {0,5,3}, 
			{0,1,4}, {0,2,4}, {0,3,4}, {0,4,4}, {0,5,4}, 
			{0,2,5}, {0,3,5}, {0,4,5} };
	
	/** The reference_vector. */
	public int[] referenceVector = {0,1,0};
	
	/** [0] = Left - / Right + [1] = Up + / Down - [2] = Forward + / Backward -. */
	public int[] toGateCorner = {1,-1, 4};
	
	/** The woosh_depth. */
	public int wooshDepth = 0;
	/** The square of the woosh_depth, used in comparisions with squared distance */
	public final int wooshDepthSquared;
	
	public Material portalMaterial = Material.STATIONARY_WATER;
	public Material irisMaterial = Material.STONE;
	public Material stargateMaterial = Material.OBSIDIAN;
	public Material activeMaterial = Material.GLOWSTONE;
	
	public boolean redstoneActivated = false;
	/**
	 * Instantiates a new stargate shape.
	 */
	public StargateShape()
	{
		wooshDepth = 3;
		wooshDepthSquared = 9;
	}
	
	/**
	 * Instantiates a new stargate shape.
	 *
	 * @param file_data the file_data
	 */
	public StargateShape(String[] file_data)
	{
		signPosition = null;
		enterPosition = null;
		
		ArrayList<Integer[]> blockPositions = new ArrayList<Integer[]>();
		ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>();
		ArrayList<Integer> lightPositions = new ArrayList<Integer>();
		
		int numBlocks = 0;
		int curWooshDepth = 3;
		
		// 1. scan all lines for lines beginning with [  - that is the height of the gate
		int height = 0;
		int width = 0;
		for ( int i = 0; i < file_data.length; i++ )
		{
			String line = file_data[i];
			
			if ( line.contains("Name=") )
			{
				this.shapeName = line.split("=")[1];
				WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + (String)this.shapeName + "\"");
			}
			else if ( line.equals("GateShape=") )
			{
				int index = i + 1;
				while ( file_data[index].startsWith("[") )
				{
					if ( width <= 0 )
					{
						Pattern p = Pattern.compile("(\\[.*?\\])");
						Matcher m = p.matcher(file_data[index]);
						while ( m.find() )
							width++;
					}
						
					height++; index++;
				}
					
				// At this point we should know the height and width
				if ( height <= 0 || width <= 0)
				{
				    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to parse custom gate due to incorrect height or width: \"" + (String)this.shapeName + "\"");
					throw new IllegalArgumentException("Unable to parse custom gate due to incorrect height or width: \"" + (String)this.shapeName + "\"");
				}
				else 
				{
				    WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG,false,"Shape: \"" + (String)this.shapeName + "\"" + " Height: \"" + Integer.toString((int)height) + "\"" + " Width: \"" + Integer.toString((int)width) + "\"" );
				}
					
				// Now parse each [X] and put into int array.
				index = i + 1;
				while ( file_data[index].startsWith("[") )
				{
						
					Pattern p = Pattern.compile("(\\[.*?\\])");
					Matcher m = p.matcher(file_data[index]);
					int j = 0;
					while ( m.find() )
					{
						String block = m.group(0);
						Integer[] point = { 0, (height - 1 - (index-i-1)), (width - 1 - j) };
						if ( block.contains("O") )
						{
							numBlocks++;
							blockPositions.add(point);
						}
						else if ( block.contains("P") )
						{
							portalPositions.add(point);
						}
						
							
						if ( block.contains("S") || block.contains("E") )
						{
							int[] pointI = new int[3];
							for (int k = 0; k < 3; k++ )
								pointI[k] = point[k];
							
							if ( block.contains("S") )
							{
								signPosition = pointI;
							}
							if ( block.contains("E") )
							{
								enterPosition = pointI;
							}
						}
							
						if ( block.contains("L") && block.contains("O") )
						{
							lightPositions.add( numBlocks - 1);
						}
							
						j++;
					}
					index++;
				}
			}
			else if ( line.contains("BUTTON_UP") )
			{
				toGateCorner[1] = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("BUTTON_RIGHT") )
			{
				toGateCorner[0] = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("BUTTON_AWAY") )
			{
				toGateCorner[2] = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("WOOSH_DEPTH") )
			{
				curWooshDepth = Integer.parseInt(line.split("=")[1]);
			}
			else if ( line.contains("PORTAL_MATERIAL") )
			{
				portalMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("IRIS_MATERIAL") )
			{
				irisMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("STARGATE_MATERIAL") )
			{
				stargateMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("ACTIVE_MATERIAL") )
			{
				activeMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("REDSTONE_ACTIVATED") )
			{
				redstoneActivated = Boolean.parseBoolean(line.split("=")[1].toLowerCase());
			}
		}
		//TODO: debug printout for the materials the gate uses.
		//TODO: debug printout for the redstone_activated
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(signPosition) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(enterPosition) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Button Position [Left/Right,Up/Down,Forward/Back]: \"" + Arrays.toString((int[])toGateCorner) + "\"");
		this.waterPositions = new int[portalPositions.size()][3];
		for ( int i = 0; i < portalPositions.size(); i++)
		{
			int[] point = new int[3];
			for (int j = 0; j < 3; j++ )
				point[j] = portalPositions.get(i)[j];
			this.waterPositions[i] = point;
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString((int[][])this.waterPositions) + "\"");
		
		this.lightPositions = new int[lightPositions.size()];
		for ( int i = 0; i < lightPositions.size(); i++)
		{
			this.lightPositions[i] = lightPositions.get(i);
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + lightPositions + "\"");
			
		this.stargatePositions = new int[blockPositions.size()][3];
		for ( int i = 0; i < blockPositions.size(); i++)
		{
			int[] point = new int[3];
			for (int j = 0; j < 3; j++ )
				point[j] = blockPositions.get(i)[j];
			this.stargatePositions[i] = point;
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString((int[][])this.stargatePositions) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + (String)this.shapeName + "\"");

		wooshDepth = curWooshDepth;
		wooshDepthSquared = curWooshDepth * curWooshDepth;
	}
}
