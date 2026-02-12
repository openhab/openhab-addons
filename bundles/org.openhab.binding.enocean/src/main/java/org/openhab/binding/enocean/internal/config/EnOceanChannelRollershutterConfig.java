/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.config;

import java.security.InvalidParameterException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration class for rollershutter channels with FSB14 support.
 *
 * @author Daniel Weber - Initial contribution
 * @author Sven Schad - added ConfigMode for FSB14 blinds
 */
@NonNullByDefault
public class EnOceanChannelRollershutterConfig {

    /**
     * Operating mode for FSB14 actuators.
     */
    public enum ConfigMode {
        LEGACY("legacy"),
        ROLLERSHUTTER("rollershutter"),
        BLINDS("blinds");

        private String value;

        ConfigMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ConfigMode getConfigMode(@Nullable String value) {
            if (value == null || value.isEmpty()) {
                return ConfigMode.LEGACY;
            }

            for (ConfigMode t : ConfigMode.values()) {
                if (t.value.equalsIgnoreCase(value)) {
                    return t;
                }
            }

            throw new InvalidParameterException("Unknown ConfigMode");
        }
    }

    public int shutTime;
    public int swapTime;
    public String configMode;

    public EnOceanChannelRollershutterConfig() {
        shutTime = 255;
        swapTime = 255;
        configMode = "";
    }

    public ConfigMode getConfigMode() {
        return ConfigMode.getConfigMode(configMode);
    }
}
