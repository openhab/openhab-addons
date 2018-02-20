/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
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
    public static final ThingTypeUID FEED_THING_TYPE_UID = new ThingTypeUID("feed", "feed");

    // List of all Channel IDs
    /**
     * Contains the published date of the last feed entry.
     */
    public static final String CHANNEL_LATEST_PUBLISHED_DATE = "latest-date";

    /**
     * Contains the title of the last feed entry.
     */
    public static final String CHANNEL_LATEST_TITLE = "latest-title";

    /**
     * Contains the description of last feed entry.
     */
    public static final String CHANNEL_LATEST_DESCRIPTION = "latest-description";

    /**
     * Description of the feed.
     */
    public static final String CHANNEL_DESCRIPTION = "description";

    /**
     * The last update date of the feed.
     */
    public static final String CHANNEL_LAST_UPDATE = "last-update";

    /**
     * The name of the feed author, if author is present.
     */
    public static final String CHANNEL_AUTHOR = "author";

    /**
     * The title of the feed.
     */
    public static final String CHANNEL_TITLE = "title";

    /**
     * Number of entries in the feed
     */
    public static final String CHANNEL_NUMBER_OF_ENTRIES = "number-of-entries";

    // Configuration parameters
    /**
     * * The URL of the feed document.
     */
    public static final String URL = "URL";

    /**
     * The refresh time in minutes.
     */
    public static final String REFRESH_TIME = "refresh";

    /**
     * The default auto refresh time in minutes.
     */
    public static final BigDecimal DEFAULT_REFRESH_TIME = new BigDecimal(20);

    /**
     * The minimum refresh time in milliseconds. Any REFRESH command send to a Thing, before this time has expired, will
     * not trigger an attempt to dowload new data form the server.
     **/
    public static final int MINIMUM_REFRESH_TIME = 3000;
}
