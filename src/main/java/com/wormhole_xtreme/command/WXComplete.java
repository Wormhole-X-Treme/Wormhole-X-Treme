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
package com.wormhole_xtreme.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.WormholeXTremeCommand;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;

// TODO: Auto-generated Javadoc
/**
 * The Class WXComplete.
 *
 * @author alron
 */
public class WXComplete implements CommandExecutor 
{

    /**
     * Instantiates a new wX complete.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public WXComplete(WormholeXTreme wormholeXTreme)
    {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = null;
        if (!WormholeXTremeCommand.playerCheck(sender))
        {
            return true;
        }
        else
        {
            p = (Player)sender;
        }
        args = WormholeXTremeCommand.commandEscaper(args);
        if (args.length >= 1 && args.length <= 3)
        {
            String name = args[0].trim().replace("\n", "").replace("\r", "");
                            
            if ( name.length() < 12)
            {
                Stargate dup_name = StargateManager.GetStargate( name );
                
                String idc = "";
                String network = "";

                for ( int i = 1; i < args.length; i++ )
                {
                    String[] key_value_string = args[i].split("=");
                    if ( key_value_string[0].equals("idc") )
                    {
                        idc = key_value_string[1];
                    }
                    else if ( key_value_string[0].equals("net") )
                    {
                        network = key_value_string[1];
                    }
                }
                boolean allowed = false;
                if (WormholeXTreme.Permissions != null)
                {
                    if (WormholeXTreme.Permissions.has(p, "wormhole.build") && ((network.equals("") || network.equals("Public") ) || (!network.equals("") && !network.equals("Public") && WormholeXTreme.Permissions.has(p, "wormhole.network.build." + network))))
                    {
                        allowed = true;
                    }
                }
                if (p.isOp() || allowed )
                {
                    if ( dup_name == null )
                    {
                        boolean success = StargateManager.CompleteStargate(p, name, idc, network);

                        if ( success )
                        {
                            p.sendMessage( ConfigManager.output_strings.get(StringTypes.CONSTRUCT_SUCCESS) );
                        }
                        else
                        {
                            p.sendMessage( "Construction Failed!?" );
                        }
                    }
                    else
                    {
                        p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_TAKEN));
                    }
                }
                else 
                {
                    p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
                }
            }
            else
            {
                p.sendMessage( ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_TOO_LONG) );
            }
        }
        else
        {
            return false;
        }
        return true;
    }

}
