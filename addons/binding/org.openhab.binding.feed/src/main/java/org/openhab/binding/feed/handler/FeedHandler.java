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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
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

/**
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
    }

    /**
     * This method checks if the provided configuration is valid.
     * When invalid parameter is found, default value is assigned.
     */
    private void checkConfiguration() {
        logger.debug("Start reading Feed Thing configuration.");
        Configuration configuration = getConfig();

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
        }

        outputFeedFormat = (String) configuration.get(FEED_FORMAT);
        if (!SUPPORTED_FEED_FORMATS.contains(outputFeedFormat)) {
            logger.warn("Format [{}] is not supported. Falling back to default value: {}.", outputFeedFormat,
                    DEFAULT_FEED_FORMAT);
            outputFeedFormat = DEFAULT_FEED_FORMAT;
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
        }
    }

    private void startAutomaticRefresh() {

        Runnable refresher = new Runnable() {
            @Override
            public void run() {
                refreshFeedState();
            }
        };

        refreshTask = scheduler.scheduleAtFixedRate(refresher, 0, refreshTime.intValue(), TimeUnit.MINUTES);
        logger.debug("Start automatic refresh at {} minutes", refreshTime.intValue());
    }

    private void refreshFeedState() {
        SyndFeed feed = fetchFeedData(urlString);
        boolean feedUpdated = updateFeedIfChanged(feed);

        if (feedUpdated) {
            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                publishChannelIfLinked(channel.getUID());
            }
        }
    }

    private void publishChannelIfLinked(ChannelUID channelUID) {

        String channelID = channelUID.getId();
        if (isLinked(channelID)) {
            State state = null;
            switch (channelID) {
                case CHANNEL_LATEST_CONTENT:
                    String content = getLatestContent(currentFeedState, numberOfEntriesStored.intValue());
                    state = new StringType(content);
                    break;
                case CHANNEL_AUTHOR:
                    String author = currentFeedState.getAuthor();
                    state = new StringType(author);
                    break;
                case CHANNEL_DESCRIPTION:
                    String description = currentFeedState.getDescription();
                    state = new StringType(description);
                    break;
                case CHANNEL_TITLE:
                    String title = currentFeedState.getTitle();
                    state = new StringType(title);
                    break;
                case CHANNEL_PUBLISHED_DATE:
                    Date pubDate = currentFeedState.getPublishedDate();
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(pubDate);
                    state = new DateTimeType(calendar);
                    break;
                case CHANNEL_NUMBER_OF_ENTRIES:
                    int numberOfEntries = currentFeedState.getEntries().size();
                    state = new DecimalType(numberOfEntries);
                    break;
            }
            if (state != null) {
                updateState(channelID, state);
            } else {
                logger.debug("Can not update channel {} - information is missing !", channelID);
            }
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
     * TODO
     */
    private String getLatestContent(SyndFeed feed, int numberOfEntries) {
        List<SyndEntry> allEntries = feed.getEntries();

        if (allEntries.size() > numberOfEntries) {
            /*
             * The entries are stored in the SyndFeed object in the following order -
             * the newest entry has index 0. The order is determined from the time the entry was posted, not the
             * published time of the entry.
             */
            allEntries = allEntries.subList(0, numberOfEntries);

            logger.debug("Content will be generated from the first {} feed entries.", numberOfEntries);
        }

        feed.setFeedType(outputFeedFormat);

        if (outputFeedFormat.equals("rss_0.91N") || outputFeedFormat.equals("rss_0.91U")) {
            // RSS 0.91 has required language attribute. Default value is assigned, because if this value is missing,
            // the conversion to RSS 0.91 will fail
            if (feed.getLanguage() == null) {
                feed.setLanguage("en-us");
            }
        }
        return getLatestContent(allEntries);
    }

    /**
     * TODO
     */
    private String getLatestContent(List<SyndEntry> entries) {
        StringBuilder latestContent = new StringBuilder();
        for (SyndEntry entry : entries) {
            String title = entry.getTitle();
            String description = entry.getDescription().getValue();
            Date publishedDate = entry.getPublishedDate();
            String publishedDateString = DateFormat.getInstance().format(publishedDate);
            // TODO new lines are not displayed in basic UI.
            // Separator is hard coded.
            String entryAsString = String.format("Title: %s%nDate: %s%nDescription: %s%n#", title, publishedDateString,
                    description);
            latestContent.append(entryAsString);
        }
        return latestContent.toString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            publishChannelIfLinked(channelUID);
        } else {
            logger.debug("Command {} is not supported for channel: {}. Supported command: REFRESH", command,
                    channelUID.getId());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (refreshTask == null) {
            startAutomaticRefresh();
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // TODO if all channels are unlinked the automatic refresh should stop ?
    }

    @Override
    public void dispose() {
        refreshTask.cancel(true);
    }
}
