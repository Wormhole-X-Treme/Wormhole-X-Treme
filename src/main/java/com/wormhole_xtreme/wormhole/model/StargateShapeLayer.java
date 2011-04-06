package com.wormhole_xtreme.wormhole.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wormhole_xtreme.wormhole.WormholeXTreme;

public class StargateShapeLayer 
{
	/** The stargate_positions. */
	public int[][] stargatePositions = null; /*{ {0,2,0}, {0,3,0}, {0,4,0}, 
		{0,1,1}, {0,5,1}, 
		{0,0,2}, {0,6,2}, 
		{0,6,3}, {0,0,3}, 
		{0,0,4}, {0,6,4}, 
		{0,5,5}, {0,1,5}, 
		{0,2,6}, {0,3,6}, {0,4,6} };*/
	
	/** The sign_position. */
	public int[] signPosition = null;//{0,3,6};
	
	/** The enter_position. */
	public int[] enterPosition = null;//{0,0,3};
	
	/** The light_positions. */
	public int[][] lightPositions = null;//{3,4,11,12};
	
	/** The water_positions. */
	public int[][] waterPositions = null; /*{ {0,2,1}, {0,3,1}, {0,4,1}, 
			{0,1,2}, {0,2,2}, {0,3,2}, {0,4,2}, {0,5,2}, 
			{0,1,3}, {0,2,3}, {0,3,3}, {0,4,3}, {0,5,3}, 
			{0,1,4}, {0,2,4}, {0,3,4}, {0,4,4}, {0,5,4}, 
			{0,2,5}, {0,3,5}, {0,4,5} };*/

	public StargateShapeLayer(String[] layerLines, int height, int width)
	{
		ArrayList<Integer[]> blockPositions = new ArrayList<Integer[]>();
		ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>();
		ArrayList<ArrayList<Integer>> lightPositions = new ArrayList<ArrayList<Integer>>();
		
		int numBlocks = 0;
		
		// 1. scan all lines for lines beginning with [  - that is the height of the gate
		for ( int i = 0; i < layerLines.length; i++ )
		{
			Pattern p = Pattern.compile("(\\[.*?\\])");
			Matcher m = p.matcher(layerLines[i]);
			int j = 0;
			while ( m.find() )
			{
				String block = m.group(0);
				Integer[] point = { 0, (height - 1 - i), (width - 1 - j) };
				
				String[] modifiers = block.split(":");
				for ( String mod : modifiers )
				{
					if ( mod.equals("S") )
					{
						numBlocks++;
						blockPositions.add(point);
					}
					else if ( mod.equals("P") )
					{
						portalPositions.add(point);
					}
					else if ( mod.equals("N") || mod.equals("E") )
					{
						int[] pointI = new int[3];
						for (int k = 0; k < 3; k++ )
							pointI[k] = point[k];
						
						if ( block.contains("N") )
						{
							signPosition = pointI;
						}
						if ( block.contains("E") )
						{
							enterPosition = pointI;
						}
					}
					else if ( mod.contains("L") )
					{
						String[] light_parts = mod.split("#");
						int light_iteration = Integer.parseInt(light_parts[1]);
						if ( lightPositions.get(light_iteration) == null )
						{
							ArrayList<Integer> new_it = new ArrayList<Integer>();
							lightPositions.set(light_iteration, new_it);
						}
						
						lightPositions.get(light_iteration).add(numBlocks - 1);
					}
				}
				j++;
			}
		}
		//TODO: debug printout for the materials the gate uses.
		//TODO: debug printout for the redstone_activated
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(signPosition) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(enterPosition) + "\"");
		this.waterPositions = new int[portalPositions.size()][3];
		for ( int i = 0; i < portalPositions.size(); i++)
		{
			int[] point = new int[3];
			for (int j = 0; j < 3; j++ )
				point[j] = portalPositions.get(i)[j];
			this.waterPositions[i] = point;
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString((int[][])this.waterPositions) + "\"");
		
		this.lightPositions = new int[lightPositions.size()][];
		for ( int i = 0; i < lightPositions.size(); i++)
		{
			this.lightPositions[i] = new int[lightPositions.get(i).size()];
			for ( int j = 0; j < this.lightPositions[i].length; j++ )
			{
				this.lightPositions[i][j] = lightPositions.get(i).get(j);
			}
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
	}
}
