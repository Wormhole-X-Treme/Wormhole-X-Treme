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
	public ArrayList<Integer[]> blockPositions = new ArrayList<Integer[]>(); /*{ {0,2,0}, {0,3,0}, {0,4,0}, 
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

	/** The enter_position. */
	public int[] activationPosition = null;//{0,0,3};
	/** The enter_position. */
	public int[] irisActivationPosition = null;//{0,0,3};
	/** The enter_position. */
	public int[] dialerPosition = null;//{0,0,3};
	
	/** The light_positions. */
	public ArrayList<ArrayList<Integer[]>> lightPositions = new ArrayList<ArrayList<Integer[]>>();//{3,4,11,12};

	/** The positions of woosh. First array is the order to activate them. Inner array is list of points */
	public ArrayList<ArrayList<Integer[]>> wooshPositions = new ArrayList<ArrayList<Integer[]>>();//{3,4,11,12};
	
	/** The water_positions. */
	public ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>(); /*{ {0,2,1}, {0,3,1}, {0,4,1}, 
			{0,1,2}, {0,2,2}, {0,3,2}, {0,4,2}, {0,5,2}, 
			{0,1,3}, {0,2,3}, {0,3,3}, {0,4,3}, {0,5,3}, 
			{0,1,4}, {0,2,4}, {0,3,4}, {0,4,4}, {0,5,4}, 
			{0,2,5}, {0,3,5}, {0,4,5} };*/

	public StargateShapeLayer(String[] layerLines, int height, int width)
	{
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
					else if ( mod.equals("N") || mod.equals("E") || mod.equals("A") || mod.equals("D") || mod.equals("IA") )
					{
						int[] pointI = new int[3];
						for (int k = 0; k < 3; k++ )
							pointI[k] = point[k];
						
						if ( mod.equals("N") )
						{
							signPosition = pointI;
						}
						if ( mod.equals("E") )
						{
							enterPosition = pointI;
						}
						if ( mod.equals("A") )
						{
							activationPosition = pointI;
						}
						if ( mod.equals("D") )
						{
							dialerPosition = pointI;
						}
						if ( mod.equals("IA") )
						{
							irisActivationPosition = pointI;
						}
					}
					else if ( mod.contains("L") )
					{
						String[] light_parts = mod.split("#");
						int light_iteration = Integer.parseInt(light_parts[1]);
						if ( lightPositions.get(light_iteration) == null )
						{
							ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
							lightPositions.set(light_iteration, new_it);
						}
						
						lightPositions.get(light_iteration).add(point);
					}
					else if ( mod.contains("W") )
					{
						String[] w_parts = mod.split("#");
						int w_iteration = Integer.parseInt(w_parts[1]);
						if ( wooshPositions.get(w_iteration) == null )
						{
							ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
							wooshPositions.set(w_iteration, new_it);
						}
						
						wooshPositions.get(w_iteration).add(point);
					}
					// A, D, IA, RA, RD
				}
				j++;
			}
		}
		//TODO: debug printout for the materials the gate uses.
		//TODO: debug printout for the redstone_activated
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(signPosition) + "\"");
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(enterPosition) + "\"");
		//WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString((int[][])this.waterPositions) + "\"");
		
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + lightPositions + "\"");
		//WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString((int[][])this.stargatePositions) + "\"");
	}
}
