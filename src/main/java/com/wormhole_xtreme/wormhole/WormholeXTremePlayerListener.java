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
    public void onPlayerInteract(final PlayerInteractEvent event)
    {
        if (event.getClickedBlock() != null)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false,"Caught Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getType().toString() + "\" Action Type: \"" + event.getAction().toString() + "\" Event Block Type: \"" + event.getClickedBlock().getType().toString() + "\" Event World: \"" + event.getClickedBlock().getWorld().toString() + "\" Event Block: " + event.getClickedBlock().toString() + "\"");
            if (handlePlayerInteractEvent(event))
            {
                event.setCancelled(true);
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false,"Cancelled Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getType().toString() + "\" Action Type: \"" + event.getAction().toString() + "\" Event Block Type: \"" + event.getClickedBlock().getType().toString() + "\" Event World: \"" + event.getClickedBlock().getWorld().toString() + "\" Event Block: " + event.getClickedBlock().toString() +  "\"");
            }
        }
        else 
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Caught and ignored Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getType().toString() + "\"");
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.player.PlayerListener#onPlayerBucketFill(org.bukkit.event.player.PlayerBucketFillEvent)
     */
    @Override
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
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
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
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
    private static boolean handlePlayerInteractEvent(final PlayerInteractEvent event)
    {
        final Block clickedBlock = event.getClickedBlock();
        final Player player = event.getPlayer();

        if (clickedBlock != null && (clickedBlock.getType() == Material.STONE_BUTTON || clickedBlock.getType() == Material.LEVER ))
        {
            if ( buttonLeverHit(player, clickedBlock, null) )
            {  
                return true;
            }
        }
        else if ( clickedBlock != null && clickedBlock.getType() == Material.WALL_SIGN )
        {
            final Stargate stargate = StargateManager.getGateFromBlock(clickedBlock);
            if ( stargate != null )
            {
                if (WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN)) 
                {
                    if ( stargate.tryClickTeleportSign(clickedBlock, player) )
                    {
                        return true;
                    }
                }
                else 
                {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
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
    public void onPlayerMove(final PlayerMoveEvent event)
    {
        if (handlePlayerMoveEvent(event))
        {
            event.setCancelled(true);
        }
    }

    /**
     * Handle player move event.
     *
     * @param event the event
     * @return true, if successful
     */
    private static boolean handlePlayerMoveEvent(final PlayerMoveEvent event)
    {
        final Player player = event.getPlayer();
        final Location toLocFinal = event.getTo();
        final Block gateBlockFinal = toLocFinal.getWorld().getBlockAt( toLocFinal.getBlockX(), toLocFinal.getBlockY(), toLocFinal.getBlockZ());
        final Stargate stargate = StargateManager.getGateFromBlock( gateBlockFinal );

        if ( stargate != null && stargate.active && stargate.target != null && stargate.gateShape != null && 
            gateBlockFinal.getType() == stargate.gateShape.portalMaterial )
        {
            String gatenetwork;
            if (stargate.network != null )
            {
                gatenetwork = stargate.network.netName;
            }
            else
            {
                gatenetwork = "Public";
            }
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player in gate:" + stargate.name + " gate Active: " + stargate.active + " Target Gate: " + stargate.target.name + " Network: " + gatenetwork );

            if (ConfigManager.getWormholeUseIsTeleport() && ((stargate.isSignPowered && !WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN)) ||
                (!stargate.isSignPowered && !WXPermissions.checkWXPermissions(player, stargate, PermissionType.DIALER))))
            {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                return false;
            }
            if ( stargate.target.irisActive )
            {
                player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Remote Iris is locked!");
                player.setNoDamageTicks(5);
                event.setFrom(stargate.teleportLocation);
                event.setTo(stargate.teleportLocation);
                player.teleport(stargate.teleportLocation);
                return true;
            }

            Location target = stargate.target.teleportLocation;
            if ( WormholeXTreme.getIconomy() != null )
            {
                final double cost = ConfigManager.getIconomyWormholeUseCost();
                boolean charge = true;
                if ((ConfigManager.getIconomyOpsExcempt() && player.isOp()) || (ConfigManager.getIconomyOwnerExempt() && stargate.owner != null && stargate.owner.equals(player.getName())))
                {
                    charge = false;
                }
                if (charge && cost > 0.0)
                {
                    final Account playerAccount = iConomy.getBank().getAccount(player.getName());
                    final double balance = playerAccount.getBalance();
                    final String currency = iConomy.getBank().getCurrency();
                    if ( balance >= cost )
                    {
                        playerAccount.subtract(cost);
                        // player_account.save();
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Wormhole Use \u00A7F- \u00A72" + cost + " \u00A77" + currency );
                        //p.sendMessage("You were charged " + cost + " " + iConomy.getBank().getCurrency() + " to use wormhole." );
                        final double ownerPercent = ConfigManager.getIconomyWormholeOwnerPercent();

                        if ( ownerPercent != 0.0 && stargate.owner != null )
                        {
                            if ( iConomy.getBank().hasAccount(stargate.owner))
                            {
                                final Account ownAcc = iConomy.getBank().getAccount(stargate.owner);
                                ownAcc.add(cost * ownerPercent);
                                // own_acc.save();
                            }
                        }
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Not enough " + currency  + "! - Requires: \u00A72" + cost + " \u00A77- Available: \u00A74" + playerAccount.getBalance() + " \u00A77" + currency);
                        //p.sendMessage("Not enough " + iConomy.getBank().getCurrency() + " to use - requires: " + cost);
                        target = stargate.teleportLocation;
                    }
                }
            }
            if (target != stargate.teleportLocation)
            {
                target = TeleportUtils.findSafeTeleportFromStargate(stargate.target);
                WorldUtils.checkChunkLoad(target.getBlock());
            }
            player.setNoDamageTicks(5);
            event.setFrom(target);
            event.setTo(target);
            player.teleport(target);
            if ( target != stargate.teleportLocation )
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.INFO,false, player.getName() + " used wormhole: " + stargate.name + " to go to: " + stargate.target.name);
            }
            if ( ConfigManager.getTimeoutShutdown() == 0 )
            {
                stargate.shutdownStargate();
            }
            return true;
        }
        else if ( stargate != null )
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player entered gate but wasn't active or didn't have a target.");
        }
        return false;
    }

    /**
     * Button lever hit.
     *
     * @param player the p
     * @param clickedBlock the clicked
     * @param direction the direction
     * @return true, if successful
     */
    private static boolean buttonLeverHit(final Player player, final Block clickedBlock, BlockFace direction)
    {
        final Stargate stargate = StargateManager.getGateFromBlock(clickedBlock);

        if ( stargate != null  )
        {
            if ( WXPermissions.checkWXPermissions(player, stargate, PermissionType.USE) )
            {
                if ( WorldUtils.isSameBlock(stargate.activationBlock, clickedBlock) )
                {
                    handleGateActivationSwitch(stargate, player);
                }
                else if ( WorldUtils.isSameBlock(stargate.irisActivationBlock, clickedBlock) )
                {
                    stargate.toggleIrisDefault();
                    if ((stargate.active) && (!stargate.irisActive)) 
                    {
                        stargate.fillGateInterior(stargate.gateShape.portalMaterial);
                    }
                }
            }
            else
            {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }

            return true;
        }
        else 
        {
            if ( direction == null )
            {
                switch ( clickedBlock.getData() )
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
            StargateShape shape = StargateManager.getPlayerBuilderShape(player);

            Stargate newGate = null;
            if ( shape != null )
            {
                newGate = StargateHelper.checkStargate(clickedBlock, direction, shape);
            }
            else
            {
                newGate = StargateHelper.checkStargate(clickedBlock, direction);
            }

            if ( newGate != null )
            {
                if ( WXPermissions.checkWXPermissions(player, newGate, PermissionType.BUILD) )
                {
                    if ( newGate.isSignPowered )
                    {
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargate Design Valid with Sign Nav.");
                        if ( newGate.name.equals("") )
                        {
                            player.sendMessage(ConfigManager.MessageStrings.constructNameInvalid.toString() + "\"\"");
                        }
                        else
                        {
                            boolean success = StargateManager.completeStargate(player, newGate);
                            if ( success )
                            {
                                player.sendMessage(ConfigManager.MessageStrings.constructSuccess.toString());
                                newGate.teleportSign.setLine(0, "-" + newGate.name + "-" );
                                newGate.teleportSign.setData(newGate.teleportSign.getData());
                                newGate.teleportSign.update();
                            }
                            else
                            {
                                player.sendMessage("Stargate constrution failed!?");
                            }
                        }

                    }
                    else
                    {
                        // Print to player that it was successful!
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid Stargate Design! \u00A73:: \u00A7B<required> \u00A76[optional]");
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/wxcomplete \u00A7B<name> \u00A76[idc=IDC] [net=NET]\u00A77\' to complete.");
                        // Add gate to unnamed gates.
                        StargateManager.addIncompleteStargate(player, newGate);
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
                    StargateManager.removeIncompleteStargate(player);
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return true;
                }   
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, player.getName() + " has pressed a button or lever but did not find any properly created gates.");
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
    private static boolean handleGateActivationSwitch(final Stargate stargate, final Player player) 
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

