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
public class Dial implements CommandExecutor {

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
        if (CommandUtilities.playerCheck(sender))
        {
            final String[] arguments = CommandUtilities.commandEscaper(args);
            if (arguments.length < 3 && arguments.length > 0 )
            {
                final Player player = (Player)sender;
                return doDial(player, arguments);
            }
            return false;
        }
        return true;
    }

    /**
     * Do dial.
     *
     * @param player the player
     * @param args the args
     * @return true, if successful
     */
    private static boolean doDial(Player player, String[] args)
    {  
        final Player p = player;
        final Stargate start = StargateManager.removeActivatedStargate(p);
        final String[] arguments = args;
        if (start != null)
        {               
            if ( WXPermissions.checkWXPermissions(p, start, PermissionType.DIALER))
            {
                final String startnetwork = CommandUtilities.getGateNetwork(start);
                if ( !start.name.equals(arguments[0]) )
                {
                    final Stargate target = StargateManager.getStargate(arguments[0]);
                    // No target
                    if ( target == null)
                    {
                        CommandUtilities.closeGate(start,p);
                        p.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());
                        return true;
                    }
                    final String targetnetwork = CommandUtilities.getGateNetwork(target);
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Dial Target - Gate: \"" + target.name + "\" Network: \"" + targetnetwork + "\"");
                    // Not on same network
                    if (!startnetwork.equals(targetnetwork))
                    {
                        CommandUtilities.closeGate(start,p);
                        p.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString() + " Not on same network.");
                        return true;
                    }
                    if (start.irisActive)
                    {
                        start.toggleIrisActive();
                    }
                    if (!target.irisDeactivationCode.equals("") && target.irisActive)
                    {
                        if ( arguments.length >= 2 && target.irisDeactivationCode.equals(arguments[1]))
                        {
                            if ( target.irisActive )
                            {
                                target.toggleIrisActive();
                                p.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "IDC accepted. Iris has been deactivated.");
                            }
                        }
                    }

                    if ( start.dialStargate(target) ) 
                    {
                        p.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargates connected!");
                    }
                    else
                    {
                        CommandUtilities.closeGate(start,p);
                        p.sendMessage(ConfigManager.MessageStrings.targetIsActive.toString());
                    }
                }
                else
                {
                    CommandUtilities.closeGate(start,p);
                    p.sendMessage(ConfigManager.MessageStrings.targetIsSelf.toString());
                }
            }
            else
            {
                p.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
        }
        else
        {
            p.sendMessage(ConfigManager.MessageStrings.gateNotActive.toString());
        }
        return true;
    }

}
