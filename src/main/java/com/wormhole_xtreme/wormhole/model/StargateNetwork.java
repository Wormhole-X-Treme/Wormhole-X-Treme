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



// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme StargateNetwork.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class StargateNetwork 
{
	
	/** The net name. */
	public String netName;
	
	/** The gate_list. */
	public ArrayList<Stargate> gateList = new ArrayList<Stargate>();
	
	/** The gate lock. */
	public Object gateLock = new Object();
	
	/** The individual_permissions. */
	public HashMap<String, PermissionsManager.PermissionLevel> individualPermissions = new HashMap<String, PermissionsManager.PermissionLevel>();
	
	/** The group_permissions. */
	public HashMap<String, PermissionsManager.PermissionLevel> groupPermissions = new HashMap<String, PermissionsManager.PermissionLevel>();
}