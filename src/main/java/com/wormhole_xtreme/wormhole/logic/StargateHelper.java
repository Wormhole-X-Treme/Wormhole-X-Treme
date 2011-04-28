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
package com.wormhole_xtreme.wormhole.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.logic.StargateUpdateRunnable.ActionToTake;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.Stargate3DShape;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.model.StargateNetwork;
import com.wormhole_xtreme.wormhole.model.StargateShape;
import com.wormhole_xtreme.wormhole.model.StargateShapeLayer;
import com.wormhole_xtreme.wormhole.utils.DataUtils;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;

/**
 * The Class StargateHelper.
 */
public class StargateHelper
{

    /** The Constant shapes. */
    private static final ConcurrentHashMap<String, StargateShape> shapes = new ConcurrentHashMap<String, StargateShape>();

    /** The Constant StargateSaveVersion. */
    private static final byte StargateSaveVersion = 7;

    /** The Empty block. */
    private final static byte[] emptyBlock = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * * This method takes in a button/lever and a facing and returns a completed stargate.
     * If the gate does not match the format for a gate it returns null.
     * 
     * @param buttonBlock
     *            the button_block
     * @param facing
     *            the facing
     * @return s If successful returns completed gate, null otherwise
     */
    public static Stargate checkStargate(final Block buttonBlock, final BlockFace facing)
    {
        final Set<String> keys = shapes.keySet();
        Stargate s = null;

        for (final String key : keys)
        {
            final StargateShape shape = shapes.get(key);
            if (shape != null)
            {
                if (shape instanceof Stargate3DShape)
                {
                    s = checkStargate3D(buttonBlock, facing, (Stargate3DShape) shape, false);
                }
                else
                {
                    s = checkStargate(buttonBlock, facing, shape, false);
                }
            }
            if (s != null)
            {
                return s;
            }
        }

        return s;
    }

    /**
     * This method takes in the DHD pressed and a shape. This method will create a stargate of the specified shape and
     * return it.
     * 
     * @param buttonBlock
     *            the button_block
     * @param facing
     *            the facing
     * @param shape
     *            the shape
     * @return checkStargate(button_block, facing, shape, true)
     */
    public static Stargate checkStargate(final Block buttonBlock, final BlockFace facing, final StargateShape shape)
    {
        if (shape instanceof Stargate3DShape)
        {
            return checkStargate3D(buttonBlock, facing, (Stargate3DShape) shape, true);
        }
        else
        {
            return checkStargate(buttonBlock, facing, shape, true);
        }
    }

    /**
     * Check stargate.
     * 
     * @param buttonBlock
     *            the button_block
     * @param facing
     *            the facing
     * @param shape
     *            the shape
     * @param create
     *            the create
     * @return the stargate
     */
    private static Stargate checkStargate(final Block buttonBlock, final BlockFace facing, final StargateShape shape, final boolean create)
    {
        final BlockFace opposite = WorldUtils.getInverseDirection(facing);
        final Block holdingBlock = buttonBlock.getFace(opposite);

        if (isStargateMaterial(holdingBlock, shape))
        {
            //System.out.println("");
            // Probably a stargate, lets start checking!
            final Stargate tempGate = new Stargate();
            tempGate.setGateWorld(buttonBlock.getWorld());
            tempGate.setGateName("");
            tempGate.setGateActivationBlock(buttonBlock);
            tempGate.setGateFacing(facing);
            tempGate.getGateStructureBlocks().add(buttonBlock.getLocation());
            tempGate.setGateShape(shape);
            if ( !isStargateMaterial(holdingBlock.getRelative(BlockFace.DOWN), tempGate.getGateShape()))
            {
                return null;
            }

            final Block possibleSignHolder = holdingBlock.getRelative(WorldUtils.getPerpendicularRightDirection(opposite));
            if (isStargateMaterial(possibleSignHolder, tempGate.getGateShape()))
            {
                // This might be a public gate with activation method of sign instead of name.
                final Block signBlock = possibleSignHolder.getRelative(tempGate.getGateFacing());
                // If the sign block is messed up just return the gate.
                if ( !tryCreateGateSign(signBlock, tempGate) && tempGate.isGateSignPowered())
                {
                    return tempGate;
                }
            }

            final int[] facingVector = {0, 0, 0};

            final World w = buttonBlock.getWorld();
            // Now we start calculaing the values for the blocks that need to be the stargate material.

            if (facing == BlockFace.NORTH)
            {
                facingVector[0] = 1;
            }
            else if (facing == BlockFace.SOUTH)
            {
                facingVector[0] = -1;
            }
            else if (facing == BlockFace.EAST)
            {
                facingVector[2] = 1;
            }
            else if (facing == BlockFace.WEST)
            {
                facingVector[2] = -1;
            }
            else if (facing == BlockFace.UP)
            {
                facingVector[1] = -1;
            }
            else if (facing == BlockFace.DOWN)
            {
                facingVector[1] = 1;
            }

            final int[] directionVector = {0, 0, 0};
            final int[] startingPosition = {0, 0, 0};

            // Calculate the cross product
            directionVector[0] = facingVector[1] * shape.getShapeReferenceVector()[2] - facingVector[2] * shape.getShapeReferenceVector()[1];
            directionVector[1] = facingVector[2] * shape.getShapeReferenceVector()[0] - facingVector[0] * shape.getShapeReferenceVector()[2];
            directionVector[2] = facingVector[0] * shape.getShapeReferenceVector()[1] - facingVector[1] * shape.getShapeReferenceVector()[0];

            // This is the 0,0,0 the block at the ground against the far side of the stargate
            startingPosition[0] = buttonBlock.getX() + facingVector[0] * shape.getShapeToGateCorner()[2] + directionVector[0] * shape.getShapeToGateCorner()[0];
            startingPosition[1] = buttonBlock.getY() + shape.getShapeToGateCorner()[1];
            startingPosition[2] = buttonBlock.getZ() + facingVector[2] * shape.getShapeToGateCorner()[2] + directionVector[2] * shape.getShapeToGateCorner()[0];

            for (int i = 0; i < shape.getShapeStructurePositions().length; i++)
            {
                final int[] bVect = shape.getShapeStructurePositions()[i];

                final int[] blockLocation = {bVect[2] * directionVector[0] * -1, bVect[1],
                    bVect[2] * directionVector[2] * -1};

                final Block maybeBlock = w.getBlockAt(blockLocation[0] + startingPosition[0], blockLocation[1] + startingPosition[1], blockLocation[2] + startingPosition[2]);
                if (create)
                {
                    maybeBlock.setType(tempGate.getGateShape().getShapeStructureMaterial());
                }

                if (isStargateMaterial(maybeBlock, tempGate.getGateShape()))
                {
                    tempGate.getGateStructureBlocks().add(maybeBlock.getLocation());
                    for (final int lightPosition : shape.getShapeLightPositions())
                    {
                        if (lightPosition == i)
                        {
                            while (tempGate.getGateLightBlocks().size() < 2)
                            {
                                tempGate.getGateLightBlocks().add(null);
                            }
                            // In 2d gate all lights go in first iteration!
                            tempGate.getGateLightBlocks().get(1).add(maybeBlock.getLocation());
                        }
                    }
                }
                else
                {
                    if (tempGate.getGateNetwork() != null)
                    {
                        tempGate.getGateNetwork().getNetworkGateList().remove(tempGate);
                        if (tempGate.isGateSignPowered())
                        {
                            tempGate.getGateNetwork().getNetworkSignGateList().remove(tempGate);
                        }
                    }
                    return null;
                }
            }

            // Set the name sign location.
            if (shape.getShapeSignPosition().length > 0)
            {
                final int[] signLocationArray = {shape.getShapeSignPosition()[2] * directionVector[0] * -1,
                    shape.getShapeSignPosition()[1], shape.getShapeSignPosition()[2] * directionVector[2] * -1};
                final Block nameBlock = w.getBlockAt(signLocationArray[0] + startingPosition[0], signLocationArray[1] + startingPosition[1], signLocationArray[2] + startingPosition[2]);
                tempGate.setGateNameBlockHolder(nameBlock);
            }
            // Now set teleport in location
            final int[] teleportLocArray = {shape.getShapeEnterPosition()[2] * directionVector[0] * -1,
                shape.getShapeEnterPosition()[1], shape.getShapeEnterPosition()[2] * directionVector[2] * -1};
            final Block teleBlock = w.getBlockAt(teleportLocArray[0] + startingPosition[0], teleportLocArray[1] + startingPosition[1], teleportLocArray[2] + startingPosition[2]);
            // First go forward one
            Block bLoc = teleBlock.getRelative(facing);
            // Now go up until we hit air or water.
            while ((bLoc.getType() != Material.AIR) && (bLoc.getType() != Material.WATER))
            {
                bLoc = bLoc.getRelative(BlockFace.UP);
            }
            final Location teleLoc = bLoc.getLocation();
            // Make sure the guy faces the right way out of the portal.
            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(facing));
            teleLoc.setPitch(0);
            // Put him in the middle of the block instead of a corner.
            // Players are 1.65 blocks tall, so we go up .66 more up :-p
            teleLoc.setX(teleLoc.getX() + 0.5);
            teleLoc.setY(teleLoc.getY() + 0.66);
            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGatePlayerTeleportLocation(teleLoc);

            for (final int[] bVect : shape.getShapeWaterPositions())
            {
                final int[] blockLocation = {bVect[2] * directionVector[0] * -1, bVect[1],
                    bVect[2] * directionVector[2] * -1};

                final Block maybeBlock = w.getBlockAt(blockLocation[0] + startingPosition[0], blockLocation[1] + startingPosition[1], blockLocation[2] + startingPosition[2]);
                if (maybeBlock.getType() == Material.AIR)
                {
                    tempGate.getGatePortalBlocks().add(maybeBlock.getLocation());
                }
                else
                {
                    if (tempGate.getGateNetwork() != null)
                    {
                        tempGate.getGateNetwork().getNetworkGateList().remove(tempGate);
                    }

                    return null;
                }
            }

            setupSignGateNetwork(tempGate);
            return tempGate;
        }

        return null;
    }

    /**
     * Check stargate3 d.
     * 
     * @param buttonBlock
     *            the button block
     * @param facing
     *            the facing
     * @param shape
     *            the shape
     * @param create
     *            the create
     * @return the stargate
     */
    private static Stargate checkStargate3D(final Block buttonBlock, final BlockFace facing, final Stargate3DShape shape, final boolean create)
    {
        final Stargate s = new Stargate();
        s.setGateWorld(buttonBlock.getWorld());
        // No need to find it, we already have it!
        s.setGateActivationBlock(buttonBlock);
        s.getGateStructureBlocks().add(s.getGateActivationBlock().getLocation());
        s.setGateShape(shape);
        s.setGateFacing(facing);

        final BlockFace opposite = WorldUtils.getInverseDirection(facing);
        final Block activationBlock = buttonBlock.getFace(opposite);
        final StargateShapeLayer act_layer = shape.getShapeLayers().get(shape.getShapeActivationLayer());

        final int[] facingVector = {0, 0, 0};

        // Now we start calculaing the values for the blocks that need to be the stargate material.

        if (facing == BlockFace.NORTH)
        {
            facingVector[0] = -1;
        }
        else if (facing == BlockFace.SOUTH)
        {
            facingVector[0] = 1;
        }
        else if (facing == BlockFace.EAST)
        {
            facingVector[2] = -1;
        }
        else if (facing == BlockFace.WEST)
        {
            facingVector[2] = 1;
        }
        else if (facing == BlockFace.UP)
        {
            facingVector[1] = 1;
        }
        else if (facing == BlockFace.DOWN)
        {
            facingVector[1] = -1;
        }

        final int[] directionVector = {0, 0, 0};
        final int[] startingPosition = {0, 0, 0};

        // Calculate the cross product
        directionVector[0] = facingVector[1] * shape.getShapeReferenceVector()[2] - facingVector[2] * shape.getShapeReferenceVector()[1];
        directionVector[1] = facingVector[2] * shape.getShapeReferenceVector()[0] - facingVector[0] * shape.getShapeReferenceVector()[2];
        directionVector[2] = facingVector[0] * shape.getShapeReferenceVector()[1] - facingVector[1] * shape.getShapeReferenceVector()[0];

        // This is the 0,0,0 the block at the ground on the activation layer
        startingPosition[0] = activationBlock.getX() - directionVector[0] * act_layer.getLayerActivationPosition()[2];
        startingPosition[1] = activationBlock.getY() - act_layer.getLayerActivationPosition()[1];
        startingPosition[2] = activationBlock.getZ() - directionVector[2] * act_layer.getLayerActivationPosition()[2];

        // 2. Add/remove from the direction component to yield each layers 0,0,0
        for (int i = 0; i <= 10; i++)
        {
            if ((shape.getShapeLayers().size() > i) && (shape.getShapeLayers().get(i) != null))
            {
                final int layerOffset = shape.getShapeActivationLayer() - i;
                final int[] layerStarter = {startingPosition[0] - facingVector[0] * layerOffset, startingPosition[1],
                    startingPosition[2] - facingVector[2] * layerOffset};
                if ( !checkStargateLayer(shape.getShapeLayers().get(i), layerStarter, directionVector, s, create))
                {
                    if (s.getGateNetwork() != null)
                    {
                        s.getGateNetwork().getNetworkGateList().remove(s);
                        if (s.isGateSignPowered())
                        {
                            s.getGateNetwork().getNetworkSignGateList().remove(s);
                        }
                    }
                    return null;
                }
            }
        }
        // Set the name sign location.
        if (shape.getShapeSignPosition().length > 0)
        {
            final int[] signLocationArray = {shape.getShapeSignPosition()[2] * directionVector[0] * -1,
                shape.getShapeSignPosition()[1], shape.getShapeSignPosition()[2] * directionVector[2] * -1};
            final Block nameBlock = s.getGateWorld().getBlockAt(signLocationArray[0] + startingPosition[0], signLocationArray[1] + startingPosition[1], signLocationArray[2] + startingPosition[2]);
            s.setGateNameBlockHolder(nameBlock);
        }
        setupSignGateNetwork(s);
        return s;
    }

    /**
     * Check stargate layer.
     * 
     * @param layer
     *            the layer
     * @param lowerCorner
     *            the lower corner
     * @param directionVector
     *            the direction vector
     * @param tempGate
     *            the temp gate
     * @param create
     *            the create
     * @return true, if successful
     */
    private static boolean checkStargateLayer(final StargateShapeLayer layer, final int[] lowerCorner, final int[] directionVector, final Stargate tempGate, final boolean create)
    {
        final World w = tempGate.getGateWorld();
        // First check all the block positions!
        for (int i = 0; i < layer.getLayerBlockPositions().size(); i++)
        {
            final Block maybeBlock = getBlockFromVector(layer.getLayerBlockPositions().get(i), directionVector, lowerCorner, w);

            if (create)
            {
                maybeBlock.setType(tempGate.getGateShape().getShapeStructureMaterial());
            }

            if (isStargateMaterial(maybeBlock, tempGate.getGateShape()))
            {
                tempGate.getGateStructureBlocks().add(maybeBlock.getLocation());
            }
            else
            {
                return false;
            }
        }

        // Next check for air in the portal positions
        for (int i = 0; i < layer.getLayerPortalPositions().size(); i++)
        {
            final Block maybeBlock = getBlockFromVector(layer.getLayerPortalPositions().get(i), directionVector, lowerCorner, w);

            if (create)
            {
                maybeBlock.setType(Material.AIR);
            }

            if (maybeBlock.getType() == Material.AIR)
            {
                tempGate.getGatePortalBlocks().add(maybeBlock.getLocation());
            }
            else
            {
                return false;
            }
        }

        // Now set player teleport in location
        if (layer.getLayerPlayerExitPosition().length > 0)
        {
            final Block teleBlock = StargateHelper.getBlockFromVector(layer.getLayerPlayerExitPosition(), directionVector, lowerCorner, w);

            // First go forward one
            Block bLoc = teleBlock.getRelative(tempGate.getGateFacing());
            // Now go up until we hit air or water.
            while ((bLoc.getType() != Material.AIR) && (bLoc.getType() != Material.WATER))
            {
                bLoc = bLoc.getRelative(BlockFace.UP);
            }
            final Location teleLoc = bLoc.getLocation();
            // Make sure the guy faces the right way out of the portal.
            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(tempGate.getGateFacing()));
            teleLoc.setPitch(0);
            // Put him in the middle of the block instead of a corner.
            // Players are 1.65 blocks tall, so we go up .66 more up :-p
            teleLoc.setX(teleLoc.getX() + 0.5);
            teleLoc.setY(teleLoc.getY() + 0.66);
            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGatePlayerTeleportLocation(teleLoc);
        }

        // Now set minecart teleport in location
        if (layer.getLayerMinecartExitPosition().length > 0)
        {
            final Block teleBlock = StargateHelper.getBlockFromVector(layer.getLayerMinecartExitPosition(), directionVector, lowerCorner, w);

            // First go forward one
            Block bLoc = teleBlock.getRelative(tempGate.getGateFacing());
            // Now go up until we hit air or water.
            while ((bLoc.getType() != Material.AIR) && (bLoc.getType() != Material.WATER))
            {
                bLoc = bLoc.getRelative(BlockFace.UP);
            }
            final Location teleLoc = bLoc.getLocation();
            // Make sure the guy faces the right way out of the portal.
            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(tempGate.getGateFacing()));
            teleLoc.setPitch(0);
            // Put him in the middle of the block instead of a corner.
            // Players are 1.65 blocks tall, so we go up .66 more up :-p
            teleLoc.setX(teleLoc.getX() + 0.5);
            teleLoc.setY(teleLoc.getY() + 0.66);
            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGateMinecartTeleportLocation(teleLoc);
        }

        for (int i = 0; i < layer.getLayerWooshPositions().size(); i++)
        {
            if (tempGate.getGateWooshBlocks().size() < i + 1)
            {
                tempGate.getGateWooshBlocks().add(new ArrayList<Location>());
            }
            if (layer.getLayerWooshPositions().get(i) != null)
            {
                for (final Integer[] position : layer.getLayerWooshPositions().get(i))
                {
                    final Block wooshBlock = StargateHelper.getBlockFromVector(position, directionVector, lowerCorner, w);
                    tempGate.getGateWooshBlocks().get(i).add(wooshBlock.getLocation());
                }
            }
        }

        for (int i = 0; i < layer.getLayerLightPositions().size(); i++)
        {
            if (tempGate.getGateLightBlocks().size() < i + 1)
            {
                tempGate.getGateLightBlocks().add(new ArrayList<Location>());
            }
            if (layer.getLayerLightPositions().get(i) != null)
            {
                for (final Integer[] position : layer.getLayerLightPositions().get(i))
                {
                    final Block lightBlock = StargateHelper.getBlockFromVector(position, directionVector, lowerCorner, w);
                    tempGate.getGateLightBlocks().get(i).add(lightBlock.getLocation());
                }
            }
        }

        // Set the dialer sign up all proper like
        if (layer.getLayerDialerPosition().length > 0)
        {
            final Block signBlockHolder = StargateHelper.getBlockFromVector(layer.getLayerDialerPosition(), directionVector, lowerCorner, w);
            final Block signBlock = signBlockHolder.getFace(tempGate.getGateFacing());

            // If somethign went wrong but the gate is sign powered, we need to error out.
            if ( !tryCreateGateSign(signBlock, tempGate) && tempGate.isGateSignPowered())
            {
                return false;
            }
            else if (tempGate.isGateSignPowered())
            {
                // is sign powered and we are good.
                tempGate.getGateStructureBlocks().add(signBlock.getLocation());
            }
            // else it isn't sign powered
        }
        if (layer.getLayerSignPosition().length > 0)
        {
            tempGate.setGateNameBlockHolder(StargateHelper.getBlockFromVector(layer.getLayerSignPosition(), directionVector, lowerCorner, w));
        }
        if (layer.getLayerRedstoneActivationPosition().length > 0)
        {
            tempGate.setGateRedstoneActivationBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneActivationPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneDialerActivationPosition().length > 0)
        {
            tempGate.setGateRedstoneDialChangeBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneDialerActivationPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerIrisActivationPosition().length > 0)
        {
            tempGate.setGateIrisActivationBlock(StargateHelper.getBlockFromVector(layer.getLayerIrisActivationPosition(), directionVector, lowerCorner, w).getFace(tempGate.getGateFacing()));
            tempGate.getGateStructureBlocks().add(tempGate.getGateIrisActivationBlock().getLocation());
        }

        return true;
    }

    /**
     * Gets the block from vector.
     * 
     * @param bVect
     *            the b vect
     * @param directionVector
     *            the direction vector
     * @param lowerCorner
     *            the lower corner
     * @param w
     *            the w
     * @return the block from vector
     */
    private static Block getBlockFromVector(final int[] bVect, final int[] directionVector, final int[] lowerCorner, final World w)
    {

        final int[] blockLocation = {bVect[2] * directionVector[0], bVect[1], bVect[2] * directionVector[2]};

        return w.getBlockAt(blockLocation[0] + lowerCorner[0], blockLocation[1] + lowerCorner[1], blockLocation[2] + lowerCorner[2]);
    }

    /**
     * Gets the block from vector.
     * 
     * @param bVect
     *            the b vect
     * @param directionVector
     *            the direction vector
     * @param lowerCorner
     *            the lower corner
     * @param w
     *            the w
     * @return the block from vector
     */
    private static Block getBlockFromVector(final Integer[] bVect, final int[] directionVector, final int[] lowerCorner, final World w)
    {

        final int[] blockLocation = {bVect[2] * directionVector[0], bVect[1], bVect[2] * directionVector[2]};

        return w.getBlockAt(blockLocation[0] + lowerCorner[0], blockLocation[1] + lowerCorner[1], blockLocation[2] + lowerCorner[2]);
    }

    /**
     * Returns a shape based on name.
     * 
     * @param name
     *            Name of stargate shape
     * @return The shape associated with that name. Null if not in list.
     */
    public static StargateShape getShape(final String name)
    {
        if (shapes.containsKey(name))
        {
            return shapes.get(name);
        }

        return null;
    }

    /**
     * Checks if is stargate material.
     * 
     * @param b
     *            the b
     * @param s
     *            the s
     * @return true, if is stargate material
     */
    private static boolean isStargateMaterial(final Block b, final StargateShape s)
    {
        return b.getType() == s.getShapeStructureMaterial();
    }

    /**
     * Load shapes.
     */
    public static void loadShapes()
    {
        final File directory = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "GateShapes" + File.separator);
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
            BufferedReader br = null;
            BufferedWriter bw = null;
            try
            {
                final File standardShapeFile = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "GateShapes" + File.separator + "Standard.shape");
                final InputStream is = WormholeXTreme.class.getResourceAsStream("/GateShapes/Standard.shape");
                br = new BufferedReader(new InputStreamReader(is));
                bw = new BufferedWriter(new FileWriter(standardShapeFile));

                for (String s = ""; (s = br.readLine()) != null;)
                {
                    bw.write(s);
                    bw.write("\n");
                }

                br.close();
                bw.close();
                is.close();
            }
            catch (final IOException e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to create files: " + e.getMessage());
            }
            catch (final NullPointerException e)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to create files: " + e.getMessage());
            }
            finally
            {
                try
                {
                    if (br != null) {
                        br.close();
                    }
                }
                catch (final IOException e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
                }
                try
                {
                    if (bw != null) {
                        bw.close();
                    }
                }
                catch (final IOException e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
                }
            }
        }

        final File[] shapeFiles = directory.listFiles();
        for (final File fi : shapeFiles)
        {
            if (fi.getName().contains(".shape"))
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Loading shape file: \"" + fi.getName() + "\"");
                BufferedReader bufferedReader = null;
                try
                {
                    final ArrayList<String> fileLines = new ArrayList<String>();
                    bufferedReader = new BufferedReader(new FileReader(fi));
                    for (String s = ""; (s = bufferedReader.readLine()) != null;)
                    {
                        fileLines.add(s);
                    }
                    bufferedReader.close();

                    final StargateShape shape = StargateShapeFactory.createShapeFromFile(fileLines.toArray(new String[fileLines.size()]));

                    if (shapes.containsKey(shape.getShapeName()))
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Shape File: " + fi.getName() + " contains shape name: " + shape.getShapeName() + " which already exists. This shape will be unavailable.");
                    }
                    else
                    {
                        shapes.put(shape.getShapeName(), shape);
                    }
                }
                catch (final FileNotFoundException e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
                }
                catch (final IOException e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to read shape file: " + e.getMessage());
                }
                finally
                {
                    try
                    {
                        if (bufferedReader != null)
                        {
                            bufferedReader.close();
                        }
                    }
                    catch (final IOException e)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
                    }
                }
                WormholeXTreme.getThisPlugin().prettyLog(Level.CONFIG, false, "Completed loading shape file: \"" + fi.getName() + "\"");
            }
        }

        if (shapes.size() == 0)
        {
            shapes.put("Standard", new StargateShape());
        }
    }

    /**
     * Parses the versioned data.
     * 
     * @param gate_data
     *            the gate_data
     * @param w
     *            the w
     * @param name
     *            the name
     * @param network
     *            the network
     * @return the stargate
     */
    public static Stargate parseVersionedData(final byte[] gate_data, final World w, final String name, final StargateNetwork network)
    {
        final Stargate s = new Stargate();
        s.setGateName(name);
        s.setGateNetwork(network);
        final ByteBuffer byteBuff = ByteBuffer.wrap(gate_data);

        // First get version byte
        s.setLoadedVersion(byteBuff.get());
        s.setGateWorld(w);

        if (s.getLoadedVersion() == 3)
        {
            final byte[] locArray = new byte[32];
            final byte[] blocArray = new byte[12];
            // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
            //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

            byteBuff.get(blocArray);
            s.setGateActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisActivationBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getInt());
            if (s.isGateSignPowered())
            {
                s.setGateTeleportSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateTeleportSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateTeleportSign((Sign) s.getGateTeleportSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateTeleportSignBlock());
                    }
                    catch (final Exception e)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    }
                }
            }

            s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateTempTargetId(byteBuff.getInt());

            final int facingSize = byteBuff.getInt();
            final byte[] strBytes = new byte[facingSize];
            byteBuff.get(strBytes);
            final String faceStr = new String(strBytes);
            s.setGateFacing(BlockFace.valueOf(faceStr));

            s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
            s.getGatePlayerTeleportLocation().setPitch(0);

            final int idcLen = byteBuff.getInt();
            final byte[] idcBytes = new byte[idcLen];
            byteBuff.get(idcBytes);
            s.setGateIrisDeactivationCode(new String(idcBytes));

            s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get())); // index++;

            int numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateStructureBlocks().add(bl.getLocation());
            }

            numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGatePortalBlocks().add(bl.getLocation());
            }

            return s;
        }
        else if (s.getLoadedVersion() == 4)
        {
            final byte[] locArray = new byte[32];
            final byte[] blocArray = new byte[12];
            // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
            //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

            byteBuff.get(blocArray);
            s.setGateActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisActivationBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateTeleportSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateTeleportSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateTeleportSign((Sign) s.getGateTeleportSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateTeleportSignBlock());
                    }
                    catch (final Exception e)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    }
                }
            }

            s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateTempTargetId(byteBuff.getLong());

            final int facingSize = byteBuff.getInt();
            final byte[] strBytes = new byte[facingSize];
            byteBuff.get(strBytes);
            final String faceStr = new String(strBytes);
            s.setGateFacing(BlockFace.valueOf(faceStr));

            s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
            s.getGatePlayerTeleportLocation().setPitch(0);

            final int idcLen = byteBuff.getInt();
            final byte[] idcBytes = new byte[idcLen];
            byteBuff.get(idcBytes);
            s.setGateIrisDeactivationCode(new String(idcBytes));

            s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get())); // index++;
            s.setGateIrisDefaultActive(s.isGateIrisActive());
            int numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateStructureBlocks().add(bl.getLocation());
            }

            numBlocks = byteBuff.getInt(); //DataUtils.byteArrayToInt(gate_data, index); index += 4;
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGatePortalBlocks().add(bl.getLocation());
            }

            return s;
        }
        else if (s.getLoadedVersion() == 5)
        {
            final byte[] locArray = new byte[32];
            final byte[] blocArray = new byte[12];
            // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
            //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

            byteBuff.get(blocArray);
            s.setGateActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisActivationBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateTeleportSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateTeleportSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateTeleportSign((Sign) s.getGateTeleportSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateTeleportSignBlock());
                    }
                    catch (final Exception e)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    }
                }
            }

            s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateTempTargetId(byteBuff.getLong());

            final int facingSize = byteBuff.getInt();
            final byte[] strBytes = new byte[facingSize];
            byteBuff.get(strBytes);
            final String faceStr = new String(strBytes);
            s.setGateFacing(BlockFace.valueOf(faceStr));

            s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
            s.getGatePlayerTeleportLocation().setPitch(0);

            final int idcLen = byteBuff.getInt();
            final byte[] idcBytes = new byte[idcLen];
            byteBuff.get(idcBytes);
            s.setGateIrisDeactivationCode(new String(idcBytes));

            s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateIrisDefaultActive(s.isGateIrisActive());
            s.setGateLit(DataUtils.byteToBoolean(byteBuff.get()));

            int numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateStructureBlocks().add(bl.getLocation());
            }

            numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGatePortalBlocks().add(bl.getLocation());
            }

            while (s.getGateLightBlocks().size() < 2)
            {
                s.getGateLightBlocks().add(null);
            }

            s.getGateLightBlocks().set(1, new ArrayList<Location>());

            numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateLightBlocks().get(1).add(bl.getLocation());
            }

            return s;
        }
        else if (s.getLoadedVersion() == 6)
        {
            final byte[] locArray = new byte[32];
            final byte[] blocArray = new byte[12];
            // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
            //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

            byteBuff.get(blocArray);
            s.setGateActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisActivationBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateTeleportSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateTeleportSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateTeleportSign((Sign) s.getGateTeleportSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateTeleportSignBlock());
                    }
                    catch (final Exception e)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    }
                }
            }

            s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateTempTargetId(byteBuff.getLong());

            final int facingSize = byteBuff.getInt();
            final byte[] strBytes = new byte[facingSize];
            byteBuff.get(strBytes);
            final String faceStr = new String(strBytes);
            s.setGateFacing(BlockFace.valueOf(faceStr));
            
            s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
            s.getGatePlayerTeleportLocation().setPitch(0);
            
            final int idcLen = byteBuff.getInt();
            final byte[] idcBytes = new byte[idcLen];
            byteBuff.get(idcBytes);
            s.setGateIrisDeactivationCode(new String(idcBytes));

            s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateIrisDefaultActive(s.isGateIrisActive());
            s.setGateLit(DataUtils.byteToBoolean(byteBuff.get()));

            boolean isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneDialChangeBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            int numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateStructureBlocks().add(bl.getLocation());
            }

            numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGatePortalBlocks().add(bl.getLocation());
            }

            int numLayers = byteBuff.getInt();

            while (s.getGateLightBlocks().size() < numLayers)
            {
                s.getGateLightBlocks().add(new ArrayList<Location>());
            }
            for (int i = 0; i < numLayers; i++)
            {
                numBlocks = byteBuff.getInt();
                for (int j = 0; j < numBlocks; j++)
                {
                    byteBuff.get(blocArray);
                    final Block bl = DataUtils.blockFromBytes(blocArray, w);
                    s.getGateLightBlocks().get(i).add(bl.getLocation());
                }
            }

            numLayers = byteBuff.getInt();

            while (s.getGateWooshBlocks().size() < numLayers)
            {
                s.getGateWooshBlocks().add(new ArrayList<Location>());
            }
            for (int i = 0; i < numLayers; i++)
            {
                numBlocks = byteBuff.getInt();
                for (int j = 0; j < numBlocks; j++)
                {
                    byteBuff.get(blocArray);
                    final Block bl = DataUtils.blockFromBytes(blocArray, w);
                    s.getGateWooshBlocks().get(i).add(bl.getLocation());
                }
            }

            if (byteBuff.remaining() > 0)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "While loading gate, not all byte data was read. This could be bad: " + byteBuff.remaining());
            }

            return s;
        }
        else if (s.getLoadedVersion() == 7)
        {
            final byte[] locArray = new byte[32];
            final byte[] blocArray = new byte[12];
            // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
            //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks

            byteBuff.get(blocArray);
            s.setGateActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisActivationBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            byteBuff.get(locArray);
            s.setGateMinecartTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateTeleportSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateTeleportSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateTeleportSign((Sign) s.getGateTeleportSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateTeleportSignBlock());
                    }
                    catch (final Exception e)
                    {
                        WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Unable to get sign for stargate: " + s.getGateName() + " and will be unable to change dial target.");
                    }
                }
            }

            s.setGateActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateTempTargetId(byteBuff.getLong());

            final int facingSize = byteBuff.getInt();
            final byte[] strBytes = new byte[facingSize];
            byteBuff.get(strBytes);
            final String faceStr = new String(strBytes);
            s.setGateFacing(BlockFace.valueOf(faceStr));
            s.getGatePlayerTeleportLocation().setYaw(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
            s.getGatePlayerTeleportLocation().setPitch(0);
            
            final int idcLen = byteBuff.getInt();
            final byte[] idcBytes = new byte[idcLen];
            byteBuff.get(idcBytes);
            s.setGateIrisDeactivationCode(new String(idcBytes));

            s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateIrisDefaultActive(s.isGateIrisActive());
            s.setGateLit(DataUtils.byteToBoolean(byteBuff.get()));

            boolean isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneDialChangeBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            int numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateStructureBlocks().add(bl.getLocation());
            }

            numBlocks = byteBuff.getInt();
            for (int i = 0; i < numBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGatePortalBlocks().add(bl.getLocation());
            }

            int numLayers = byteBuff.getInt();

            while (s.getGateLightBlocks().size() < numLayers)
            {
                s.getGateLightBlocks().add(new ArrayList<Location>());
            }
            for (int i = 0; i < numLayers; i++)
            {
                numBlocks = byteBuff.getInt();
                for (int j = 0; j < numBlocks; j++)
                {
                    byteBuff.get(blocArray);
                    final Block bl = DataUtils.blockFromBytes(blocArray, w);
                    s.getGateLightBlocks().get(i).add(bl.getLocation());
                }
            }

            numLayers = byteBuff.getInt();

            while (s.getGateWooshBlocks().size() < numLayers)
            {
                s.getGateWooshBlocks().add(new ArrayList<Location>());
            }
            for (int i = 0; i < numLayers; i++)
            {
                numBlocks = byteBuff.getInt();
                for (int j = 0; j < numBlocks; j++)
                {
                    byteBuff.get(blocArray);
                    final Block bl = DataUtils.blockFromBytes(blocArray, w);
                    s.getGateWooshBlocks().get(i).add(bl.getLocation());
                }
            }

            if (byteBuff.remaining() > 0)
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "While loading gate, not all byte data was read. This could be bad: " + byteBuff.remaining());
            }

            return s;
        }

        return null;
    }

    /**
     * Sets the up sign gate network.
     * 
     * @param stargate
     *            the new up sign gate network
     */
    private static void setupSignGateNetwork(final Stargate stargate)
    {
        // Moved this here so that it only creates the sign if the gate is correctly built.
        if ((stargate.getGateName() != null) && (stargate.getGateName().length() > 0))
        {
            String networkName = "Public";

            if ((stargate.getGateTeleportSign() != null) && !stargate.getGateTeleportSign().getLine(1).equals(""))
            {
                // We have a specific network
                networkName = stargate.getGateTeleportSign().getLine(1);
            }
            StargateNetwork net = StargateManager.getStargateNetwork(networkName);
            if (net == null)
            {
                net = StargateManager.addStargateNetwork(networkName);
            }
            StargateManager.addGateToNetwork(stargate, networkName);

            stargate.setGateNetwork(net);
            stargate.setGateSignIndex( -1);
            WormholeXTreme.getScheduler().scheduleSyncDelayedTask(WormholeXTreme.getThisPlugin(), new StargateUpdateRunnable(stargate, ActionToTake.SIGNCLICK));
        }
    }

    /**
     * Stargateto binary.
     * 
     * @param s
     *            the s
     * @return the byte[]
     */
    public static byte[] stargatetoBinary(final Stargate s)
    {
        byte[] utfFaceBytes;
        byte[] utfIdcBytes;
        try
        {
            utfFaceBytes = s.getGateFacing().toString().getBytes("UTF8");
            utfIdcBytes = s.getGateIrisDeactivationCode().getBytes("UTF8");
        }
        catch (final Exception e)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE, false, "Unable to store gate in DB, byte encoding failed: " + e.getMessage());
            e.printStackTrace();
            final byte[] b = null;
            return b;
        }

        // IrisActivation, Dialer, NameSign, Activation, redstoneAct, redstoneDial,
        final int numBlocks = 6;
        // Enter location, Minecart enter location
        final int numLocations = 2;
        final int locationSize = 32;
        final int blockSize = 12;
        // Version, isSignPowered, Active, IrisActive, LitGate
        // RedstoneActivation & RedstoneDialChange
        final int numBytesWithVersion = 7;
        // string size ints 2 + block count ints 2 + sign index int = 5 ints
        // Extra ints are added while calculating size of light and woosh block structures
        final int numInts = 5;
        // Target IDs (sign & target)
        final int numLongs = 2;

        // Size of all the basic sizes we know
        int size = numBytesWithVersion + (numInts * 4) + (numLongs * 8) + (numBlocks * blockSize) + (numLocations * locationSize);
        // Size of the gate blocks
        size += (s.getGateStructureBlocks().size() * blockSize) + (s.getGatePortalBlocks().size() * blockSize);
        // Start with numbers for lightBlocks and wooshBlocks
        int other_ints = 2;
        // Add all the blocks of the lights
        for (int i = 0; i < s.getGateLightBlocks().size(); i++)
        {
            if (s.getGateLightBlocks().get(i) != null)
            {
                size += s.getGateLightBlocks().get(i).size() * blockSize;
            }
            // increment number of total ints
            other_ints++;
        }
        // Add all the blocks of the woosh
        for (int i = 0; i < s.getGateWooshBlocks().size(); i++)
        {
            if (s.getGateWooshBlocks().get(i) != null)
            {
                size += s.getGateWooshBlocks().get(i).size() * blockSize;
            }
            // increment number of total ints
            other_ints++;
        }
        // Size of the strings.
        size += utfFaceBytes.length + utfIdcBytes.length;
        size += other_ints * 4;

        final ByteBuffer dataArr = ByteBuffer.allocate(size);

        dataArr.put(StargateSaveVersion);
        dataArr.put(DataUtils.blockToBytes(s.getGateActivationBlock()));

        if (s.getGateIrisActivationBlock() != null)
        {
            dataArr.put(DataUtils.blockToBytes(s.getGateIrisActivationBlock()));
        }
        else
        {
            dataArr.put(emptyBlock);
        }

        if (s.getGateNameBlockHolder() != null)
        {
            dataArr.put(DataUtils.blockToBytes(s.getGateNameBlockHolder()));
        }
        else
        {
            dataArr.put(emptyBlock);
        }

        dataArr.put(DataUtils.locationToBytes(s.getGatePlayerTeleportLocation()));
        if (s.getGateMinecartTeleportLocation() != null)
        {
            dataArr.put(DataUtils.locationToBytes(s.getGateMinecartTeleportLocation()));
        }
        else 
        {
            dataArr.put(DataUtils.locationToBytes(s.getGatePlayerTeleportLocation()));
        }
        
        if (s.isGateSignPowered())
        {
            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateTeleportSignBlock()));

            // SignIndex
            dataArr.putInt(s.getGateSignIndex());

            // SignTarget
            if (s.getGateSignTarget() != null)
            {
                dataArr.putLong(s.getGateSignTarget().getGateId());
            }
            else
            {
                dataArr.putLong( -1);
            }
        }
        else
        {
            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
            dataArr.putInt( -1);
            dataArr.putLong( -1);
        }

        if (s.isGateActive() && (s.getGateTarget() != null))
        {
            dataArr.put((byte) 1);
            dataArr.putLong(s.getGateTarget().getGateId());
        }
        else
        {
            dataArr.put((byte) 0);
            dataArr.putLong( -1);
        }

        dataArr.putInt(utfFaceBytes.length);
        dataArr.put(utfFaceBytes);

        dataArr.putInt(utfIdcBytes.length);
        dataArr.put(utfIdcBytes);

        if (s.isGateIrisActive())
        {
            dataArr.put((byte) 1);
        }
        else
        {
            dataArr.put((byte) 0);
        }

        if (s.isGateLit())
        {
            dataArr.put((byte) 1);
        }
        else
        {
            dataArr.put((byte) 0);
        }

        if (s.getGateRedstoneActivationBlock() != null)
        {
            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneActivationBlock()));
        }
        else
        {
            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
        }

        if (s.getGateRedstoneDialChangeBlock() != null)
        {
            dataArr.put((byte) 1);
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneDialChangeBlock()));
        }
        else
        {
            dataArr.put((byte) 0);
            dataArr.put(emptyBlock);
        }

        dataArr.putInt(s.getGateStructureBlocks().size());
        for (int i = 0; i < s.getGateStructureBlocks().size(); i++)
        {
            dataArr.put(DataUtils.blockLocationToBytes(s.getGateStructureBlocks().get(i)));
        }

        dataArr.putInt(s.getGatePortalBlocks().size());
        for (int i = 0; i < s.getGatePortalBlocks().size(); i++)
        {
            dataArr.put(DataUtils.blockLocationToBytes(s.getGatePortalBlocks().get(i)));
        }

        dataArr.putInt(s.getGateLightBlocks().size());
        for (int i = 0; i < s.getGateLightBlocks().size(); i++)
        {
            if (s.getGateLightBlocks().get(i) != null)
            {
                dataArr.putInt(s.getGateLightBlocks().get(i).size());
                for (int j = 0; j < s.getGateLightBlocks().get(i).size(); j++)
                {
                    dataArr.put(DataUtils.blockLocationToBytes(s.getGateLightBlocks().get(i).get(j)));
                }
            }
            else
            {
                dataArr.putInt(0);
            }
        }

        dataArr.putInt(s.getGateWooshBlocks().size());
        for (int i = 0; i < s.getGateWooshBlocks().size(); i++)
        {
            if (s.getGateWooshBlocks().get(i) != null)
            {
                dataArr.putInt(s.getGateWooshBlocks().get(i).size());
                for (int j = 0; j < s.getGateWooshBlocks().get(i).size(); j++)
                {
                    dataArr.put(DataUtils.blockLocationToBytes(s.getGateWooshBlocks().get(i).get(j)));
                }
            }
            else
            {
                dataArr.putInt(0);
            }
        }

        if (dataArr.remaining() > 0)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.WARNING, false, "Gate data not filling whole byte array. This could be bad:" + dataArr.remaining());
        }

        return dataArr.array();
    }

    /**
     * Try create gate sign.
     * 
     * @param signBlock
     *            the sign block
     * @param tempGate
     *            the temp gate
     * @return true, if successful
     */
    private static boolean tryCreateGateSign(final Block signBlock, final Stargate tempGate)
    {

        if (signBlock.getType() == Material.WALL_SIGN)
        {
            tempGate.setGateSignPowered(true);
            tempGate.setGateTeleportSignBlock(signBlock);
            tempGate.setGateTeleportSign((Sign) signBlock.getState());
            tempGate.getGateStructureBlocks().add(signBlock.getLocation());

            final String name = tempGate.getGateTeleportSign().getLine(0);
            if ( StargateManager.getStargate(name) != null)
            {
                tempGate.setGateName("");
                return false;
            }

            if (name.length() > 2)
            {
                tempGate.setGateName(name);
            }

            return true;
        }

        return false;
    }
}
