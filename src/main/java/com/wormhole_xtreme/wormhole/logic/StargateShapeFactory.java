package com.wormhole_xtreme.wormhole.logic;

import com.wormhole_xtreme.wormhole.model.Stargate3DShape;
import com.wormhole_xtreme.wormhole.model.StargateShape;

public class StargateShapeFactory 
{

	public static StargateShape createShapeFromFile(String[] fileLines)
	{
		for ( String line : fileLines )
		{
			if ( line.startsWith("Version=2") )
			{
			//	if ( line.split("=")[1].equals("2") )
					return create3DShape(fileLines);
			}
		}
		
		return create2DShape(fileLines);
	}
	
	private static Stargate3DShape create3DShape(String[] fileLines)
	{
		return new Stargate3DShape(fileLines);
	}
	
	private static StargateShape create2DShape(String[] fileLines)
	{
		return new StargateShape(fileLines);
	}
}
