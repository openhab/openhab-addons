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
import org.openhab.binding.feed.FeedBindingConstants;
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
 *
 * The {@link FeedHandler } is responsible for handling commands, which are
 * sent to one of the channels and for the regular updates of the feed data.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class FeedHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FeedHandler.class);

    private String urlString;
    private BigDecimal refreshTime;
    /**
     * All Feeds are converted to this format. Supported formats are {@link #SUPPORTED_FEED_FORMATS}
     */
    private String outputFeedFormat;
    private BigDecimal numberOfEntriesStored;

    /**
     * FeedFetcher is used to fetch data from feed. It supports conditional GET Requests.
     **/
    private FeedFetcher feedFetcher;
    private ScheduledFuture<?> refreshTask;
    private SyndFeed currentFeedState;

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
        Configuration configuration = editConfiguration();

        // It is not necessary to check if the URL is valid, this will be done in fetchFeedData() method
        urlString = (String) configuration.get(URL);

        try {
            refreshTime = (BigDecimal) configuration.get(REFRESH_TIME);
            if (refreshTime.intValue() <= 0) {
                throw new IllegalArgumentException("Refresh time must be positive number!");
            }
        } catch (Exception e) {
            logger.warn("Refresh time [{}] is not valid. Falling back to default value: {}. {}", refreshTime,
                    DEFAULT_REFRESH_TIME, e.getMessage());
            refreshTime = DEFAULT_REFRESH_TIME;
            configuration.put(FeedBindingConstants.REFRESH_TIME, DEFAULT_REFRESH_TIME);

        }

        outputFeedFormat = (String) configuration.get(FEED_FORMAT);
        if (!SUPPORTED_FEED_FORMATS.contains(outputFeedFormat)) {
            logger.warn("Format [{}] is not supported. Falling back to default value: {}.", outputFeedFormat,
                    DEFAULT_FEED_FORMAT);
            outputFeedFormat = DEFAULT_FEED_FORMAT;
            configuration.put(FeedBindingConstants.FEED_FORMAT, DEFAULT_FEED_FORMAT);
        }

        try {
            numberOfEntriesStored = (BigDecimal) configuration.get(NUMBER_OF_ENTRIES);
            if (numberOfEntriesStored.intValue() <= 0) {
                throw new IllegalArgumentException("Number of entries must be positive number!");
            }
        } catch (Exception e) {
            logger.warn("Number of entries [{}] is invalid. Falling back to default value: {}. {}",
                    numberOfEntriesStored, DEFAULT_NUMBER_OF_ENTRIES, e.getMessage());

            numberOfEntriesStored = DEFAULT_NUMBER_OF_ENTRIES;
            configuration.put(FeedBindingConstants.NUMBER_OF_ENTRIES, DEFAULT_NUMBER_OF_ENTRIES);
        }

        updateConfiguration(configuration);
    }

    private void startAutomaticRefresh() {

        Runnable refresher = new Runnable() {
            @Override
            public void run() {
                refreshFeedState();
            }
        };

        refreshTask = scheduler.scheduleAtFixedRate(refresher, 0, refreshTime.intValue(), TimeUnit.MINUTES);

    }

    private void refreshFeedState() {

        SyndFeed feed = fetchFeedData(urlString);
        boolean feedUpdated = updateFeedIfChanged(feed);

        if (feedUpdated) {
            String content = getFeedContent(currentFeedState, numberOfEntriesStored.intValue());
            updateState(FEED_CHANNEL, new StringType(content));
            logger.debug("Content updated !");
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
        // SyndFeed class has implementation of equals ()
        if (newFeedState != null && !newFeedState.equals(currentFeedState)) {
            currentFeedState = newFeedState;
            logger.debug("New content available!");
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
            updateStatus(ThingStatus.ONLINE);
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
            logger.warn("Feed content is not valid: " + urlString, e);
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
             * published time of the entry.
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
     * @return <code>String</code> containing the XML data or <code>null</code> if XML is invalid for the selected
     *         {@link #outputFeedFormat}
     */
    private String getFeedContent(SyndFeed feed) {
        feed.setFeedType(outputFeedFormat);

        if (outputFeedFormat.equals("rss_0.91N") || outputFeedFormat.equals("rss_0.91U")) {
            // RSS 0.91 has required language attribute. Default value is assigned, because if this value is missing,
            // the
            // conversion to RSS 0.91 will fail
            if (feed.getLanguage() == null) {
                feed.setLanguage("en-us");
            }
        }

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
                refreshFeedState();
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
        if (refreshTask.isDone()) {
            logger.error("Refresh task is not terminated properly");
        }
        refreshTask.cancel(true);
    }
}
