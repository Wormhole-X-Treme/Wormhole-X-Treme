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
package com.wormhole_xtreme.wormhole.logic;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.permissions.StargateRestrictions;

/**
 * WormholeXtreme Runnable thread for updating stargates.
 * 
 * @author Ben Echols (Lologarithm)
 */
public class StargateUpdateRunnable implements Runnable
{

    /**
     * The Enum ActionToTake.
     */
    public enum ActionToTake
    {

        /** The SHUTDOWN task. */
        SHUTDOWN,

        /** The ANIMATE OPENING task. */
        ANIMATE_WOOSH,

        /** The DEACTIVATE task. */
        DEACTIVATE,

        /** The AFTERSHUTDOWN task. */
        AFTERSHUTDOWN,

        /** The SIGNCLICK. */
        SIGNCLICK,

        /** Action to iterate over lighting up blocks during activation. */
        LIGHTUP,

        COOLDOWN_REMOVE;
    }

    /** The stargate. */
    private final Stargate stargate;

    /** The player. */
    private final Player player;

    /** The action. */
    private final ActionToTake action;

    public StargateUpdateRunnable(final Player player, final ActionToTake action)
    {
        this(null, player, action);
    }

    /**
     * Instantiates a new stargate update runnable.
     * 
     * @param stargate
     *            the s
     * @param action
     *            the act
     */
    public StargateUpdateRunnable(final Stargate stargate, final ActionToTake action)
    {
        this(stargate, null, action);
    }

    /**
     * Instantiates a new stargate update runnable.
     * 
     * @param stargate
     *            the s
     * @param player
     *            the p
     * @param action
     *            the act
     */
    public StargateUpdateRunnable(final Stargate stargate, final Player player, final ActionToTake action)
    {
        this.stargate = stargate;
        this.action = action;
        this.player = player;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Run Action \"" + action.toString() + (stargate != null
            ? "\" Stargate \"" + stargate.getGateName()
            : "") + "\"");
        switch (action)
        {
            case SHUTDOWN :
                stargate.shutdownStargate(true);
                break;
            case ANIMATE_WOOSH :
                stargate.animateOpening();
                break;
            case DEACTIVATE :
                stargate.timeoutStargate(player);
                break;
            case AFTERSHUTDOWN :
                stargate.stopAfterShutdownTimer();
                break;
            case SIGNCLICK :
                stargate.teleportSignClicked();
                if (player != null)
                {
                    if (stargate.getGateDialSignTarget() != null)
                    {
                        final String target = stargate.getGateDialSignTarget().getGateName();
                        player.sendMessage("Dialer set to: " + target);
                    }
                    else
                    {
                        player.sendMessage("No available target to set dialer to.");
                    }
                }
                break;
            case LIGHTUP :
                stargate.lightStargate(true);
                break;
            case COOLDOWN_REMOVE :
                StargateRestrictions.removePlayerUseCooldown(player);
                break;
            default :
                break;
        }
    }
}
