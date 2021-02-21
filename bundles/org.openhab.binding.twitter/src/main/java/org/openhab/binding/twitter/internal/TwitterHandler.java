/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.twitter.internal;

import static org.openhab.binding.twitter.internal.TwitterBindingConstants.CHANNEL_LASTTWEET;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.twitter.internal.config.TwitterConfig;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * The {@link TwitterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Hanson - Initial contribution
 */
// @NonNullByDefault
public class TwitterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TwitterHandler.class);

    private @Nullable TwitterConfig config;
    @SuppressWarnings("unused")
    private @Nullable ScheduledFuture<?> refreshTask;

    private static final int CHARACTER_LIMIT = 280;

    static twitter4j.Twitter client = null;
    boolean isProperlyConfigured = false;

    public TwitterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(TwitterConfig.class);

        try {
            client = createClient();
            refresh();
            isProperlyConfigured = true;
            refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.MINUTES);
            updateStatus(ThingStatus.ONLINE);
        } catch (UnhandledException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void refresh() {
        try {
            if (!checkPrerequisites()) {
                return;
            }
            if (client != null) {
                ResponseList<Status> statuses = client.getUserTimeline();
                if (statuses.size() > 0) {
                    updateState(CHANNEL_LASTTWEET, StringType.valueOf(statuses.get(0).getText()));
                }
            }
        } catch (TwitterException e) {
            logger.info("error when trying to refresh Twitter Account: {}", e.getMessage());
        }
    }

    /**
     * Internal method for sending a tweet, with or without image
     *
     * @param tweetTxt
     *            text string to be sent as a Tweet
     * @param fileToAttach
     *            the file to attach. May be null if no attached file.
     *
     * @return <code>true</code>, if sending the tweet has been successful and
     *         <code>false</code> in all other cases.
     */
    private boolean sendTweet(final String tweetTxt, final File fileToAttach) {
        // abbreviate the Tweet to meet the 140 character limit ...
        String abbreviatedTweetTxt = StringUtils.abbreviate(tweetTxt, CHARACTER_LIMIT);
        try {
            // send the Tweet
            StatusUpdate status = new StatusUpdate(abbreviatedTweetTxt);
            if (fileToAttach != null && fileToAttach.isFile()) {
                status.setMedia(fileToAttach);
            }
            Status updatedStatus = client.updateStatus(status);
            logger.debug("Successfully sent Tweet '{}'", updatedStatus.getText());
            return true;
        } catch (TwitterException e) {
            logger.warn("Failed to send Tweet '{}' because of : {}", abbreviatedTweetTxt, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Sends a standard Tweet.
     *
     * @param tweetTxt
     *            text string to be sent as a Tweet
     *
     * @return <code>true</code>, if sending the tweet has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendTweet(String tweetTxt) {
        if (!checkPrerequisites()) {
            return false;
        }
        return sendTweet(tweetTxt, (File) null);
    }

    /**
     * Sends a Tweet with an image
     *
     * @param tweetTxt
     *            text string to be sent as a Tweet
     * @param tweetPicture
     *            the path of the picture that needs to be attached (either an url,
     *            either a path pointing to a local file)
     *
     * @return <code>true</code>, if sending the tweet has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendTweet(String tweetTxt, String tweetPicture) {
        if (!checkPrerequisites()) {
            return false;
        }

        // prepare the image attachment
        File fileToAttach = null;
        boolean deleteTemporaryFile = false;
        if (StringUtils.startsWith(tweetPicture, "http://") || StringUtils.startsWith(tweetPicture, "https://")) {
            // we have a remote url and need to download the remote file to a temporary location
            String tDir = System.getProperty("java.io.tmpdir");
            String path = tDir + File.separator + "openhab-twitter-remote_attached_file" + "."
                    + FilenameUtils.getExtension(tweetPicture);
            try {
                URL url = new URL(tweetPicture);
                fileToAttach = new File(path);
                deleteTemporaryFile = true;
                FileUtils.copyURLToFile(url, fileToAttach);
            } catch (MalformedURLException e) {
                logger.warn("Can't read file from '{}'", tweetPicture, e);
            } catch (IOException e) {
                logger.warn("Can't save file from '{}' to '{}'", tweetPicture, path, e);
            }
        } else {
            // we have a local file and can just use it directly
            fileToAttach = new File(tweetPicture);
        }

        if (fileToAttach != null && fileToAttach.isFile()) {
            logger.debug("Image '{}' correctly found, will be included in tweet", tweetPicture);
        } else {
            logger.warn("Image '{}' not found, will only tweet text", tweetPicture);
        }

        // send the Tweet
        boolean result = sendTweet(tweetTxt, fileToAttach);
        // delete temp file (if needed)
        if (deleteTemporaryFile) {
            FileUtils.deleteQuietly(fileToAttach);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    public boolean sendDirectMessage(String recipientId, String messageTxt) {
        if (!checkPrerequisites()) {
            return false;
        }

        try {
            // abbreviate the Tweet to meet the allowed character limit ...
            String abbreviatedMessageTxt = StringUtils.abbreviate(messageTxt, CHARACTER_LIMIT);
            // send the direct message
            DirectMessage message = client.sendDirectMessage(recipientId, abbreviatedMessageTxt);
            logger.debug("Successfully sent direct message '{}' to @'{}'", message.getText(),
                    message.getRecipientScreenName());
            return true;
        } catch (TwitterException e) {
            logger.warn("Failed to send Direct Message '{}' because of :'{}'", messageTxt, e.getLocalizedMessage());
            return false;
        }
    }

    private boolean checkPrerequisites() {
        if (client == null) {
            logger.debug("Twitter client is not yet configured > execution aborted!");
            return false;
        }
        if (!isProperlyConfigured) {
            logger.debug("Twitter client is not yet configured > execution aborted!");
            return false;
        }
        return true;
    }

    /**
     * Creates and returns a Twitter4J Twitter client.
     *
     * @return a new instance of a Twitter4J Twitter client.
     */
    @SuppressWarnings("null")
    private twitter4j.Twitter createClient() {
        twitter4j.Twitter client = TwitterFactory.getSingleton();
        client.setOAuthConsumer(config.consumerKey, config.consumerSecret);
        client.setOAuthAccessToken(new AccessToken(config.accessToken, config.accessTokenSecret));
        return client;
    }
}
