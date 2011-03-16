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
package com.wormhole_xtreme.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.wormhole_xtreme.WormholeXTreme;



/**
 *  WormholeXTreme DBUpdateUtil
 *  @author Ben Echols (Lologarithm) 
 */
public class DBUpdateUtil 
{
	static Connection sql_con;
	private static final WormholeXTreme wxt = WormholeXTreme.ThisPlugin;
	
	public static boolean updateDB()
	{
		File dir = new File("plugins" + File.separator + "WormholeXTremeDB" + File.separator);
		File dest_dir = new File("plugins" + File.separator + "WormholeXTreme" + File.separator + "WormholeXTremeDB" + File.separator);
		if ( dir.exists() && dir.isDirectory() )
		{
			if ( !dest_dir.exists() )
				try {
					dest_dir.mkdir();
				} catch (Exception e) {
					wxt.prettyLog(Level.SEVERE,false,"Unable to make directory: " + e.getMessage());
				}
			File[] files = dir.listFiles();
			for ( File f : files)
			{
				try {
					f.renameTo(new File(dest_dir, f.getName()));
				} catch (Exception e) {
					wxt.prettyLog(Level.SEVERE,false,"Unable to rename files: " + e.getMessage());
				}
			}
			
			try {
				dir.delete();
			} catch (Exception e) {
				wxt.prettyLog(Level.SEVERE,false,"Unable to delete directory: " + e.getMessage() );
			}
		}
		
		try
		{		
			Class.forName("org.hsqldb.jdbcDriver" );
			sql_con = DriverManager.getConnection("jdbc:hsqldb:./plugins/WormholeXTreme/WormholeXTremeDB/WormholeXTremeDB", "sa", "");
			sql_con.setAutoCommit(true);
		}
		catch (Exception e)
		{
			return false;
		}
		
		int version = getCurrentVersion();		
		int count = getCountDBFiles();

		updateDB(version, count);
		
		return true;
	}

	private static int getCurrentVersion()
	{
		int ver = 0;
		
		try
		{
			Statement stmt = sql_con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(Version) as ver FROM VersionInfo");
			if (rs.next()) {
				ver = rs.getInt("ver");
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			wxt.prettyLog(Level.WARNING,false,"Failed to load WormholeXTremeDB version info, defaulting to 0.");
			wxt.prettyLog(Level.WARNING,false,"If this is your first time running this plugin, you can ignore this error.");
			return 0;
		}
		
		return ver;
	}
	
	private static int getCountDBFiles()
	{
		CodeSource src = WormholeXTreme.class.getProtectionDomain().getCodeSource();
		URL jar = src.getLocation();
		int count = 0;
		try
		{
			ZipInputStream zis = new ZipInputStream(jar.openStream());
			ZipEntry entry;
			while ( (entry = zis.getNextEntry()) != null )
			{
				if ( entry.getName().contains("db_create_") )
					count++;
			}
			zis.close();
		}
		catch (IOException e)
		{
			wxt.prettyLog(Level.SEVERE,false,"Unable to open jar file to read SQL Update commands: " + e.getMessage() );
		}

		return count;
	}

	private static void updateDB(int version, int count)
	{
		if ( count > version )
		{
			boolean success = true;
			try
			{
				Statement stmt = sql_con.createStatement();
				for (int i = (version+1); i <= count; i++)
				{
					StringBuilder sb = new StringBuilder();
					ArrayList<String> lines = readTextFromJar("/sql_commands/db_create_" + i);
					
			        for( String line : lines)
			        {
			        	if(!line.startsWith("#") && !line.startsWith("--"))
			        		sb.append(line);
			        	
			        	if( line.endsWith(";") && !line.startsWith("#") )
			        	{
			        		try
			        		{
			        			stmt.executeUpdate(sb.toString());
			        			//System.out.println("StargatesDB updated:" + rs + " : " + sb.toString());
			        		}
			        		catch (SQLException sql_e)
			        		{
			        			int code = sql_e.getErrorCode(); 
			        			if ( code == -27 || code == -21)
			        			{
			        				wxt.prettyLog(Level.WARNING,false,"(" + code + ")Continuing after Error:" + sql_e);
			        			}
			        			else
			        			{
			        				wxt.prettyLog(Level.SEVERE,false,"(" + code + ")Failure On:" + sql_e);
			        				success = false;
			        				break;
			        			}
			        				
			        		}
			        		sb = new StringBuilder();
			        	}
			        }
					Thread.sleep(250);
				}
		        stmt.close();
		        sql_con.close();
			}
			catch (Exception e)
			{
				wxt.prettyLog(Level.SEVERE,false,"Failure to update db:" + e);
			}
			
			if ( success )
				wxt.prettyLog(Level.INFO,false,"Successfully updated database.");
			else
				wxt.prettyLog(Level.SEVERE,false,"Failed to update DB.");
		}
		else
		{
			//System.out.println("Database is already up to date.");
		}
	}
	
	public static ArrayList<String> readTextFromJar(String s) 
	{
		InputStream is = null;
		BufferedReader br = null;
	    String line;
	    ArrayList<String> list = new ArrayList<String>();

	    try 
	    { 
	    	is = WormholeXTreme.class.getResourceAsStream(s);
	    	br = new BufferedReader(new InputStreamReader(is));
	    	while (null != (line = br.readLine())) 
	    	{
	    		list.add(line);
	    	}
	    }
	    catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }
	    finally 
	    {
	    	try 
	    	{
	    		if (br != null) br.close();
	    		if (is != null) is.close();
	    	}
	    	catch (IOException e) 
	    	{
	    		e.printStackTrace();
	    	}
	    }
	    return list;
	}
}