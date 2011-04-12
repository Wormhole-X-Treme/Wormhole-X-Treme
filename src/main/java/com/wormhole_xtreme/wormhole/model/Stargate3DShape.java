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
	public ArrayList<StargateShapeLayer> layers = new ArrayList<StargateShapeLayer>();
	public int activation_layer = -1;
	public int sign_layer = -1;
	
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
			
			if ( line.startsWith("#") )
				continue;
			
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
			}
			else if ( line.startsWith("Layer") )
			{
				// TODO : Add some debug output for each layer!
				// 1. get layer #
				int layer = Integer.valueOf(line.trim().split("[#=]")[1]);
				
				// 2. add each line that starts with [ to a new string[]
				i++;
				String[] layerLines = new String[height];
				int line_index = 0;
				while ( fileLines[i].startsWith("[") || fileLines[i].startsWith("#"))
				{
				    WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG,false,"Layer=" + layer + " i=" + i + " line_index=" + line_index + " Line=" + fileLines[i] );
					layerLines[line_index] = fileLines[i];
					i++;

					if ( fileLines[i].startsWith("#") )
					{
						continue;
					}

					line_index++;
				}
				
				// 3. call constructor
				StargateShapeLayer ssl = new StargateShapeLayer(layerLines, height, width);
				// bad hack to make sure list is big enough :(
				while ( layers.size() <= layer )
				{
					layers.add(null);
				}
				layers.set(layer, ssl);
				
				if ( ssl.activationPosition != null )
					this.activation_layer = layer;
				if ( ssl.dialerPosition != null )
					this.sign_layer = layer;
			}
			else if ( line.contains("PORTAL_MATERIAL=") )
			{
				portalMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("IRIS_MATERIAL=") )
			{
				irisMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("STARGATE_MATERIAL=") )
			{
				stargateMaterial = Material.valueOf(line.split("=")[1]);
			}
			else if ( line.contains("ACTIVE_MATERIAL=") )
			{
				activeMaterial = Material.valueOf(line.split("=")[1]);
			}
		}
		WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + (String)this.shapeName + "\"");
	}
}
