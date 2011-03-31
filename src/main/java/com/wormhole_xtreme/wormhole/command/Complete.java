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
import com.wormhole_xtreme.wormhole.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;

// TODO: Auto-generated Javadoc
/**
 * The Class Complete.
 *
 * @author alron
 */
public class Complete implements CommandExecutor 
{

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
        if (CommandUtilities.playerCheck(sender))
        {
            final String[] arguments = CommandUtilities.commandEscaper(args);
            if (arguments.length <= 3 || arguments.length != 0 )
            {
                final Player player = (Player)sender;
                return doComplete(player, arguments);
            }
            return false;
        }
        return true;
    }
    
    /**
     * Do complete.
     *
     * @param player the player
     * @param args the args
     * @return true, if successful
     */
    private static boolean doComplete(Player player, String[] args)
    {
        final Player p = player;
        final String[] a = args;
        final String name = args[0].trim().replace("\n", "").replace("\r", "");

        if ( name.length() < 12)
        {
            final Stargate dupName = StargateManager.GetStargate( name );

            String idc = "";
            String network = "";

            for ( int i = 1; i < a.length; i++ )
            {
                String[] key_value_string = a[i].split("=");
                if ( key_value_string[0].equals("idc") )
                {
                    idc = key_value_string[1];
                }
                else if ( key_value_string[0].equals("net") )
                {
                    network = key_value_string[1];
                }
            }
            if (WXPermissions.checkWXPermissions(p, network, PermissionType.BUILD))
            {
                if ( dupName == null )
                {
                    final boolean success = StargateManager.CompleteStargate(p, name, idc, network);

                    if ( success )
                    {
                        p.sendMessage( ConfigManager.output_strings.get(StringTypes.CONSTRUCT_SUCCESS) );
                    }
                    else
                    {
                        p.sendMessage(ConfigManager.errorheader + "Construction Failed!?" );
                    }
                }
                else
                {
                    p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_TAKEN) + "\"" + name + "\"");
                }
            }
            else 
            {
                p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
            }
        }
        else
        {
            p.sendMessage( ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_TOO_LONG) + "\"" + name + "\"" );
        }
        return true;
    }

}
