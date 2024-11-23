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
package org.openhab.binding.feed.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FeedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Juergen Pabel - Added enclosure channel
 */
@NonNullByDefault
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
     * Contains the description of the last feed entry.
     */
    public static final String CHANNEL_LATEST_DESCRIPTION = "latest-description";

    /**
     * Contains the link to the last feed entry.
     */
    public static final String CHANNEL_LATEST_LINK = "latest-link";

    /**
     * Contains the enclosure link to the last feed entry.
     */
    public static final String CHANNEL_LATEST_ENCLOSURE = "latest-enclosure";

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
    public static final long DEFAULT_REFRESH_TIME = 20;

    /**
     * The minimum refresh time in milliseconds. Any REFRESH command send to a Thing, before this time has expired, will
     * not trigger an attempt to download new data from the server.
     **/
    public static final int MINIMUM_REFRESH_TIME = 3000;
}
