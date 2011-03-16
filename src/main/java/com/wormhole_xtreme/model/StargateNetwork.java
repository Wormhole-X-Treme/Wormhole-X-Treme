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
package com.wormhole_xtreme.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.wormhole_xtreme.permissions.PermissionsManager;



/** 
 * WormholeXtreme StargateNetwork
 * @author Ben Echols (Lologarithm) 
 */ 
public class StargateNetwork 
{
	public String netName;
	
	public ArrayList<Stargate> gate_list = new ArrayList<Stargate>();
	public Object gateLock = new Object();
	
	public HashMap<String, PermissionsManager.PermissionLevel> individual_permissions = new HashMap<String, PermissionsManager.PermissionLevel>();
	public HashMap<String, PermissionsManager.PermissionLevel> group_permissions = new HashMap<String, PermissionsManager.PermissionLevel>();
}