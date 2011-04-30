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
package com.wormhole_xtreme.wormhole.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.plugin.PluginDescriptionFile;

import com.wormhole_xtreme.wormhole.permissions.PermissionsManager.PermissionLevel;

/**
 * The Class ConfigManager.
 */
public class ConfigManager
{

    /**
     * The Enum ConfigKeys.
     */
    public enum ConfigKeys
    {

        /** The BUIL t_ i n_ permission s_ enabled. */
        BUILT_IN_PERMISSIONS_ENABLED,

        /** The BUIL t_ i n_ defaul t_ permissio n_ level. */
        BUILT_IN_DEFAULT_PERMISSION_LEVEL,

        /** The PERMISSION SUPPORT DISABLE. */
        PERMISSIONS_SUPPORT_DISABLE,

        /** The SIMPLE PERMISSIONS. */
        SIMPLE_PERMISSIONS,

        /** The WORMHOL e_ us e_ i s_ teleport. */
        WORMHOLE_USE_IS_TELEPORT,

        /** The TIMEOU t_ activate. */
        TIMEOUT_ACTIVATE,

        /** The TIMEOU t_ shutdown. */
        TIMEOUT_SHUTDOWN,

        /** The ICONOMY SUPPORT DISABLE. */
        ICONOMY_SUPPORT_DISABLE,
        /** The ICONOM y_ wormhol e_ us e_ cost. */
        ICONOMY_WORMHOLE_USE_COST,

        /** The ICONOM y_ wormhol e_ buil d_ cost. */
        ICONOMY_WORMHOLE_BUILD_COST,

        /** The ICONOM y_ op s_ exempt. */
        ICONOMY_OPS_EXEMPT,

        /** The ICONOM y_ owne r_ exempt. */
        ICONOMY_OWNER_EXEMPT,

        /** The ICONOM y_ wormhol e_ owne r_ percent. */
        ICONOMY_WORMHOLE_OWNER_PERCENT,

        /** The HELP SUPPORT DISABLE. */
        HELP_SUPPORT_DISABLE,

        /** The WORLDS SUPPORT DISABLE key. */
        WORLDS_SUPPORT_ENABLED,

        /** The LOG LEVEL. */
        LOG_LEVEL
    }

    /**
     * The Enum StringTypes.
     */
    public static enum MessageStrings
    {

        /** The error header. */
        errorHeader("\u00A73:: \u00A75error \u00A73:: \u00A77"),

        /** The normal header. */
        normalHeader("\u00A73:: \u00A77"),

        /** The permission no. */
        permissionNo(errorHeader + "You lack the permissions to do this."),

        /** The target is self. */
        targetIsSelf(errorHeader + "Can't dial own gate without solar flare"),

        /** The target invalid. */
        targetInvalid(errorHeader + "Invalid gate target."),

        /** The target is active. */
        targetIsActive(errorHeader + "Target gate is currently active."),

        /** The gate not active. */
        gateNotActive(errorHeader + "No gate activated to dial."),

        /** The gate remove active. */
        gateRemoveActive(errorHeader + "Gate remotely activated."),

        /** The gate shutdown. */
        gateShutdown(normalHeader + "Gate successfully shutdown."),

        /** The gate activated. */
        gateActivated(normalHeader + "Gate successfully activated."),

        /** The gate deactivated. */
        gateDeactivated(normalHeader + "Gate successfully deactivated."),

        /** The gate dialed. */
        gateConnected(normalHeader + "Stargates connected."),

        /** The construct success. */
        constructSuccess(normalHeader + "Gate successfully constructed."),

        /** The construct name invalid. */
        constructNameInvalid(errorHeader + "Gate name invalid: "),

        /** The construct name too long. */
        constructNameTooLong(errorHeader + "Gate name too long: "),

        /** The construct name taken. */
        constructNameTaken(errorHeader + "Gate name already taken: "),

        /** The request invalid. */
        requestInvalid(errorHeader + "Invalid Request"),

        /** The gate not specified. */
        gateNotSpecified(errorHeader + "No gate name specified.");

        /** The m. */
        private final String m;

        /**
         * Instantiates a new string types.
         * 
         * @param message
         *            the message
         */
        private MessageStrings(final String message)
        {
            m = message;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString()
        {
            return m;
        }
    }

    /** The Constant configurations. */
    protected static final ConcurrentHashMap<ConfigKeys, Setting> configurations = new ConcurrentHashMap<ConfigKeys, Setting>();

    /**
     * Get Built in default permission level settings from ConfigKeys. Return sane PermissionLevel.
     * Return default value if key is missing or broken.
     * 
     * @return the built in default permission level
     */
    public static PermissionLevel getBuiltInDefaultPermissionLevel()
    {
        Setting bidpl;
        if ((bidpl = ConfigManager.configurations.get(ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL)) != null)
        {
            return bidpl.getPermissionLevel();
        }
        else
        {
            return PermissionLevel.WORMHOLE_USE_PERMISSION;
        }
    }

    /**
     * Get Built in permissions enabled settings from ConfigKeys. Return sane boolean value.
     * Return default value if key is missing or broken.
     * 
     * @return the built in permissions enabled
     */
    public static boolean getBuiltInPermissionsEnabled()
    {
        Setting bipe;
        if ((bipe = ConfigManager.configurations.get(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED)) != null)
        {
            return bipe.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets the Help plugin support status.
     * 
     * @return true, if Help plugin support is disabled.
     */
    public static boolean getHelpSupportDisable()
    {
        Setting hsd;
        if ((hsd = ConfigManager.configurations.get(ConfigKeys.HELP_SUPPORT_DISABLE)) != null)
        {
            return hsd.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    /**
     * Get iConomy Op Excempt settings from ConfigKeys. Return sane boolean value.
     * Return default value if key is missing or broken.
     * 
     * @return the iconomy ops excempt
     */
    public static boolean getIconomyOpsExcempt()
    {
        Setting ioe;
        if ((ioe = ConfigManager.configurations.get(ConfigKeys.ICONOMY_OPS_EXEMPT)) != null)
        {
            return ioe.getBooleanValue();
        }
        else
        {
            return true;
        }
    }

    /**
     * Gets the iconomy owner exempt.
     * 
     * @return the iconomy owner exempt
     */
    public static boolean getIconomyOwnerExempt()
    {
        Setting ioe;
        if ((ioe = ConfigManager.configurations.get(ConfigKeys.ICONOMY_OWNER_EXEMPT)) != null)
        {
            return ioe.getBooleanValue();
        }
        else
        {
            return true;
        }
    }

    /**
     * Gets the iConomy plugin support status.
     * 
     * @return true, if iConomy plugin support is disabled.
     */
    public static boolean getIconomySupportDisable()
    {
        Setting isd;
        if ((isd = ConfigManager.configurations.get(ConfigKeys.ICONOMY_SUPPORT_DISABLE)) != null)
        {
            return isd.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    /**
     * Get iConomy wormhole build cost settings from ConfigKeys. Return sane int value.
     * Return default value if key is missing or broken.
     * 
     * @return the iconomy wormhole build cost
     */
    public static double getIconomyWormholeBuildCost()
    {
        Setting iwbc;
        if ((iwbc = ConfigManager.configurations.get(ConfigKeys.ICONOMY_WORMHOLE_BUILD_COST)) != null)
        {
            return iwbc.getDoubleValue();
        }
        else
        {
            final double i = 0.0;
            return i;
        }
    }

    /**
     * Get iConomy wormhole owner percent settings from ConfigKeys. Return sane double value.
     * Return default value if key is missing or broken.
     * 
     * @return the iconomy wormhole owner percent
     */
    public static double getIconomyWormholeOwnerPercent()
    {
        Setting iwop;
        if ((iwop = ConfigManager.configurations.get(ConfigKeys.ICONOMY_WORMHOLE_OWNER_PERCENT)) != null)
        {
            return iwop.getDoubleValue();
        }
        else
        {
            final double d = 0.0;
            return d;
        }
    }

    /**
     * Get iConomy wormhole use cost settings from ConfigKeys. Return sane integer value.
     * Return default value if key is missing or broken.
     * 
     * @return the iconomy wormhole use cost
     */
    public static double getIconomyWormholeUseCost()
    {
        Setting iwuc;
        if ((iwuc = ConfigManager.configurations.get(ConfigKeys.ICONOMY_WORMHOLE_USE_COST)) != null)
        {
            return iwuc.getDoubleValue();
        }
        else
        {
            final double i = 0.0;
            return i;
        }
    }

    /**
     * Get Log Level setting from ConfigKeys. Return sane Level value.
     * Return default value if key is missing or broken.
     * 
     * @return the log level
     */
    public static Level getLogLevel()
    {
        Setting ll;
        if ((ll = ConfigManager.configurations.get(ConfigKeys.LOG_LEVEL)) != null)
        {
            return ll.getLevel();
        }
        else
        {
            return Level.INFO;
        }
    }

    /**
     * Gets the Permissions plugin support status.
     * 
     * @return true, if Permissions plugin support is disabled.
     */
    public static boolean getPermissionsSupportDisable()
    {
        Setting psd;
        if ((psd = ConfigManager.configurations.get(ConfigKeys.PERMISSIONS_SUPPORT_DISABLE)) != null)
        {
            return psd.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets the simple permissions.
     * 
     * @return the simple permissions
     */
    public static boolean getSimplePermissions()
    {
        Setting sp;
        if ((sp = ConfigManager.configurations.get(ConfigKeys.SIMPLE_PERMISSIONS)) != null)
        {
            return sp.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    /**
     * Get Timeout Activate setting from ConfigKeys.
     * Return default value if key is missing or broken.
     * 
     * @return Timeout in seconds.
     */
    public static int getTimeoutActivate()
    {
        Setting ta;
        if ((ta = ConfigManager.configurations.get(ConfigKeys.TIMEOUT_ACTIVATE)) != null)
        {
            return ta.getIntValue();
        }
        else
        {
            final int i = 30;
            return i;
        }
    }

    /**
     * Get Timeout Shutdown setting from ConfigKeys.
     * Return default value if key is missing or broken.
     * 
     * @return Timeout in seconds.
     */
    public static int getTimeoutShutdown()
    {
        Setting ts;
        if ((ts = ConfigManager.configurations.get(ConfigKeys.TIMEOUT_SHUTDOWN)) != null)
        {
            return ts.getIntValue();
        }
        else
        {
            final int i = 38;
            return i;
        }
    }

    /*
     * Get Built in permissions enabled settings from ConfigKeys. Return sane boolean value.
     * Return default value if key is missing or broken.
     */
    /**
     * Gets the wormhole use is teleport.
     * 
     * @return the wormhole use is teleport
     */
    public static boolean getWormholeUseIsTeleport()
    {
        Setting bipe;
        if ((bipe = ConfigManager.configurations.get(ConfigKeys.WORMHOLE_USE_IS_TELEPORT)) != null)
        {
            return bipe.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    public static boolean isWormholeWorldsSupportEnabled()
    {
        Setting wsd;
        if ((wsd = ConfigManager.configurations.get(ConfigKeys.WORLDS_SUPPORT_ENABLED)) != null)
        {
            return wsd.getBooleanValue();
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets the config value.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public static void setConfigValue(final ConfigKeys key, final Object value)
    {
        final Setting s = configurations.get(key);
        if (value != null)
        {
            s.setValue(value);
        }
        else
        {
            //TODO SCREAM BLOODY MURDER IN LOGS ABOUT NULL VALUE
        }
    }

    /**
     * Sets the simple permissions.
     * 
     * @param b
     *            the new simple permissions
     */
    public static void setSimplePermissions(final boolean b)
    {
        ConfigManager.setConfigValue(ConfigKeys.SIMPLE_PERMISSIONS, b);
    }

    /**
     * Set timeout activate setting in ConfigKeys.
     * 
     * @param i
     *            Timeout in seconds.
     */
    public static void setTimeoutActivate(final int i)
    {
        ConfigManager.setConfigValue(ConfigKeys.TIMEOUT_ACTIVATE, i);
    }

    /**
     * Set timeout shutdown setting in ConfigKeys.
     * 
     * @param i
     *            the new timeout shutdown
     */
    public static void setTimeoutShutdown(final int i)
    {
        ConfigManager.setConfigValue(ConfigKeys.TIMEOUT_SHUTDOWN, i);
    }

    /**
     * Sets the up configs.
     * 
     * @param pdf
     *            the new up configs
     */
    public static void setupConfigs(final PluginDescriptionFile pdf)
    {
        Configuration.loadConfiguration(pdf);
    }
}