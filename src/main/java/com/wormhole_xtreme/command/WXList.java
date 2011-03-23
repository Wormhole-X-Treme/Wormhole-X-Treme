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

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;

// TODO: Auto-generated Javadoc
/**
 * The Class WXList.
 *
 * @author alron
 */
public class WXList implements CommandExecutor {

    /**
     * Instantiates a new wX list.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public WXList(WormholeXTreme wormholeXTreme) {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean allowed = false;
        Player p = null;
        
        if (CommandUtlities.playerCheck(sender)) {
            p = (Player) sender;
            if ( p.isOp() || (WormholeXTreme.permissions != null && ((ConfigManager.getSimplePermissions() && (WormholeXTreme.permissions.has(p, "wormhole.simple.config") || WormholeXTreme.permissions.has(p, "wormhole.simple.use"))) || 
                (!ConfigManager.getSimplePermissions() && (WormholeXTreme.permissions.has(p, "wormhole.config")) || (WormholeXTreme.permissions.has(p, "wormhole.list"))))))
            {
                allowed = true;
            }
        }
        if (!CommandUtlities.playerCheck(sender) || allowed )
        {
            ArrayList<Stargate> gates = StargateManager.GetAllGates();
            sender.sendMessage(ConfigManager.normalheader + "Available gates \u00A73::");
            StringBuilder sb = new StringBuilder();
            // TODO: Add checks for complex permissions enabled users running this command and only display what they have access to use.
            for ( int i = 0; i < gates.size(); i++)
            {
                sb.append("\u00A77" + gates.get(i).Name);
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
            sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
        }
        return true;
    }

}
