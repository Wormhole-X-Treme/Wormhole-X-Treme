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

import java.util.logging.Level;

import org.bukkit.Location; 
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener; 
import org.bukkit.event.player.PlayerMoveEvent; 

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.logic.StargateHelper;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.model.StargateShape;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.wormhole.utils.TeleportUtils;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme Player Listener.
 *
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */ 
public class WormholeXTremePlayerListener extends PlayerListener 
{ 
 
	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent)
	 */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        final PlayerInteractEvent eventFinal = event;
        if (eventFinal.getClickedBlock() != null)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false,"Caught Player: \"" + eventFinal.getPlayer().getName() + "\" Event type: \"" + eventFinal.getType().toString() + "\" Action Type: \"" + eventFinal.getAction().toString() + "\" Event Block Type: \"" + eventFinal.getClickedBlock().getType().toString() + "\" Event World: \"" + eventFinal.getClickedBlock().getWorld().toString() + "\" Event Block: " + eventFinal.getClickedBlock().toString() + "\"");
            if (handlePlayerInteractEvent(eventFinal))
            {
                eventFinal.setCancelled(true);
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false,"Cancelled Player: \"" + eventFinal.getPlayer().getName() + "\" Event type: \"" + eventFinal.getType().toString() + "\" Action Type: \"" + eventFinal.getAction().toString() + "\" Event Block Type: \"" + eventFinal.getClickedBlock().getType().toString() + "\" Event World: \"" + eventFinal.getClickedBlock().getWorld().toString() + "\" Event Block: " + eventFinal.getClickedBlock().toString() +  "\"");
            }
        }
        else 
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Caught and ignored Player: \"" + eventFinal.getPlayer().getName() + "\" Event type: \"" + eventFinal.getType().toString() + "\"");
        }
    }
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerBucketFill(org.bukkit.event.player.PlayerBucketFillEvent)
	 */
	@Override
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
	    if (!event.isCancelled())
	    {
	        final Stargate stargate = StargateManager.getGateFromBlock(event.getBlockClicked());
	        if (stargate != null || StargateManager.isBlockInGate(event.getBlockClicked()))
	        {
	            event.setCancelled(true);
	        }
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent)
	 */
	@Override
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
	    if (!event.isCancelled())
	    {
	        final Stargate stargate = StargateManager.getGateFromBlock(event.getBlockClicked());
	        if (stargate != null || StargateManager.isBlockInGate(event.getBlockClicked()))
	        {
	            event.setCancelled(true);
	        }
	    }
	}
	
	/**
	 * Handle player interact event.
	 *
	 * @param event the event
	 * @return true, if successful
	 */
	private static boolean handlePlayerInteractEvent(PlayerInteractEvent event)
	{
	    final PlayerInteractEvent eventFinal = event;
	    final Block blockClickedFinal = eventFinal.getClickedBlock();
	    final Player playerFinal = eventFinal.getPlayer();

	    if (blockClickedFinal != null && (blockClickedFinal.getType() == Material.STONE_BUTTON || blockClickedFinal.getType() == Material.LEVER ))
	    {
	        if ( buttonLeverHit(playerFinal, blockClickedFinal, null) )
	        {  
	            return true;
	        }
	    }
	    else if ( blockClickedFinal != null && blockClickedFinal.getType() == Material.WALL_SIGN )
	    {
	        final Stargate stargate = StargateManager.getGateFromBlock(blockClickedFinal);
	        if ( stargate != null )
	        {
	            if (WXPermissions.checkWXPermissions(playerFinal, stargate, PermissionType.SIGN)) 
	            {
	                if ( stargate.tryClickTeleportSign(blockClickedFinal) )
	                {
	                    final Player schedulePlayer = playerFinal;
	                    final Stargate scheduleStargate = stargate;
	                    WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new Runnable() {
	                       public void run()
	                       {
	                           String target = "";
	                           if (scheduleStargate != null && scheduleStargate.signTarget != null)
	                           {
	                               target = scheduleStargate.signTarget.name;
	                               schedulePlayer.sendMessage("Dialer set to: " + target);
	                           }
	                           else
	                           {
	                               schedulePlayer.sendMessage("No available target to set dialer to.");
	                           }
	                       }
	                    },2);
	                    // player.sendMessage("Dialer set to: " + target);
	                    return true;
	                }
	            }
	            else 
	            {
	                playerFinal.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
	                return true;
	            }
	        }
	    }
	    return false;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.event.player.PlayerListener#onPlayerMove(org.bukkit.event.player.PlayerMoveEvent)
     */
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
	    final PlayerMoveEvent eventFinal = event;
	    if (handlePlayerMoveEvent(eventFinal))
	    {
	        eventFinal.setCancelled(true);
	    }
	}
	
	/**
	 * Handle player move event.
	 *
	 * @param event the event
	 * @return true, if successful
	 */
	private static boolean handlePlayerMoveEvent(PlayerMoveEvent event)
	{
	    final PlayerMoveEvent eventFinal = event;
	    final Player playerFinal = eventFinal.getPlayer();
	    final Location toLocFinal = eventFinal.getTo();
	    final Block gateBlockFinal = toLocFinal.getWorld().getBlockAt( toLocFinal.getBlockX(), toLocFinal.getBlockY(), toLocFinal.getBlockZ());
	    final Stargate stargateFinal = StargateManager.getGateFromBlock( gateBlockFinal );

	    if ( stargateFinal != null && stargateFinal.active && stargateFinal.target != null && stargateFinal.gateShape != null && 
	        gateBlockFinal.getType() == stargateFinal.gateShape.portalMaterial )
	    {
	        String gatenetwork;
	        if (stargateFinal.network != null )
	        {
	            gatenetwork = stargateFinal.network.netName;
	        }
	        else
	        {
	            gatenetwork = "Public";
	        }
	        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player in gate:" + stargateFinal.name + " gate Active: " + stargateFinal.active + " Target Gate: " + stargateFinal.target.name + " Network: " + gatenetwork );

	        if (ConfigManager.getWormholeUseIsTeleport() && ((stargateFinal.isSignPowered && !WXPermissions.checkWXPermissions(playerFinal, stargateFinal, PermissionType.SIGN)) ||
	            (!stargateFinal.isSignPowered && !WXPermissions.checkWXPermissions(playerFinal, stargateFinal, PermissionType.DIALER))))
	        {
	            playerFinal.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
	            return false;
	        }
	        if ( stargateFinal.target.irisActive )
	        {
	            playerFinal.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Remote Iris is locked!");
	            playerFinal.setNoDamageTicks(5);
	            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new Runnable() {
	                public void run()
	                {
	                    eventFinal.setFrom(stargateFinal.teleportLocation);
	                    eventFinal.setTo(stargateFinal.teleportLocation);
	                    playerFinal.teleport(stargateFinal.teleportLocation);
	                }
	            },2);
	            return true;
	        }

	        Location target = stargateFinal.target.teleportLocation;
	        if ( WormholeXTreme.getIconomy() != null )
	        {
	            double cost = ConfigManager.getIconomyWormholeUseCost();
	            boolean charge = true;
	            if ((ConfigManager.getIconomyOpsExcempt() && playerFinal.isOp()) || (stargateFinal.owner != null && stargateFinal.owner.equals(playerFinal.getName())))
	            {
	                charge = false;
	            }
	            if (charge && cost > 0.0)
	            {
	                Account playerAccount = iConomy.getBank().getAccount(playerFinal.getName());
	                double balance = playerAccount.getBalance();
	                String currency = iConomy.getBank().getCurrency();
	                if ( balance >= cost )
	                {
	                    playerAccount.subtract(cost);
	                    // player_account.save();
	                    playerFinal.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Wormhole Use \u00A7F- \u00A72" + cost + " \u00A77" + currency );
	                    //p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to use wormhole." );
	                    final double ownerPercent = ConfigManager.getIconomyWormholeOwnerPercent();

	                    if ( ownerPercent != 0.0 && stargateFinal.owner != null )
	                    {
	                        if ( iConomy.getBank().hasAccount(stargateFinal.owner))
	                        {
	                            final Account ownAcc = iConomy.getBank().getAccount(stargateFinal.owner);
	                            ownAcc.add(cost * ownerPercent);
	                            // own_acc.save();
	                        }
	                    }
	                }
	                else
	                {
	                    playerFinal.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Not enough " + currency  + "! - Requires: \u00A72" + cost + " \u00A77- Available: \u00A74" + playerAccount.getBalance() + " \u00A77" + currency);
	                    //p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to use - requires: " + cost);
	                    target = stargateFinal.teleportLocation;
	                }
	            }
	        }
	        if (target != stargateFinal.teleportLocation)
	        {
	            WorldUtils.checkChunkLoad(target.getBlock());
	            target = TeleportUtils.findSafeTeleportFromStargate(stargateFinal.target);
	        }
	        playerFinal.setNoDamageTicks(5);
	        final Location targetFinal = target;
	        WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new Runnable() {
	            public void run()
	            {
	                eventFinal.setFrom(targetFinal);
	                eventFinal.setTo(targetFinal);
	                playerFinal.teleport(targetFinal);
	            }
	        }, 2);
	        if ( target != stargateFinal.teleportLocation )
	        {
	            WormholeXTreme.getThisPlugin().prettyLog(Level.INFO,false, playerFinal.getName() + " used wormhole: " + stargateFinal.name + " to go to: " + stargateFinal.target.name);
	        }
	        if ( ConfigManager.getTimeoutShutdown() == 0 )
	        {
	            stargateFinal.shutdownStargate();
	        }
	        return true;
	    }
	    else if ( stargateFinal != null )
	    {
	        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player entered gate but wasn't active or didn't have a target.");
	    }
	    return false;
	}
	
	/**
     * Button lever hit.
     *
     * @param p the p
     * @param blockClicked the clicked
     * @param direction the direction
     * @return true, if successful
     */
    private static boolean buttonLeverHit(Player p, Block blockClicked, BlockFace direction)
    {
        final Stargate s = StargateManager.getGateFromBlock(blockClicked);
        
        if ( s != null  )
        {
            if ( WXPermissions.checkWXPermissions(p, s, PermissionType.USE) )
            {
                if ( WorldUtils.isSameBlock(s.activationBlock, blockClicked) )
                {
                    handleGateActivationSwitch(s, p);
                }
                else if ( WorldUtils.isSameBlock(s.irisActivationBlock, blockClicked) )
                {
                    s.toggleIrisDefault();
                    if ((s.active) && (!s.irisActive)) 
                    {
                        s.fillGateInterior(s.gateShape.portalMaterial);
                    }
                }
            }
            else
            {
                p.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
            
            return true;
        }
        else 
        {
            if ( direction == null )
            {
                switch ( blockClicked.getData() )
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
                default:
                    break;
                }
                
                if ( direction == null)
                {
                    return false;
                }
            }
            // Check to see if player has already run the "build" command.
            StargateShape shape = StargateManager.getPlayerBuilderShape(p);
            
            Stargate newGate = null;
            if ( shape != null )
            {
                newGate = StargateHelper.checkStargate(blockClicked, direction, shape);
            }
            else
            {
                newGate = StargateHelper.checkStargate(blockClicked, direction);
            }
            
            if ( newGate != null )
            {
                if ( WXPermissions.checkWXPermissions(p, newGate, PermissionType.BUILD) )
                {
                    if ( newGate.isSignPowered )
                    {
                        p.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargate Design Valid with Sign Nav.");
                        if ( newGate.name.equals("") )
                        {
                            p.sendMessage(ConfigManager.MessageStrings.constructNameInvalid.toString() + "\"\"");
                        }
                        else
                        {
                            boolean success = StargateManager.completeStargate(p, newGate);
                            if ( success )
                            {
                                p.sendMessage(ConfigManager.MessageStrings.constructSuccess.toString());
                                newGate.teleportSign.setLine(0, "-" + newGate.name + "-" );
                                newGate.teleportSign.setData(newGate.teleportSign.getData());
                                newGate.teleportSign.update();
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
                        p.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid Stargate Design! \u00A73:: \u00A7B<required> \u00A76[optional]");
                        p.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/wxcomplete \u00A7B<name> \u00A76[idc=IDC] [net=NET]\u00A77\' to complete.");
                        // Add gate to unnamed gates.
                        StargateManager.addIncompleteStargate(p, newGate);
                    }
                    return true;
                }
                else
                {
                    if ( newGate.isSignPowered )
                    {
                        newGate.network.gateList.remove(newGate);
                        newGate.network.signGateList.remove(newGate);
                        newGate.teleportSign.setLine(0, newGate.name);
                        if (newGate.network != null)
                        {
                            newGate.teleportSign.setLine(1, newGate.network.netName );
                        }
                        newGate.teleportSign.setData(newGate.teleportSign.getData());
                        newGate.teleportSign.update();
                    }
                    StargateManager.removeIncompleteStargate(p);
                    p.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return true;
                }   
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, p.getName() + " has pressed a button or lever but did not find any properly created gates.");
            }
        }
        return false;
    }

    /**
     * Handle gate activation switch.
     *
     * @param stargate the stargate
     * @param player the player
     * @return true, if successful
     */
    private static boolean handleGateActivationSwitch(Stargate stargate, Player player) 
    {
        if ( stargate.active || stargate.litGate )
        {
            if ( stargate.target != null)
            {
                //Shutdown stargate
                stargate.shutdownStargate();
                player.sendMessage(ConfigManager.MessageStrings.gateShutdown.toString());
                return true;
            }
            else
            {
                Stargate s2 = StargateManager.removeActivatedStargate(player);
                if ( s2 != null && stargate.gateId == s2.gateId )
                {
                    stargate.stopActivationTimer(player);
                    stargate.deActivateStargate();
                    stargate.dialButtonLeverState();
                    stargate.unLightStargate();
                    player.sendMessage(ConfigManager.MessageStrings.gateDeactivated.toString());
                    return true;
                }
                else
                {
                    if ( stargate.litGate && !stargate.active )
                    {
                        player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate has been activated by someone else already.");
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.MessageStrings.gateRemoveActive.toString());
                    }
                    return false;
                }
            }      
        }
        else
        {
            if ( stargate.isSignPowered  )
            {          
                if ( WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN) )
                {
                    if ( stargate.teleportSign == null && stargate.teleportSignBlock != null )
                    {
                        stargate.tryClickTeleportSign(stargate.teleportSignBlock);
                    }
                    
                    if ( stargate.signTarget != null)
                    {
                        if ( stargate.dialStargate(stargate.signTarget) )
                        {
                            player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargates connected!");
                            return true;
                        }
                        else
                        {
                            player.sendMessage(ConfigManager.MessageStrings.gateRemoveActive.toString());
                            return false;
                        }
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());
                        return false;
                    }
                }
                else
                {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return false;
                }
            }
            else
            {
                //Activate Stargate
                player.sendMessage(ConfigManager.MessageStrings.gateActivated.toString());
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Chevrons Locked! \u00A73:: \u00A7B<required> \u00A76[optional]");
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/dial \u00A7B<gatename> \u00A76[idc]\u00A77\'");
                StargateManager.addActivatedStargate(player, stargate);
                stargate.startActivationTimer(player);
                stargate.lightStargate();
                return true;
            }
        }
    }
} 
 
