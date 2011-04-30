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
package com.wormhole_xtreme.wormhole.command;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;

/**
 * The Class Dial.
 * 
 * @author alron
 */
public class Dial implements CommandExecutor
{

    /**
     * Do dial.
     * 
     * @param player
     *            the player
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doDial(final Player player, final String[] args)
    {
        final Stargate start = StargateManager.removeActivatedStargate(player);
        final String[] arguments = args;
        if (start != null)
        {
            if (WXPermissions.checkWXPermissions(player, start, PermissionType.DIALER))
            {
                final String startnetwork = CommandUtilities.getGateNetwork(start);
                if ( !start.getGateName().equals(arguments[0]))
                {
                    final Stargate target = StargateManager.getStargate(arguments[0]);
                    // No target
                    if (target == null)
                    {
                        CommandUtilities.closeGate(start, false);
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());
                        return true;
                    }
                    final String targetnetwork = CommandUtilities.getGateNetwork(target);
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Dial Target - Gate: \"" + target.getGateName() + "\" Network: \"" + targetnetwork + "\"");
                    // Not on same network
                    if ( !startnetwork.equals(targetnetwork))
                    {
                        CommandUtilities.closeGate(start, false);
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString() + " Not on same network.");
                        return true;
                    }
                    if (start.isGateIrisActive())
                    {
                        start.toggleIrisActive(false);
                    }
                    if ( !target.getGateIrisDeactivationCode().equals("") && target.isGateIrisActive())
                    {
                        if ((arguments.length >= 2) && target.getGateIrisDeactivationCode().equals(arguments[1]))
                        {
                            if (target.isGateIrisActive())
                            {
                                target.toggleIrisActive(false);
                                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "IDC accepted. Iris has been deactivated.");
                            }
                        }
                    }

                    if (start.dialStargate(target, false))
                    {
                        player.sendMessage(ConfigManager.MessageStrings.gateConnected.toString());
                    }
                    else
                    {
                        CommandUtilities.closeGate(start, false);
                        player.sendMessage(ConfigManager.MessageStrings.targetIsActive.toString());
                    }
                }
                else
                {
                    CommandUtilities.closeGate(start, false);
                    player.sendMessage(ConfigManager.MessageStrings.targetIsSelf.toString());
                }
            }
            else
            {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
        }
        else
        {
            player.sendMessage(ConfigManager.MessageStrings.gateNotActive.toString());
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
    {
        if (CommandUtilities.playerCheck(sender))
        {
            final String[] arguments = CommandUtilities.commandEscaper(args);
            if ((arguments.length < 3) && (arguments.length > 0))
            {
                return doDial((Player) sender, arguments);
            }
            return false;
        }
        return true;
    }

}
