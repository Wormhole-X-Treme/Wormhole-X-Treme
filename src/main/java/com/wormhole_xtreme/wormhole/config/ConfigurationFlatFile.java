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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.config.ConfigManager.ConfigKeys;

/**
 * The Class ConfigurationFlatFile.
 * Based on class "MinecartFlatFile" from MinecartMania by Afforess.
 */
public class ConfigurationFlatFile
{

    /**
     * Creates the new header.
     * 
     * @param output
     *            the output
     * @param title
     *            the title
     * @param subtitle
     *            the subtitle
     * @param firstHeader
     *            the first header
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void createNewHeader(final BufferedWriter output, final String title, final String subtitle, final boolean firstHeader) throws IOException
    {
        final String linebreak = "-------------------------------";
        if ( !firstHeader)
        {
            output.write("---------------");
            output.newLine();
            output.newLine();
            output.write(linebreak);
            output.newLine();
        }
        output.write(title);
        output.newLine();
        output.write(subtitle);
        output.newLine();
        output.write(linebreak);
        output.newLine();
        output.newLine();
    }

    /**
     * Creates the new setting.
     * 
     * @param output
     *            the output
     * @param name
     *            the name
     * @param value
     *            the value
     * @param description
     *            the description
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void createNewSetting(final BufferedWriter output, final ConfigKeys name, final String value, final String description) throws IOException
    {
        final String linebreak = "---------------";
        output.append(linebreak);
        output.newLine();
        output.write("Setting: " + name);
        output.newLine();
        output.write("Value: " + value);
        output.newLine();
        output.write("Description:");

        final ArrayList<String> desc = new ArrayList<String>();
        desc.add(0, "");
        final int maxLength = 80;
        final String[] words = description.split(" ");
        int lineNumber = 0;
        for (final String word : words)
        {
            if (desc.get(lineNumber).length() + word.length() < maxLength)
            {
                desc.set(lineNumber, desc.get(lineNumber) + " " + word);
            }
            else
            {
                lineNumber++;
                desc.add(lineNumber, "             " + word);
            }
        }
        for (final String s : desc)
        {
            output.write(s);
            output.newLine();
        }
    }

    /**
     * Gets the value from setting.
     * 
     * @param input
     *            the input
     * @param name
     *            the name
     * @param defaultVal
     *            the default val
     * @return the value from setting
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String getValueFromSetting(final File input, final ConfigKeys name, final String defaultVal) throws IOException
    {

        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader(new FileReader(input));
            for (String s = ""; (s = bufferedReader.readLine()) != null;)
            {
                try
                {
                    s = s.trim();
                    if (s.contains("Setting:"))
                    {
                        final String key[] = s.split(":");
                        key[1] = key[1].trim();
                        final ConfigKeys key_value = ConfigKeys.valueOf(key[1]);
                        if (key_value == name)
                        {
                            //Next line
                            if ((s = bufferedReader.readLine()) != null)
                            {
                                final String val[] = s.split(":");
                                bufferedReader.close();
                                return val[1].trim();
                            }
                        }
                    }
                }
                catch (final Exception e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Error parsing setting enum:" + e.toString());
                }
            }
            bufferedReader.close();

        }
        catch (final FileNotFoundException e)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
        }
        finally
        {
            bufferedReader.close();
        }
        return defaultVal.trim();
    }
}
