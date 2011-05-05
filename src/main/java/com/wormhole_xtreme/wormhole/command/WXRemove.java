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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;

/**
 * The Class WXRemove.
 * 
 * @author alron
 */
public class WXRemove implements CommandExecutor
{

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
    {
        final String[] a = CommandUtilities.commandEscaper(args);
        if ((a.length >= 1) && (a.length <= 2))
        {
            if (a[0].equals("-all"))
            {
                return false;
            }
            final Stargate s = StargateManager.getStargate(a[0]);

            if (s != null)
            {
                if (CommandUtilities.playerCheck(sender)
                    ? WXPermissions.checkWXPermissions((Player) sender, s, PermissionType.REMOVE)
                    : true)
                {
                    boolean destroy = false;
                    if ((a.length == 2) && a[1].equalsIgnoreCase("-all"))
                    {
                        destroy = true;
                    }
                    CommandUtilities.gateRemove(s, destroy);
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Wormhole Removed: " + s.getGateName());
                }
                else
                {
                    sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                }

            }
            else
            {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate does not exist: " + a[0] + ". Remember proper capitalization.");
            }
        }
        else
        {
            return false;
        }
        return true;
    }

}
