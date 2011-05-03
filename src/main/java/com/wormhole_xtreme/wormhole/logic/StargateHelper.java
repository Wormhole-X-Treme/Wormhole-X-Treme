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
    private static final byte StargateSaveVersion = 8;

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
        Stargate s = null;

        for (final String key : shapes.keySet())
        {
            final StargateShape shape = shapes.get(key);
            if (shape != null)
            {
                s = shape instanceof Stargate3DShape
                    ? checkStargate3D(buttonBlock, facing, (Stargate3DShape) shape, false)
                    : checkStargate(buttonBlock, facing, shape, false);
                if (s != null)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINEST, false, "Shape: " + shape.getShapeName() + " is found!");
                }
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
            tempGate.setGateDialLeverBlock(buttonBlock);
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
        s.setGateDialLeverBlock(buttonBlock);
        s.getGateStructureBlocks().add(s.getGateDialLeverBlock().getLocation());
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
        /** Set the gate as redstone powered as needed */
        if (shape.isShapeRedstoneActivated()) {
            s.setGateRedstonePowered(true);
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
            Block teleBlock = StargateHelper.getBlockFromVector(layer.getLayerPlayerExitPosition(), directionVector, lowerCorner, w);

            // First go forward one
            // Block bLoc = teleBlock.getRelative(tempGate.getGateFacing());
            // Now go up until we hit air or water.
            while ((teleBlock.getType() != Material.AIR) && (teleBlock.getType() != Material.WATER))
            {
                teleBlock = teleBlock.getRelative(BlockFace.UP);
            }
            final Location teleLoc = teleBlock.getLocation();
            // Make sure the guy faces the right way out of the portal.
            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(tempGate.getGateFacing()));
            teleLoc.setPitch(0);
            // Put him in the middle of the block instead of a corner.
            // Players are 1.65 blocks tall, so we go up .66 more up :-p
            teleLoc.setX(teleLoc.getX() + 0.5);
            //teleLoc.setY(teleLoc.getY() + 0.66);
            teleLoc.setZ(teleLoc.getZ() + 0.5);
            tempGate.setGatePlayerTeleportLocation(teleLoc);
        }

        // Now set minecart teleport in location
        if (layer.getLayerMinecartExitPosition().length > 0)
        {
            Block teleBlock = StargateHelper.getBlockFromVector(layer.getLayerMinecartExitPosition(), directionVector, lowerCorner, w);

            // First go forward one
            //Block bLoc = teleBlock.getRelative(tempGate.getGateFacing());
            // Now go up until we hit air or water.
            while ((teleBlock.getType() != Material.AIR) && (teleBlock.getType() != Material.WATER))
            {
                teleBlock = teleBlock.getRelative(BlockFace.UP);
            }
            final Location teleLoc = teleBlock.getLocation();
            // Make sure the guy faces the right way out of the portal.
            teleLoc.setYaw(WorldUtils.getDegreesFromBlockFace(tempGate.getGateFacing()));
            teleLoc.setPitch(0);
            // Put him in the middle of the block instead of a corner.
            // Players are 1.65 blocks tall, so we go up .66 more up :-p
            teleLoc.setX(teleLoc.getX() + 0.5);
            //teleLoc.setY(teleLoc.getY() + 0.66);
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
        if (layer.getLayerDialSignPosition().length > 0)
        {
            final Block signBlockHolder = StargateHelper.getBlockFromVector(layer.getLayerDialSignPosition(), directionVector, lowerCorner, w);
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
        if (layer.getLayerNameSignPosition().length > 0)
        {
            tempGate.setGateNameBlockHolder(StargateHelper.getBlockFromVector(layer.getLayerNameSignPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneDialActivationPosition().length > 0)
        {
            tempGate.setGateRedstoneDialActivationBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneDialActivationPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneSignActivationPosition().length > 0)
        {
            tempGate.setGateRedstoneSignActivationBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneSignActivationPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerRedstoneGateActivatedPosition().length > 0)
        {
            tempGate.setGateRedstoneGateActivatedBlock(StargateHelper.getBlockFromVector(layer.getLayerRedstoneGateActivatedPosition(), directionVector, lowerCorner, w));
        }

        if (layer.getLayerIrisActivationPosition().length > 0)
        {
            tempGate.setGateIrisLeverBlock(StargateHelper.getBlockFromVector(layer.getLayerIrisActivationPosition(), directionVector, lowerCorner, w).getFace(tempGate.getGateFacing()));
            tempGate.getGateStructureBlocks().add(tempGate.getGateIrisLeverBlock().getLocation());
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
                    if (br != null)
                    {
                        br.close();
                    }
                }
                catch (final IOException e)
                {
                    WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
                }
                try
                {
                    if (bw != null)
                    {
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
            s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateDialSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getInt());
            if (s.isGateSignPowered())
            {
                s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateDialSignBlock());
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
            s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateDialSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateDialSignBlock());
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
            s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateDialSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateDialSignBlock());
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
            s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

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
            s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateDialSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateDialSignBlock());
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
            s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

            boolean isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneDialActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneSignActivationBlock(DataUtils.blockFromBytes(blocArray, w));
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
            s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            byteBuff.get(locArray);
            s.setGateMinecartTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateDialSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateDialSignBlock());
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
            s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

            boolean isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneDialActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            isRedstone = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstone)
            {
                s.setGateRedstoneSignActivationBlock(DataUtils.blockFromBytes(blocArray, w));
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
        else if (s.getLoadedVersion() == 8)
        {
            final byte[] locArray = new byte[32];
            final byte[] blocArray = new byte[12];
            // version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
            //  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks
            /** 
             * version, gateDialLeverBlock, gateIrisLeverBlock, gateNameBlockHolder, gatePlayerTeleportLocation,
             * gateMinecartTeleportLocation, gateSignPowered, gateDialSignIndex, gateDialSignTarget, 
             * gateDialSignBlock, gateActive, gateTarget, gateFacingLength, gateFacing,
             * gateIrisDeactivationCodeLength, gateIrisDeactivationCode, gateIrisActive, gateLightsActive,
             * redstoneDA, gateRedstoneDialActivationBlock, redstoneSA, gateRedstoneSignActivationBlock,
             * redstoneGA, gateRedstoneGateActivatedBlock, gateRedstonePowered, numStructureBlocks, gateStructureBlocks,
             * numPortalBlocks, gatePortalBlocks, numLightLayers, gateLightBlocks, numWooshLayers,
             * gateWooshBlocks
             */
            byteBuff.get(blocArray);
            s.setGateDialLeverBlock(DataUtils.blockFromBytes(blocArray, w));
            // WorldUtils.checkChunkLoad(s.activationBlock);

            byteBuff.get(blocArray);
            s.setGateIrisLeverBlock(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(blocArray);
            s.setGateNameBlockHolder(DataUtils.blockFromBytes(blocArray, w));

            byteBuff.get(locArray);
            s.setGatePlayerTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            byteBuff.get(locArray);
            s.setGateMinecartTeleportLocation(DataUtils.locationFromBytes(locArray, w));

            s.setGateSignPowered(DataUtils.byteToBoolean(byteBuff.get()));

            byteBuff.get(blocArray);
            s.setGateDialSignIndex(byteBuff.getInt());
            s.setGateTempSignTarget(byteBuff.getLong());
            if (s.isGateSignPowered())
            {
                s.setGateDialSignBlock(DataUtils.blockFromBytes(blocArray, w));

                if (w.isChunkLoaded(s.getGateDialSignBlock().getChunk()))
                {
                    try
                    {
                        s.setGateDialSign((Sign) s.getGateDialSignBlock().getState());
                        s.tryClickTeleportSign(s.getGateDialSignBlock());
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
            s.getGateMinecartTeleportLocation().setY(WorldUtils.getDegreesFromBlockFace(s.getGateFacing()));
            s.getGateMinecartTeleportLocation().setPitch(0);

            final int idcLen = byteBuff.getInt();
            final byte[] idcBytes = new byte[idcLen];
            byteBuff.get(idcBytes);
            s.setGateIrisDeactivationCode(new String(idcBytes));

            s.setGateIrisActive(DataUtils.byteToBoolean(byteBuff.get()));
            s.setGateIrisDefaultActive(s.isGateIrisActive());
            s.setGateLightsActive(DataUtils.byteToBoolean(byteBuff.get()));

            final boolean isRedstoneDA = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstoneDA)
            {
                s.setGateRedstoneDialActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            final boolean isRedstoneSA = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstoneSA)
            {
                s.setGateRedstoneSignActivationBlock(DataUtils.blockFromBytes(blocArray, w));
            }

            final boolean isRedstoneGA = DataUtils.byteToBoolean(byteBuff.get());
            byteBuff.get(blocArray);
            if (isRedstoneGA)
            {
                s.setGateRedstoneGateActivatedBlock(DataUtils.blockFromBytes(blocArray, w));
            }
            
            s.setGateRedstonePowered(DataUtils.byteToBoolean(byteBuff.get()));
            
            final int numStructureBlocks = byteBuff.getInt();
            for (int i = 0; i < numStructureBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGateStructureBlocks().add(bl.getLocation());
            }

            final int numPortalBlocks = byteBuff.getInt();
            for (int i = 0; i < numPortalBlocks; i++)
            {
                byteBuff.get(blocArray);
                final Block bl = DataUtils.blockFromBytes(blocArray, w);
                s.getGatePortalBlocks().add(bl.getLocation());
            }

            final int numLightLayers = byteBuff.getInt();

            while (s.getGateLightBlocks().size() < numLightLayers)
            {
                s.getGateLightBlocks().add(new ArrayList<Location>());
            }
            
            for (int i = 0; i < numLightLayers; i++)
            {
                final int numLightBlocks = byteBuff.getInt();
                for (int j = 0; j < numLightBlocks; j++)
                {
                    byteBuff.get(blocArray);
                    final Block bl = DataUtils.blockFromBytes(blocArray, w);
                    s.getGateLightBlocks().get(i).add(bl.getLocation());
                }
            }

            final int numWooshLayers = byteBuff.getInt();

            while (s.getGateWooshBlocks().size() < numWooshLayers)
            {
                s.getGateWooshBlocks().add(new ArrayList<Location>());
            }
            for (int i = 0; i < numWooshLayers; i++)
            {
                final int numWooshBlocks = byteBuff.getInt();
                for (int j = 0; j < numWooshBlocks; j++)
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

            if ((stargate.getGateDialSign() != null) && !stargate.getGateDialSign().getLine(1).equals(""))
            {
                // We have a specific network
                networkName = stargate.getGateDialSign().getLine(1);
            }
            StargateNetwork net = StargateManager.getStargateNetwork(networkName);
            if (net == null)
            {
                net = StargateManager.addStargateNetwork(networkName);
            }
            StargateManager.addGateToNetwork(stargate, networkName);

            stargate.setGateNetwork(net);
            stargate.setGateDialSignIndex( -1);
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

        /** 
         * gateDialLeverBlock, gateIrisLeverBlock, gateNameBlockHolder, gateDialSignBlock, 
         * gateRedstoneDialActivationBlock, gateRedstoneSignActivationBlock, gateRedstoneGateActivatedBlock 
         * */
        
        final int numBlocks = 7;
        // Enter location, Minecart enter location
        final int numLocations = 2;
        final int locationSize = 32;
        final int blockSize = 12;
        /** 
         * saveVersion, gateSignPowered, gateActive, gateIrisActive, gateLightsActive, 
         * gateRedstoneDialActivation, gateRedstoneSignActivation, gateRedstoneGateActivated, gateRedstonePowered 
         * */
        final int numBytesWithVersion = 9;
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

        /** saveVersion - byte 1*/
        dataArr.put(StargateSaveVersion);
        /** gateDialLeverBlock - block 1*/
        dataArr.put(DataUtils.blockToBytes(s.getGateDialLeverBlock()));
        /** gateIrisLeverBlock - block 2*/
        dataArr.put(s.getGateIrisLeverBlock() != null ? DataUtils.blockToBytes(s.getGateIrisLeverBlock()) : emptyBlock);
        /** gateNameBlockHolder - block 3*/
        dataArr.put(s.getGateNameBlockHolder() != null ? DataUtils.blockToBytes(s.getGateNameBlockHolder()) : emptyBlock);
        /** gatePlayerTeleportLocation - location 1*/
        dataArr.put(DataUtils.locationToBytes(s.getGatePlayerTeleportLocation()));
        /** gateMinecartTeleportLocation - location 2*/
        dataArr.put(s.getGateMinecartTeleportLocation() != null ? DataUtils.locationToBytes(s.getGateMinecartTeleportLocation()) : DataUtils.locationToBytes(s.getGatePlayerTeleportLocation()));

        if (s.isGateSignPowered())
        {
            /** gateSignPowered - byte 2*/
            dataArr.put((byte) 1);
            /** gateDialSignBlock - block 4*/
            dataArr.put(DataUtils.blockToBytes(s.getGateDialSignBlock()));
            /** gateDialSignIndex - int 1*/
            dataArr.putInt(s.getGateDialSignIndex());
            /** gateDialSignTarget - long 1*/
            dataArr.putLong(s.getGateDialSignTarget() != null ? s.getGateDialSignTarget().getGateId() : -1);
        }
        else
        {
            /** gateSignPowered - byte 2*/
            dataArr.put((byte) 0);
            /** gateDialSignBlock - block 4*/
            dataArr.put(emptyBlock);
            /** gateDialSignIndex - int 1*/
            dataArr.putInt( -1);
            /** gateDialSignTarget - long 1*/
            dataArr.putLong( -1);
        }

        if (s.isGateActive() && (s.getGateTarget() != null))
        {
            /** gateActive - byte 3*/
            dataArr.put((byte) 1);
            /** gateTarget - long 2*/
            dataArr.putLong(s.getGateTarget().getGateId());
        }
        else
        {
            /** gateActive - byte 3*/
            dataArr.put((byte) 0);
            /** gateTarget - long 2*/
            dataArr.putLong( -1);
        }
        
        /** utfFaceBytes - int 2 */
        dataArr.putInt(utfFaceBytes.length);
        dataArr.put(utfFaceBytes);
        /** utfIdcBytes - int 3 */
        dataArr.putInt(utfIdcBytes.length);
        dataArr.put(utfIdcBytes);
        /** gateIrisActive - byte 4 */
        dataArr.put(s.isGateIrisActive() ? (byte) 1 : (byte) 0);
        /** gateLightsActive - byte 5 */
        dataArr.put(s.isGateLightsActive() ? (byte) 1 : (byte) 0);

        if (s.getGateRedstoneDialActivationBlock() != null)
        {
            /** gateRedstoneDialActivation - byte 6 */
            dataArr.put((byte) 1);
            /** gateRedstoneDialActivationBlock - block 5 */
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneDialActivationBlock()));
        }
        else
        {
            /** gateRedstoneDialActivation - byte 6 */
            dataArr.put((byte) 0);
            /** gateRedstoneDialActivationBlock - block 5 */
            dataArr.put(emptyBlock);
        }

        if (s.getGateRedstoneSignActivationBlock() != null)
        {
            /** gateRedstoneSignActivation - byte 7 */
            dataArr.put((byte) 1);
            /** gateRedstoneSignActivationBlock - block 6*/
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneSignActivationBlock()));
        }
        else
        {
            /** gateRedstoneSignActivation - byte 7 */
            dataArr.put((byte) 0);
            /** gateRedstoneSignActivationBlock - block 6*/
            dataArr.put(emptyBlock);
        }

        if (s.getGateRedstoneGateActivatedBlock() != null)
        {
            /** gateRedstoneGateActivated - byte 8*/
            dataArr.put((byte) 1);
            /** gateRedstoneGateActivatedBlock - block 7 */
            dataArr.put(DataUtils.blockToBytes(s.getGateRedstoneGateActivatedBlock()));
        }
        else
        {
            /** gateRedstoneGateActivated - byte 8*/
            dataArr.put((byte) 0);
            /** gateRedstoneGateActivatedBlock - block 7 */
            dataArr.put(emptyBlock);
        }
        /** gateRedstonePowered - byte 9 */
        dataArr.put(s.isGateRedstonePowered() ?  (byte) 1 : (byte) 0);
        
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
            tempGate.setGateDialSignBlock(signBlock);
            tempGate.setGateDialSign((Sign) signBlock.getState());
            tempGate.getGateStructureBlocks().add(signBlock.getLocation());

            final String name = tempGate.getGateDialSign().getLine(0);
            if (StargateManager.getStargate(name) != null)
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
