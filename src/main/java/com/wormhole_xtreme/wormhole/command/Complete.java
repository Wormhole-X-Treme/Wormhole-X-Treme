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
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.StargateRestrictions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;

/**
 * The Class Complete.
 * 
 * @author alron
 */
public class Complete implements CommandExecutor
{

    /**
     * Do complete.
     * 
     * @param player
     *            the player
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doComplete(final Player player, final String[] args)
    {
        final String name = args[0].trim().replace("\n", "").replace("\r", "");

        if (name.length() < 12)
        {
            String idc = "";
            String network = "Public";

            for (int i = 1; i < args.length; i++)
            {
                final String[] key_value_string = args[i].split("=");
                if (key_value_string[0].equals("idc"))
                {
                    idc = key_value_string[1];
                }
                else if (key_value_string[0].equals("net"))
                {
                    network = key_value_string[1];
                }
            }
            if (WXPermissions.checkWXPermissions(player, network, PermissionType.BUILD))
            {
                if ( !StargateRestrictions.isPlayerBuildRestricted(player))
                {
                    if (StargateManager.getStargate(name) == null)
                    {
                        if (StargateManager.completeStargate(player, name, idc, network))
                        {
                            player.sendMessage(ConfigManager.MessageStrings.constructSuccess.toString());
                        }
                        else
                        {
                            player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Construction Failed!?");
                        }
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.MessageStrings.constructNameTaken.toString() + "\"" + name + "\"");
                    }
                }
                else
                {
                    player.sendMessage(ConfigManager.MessageStrings.playerBuildCountRestricted.toString());
                }
            }
            else
            {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
        }
        else
        {
            player.sendMessage(ConfigManager.MessageStrings.constructNameTooLong.toString() + "\"" + name + "\"");
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
    {
        final String[] arguments = CommandUtilities.commandEscaper(args);
        if ((arguments.length <= 3) && (arguments.length > 0))
        {
            return CommandUtilities.playerCheck(sender)
                ? doComplete((Player) sender, arguments)
                : true;
        }
        return false;
    }

}
