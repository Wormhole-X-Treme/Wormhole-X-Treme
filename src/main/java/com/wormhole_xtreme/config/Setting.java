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
package com.wormhole_xtreme.config;

import java.util.logging.Level;


import org.bukkit.Material;

import com.wormhole_xtreme.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;



/*
 * This class is based on a class "Setting.java" 
 * from MinecartMania written by Afforess from Bukkit.org
 */
public class Setting 
{

	private ConfigKeys name;
	private String desc;
	private Object value;
	private String plugin;

	public Setting(ConfigKeys name, Object value, String desc, String plugin) 
	{
		if (name != null && desc != null && value != null && plugin != null ) {
			this.name = name;
			this.desc = desc;
			this.value = value;
			this.plugin = plugin;
		}
	}
	
	public ConfigKeys getName() 
	{
		return name;
	}

	public String getDescription() {
		return desc;
	}

	public Object getValue() {
		return value;
	}
	
	public boolean getBooleanValue()
	{
		return ((Boolean)value).booleanValue();
	}

	public int getIntValue()
	{
		return ((Integer)value).intValue();
	}
	
	public double getDoubleValue()
	{
		return ((Double)value).doubleValue();
	}

	public String getStringValue()
	{
		return (String)value;
	}
	
	public Material getMaterialValue()
	{
		return (Material)value;
	}
	
	public String getPluginName() 
	{
		return plugin;
	}
	
	public PermissionLevel getPermissionLevel()
	{
		return (PermissionLevel)value;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
	}
	public Level getLevel()
	{
		return Level.parse((String)value);
	}
	/*
	public boolean isBoolean() {
		return value instanceof Boolean;
	}

	public boolean isInteger() {
		return value instanceof Integer;
	}

	public boolean isDouble() {
		return value instanceof Double;
	}

	public boolean isString() {
		return value instanceof String;
	}*/
}
