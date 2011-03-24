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
package com.wormhole_xtreme; 

import java.util.logging.Level;


import org.bukkit.Location; 
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener; 
import org.bukkit.event.player.PlayerMoveEvent; 

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.logic.StargateHelper;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.model.StargateShape;
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;
import com.wormhole_xtreme.utils.WorldUtils;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Player Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremePlayerListener extends PlayerListener 
{ 
	
	/** The wxt. */
	private WormholeXTreme wxt = null;
	//private ConcurrentHashMap<String, Integer> PlayerCompassOn = new ConcurrentHashMap<String, Integer>(); 
	//private final WormholeXTreme plugin;
	/**
	 * Instantiates a new wormhole x treme player listener.
	 *
	 * @param instance the instance
	 */
	public WormholeXTremePlayerListener(WormholeXTreme instance) 
	{ 
		//plugin = instance; 
		wxt = instance;
	}
 
	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent)
	 */
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
	    Block clicked = event.getClickedBlock();
	    Player player = event.getPlayer();
	    if ( clicked.getType() == Material.STONE_BUTTON || clicked.getType() == Material.LEVER )
	    {
	        if ( this.ButtonLeverHit(player, clicked, null) )
	        {
	            event.setCancelled(true);
	        }
	    }
	    else if ( clicked.getType() == Material.WALL_SIGN )
	    {
	        Stargate s = StargateManager.getGateFromBlock(clicked);

	        if ( s != null )
	        {
	            String signnetwork;
	            if (s.Network != null )
	            {
	                signnetwork = s.Network.netName;
	            }
	            else
	            {
	                signnetwork = "Public";
	            }
	            Boolean allowed = false;
	            if ( WormholeXTreme.permissions != null && ConfigManager.getSimplePermissions())
	            {
	                if (WormholeXTreme.permissions.has(player, "wormhole.simple.use"))
	                {
	                    allowed = true;
	                }
	            }
	            else if (WormholeXTreme.permissions != null && !ConfigManager.getSimplePermissions())
	            {
	                if ( WormholeXTreme.permissions.has(player, "wormhole.use.sign") && (signnetwork.equals("Public") || (!signnetwork.equals("Public") && WormholeXTreme.permissions.has(player, "wormhole.network.use." + signnetwork))))
	                {
	                    allowed = true;
	                }
	            }
	            else 
	            {
	                PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, s);
	                if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
	                {
	                    allowed = true;
	                }
	            }


	            if ( player.isOp() || allowed) 
	            {
	                if ( s.TryClickTeleportSign(clicked) )
	                {
	                    String target = "";
	                    if ( s.SignTarget != null )
	                    {
	                        target = s.SignTarget.Name;
	                    }
	                    player.sendMessage("Dialer set to: " + target);
	                }
	            }
	            else 
	            {
	                player.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
	            }
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerMove(org.bukkit.event.player.PlayerMoveEvent)
     */
	@Override
    public void onPlayerMove(PlayerMoveEvent event)
	{
		Player p = event.getPlayer();
		Location l = event.getTo();
		Block ch = l.getWorld().getBlockAt( l.getBlockX(), l.getBlockY(), l.getBlockZ());
		Stargate st = StargateManager.getGateFromBlock( ch );

		if ( st != null && st.Active && st.Target != null )
		{
		    String gatenetwork;
		    if (st.Network != null )
		    {
		        gatenetwork = st.Network.netName;
		    }
		    else
		    {
		        gatenetwork = "Public";
		    }
			wxt.prettyLog(Level.FINE, false, "Player in gate:" + st.Name + " gate Active: " + st.Active + " Target Gate: " + st.Target.Name + " Network: " + gatenetwork );
			
			if ( WormholeXTreme.permissions != null)
			{
			    // If use permission is also teleport permission we should check here:
				if (ConfigManager.getWormholeUseIsTeleport() && !ConfigManager.getSimplePermissions() && 
					((st.IsSignPowered && !WormholeXTreme.permissions.permission(p, "wormhole.use.sign")) || 
					(!st.IsSignPowered && !WormholeXTreme.permissions.permission(p, "wormhole.use.dialer")) || 
					(!gatenetwork.equals("Public") && !WormholeXTreme.permissions.has(p, "wormhole.network.use." + gatenetwork))))
				{
					// This means that the user doesn't have permission to use.
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
					return;
				}
				else if (ConfigManager.getWormholeUseIsTeleport() && ConfigManager.getSimplePermissions() &&
				    ((st.IsSignPowered && !WormholeXTreme.permissions.has(p, "wormhole.simple.use"))))
				{
				    p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
				    return;
				}
			}
			
			if ( st.Target.IrisActive )
			{
				p.sendMessage("\u00A73:: \u00A75error \u00A73:: \u00A77Remote Iris is locked!");
				//p.sendMessage("Remote Iris is active - unable to teleport!");
				event.setFrom(st.TeleportLocation);
				event.setTo(st.TeleportLocation);
				p.teleport(st.TeleportLocation);
				if (p.getFireTicks() > 0 )
				{
				    p.setFireTicks(0);
				}
				return;
			}
		
			Location target = st.Target.TeleportLocation;
			if ( WormholeXTreme.iconomy != null )
			{
				boolean exempt = ConfigManager.getIconomyOpsExcempt();
				if ( !exempt || !p.isOp() )
				{
					double cost = ConfigManager.getIconomyWormholeUseCost();
					if (cost != 0.0) 
					{
						Account player_account = iConomy.getBank().getAccount(p.getName());
						double balance = player_account.getBalance();
						String currency = iConomy.getBank().getCurrency();
					    if ( balance >= cost )
					    {
						    player_account.subtract(cost);
						    // player_account.save();
						    p.sendMessage("\u00A73:: \u00A77Wormhole Use \u00A7F- \u00A72" + cost + " \u00A77" + currency );
						    //p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to use wormhole." );
						    double owner_percent = ConfigManager.getIconomyWormholeOwnerPercent();
						
						    if ( owner_percent != 0.0 && st.Owner != null )
						    {
							    if ( st.Owner != null && iConomy.getBank().hasAccount(st.Owner))
							    {
								    Account own_acc = iConomy.getBank().getAccount(st.Owner);
								    own_acc.add(cost * owner_percent);
								    // own_acc.save();
							    }
						    }
					    }
					    else
					    {
						    p.sendMessage("\u00A73:: \u00A77Not enough " + currency  + "! - Requires: \u00A72" + cost + " \u00A77- Available: \u00A74" + player_account.getBalance() + " \u00A77" + currency);
						    //p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to use - requires: " + cost);
						    target = st.TeleportLocation;
					    }
					}
				}
			}
			
			Block target_block = target.getWorld().getBlockAt(target.getBlockX(), target.getBlockY(), target.getBlockZ());
			while ( target_block.getType() != Material.AIR && target_block.getType() != Material.WATER && target_block.getType() != Material.LAVA )
			{
				target_block = target_block.getFace(BlockFace.UP);
				target.setY(target.getY() + 1.0);
			}		
			event.setFrom(target);
			event.setTo(target);
			p.teleport(target);
			event.setCancelled(true);
			if ( target == st.Target.TeleportLocation )
				wxt.prettyLog(Level.INFO,false, p.getDisplayName() + " used wormhole: " + st.Name + " to go to: " + st.Target.Name);
			
			if ( ConfigManager.getTimeoutShutdown() == 0 )
			{
				st.ShutdownStargate();
			}
		}
		else if ( st != null )
		{
			wxt.prettyLog(Level.FINE, false, "Player entered gate but wasn't active or didn't have a target.");
		}
	}
	
	   /**
     * Button lever hit.
     *
     * @param p the p
     * @param clicked the clicked
     * @param direction the direction
     * @return true, if successful
     */
    private boolean ButtonLeverHit(Player p, Block clicked, BlockFace direction)
    {
        Stargate s = StargateManager.getGateFromBlock(clicked);
        
        if ( s != null  )
        {
            PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, s);
            
            String gatenetwork;
            if (s.Network != null)
            {
                gatenetwork = s.Network.netName;
            }
            else
            {
                gatenetwork = "Public";
            }
            boolean allowed = false;
            if ( WormholeXTreme.permissions != null && ConfigManager.getSimplePermissions() && WormholeXTreme.permissions.has(p, "wormhole.simple.use"))
            {    
                allowed = true;
            }
            else if ( WormholeXTreme.permissions != null && !ConfigManager.getSimplePermissions())
            {
                if ( (gatenetwork.equals("Public") || (!gatenetwork.equals("Public") && WormholeXTreme.permissions.has(p, "wormhole.network.use." + gatenetwork))) && 
                    (WormholeXTreme.permissions.has(p, "wormhole.use.sign") || WormholeXTreme.permissions.has(p, "wormhole.use.dialer")) )
                {    
                    allowed = true;
                }               
            }
            else if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
            {
                allowed = true;
            }

            if ( p.isOp() || allowed )
            {
                if ( WorldUtils.isSameBlock(s.ActivationBlock, clicked) )
                {
                    this.HandleGateActivationSwitch(s, p);
                }
                else if ( WorldUtils.isSameBlock(s.IrisActivationBlock, clicked) )
                {
                    this.HandleIrisActivationSwitch(s,p);
                    if ((s.Active) && (!s.IrisActive)) 
                    {
                        s.FillGateInterior(ConfigManager.getPortalMaterial());
                    }
                }
            }
            else
            {
                p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
            }
            
            return true;
        }
        else 
        {
            if ( direction == null )
            {
                switch ( clicked.getData() )
                {
                case 1:
                    direction = BlockFace.SOUTH;
                    break;
                case 2:
                    direction = BlockFace.NORTH;
                    break;
                case 3:
                    direction = BlockFace.WEST;
                    break;
                case 4:
                    direction = BlockFace.EAST;
                    break;
                }
                
                if ( direction == null)
                {
                    return false;
                }
            }
            // Check to see if player has already run the "build" command.
            StargateShape shape = StargateManager.GetPlayerBuilderShape(p);
            
            Stargate new_gate = null;
            if ( shape != null )
            {
                new_gate = StargateHelper.checkStargate(clicked, direction, shape);
            }
            else
            {
                new_gate = StargateHelper.checkStargate(clicked, direction);
            }
            
            if ( new_gate != null )
            {
                boolean allowed = false;
                if ( WormholeXTreme.permissions != null && !ConfigManager.getSimplePermissions())
                {
                    if ( WormholeXTreme.permissions.has(p, "wormhole.build"))
                    {
                        allowed = true;
                    }
                }
                else if ( WormholeXTreme.permissions != null && ConfigManager.getSimplePermissions())
                {
                    if (WormholeXTreme.permissions.has(p, "wormhole.simple.build"))
                    {
                        allowed = true;
                    }
                }
                else 
                {
                    PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, new_gate);
                    if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
                    {
                        allowed = true;
                    }
                }

                if ( p.isOp() || allowed )
                {
                    if ( new_gate.IsSignPowered )
                    {
                        p.sendMessage("\u00A73:: \u00A75completed \u00A73:: \u00A77Stargate Design Valid with Sign Nav.");
                        if ( new_gate.Name.equals("") )
                        {
                            p.sendMessage("\u00A73:: \u00A74error \u00A73:: \u00A77Stargate name invalid. Replace sign and try again.");
                            p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_INVALID));
                        }
                        else
                        {
                            boolean success = StargateManager.CompleteStargate(p, new_gate);
                            if ( success )
                            {
                                p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_SUCCESS));
                                new_gate.TeleportSign.setLine(0, "-" + new_gate.Name + "-" );
                                new_gate.TeleportSign.setData(new_gate.TeleportSign.getData());
                                new_gate.TeleportSign.update();
                            }
                            else
                            {
                                p.sendMessage("Stargate constrution failed!?");
                            }
                        }
                        
                    }
                    else
                    {
                        // Print to player that it was successful!
                        p.sendMessage("\u00A73:: \u00A75Valid Stargate Design! \u00A73:: \u00A7B<required> \u00A76[optional]");
                        p.sendMessage("\u00A73:: \u00A77Type \'\u00A7F/wxcomplete \u00A7B<name> \u00A76[idc=IDC] [net=NET]\u00A77\' to complete.");
                        // Add gate to unnamed gates.
                        StargateManager.AddIncompleteStargate(p, new_gate);
                    }
                }
                else
                {
                    if ( new_gate.IsSignPowered )
                    {
                        new_gate.Network.gate_list.remove(new_gate);
                        new_gate.TeleportSign.setLine(0, new_gate.Name);
                        if (new_gate.Network != null)
                        {
                            new_gate.TeleportSign.setLine(1, new_gate.Network.netName );
                        }
                        new_gate.TeleportSign.setData(new_gate.TeleportSign.getData());
                        new_gate.TeleportSign.update();
                    }
                    StargateManager.RemoveIncompleteStargate(p);
                    p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
                }   
                return true;
            }
            else
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINEST, false, p.getName() + " has pressed a button or level but did not find any properly created gates.");
            }
        }
        
        return false;
    }
    
    /**
     * Handle iris activation switch.
     *
     * @param s the s
     * @param p the p
     */
    private void HandleIrisActivationSwitch(Stargate s, Player p) 
    {
        s.ToggleIrisLever();
    }

    /**
     * Handle gate activation switch.
     *
     * @param s the s
     * @param p the p
     */
    private void HandleGateActivationSwitch(Stargate s, Player p) 
    {
        if ( s.Active || s.LitGate )
        {
            if ( s.Target != null)
            {
                //Shutdown stargate
                s.ShutdownStargate();
                p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_SHUTDOWN));
            }
            else
            {
                Stargate s2 = StargateManager.RemoveActivatedStargate(p);
                if ( s2 != null && s.GateId == s2.GateId )
                {
                    s.StopActivationTimer(p);
                    s.DeActivateStargate();
                    s.UnLightStargate();
                    p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_DEACTIVATED));
                }
                else
                {
                    if ( s.LitGate && !s.Active )
                    {
                        p.sendMessage("Gate has been activated by someone else already.");
                    }
                    else
                    {
                        p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_REMOTE_ACTIVE));
                    }
                }
            }
                
        }
        else
        {
            if ( s.IsSignPowered  )
            {
                boolean allowed = false;
                if ( WormholeXTreme.permissions != null && !ConfigManager.getSimplePermissions())
                {
                    if ( WormholeXTreme.permissions.has(p, "wormhole.use.sign") )
                    {
                        allowed = true;
                    }
                }
                else if (WormholeXTreme.permissions != null && ConfigManager.getSimplePermissions())
                {
                    if (WormholeXTreme.permissions.has(p, "wormhole.simple.use"))
                    {
                        allowed = true;
                    }
                }
                else
                {
                    allowed = true;
                }
                
                if ( p.isOp() || allowed )
                {
                    if ( s.TeleportSign == null && s.TeleportSignBlock != null )
                    {
                        s.TryClickTeleportSign(s.TeleportSignBlock);
                    }
                    
                    if ( s.SignTarget != null)
                    {
                        if ( s.DialStargate(s.SignTarget) )
                        {
                            p.sendMessage("\u00A73:: \u00A75Stargates connected!");
                        }
                        else
                        {
                            p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_REMOTE_ACTIVE));
                        }
                    }
                    else
                    {
                        p.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_INVALID));
                    }
                }
                else
                {
                    p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
                }
            }
            else
            {
                //Activate Stargate
                p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_ACTIVATED));
                p.sendMessage("\u00A73:: \u00A75Chevrons Locked! \u00A73:: \u00A7B<required> \u00A76[optional]");
                p.sendMessage("\u00A73:: \u00A77Type \'\u00A7F/dial \u00A7B<gatename> \u00A76[idc]\u00A77\'");
                StargateManager.AddActivatedStargate(p, s);
                s.StartActivationTimer(p);
                s.LightStargate();
            }
        }
    }
} 
 
