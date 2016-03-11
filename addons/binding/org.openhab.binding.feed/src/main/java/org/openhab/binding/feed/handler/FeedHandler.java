/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feed.handler;

import static org.openhab.binding.feed.FeedBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.fetcher.FeedFetcher;
import com.rometools.fetcher.FetcherException;
import com.rometools.fetcher.impl.FeedFetcherCache;
import com.rometools.fetcher.impl.HashMapFeedInfoCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

/**
 * Feed binding integrates Web Feed functionality with openHAB.
 * It fetches Feed Data from given URL and regularly updates the data.
 * The binding can be used in combination with openHAB rules to provide XML feed data to different devices.
 * It supports a wide range of popular feed formats.
 *
 * The {@link FeedHandler } is responsible for handling commands, which are
 * sent to one of the channels and for the regular updates of the feed data.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class FeedHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FeedHandler.class);

    // Information about the feed
    private String urlString;
    /**
     * The refresh time specifies how often the binding checks for new entries in the feed.
     */
    private BigDecimal refreshTime;
    /**
     * Represents the output Feed format. All Feeds are converted to this format. Supported formats are
     * {@link #SUPPORTED_FEED_FORMATS}
     */
    private String feedFormat;
    /**
     * Specifies how many feed entries are stored in the state of the thinf.
     */
    private BigDecimal numberOfEntriesStored;

    /**
     * FeedFetcher is used to fetch data from feed.It supports conditional GET Requests.
     **/
    private FeedFetcher feedFetcher;
    ScheduledFuture<?> refreshTask;
    SyndFeed currentFeedState;

    public FeedHandler(Thing thing) {
        super(thing);
        FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
        currentFeedState = null;
    }

    @Override
    public void initialize() {

        checkConfiguration();

        startAutomaticRefresh();
    }

    /**
     * This method checks if the provided configuration is valid.
     * When invalid parameter is found, default value is assigned.
     */
    private void checkConfiguration() {
        logger.debug("Start reading Feed Thing configuration.");
        Configuration configuration = this.thing.getConfiguration();
        // It is not necessary to check if the URL is valid, this will be done in fetchFeedData() method
        urlString = (String) configuration.get(URL);
        try {
            refreshTime = (BigDecimal) configuration.get(REFRESH_TIME);
        } catch (Exception e) {
            refreshTime = DEFAULT_REFRESH_TIME;
            logger.debug("Can't read refresh time value. Falling back to default value: {}", REFRESH_TIME);
        }

        feedFormat = (String) configuration.get(FEED_FORMAT);
        if (!SUPPORTED_FEED_FORMATS.contains(feedFormat)) {
            feedFormat = DEFAULT_FEED_FORMAT;
            logger.debug("This format is not supported. Falling back to default value: {}", DEFAULT_FEED_FORMAT);
        }

        try {
            numberOfEntriesStored = (BigDecimal) configuration.get(NUMBER_OF_ENTRIES);
        } catch (Exception e) {
            numberOfEntriesStored = DEFAULT_NUMBER_OF_ENTRIES;
            logger.debug("Can't read number of entries. Falling back to default value: {}", DEFAULT_NUMBER_OF_ENTRIES);
        }
    }

    private void startAutomaticRefresh() {

        Runnable refresher = new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        };

        refreshTask = scheduler.scheduleAtFixedRate(refresher, 0, refreshTime.intValue(), TimeUnit.MINUTES);

    }

    private void refresh() {
        SyndFeed feed = fetchFeedData(urlString);
        boolean feedUpdated = updateFeedIfChanged(feed);

        if (feedUpdated) {
            String content = getFeedContent(currentFeedState, numberOfEntriesStored.intValue());
            updateState(FEED_CHANNEL, new StringType(content));
        }
    }

    /**
     * This method updates the {@link #currentFeedState}, only if there are changes on the server, since the last check.
     * It compares the content on the server with the local
     * stored {@link #currentFeedState} in the {@link FeedHandler}.
     *
     * @return <code>true</code> if new content is available on the server since the last update or <code>false</code>
     *         otherwise
     */
    private synchronized boolean updateFeedIfChanged(SyndFeed newFeedState) {
        if (newFeedState != null && !newFeedState.equals(currentFeedState)) {
            currentFeedState = newFeedState;
            logger.debug("Content updated!");
            return true;
        }
        logger.debug("Content is up to date!");
        return false;

    }

    /**
     * This method tries to make connection with the server and fetch data from the feed.
     * The status of the feed thing is set to {@link ThingStatus#ONLINE}, if the fetching was successful.
     * Otherwise the status will be set to {@link ThingStatus#OFFLINE} with
     * {@link ThingStatusDetail#CONFIGURATION_ERROR} or
     * {@link ThingStatusDetail#COMMUNICATION_ERROR} and adequate message.
     *
     * @param urlString - URL of the Feed
     * @return {@link SyndFeed} instance with the feed data, if the connection attempt was successful and
     *         <code>null</code> otherwise
     */
    private SyndFeed fetchFeedData(String urlString) {
        try {
            URL url = new URL(urlString);
            SyndFeed feed = null;
            feed = feedFetcher.retrieveFeed(url);
            logger.debug("Connection to feed successful");
            if (this.thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            return feed;
        } catch (MalformedURLException e) {
            logger.warn("Url '{}' is not valid: {}", urlString, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return null;
        } catch (IOException e) {
            logger.warn("Error accessing feed: " + urlString, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            logger.warn("Feed URL is null", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return null;
        } catch (FeedException e) {
            logger.warn("Feed is not valid: " + urlString, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return null;
        } catch (FetcherException e) {
            logger.warn("HTTP error occured:", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a String with the XML representation for the given {@link SyndFeed} in one of the specified formats
     * {@link #SUPPORTED_FEED_FORMATS}.
     *
     * @param numberOfEntries - number of {@link SyndEntry} or all entries, if they are less than this number
     * @param feed
     * @return <code>String</code> containing the XML data or <code>null</code> if XML can not be created
     */

    private String getFeedContent(SyndFeed feed, int numberOfEntries) {
        List<SyndEntry> allEntries = feed.getEntries();

        if (allEntries.size() > numberOfEntries) {
            /*
             * The entries are stored in the SyndFeed object in the following order -
             * the newest entry has index 0. The order is determined from the time the entry was posted, not the
             * published date of the entry.
             */
            allEntries = allEntries.subList(0, numberOfEntries);
            feed.setEntries(allEntries);
            logger.debug("Content will be generated from the first {} feed entries.", numberOfEntries);
        }
        return getFeedContent(feed);

    }

    /**
     * Creates a String with the XML representation for the given {@link SyndFeed} in one of the specified formats
     * {@link #SUPPORTED_FEED_FORMATS}.This method sets the Feed format as well.
     *
     * @param feed
     * @return <code>String</code> containing the XML data or <code>null</code> if XML can not be created
     */
    private String getFeedContent(SyndFeed feed) {
        feed.setFeedType(feedFormat);

        SyndFeedOutput outputBuilder = new SyndFeedOutput();
        String content = null;
        try {
            content = outputBuilder.outputString(feed);
        } catch (Exception e) {
            logger.error("The XML representation for the feed could not be created. {}", e.getMessage());
        }
        return content;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(FEED_CHANNEL)) {
                refresh();
            } else {
                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}. Supported command: REFRESH", command,
                    channelUID.getId());
        }
    }

    @Override
    public void dispose() {
        refreshTask.cancel(true);
    }
}
