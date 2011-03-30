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
package com.wormhole_xtreme.wormhole.logic;


import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.model.Stargate;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Runnable thread for updating stargates.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class StargateUpdateRunnable implements Runnable
{
	
	/** The stargate. */
	private Stargate stargate;
	
	/** The player. */
	private Player player;
	
	/** The action. */
	private ActionToTake action;
	
	/**
	 * Instantiates a new stargate update runnable.
	 *
	 * @param s the s
	 * @param act the act
	 */
	public StargateUpdateRunnable(Stargate s, ActionToTake act)
	{
		this.stargate = s;
		this.action = act;
	}
	
	/**
	 * Instantiates a new stargate update runnable.
	 *
	 * @param s the s
	 * @param p the p
	 * @param act the act
	 */
	public StargateUpdateRunnable(Stargate s, Player p, ActionToTake act)
	{
		this(s, act);
		this.player = p;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
	    WormholeXTreme.thisPlugin.prettyLog(Level.FINE, false, "Run Action \"" + this.action.toString() + "\" Stargate \"" + this.stargate.Name + "\"");
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
		else if ( this.action == ActionToTake.AFTERSHUTDOWN )
		{
		    stargate.AfterShutdownStargate();
		}
	}
	
	/**
	 * The Enum ActionToTake.
	 */
	public enum ActionToTake
	{
		
		/** The SHUTDOWN task. */
		SHUTDOWN,
		
		/** The ANIMATE OPENING task. */
		ANIMATE_OPENING,
		
		/** The DEACTIVATE task. */
		DEACTIVATE,
		
		/** The AFTERSHUTDOWN task. */
		AFTERSHUTDOWN,
	}
	
}
