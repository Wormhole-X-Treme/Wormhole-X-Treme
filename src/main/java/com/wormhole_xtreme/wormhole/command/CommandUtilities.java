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
package com.wormhole_xtreme.wormhole.command;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;

/**
 * WormholeXTreme Commands and command specific methods.
 * 
 * @author Dean Bailey (alron)
 * @author Ben Echols (Lologarithm)
 */
class CommandUtilities
{

    /**
     * Close gate.
     * 
     * @param stargate
     *            the stargate
     * @param player
     *            the player
     */
    static final void closeGate(final Stargate stargate)
    {
        if (stargate != null)
        {
            stargate.stopActivationTimer();
            stargate.setGateActive(false);
            stargate.lightStargate(false);
        }
    }

    /**
     * Command escaper.
     * Checks for " and escapes it.
     * 
     * @param args
     *            The String[] argument list to escape quotes on.
     * @return String[] with properly escaped quotes.
     */
    static String[] commandEscaper(final String[] args)
    {
        StringBuilder tempString = new StringBuilder();
        boolean startQuoteFound = false;
        boolean endQuoteFound = false;

        final ArrayList<String> argsPartsList = new ArrayList<String>();

        for (final String part : args)
        {
            // First check to see if we have a starting or stopping quote
            if (part.contains("\"") && !startQuoteFound)
            {
                // Two quotes in same string = no spaces in quoted text;
                if ( !part.replaceFirst("\"", "").contains("\""))
                {
                    startQuoteFound = true;
                }
            }
            else if (part.contains("\"") && startQuoteFound)
            {
                endQuoteFound = true;
            }

            // If no quotes yet, we just append to list
            if ( !startQuoteFound)
            {
                argsPartsList.add(part);
            }

            // If we have quotes we should make sure to append the values
            // if we found the last quote we should stop adding.
            if (startQuoteFound)
            {
                tempString.append(part.replace("\"", ""));
                if (endQuoteFound)
                {
                    argsPartsList.add(tempString.toString());
                    startQuoteFound = false;
                    endQuoteFound = false;
                    tempString = new StringBuilder();
                }
                else
                {
                    tempString.append(" ");
                }
            }
        }
        return argsPartsList.toArray(new String[argsPartsList.size()]);
    }

    /**
     * Gate remove.
     * 
     * @param stargate
     *            the stargate
     * @param destroy
     *            true to destroy gate blocks
     */
    static void gateRemove(final Stargate stargate, final boolean destroy)
    {
        stargate.setupGateSign(false);
        stargate.resetTeleportSign();
        if ( !stargate.getGateIrisDeactivationCode().equals(""))
        {
            if (stargate.isGateIrisActive())
            {
                stargate.toggleIrisActive(false);
            }
            stargate.setupIrisLever(false);
        }
        if (destroy)
        {
            stargate.deleteGateBlocks();
            stargate.deletePortalBlocks();
            stargate.deleteTeleportSign();
        }
        StargateManager.removeStargate(stargate);
    }

    /**
     * Gets the gate network.
     * 
     * @param stargate
     *            the stargate
     * @return the gate network
     */
    static String getGateNetwork(final Stargate stargate)
    {
        if (stargate != null)
        {
            if (stargate.getGateNetwork() != null)
            {
                return stargate.getGateNetwork().getNetworkName();
            }
        }
        return "Public";
    }

    /**
     * Player check.
     * 
     * @param sender
     *            the sender
     * @return true, if successful
     */
    static boolean playerCheck(final CommandSender sender)
    {
        if (sender instanceof Player)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
