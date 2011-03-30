/**
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
package com.wormhole_xtreme.wormhole.event;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;


/**
 * The Stargate Minecart Teleport Event Class.
 *
 * @author alron
 */
public class StargateMinecartTeleportEvent extends Event {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1176071751488327352L;
    
    /** The old minecart. */
    Minecart oldMinecart;
    
    /** The new minecart. */
    Minecart newMinecart;
    
    /**
     * Instantiates a new stargate minecart teleport event.
     *
     * @param oldMinecart the old minecart
     * @param newMinecart the new minecart
     */
    public StargateMinecartTeleportEvent(Minecart oldMinecart, Minecart newMinecart) 
    {
        super("StargateMinecartTeleportEvent");
        this.oldMinecart = oldMinecart;
        this.newMinecart = newMinecart;
    }
    
    /**
     * Gets the old minecart.
     *
     * @return the old minecart
     */
    public Minecart getOldMinecart() 
    {
        return this.oldMinecart;
    }
    
    /**
     * Gets the new minecart.
     *
     * @return the new minecart
     */
    public Minecart getNewMinecart() 
    {
        return this.newMinecart;
    }

}
