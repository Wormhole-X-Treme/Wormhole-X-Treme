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


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme StargateDBManager.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class StargateDBManager 
{
	
	/** The sql_connection. */
	private static Connection sql_connection;
	
	/**
	 * Load stargates.
	 *
	 * @param server the server
	 */
	public static void loadStargates(Server server)
	{
		if ( sql_connection == null )
			connectDB();
		
		List<World> worlds = server.getWorlds();
		
		try
		{
			if (  sql_connection.isClosed() )
				connectDB();
			
			PreparedStatement stmt = sql_connection.prepareStatement("SELECT * FROM Stargates;");

			ResultSet gates_data = stmt.executeQuery();
			while ( gates_data.next() )
			{
				String network_name = gates_data.getString("Network");
				StargateNetwork sn = null;
				if ( network_name != null )
				{
					sn = StargateManager.getStargateNetwork(network_name);
					if ( sn == null && !network_name.equals("") )
						sn = StargateManager.addStargateNetwork(network_name);
				}
				// Is this the best way to retrieve a world?
				long world_id = gates_data.getLong("World");
				String worldname = gates_data.getString("WorldName");
				String world_environment = gates_data.getString("WorldEnvironment");
				
				
				World w = null;
				if ( worldname.equals("") )
				{
					for ( World poss_w : worlds )
					{
						if ( poss_w.getId() == world_id )
						{
							w = poss_w;
							break;
						}
					}
				}
				else
					w = server.getWorld(worldname);
				
				if ( w == null && !worldname.equals("") )
				{
					server.createWorld(worldname, Environment.valueOf(world_environment) );
					w = server.getWorld(worldname);
				}
				else if ( w == null )
				{
					// Default to first world
					w = worlds.get(0);
				}
				
				Stargate s = StargateHelper.parseVersionedData(gates_data.getBytes("GateData"), w, gates_data.getString("Name"), sn);
				if (s != null )
				{
					s.gateId = gates_data.getInt("Id");
					s.owner = gates_data.getString("Owner");
					String gate_shape_name = gates_data.getString("GateShape");
					if (gate_shape_name == null )
						gate_shape_name = "Standard";
					
					s.gateShape = StargateHelper.getShape(gate_shape_name);
					if (  sn != null )
						sn.gate_list.add(s);
					
					StargateManager.addStargate(s);
				}
				else
				{
					WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, true, "Failed to load Stargate '" + sn + "' from DB.");
				}
			}
			gates_data.close();
			stmt.close();
			
			ArrayList<Stargate> gate_list = StargateManager.getAllGates();
			for ( Stargate s : gate_list )
			{
				World w = s.myWorld;
			
				if ( s.litGate && !s.active)
				{
					s.unLightStargate();
				}
				
				if ( s.tempTargetId >= 0 )
				{
					// I know this is bad, I am just trying to get this feature out asap.
					for ( Stargate t : gate_list )
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
					for ( Stargate t : gate_list )
					{
						if ( t.gateId == s.tempSignTarget)
						{
							s.signTarget = t;
							break;
						}
					}					
				}
				
				if ( s.isSignPowered )
				{
					if ( w.isChunkLoaded(s.teleportSignBlock.getChunk() ))
					{
						s.tryClickTeleportSign(s.teleportSignBlock);
					}
				}
			}
			
			WormholeXTreme.getThisPlugin().prettyLog(Level.INFO,false, gate_list.size() + " Wormholes loaded from WormholeDB.");
			
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error loading stargates from DB: " + e.getMessage()); 
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			if ( getGateStatement == null )
				getGateStatement = sql_connection.prepareStatement("SELECT * FROM Stargates WHERE Name = ?");
			getGateStatement.setString(1, s.name);
			
			ResultSet gates_data = getGateStatement.executeQuery();
			if ( gates_data.next() )
			{
				if ( updateGateStatement == null )
					updateGateStatement = sql_connection.prepareStatement("UPDATE Stargates SET GateData = ?, Network = ?, World = ?, WorldName = ?, WorldEnvironment = ?, Owner = ?, GateShape = ? WHERE Id = ?");
				
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
				gates_data.close();
				
				if ( storeStatement == null )
					storeStatement = sql_connection.prepareStatement("INSERT INTO Stargates(Name, GateData, Network, World, WorldName, WorldEnvironment, Owner, GateShape) VALUES ( ? , ? , ? , ? , ? , ?, ?, ? );");
		
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
				gates_data = getGateStatement.executeQuery();
				if ( gates_data.next() )
				{
					s.gateId = gates_data.getInt("Id");
				}
			}
		}
		catch ( SQLException e) 
		{
		    WormholeXTreme.getThisPlugin().prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Removes the stargate from sql.
	 *
	 * @param s the s
	 */
	public static void removeStargateFromSQL(Stargate s)
	{
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			if ( removeStatement == null )
				removeStatement = sql_connection.prepareStatement("DELETE FROM Stargates WHERE name = ?;");

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
	    	if ( sql_connection == null || sql_connection.isClosed() )
	    	{
		    	sql_connection = DriverManager.getConnection("jdbc:hsqldb:./plugins/WormholeXTreme/WormholeXTremeDB/WormholeXTremeDB", "sa", "");
		    	sql_connection.setAutoCommit(true);
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
			if (!sql_connection.isClosed()) {
				storeStatement = sql_connection.prepareStatement("SHUTDOWN");
				storeStatement.execute();
				sql_connection.close();
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			
			if ( getIndvPermStatement == null )
				getIndvPermStatement = sql_connection.prepareStatement("SELECT Permission FROM StargateIndividualPermissions WHERE PlayerName = ?;");
			
			getIndvPermStatement.setString(1, player);
			ResultSet perm = getIndvPermStatement.executeQuery();
			if ( !perm.next() )
			{
				if ( storeIndvPermStatement == null )
					storeIndvPermStatement = sql_connection.prepareStatement("INSERT INTO StargateIndividualPermissions ( PlayerName, Permission ) VALUES ( ? , ? );");
				
				storeIndvPermStatement.setString(1, player);
				storeIndvPermStatement.setString(2, pl.toString());
				storeIndvPermStatement.executeUpdate();
			}
			else
			{
				if ( updateIndvPermStatement == null )
					updateIndvPermStatement = sql_connection.prepareStatement("UPDATE StargateIndividualPermissions SET Permission = ? WHERE PlayerName = ?;");
				
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			
			if ( getAllIndvPermStatement == null )
				getAllIndvPermStatement = sql_connection.prepareStatement("SELECT PlayerName, Permission FROM StargateIndividualPermissions;");
			
			ResultSet perm = getAllIndvPermStatement.executeQuery();
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			
			if ( getGroupPermStatement == null )
				getGroupPermStatement = sql_connection.prepareStatement("SELECT Permission FROM StargateGroupPermissions WHERE GroupName = ?;");
			
			getGroupPermStatement.setString(1, group);
			ResultSet perm = getGroupPermStatement.executeQuery();
			
			if ( !perm.next() )
			{
				if ( storeGroupPermStatement == null )
					storeGroupPermStatement = sql_connection.prepareStatement("INSERT INTO StargateGroupPermissions ( GroupName, Permission ) VALUES ( ? , ? );");
				
				storeGroupPermStatement.setString(1, group);
				storeGroupPermStatement.setString(2, pl.toString());
				storeGroupPermStatement.executeUpdate();
			}
			else
			{
				if ( updateGroupPermStatement == null )
					updateGroupPermStatement = sql_connection.prepareStatement("UPDATE StargateGroupPermissions SET Permission = ? WHERE GroupName = ?;");
				
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			
			if ( getAllGroupPermStatement == null )
				getAllGroupPermStatement = sql_connection.prepareStatement("SELECT GroupName, Permission FROM StargateGroupPermissions;");
			
			ResultSet perm = getAllGroupPermStatement.executeQuery();
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			
			if ( deleteConfigStatement == null )
				deleteConfigStatement = sql_connection.prepareStatement("DELETE FROM Configurations");
			
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
		if ( sql_connection == null  )
			connectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				connectDB();
			
			if ( getAllConfigStatement == null )
				getAllConfigStatement = sql_connection.prepareStatement("SELECT Key, Value FROM Configurations;");
			
			ResultSet conf = getAllConfigStatement.executeQuery();
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
		
		return configs;
	}
	/*
	public static String GetConfiguration(String key)
	{
		return null;
	}*/


}
