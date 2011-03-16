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
package com.wormhole_xtreme.model;

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

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.logic.StargateHelper;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;


// TODO: Auto-generated Javadoc
/**
 * WormholeXtreme StargateDBManager.
 *
 * @author Ben Echols (Lologarithm)
 */ 
public class StargateDBManager 
{
	
	/** The Constant wxt. */
	private static final WormholeXTreme wxt = WormholeXTreme.ThisPlugin;
	
	/** The sql_connection. */
	private static Connection sql_connection;
	
	/**
	 * Load stargates.
	 *
	 * @param server the server
	 */
	public static void LoadStargates(Server server)
	{
		if ( sql_connection == null )
			ConnectDB();
		
		List<World> worlds = server.getWorlds();
		
		try
		{
			if (  sql_connection.isClosed() )
				ConnectDB();
			
			PreparedStatement stmt = sql_connection.prepareStatement("SELECT * FROM Stargates;");

			ResultSet gates_data = stmt.executeQuery();
			while ( gates_data.next() )
			{
				String network_name = gates_data.getString("Network");
				StargateNetwork sn = null;
				if ( network_name != null )
				{
					sn = StargateManager.GetStargateNetwork(network_name);
					if ( sn == null && !network_name.equals("") )
						sn = StargateManager.AddStargateNetwork(network_name);
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
					s.GateId = gates_data.getInt("Id");
					s.Owner = gates_data.getString("Owner");
					String gate_shape_name = gates_data.getString("GateShape");
					if (gate_shape_name == null )
						gate_shape_name = "Standard";
					
					s.GateShape = StargateHelper.getShape(gate_shape_name);
					if (  sn != null )
						sn.gate_list.add(s);
					
					StargateManager.AddStargate(s);
				}
				else
				{
					wxt.prettyLog(Level.INFO, true, "Failed to load Stargate '" + sn + "' from DB.");
				}
			}
			gates_data.close();
			stmt.close();
			
			ArrayList<Stargate> gate_list = StargateManager.GetAllGates();
			for ( Stargate s : gate_list )
			{
				World w = s.MyWorld;
			
				if ( s.LitGate && !s.Active)
				{
					s.UnLightStargate();
				}
				
				if ( s.temp_target_id >= 0 )
				{
					// I know this is bad, I am just trying to get this feature out asap.
					for ( Stargate t : gate_list )
					{
						if ( t.GateId == s.temp_target_id)
						{
							s.ForceDialStargate(t);
							break;
						}
					}
				}
				
				if ( s.temp_sign_target >= 0 )
				{
					// I know this is bad, I am just trying to get this feature out asap.
					for ( Stargate t : gate_list )
					{
						if ( t.GateId == s.temp_sign_target)
						{
							s.SignTarget = t;
							break;
						}
					}					
				}
				
				if ( s.IsSignPowered )
				{
					if ( w.isChunkLoaded(s.TeleportSignBlock.getChunk() ))
					{
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
				}
			}
			
			wxt.prettyLog(Level.INFO,false, gate_list.size() + " Wormholes loaded from WormholeDB.");
			
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error loading stargates from DB: " + e.getMessage()); 
		}
		
	}
	
	/** The Store statement. */
	private static volatile PreparedStatement StoreStatement;
	
	/** The Update gate statement. */
	private static volatile PreparedStatement UpdateGateStatement;
	
	/** The Get gate statement. */
	private static volatile PreparedStatement GetGateStatement;
	
	/** The Remove statement. */
	private static volatile PreparedStatement RemoveStatement;
	
	/**
	 * Stargate to sql.
	 *
	 * @param s the s
	 */
	public static void StargateToSQL(Stargate s)
	{
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			if ( GetGateStatement == null )
				GetGateStatement = sql_connection.prepareStatement("SELECT * FROM Stargates WHERE Name = ?");
			GetGateStatement.setString(1, s.Name);
			
			ResultSet gates_data = GetGateStatement.executeQuery();
			if ( gates_data.next() )
			{
				if ( UpdateGateStatement == null )
					UpdateGateStatement = sql_connection.prepareStatement("UPDATE Stargates SET GateData = ?, Network = ?, World = ?, WorldName = ?, WorldEnvironment = ?, Owner = ?, GateShape = ? WHERE Id = ?");
				
				UpdateGateStatement.setBytes(1, StargateHelper.stargatetoBinary(s));
				if ( s.Network != null)
					UpdateGateStatement.setString(2, s.Network.netName);
				else
					UpdateGateStatement.setString(2, "");
				UpdateGateStatement.setLong(3, s.MyWorld.getId());
				UpdateGateStatement.setString(4, s.MyWorld.getName());
				UpdateGateStatement.setString(5, s.MyWorld.getEnvironment().toString());
				UpdateGateStatement.setString(6, s.Owner);
				if ( s.GateShape == null )
					UpdateGateStatement.setString(7, "Standard");
				else
					UpdateGateStatement.setString(7, s.GateShape.shapeName);
				
				UpdateGateStatement.setLong(8, s.GateId);				
				UpdateGateStatement.executeUpdate();
			}
			else
			{
				gates_data.close();
				
				if ( StoreStatement == null )
					StoreStatement = sql_connection.prepareStatement("INSERT INTO Stargates(Name, GateData, Network, World, WorldName, WorldEnvironment, Owner, GateShape) VALUES ( ? , ? , ? , ? , ? , ?, ?, ? );");
		
				StoreStatement.setString(1, s.Name);
				byte[] data = StargateHelper.stargatetoBinary(s);
				StoreStatement.setBytes(2, data);
				if ( s.Network != null)
					StoreStatement.setString(3, s.Network.netName);
				else
					StoreStatement.setString(3, "");
				
				StoreStatement.setLong(4, s.MyWorld.getId());
				StoreStatement.setString(5, s.MyWorld.getName());
				StoreStatement.setString(6, s.MyWorld.getEnvironment().toString());
				StoreStatement.setString(7, s.Owner);
				StoreStatement.setString(8, s.GateShape.shapeName);
				
				StoreStatement.executeUpdate();

				GetGateStatement.setString(1, s.Name);
				gates_data = GetGateStatement.executeQuery();
				if ( gates_data.next() )
				{
					s.GateId = gates_data.getInt("Id");
				}
			}
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Removes the stargate from sql.
	 *
	 * @param s the s
	 */
	public static void RemoveStargateFromSQL(Stargate s)
	{
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			if ( RemoveStatement == null )
				RemoveStatement = sql_connection.prepareStatement("DELETE FROM Stargates WHERE name = ?;");

			RemoveStatement.setString(1, s.Name);
			RemoveStatement.executeUpdate();
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Connect db.
	 */
	private static void ConnectDB()
	{
		try 
		{
			Class.forName("org.hsqldb.jdbcDriver" );
		} 
		catch (Exception e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"ERROR: failed to load HSQLDB JDBC driver.");
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
	    		wxt.prettyLog(Level.SEVERE,false,"WormholeDB already connected.");
	    	}
	    }
	    catch ( SQLException e)
	    {
	    	wxt.prettyLog(Level.SEVERE,false,"Failed to intialized internal DB. Stargates will not be saved: " + e.getMessage());
	    }
	}

	/**
	 * Shutdown.
	 */
	public static void Shutdown()
	{
		try
		{
			//StoreStatement.close();
			//RemoveStatement.close();
			if (!sql_connection.isClosed()) {
				StoreStatement = sql_connection.prepareStatement("SHUTDOWN");
				StoreStatement.execute();
				sql_connection.close();
				wxt.prettyLog(Level.INFO, false, "WormholeDB shutdown successfull.");
			}
		}
		catch (SQLException e)
		{
			wxt.prettyLog(Level.SEVERE,false," Failed to shutdown:" + e.getMessage());
		}
	}
	
	/** The Update indv perm statement. */
	private static volatile PreparedStatement UpdateIndvPermStatement = null;
	
	/** The Store indv perm statement. */
	private static volatile PreparedStatement StoreIndvPermStatement = null;
	
	/** The Get indv perm statement. */
	private static volatile PreparedStatement GetIndvPermStatement = null;
	
	/**
	 * Store individual permission in db.
	 *
	 * @param player the player
	 * @param pl the pl
	 */
	public static void StoreIndividualPermissionInDB(String player, PermissionLevel pl)
	{
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			
			if ( GetIndvPermStatement == null )
				GetIndvPermStatement = sql_connection.prepareStatement("SELECT Permission FROM StargateIndividualPermissions WHERE PlayerName = ?;");
			
			GetIndvPermStatement.setString(1, player);
			ResultSet perm = GetIndvPermStatement.executeQuery();
			if ( !perm.next() )
			{
				if ( StoreIndvPermStatement == null )
					StoreIndvPermStatement = sql_connection.prepareStatement("INSERT INTO StargateIndividualPermissions ( PlayerName, Permission ) VALUES ( ? , ? );");
				
				StoreIndvPermStatement.setString(1, player);
				StoreIndvPermStatement.setString(2, pl.toString());
				StoreIndvPermStatement.executeUpdate();
			}
			else
			{
				if ( UpdateIndvPermStatement == null )
					UpdateIndvPermStatement = sql_connection.prepareStatement("UPDATE StargateIndividualPermissions SET Permission = ? WHERE PlayerName = ?;");
				
				UpdateIndvPermStatement.setString(2, player);
				UpdateIndvPermStatement.setString(1, pl.toString());
				int modified = UpdateIndvPermStatement.executeUpdate();

				if ( modified != 1)
					wxt.prettyLog(Level.SEVERE,false,"Failed to update " + player + " permissions in DB.");
			}
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error StoreIndividualPermissionInDB : " + e.getMessage());
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
private static volatile PreparedStatement GetAllIndvPermStatement = null;
	
	/**
	 * Gets the all individual permissions.
	 *
	 * @return the concurrent hash map
	 */
	public static ConcurrentHashMap<String, PermissionLevel> GetAllIndividualPermissions()
	{
		ConcurrentHashMap<String, PermissionLevel> perms = new ConcurrentHashMap<String, PermissionLevel>();
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			
			if ( GetAllIndvPermStatement == null )
				GetAllIndvPermStatement = sql_connection.prepareStatement("SELECT PlayerName, Permission FROM StargateIndividualPermissions;");
			
			ResultSet perm = GetAllIndvPermStatement.executeQuery();
			while ( perm.next() )
			{
				perms.put( perm.getString("PlayerName"), PermissionLevel.valueOf(perm.getString("Permission")) );
			}
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error GetAllIndividualPermissions: " + e.getMessage());
			e.printStackTrace();
		}
		
		return perms;
	}
	
	/** The Update group perm statement. */
	private static volatile PreparedStatement UpdateGroupPermStatement = null;
	
	/** The Store group perm statement. */
	private static volatile PreparedStatement StoreGroupPermStatement = null;
	
	/** The Get group perm statement. */
	private static volatile PreparedStatement GetGroupPermStatement = null;
	
	/**
	 * Store group permission in db.
	 *
	 * @param group the group
	 * @param pl the pl
	 */
	public static void StoreGroupPermissionInDB(String group, PermissionLevel pl)
	{
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			
			if ( GetGroupPermStatement == null )
				GetGroupPermStatement = sql_connection.prepareStatement("SELECT Permission FROM StargateGroupPermissions WHERE GroupName = ?;");
			
			GetGroupPermStatement.setString(1, group);
			ResultSet perm = GetGroupPermStatement.executeQuery();
			
			if ( !perm.next() )
			{
				if ( StoreGroupPermStatement == null )
					StoreGroupPermStatement = sql_connection.prepareStatement("INSERT INTO StargateGroupPermissions ( GroupName, Permission ) VALUES ( ? , ? );");
				
				StoreGroupPermStatement.setString(1, group);
				StoreGroupPermStatement.setString(2, pl.toString());
				StoreGroupPermStatement.executeUpdate();
			}
			else
			{
				if ( UpdateGroupPermStatement == null )
					UpdateGroupPermStatement = sql_connection.prepareStatement("UPDATE StargateGroupPermissions SET Permission = ? WHERE GroupName = ?;");
				
				UpdateGroupPermStatement.setString(2, group);
				UpdateGroupPermStatement.setString(1, pl.toString());
				UpdateGroupPermStatement.executeUpdate();
			}
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error StoreGroupPermissionInDB: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/** The Get all group perm statement. */
	private static volatile PreparedStatement GetAllGroupPermStatement = null;
	
	/**
	 * Gets the all group permissions.
	 *
	 * @return the hash map
	 */
	public static HashMap<String, PermissionLevel> GetAllGroupPermissions()
	{
		HashMap<String, PermissionLevel> perms = new HashMap<String, PermissionLevel>();
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			
			if ( GetAllGroupPermStatement == null )
				GetAllGroupPermStatement = sql_connection.prepareStatement("SELECT GroupName, Permission FROM StargateGroupPermissions;");
			
			ResultSet perm = GetAllGroupPermStatement.executeQuery();
			while ( perm.next() )
			{
				perms.put( perm.getString("GroupName"), PermissionLevel.valueOf(perm.getString("Permission")) );
			}
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error GetAllGroupPermStatement: " + e.getMessage());
			e.printStackTrace();
		}
		
		return perms;
	}
	

	/*private static PreparedStatement StoreConfigStatement = null;
	private static PreparedStatement UpdateConfigStatement = null;*/
	/** The Delete config statement. */
	private static volatile PreparedStatement DeleteConfigStatement = null;
	
	/**
	 * Delete configurations.
	 */
	public static void DeleteConfigurations()
	{
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			
			if ( DeleteConfigStatement == null )
				DeleteConfigStatement = sql_connection.prepareStatement("DELETE FROM Configurations");
			
			DeleteConfigStatement.execute();
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error storing stargate to DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/** The Get all config statement. */
	private static volatile PreparedStatement GetAllConfigStatement = null;
	
	/**
	 * Gets the all configuration.
	 *
	 * @return the hash map
	 */
	public static HashMap<String, String> GetAllConfiguration()
	{
		HashMap<String, String> configs = new HashMap<String, String>();
		if ( sql_connection == null  )
			ConnectDB();
		
		try
		{
			if ( sql_connection.isClosed() )
				ConnectDB();
			
			if ( GetAllConfigStatement == null )
				GetAllConfigStatement = sql_connection.prepareStatement("SELECT Key, Value FROM Configurations;");
			
			ResultSet conf = GetAllConfigStatement.executeQuery();
			while ( conf.next() )
			{
				configs.put( conf.getString("Key"),  conf.getString("Value"));
			}
		}
		catch ( SQLException e) 
		{
			wxt.prettyLog(Level.SEVERE,false,"Error GetAllGroupPermStatement: " + e.getMessage());
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
