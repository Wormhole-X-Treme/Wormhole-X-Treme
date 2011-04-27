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
package com.wormhole_xtreme.wormhole;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * The listener interface for receiving wormholeXTremeRedstone events.
 * The class that is interested in processing a wormholeXTremeRedstone
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addWormholeXTremeRedstoneListener<code> method. When
 * the wormholeXTremeRedstone event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see WormholeXTremeRedstoneEvent
 */
public class WormholeXTremeRedstoneListener extends BlockListener
{

    /**
     * Instantiates a new wormhole x treme redstone listener.
     */
    public WormholeXTremeRedstoneListener()
    {
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.block.BlockListener#onBlockRedstoneChange(org.bukkit.event.block.BlockRedstoneEvent)
     */
    @Override
    public void onBlockRedstoneChange(final BlockRedstoneEvent event)
    {
        // new current arrived
        if ((event.getOldCurrent() == 0) && (event.getNewCurrent() > 0))
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
