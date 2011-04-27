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
package com.wormhole_xtreme.wormhole.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.wormhole_xtreme.wormhole.permissions.PermissionsManager;

/**
 * WormholeXtreme StargateNetwork.
 * 
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */
public class StargateNetwork
{

    /** The net name. */
    private String networkName;

    /** The gate list. */
    private ArrayList<Stargate> networkGateList = new ArrayList<Stargate>();

    /** The sign gate list. */
    private ArrayList<Stargate> networkSignGateList = new ArrayList<Stargate>();

    /** The gate lock. */
    private Object networkGateLock = new Object();

    /** The individual permissions. */
    private HashMap<String, PermissionsManager.PermissionLevel> networkIndividualPermissions = new HashMap<String, PermissionsManager.PermissionLevel>();

    /** The group permissions. */
    private HashMap<String, PermissionsManager.PermissionLevel> networkGroupPermissions = new HashMap<String, PermissionsManager.PermissionLevel>();

    /**
     * Gets the network gate list.
     * 
     * @return the network gate list
     */
    public ArrayList<Stargate> getNetworkGateList()
    {
        return networkGateList;
    }

    /**
     * Gets the network gate lock.
     * 
     * @return the network gate lock
     */
    public Object getNetworkGateLock()
    {
        return networkGateLock;
    }

    /**
     * Gets the network group permissions.
     * 
     * @return the network group permissions
     */
    public HashMap<String, PermissionsManager.PermissionLevel> getNetworkGroupPermissions()
    {
        return networkGroupPermissions;
    }

    /**
     * Gets the network individual permissions.
     * 
     * @return the network individual permissions
     */
    public HashMap<String, PermissionsManager.PermissionLevel> getNetworkIndividualPermissions()
    {
        return networkIndividualPermissions;
    }

    /**
     * Gets the network name.
     * 
     * @return the network name
     */
    public String getNetworkName()
    {
        return networkName;
    }

    /**
     * Gets the network sign gate list.
     * 
     * @return the network sign gate list
     */
    public ArrayList<Stargate> getNetworkSignGateList()
    {
        return networkSignGateList;
    }

    /**
     * Sets the network gate list.
     * 
     * @param networkGateList
     *            the new network gate list
     */
    public void setNetworkGateList(final ArrayList<Stargate> networkGateList)
    {
        this.networkGateList = networkGateList;
    }

    /**
     * Sets the network gate lock.
     * 
     * @param networkGateLock
     *            the new network gate lock
     */
    public void setNetworkGateLock(final Object networkGateLock)
    {
        this.networkGateLock = networkGateLock;
    }

    /**
     * Sets the network group permissions.
     * 
     * @param networkGroupPermissions
     *            the network group permissions
     */
    public void setNetworkGroupPermissions(final HashMap<String, PermissionsManager.PermissionLevel> networkGroupPermissions)
    {
        this.networkGroupPermissions = networkGroupPermissions;
    }

    /**
     * Sets the network individual permissions.
     * 
     * @param networkIndividualPermissions
     *            the network individual permissions
     */
    public void setNetworkIndividualPermissions(final HashMap<String, PermissionsManager.PermissionLevel> networkIndividualPermissions)
    {
        this.networkIndividualPermissions = networkIndividualPermissions;
    }

    /**
     * Sets the network name.
     * 
     * @param networkName
     *            the new network name
     */
    public void setNetworkName(final String networkName)
    {
        this.networkName = networkName;
    }

    /**
     * Sets the network sign gate list.
     * 
     * @param networkSignGateList
     *            the new network sign gate list
     */
    public void setNetworkSignGateList(final ArrayList<Stargate> networkSignGateList)
    {
        this.networkSignGateList = networkSignGateList;
    }
}