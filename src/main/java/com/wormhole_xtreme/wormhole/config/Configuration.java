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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.plugin.PluginDescriptionFile;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager.PermissionLevel;

/**
 * The Class Configuration.
 * Bssed on class "Configuration" from MinecartMania by Afforess.
 */
public class Configuration
{

    /** The options. */
    private static File options = null;

    /**
     * Invalid file.
     * 
     * @param file
     *            the file
     * @param desc
     *            the desc
     * @return true, if successful
     */
    private static boolean invalidFile(final File file, final PluginDescriptionFile desc)
    {
        BufferedReader bufferedreader = null;
        try
        {
            bufferedreader = new BufferedReader(new FileReader(file));
            for (String s = ""; (s = bufferedreader.readLine()) != null;)
            {
                if (s.indexOf(desc.getVersion()) > -1)
                {
                    return false;
                }
            }
        }
        catch (final IOException exception)
        {
            return true;
        }
        finally
        {
            try
            {
                if (bufferedreader != null)
                {
                    bufferedreader.close();
                }
            }
            catch (final IOException e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Failure to close stream: " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * Load configuration.
     * 
     * @param desc
     *            the desc
     */
    protected static void loadConfiguration(final PluginDescriptionFile desc)
    {
        readFile(desc);
    }

    /**
     * Read file.
     * 
     * @param file
     *            the file
     * @param desc
     *            the desc
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void readFile(final File file, final PluginDescriptionFile desc) throws IOException
    {

        for (final Setting element : DefaultSettings.config)
        {

            final String value = ConfigurationFlatFile.getValueFromSetting(file, element.getName(), element.getValue().toString());

            //Attempt to parse the value as boolean
            if (value.toLowerCase().contains("true") || value.toLowerCase().contains("false"))
            {
                final Setting s = new Setting(element.getName(), Boolean.parseBoolean(value), element.getDescription(), "WormholeXTreme");
                ConfigManager.configurations.put(s.getName(), s);
            }
            else
            {
                Setting s = null;
                try
                {
                    s = new Setting(element.getName(), Double.parseDouble(value), element.getDescription(), "WormholeXTreme");
                }
                catch (final NumberFormatException nfe)
                {
                    // Probably an enum
                    if (element.getName() == ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL)
                    {
                        s = new Setting(element.getName(), PermissionLevel.valueOf(value), element.getDescription(), "WormholeXTreme");
                    }
                    else
                    {
                        // I guess its a string
                        s = new Setting(element.getName(), value, element.getDescription(), "WormholeXTreme");
                    }
                }

                ConfigManager.configurations.put(s.getName(), s);
            }
        }
    }

    /**
     * Read file.
     * 
     * @param desc
     *            the desc
     */
    private static void readFile(final PluginDescriptionFile desc)
    {
        final File directory = new File("plugins" + File.separator + desc.getName() + File.separator);
        if ( !directory.exists())
        {
            try
            {
                directory.mkdir();
            }
            catch (final Exception e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to make directory: " + e.getMessage());
            }
        }
        final String input = directory.getPath() + File.separator + "Settings.txt";
        options = new File(input);
        if ( !options.exists())
        {
            writeFile(options, desc, DefaultSettings.config);
        }
        try
        {
            readFile(options, desc);
        }
        catch (final IOException e)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Failed to read fiele: " + e.getMessage());
        }
        if (invalidFile(options, desc))
        {
            writeFile(desc);
        }
    }

    /**
     * Write file.
     * 
     * @param file
     *            the file
     * @param desc
     *            the desc
     * @param config
     *            the config
     */
    private static void writeFile(final File file, final PluginDescriptionFile desc, final Setting[] config)
    {
        try
        {
            try
            {
                file.createNewFile();
            }
            catch (final Exception e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to Create File: " + e.getMessage());
            }
            final BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(file));

            ConfigurationFlatFile.createNewHeader(bufferedwriter, desc.getName() + " " + desc.getVersion(), desc.getName() + " Config Settings", true);

            for (final Setting element : config)
            {
                ConfigurationFlatFile.createNewSetting(bufferedwriter, element.getName(), element.getValue().toString(), element.getDescription());
            }
            bufferedwriter.close();
        }
        catch (final Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Write file.
     * 
     * @param desc
     *            the desc
     */
    public static void writeFile(final PluginDescriptionFile desc)
    {
        try
        {
            try
            {
                options.createNewFile();
            }
            catch (final Exception e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to create new file: " + e.getMessage());
            }
            final BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(options));

            ConfigurationFlatFile.createNewHeader(bufferedwriter, desc.getName() + " " + desc.getVersion(), desc.getName() + " Config Settings", true);

            final Set<ConfigKeys> keys = ConfigManager.configurations.keySet();
            final ArrayList<ConfigKeys> list = new ArrayList<ConfigKeys>(keys);
            Collections.sort(list);
            for (final ConfigKeys key : list)
            {
                final Setting s = ConfigManager.configurations.get(key);
                if (s != null)
                {
                    ConfigurationFlatFile.createNewSetting(bufferedwriter, s.getName(), s.getValue().toString(), s.getDescription());
                }

            }
            bufferedwriter.close();
        }
        catch (final Exception exception)
        {
            exception.printStackTrace();
        }
    }

}
