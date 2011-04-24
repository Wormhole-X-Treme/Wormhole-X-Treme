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
    public int[]  redstoneActivationPosition = null;
    /** Position of point that allows gate to cycle sign targets via redstone. */
    public int[]  redstoneDialerActivationPosition = null;

    /** The light_positions. */
    public ArrayList<ArrayList<Integer[]>> lightPositions = new ArrayList<ArrayList<Integer[]>>();

    /** The positions of woosh. First array is the order to activate them. Inner array is list of points */
    public ArrayList<ArrayList<Integer[]>> wooshPositions = new ArrayList<ArrayList<Integer[]>>();

    /** The water_positions. */
    public ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>();

    /**
     * Instantiates a new stargate shape layer.
     *
     * @param layerLines the layer lines
     * @param height the height
     * @param width the width
     */
    public StargateShapeLayer(String[] layerLines, int height, int width)
    {
        int numBlocks = 0;

        // 1. scan all lines for lines beginning with [  - that is the height of the gate
        for ( int i = 0; i < layerLines.length; i++ )
        {
            Pattern p = Pattern.compile("\\[(.+?)\\]");
            Matcher m = p.matcher(layerLines[i]);
            int j = 0;
            while ( m.find() )
            {
                String block = m.group(1);
                Integer[] point = { 0, (height - 1 - i), (width - 1 - j) };

                String[] modifiers = block.split(":");
                for ( String mod : modifiers )
                {
                    if ( mod.equals("S") )
                    {
                        numBlocks++;
                        this.blockPositions.add(point);
                    }
                    else if ( mod.equals("P") )
                    {
                        this.portalPositions.add(point);
                    }
                    else if ( mod.equals("N") || mod.equals("E") || mod.equals("A") || mod.equals("D") || mod.equals("IA") )
                    {
                        int[] pointI = new int[3];
                        for (int k = 0; k < 3; k++ )
                            pointI[k] = point[k];

                        if ( mod.equals("N") )
                        {
                            this.signPosition = pointI;
                        }
                        if ( mod.equals("E") )
                        {
                            this.enterPosition = pointI;
                        }
                        if ( mod.equals("A") )
                        {
                            this.activationPosition = pointI;
                        }
                        if ( mod.equals("D") )
                        {
                            this.dialerPosition = pointI;
                        }
                        if ( mod.equals("IA") )
                        {
                            this.irisActivationPosition = pointI;
                        }
                        if ( mod.equals("RA") )
                        {
                            this.redstoneActivationPosition = pointI;
                        }
                        if ( mod.equals("RD") )
                        {
                            this.redstoneDialerActivationPosition = pointI;
                        }
                    }
                    else if ( mod.contains("L") )
                    {
                        String[] light_parts = mod.split("#");
                        int light_iteration = Integer.parseInt(light_parts[1]);

                        while( this.lightPositions.size() <= light_iteration)
                            this.lightPositions.add(null);

                        if ( this.lightPositions.get(light_iteration) == null )
                        {
                            ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
                            this.lightPositions.set(light_iteration, new_it);
                        }
                        
                        this.lightPositions.get(light_iteration).add(point);
                        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Position (Order:" + light_parts[1] + " Position:" + Arrays.toString(point) + ")");
                    }
                    else if ( mod.contains("W") )
                    {
                        String[] w_parts = mod.split("#");
                        int w_iteration = Integer.parseInt(w_parts[1]);

                        while( this.wooshPositions.size() <= w_iteration)
                            this.wooshPositions.add(null);

                        if ( this.wooshPositions.get(w_iteration) == null )
                        {
                            ArrayList<Integer[]> new_it = new ArrayList<Integer[]>();
                            this.wooshPositions.set(w_iteration, new_it);
                        }

                        this.wooshPositions.get(w_iteration).add(point);
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
