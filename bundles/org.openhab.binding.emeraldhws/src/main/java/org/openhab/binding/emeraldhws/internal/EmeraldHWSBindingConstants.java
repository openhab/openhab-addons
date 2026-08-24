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
package org.openhab.binding.emeraldhws.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EmeraldHWSBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author paul@smedley.id.au - Initial contribution
 */
@NonNullByDefault
public class EmeraldHWSBindingConstants {

    private static final String BINDING_ID = "emeraldhws";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_HWS = new ThingTypeUID(BINDING_ID, "hws");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_CURRENT_TEMPERATURE = "current-temperature";
    public static final String CHANNEL_SET_TEMPERATURE = "set-temperature";
    public static final String CHANNEL_FAULT = "fault";
    public static final String CHANNEL_DEFROST = "defrost";
    public static final String CHANNEL_WORK_STATE = "work-state";

    public static final String PROPERTY_WIFI_NAME = "wifi-name";

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_HWS);
}
