/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feed;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FeedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class FeedBindingConstants {

    public static final String BINDING_ID = "feed";

    // List of all Thing Type UIDs
    public final static ThingTypeUID FEED_THING_TYPE_UID = new ThingTypeUID("feed", "feed");

    // List of all Channel IDs
    /**
     * Contains title, description and published date for the last feed entry.
     */
    public final static String CHANNEL_LATEST_ENTRY = "latest-entry";

    /**
     * Description of the feed
     */
    public final static String CHANNEL_DESCRIPTION = "description";

    /**
     * The last update date of the feed
     */
    public final static String CHANNEL_LAST_UPDATE = "last-update";

    /**
     * The name of the feed author, if author is present
     */
    public final static String CHANNEL_AUTHOR = "author";

    /**
     * The title of the feed
     */
    public final static String CHANNEL_TITLE = "title";

    /**
     * Number of entries in the feed
     */
    public final static String CHANNEL_NUMBER_OF_ENTRIES = "number-of-entries";

    // Configuration parameters
    /**
     * * The URL of the feed document.
     */
    public final static String URL = "URL";

    /**
     * The refresh time in minutes.
     */
    public final static String REFRESH_TIME = "refresh";

    /**
     * The default auto refresh time in minutes.
     */
    public final static BigDecimal DEFAULT_REFRESH_TIME = new BigDecimal(20);
}
