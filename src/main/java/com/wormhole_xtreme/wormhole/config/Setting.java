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
package com.wormhole_xtreme.wormhole.config;

import java.util.logging.Level;

import org.bukkit.Material;

import com.wormhole_xtreme.wormhole.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager.PermissionLevel;

/**
 * The Class Setting.
 * Based on class "Setting" from MinecartMania by Afforess.
 */
class Setting
{

    /** The name. */
    private final ConfigKeys name;

    /** The desc. */
    private final String desc;

    /** The value. */
    private Object value;

    /** The plugin. */
    private final String plugin;

    /**
     * Instantiates a new setting.
     * 
     * @param name
     *            the name
     * @param value
     *            the value
     * @param desc
     *            the desc
     * @param plugin
     *            the plugin
     */
    protected Setting(final ConfigKeys name, final Object value, final String desc, final String plugin)
    {
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.plugin = plugin;
    }

    /**
     * Gets the boolean value.
     * 
     * @return the boolean value
     */
    public boolean getBooleanValue()
    {
        return ((Boolean) value).booleanValue();
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription()
    {
        return desc;
    }

    /**
     * Gets the double value.
     * 
     * @return the double value
     */
    public double getDoubleValue()
    {
        return ((Double) value).doubleValue();
    }

    /**
     * Gets the int value.
     * 
     * @return the int value
     */
    public int getIntValue()
    {
        return ((Integer) value).intValue();
    }

    /**
     * Gets the level.
     * 
     * @return the level
     */
    public Level getLevel()
    {
        return Level.parse((String) value);
    }

    /**
     * Gets the material value.
     * 
     * @return the material value
     */
    public Material getMaterialValue()
    {
        return (Material) value;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public ConfigKeys getName()
    {
        return name;
    }

    /**
     * Gets the permission level.
     * 
     * @return the permission level
     */
    public PermissionLevel getPermissionLevel()
    {
        return (PermissionLevel) value;
    }

    /**
     * Gets the plugin name.
     * 
     * @return the plugin name
     */
    public String getPluginName()
    {
        return plugin;
    }

    /**
     * Gets the string value.
     * 
     * @return the string value
     */
    public String getStringValue()
    {
        return (String) value;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            the new value
     */
    public void setValue(final Object value)
    {
        this.value = value;
    }
}
