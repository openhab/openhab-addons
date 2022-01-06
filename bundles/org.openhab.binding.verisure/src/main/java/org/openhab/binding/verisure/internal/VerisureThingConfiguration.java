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
package org.openhab.binding.verisure.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for VerisureThingHandler.
 *
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VerisureThingConfiguration {
    public static final String DEVICE_ID_LABEL = "deviceId";

    private String deviceId = "";
    private int numberOfEvents;
    private int eventTriggerDelay;

    public String getDeviceId() {
        // Make sure device id is normalized, i.e. replace all non character/digits with empty string
        return normalizeDeviceId(deviceId);
    }

    public static String normalizeDeviceId(String unnormalizedDeviceId) {
        return unnormalizedDeviceId.replaceAll("[^a-zA-Z0-9]+", "");
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public int getEventTriggerDelay() {
        return eventTriggerDelay;
    }
}
