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

import java.util.ArrayList;

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
 * The Class WXList.
 *
 * @author alron
 */
public class WXList implements CommandExecutor {

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;

        if (CommandUtilities.playerCheck(sender)) {
            player = (Player) sender;
        }
        if (!CommandUtilities.playerCheck(sender) || (player != null && WXPermissions.checkWXPermissions(player, PermissionType.LIST)) )
        {
            ArrayList<Stargate> gates = StargateManager.getAllGates();
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Available gates \u00A73::");
            StringBuilder sb = new StringBuilder();
            // TODO: Add checks for complex permissions enabled users running this command and only display what they have access to use.
            for ( int i = 0; i < gates.size(); i++)
            {
                sb.append("\u00A77" + gates.get(i).name);
                if ( i != gates.size() - 1)
                {
                    sb.append("\u00A78, ");
                }
                if (sb.toString().length() >= 75 )
                {
                    sender.sendMessage(sb.toString());
                    sb = new StringBuilder();
                }
            }
            if (!sb.toString().equals(""))
            {
                sender.sendMessage(sb.toString());
            }

        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        return true;
    }

}
