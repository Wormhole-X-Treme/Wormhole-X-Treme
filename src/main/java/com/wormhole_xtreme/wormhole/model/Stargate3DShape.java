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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;

import com.wormhole_xtreme.wormhole.WormholeXTreme;

/**
 * The Class Stargate3DShape.
 */
public class Stargate3DShape extends StargateShape
{
    /**
     * Layers of the 3D shape. Layers go from 1 - 10
     */
    public ArrayList<StargateShapeLayer> layers = new ArrayList<StargateShapeLayer>();

    /** The activation_layer. */
    public int activation_layer = -1;

    /** The sign_layer. */
    public int sign_layer = -1;

    /**
     * Instantiates a new stargate3 d shape.
     * 
     * @param fileLines
     *            the file lines
     */
    public Stargate3DShape(final String[] fileLines)
    {
        signPosition = null;
        enterPosition = null;

        // 1. scan all lines for lines beginning with [  - that is the height of the gate
        int height = 0;
        int width = 0;
        for (int i = 0; i < fileLines.length; i++)
        {
            final String line = fileLines[i];

            if (line.startsWith("#"))
            {
                continue;
            }

            if (line.contains("Name="))
            {
                shapeName = line.split("=")[1];
                WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + shapeName + "\"");
            }
            else if (line.equals("GateShape="))
            {
                int index = i;
                // Find start of first line
                while ( !fileLines[index].startsWith("["))
                {
                    index++;
                }

                while (fileLines[index].startsWith("["))
                {
                    if (width <= 0)
                    {
                        final Pattern p = Pattern.compile("(\\[.*?\\])");
                        final Matcher m = p.matcher(fileLines[index]);
                        while (m.find())
                        {
                            width++;
                        }
                    }

                    height++;
                    index++;
                }

                // At this point we should know the height and width
                if ((height <= 0) || (width <= 0))
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to parse custom gate due to incorrect height or width: \"" + shapeName + "\"");
                    throw new IllegalArgumentException("Unable to parse custom gate due to incorrect height or width: \"" + shapeName + "\"");
                }
                else
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Shape: \"" + shapeName + "\"" + " Height: \"" + Integer.toString(height) + "\"" + " Width: \"" + Integer.toString(width) + "\"");
                }
            }
            else if (line.startsWith("Layer"))
            {
                // TODO : Add some debug output for each layer!
                // 1. get layer #
                final int layer = Integer.valueOf(line.trim().split("[#=]")[1]);

                // 2. add each line that starts with [ to a new string[]
                i++;
                final String[] layerLines = new String[height];
                int line_index = 0;
                while (fileLines[i].startsWith("[") || fileLines[i].startsWith("#"))
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Layer=" + layer + " i=" + i + " line_index=" + line_index + " Line=" + fileLines[i]);
                    layerLines[line_index] = fileLines[i];
                    i++;

                    if (fileLines[i].startsWith("#"))
                    {
                        continue;
                    }

                    line_index++;
                }

                // 3. call constructor
                final StargateShapeLayer ssl = new StargateShapeLayer(layerLines, height, width);
                // bad hack to make sure list is big enough :(
                while (layers.size() <= layer)
                {
                    layers.add(null);
                }
                layers.set(layer, ssl);

                if (ssl.activationPosition != null)
                {
                    activation_layer = layer;
                }
                if (ssl.dialerPosition != null)
                {
                    sign_layer = layer;
                }
            }
            else if (line.contains("PORTAL_MATERIAL="))
            {
                portalMaterial = Material.valueOf(line.split("=")[1]);
            }
            else if (line.contains("IRIS_MATERIAL="))
            {
                irisMaterial = Material.valueOf(line.split("=")[1]);
            }
            else if (line.contains("STARGATE_MATERIAL="))
            {
                stargateMaterial = Material.valueOf(line.split("=")[1]);
            }
            else if (line.contains("ACTIVE_MATERIAL="))
            {
                activeMaterial = Material.valueOf(line.split("=")[1]);
            }
            else if (line.contains("LIGHT_TICKS="))
            {
                lightTicks = Integer.valueOf(line.split("=")[1]);
            }
            else if (line.contains("WOOSH_TICKS="))
            {
                wooshTicks = Integer.valueOf(line.split("=")[1]);
            }
        }
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + shapeName + "\"");
    }
}
