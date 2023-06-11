/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.hueemulation.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Find all configuration admin interactions in this class
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class WriteConfig {
    public static void setUUID(ConfigurationAdmin configAdmin, String uuid) {
        try {
            Configuration configuration = configAdmin.getConfiguration(HueEmulationService.CONFIG_PID, null);
            Dictionary<String, Object> dictionary = configuration.getProperties();
            if (dictionary == null) {
                dictionary = new Hashtable<>();
            }
            dictionary.put(HueEmulationConfig.CONFIG_UUID, uuid);
            configuration.update(dictionary); // This will restart the service (and call activate again)
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Persists the link mode off state
     */
    public static void unsetPairingMode(ConfigurationAdmin configAdmin) {
        try {
            org.osgi.service.cm.Configuration configuration = configAdmin
                    .getConfiguration(HueEmulationService.CONFIG_PID, null);
            Dictionary<String, Object> dictionary = configuration.getProperties();
            dictionary.put(HueEmulationConfig.CONFIG_PAIRING_ENABLED, false);
            dictionary.put(HueEmulationConfig.CONFIG_CREATE_NEW_USER_ON_THE_FLY, false);
            dictionary.put(HueEmulationConfig.CONFIG_EMULATE_V1, false);
            configuration.update(dictionary);
        } catch (IOException ignore) {
        }
    }
}
