/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feed;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

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
    public final static String FEED_CHANNEL = "feed-channel";

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
     * The output feed format.
     */
    public final static String FEED_FORMAT = "format";
    /**
     * The number of entries stored and tracked for updates.
     */
    public final static String NUMBER_OF_ENTRIES = "length";
    /**
     * Feed formats, supported form the binding.
     */
    public final static Set<String> SUPPORTED_FEED_FORMATS = Sets.newHashSet("rss_0.9", "rss_0.92", "rss_0.93",
            "rss_0.94", "rss_1.0", "rss_2.0", "atom_0.3", "atom_1.0", "rss_0.91N", "rss_0.91U");

    // Default configuration parameters
    /**
     * The default output feed format.
     */
    public final static String DEFAULT_FEED_FORMAT = "atom_1.0";
    /**
     * The default auto refresh time in minutes.
     */
    public final static BigDecimal DEFAULT_REFRESH_TIME = new BigDecimal(20);
    /**
     * The default number of feed entries stored and tracked for updates.
     */
    public final static BigDecimal DEFAULT_NUMBER_OF_ENTRIES = new BigDecimal(20);

}
