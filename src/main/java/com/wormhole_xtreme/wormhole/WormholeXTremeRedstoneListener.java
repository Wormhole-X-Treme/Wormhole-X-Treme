package com.wormhole_xtreme.wormhole;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class WormholeXTremeRedstoneListener extends BlockListener 
{
	public WormholeXTremeRedstoneListener()
	{
	}
	
	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event)
	{
		// new current arrived
		if ( event.getOldCurrent() == 0 && event.getNewCurrent() > 0 )
		{
			//Block b = event.getBlock();
			
			// Check around this block for a stargate block
			/*for ( int x = -1; x < 2; x++)
			{
				
			}*/
			
			// If block is activationblock, toggle gate state
		}
	}
}
