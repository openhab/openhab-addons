/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.logreader;

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
    public static final ThingTypeUID THING_TAILER = new ThingTypeUID(BINDING_ID, "tailer");

    // List of all Channel ids for "reader"
    public static final String CHANNEL_READER_LOGROTATED = "logRotated";
    public static final String CHANNEL_READER_LASTLINE = "lastLine";
    public static final String CHANNEL_READER_LASTREAD = "lastRead";
    public static final String CHANNEL_READER_WARNINGS = "warningLines";
    public static final String CHANNEL_READER_ERRORS = "errorLines";
    public static final String CHANNEL_READER_LASTWARNING = "lastWarningLine";
    public static final String CHANNEL_READER_LASTERROR = "lastErrorLine";

    // List of all Channel ids for "tailer"
    public static final String CHANNEL_TAILER_LASTWARNING = "lastWarningEvent";
    public static final String CHANNEL_TAILER_LASTERROR = "lastErrorEvent";
    public static final String CHANNEL_TAILER_LASTCUSTOMEVENT = "lastCustomEvent";
    public static final String CHANNEL_TAILER_WARNINGS = "warningEvents";
    public static final String CHANNEL_TAILER_ERRORS = "errorEvents";
    public static final String CHANNEL_TAILER_CUSTOMEVENTS = "customEvents";
    public static final String CHANNEL_TAILER_LOGROTATED = "logRotated";

    public static final String CHANNEL_TAILER_NEWWARNING = "newWarningEvent";
    public static final String CHANNEL_TAILER_NEWERROR = "newErrorEvent";
    public static final String CHANNEL_TAILER_NEWCUSTOM = "newCustomEvent";

    // List of config parameters
    public static final String FILEPATH = "filePath";
    public static final String REFRESHRATE = "refreshRate";

}
