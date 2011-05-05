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

import com.wormhole_xtreme.wormhole.config.ConfigManager.ConfigKeys;
import com.wormhole_xtreme.wormhole.permissions.PermissionsManager.PermissionLevel;

/**
 * The Class DefaultSettings.
 * Based on class "SettingsList" from MinecartMania by Afforess.
 */
class DefaultSettings
{

    /** The Constant config. */
    final static Setting[] config = {
        new Setting(ConfigKeys.TIMEOUT_ACTIVATE, 30, "Number of seconds after a gate is activated, but before dialing before timing out.", "WormholeXTreme"),
        new Setting(ConfigKeys.TIMEOUT_SHUTDOWN, 38, "Number of seconds after a gate is dialed before automatically shutdown. With 0 timeout a gate won't shutdown until something goes through the gate.", "WormholeXTreme"),
        new Setting(ConfigKeys.BUILT_IN_PERMISSIONS_ENABLED, false, "This should be set to true if you want the built in permissions enabled. This setting does nothing if you have Permissions plugin installed.", "WormholeXTreme"),
        new Setting(ConfigKeys.BUILT_IN_DEFAULT_PERMISSION_LEVEL, PermissionLevel.WORMHOLE_USE_PERMISSION, "If built in permissions are being used, this is the default level of control users (non-ops) have.", "WormholeXTreme"),
        new Setting(ConfigKeys.PERMISSIONS_SUPPORT_DISABLE, false, "If set to true, Permissions plugin will not be attached to evem if available.", "WormholeXTreme"),
        new Setting(ConfigKeys.SIMPLE_PERMISSIONS, false, "If using Permissions plugin based permissions, setting this to true switches WormholeXTreme to use an extremely simplified permissions set ('wormhole.simple.use', 'wormhole.simple.build', 'wormhole.simple.config', and 'wormhole.simple.remove').", "WormholeXTreme"),
        new Setting(ConfigKeys.WORMHOLE_USE_IS_TELEPORT, false, "The wormhole.use (or wormhole.simple.use) permission means that a user can teleport through gate. When false a user will be able to teleport but not activate a gate. When true only users with wormhole.use (or wormhole.simple.use) can even teleport.", "WormholeXTreme"),
        new Setting(ConfigKeys.HELP_SUPPORT_DISABLE, false, "If set to true, Help plugin will not be attached to even if available.", "WormholeXTreme"),
        new Setting(ConfigKeys.WORLDS_SUPPORT_ENABLED, false, "If set to true, the Wormhole X-Treme will offload all of its Chunk and World loading functionality to Wormhole Extreme Worlds.", "WormholeXTreme"),
        new Setting(ConfigKeys.LOG_LEVEL, "INFO", "Log level to use for minecraft logging purposes. Values are SEVERE, WARNING, INFO, CONFIG, FINE, FINER, and FINEST. In order of least to most logging output.", "WormholeXTreme")};

}
