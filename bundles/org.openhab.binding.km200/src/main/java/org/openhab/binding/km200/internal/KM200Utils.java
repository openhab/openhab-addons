/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KM200Utils is a class with common utilities.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KM200Utils.class);

    /**
     * Translates a service name to a service path (Replaces # through /)
     *
     */
    public static @Nullable String translatesNameToPath(@Nullable String name) {
        return name == null ? null : name.replace("#", "/");
    }

    /**
     * Translates a service path to a service name (Replaces / through #)
     *
     */
    public static String translatesPathToName(String path) {
        return path.replace("/", "#");
    }

    /**
     * This function checks whether the service has a replacement parameter
     *
     */
    public static String checkParameterReplacement(Channel channel, KM200Device device) {
        String service = KM200Utils.translatesNameToPath(channel.getProperties().get("root"));
        if (service == null) {
            LOGGER.warn("Root property not found in device {}", device);
            throw new IllegalStateException("root property not found");
        }
        String currentService = KM200Utils
                .translatesNameToPath(channel.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME));
        if (currentService != null) {
            if (device.containsService(currentService)) {
                KM200ServiceObject curSerObj = device.getServiceObject(currentService);
                if (null != curSerObj) {
                    if (DATA_TYPE_STRING_VALUE.equals(curSerObj.getServiceType())) {
                        String val = (String) curSerObj.getValue();
                        if (val != null) {
                            service = service.replace(SWITCH_PROGRAM_REPLACEMENT, val);
                        }
                        return service;
                    }
                }
            }
        }
        return service;
    }

    /**
     * This function checks whether the channel has channel parameters
     *
     */
    public static Map<String, String> getChannelConfigurationStrings(Channel channel) {
        Map<String, String> paraNames = new HashMap<>();
        if (channel.getConfiguration().containsKey("on")) {
            paraNames.put("on", channel.getConfiguration().get("on").toString());
            LOGGER.debug("Added ON: {}", channel.getConfiguration().get("on"));
        }

        if (channel.getConfiguration().containsKey("off")) {
            paraNames.put("off", channel.getConfiguration().get("off").toString());
            LOGGER.debug("Added OFF: {}", channel.getConfiguration().get("off"));
        }
        return paraNames;
    }
}
