/**
 * 
 */
package com.wormhole_xtreme;

import java.util.ArrayList;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.logic.StargateHelper;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.model.StargateShape;
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;


/**
 * @author alron
 *
 */
public class WormholeXTremeCommand {


	private static boolean playerCheck(CommandSender sender) {
		if (sender instanceof Player)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/*
	 * Checks for " and escapes it.
	 * Returns String[] with properly escaped quotes.
	 */
	private static String[] commandEscaper(String[] args)
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
	
	private static void doCompassPoint(Player p)
	{
		Location current = p.getLocation();
		// HaNieL from Bukkit.org gave me this!
		ArrayList<Stargate> gates = StargateManager.GetAllGates();
        double man = Double.MAX_VALUE;
        Stargate closest = null;
        
        for(Stargate s : gates)
        {
            Location t = s.TeleportLocation;
            double distance = Math.sqrt( Math.pow(current.getX() - t.getX(), 2) + 
            							Math.pow(current.getY() - t.getY(), 2) +
            							Math.pow(current.getZ() - t.getZ(), 2) );
            if(distance < man)
            {
                man = distance;
                closest = s;
            }
        }
        
        if(closest != null)
        {
            p.setCompassTarget(closest.TeleportLocation);
            p.sendMessage("Compass set to wormhole: " + closest.Name);
        }
        else
        {
        	p.sendMessage("No wormholes to track!");
        }
	}
	
	private static void doGateList(CommandSender s)
	{
		boolean allowed = false;
		Player p = null;
		
		if (playerCheck(s)) {
			p = (Player) s;
			if ( p.isOp() || (WormholeXTreme.Permissions != null && (WormholeXTreme.Permissions.has(p, "wormhole.config")) || (WormholeXTreme.Permissions.has(p, "wormhole.list"))))
			{
				allowed = true;
			}
		}
		if (  !playerCheck(s) || allowed )
		{
			ArrayList<Stargate> gates = StargateManager.GetAllGates();
			StringBuilder sb = new StringBuilder("List of all gates: ");
			for ( int i = 0; i < gates.size(); i++)
			{
				sb.append(gates.get(i).Name);
				
				if ( i != gates.size() - 1)
					sb.append(",");
			}
			s.sendMessage(sb.toString());
		}
	}
	private static void doGateComplete(Player p, String[] args)
	{
		
		if ( args.length >= 2)
		{
			if ( args[1].length() < 12 )
			{
				args[1] = args[1].trim().replace("\n", "").replace("\r", "");
				String idc = "";
				if (  args.length == 3)
				{
					idc = args[2];
				}
				Stargate dup_name = StargateManager.GetStargate( args[1] );
				if ( dup_name == null )
				{
					boolean success = StargateManager.CompleteStargate(p, args[1], idc);
					
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
				p.sendMessage( ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_TOO_LONG) );
			}
		}
	}
	private static boolean doGateRemove(CommandSender sender, String[] args, boolean root_command)
	{
		
		if ( (root_command && args.length >=1) || (!root_command && args.length >= 2) )
		{
		    String mpa;
		    String mpb;

		    if (root_command)
		    {
		        mpa = args[0];
		        mpb = args[1];
		    }
		    else
		    {
		        mpa = args[1];
		        mpb = args[2];
		    }

		    if (mpa.equals("-all"))
		    {
		        return false;
		    }
			Stargate s = StargateManager.GetStargate(mpa);
			if ( s != null )
			{
				boolean allowed = false;
				if (playerCheck(sender))
				{
					Player p = (Player) sender;
					if  (WormholeXTreme.Permissions != null && (WormholeXTreme.Permissions.has(p, "wormhole.remove.all") || 
						( s.Owner != null && s.Owner.equals(p.getName()) && WormholeXTreme.Permissions.has(p, "wormhole.remove.own"))))
					{
						allowed = true;
					}
					else if (PermissionsManager.getPermissionLevel(p, s) == PermissionsManager.PermissionLevel.WORMHOLE_FULL_PERMISSION )
					{
						allowed = true;
					}
				}
				if ( !playerCheck(sender) || allowed )
				{
					s.DeleteNameSign();
					s.ResetTeleportSign();
					if (!s.IrisDeactivationCode.equals(""))
					{
					    s.DeleteIrisLever();
					}
					if ( ((root_command && args.length == 2) || (!root_command && args.length == 3 )) && mpb.equals("-all") )
					{
						s.DeleteNameSign();
						s.DeleteGateBlocks();
						s.DeletePortalBlocks();
						s.DeleteTeleportSignBlock();
						s.DeleteNameBlock();
					}
					StargateManager.RemoveStargate(s);
					sender.sendMessage("Wormhole Removed: " + s.Name);
					return true;
				}
				else
				{
					sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
					return true;
				}
					
			}
			else
			{
				sender.sendMessage("Gate does not exist: " + mpa + ". Remember proper capitalization.");

			}
		}
		else
		{
			sender.sendMessage("You did not enter a gate name");
		}
		return false;
	}
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
				
				if ( m == Material.DIAMOND_BLOCK || m == Material.GLASS || m == Material.IRON_BLOCK || m == Material.BEDROCK || m == Material.STONE )
				{
					ConfigManager.setIrisMaterial(m);
				}
			}

			s.sendMessage("Iris material is currently: " + ConfigManager.getIrisMaterial());
			s.sendMessage("Valid materials are: STONE, DIAMOND_BLOCK, GLASS, IRON_BLOCK, BEDROCK");
		}
		else
		{
			s.sendMessage( ConfigManager.output_strings.get(StringTypes.PERMISSION_NO) );
		}
	}
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
	
	private static void doGo(Player p, String[] args)
	{
		if (p.isOp())
		{
			if ( args.length == 2)
			{
				Stargate s = StargateManager.GetStargate(args[1]);
				if ( s != null )
				{
					p.teleportTo(s.TeleportLocation);
				}
				else
				{
					p.sendMessage("Gate does not exist: " + args[1]);
				}
			}
		}
	}
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
					
					sender.sendMessage("Gate: " + s.Name + " Owned by:" + s.Owner);
				}
				else
				{
					sender.sendMessage("Invalid gate name.");
				}
			}
			else
			{
				sender.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
			}
	}
	private static void doDial(Player p, String[] args)
	{
		Stargate start = StargateManager.RemoveActivatedStargate(p);
		if (start != null)
		{
			boolean allowed = false;
			if (WormholeXTreme.Permissions != null)
			{
				if (WormholeXTreme.Permissions.has(p, "wormhole.use.dialer"))
				{
					allowed = true;
				}
			}
			else
			{
				PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, start);
				if ( lvl.equals(PermissionLevel.WORMHOLE_FULL_PERMISSION) || lvl.equals(PermissionLevel.WORMHOLE_CREATE_PERMISSION) || lvl.equals(PermissionLevel.WORMHOLE_USE_PERMISSION)) {
					allowed = true;
				}
			}
			
			if ( allowed )
			{
				if ( !start.Name.equals(args[0]) )
				{
					Stargate target = StargateManager.GetStargate(args[0]);
					if ( target != null)
					{
						if (!target.IrisDeactivationCode.equals("") && target.IrisActive)
						{
							if ( args.length >= 2 && target.IrisDeactivationCode.equals(args[1]))
							{
								if ( target.IrisActive )
								{
									target.ToggleIrisActive();
									p.sendMessage("IDC accepted and Iris has been deactivated.");
								}
							}
						}
						
						if ( start.DialStargate(target) ) 
						{
							p.sendMessage("Stargates connected!");
						}
						else
						{
							start.UnLightStargate();
							p.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_IS_ACTIVE));
						}
					}
					else
					{
						start.UnLightStargate();
						p.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_INVALID));
					}
				}
				else
				{
					start.UnLightStargate();
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_IS_SELF));
				}
			}
			else
			{
				p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
			}
		}
		else
		{
			p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_NOT_ACTIVE));
		}
	}
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
	
	private static boolean doAddPlayerBuilder(CommandSender sender, String[] message_parts, boolean root_command) 
	{
		if ( playerCheck(sender) )
		{
			Player p = (Player) sender;
			if ( (root_command && message_parts.length >= 1) || (!root_command && message_parts.length >= 2) )
			{
			    String mp;
			    if (root_command)
			    {
			        mp = message_parts[0];
			    }
			    else
			    {
			        mp = message_parts[1];
			    } 
				if ( p.isOp() || WormholeXTreme.Permissions.has(p, "wormhole.config") )
				{
					StargateShape shape = StargateHelper.getShape(mp);
					if  ( shape != null)
					{
						StargateManager.AddPlayerBuilderShape(p, shape);
						p.sendMessage("Press Activation button on new DHD to autobuild Stargate in the shape of: " + mp );
						return true;
					}
					else
					{
						p.sendMessage("Invalid shape: " + mp);
					}
				}
				else
				{
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
					return true;
				}
			}
			else
			{
				p.sendMessage("Build command requires a shape");
			}
		}
		else
		{
		    return true;
			 //("Cannot use this command without being a player.")
		}
		return false;
	}
	
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

		if( message_parts[0].equalsIgnoreCase("complete") && p != null )
		{
			doGateComplete(p, message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("compass") && p != null)
		{
			doCompassPoint(p);
		}
		else if (message_parts[0].equalsIgnoreCase("go") && p != null )
		{
			doGo(p,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("dial") && p != null)
		{
			doDial(p,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("list") )
		{
			doGateList(sender);
		}
		else if ( message_parts[0].equalsIgnoreCase("owner"))
		{
			doOwner(sender,message_parts);
		}
		else if ( message_parts[0].equalsIgnoreCase("remove") )
		{
			return doGateRemove(sender,message_parts,false);
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
		else if ( message_parts[0].equalsIgnoreCase("build") )
		{
			return doAddPlayerBuilder(sender,message_parts,false);
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

	/*
	 * Dial manual Stargate
	 * Check if CommandSender is a player, if !player return true as this must be a console.
	 * Otherwise check to see if there is a Stargate name specified, also check for IDC.
	 * If Stargate is specified dial it.
	 * If IDC is specified, try and use it during the dial.
	 * 
	 */
	public static boolean commandDial(CommandSender sender, String[] args)
	{
		Player player = null;
		if (!playerCheck(sender))
		{
			return true;
		}
		else {
			player = (Player) sender;
		}
		String[] message_parts = commandEscaper(args);
		if ((message_parts.length > 2 ) || (message_parts.length == 0)) {
			return false;
		}
		
		doDial(player,args);
		return true;
	}
	
	/*
	 * List Stargates
	 */
	public static boolean commandList(CommandSender sender, String[] args)
	{
	    doGateList(sender);
	    return true;
	}
	
	/*
	 * Build Stargate
	 */
	public static boolean commandBuildGate(CommandSender sender, String[] args)
	{	    
	    return doAddPlayerBuilder(sender, args,true);
	}
	/*
	 * Remove stargate (and delete gate blocks too)
	 */
	public static boolean commandRemoveGate(CommandSender sender, String[] args)
	{
	    return doGateRemove(sender, args, true);
	}
}
