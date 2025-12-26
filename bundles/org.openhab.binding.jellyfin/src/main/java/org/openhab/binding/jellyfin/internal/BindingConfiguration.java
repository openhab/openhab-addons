/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * The {@link BindingConfiguration} class contains fields mapping binding
 * configuration parameters.
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class BindingConfiguration {
    /**
     * Port used to locate available servers on local network(s).
     */
    public int discoveryPort = 7359;
    /**
     * Maximum amount of time to wait for a response during the server discovery
     * process.
     */
    public int discoveryTimeout = 2000;
    /**
     * Jellyfin specific discovery message.
     */
    public String discoveryMessage = "who is JellyfinServer?";

    public static BindingConfiguration getConfiguration(ConfigurationAdmin configurationAdmin) {
        BindingConfiguration bindingConfig = new BindingConfiguration();

        try {
            var configuration = configurationAdmin.getConfiguration(Constants.BINDING_PID);
            Dictionary<String, Object> properties = configuration.getProperties();

            if (properties != null) {
                org.openhab.core.config.core.Configuration config = new org.openhab.core.config.core.Configuration();
                for (Enumeration<String> keys = properties.keys(); keys.hasMoreElements();) {
                    String key = keys.nextElement();
                    config.put(key, properties.get(key));
                }
                return config.as(BindingConfiguration.class);
            }
        } catch (IOException e) {
            // IOException reading configuration - use defaults
        }

        return bindingConfig;
    }
}
