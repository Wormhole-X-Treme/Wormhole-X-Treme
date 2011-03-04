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
