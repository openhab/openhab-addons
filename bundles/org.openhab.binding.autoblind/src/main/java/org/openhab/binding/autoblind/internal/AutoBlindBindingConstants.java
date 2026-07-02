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
package org.openhab.binding.autoblind.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Binding constants for the AutoBlind binding.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class AutoBlindBindingConstants {

    public static final String BINDING_ID = "autoblind";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HUB = new ThingTypeUID(BINDING_ID, "hub");
    public static final ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BINDING_ID, "shade");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HUB, THING_TYPE_SHADE);

    // Channel IDs
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_LOW_BATTERY = "lowBattery";
    public static final String CHANNEL_FORCE_REFRESH = "forceRefresh";

    // Config parameter names
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_POLLING_INTERVAL = "pollingInterval";
    public static final String CONFIG_PERIPHERAL_UID = "peripheralUid";

    // API
    public static final int API_PORT = 10123;
    public static final int LOW_BATTERY_THRESHOLD = 20;
    public static final long COMMAND_SPACING_MS = 750;
    public static final long COMMAND_SUPPRESSION_MS = 30_000;
    public static final int POSITION_TOLERANCE = 5;

    // Notification long-poll
    public static final float NOTIFICATION_TIMEOUT_SEC = 2.0f;
    public static final int NOTIFICATION_HTTP_BUFFER_SEC = 5;
    public static final long NOTIFICATION_FAILSAFE_MS = 120_000;
    public static final int SETTLEMENT_TOLERANCE = 2;
}
