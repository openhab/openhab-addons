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
package org.openhab.binding.mqtt.awtrixlight.internal;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;

/**
 * The {@link FirmwareSwitchConfiguration} selects what a thing keeps of its configuration when it changes to the
 * thing type of the other firmware. Both firmware types use the same parameters for the clock and for the app, the
 * others are not known to the new thing type.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public final class FirmwareSwitchConfiguration {

    private static final Set<String> CLOCK_PARAMETERS = Set.of("basetopic", "appLockTimeout", "discoverDefaultApps");
    private static final Set<String> APP_PARAMETERS = Set.of("appname", "useButtons");

    private FirmwareSwitchConfiguration() {
    }

    public static Configuration forClock(Configuration configuration) {
        return keep(configuration, CLOCK_PARAMETERS);
    }

    public static Configuration forApp(Configuration configuration) {
        return keep(configuration, APP_PARAMETERS);
    }

    private static Configuration keep(Configuration configuration, Set<String> parameters) {
        Map<String, Object> kept = configuration.getProperties().entrySet().stream()
                .filter(entry -> parameters.contains(entry.getKey()) && entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Configuration(kept);
    }
}
