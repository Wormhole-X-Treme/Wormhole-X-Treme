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
import com.wormhole_xtreme.permissions.WXPermissions;
import com.wormhole_xtreme.permissions.WXPermissions.PermissionType;
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
	private static WormholeXTreme wxt = null;
	//private ConcurrentHashMap<String, Integer> PlayerCompassOn = new ConcurrentHashMap<String, Integer>(); 
	//private final WormholeXTreme plugin;
	/**
	 * Instantiates a new wormhole x treme player listener.
	 *
	 * @param wormholeXTreme the wormhole x treme
	 */
	public WormholeXTremePlayerListener(WormholeXTreme wormholeXTreme) 
	{ 
		//plugin = instance; 
		wxt = wormholeXTreme;
	}
 
	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent)
	 */
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
	    Block clicked = event.getClickedBlock();
	    Player player = event.getPlayer();

	    if (clicked != null && (clicked.getType() == Material.STONE_BUTTON || clicked.getType() == Material.LEVER ))
	    {
	        if ( !this.ButtonLeverHit(player, clicked, null) )
	        {  
	            event.setCancelled(true);
	        }
	        wxt.prettyLog(Level.INFO, false," " + clicked.getData() );
	    }
	    else if ( clicked != null && clicked.getType() == Material.WALL_SIGN )
	    {
	        Stargate stargate = StargateManager.getGateFromBlock(clicked);

	        if ( stargate != null )
	        {
	            if (WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN)) 
	            {
	                if ( stargate.TryClickTeleportSign(clicked) )
	                {
	                    String target = "";
	                    if ( stargate.SignTarget != null )
	                    {
	                        target = stargate.SignTarget.Name;
	                    }
	                    player.sendMessage("Dialer set to: " + target);
	                }
	            }
	            else 
	            {
	                player.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
	                event.setCancelled(true);
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

	        if (ConfigManager.getWormholeUseIsTeleport() && ((st.IsSignPowered && !WXPermissions.checkWXPermissions(p, st, PermissionType.SIGN)) ||
	            (!st.IsSignPowered && !WXPermissions.checkWXPermissions(p, st, PermissionType.DIALER))))
	        {
	            p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
	            return;
	        }
	        if ( st.Target.IrisActive )
	        {
	            p.sendMessage(ConfigManager.errorheader + "Remote Iris is locked!");
	            p.setNoDamageTicks(2);
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
	            double cost = ConfigManager.getIconomyWormholeUseCost();
	            if (!ConfigManager.getIconomyOpsExcempt() && !p.isOp() && cost != 0.0 && st.Owner != null && !st.Owner.equals(p.getName()) ) 
	            {
	                Account player_account = iConomy.getBank().getAccount(p.getName());
	                double balance = player_account.getBalance();
	                String currency = iConomy.getBank().getCurrency();
	                if ( balance >= cost )
	                {
	                    player_account.subtract(cost);
	                    // player_account.save();
	                    p.sendMessage(ConfigManager.normalheader + "Wormhole Use \u00A7F- \u00A72" + cost + " \u00A77" + currency );
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
	                    p.sendMessage(ConfigManager.errorheader + "Not enough " + currency  + "! - Requires: \u00A72" + cost + " \u00A77- Available: \u00A74" + player_account.getBalance() + " \u00A77" + currency);
	                    //p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to use - requires: " + cost);
	                    target = st.TeleportLocation;
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
	        p.setNoDamageTicks(2);
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
            if ( WXPermissions.checkWXPermissions(p, s, PermissionType.USE) )
            {
                if ( WorldUtils.isSameBlock(s.ActivationBlock, clicked) )
                {
                    return this.HandleGateActivationSwitch(s, p);
                }
                else if ( WorldUtils.isSameBlock(s.IrisActivationBlock, clicked) )
                {
                    this.HandleIrisActivationSwitch(s,p);
                    if ((s.Active) && (!s.IrisActive)) 
                    {
                        s.FillGateInterior(ConfigManager.getPortalMaterial());
                    }
                    return true;
                }
            }
            else
            {
                p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
            }
            
            return false;
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
                    return true;
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
                if ( WXPermissions.checkWXPermissions(p, new_gate, PermissionType.BUILD) )
                {
                    if ( new_gate.IsSignPowered )
                    {
                        p.sendMessage(ConfigManager.normalheader + "Stargate Design Valid with Sign Nav.");
                        if ( new_gate.Name.equals("") )
                        {
                            p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_INVALID) + "\"\"");
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
                                return false;
                            }
                        }
                        
                    }
                    else
                    {
                        // Print to player that it was successful!
                        p.sendMessage(ConfigManager.normalheader + "Valid Stargate Design! \u00A73:: \u00A7B<required> \u00A76[optional]");
                        p.sendMessage(ConfigManager.normalheader + "Type \'\u00A7F/wxcomplete \u00A7B<name> \u00A76[idc=IDC] [net=NET]\u00A77\' to complete.");
                        // Add gate to unnamed gates.
                        StargateManager.AddIncompleteStargate(p, new_gate);
                    }
                    return true;
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
                    return false;
                }   
            }
            else
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.FINEST, false, p.getName() + " has pressed a button or lever but did not find any properly created gates.");
                return true;
            }
        } 
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
     * @param stargate the stargate
     * @param player the player
     * @return true, if successful
     */
    private boolean HandleGateActivationSwitch(Stargate stargate, Player player) 
    {
        if ( stargate.Active || stargate.LitGate )
        {
            if ( stargate.Target != null)
            {
                //Shutdown stargate
                stargate.ShutdownStargate();
                player.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_SHUTDOWN));
                return true;
            }
            else
            {
                Stargate s2 = StargateManager.RemoveActivatedStargate(player);
                if ( s2 != null && stargate.GateId == s2.GateId )
                {
                    stargate.StopActivationTimer(player);
                    stargate.DeActivateStargate();
                    stargate.UnLightStargate();
                    player.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_DEACTIVATED));
                    return true;
                }
                else
                {
                    if ( stargate.LitGate && !stargate.Active )
                    {
                        player.sendMessage(ConfigManager.errorheader + "Gate has been activated by someone else already.");
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_REMOTE_ACTIVE));
                    }
                    return false;
                }
            }      
        }
        else
        {
            if ( stargate.IsSignPowered  )
            {          
                if ( WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN) )
                {
                    if ( stargate.TeleportSign == null && stargate.TeleportSignBlock != null )
                    {
                        stargate.TryClickTeleportSign(stargate.TeleportSignBlock);
                    }
                    
                    if ( stargate.SignTarget != null)
                    {
                        if ( stargate.DialStargate(stargate.SignTarget) )
                        {
                            player.sendMessage(ConfigManager.normalheader + "Stargates connected!");
                            return true;
                        }
                        else
                        {
                            player.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_REMOTE_ACTIVE));
                            return false;
                        }
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_INVALID));
                        return false;
                    }
                }
                else
                {
                    player.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
                    return false;
                }
            }
            else
            {
                //Activate Stargate
                player.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_ACTIVATED));
                player.sendMessage(ConfigManager.normalheader + "Chevrons Locked! \u00A73:: \u00A7B<required> \u00A76[optional]");
                player.sendMessage(ConfigManager.normalheader + "Type \'\u00A7F/dial \u00A7B<gatename> \u00A76[idc]\u00A77\'");
                StargateManager.AddActivatedStargate(player, stargate);
                stargate.StartActivationTimer(player);
                stargate.LightStargate();
                return true;
            }
        }
    }
} 
 
