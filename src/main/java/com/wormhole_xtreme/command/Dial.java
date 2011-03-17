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

import java.util.logging.Level;

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
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;

/**
 * @author alron
 *
 */
public class Dial implements CommandExecutor {

    /**
     * Instantiates a new dial.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public Dial(WormholeXTreme wormholeXTreme) {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
        Player player = null;
        if (!WormholeXTremeCommand.playerCheck(sender))
        {
            return true;
        }
        else
        {
            player = (Player)sender;
        }
        args = WormholeXTremeCommand.commandEscaper(args);
        if (args.length > 2 || args.length == 0)
        {
            return false;
        }
        Stargate start = StargateManager.RemoveActivatedStargate(player);
        if (start != null)
        {               
            String startnetwork;
            if (start.Network != null)
            {
                startnetwork = start.Network.netName;
            }
            else 
            {
                startnetwork = "Public";
            }
            boolean allowed = false;
            if (WormholeXTreme.Permissions != null)
            {
                WormholeXTreme.ThisPlugin.prettyLog(Level.FINEST, false, "Dial Start - Gate: \""+ start.Name +" \"Network: \"" + startnetwork + "\"");
                if (WormholeXTreme.Permissions.has(player, "wormhole.use.dialer") && (startnetwork.equals("Public") || (!startnetwork.equals("Public") && WormholeXTreme.Permissions.has(player, "wormhole.network.use." + startnetwork))))
                {
                    allowed = true;
                }
            }
            else
            {
                PermissionLevel lvl = PermissionsManager.getPermissionLevel(player, start);
                if ( lvl.equals(PermissionLevel.WORMHOLE_FULL_PERMISSION) || lvl.equals(PermissionLevel.WORMHOLE_CREATE_PERMISSION) || lvl.equals(PermissionLevel.WORMHOLE_USE_PERMISSION)) {
                    allowed = true;
                }
            }
            
            if ( player.isOp() || allowed )
            {
                if ( !start.Name.equals(args[0]) )
                {
                    Stargate target = StargateManager.GetStargate(args[0]);
                    // No target
                    if ( target == null)
                    {
                        start.StopActivationTimer(player);
                        start.DeActivateStargate();
                        start.UnLightStargate();
                        player.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_INVALID));
                        return true;
                    }
                    String targetnetwork;
                    if (target.Network != null)
                    {
                        targetnetwork = target.Network.netName;
                    }
                    else 
                    {
                        targetnetwork = "Public";
                    }
                    WormholeXTreme.ThisPlugin.prettyLog(Level.FINEST, false, "Dial Target - Gate: \"" + target.Name + "\" Network: \"" + targetnetwork + "\"");
                    // Not on same network
                    if (!startnetwork.equals(targetnetwork))
                    {
                        start.StopActivationTimer(player);
                        start.DeActivateStargate();
                        start.UnLightStargate();
                        player.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_INVALID) + " Not on same network.");
                        return true;
                    }
                        
                    if (!target.IrisDeactivationCode.equals("") && target.IrisActive)
                    {
                        if ( args.length >= 2 && target.IrisDeactivationCode.equals(args[1]))
                        {
                            if ( target.IrisActive )
                            {
                                target.ToggleIrisActive();
                                player.sendMessage("\u00A73:: \u00A75IDC accepted. Iris has been deactivated.");
                            }
                        }
                    }
                    
                    if ( start.DialStargate(target) ) 
                    {
                        player.sendMessage("\u00A73:: \u00A75Stargates connected!");
                    }
                    else
                    {
                        start.StopActivationTimer(player);
                        start.DeActivateStargate();
                        start.UnLightStargate();
                        player.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_IS_ACTIVE));
                    }
                }
                else
                {
                    start.StopActivationTimer(player);
                    start.DeActivateStargate();
                    start.UnLightStargate();
                    player.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_IS_SELF));
                }
            }
            else
            {
                player.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
            }
        }
        else
        {
            player.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_NOT_ACTIVE));
        }
        return true;
    }

}
