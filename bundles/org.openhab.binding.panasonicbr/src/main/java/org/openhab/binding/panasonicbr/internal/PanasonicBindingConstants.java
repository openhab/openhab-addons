/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonicbr.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PanasonicBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class PanasonicBindingConstants {
    public static final String BINDING_ID = "panasonicbr";
    public static final String USER_AGENT = "MEI-LAN-REMOTE-CALL";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");

    // List of all Channel id's
    public static final String BUTTON = "button";
    public static final String PLAY_MODE = "play_mode";
    public static final String TIME_ELAPSED = "time_elapsed";
    public static final String TIME_TOTAL = "time_total";
    public static final String CHAPTER_CURRENT = "chapter_current";
    public static final String CHAPTER_TOTAL = "chapter_total";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PLAYER);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream.of(BUTTON, PLAY_MODE, TIME_ELAPSED)
            .collect(Collectors.toSet());

    // Units of measurement of the data delivered by the API
    public static final Unit<Time> API_SECONDS_UNIT = SmartHomeUnits.SECOND;

    public static final String CRLF = "\r\n";
    public static final String COMMA = ",";
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String TWO = "2";
    public static final String STOP = "STOP";
    public static final String PLAY = "PLAY";
    public static final String PAUSE = "PAUSE";
    public static final String UNKNOWN = "unknown";
}
