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
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;

/**
 * @author alron
 *
 */
public class WXGo implements CommandExecutor {

    /**
     * 
     */
    public WXGo(WormholeXTreme wormholeXTreme) {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
        Player player = null;
        if (!CommandUtlities.playerCheck(sender))
        {
            return true;
        }
        else
        {
            player = (Player)sender;
        }
        boolean allowed = false;
        if ( player.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(player, "wormhole.go")))
        {
            allowed = true;
        }
        if (allowed)
        {
            args = CommandUtlities.commandEscaper(args);
            if ( args.length == 1)
            {
                String gogate = args[0].trim().replace("\n", "").replace("\r", "");
                Stargate s = StargateManager.GetStargate(gogate);
                if ( s != null )
                {
                    player.teleportTo(s.TeleportLocation);
                }
                else
                {
                    player.sendMessage("\u00A73:: \u00A75error \u00A73:: \u00A77Gate does not exist: " + args[0]);
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            player.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
        }
        return true;
    }

}
