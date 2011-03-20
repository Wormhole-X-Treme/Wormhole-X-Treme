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
import com.wormhole_xtreme.logic.StargateHelper;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.model.StargateShape;

/**
 * @author alron
 *
 */
public class WXBuild implements CommandExecutor {

    /**
     * Instantiates a new wX build.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public WXBuild(WormholeXTreme wormholeXTreme) {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        Player player = null;
        if (!CommandUtlities.playerCheck(sender) )
        {
            return true;
        }
        else
        {
            player = (Player)sender;
        }
        args = CommandUtlities.commandEscaper(args);
        if (args.length == 1) 
        {
            boolean allowed = false;
            if (WormholeXTreme.Permissions != null && !ConfigManager.getSimplePermissions())
            {
                if (WormholeXTreme.Permissions.has(player, "wormhole.config"))
                {
                    allowed = true;
                }
            }
            else if (WormholeXTreme.Permissions != null && ConfigManager.getSimplePermissions())
            {
                if (WormholeXTreme.Permissions.has(player, "wormhole.simple.config"))
                {
                    allowed = true;
                }
            }
            if ( player.isOp() || allowed )
            {
                StargateShape shape = StargateHelper.getShape(args[0]);
                if  ( shape != null)
                {
                    StargateManager.AddPlayerBuilderShape(player, shape);
                    player.sendMessage(ConfigManager.normalheader + "Press Activation button on new DHD to autobuild Stargate in the shape of: " + args[0] );
                }
                else
                {
                    player.sendMessage(ConfigManager.errorheader + "Invalid shape: " + args[0]);
                }
            }
            else
            {
                player.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
            }
        }
        else
        {
            return false;
        }
        return true;
    }

}
