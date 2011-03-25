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

import org.bukkit.Material;
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
import com.wormhole_xtreme.permissions.WXPermissions;
import com.wormhole_xtreme.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.plugin.HelpSupport;

// TODO: Auto-generated Javadoc
/**
 * The Class Wormhole.
 *
 * @author alron
 */
public class Wormhole implements CommandExecutor 
{
    private static HelpSupport helpSupport = new HelpSupport(WormholeXTreme.thisPlugin);
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
        Player player = null;
        if (CommandUtlities.playerCheck(sender))
        {
            player = (Player)sender;
        }
        if ((player != null && WXPermissions.checkWXPermissions(player, PermissionType.CONFIG)) || !CommandUtlities.playerCheck(sender))
        {
            args = CommandUtlities.commandEscaper(args);
            if ((args.length > 4 ) || (args.length == 0)) 
            {
                return false;
            }
            if ( args[0].equalsIgnoreCase("owner"))
            {
                return doOwner(sender,args);
            }
            else if ( args[0].equalsIgnoreCase("perm") || args[0].equalsIgnoreCase("perms"))
            {
                doPerms(sender,args);
            }
            else if ( args[0].equalsIgnoreCase("material") || args[0].equalsIgnoreCase("portalmaterial"))
            {
                return doPortalMaterial(sender,args);
            }
            else if ( args[0].equalsIgnoreCase("irismaterial") )
            {
                return doIrisMaterial(sender,args);
            }
            else if ( args[0].equalsIgnoreCase("timeout") || args[0].equalsIgnoreCase("shutdown_timeout") )
            {
                return doShutdownTimeout(sender,args);
            }
            else if ( args[0].equalsIgnoreCase("activate_timeout") )
            {
                return doActivateTimeout(sender,args);
            }
            else if (args[0].equalsIgnoreCase("simple"))
            {
                return doSimplePermissions(sender,args);
            }
            else 
            {
                sender.sendMessage(ConfigManager.output_strings.get(StringTypes.REQUEST_INVALID) + ": " + args[0]);
                sender.sendMessage(ConfigManager.errorheader + "Valid commands are 'owner', 'perms', 'portalmaterial', 'irismaterial', 'shutdown_timeout', 'activate_timeout' and 'simple'.");
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
        }
        return true;
    }
    
    
    /**
     * Do simple permissions.
     *
     * @param sender the sender
     * @param args the args
     * @return true, if successful
     */
    private static boolean doSimplePermissions(CommandSender sender, String[] args) {
        if (args.length == 2)
        {
            Player player = null;
            boolean simple;
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes"))
            {
                simple = true;
            }
            else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no"))
            {
                simple = false;
            }
            else
            {
                sender.sendMessage(ConfigManager.errorheader + "Invalid Setting: " + args[1]);
                sender.sendMessage(ConfigManager.errorheader + "Valid options: true/yes, false/no");
                return false;
            }
            if (WormholeXTreme.permissions != null && CommandUtlities.playerCheck(sender))
            {
                player = (Player)sender;
                if (simple && !WormholeXTreme.permissions.has(player, "wormhole.simple.config"))
                {
                    sender.sendMessage(ConfigManager.errorheader + "You currently do not have the 'wormhole.simple.config' permission.");
                    sender.sendMessage(ConfigManager.errorheader + "Please make sure you have this permission before running this command again.");
                    return true;
                }
                else if (!simple && !WormholeXTreme.permissions.has(player, "wormhole.config"))
                {
                    sender.sendMessage(ConfigManager.errorheader + "You currently do not have the 'wormhole.config' permission.");
                    sender.sendMessage(ConfigManager.errorheader + "Please make sure you have this permission before running this command again.");
                    return true;
                }
            }
            ConfigManager.setSimplePermissions(simple);
            sender.sendMessage(ConfigManager.normalheader + "Simple Permissions set to: " + ConfigManager.getSimplePermissions());
            helpSupport.registerHelpCommands();
            if (player != null)
            {
                WormholeXTreme.thisPlugin.prettyLog(Level.INFO, false, "Simple Permissions set to: \"" + simple + "\" by: \"" + player.getName() + "\"");
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.normalheader + "Simple Permissions: " + ConfigManager.getSimplePermissions());
            sender.sendMessage(ConfigManager.normalheader + "Valid options: true/yes, false/no");
        }
        return true;
    }

    /**
     * Do Portal Material.
     *
     * @param sender the sender
     * @param args the args
     * @return true, if successful
     */
	private static boolean doPortalMaterial(CommandSender sender, String[] args)
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
				sender.sendMessage(ConfigManager.normalheader + "Portal material set to: " + ConfigManager.getPortalMaterial());
			}
			else
			{
			    sender.sendMessage(ConfigManager.errorheader + "Invalid Portal Material: " + args[1]);
			    sender.sendMessage(ConfigManager.errorheader + "Valid materials are: STATIONARY_WATER, STATIONARY_LAVA, AIR, PORTAL");
			    return false;
			}
		}
		else
		{
		    sender.sendMessage(ConfigManager.normalheader + "Portal material is currently: " + ConfigManager.getPortalMaterial());
		    sender.sendMessage(ConfigManager.normalheader + "Valid materials are: STATIONARY_WATER, STATIONARY_LAVA, AIR, PORTAL");
		}
		return true;
	}
	
	/**
	 * Do iris material.
	 *
	 * @param sender the sender
	 * @param args the args
	 * @return true, if successful
	 */
	private static boolean doIrisMaterial(CommandSender sender, String[] args)
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
			
			if ( m == Material.DIAMOND_BLOCK || m == Material.GLASS || m == Material.IRON_BLOCK || m == Material.BEDROCK || m == Material.STONE || m == Material.LAPIS_BLOCK )
			{
				ConfigManager.setIrisMaterial(m);
				sender.sendMessage(ConfigManager.normalheader + "Iris material set to: " + ConfigManager.getIrisMaterial());
			}
			else 
			{
			    sender.sendMessage(ConfigManager.errorheader + "Invalid Iris Material: " + args[1]);
			    sender.sendMessage(ConfigManager.errorheader + "Valid materials are: STONE, DIAMOND_BLOCK, GLASS, IRON_BLOCK, BEDROCK, and LAPIS_BLOCK");
			    return false;
			}
		}
		else 
		{
		    sender.sendMessage(ConfigManager.normalheader + "Iris material is currently: " + ConfigManager.getIrisMaterial());
		    sender.sendMessage(ConfigManager.normalheader + "Valid materials are: STONE, DIAMOND_BLOCK, GLASS, IRON_BLOCK, BEDROCK, and LAPIS_BLOCK");
		}
		return true;
	}
	
	/**
	 * Do shutdown timeout.
	 *
	 * @param sender the sender
	 * @param args the args
	 * @return true, if successful
	 */
	private static boolean doShutdownTimeout(CommandSender sender, String[] args)
	{
		if ( args.length == 2)
		{
			try
			{
				int timeout = Integer.parseInt(args[1]);
				if (  timeout > -1 && timeout <= 60)
				{
					ConfigManager.setTimeoutShutdown(timeout);
					sender.sendMessage(ConfigManager.normalheader + "shutdown_timeout set to: " + ConfigManager.getTimeoutShutdown());
				}
				else
				{
				    sender.sendMessage(ConfigManager.errorheader + "Invalid shutdown_timeout: " + args[1]);
					sender.sendMessage(ConfigManager.errorheader + "Valid timeout is between 0 and 60 seconds.");
					return false;
				}
			}
			catch (Exception e)
			{
			    sender.sendMessage(ConfigManager.errorheader + "Invalid shutdown_timeout: " + args[1]);
				sender.sendMessage(ConfigManager.errorheader + "Valid timeout is between 0 and 60 seconds.");
				return false;
			}
		}
		else
		{
		    sender.sendMessage(ConfigManager.normalheader + "Current shutdown_timeout is: " + ConfigManager.getTimeoutShutdown() );
		    sender.sendMessage(ConfigManager.normalheader + "Valid timeout is between 0 and 60 seconds.");
		}
		return true;
	}
	
	/**
	 * Do activate timeout.
	 *
	 * @param sender the sender
	 * @param args the args
	 * @return true, if successful
	 */
	private static boolean doActivateTimeout(CommandSender sender, String[] args)
	{
		if ( args.length == 2)
		{
			try
			{
				int timeout = Integer.parseInt(args[1]);
				if (  timeout >= 10 && timeout <= 60)
				{
					ConfigManager.setTimeoutActivate(timeout);
					sender.sendMessage(ConfigManager.normalheader + "activate_timeout set to: " + ConfigManager.getTimeoutActivate() );
				}
				else 
				{
				    sender.sendMessage(ConfigManager.errorheader + "Invalid activate_timeout: " + args[1]);
					sender.sendMessage(ConfigManager.errorheader + "Valid timeout is between 10 and 60 seconds.");
					return false;
				}
			}
			catch (Exception e)
			{
			    sender.sendMessage(ConfigManager.errorheader + "Invalid activate_timeout: " + args[1]);
				sender.sendMessage(ConfigManager.errorheader + "Valid timeout is between 10 and 60 seconds.");
				return false;
			}
		}
		else
		{
		    sender.sendMessage(ConfigManager.normalheader + "Current activate_timeout is: " + ConfigManager.getTimeoutActivate() );
		    sender.sendMessage(ConfigManager.normalheader + "Valid timeout is between 10 and 60 seconds.");
		}
		return true;
	}
	
	/**
	 * Do owner.
	 *
	 * @param sender the sender
	 * @param args the args
	 * @return true, if successful
	 */
	private static boolean doOwner(CommandSender sender, String[] args)
	{
		if ( args.length >= 2 )
		{
			Stargate s = StargateManager.GetStargate(args[1]);
			if (s != null )
			{
				if ( args.length == 3)
				{
					s.Owner = args[2];
					sender.sendMessage(ConfigManager.normalheader + "Gate: " + s.Name + " Now owned by: " + s.Owner);
				}
				else if ( args.length == 2)
				{
				    sender.sendMessage(ConfigManager.normalheader + "Gate: " + s.Name + " Owned by: " + s.Owner);
				}
			}
			else
			{
				sender.sendMessage(ConfigManager.errorheader + "Invalid gate name: " + args[1]);
			}
		}
		else
		{
		    sender.sendMessage(ConfigManager.errorheader + "No gate name specified.");
		    return false;
		}
		return true;
	}
	

	/**
	 * Do perms.
	 *
	 * @param sender the sender
	 * @param args the args
	 */
	private static void doPerms(CommandSender sender, String[] args)
	{
		if (CommandUtlities.playerCheck(sender))
		{
			Player p = (Player) sender;
			PermissionsManager.HandlePermissionRequest(p, args);
		}
	}
    

}
