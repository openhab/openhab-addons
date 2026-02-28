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
package org.openhab.binding.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HomeAssistantBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantBindingConstants {

    public static final String LEGACY_BINDING_ID = "mqtt";
    public static final String BINDING_ID = "homeassistant";

    // List of all Thing Type UIDs
    public static final ThingTypeUID LEGACY_MQTT_HOMEASSISTANT_THING = new ThingTypeUID(LEGACY_BINDING_ID,
            "homeassistant");
    public static final ThingTypeUID HOMEASSISTANT_DEVICE_THING = new ThingTypeUID(BINDING_ID, "device");
}
