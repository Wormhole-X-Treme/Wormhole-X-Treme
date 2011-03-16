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


import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.model.Stargate;


/** 
 * WormholeXtreme Runnable thread for updating stargates  
 * @author Ben Echols (Lologarithm) 
 */ 
public class StargateUpdateRunnable implements Runnable
{
	private Stargate stargate;
	private Player player;
	private ActionToTake action;
	
	public StargateUpdateRunnable(Stargate s, ActionToTake act)
	{
		this.stargate = s;
		this.action = act;
	}
	
	public StargateUpdateRunnable(Stargate s, Player p, ActionToTake act)
	{
		this(s, act);
		this.player = p;
	}
	
	@Override
	public void run()
	{
	    WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Run Action \"" + this.action + "\" Stargate \"" + this.stargate.Name + "\"");
		if ( this.action == ActionToTake.SHUTDOWN )
		{    
			stargate.ShutdownStargate();
		}
		else if ( this.action == ActionToTake.ANIMATE_OPENING )
		{
			stargate.AnimateOpening();
		}
		else if ( this.action == ActionToTake.DEACTIVATE )
		{
			stargate.TimeoutStargate(player);
		}
	}
	
	public enum ActionToTake
	{
		SHUTDOWN,
		ANIMATE_OPENING,
		DEACTIVATE
	}
	
}
