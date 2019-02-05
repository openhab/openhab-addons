/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.logreader.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LogReaderBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class LogReaderBindingConstants {

    private static final String BINDING_ID = "logreader";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_READER = new ThingTypeUID(BINDING_ID, "reader");

    // List of all Channel ids
    public static final String CHANNEL_LASTWARNING = "lastWarningEvent";
    public static final String CHANNEL_LASTERROR = "lastErrorEvent";
    public static final String CHANNEL_LASTCUSTOMEVENT = "lastCustomEvent";
    public static final String CHANNEL_WARNINGS = "warningEvents";
    public static final String CHANNEL_ERRORS = "errorEvents";
    public static final String CHANNEL_CUSTOMEVENTS = "customEvents";
    public static final String CHANNEL_LOGROTATED = "logRotated";

    public static final String CHANNEL_NEWWARNING = "newWarningEvent";
    public static final String CHANNEL_NEWERROR = "newErrorEvent";
    public static final String CHANNEL_NEWCUSTOM = "newCustomEvent";
}
