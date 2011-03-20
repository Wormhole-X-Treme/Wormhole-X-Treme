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
import com.wormhole_xtreme.permissions.PermissionsManager;

// TODO: Auto-generated Javadoc
/**
 * The Class WXRemove.
 *
 * @author alron
 */
public class WXRemove implements CommandExecutor {

    /**
     * Instantiates a new wX remove.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public WXRemove(WormholeXTreme wormholeXTreme) {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandUtlities.commandEscaper(args);
        if (args.length >=1 && args.length <= 2)
        {
            if (args[0].equals("-all"))
            {
                return false;
            }
            Stargate s = StargateManager.GetStargate(args[0]);

            if ( s != null )
            {
                boolean allowed = false;
                if (CommandUtlities.playerCheck(sender))
                {
                    Player p = (Player) sender;
                    if  (WormholeXTreme.Permissions != null && !ConfigManager.getSimplePermissions())
                    {
                        if (WormholeXTreme.Permissions.has(p, "wormhole.remove.all") || ( s.Owner != null && s.Owner.equals(p.getName()) && WormholeXTreme.Permissions.has(p, "wormhole.remove.own")))
                        {
                            allowed = true;
                        }   
                    }
                    else if (WormholeXTreme.Permissions != null && ConfigManager.getSimplePermissions())
                    {
                        if (WormholeXTreme.Permissions.has(p, "wormhole.simple.remove"))
                        {
                            allowed = true;
                        }
                    }
                    else if (PermissionsManager.getPermissionLevel(p, s) == PermissionsManager.PermissionLevel.WORMHOLE_FULL_PERMISSION )
                    {
                        allowed = true;
                    }
                }
                if ( !CommandUtlities.playerCheck(sender) || allowed )
                {
                    s.DeleteNameSign();
                    s.ResetTeleportSign();
                    if (!s.IrisDeactivationCode.equals(""))
                    {
                        if (s.IrisActive)
                        {
                            s.ToggleIrisActive();
                        }
                        s.DeleteIrisLever();
                    }
                    if ( args.length == 2 && args[1].equals("-all"))
                    {
                        s.DeleteNameSign();
                        s.DeleteGateBlocks();
                        s.DeletePortalBlocks();
                        s.DeleteTeleportSignBlock();
                        s.DeleteNameBlock();
                    }
                    StargateManager.RemoveStargate(s);
                    sender.sendMessage(ConfigManager.normalheader + "Wormhole Removed: " + s.Name);
                }
                else
                {
                    sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
                }
                    
            }
            else
            {
                sender.sendMessage(ConfigManager.errorheader + "Gate does not exist: " + args[0] + ". Remember proper capitalization.");
            }
        }
        else
        {
            return false;
        }
        return true;
    }

}
