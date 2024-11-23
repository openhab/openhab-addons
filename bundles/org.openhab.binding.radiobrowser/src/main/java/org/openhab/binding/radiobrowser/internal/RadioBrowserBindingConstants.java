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
package org.openhab.binding.radiobrowser.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RadioBrowserBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RadioBrowserBindingConstants {
    private static final String BINDING_ID = "radiobrowser";
    public static final int HTTP_TIMEOUT_SECONDS = 10;
    public static final String ALL_SERVERS = "all.api.radio-browser.info";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RADIO = new ThingTypeUID(BINDING_ID, "radio");

    // List of all Channel ids
    public static final String CHANNEL_COUNTRY = "country";
    public static final String CHANNEL_LANGUAGE = "language";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_GENRE = "genre";
    public static final String CHANNEL_STATION = "station";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_ICON = "icon";
    public static final String CHANNEL_STREAM = "stream";
    public static final String CHANNEL_RECENT = "recent";
}
