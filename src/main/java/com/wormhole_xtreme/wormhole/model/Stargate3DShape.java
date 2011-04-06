package com.wormhole_xtreme.wormhole.model;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;

import com.wormhole_xtreme.wormhole.WormholeXTreme;

public class Stargate3DShape extends StargateShape 
{
	/**
	 * Layers of the 3D shape. Layers go from 1 - 10
	 */
	ArrayList<StargateShapeLayer> layers = new ArrayList<StargateShapeLayer>();

	public Stargate3DShape(String[] fileLines)
	{
		signPosition = null;
		enterPosition = null;
	
		// 1. scan all lines for lines beginning with [  - that is the height of the gate
		int height = 0;
		int width = 0;
		for ( int i = 0; i < fileLines.length; i++ )
		{
			String line = fileLines[i];
			
			if ( line.contains("Name=") )
			{
				this.shapeName = line.split("=")[1];
				WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + (String)this.shapeName + "\"");
			}
			else if ( line.equals("GateShape=") )
			{
				int index = i;
				// Find start of first line
				while ( !fileLines[index].startsWith("[") )
				{
					index++;
				}
				
				while ( fileLines[index].startsWith("[") )
				{
					if ( width <= 0 )
					{
						Pattern p = Pattern.compile("(\\[.*?\\])");
						Matcher m = p.matcher(fileLines[index]);
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
					
				index = i + 1;
				// Now parse each layer
				if ( fileLines[index].startsWith("Layer") )
				{
					// 1. get layer #
					
					// 2. add each line that starts with [ to a new string[]
					// 3. call constructor
					index++;
				}
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
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + (String)this.shapeName + "\"");
	}
}
