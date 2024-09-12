/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.generacmobilelink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GeneracMobileLinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneracMobileLinkBindingConstants {
    public static final String BINDING_ID = "generacmobilelink";
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_GENERATOR = new ThingTypeUID(BINDING_ID, "generator");

    public static final String PROPERTY_GENERATOR_ID = "generatorId";

    public static final String CHANNEL_HERO_IMAGE_URL = "heroImageUrl";
    public static final String CHANNEL_STATUS_LABEL = "statusLabel";
    public static final String CHANNEL_STATUS_TEXT = "statusText";
    public static final String CHANNEL_ACTIVATION_DATE = "activationDate";
    public static final String CHANNEL_DEVICE_SSID = "deviceSsid";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_IS_CONNECTED = "isConnected";
    public static final String CHANNEL_IS_CONNECTING = "isConnecting";
    public static final String CHANNEL_SHOW_WARNING = "showWarning";
    public static final String CHANNEL_HAS_MAINTENANCE_ALERT = "hasMaintenanceAlert";
    public static final String CHANNEL_LAST_SEEN = "lastSeen";
    public static final String CHANNEL_CONNECTION_TIME = "connectionTime";
    public static final String CHANNEL_RUN_HOURS = "runHours";
    public static final String CHANNEL_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String CHANNEL_HOURS_OF_PROTECTION = "hoursOfProtection";
    public static final String CHANNEL_SIGNAL_STRENGH = "signalStrength";
}
