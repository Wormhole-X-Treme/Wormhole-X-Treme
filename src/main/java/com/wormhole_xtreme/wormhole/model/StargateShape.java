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

import org.bukkit.Material;

import com.wormhole_xtreme.wormhole.WormholeXTreme;

/**
 * The Class StargateShape.
 */
public class StargateShape
{

    /** The shape name. */
    private String shapeName = "Standard";

    /** The stargate_positions. */
    private int[][] shapeStructurePositions = {{0, 2, 0}, {0, 3, 0}, {0, 4, 0}, {0, 1, 1}, {0, 5, 1}, {0, 0, 2},
        {0, 6, 2}, {0, 6, 3}, {0, 0, 3}, {0, 0, 4}, {0, 6, 4}, {0, 5, 5}, {0, 1, 5}, {0, 2, 6}, {0, 3, 6}, {0, 4, 6}};

    /** The sign_position. */
    private int[] shapeSignPosition = {0, 3, 6};

    /** The enter_position. */
    private int[] shapeEnterPosition = {0, 0, 3};

    /** The light_positions. */
    private int[] shapeLightPositions = {3, 4, 11, 12};

    /** The water_positions. */
    private int[][] shapePortalPositions = {{0, 2, 1}, {0, 3, 1}, {0, 4, 1}, {0, 1, 2}, {0, 2, 2}, {0, 3, 2},
        {0, 4, 2}, {0, 5, 2}, {0, 1, 3}, {0, 2, 3}, {0, 3, 3}, {0, 4, 3}, {0, 5, 3}, {0, 1, 4}, {0, 2, 4}, {0, 3, 4},
        {0, 4, 4}, {0, 5, 4}, {0, 2, 5}, {0, 3, 5}, {0, 4, 5}};

    /** The reference_vector, this vector always points up for calculating cross product. */
    private int[] shapeReferenceVector = {0, 1, 0};

    /** [0] = Left - / Right + [1] = Up + / Down - [2] = Forward + / Backward -. */
    private int[] shapeToGateCorner = {1, -1, 4};

    /** The woosh_depth. */
    private int shapeWooshDepth = 0;

    /** The square of the woosh_depth, used in comparisions with squared distance. */
    private int shapeWooshDepthSquared;

    /** The portal material. */
    private Material shapePortalMaterial = Material.STATIONARY_WATER;

    /** The iris material. */
    private Material shapeIrisMaterial = Material.STONE;

    /** The stargate material. */
    private Material shapeStructureMaterial = Material.OBSIDIAN;

    /** The active material. */
    private Material shapeLightMaterial = Material.GLOWSTONE;

    /** The shape woosh ticks. */
    private int shapeWooshTicks = 3;

    /** The shape light ticks. */
    private int shapeLightTicks = 3;

    /**
     * Instantiates a new stargate shape.
     */
    public StargateShape()
    {
        setShapeWooshDepth(3);
        setShapeWooshDepthSquared(9);
    }

    /**
     * Instantiates a new stargate shape.
     * 
     * @param file_data
     *            the file_data
     */
    public StargateShape(final String[] file_data)
    {
        setShapeSignPosition(new int[]{});
        setShapeEnterPosition(new int[]{});

        final ArrayList<Integer[]> blockPositions = new ArrayList<Integer[]>();
        final ArrayList<Integer[]> portalPositions = new ArrayList<Integer[]>();
        final ArrayList<Integer> lightPositions = new ArrayList<Integer>();

        int numBlocks = 0;
        int curWooshDepth = 3;

        // 1. scan all lines for lines beginning with [  - that is the height of the gate
        int height = 0;
        int width = 0;
        for (int i = 0; i < file_data.length; i++)
        {
            final String line = file_data[i];

            if (line.contains("Name="))
            {
                shapeName = line.split("=")[1];
                WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Begin parsing shape: \"" + shapeName + "\"");
            }
            else if (line.equals("GateShape="))
            {
                int index = i + 1;
                while (file_data[index].startsWith("["))
                {
                    if (width <= 0)
                    {
                        final Pattern p = Pattern.compile("(\\[.*?\\])");
                        final Matcher m = p.matcher(file_data[index]);
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

                // Now parse each [X] and put into int array.
                index = i + 1;
                while (file_data[index].startsWith("["))
                {

                    final Pattern p = Pattern.compile("(\\[.*?\\])");
                    final Matcher m = p.matcher(file_data[index]);
                    int j = 0;
                    while (m.find())
                    {
                        final String block = m.group(0);
                        final Integer[] point = {0, (height - 1 - (index - i - 1)), (width - 1 - j)};
                        if (block.contains("O"))
                        {
                            numBlocks++;
                            blockPositions.add(point);
                        }
                        else if (block.contains("P"))
                        {
                            portalPositions.add(point);
                        }

                        if (block.contains("S") || block.contains("E"))
                        {
                            final int[] pointI = new int[3];
                            for (int k = 0; k < 3; k++)
                            {
                                pointI[k] = point[k];
                            }

                            if (block.contains("S"))
                            {
                                setShapeSignPosition(pointI);
                            }
                            if (block.contains("E"))
                            {
                                setShapeEnterPosition(pointI);
                            }
                        }

                        if (block.contains("L") && block.contains("O"))
                        {
                            lightPositions.add(numBlocks - 1);
                        }

                        j++;
                    }
                    index++;
                }
            }
            else if (line.contains("BUTTON_UP"))
            {
                getShapeToGateCorner()[1] = Integer.parseInt(line.split("=")[1]);
            }
            else if (line.contains("BUTTON_RIGHT"))
            {
                getShapeToGateCorner()[0] = Integer.parseInt(line.split("=")[1]);
            }
            else if (line.contains("BUTTON_AWAY"))
            {
                getShapeToGateCorner()[2] = Integer.parseInt(line.split("=")[1]);
            }
            else if (line.contains("WOOSH_DEPTH"))
            {
                curWooshDepth = Integer.parseInt(line.split("=")[1]);
            }
            else if (line.contains("PORTAL_MATERIAL"))
            {
                setShapePortalMaterial(Material.valueOf(line.split("=")[1]));
            }
            else if (line.contains("IRIS_MATERIAL"))
            {
                setShapeIrisMaterial(Material.valueOf(line.split("=")[1]));
            }
            else if (line.contains("STARGATE_MATERIAL"))
            {
                setShapeStructureMaterial(Material.valueOf(line.split("=")[1]));
            }
            else if (line.contains("ACTIVE_MATERIAL"))
            {
                setShapeLightMaterial(Material.valueOf(line.split("=")[1]));
            }
        }
        //TODO: debug printout for the materials the gate uses.
        //TODO: debug printout for the redstone_activated
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Sign Position: \"" + Arrays.toString(getShapeSignPosition()) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Enter Position: \"" + Arrays.toString(getShapeEnterPosition()) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Button Position [Left/Right,Up/Down,Forward/Back]: \"" + Arrays.toString(getShapeToGateCorner()) + "\"");

        final int[][] tempPortalPositions = new int[portalPositions.size()][3];
        for (int i = 0; i < portalPositions.size(); i++)
        {
            final int[] point = new int[3];
            for (int j = 0; j < 3; j++)
            {
                point[j] = portalPositions.get(i)[j];
            }
            tempPortalPositions[i] = point;
        }
        setShapePortalPositions(tempPortalPositions);
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Portal Positions: \"" + Arrays.deepToString(getShapePortalPositions()) + "\"");

        final int[] tempLightPositions = new int[lightPositions.size()];
        for (int i = 0; i < lightPositions.size(); i++)
        {
            tempLightPositions[i] = lightPositions.get(i);
        }
        setShapeLightPositions(tempLightPositions);
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Light Material Positions: \"" + Arrays.toString(getShapeLightPositions()) + "\"");

        final int[][] tempStructurePositions = new int[blockPositions.size()][3];
        for (int i = 0; i < blockPositions.size(); i++)
        {
            final int[] point = new int[3];
            for (int j = 0; j < 3; j++)
            {
                point[j] = blockPositions.get(i)[j];
            }
            tempStructurePositions[i] = point;
        }
        setShapeStructurePositions(tempStructurePositions);
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Stargate Material Positions: \"" + Arrays.deepToString(getShapeStructurePositions()) + "\"");
        WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Finished parsing shape: \"" + shapeName + "\"");

        setShapeWooshDepth(curWooshDepth);
        setShapeWooshDepthSquared(curWooshDepth * curWooshDepth);
    }

    /**
     * Gets the shape enter position.
     * 
     * @return the shape enter position
     */
    public int[] getShapeEnterPosition()
    {
        return shapeEnterPosition.clone();
    }

    /**
     * Gets the shape iris material.
     * 
     * @return the shape iris material
     */
    public Material getShapeIrisMaterial()
    {
        return shapeIrisMaterial;
    }

    /**
     * Gets the shape active material.
     * 
     * @return the shape active material
     */
    public Material getShapeLightMaterial()
    {
        return shapeLightMaterial;
    }

    /**
     * Gets the shape light positions.
     * 
     * @return the shape light positions
     */
    public int[] getShapeLightPositions()
    {
        return shapeLightPositions.clone();
    }

    /**
     * Gets the shape light ticks.
     * 
     * @return the shape light ticks
     */
    public int getShapeLightTicks()
    {
        return shapeLightTicks;
    }

    /**
     * Gets the shape name.
     * 
     * @return the shape name
     */
    public String getShapeName()
    {
        return shapeName;
    }

    /**
     * Gets the shape portal material.
     * 
     * @return the shape portal material
     */
    public Material getShapePortalMaterial()
    {
        return shapePortalMaterial;
    }

    /**
     * Gets the shape water positions.
     * 
     * @return the shape water positions
     */
    public int[][] getShapePortalPositions()
    {
        return shapePortalPositions.clone();
    }

    /**
     * Gets the shape reference vector.
     * 
     * @return the shape reference vector
     */
    public int[] getShapeReferenceVector()
    {
        return shapeReferenceVector.clone();
    }

    /**
     * Gets the shape sign position.
     * 
     * @return the shape sign position
     */
    public int[] getShapeSignPosition()
    {
        return shapeSignPosition.clone();
    }

    /**
     * Gets the shape structure material.
     * 
     * @return the shape structure material
     */
    public Material getShapeStructureMaterial()
    {
        return shapeStructureMaterial;
    }

    /**
     * Gets the shape structure positions.
     * 
     * @return the shape structure positions
     */
    public int[][] getShapeStructurePositions()
    {
        return shapeStructurePositions.clone();
    }

    /**
     * Gets the shape to gate corner.
     * 
     * @return the shape to gate corner
     */
    public int[] getShapeToGateCorner()
    {
        return shapeToGateCorner.clone();
    }

    /**
     * Gets the shape woosh depth.
     * 
     * @return the shape woosh depth
     */
    public int getShapeWooshDepth()
    {
        return shapeWooshDepth;
    }

    /**
     * Gets the shape woosh depth squared.
     * 
     * @return the shape woosh depth squared
     */
    public int getShapeWooshDepthSquared()
    {
        return shapeWooshDepthSquared;
    }

    /**
     * Gets the shape woosh ticks.
     * 
     * @return the shape woosh ticks
     */
    public int getShapeWooshTicks()
    {
        return shapeWooshTicks;
    }

    /**
     * Sets the shape enter position.
     * 
     * @param shapeEnterPosition
     *            the new shape enter position
     */
    public void setShapeEnterPosition(final int[] shapeEnterPosition)
    {
        this.shapeEnterPosition = shapeEnterPosition.clone();
    }

    /**
     * Sets the shape iris material.
     * 
     * @param shapeIrisMaterial
     *            the new shape iris material
     */
    public void setShapeIrisMaterial(final Material shapeIrisMaterial)
    {
        this.shapeIrisMaterial = shapeIrisMaterial;
    }

    /**
     * Sets the shape active material.
     * 
     * @param shapeLightMaterial
     *            the new shape light material
     */
    public void setShapeLightMaterial(final Material shapeLightMaterial)
    {
        this.shapeLightMaterial = shapeLightMaterial;
    }

    /**
     * Sets the shape light positions.
     * 
     * @param shapeLightPositions
     *            the new shape light positions
     */
    public void setShapeLightPositions(final int[] shapeLightPositions)
    {
        this.shapeLightPositions = shapeLightPositions.clone();
    }

    /**
     * Sets the shape light ticks.
     * 
     * @param shapeLightTicks
     *            the new shape light ticks
     */
    public void setShapeLightTicks(final int shapeLightTicks)
    {
        this.shapeLightTicks = shapeLightTicks;
    }

    /**
     * Sets the shape name.
     * 
     * @param shapeName
     *            the new shape name
     */
    public void setShapeName(final String shapeName)
    {
        this.shapeName = shapeName;
    }

    /**
     * Sets the shape portal material.
     * 
     * @param shapePortalMaterial
     *            the new shape portal material
     */
    public void setShapePortalMaterial(final Material shapePortalMaterial)
    {
        this.shapePortalMaterial = shapePortalMaterial;
    }

    /**
     * Sets the shape water positions.
     * 
     * @param shapePortalPositions
     *            the new shape portal positions
     */
    public void setShapePortalPositions(final int[][] shapePortalPositions)
    {
        this.shapePortalPositions = shapePortalPositions.clone();
    }

    /**
     * Sets the shape reference vector.
     * 
     * @param shapeReferenceVector
     *            the new shape reference vector
     */
    public void setShapeReferenceVector(final int[] shapeReferenceVector)
    {
        this.shapeReferenceVector = shapeReferenceVector.clone();
    }

    /**
     * Sets the shape sign position.
     * 
     * @param shapeSignPosition
     *            the new shape sign position
     */
    public void setShapeSignPosition(final int[] shapeSignPosition)
    {
        this.shapeSignPosition = shapeSignPosition.clone();
    }

    /**
     * Sets the shape structure material.
     * 
     * @param shapeStructureMaterial
     *            the new shape structure material
     */
    public void setShapeStructureMaterial(final Material shapeStructureMaterial)
    {
        this.shapeStructureMaterial = shapeStructureMaterial;
    }

    /**
     * Sets the shape structure positions.
     * 
     * @param shapeStructurePositions
     *            the new shape structure positions
     */
    public void setShapeStructurePositions(final int[][] shapeStructurePositions)
    {
        this.shapeStructurePositions = shapeStructurePositions.clone();
    }

    /**
     * Sets the shape to gate corner.
     * 
     * @param shapeToGateCorner
     *            the new shape to gate corner
     */
    public void setShapeToGateCorner(final int[] shapeToGateCorner)
    {
        this.shapeToGateCorner = shapeToGateCorner.clone();
    }

    /**
     * Sets the shape woosh depth.
     * 
     * @param shapeWooshDepth
     *            the new shape woosh depth
     */
    public void setShapeWooshDepth(final int shapeWooshDepth)
    {
        this.shapeWooshDepth = shapeWooshDepth;
    }

    /**
     * Sets the shape woosh depth squared.
     * 
     * @param shapeWooshDepthSquared
     *            the new shape woosh depth squared
     */
    public void setShapeWooshDepthSquared(final int shapeWooshDepthSquared)
    {
        this.shapeWooshDepthSquared = shapeWooshDepthSquared;
    }

    /**
     * Sets the shape woosh ticks.
     * 
     * @param shapeWooshTicks
     *            the new shape woosh ticks
     */
    public void setShapeWooshTicks(final int shapeWooshTicks)
    {
        this.shapeWooshTicks = shapeWooshTicks;
    }
}
