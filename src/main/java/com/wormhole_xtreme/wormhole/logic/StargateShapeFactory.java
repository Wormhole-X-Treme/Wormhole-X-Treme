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

import com.wormhole_xtreme.wormhole.model.Stargate3DShape;
import com.wormhole_xtreme.wormhole.model.StargateShape;

/**
 * A factory for creating StargateShape objects.
 */
public class StargateShapeFactory
{

    /**
     * Creates a new StargateShape object.
     * 
     * @param fileLines
     *            the file lines
     * @return the stargate shape
     */
    private static StargateShape create2DShape(final String[] fileLines)
    {
        return new StargateShape(fileLines);
    }

    /**
     * Creates a new StargateShape object.
     * 
     * @param fileLines
     *            the file lines
     * @return the stargate3 d shape
     */
    private static Stargate3DShape create3DShape(final String[] fileLines)
    {
        return new Stargate3DShape(fileLines);
    }

    /**
     * Creates a new StargateShape object.
     * 
     * @param fileLines
     *            the file lines
     * @return the stargate shape
     */
    public static StargateShape createShapeFromFile(final String[] fileLines)
    {
        for (final String line : fileLines)
        {
            if (line.startsWith("Version=2"))
            {
                //	if ( line.split("=")[1].equals("2") )
                return create3DShape(fileLines);
            }
        }

        return create2DShape(fileLines);
    }
}
