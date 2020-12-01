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
package org.openhab.binding.sony.internal.ircc;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.internal.SonyBindingConstants;

/**
 * The class provides all the constants specific to the Ircc system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccConstants {

    // The thing constants
    public static final ThingTypeUID THING_TYPE_IRCC = new ThingTypeUID(SonyBindingConstants.BINDING_ID,
            SonyBindingConstants.IRCC_THING_TYPE_PREFIX);
    static final String ACCESSCODE_RQST = "RQST";
    static final String GRP_PRIMARY = "primary";
    static final String GRP_VIEWING = "viewing";
    static final String GRP_CONTENT = "content";

    // All the channel constants
    static final String PROP_VERSION = "IRCC Version";
    static final String PROP_REGISTRATIONMODE = "Registration Mode";
    static final String CHANNEL_POWER = "power";
    static final String CHANNEL_CMD = "command";
    static final String CHANNEL_CONTENTURL = "contenturl";
    static final String CHANNEL_TEXT = "textfield";
    static final String CHANNEL_INTEXT = "intext";
    static final String CHANNEL_INBROWSER = "inbrowser";
    static final String CHANNEL_ISVIEWING = "isviewing";
    static final String CHANNEL_ID = "id";
    static final String CHANNEL_TITLE = "title";
    static final String CHANNEL_CLASS = "class";
    static final String CHANNEL_SOURCE = "source";
    static final String CHANNEL_SOURCE2 = "zone2source";
    static final String CHANNEL_MEDIATYPE = "mediatype";
    static final String CHANNEL_MEDIAFORMAT = "mediaformat";
    static final String CHANNEL_EDITION = "edition";
    static final String CHANNEL_DESCRIPTION = "description";
    static final String CHANNEL_GENRE = "genre";
    static final String CHANNEL_DURATION = "duration";
    static final String CHANNEL_RATING = "rating";
    static final String CHANNEL_DATERELEASE = "daterelease";
    static final String CHANNEL_DIRECTOR = "director";
    static final String CHANNEL_PRODUCER = "producer";
    static final String CHANNEL_SCREENWRITER = "screenwriter";
    static final String CHANNEL_ICONDATA = "image";
}
