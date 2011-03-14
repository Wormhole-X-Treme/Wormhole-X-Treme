/**
 * 
 */
package com.wormhole_xtreme.command;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.WormholeXTremeCommand;

/**
 * @author alron
 *
 */
public class WXForce implements CommandExecutor {

    /**
     * @param wormholeXTreme
     */
    public WXForce(WormholeXTreme wormholeXTreme) {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args.length <= 2)
        {
            boolean allowed = false;
            Player player = null;
            if (WormholeXTremeCommand.playerCheck(sender))
            {
                player = (Player) sender;
                if (player.isOp() || (WormholeXTreme.Permissions != null && (WormholeXTreme.Permissions.has(player, "wormhole.config") || WormholeXTreme.Permissions.has(player, "wormhole.remove.all"))))
                {
                    allowed = true;
                }
            }
            else 
            {
                allowed = true;
            }
            if (allowed)
            {
                boolean close = false;
                boolean drop = false;
                if (args[0].equals("close"))
                {
                    close = true;
                }
                else if (args[0].equals("drop"))
                {
                    drop = true;
                }
                if (args.length == 2)
                {
                    if (args[1].equals("close"))
                    {
                        close = true;
                    }
                    else if (args[1].equals("drop"))
                    {
                        drop = true;
                    }
                }
                ArrayList<Stargate> gates = StargateManager.GetAllGates();
                ArrayList<String> activelist = new ArrayList<String>();
                ArrayList<String> droplist = new ArrayList<String>();
                for ( Stargate gate : gates )
                {
                    if (gate.Active && close)
                    {
                        activelist.add(gate.Name);
                        gate.ShutdownStargate();
                    }
                    if (gate.IrisActive && drop)
                    {
                        droplist.add(gate.Name);
                        gate.ToggleIrisActive();
                    }
                }
                if (close && !activelist.isEmpty())
                {
                    StringBuilder deactivated = new StringBuilder();
                    sender.sendMessage("\u00A73:: \u00A75Forced Closed Gate(s)\u00A73::");
                    for ( int i = 0; i < activelist.size(); i++)
                    {
                        deactivated.append("\u00A77" + activelist.get(i) );
                        if ( i != activelist.size() - 1 )
                        {
                            deactivated.append("\u00A78, ");
                        }
                        if (deactivated.toString().length() >= 75)
                        {
                            sender.sendMessage(deactivated.toString());
                            deactivated = new StringBuilder();
                        }
                    }
                    if (!deactivated.toString().equals("")) {
                        sender.sendMessage(deactivated.toString());
                    }
                }
                
                if (drop && !droplist.isEmpty())
                {
                    StringBuilder dropped = new StringBuilder();
                    sender.sendMessage("\u00A73:: \u00A75Forced Dropped Iris(es)\u00A73::");
                    for ( int i = 0; i < droplist.size(); i++)
                    {
                        dropped.append("\u00A77" + droplist.get(i) );
                        if ( i != droplist.size() - 1 )
                        {
                            dropped.append("\u00A78, ");
                        }
                        if (dropped.toString().length() >= 75)
                        {
                            sender.sendMessage(dropped.toString());
                            dropped = new StringBuilder();
                        }
                    }
                    if (!dropped.toString().equals("")) {
                        sender.sendMessage(dropped.toString());
                    }
                }
                
            }
            else 
            {
                sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
            }
            return true;
        }
        else 
        {
            return false;
        }
    }


}
