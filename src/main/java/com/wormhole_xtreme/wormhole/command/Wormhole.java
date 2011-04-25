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
package com.wormhole_xtreme.wormhole.command;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.wormhole.plugin.HelpSupport;

/**
 * The Class Wormhole.
 * 
 * @author alron
 */
public class Wormhole implements CommandExecutor
{

    /**
     * Do activate timeout.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doActivateTimeout(final CommandSender sender, final String[] args)
    {
        if (args.length == 2)
        {
            try
            {
                final int timeout = Integer.parseInt(args[1]);
                if ((timeout >= 10) && (timeout <= 60))
                {
                    ConfigManager.setTimeoutActivate(timeout);
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "activate_timeout set to: " + ConfigManager.getTimeoutActivate());
                }
                else
                {
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid activate_timeout: " + args[1]);
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid timeout is between 10 and 60 seconds.");
                    return false;
                }
            }
            catch (final NumberFormatException e)
            {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid activate_timeout: " + args[1]);
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid timeout is between 10 and 60 seconds.");
                return false;
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Current activate_timeout is: " + ConfigManager.getTimeoutActivate());
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid timeout is between 10 and 60 seconds.");
        }
        return true;
    }

    /**
     * Do iris material.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doIrisMaterial(final CommandSender sender, final String[] args)
    {
        /*if ( args.length == 2)
        {
            Material m = Material.STONE;
            try
            {
                m = Material.valueOf(args[1]);
            }
            catch (Exception e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Caught Exception on iris material" + e.getMessage());
            }

            if ( m == Material.DIAMOND_BLOCK || m == Material.GLASS || m == Material.IRON_BLOCK || m == Material.BEDROCK || m == Material.STONE || m == Material.LAPIS_BLOCK )
            {
                ConfigManager.setIrisMaterial(m);
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Iris material set to: " + ConfigManager.getIrisMaterial());
            }
            else 
            {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid Iris Material: " + args[1]);
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid materials are: STONE, DIAMOND_BLOCK, GLASS, IRON_BLOCK, BEDROCK, and LAPIS_BLOCK");
                return false;
            }
        }
        else 
        {
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Iris material is currently: " + ConfigManager.getIrisMaterial());
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid materials are: STONE, DIAMOND_BLOCK, GLASS, IRON_BLOCK, BEDROCK, and LAPIS_BLOCK");
        }
        return true;*/
        sender.sendMessage("This command is disabled. Material is now set in gate shape.");
        return true;
    }

    /**
     * Do owner.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doOwner(final CommandSender sender, final String[] args)
    {
        if (args.length >= 2)
        {
            final Stargate s = StargateManager.getStargate(args[1]);
            if (s != null)
            {
                if (args.length == 3)
                {
                    s.owner = args[2];
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Gate: " + s.name + " Now owned by: " + s.owner);
                }
                else if (args.length == 2)
                {
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Gate: " + s.name + " Owned by: " + s.owner);
                }
            }
            else
            {
                sender.sendMessage(ConfigManager.MessageStrings.constructNameInvalid.toString() + "\"" + args[1] + "\"");
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.gateNotSpecified.toString());
            return false;
        }
        return true;
    }

    /**
     * Do perms.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     */
    private static void doPerms(final CommandSender sender, final String[] args)
    {
        if (CommandUtilities.playerCheck(sender))
        {
            final Player p = (Player) sender;
            PermissionsManager.handlePermissionRequest(p, args);
        }
    }

    /**
     * Do Portal Material.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doPortalMaterial(final CommandSender sender, final String[] args)
    {
        /* if ( args.length == 2)
        {
            Material m = Material.STONE;
            try
            {
                m = Material.valueOf(args[1]);
            }
            catch (Exception e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Caught Exception on iris material" + e.getMessage());
            }

            if ( m == Material.STATIONARY_LAVA || m == Material.STATIONARY_WATER || m == Material.AIR || m == Material.PORTAL )
            {
                ConfigManager.setPortalMaterial(m);
                sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Portal material set to: " + ConfigManager.getPortalMaterial());
            }
            else
            {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid Portal Material: " + args[1]);
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid materials are: STATIONARY_WATER, STATIONARY_LAVA, AIR, PORTAL");
                return false;
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Portal material is currently: " + ConfigManager.getPortalMaterial());
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid materials are: STATIONARY_WATER, STATIONARY_LAVA, AIR, PORTAL");
        }
        return true;*/
        sender.sendMessage("This command is disabled. Material is now set in gate shape.");
        return true;
    }

    /**
     * Do regenerate.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doRegenerate(final CommandSender sender, final String[] args)
    {
        final CommandSender cs = sender;
        final String[] a = args;
        if (a.length >= 2)
        {
            final Stargate s = StargateManager.getStargate(args[1]);
            if (s != null)
            {
                s.dialButtonLeverState(true);
                if ( !s.irisDeactivationCode.equals(""))
                {
                    s.setupIrisLever(false);
                    s.setupIrisLever(true);
                }
                cs.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Regenerating Gate: " + s.name);
            }
            else
            {
                cs.sendMessage(ConfigManager.MessageStrings.constructNameInvalid.toString() + "\"" + a[1] + "\"");
            }
        }
        else
        {
            cs.sendMessage(ConfigManager.MessageStrings.gateNotSpecified.toString());
            return false;
        }
        return true;
    }

    /**
     * Do shutdown timeout.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doShutdownTimeout(final CommandSender sender, final String[] args)
    {
        if (args.length == 2)
        {
            try
            {
                final int timeout = Integer.parseInt(args[1]);
                if ((timeout > -1) && (timeout <= 60))
                {
                    ConfigManager.setTimeoutShutdown(timeout);
                    sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "shutdown_timeout set to: " + ConfigManager.getTimeoutShutdown());
                }
                else
                {
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid shutdown_timeout: " + args[1]);
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid timeout is between 0 and 60 seconds.");
                    return false;
                }
            }
            catch (final NumberFormatException e)
            {
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid shutdown_timeout: " + args[1]);
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid timeout is between 0 and 60 seconds.");
                return false;
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Current shutdown_timeout is: " + ConfigManager.getTimeoutShutdown());
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid timeout is between 0 and 60 seconds.");
        }
        return true;
    }

    /**
     * Do simple permissions.
     * 
     * @param sender
     *            the sender
     * @param args
     *            the args
     * @return true, if successful
     */
    private static boolean doSimplePermissions(final CommandSender sender, final String[] args)
    {
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
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Invalid Setting: " + args[1]);
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid options: true/yes, false/no");
                return false;
            }
            if ((WormholeXTreme.getPermissions() != null) && CommandUtilities.playerCheck(sender))
            {
                player = (Player) sender;
                if (simple && !WormholeXTreme.getPermissions().has(player, "wormhole.simple.config"))
                {
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "You currently do not have the 'wormhole.simple.config' permission.");
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Please make sure you have this permission before running this command again.");
                    return true;
                }
                else if ( !simple && !WormholeXTreme.getPermissions().has(player, "wormhole.config"))
                {
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "You currently do not have the 'wormhole.config' permission.");
                    sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Please make sure you have this permission before running this command again.");
                    return true;
                }
            }
            ConfigManager.setSimplePermissions(simple);
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Simple Permissions set to: " + ConfigManager.getSimplePermissions());
            if ( !ConfigManager.getHelpSupportDisable())
            {
                HelpSupport.registerHelpCommands();
            }
            if (player != null)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "Simple Permissions set to: \"" + simple + "\" by: \"" + player.getName() + "\"");
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Simple Permissions: " + ConfigManager.getSimplePermissions());
            sender.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid options: true/yes, false/no");
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
    {
        final Player player = CommandUtilities.playerCheck(sender) ? (Player) sender : null;
        if (((player != null) && WXPermissions.checkWXPermissions(player, PermissionType.CONFIG)) || !CommandUtilities.playerCheck(sender))
        {
            final String[] a = CommandUtilities.commandEscaper(args);
            if ((a.length > 4) || (a.length == 0))
            {
                return false;
            }
            if (a[0].equalsIgnoreCase("owner"))
            {
                return doOwner(sender, a);
            }
            else if (a[0].equalsIgnoreCase("perm") || a[0].equalsIgnoreCase("perms"))
            {
                doPerms(sender, a);
            }
            else if (a[0].equalsIgnoreCase("material") || a[0].equalsIgnoreCase("portalmaterial"))
            {
                return doPortalMaterial(sender, a);
            }
            else if (a[0].equalsIgnoreCase("irismaterial"))
            {
                return doIrisMaterial(sender, a);
            }
            else if (a[0].equalsIgnoreCase("timeout") || a[0].equalsIgnoreCase("shutdown_timeout"))
            {
                return doShutdownTimeout(sender, a);
            }
            else if (a[0].equalsIgnoreCase("activate_timeout"))
            {
                return doActivateTimeout(sender, a);
            }
            else if (a[0].equalsIgnoreCase("simple"))
            {
                return doSimplePermissions(sender, a);
            }
            else if (a[0].equalsIgnoreCase("regenerate") || a[0].equalsIgnoreCase("regen"))
            {
                return doRegenerate(sender, a);
            }
            else
            {
                sender.sendMessage(ConfigManager.MessageStrings.requestInvalid.toString() + ": " + a[0]);
                sender.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Valid commands are 'owner', 'perms', 'portalmaterial', 'irismaterial', 'shutdown_timeout', 'activate_timeout', 'simple' and 'regenerate'.");
            }
        }
        else
        {
            sender.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
        }
        return true;
    }
}
