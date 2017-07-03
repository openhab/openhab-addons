/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.SonyBindingConstants;

// TODO: Auto-generated Javadoc
/**
 * The class provides all the constants specific to the Ircc system.
 *
 * @author Tim Roberts - Initial contribution
 */
public class IrccConstants {

    /** The Constant THING_TYPE_IRCC. */
    // The thing constants
    public final static ThingTypeUID THING_TYPE_IRCC = new ThingTypeUID(SonyBindingConstants.BINDING_ID, "ircc");

    /** The Constant ACCESSCODE_RQST. */
    public final static String ACCESSCODE_RQST = "RQST";

    /** The Constant GRP_PRIMARY. */
    public final static String GRP_PRIMARY = "primary";

    /** The Constant GRP_VIEWING. */
    public final static String GRP_VIEWING = "viewing";

    /** The Constant GRP_CONTENT. */
    public final static String GRP_CONTENT = "content";

    /** The Constant PROP_VERSION. */
    // All the channel constants
    public final static String PROP_VERSION = "IRCC Version";

    /** The Constant PROP_REGISTRATIONMODE. */
    public final static String PROP_REGISTRATIONMODE = "Registration Mode";

    /** The Constant CHANNEL_POWER. */
    public final static String CHANNEL_POWER = "power";

    /** The Constant CHANNEL_CMD. */
    public final static String CHANNEL_CMD = "command";

    /** The Constant CHANNEL_CONTENTURL. */
    public final static String CHANNEL_CONTENTURL = "contenturl";

    /** The Constant CHANNEL_TEXT. */
    public final static String CHANNEL_TEXT = "textfield";

    /** The Constant CHANNEL_INTEXT. */
    public final static String CHANNEL_INTEXT = "intext";

    /** The Constant CHANNEL_INBROWSER. */
    public final static String CHANNEL_INBROWSER = "inbrowser";

    /** The Constant CHANNEL_ISVIEWING. */
    public final static String CHANNEL_ISVIEWING = "isviewing";

    /** The Constant CHANNEL_ID. */
    public final static String CHANNEL_ID = "id";

    /** The Constant CHANNEL_TITLE. */
    public final static String CHANNEL_TITLE = "title";

    /** The Constant CHANNEL_CLASS. */
    public final static String CHANNEL_CLASS = "class";

    /** The Constant CHANNEL_SOURCE. */
    public final static String CHANNEL_SOURCE = "source";

    /** The Constant CHANNEL_SOURCE2. */
    public final static String CHANNEL_SOURCE2 = "zone2source";

    /** The Constant CHANNEL_MEDIATYPE. */
    public final static String CHANNEL_MEDIATYPE = "mediatype";

    /** The Constant CHANNEL_MEDIAFORMAT. */
    public final static String CHANNEL_MEDIAFORMAT = "mediaformat";

    /** The Constant CHANNEL_EDITION. */
    public final static String CHANNEL_EDITION = "edition";

    /** The Constant CHANNEL_DESCRIPTION. */
    public final static String CHANNEL_DESCRIPTION = "description";

    /** The Constant CHANNEL_GENRE. */
    public final static String CHANNEL_GENRE = "genre";

    /** The Constant CHANNEL_DURATION. */
    public final static String CHANNEL_DURATION = "duration";

    /** The Constant CHANNEL_RATING. */
    public final static String CHANNEL_RATING = "rating";

    /** The Constant CHANNEL_DATERELEASE. */
    public final static String CHANNEL_DATERELEASE = "daterelease";

    /** The Constant CHANNEL_DIRECTOR. */
    public final static String CHANNEL_DIRECTOR = "director";

    /** The Constant CHANNEL_PRODUCER. */
    public final static String CHANNEL_PRODUCER = "producer";

    /** The Constant CHANNEL_SCREENWRITER. */
    public final static String CHANNEL_SCREENWRITER = "screenwriter";

    /** The Constant CHANNEL_ICONDATA. */
    public final static String CHANNEL_ICONDATA = "image";

}
