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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;

import com.wormhole_xtreme.wormhole.WormholeXTreme;
import com.wormhole_xtreme.wormhole.logic.StargateHelper;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager.PermissionLevel;



/**
 * WormholeXtreme StargateDBManager.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class StargateDBManager 
{
	
	/** The sql_connection. */
	private static Connection wormholeSQLConnection = null;
	
	/**
	 * Sets the wormhole sql connection.
	 *
	 * @param connection the new wormhole sql connection
	 */
	private static void setWormholeSQLConnection(Connection connection)
	{
	    StargateDBManager.wormholeSQLConnection = connection;
	}
	
	/**
	 * Load stargates.
	 *
	 * @param server the server
	 */
	public static void loadStargates(Server server)
	{
		if ( wormholeSQLConnection == null )
		{
			connectDB();
		}
		final List<World> worlds = server.getWorlds();
		PreparedStatement stmt = null;
		ResultSet gatesData = null;
		try
		{
			if (  wormholeSQLConnection.isClosed() )
			{
				connectDB();
			}
			stmt = wormholeSQLConnection.prepareStatement("SELECT * FROM Stargates;");

			gatesData = stmt.executeQuery();
			while ( gatesData.next() )
			{
				String networkName = gatesData.getString("Network");
				StargateNetwork sn = null;
				if ( networkName != null )
				{
					sn = StargateManager.getStargateNetwork(networkName);
					if ( sn == null && !networkName.equals("") )
						sn = StargateManager.addStargateNetwork(networkName);
				}
				// Is this the best way to retrieve a world?
				long worldId = gatesData.getLong("World");
				String worldname = gatesData.getString("WorldName");
				String worldEnvironment = gatesData.getString("WorldEnvironment");
				
				
				World w = null;
				if ( worldname.equals("") )
				{
					for ( World possW : worlds )
					{
						if ( possW.getId() == worldId )
						{
							w = possW;
							break;
						}
					}
				}
				else
					w = server.getWorld(worldname);
				
				if ( w == null && !worldname.equals("") )
				{
					server.createWorld(worldname, Environment.valueOf(worldEnvironment) );
					w = server.getWorld(worldname);
				}
				else if ( w == null )
				{
					// Default to first world
					w = worlds.get(0);
				}
				
				Stargate s = StargateHelper.parseVersionedData(gatesData.getBytes("GateData"), w, gatesData.getString("Name"), sn);
				if (s != null )
				{
					s.gateId = gatesData.getInt("Id");
					s.owner = gatesData.getString("Owner");
					String gateShapeName = gatesData.getString("GateShape");
					if (gateShapeName == null )
						gateShapeName = "Standard";
					
					s.gateShape = StargateHelper.getShape(gateShapeName);
					if (  sn != null )
					{
						sn.gateList.add(s);
						if (s.isSignPowered)
						{
						    sn.signGateList.add(s);
						}
					}
					StargateManager.addStargate(s);
				}
				else
				{
					WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, true, "Failed to load Stargate '" + sn + "' from DB.");
				}
			}
			gatesData.close();
			stmt.close();
			
			ArrayList<Stargate> gateList = StargateManager.getAllGates();
			for ( Stargate s : gateList )
			{
				World w = s.myWorld;
			
				if ( s.litGate && !s.active)
				{
					s.unLightStargate();
				}
				
				if ( s.tempTargetId >= 0 )
				{
					// I know this is bad, I am just trying to get this feature out asap.
					for ( Stargate t : gateList )
					{
						if ( t.gateId == s.tempTargetId)
						{
							s.forceDialStargate(t);
							break;
						}
					}
				}
				
				if ( s.tempSignTarget >= 0 )
				{
					// I know this is bad, I am just trying to get this feature out asap.
					for ( Stargate t : gateList )
					{
						if ( t.gateId == s.tempSignTarget)
						{
							s.signTarget = t;
							break;
						}
					}					
				}
				
				if ( s.isSignPowered && s.signTarget == null )
				{
					if ( w.isChunkLoaded(s.teleportSignBlock.getChunk() ))
					{
						s.tryClickTeleportSign(s.teleportSignBlock);
					}
				}
			}
			
			WormholeXTreme.getThisPlugin().prettyLog(Level.INFO,false, gateList.size() + " Wormholes loaded from WormholeDB.");
			
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error loading stargates from DB: " + e.getMessage()); 
		}
		finally
		{
		    try 
		    {
		        gatesData.close();
		    }
		    catch (SQLException e) 
		    {
		        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
		    }
		    try 
		    {
		        stmt.close();
		    }
		    catch (SQLException e) 
		    {
		        WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
		    }
		}
	}
	
	/** The Store statement. */
	private static volatile PreparedStatement storeStatement;
	
	/** The Update gate statement. */
	private static volatile PreparedStatement updateGateStatement;
	
	/** The Get gate statement. */
	private static volatile PreparedStatement getGateStatement;
	
	/** The Remove statement. */
	private static volatile PreparedStatement removeStatement;
	
	/**
	 * Stargate to sql.
	 *
	 * @param s the s
	 */
	public static void stargateToSQL(Stargate s)
	{
		if ( wormholeSQLConnection == null  )
		{
			connectDB();
		}
		ResultSet gatesData = null;
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			if ( getGateStatement == null )
				getGateStatement = wormholeSQLConnection.prepareStatement("SELECT * FROM Stargates WHERE Name = ?");
			getGateStatement.setString(1, s.name);
			
			gatesData = getGateStatement.executeQuery();
			if ( gatesData.next() )
			{
				if ( updateGateStatement == null )
					updateGateStatement = wormholeSQLConnection.prepareStatement("UPDATE Stargates SET GateData = ?, Network = ?, World = ?, WorldName = ?, WorldEnvironment = ?, Owner = ?, GateShape = ? WHERE Id = ?");
				
				updateGateStatement.setBytes(1, StargateHelper.stargatetoBinary(s));
				if ( s.network != null)
					updateGateStatement.setString(2, s.network.netName);
				else
					updateGateStatement.setString(2, "");
				updateGateStatement.setLong(3, s.myWorld.getId());
				updateGateStatement.setString(4, s.myWorld.getName());
				updateGateStatement.setString(5, s.myWorld.getEnvironment().toString());
				updateGateStatement.setString(6, s.owner);
				if ( s.gateShape == null )
					updateGateStatement.setString(7, "Standard");
				else
					updateGateStatement.setString(7, s.gateShape.shapeName);
				
				updateGateStatement.setLong(8, s.gateId);				
				updateGateStatement.executeUpdate();
			}
			else
			{
				gatesData.close();
				
				if ( storeStatement == null )
					storeStatement = wormholeSQLConnection.prepareStatement("INSERT INTO Stargates(Name, GateData, Network, World, WorldName, WorldEnvironment, Owner, GateShape) VALUES ( ? , ? , ? , ? , ? , ?, ?, ? );");
		
				storeStatement.setString(1, s.name);
				byte[] data = StargateHelper.stargatetoBinary(s);
				storeStatement.setBytes(2, data);
				if ( s.network != null)
					storeStatement.setString(3, s.network.netName);
				else
					storeStatement.setString(3, "");
				
				storeStatement.setLong(4, s.myWorld.getId());
				storeStatement.setString(5, s.myWorld.getName());
				storeStatement.setString(6, s.myWorld.getEnvironment().toString());
				storeStatement.setString(7, s.owner);
				storeStatement.setString(8, s.gateShape.shapeName);
				
				storeStatement.executeUpdate();

				getGateStatement.setString(1, s.name);
				gatesData = getGateStatement.executeQuery();
				if ( gatesData.next() )
				{
					s.gateId = gatesData.getInt("Id");
				}
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		    try 
		    {
                gatesData.close();
            }
            catch (SQLException e) 
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
            }
		}
	}

	/**
	 * Removes the stargate from sql.
	 *
	 * @param s the s
	 */
	public static void removeStargateFromSQL(Stargate s)
	{
		if ( wormholeSQLConnection == null  )
			connectDB();
		
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			if ( removeStatement == null )
				removeStatement = wormholeSQLConnection.prepareStatement("DELETE FROM Stargates WHERE name = ?;");

			removeStatement.setString(1, s.name);
			removeStatement.executeUpdate();
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Connect db.
	 */
	private static void connectDB()
	{
		try 
		{
			Class.forName("org.hsqldb.jdbcDriver" );
		} 
		catch (Exception e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
			return;
		}

	    try
	    {
	    	if ( wormholeSQLConnection == null || wormholeSQLConnection.isClosed() )
	    	{
		    	setWormholeSQLConnection(DriverManager.getConnection("jdbc:hsqldb:./plugins/WormholeXTreme/WormholeXTremeDB/WormholeXTremeDB", "sa", ""));
		    	wormholeSQLConnection.setAutoCommit(true);
	    	}
	    	else
	    	{
	    	    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"WormholeDB already connected.");
	    	}
	    }
	    catch ( SQLException e)
	    {
	        WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Failed to intialized internal DB. Stargates will not be saved: " + e.getMessage());
	    }
	}

	/**
	 * Shutdown.
	 */
	public static void shutdown()
	{
		try
		{
			//StoreStatement.close();
			//RemoveStatement.close();
			if (!wormholeSQLConnection.isClosed()) {
				storeStatement = wormholeSQLConnection.prepareStatement("SHUTDOWN");
				storeStatement.execute();
				wormholeSQLConnection.close();
				WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, "WormholeDB shutdown successfull.");
			}
		}
		catch (SQLException e)
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false," Failed to shutdown:" + e.getMessage());
		}
	}
	
	/** The Update indv perm statement. */
	private static volatile PreparedStatement updateIndvPermStatement = null;
	
	/** The Store indv perm statement. */
	private static volatile PreparedStatement storeIndvPermStatement = null;
	
	/** The Get indv perm statement. */
	private static volatile PreparedStatement getIndvPermStatement = null;
	
	/**
	 * Store individual permission in db.
	 *
	 * @param player the player
	 * @param pl the pl
	 */
	public static void storeIndividualPermissionInDB(String player, PermissionLevel pl)
	{
		if ( wormholeSQLConnection == null  )
		{
			connectDB();
		}
		ResultSet perm = null;
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			
			if ( getIndvPermStatement == null )
				getIndvPermStatement = wormholeSQLConnection.prepareStatement("SELECT Permission FROM StargateIndividualPermissions WHERE PlayerName = ?;");
			
			getIndvPermStatement.setString(1, player);
			perm = getIndvPermStatement.executeQuery();
			if ( !perm.next() )
			{
				if ( storeIndvPermStatement == null )
					storeIndvPermStatement = wormholeSQLConnection.prepareStatement("INSERT INTO StargateIndividualPermissions ( PlayerName, Permission ) VALUES ( ? , ? );");
				
				storeIndvPermStatement.setString(1, player);
				storeIndvPermStatement.setString(2, pl.toString());
				storeIndvPermStatement.executeUpdate();
			}
			else
			{
				if ( updateIndvPermStatement == null )
					updateIndvPermStatement = wormholeSQLConnection.prepareStatement("UPDATE StargateIndividualPermissions SET Permission = ? WHERE PlayerName = ?;");
				
				updateIndvPermStatement.setString(2, player);
				updateIndvPermStatement.setString(1, pl.toString());
				int modified = updateIndvPermStatement.executeUpdate();

				if ( modified != 1)
				    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Failed to update " + player + " permissions in DB.");
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error StoreIndividualPermissionInDB : " + e.getMessage());
			e.printStackTrace();
		}
		finally 
		{
		    try 
		    {
                perm.close();
            }
            catch (SQLException e) 
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
            }
		}
	}
	
//	public static PermissionLevel GetIndividualPermission(String player)
//	{
//		if ( sql_connection == null  )
//			ConnectDB();
//		
//		try
//		{
//			if ( sql_connection.isClosed() )
//				ConnectDB();
//			
//			if ( GetIndvPermStatement == null )
//				GetIndvPermStatement = sql_connection.prepareStatement("SELECT Permission FROM StargateIndividualPermissions WHERE PlayerName = ?;");
//			
//			GetIndvPermStatement.setString(1, player);
//			ResultSet perm = GetIndvPermStatement.executeQuery();
//			if ( perm.next() )
//				return PermissionLevel.valueOf(perm.getString(1));
//		}
//		catch ( SQLException e) 
//		{
//			wxt.prettyLog(Level.SEVERE,false,"Error GetIndividualPermission: " + e.getMessage());
//			e.printStackTrace();
//		}
//		
//		return PermissionLevel.NO_PERMISSION_SET;
//	}
	
	/** The Get all indv perm statement. */
private static volatile PreparedStatement getAllIndvPermStatement = null;
	
	/**
	 * Gets the all individual permissions.
	 *
	 * @return the concurrent hash map
	 */
	public static ConcurrentHashMap<String, PermissionLevel> getAllIndividualPermissions()
	{
		ConcurrentHashMap<String, PermissionLevel> perms = new ConcurrentHashMap<String, PermissionLevel>();
		if ( wormholeSQLConnection == null  )
		{
			connectDB();
		}
		ResultSet perm = null;
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			
			if ( getAllIndvPermStatement == null )
				getAllIndvPermStatement = wormholeSQLConnection.prepareStatement("SELECT PlayerName, Permission FROM StargateIndividualPermissions;");
			
			perm = getAllIndvPermStatement.executeQuery();
			while ( perm.next() )
			{
				perms.put( perm.getString("PlayerName"), PermissionLevel.valueOf(perm.getString("Permission")) );
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error GetAllIndividualPermissions: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		    try 
		    {
                perm.close();
            }
            catch (SQLException e) 
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
            }
		}
		return perms;
	}
	
	/** The Update group perm statement. */
	private static volatile PreparedStatement updateGroupPermStatement = null;
	
	/** The Store group perm statement. */
	private static volatile PreparedStatement storeGroupPermStatement = null;
	
	/** The Get group perm statement. */
	private static volatile PreparedStatement getGroupPermStatement = null;
	
	/**
	 * Store group permission in db.
	 *
	 * @param group the group
	 * @param pl the pl
	 */
	public static void storeGroupPermissionInDB(String group, PermissionLevel pl)
	{
		if ( wormholeSQLConnection == null  )
		{
			connectDB();
		}
		ResultSet perm = null;
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			
			if ( getGroupPermStatement == null )
				getGroupPermStatement = wormholeSQLConnection.prepareStatement("SELECT Permission FROM StargateGroupPermissions WHERE GroupName = ?;");
			
			getGroupPermStatement.setString(1, group);
			perm = getGroupPermStatement.executeQuery();
			
			if ( !perm.next() )
			{
				if ( storeGroupPermStatement == null )
					storeGroupPermStatement = wormholeSQLConnection.prepareStatement("INSERT INTO StargateGroupPermissions ( GroupName, Permission ) VALUES ( ? , ? );");
				
				storeGroupPermStatement.setString(1, group);
				storeGroupPermStatement.setString(2, pl.toString());
				storeGroupPermStatement.executeUpdate();
			}
			else
			{
				if ( updateGroupPermStatement == null )
					updateGroupPermStatement = wormholeSQLConnection.prepareStatement("UPDATE StargateGroupPermissions SET Permission = ? WHERE GroupName = ?;");
				
				updateGroupPermStatement.setString(2, group);
				updateGroupPermStatement.setString(1, pl.toString());
				updateGroupPermStatement.executeUpdate();
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error StoreGroupPermissionInDB: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		    try 
		    {
                perm.close();
            }
            catch (SQLException e) 
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
            }
		}
	}
	
	/** The Get all group perm statement. */
	private static volatile PreparedStatement getAllGroupPermStatement = null;
	
	/**
	 * Gets the all group permissions.
	 *
	 * @return the hash map
	 */
	public static HashMap<String, PermissionLevel> getAllGroupPermissions()
	{
		HashMap<String, PermissionLevel> perms = new HashMap<String, PermissionLevel>();
		if ( wormholeSQLConnection == null  )
		{
			connectDB();
		}
		ResultSet perm = null;
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			
			if ( getAllGroupPermStatement == null )
				getAllGroupPermStatement = wormholeSQLConnection.prepareStatement("SELECT GroupName, Permission FROM StargateGroupPermissions;");
			
			perm = getAllGroupPermStatement.executeQuery();
			while ( perm.next() )
			{
				perms.put( perm.getString("GroupName"), PermissionLevel.valueOf(perm.getString("Permission")) );
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error GetAllGroupPermStatement: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		    try 
		    {
                perm.close();
            }
            catch (SQLException e) 
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
            }
		}
		return perms;
	}
	

	/*private static PreparedStatement StoreConfigStatement = null;
	private static PreparedStatement UpdateConfigStatement = null;*/
	/** The Delete config statement. */
	private static volatile PreparedStatement deleteConfigStatement = null;
	
	/**
	 * Delete configurations.
	 */
	public static void deleteConfigurations()
	{
		if ( wormholeSQLConnection == null  )
			connectDB();
		
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			
			if ( deleteConfigStatement == null )
				deleteConfigStatement = wormholeSQLConnection.prepareStatement("DELETE FROM Configurations");
			
			deleteConfigStatement.execute();
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/** The Get all config statement. */
	private static volatile PreparedStatement getAllConfigStatement = null;
	
	/**
	 * Gets the all configuration.
	 *
	 * @return the hash map
	 */
	public static HashMap<String, String> getAllConfiguration()
	{
		HashMap<String, String> configs = new HashMap<String, String>();
		if ( wormholeSQLConnection == null  )
		{
			connectDB();
		}
		ResultSet conf = null;
		try
		{
			if ( wormholeSQLConnection.isClosed() )
				connectDB();
			
			if ( getAllConfigStatement == null )
				getAllConfigStatement = wormholeSQLConnection.prepareStatement("SELECT Key, Value FROM Configurations;");
			
			conf = getAllConfigStatement.executeQuery();
			while ( conf.next() )
			{
				configs.put( conf.getString("Key"),  conf.getString("Value"));
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error GetAllGroupPermStatement: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		    try 
		    {
                conf.close();
            }
            catch (SQLException e) 
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, e.getMessage());
            }
		}
		return configs;
	}
	/*
	public static String GetConfiguration(String key)
	{
		return null;
	}*/


}
