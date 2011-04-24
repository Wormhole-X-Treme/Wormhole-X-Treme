/*
 * Wormhole X-Treme Plugin for Bukkit
 * Copyright (C) 2011 Ben Echols
 *                    Dean Bailey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.wormhole_xtreme.wormhole.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wormhole_xtreme.wormhole.WormholeXTreme;

/**
 * The Class StargateShapeLayer.
 */
public class StargateShapeLayer
{

    /** The block positions. */
    public ArrayList<Integer[]> blockPositions = new ArrayList<Integer[]>();

    /** The sign position. */
    public int[] signPosition = null;

    /** The enter position. */
    public int[] enterPosition = null;

    /** The activation position. */
    public int[] activationPosition = null;

    /** The iris activation position. */
    public int[] irisActivationPosition = null;

    /** The dialer position. */
    public int[] dialerPosition = null;
    /** Position of point that allows gate to be activated via redstone. */
    public int[] redstoneActivationPosition = null;
    /** Position of point that allows gate to cycle sign targets via redstone. */
    public int[] redstoneDialerActivationPosition = null;

    /** The light_positions. */
    public ArrayList<ArrayList<Integer[]>> lightPositions = new ArrayList<ArrayList<Integer[]>>();

    /** The positions of woosh. First array is the order to activate them. Inner array is list of points */
    public ArrayList<ArrayList<Integer[]>> wooshPositions = new ArrayList<ArrayList<Integer[]>>();

    /** The water_positions. */
    public ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>();

    /**
     * Instantiates a new stargate shape layer.
     * 
     * @param layerLines
     *            the layer lines
     * @param height
     *            the height
     * @param width
     *            the width
     */
    public StargateShapeLayer(final String[] layerLines, final int height, final int width)
    {
        int numBlocks = 0;

        // 1. scan all lines for lines beginning with [  - that is the height of the gate
        for (int i = 0; i < layerLines.length; i++)
        {
            // final Pattern p = Pattern.compile("\\[(.+?)\\]");
            final Matcher m = Pattern.compile("\\[(.+?)\\]").matcher(layerLines[i]);
            int j = 0;
            while (m.find())
            {
                //final String block = m.group(1);
                final Integer[] point =
                {
                    0, (height - 1 - i), (width - 1 - j)
                };

                //final String[] modifiers = block.split(":");
                for (final String mod : m.group(1).split(":"))
                {
                    if (mod.equalsIgnoreCase("S"))
                    {
                        numBlocks++;
                        blockPositions.add(point);
                    }
                    else if (mod.equalsIgnoreCase("P"))
                    {
                        portalPositions.add(point);
                    }
                    else if (mod.equalsIgnoreCase("N") || mod.equalsIgnoreCase("E") || mod.equalsIgnoreCase("A") || mod.equalsIgnoreCase("D") || mod.equalsIgnoreCase("IA"))
                    {
                        final int[] pointI = new int[3];
                        for (int k = 0; k < 3; k++)
                        {
                            pointI[k] = point[k];
                        }

                        if (mod.equalsIgnoreCase("N"))
                        {
                            signPosition = pointI;
                        }
                        if (mod.equalsIgnoreCase("E"))
                        {
                            enterPosition = pointI;
                        }
                        if (mod.equalsIgnoreCase("A"))
                        {
                            activationPosition = pointI;
                        }
                        if (mod.equalsIgnoreCase("D"))
                        {
                            dialerPosition = pointI;
                        }
                        if (mod.equalsIgnoreCase("IA"))
                        {
                            irisActivationPosition = pointI;
                        }
                        if (mod.equalsIgnoreCase("RA"))
                        {
                            redstoneActivationPosition = pointI;
                        }
                        if (mod.equalsIgnoreCase("RD"))
                        {
                            redstoneDialerActivationPosition = pointI;
                        }
                    }
                    else if (mod.contains("L") || mod.contains("l"))
                    {
                        final String[] light_parts = mod.split("#");
                        final int light_iteration = Integer.parseInt(light_parts[1]);

                        while (lightPositions.size() <= light_iteration)
                        {
                            lightPositions.add(null);
                        }

                        if (lightPositions.get(light_iteration) == null)
                        {
                            final ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
                            lightPositions.set(light_iteration, new_it);
                        }

                        lightPositions.get(light_iteration).add(point);
                        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Position (Order:" + light_parts[1] + " Position:" + Arrays.toString(point) + ")");
                    }
                    else if (mod.contains("W") || mod.contains("w"))
                    {
                        final String[] w_parts = mod.split("#");
                        final int w_iteration = Integer.parseInt(w_parts[1]);

                        while (wooshPositions.size() <= w_iteration)
                        {
                            wooshPositions.add(null);
                        }

                        if (wooshPositions.get(w_iteration) == null)
                        {
                            final ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
                            wooshPositions.set(w_iteration, new_it);
                        }

                        wooshPositions.get(w_iteration).add(point);
                        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Woosh Position (Order:" + w_parts[1] + " Position:" + Arrays.toString(point) + ")");
                    }
                }
                j++;
            }
        }
        //TODO: debug printout for the materials the gate uses.
        //TODO: debug printout for the redstone_activated
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(signPosition) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(enterPosition) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Activation Position: \"" + Arrays.toString(activationPosition) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Iris Activation Position: \"" + Arrays.toString(irisActivationPosition) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Dialer Position: \"" + Arrays.toString(dialerPosition) + "\"");
        //WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString((int[][])this.waterPositions) + "\"");

        // WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + lightPositions.toString() + "\"");
        //WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString((int[][])this.stargatePositions) + "\"");
    }
}
