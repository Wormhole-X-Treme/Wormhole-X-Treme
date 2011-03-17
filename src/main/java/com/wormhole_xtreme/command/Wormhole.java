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

import org.bukkit.Material;
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

/**
 * @author alron
 *
 */
public class Wormhole implements CommandExecutor 
{

    /**
     * Instantiates a new wormhole.
     *
     * @param wormholeXTreme the wormhole x treme
     */
    public Wormhole(WormholeXTreme wormholeXTreme) 
    {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
        args = WormholeXTremeCommand.commandEscaper(args);
        if ((args.length > 4 ) || (args.length == 0)) 
        {
            return false;
        }
        if ( args[0].equalsIgnoreCase("owner"))
        {
            doOwner(sender,args);
        }
        else if ( args[0].equalsIgnoreCase("perm") || args[0].equalsIgnoreCase("perms"))
        {
            doPerms(sender,args);
        }
        else if ( args[0].equalsIgnoreCase("material") )
        {
            doMaterial(sender,args);
        }
        else if ( args[0].equalsIgnoreCase("irismaterial") )
        {
            doIrisMaterial(sender,args);
        }
        else if ( args[0].equalsIgnoreCase("timeout") || args[0].equalsIgnoreCase("shutdown_timeout") )
        {
            doShutdownTimeout(sender,args);
        }
        else if ( args[0].equalsIgnoreCase("activate_timeout") )
        {
            doActivateTimeout(sender,args);
        }
        else 
        {
            sender.sendMessage(ConfigManager.output_strings.get(StringTypes.REQUEST_INVALID));
            return false;
        }
        return true;
    }
    
    /**
	 * Do material.
	 *
	 * @param s the s
	 * @param args the args
	 */
	private static void doMaterial(CommandSender s, String[] args)
	{
		boolean allowed = false;
		if (WormholeXTremeCommand.playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}	
		if (allowed || !WormholeXTremeCommand.playerCheck(s))
		{
			if ( args.length == 2)
			{
				Material m = Material.STONE;
				try
				{
					m = Material.valueOf(args[1]);
				}
				catch (Exception e)
				{
				}
				
				if ( m == Material.STATIONARY_LAVA || m == Material.STATIONARY_WATER || m == Material.AIR || m == Material.PORTAL )
				{
					ConfigManager.setPortalMaterial(m);
				}
			}

			s.sendMessage("Portal material is currently: " + ConfigManager.getPortalMaterial());
			s.sendMessage("Valid materials are: STATIONARY_WATER, STATIONARY_LAVA, AIR, PORTAL");
		}
		else
		{
			s.sendMessage( ConfigManager.output_strings.get(StringTypes.PERMISSION_NO) );
		}
	}
	
	/**
	 * Do iris material.
	 *
	 * @param s the s
	 * @param args the args
	 */
	private static void doIrisMaterial(CommandSender s, String[] args)
	{
		boolean allowed = false;
		if (WormholeXTremeCommand.playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}
		if (allowed || !WormholeXTremeCommand.playerCheck(s))
		{
			if ( args.length == 2)
			{
				Material m = Material.STONE;
				try
				{
					m = Material.valueOf(args[1]);
				}
				catch (Exception e)
				{
				}
				
				if ( m == Material.DIAMOND_BLOCK || m == Material.GLASS || m == Material.IRON_BLOCK || m == Material.BEDROCK || m == Material.STONE 
				    || m == Material.LAPIS_BLOCK )
				{
					ConfigManager.setIrisMaterial(m);
				}
			}

			s.sendMessage("Iris material is currently: " + ConfigManager.getIrisMaterial());
			s.sendMessage("Valid materials are: STONE, DIAMOND_BLOCK, GLASS, IRON_BLOCK, BEDROCK, and LAPIS_BLOCK");
		}
		else
		{
			s.sendMessage( ConfigManager.output_strings.get(StringTypes.PERMISSION_NO) );
		}
	}
	
	/**
	 * Do shutdown timeout.
	 *
	 * @param s the s
	 * @param args the args
	 */
	private static void doShutdownTimeout(CommandSender s, String[] args)
	{
		boolean allowed = false;
		if (WormholeXTremeCommand.playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || !WormholeXTremeCommand.playerCheck(p) || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
			
		}
		if (allowed || !WormholeXTremeCommand.playerCheck(s))
		{
			if ( args.length == 2)
			{
				try
				{
					int timeout = Integer.parseInt(args[1]);
					if (  timeout > -1 && timeout <= 60)
					{
						ConfigManager.setTimeoutShutdown(timeout);
					}
					else
					{
						s.sendMessage("Valid timeout is between 0 and 60 seconds.");
					}
				}
				catch (Exception e)
				{
					s.sendMessage("Valid timeout is between 0 and 60 seconds.");
				}
			}
			s.sendMessage("Current shutdown_timeout is: " + ConfigManager.getTimeoutShutdown() );
		}
		else
		{
			s.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
		}
	}
	
	/**
	 * Do activate timeout.
	 *
	 * @param s the s
	 * @param args the args
	 */
	private static void doActivateTimeout(CommandSender s, String[] args)
	{
		boolean allowed = false;
		if (WormholeXTremeCommand.playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}
		if (allowed || !WormholeXTremeCommand.playerCheck(s))
		{
			if ( args.length == 2)
			{
				try
				{
					int timeout = Integer.parseInt(args[1]);
					if (  timeout >= 10 && timeout <= 60)
					{
						ConfigManager.setTimeoutActivate(timeout);
					}
					else 
					{
						s.sendMessage("Valid timeout is between 10 and 60 seconds.");
					}
				}
				catch (Exception e)
				{
					s.sendMessage("Valid timeout is between 10 and 60 seconds.");
				}
			}
			s.sendMessage("Current activate_timeout is: " + ConfigManager.getTimeoutActivate() );
		}
		else
		{
			s.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
		}
	}
	
	/**
	 * Do owner.
	 *
	 * @param sender the sender
	 * @param args the args
	 */
	private static void doOwner(CommandSender sender, String[] args)
	{
		boolean allowed = false;
		if (WormholeXTremeCommand.playerCheck(sender))
		{
			Player p = (Player) sender;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}
		if (allowed || !WormholeXTremeCommand.playerCheck(sender))
			if ( args.length > 1 )
			{
				Stargate s = StargateManager.GetStargate(args[1]);
				if (s != null )
				{
					if ( args.length > 2)
					{
						s.Owner = args[2];
					}
					
					sender.sendMessage("\u00A73:: \u00a75Gate: \u00a77" + s.Name + " \u00a75Owned by: \u00a77" + s.Owner);
				}
				else
				{
					sender.sendMessage("\u00A73:: \u00A75error \u00A73:: \u00A77Invalid gate name.");
				}
			}
			else
			{
				sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
			}
	}
	

	/**
	 * Do perms.
	 *
	 * @param sender the sender
	 * @param args the args
	 */
	private static void doPerms(CommandSender sender, String[] args)
	{
		if (WormholeXTremeCommand.playerCheck(sender))
		{
			Player p = (Player) sender;
			PermissionsManager.HandlePermissionRequest(p, args);
		}
	}
    

}
