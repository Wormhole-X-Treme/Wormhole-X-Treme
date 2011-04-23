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
import com.wormhole_xtreme.wormhole.utils.TeleportUtils;

/**
 * The Class Go.
 *
 * @author alron
 */
public class Go implements CommandExecutor {

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
                return doGo(player, arguments);
            }
            return false;
        }
        return true;
    }

    /**
     * Do go.
     *
     * @param player the player
     * @param args the args
     * @return true, if successful
     */
    private static boolean doGo(Player player, String[] args)
    {
        final Player p = player;
        final String[] a = args;
        if (WXPermissions.checkWXPermissions(p, PermissionType.GO))
        {
            if ( a.length == 1)
            {
                final String goGate = a[0].trim().replace("\n", "").replace("\r", "");
                final Stargate s = StargateManager.getStargate(goGate);
                if ( s != null)
                {
                    p.teleport(TeleportUtils.findSafeTeleportFromStargate(s));
                }
                else
                {
                    p.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate does not exist: " + a[0]);
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            p.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        return true;
    }

}
