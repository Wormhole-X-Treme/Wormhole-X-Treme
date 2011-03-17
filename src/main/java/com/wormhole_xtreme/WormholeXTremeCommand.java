/*
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
package com.wormhole_xtreme;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.permissions.PermissionsManager;


/**
 * WormholeXTreme Commands and command specific methods.
 *
 * @author Dean Bailey (alron)
 * @author Ben Echols (Lologarithm)
 */
public class WormholeXTremeCommand {


	/**
	 * Player check.
	 *
	 * @param sender the sender
	 * @return true, if successful
	 */
	public static boolean playerCheck(CommandSender sender) {
		if (sender instanceof Player)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Command escaper.
	 * Checks for " and escapes it.
	 *
	 * @param args The String[] argument list to escape quotes on.
	 * @return String[] with properly escaped quotes.
	 */
	public static String[] commandEscaper(String[] args)
	{
		StringBuilder temp_string = new StringBuilder();
		boolean start_quote_found = false;
		boolean end_quote_found = false;
		
		ArrayList<String> args_parts_list = new ArrayList<String>();
		
		for(String part : args)
		{
			// First check to see if we have a starting or stopping quote
			if ( part.contains("\"") && !start_quote_found)
			{
				// Two quotes in same string = no spaces in quoted text;
				if ( !part.replaceFirst("\"", "").contains("\"") )
				{
					start_quote_found = true;
				}
			}
			else if ( part.contains("\"") && start_quote_found)
			{
				end_quote_found = true;
			}

			// If no quotes yet, we just append to list
			if ( !start_quote_found )
				args_parts_list.add(part);
			
			// If we have quotes we should make sure to append the values
			// if we found the last quote we should stop adding.
			if ( start_quote_found)
			{
				temp_string.append(part.replace("\"", ""));
				if ( end_quote_found )
				{
					args_parts_list.add(temp_string.toString());
					start_quote_found = false;
					end_quote_found = false;
					temp_string = new StringBuilder();
				}
				else
					temp_string.append(" ");
			}
		}
		return args_parts_list.toArray(new String[] {});
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
		if (playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}	
		if (allowed || !playerCheck(s))
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
		if (playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}
		if (allowed || !playerCheck(s))
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
		if (playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || !playerCheck(p) || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
			
		}
		if (allowed || !playerCheck(s))
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
		if (playerCheck(s))
		{
			Player p = (Player) s;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}
		if (allowed || !playerCheck(s))
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
		if (playerCheck(sender))
		{
			Player p = (Player) sender;
			if ( p.isOp() || ( WormholeXTreme.Permissions != null && WormholeXTreme.Permissions.has(p, "wormhole.config")))
			{
				allowed = true;
			}
		}
		if (allowed || !playerCheck(sender))
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
		if (playerCheck(sender))
		{
			Player p = (Player) sender;
			PermissionsManager.HandlePermissionRequest(p, args);
		}
		else
		{
			// PermissionsManager.HandlePermissionRequest(sender, args);
		}
	}
	
	
	/**
	 * Command wormhole.
	 *
	 * @param sender the sender
	 * @param args the args
	 * @return true, if successful
	 */
	public static boolean commandWormhole(CommandSender sender, String[] args)
	{
		Player p = null;
		String[] message_parts = commandEscaper(args);
		if ((message_parts.length > 4 ) || (message_parts.length == 0)) {
			return false;
		}
		if (playerCheck(sender))
		{
			p = (Player) sender;
		}

        if ( message_parts[0].equalsIgnoreCase("owner"))
		{
			doOwner(sender,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("perm") || message_parts[0].equalsIgnoreCase("perms"))
		{
			doPerms(sender,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("material") )
		{
			doMaterial(sender,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("irismaterial") )
		{
			doIrisMaterial(sender,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("timeout") || message_parts[0].equalsIgnoreCase("shutdown_timeout") )
		{
			doShutdownTimeout(sender,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("activate_timeout") )
		{
			doActivateTimeout(sender,message_parts);
		}
		else if (p != null)
			{
				p.sendMessage(ConfigManager.output_strings.get(StringTypes.REQUEST_INVALID));
				return false;
			}
			else 
			{
				sender.sendMessage(ConfigManager.output_strings.get(StringTypes.REQUEST_INVALID));
				return true;
			}
		return true;
	}
}
