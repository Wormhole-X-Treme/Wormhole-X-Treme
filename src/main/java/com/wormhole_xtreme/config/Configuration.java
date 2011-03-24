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
package com.wormhole_xtreme.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;


import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;



// TODO: Auto-generated Javadoc
/*
 * This class is based on a class "Configuration.java" 
 * from MinecartMania written by Afforess from Bukkit.org
 */
/**
 * The Class Configuration.
 */
public class Configuration 
{
	
	/** The options. */
	private static File options = null;
	
	
	/**
	 * Load configuration.
	 *
	 * @param desc the desc
	 */
	public static void loadConfiguration(PluginDescriptionFile desc) 
	{
		readFile(desc);
	}

	/**
	 * Read file.
	 *
	 * @param desc the desc
	 */
	private static void readFile(PluginDescriptionFile desc) 
	{	
		File directory = new File("plugins" + File.separator + desc.getName() + File.separator);
		if (!directory.exists()) {
			try {
				directory.mkdir();
			} catch (Exception e) {
				WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE,false,"Unable to make directory: " + e.getMessage());
			}
		}
		String input = directory.getPath() + File.separator + "Settings.txt";
		options = new File(input);
		if (!options.exists() )
		{
			WriteFile(options, desc, DefaultSettings.config);
		}
		/*else if (invalidFile(options)) {
			updateFile(options);
		}*/
		try 
		{
			ReadFile(options, desc);
		}
		catch (IOException e) 
		{
			WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE, false, "Failed to read fiele: " + e.getMessage() );
		}
		if( invalidFile(options, desc))
		{
			writeFile(desc);
		}
	}

	/**
	 * Invalid file.
	 *
	 * @param file the file
	 * @param desc the desc
	 * @return true, if successful
	 */
	private static boolean invalidFile(File file, PluginDescriptionFile desc) 
	{
		BufferedReader bufferedreader = null;
	    try 
		{
			bufferedreader = new BufferedReader(new FileReader(file));
			for (String s = ""; (s = bufferedreader.readLine()) != null; ) 
			{
				if (s.indexOf(desc.getVersion()) > -1) 
				{
					return false;
				}
			}
		}
		catch (IOException exception)
		{
			return true;
		}
		finally 
		{
		    try 
		    {
		        if (bufferedreader != null)
		        {
		            bufferedreader.close();
		        }
		    }
		    catch (IOException e) 
		    { 
		        WormholeXTreme.thisPlugin.prettyLog(Level.WARNING, false, "Failure to close stream: " + e.getMessage()); 
		    }
		}
		return true;
	}

	/*
	@SuppressWarnings("unused")
	private static void updateFile(File options, PluginDescriptionFile pdf) 
	{
		try 
		{
			ConfigurationFlatFile.updateVersionHeader(options, pdf.getName() + " " + pdf.getVersion();
			for (int i = 0; i < SettingList.config.length; i++) 
			{
				ConfigurationFlatFile.updateSetting(
						options,
						SettingList.config[i].getName(),
						SettingList.config[i].getDescription(),
						//Attempt to read value, otherwise use default
						MinecartManiaFlatFile.getValueFromSetting(options, SettingList.config[i].getName(), SettingList.config[i].getValue().toString()));
			}
		} catch (IOException e) {
			MinecartManiaCore.log.severe("Failed to update Minecart Mania settings!");
			e.printStackTrace();
		}
	}*/

	/**
	 * Write file.
	 *
	 * @param desc the desc
	 */
	public static void writeFile(PluginDescriptionFile desc)
	{
		try 
		{
			try {
				options.createNewFile();
			} catch (Exception e) {
				WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE,false,"Unable to create new file: " + e.getMessage());
			}
			BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(options));

			ConfigurationFlatFile.createNewHeader(
					bufferedwriter,
					desc.getName() + " " + desc.getVersion(),
					desc.getName() + " Config Settings",
					true);

			Set<ConfigKeys> keys = ConfigManager.configurations.keySet();
			ArrayList<ConfigKeys> list = new ArrayList<ConfigKeys>(keys);
			Collections.sort(list);
			for ( ConfigKeys key : list ) 
			{
				Setting s = ConfigManager.configurations.get(key);
				if (s != null)
				{
					ConfigurationFlatFile.createNewSetting(bufferedwriter, s.getName(), s.getValue().toString(),s.getDescription());
				}
				
			}
			bufferedwriter.close();
		}
		catch (Exception exception)
		{
			//MinecartManiaCore.log.severe("Failed to write " + desc.getName() +" settings!");
			exception.printStackTrace();
		}
	}
	
	/**
	 * Write file.
	 *
	 * @param file the file
	 * @param desc the desc
	 * @param config the config
	 */
	private static void WriteFile(File file, PluginDescriptionFile desc, Setting[] config)
	{
		try 
		{
			try {
				file.createNewFile();
			} catch (Exception e) {
				WormholeXTreme.thisPlugin.prettyLog(Level.SEVERE,false,"Unable to Create File: " + e.getMessage());
			}
			BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(file));

			ConfigurationFlatFile.createNewHeader(
					bufferedwriter,
					desc.getName() + " " + desc.getVersion(),
					desc.getName() + " Config Settings",
					true);

			for (int i = 0; i < config.length; i++) 
			{
				ConfigurationFlatFile.createNewSetting(
						bufferedwriter,
						config[i].getName(),
						config[i].getValue().toString(),
						config[i].getDescription());
			}
			bufferedwriter.close();
		}
		catch (Exception exception)
		{
			//MinecartManiaCore.log.severe("Failed to write " + desc.getName() +" settings!");
			exception.printStackTrace();
		}
	}

	/**
	 * Read file.
	 *
	 * @param file the file
	 * @param desc the desc
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void ReadFile(File file, PluginDescriptionFile desc) throws IOException
	{

		for (int i = 0; i < DefaultSettings.config.length; i++) 
		{
				
			String value = ConfigurationFlatFile.getValueFromSetting( file, DefaultSettings.config[i].getName(),DefaultSettings.config[i].getValue().toString());
			
			//Attempt to parse the value as boolean
			if (value.toLowerCase().contains("true") || value.toLowerCase().contains("false")) 
			{
				Setting s = new Setting(DefaultSettings.config[i].getName(), Boolean.parseBoolean(value), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
				ConfigManager.configurations.put(s.getName(), s);
			}
			else
			{
				try
				{
					// Check if this is a number
					Setting s = new Setting(DefaultSettings.config[i].getName(), Integer.parseInt(value), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
					if (s.getName() == ConfigKeys.ICONOMY_WORMHOLE_BUILD_COST || s.getName() == ConfigKeys.ICONOMY_WORMHOLE_USE_COST)
					{
					    s = new Setting(DefaultSettings.config[i].getName(),  Double.parseDouble(value + ".0"), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
					}
					ConfigManager.configurations.put(s.getName(), s);
				}
				catch ( NumberFormatException e)
				{
					Setting s = null;
					try
					{
						s = new Setting(DefaultSettings.config[i].getName(), Double.parseDouble(value), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
					}
					catch ( NumberFormatException nfe)
					{						
						// Probably an enum
						if ( DefaultSettings.config[i].getName() == ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL )
						{
							s = new Setting(DefaultSettings.config[i].getName(), PermissionLevel.valueOf(value), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
						}
						// TODO: Build PORTAL_MATRIAL white-list and verify against it for PORTAL_MATERIAL configuration entry. Until then blindly accept any material value.
						else if ( DefaultSettings.config[i].getName() == ConfigKeys.PORTAL_MATERIAL )
						{
							s = new Setting(DefaultSettings.config[i].getName(), Material.valueOf(value), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
						}
						// TODO: Build IRIS_MATERIAL white-list and verify against it for IRIS_MATERIAL configuration entry. Until then blindly accept any material value.
						else if ( DefaultSettings.config[i].getName() == ConfigKeys.IRIS_MATERIAL )
						{
							s = new Setting(DefaultSettings.config[i].getName(), Material.valueOf(value), DefaultSettings.config[i].getDescription(), "WormholeXTreme");
						}
						else
						{
							// I guess its a string
							s = new Setting(DefaultSettings.config[i].getName(), value, DefaultSettings.config[i].getDescription(), "WormholeXTreme");
						}
					}
						
					ConfigManager.configurations.put(s.getName(), s);
				}
			}
			

		}
	}

}
