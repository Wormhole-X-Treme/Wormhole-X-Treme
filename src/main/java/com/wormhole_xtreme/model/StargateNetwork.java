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