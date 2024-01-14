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
package org.openhab.binding.x.internal;

import static org.openhab.binding.x.internal.XBindingConstants.CHANNEL_LASTPOST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.x.internal.action.XActions;
import org.openhab.binding.x.internal.config.XConfig;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.v1.DirectMessage;
import twitter4j.v1.ResponseList;
import twitter4j.v1.Status;
import twitter4j.v1.StatusUpdate;

/**
 * The {@link XHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Hanson - Initial contribution
 */

@NonNullByDefault
public class XHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(XHandler.class);

    private XConfig config = new XConfig();

    private @Nullable ScheduledFuture<?> refreshTask;

    private static final int CHARACTER_LIMIT = 280;

    private static @Nullable Twitter client = null;
    boolean isProperlyConfigured = false;

    public XHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    // creates list of available Actions
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(XActions.class);
    }

    @Override
    public void initialize() {
        config = getConfigAs(XConfig.class);

        // create a New X/Twitter Client
        Twitter localClient = createClient();
        client = localClient;
        refresh();// Get latest status
        isProperlyConfigured = true;
        refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.MINUTES);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
        }
    }

    /**
     * Internal method for Getting X Status
     *
     */
    private void refresh() {
        try {
            if (!checkPrerequisites()) {
                return;
            }
            Twitter localClient = client;
            if (localClient != null) {
                ResponseList<Status> statuses = localClient.v1().timelines().getUserTimeline();
                if (!statuses.isEmpty()) {
                    updateState(CHANNEL_LASTPOST, StringType.valueOf(statuses.get(0).getText()));
                } else {
                    logger.debug("No Statuses Found");
                }
            }
        } catch (TwitterException e) {
            logger.debug("Error when trying to refresh X Account: {}", e.getMessage());
        }
    }

    /**
     * Internal method for sending a post, with or without image
     *
     * @param postTxt
     *            text string to be sent as a Post
     * @param fileToAttach
     *            the file to attach. May be null if no attached file.
     *
     * @return <code>true</code>, if sending the post has been successful and
     *         <code>false</code> in all other cases.
     */
    private boolean sendPost(final String postTxt, final @Nullable File fileToAttach) {
        if (!checkPrerequisites()) {
            return false;
        }
        // abbreviate the Post to meet the 280 character limit ...
        String abbreviatedPostTxt = abbreviateString(postTxt, CHARACTER_LIMIT);
        try {
            Twitter localClient = client;
            if (localClient != null) {
                // send the Post
                StatusUpdate status = StatusUpdate.of(abbreviatedPostTxt);
                if (fileToAttach != null && fileToAttach.isFile()) {
                    status = status.media(fileToAttach);
                }
                Status updatedStatus = localClient.v1().tweets().updateStatus(status);
                logger.debug("Successfully sent Post '{}'", updatedStatus.getText());
                updateState(CHANNEL_LASTPOST, StringType.valueOf(updatedStatus.getText()));
                return true;
            }
        } catch (TwitterException e) {
            logger.warn("Failed to send Post '{}' because of : {}", abbreviatedPostTxt, e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Sends a standard Post.
     *
     * @param postTxt
     *            text string to be sent as a Post
     *
     * @return <code>true</code>, if sending the post has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendPost(String postTxt) {
        if (!checkPrerequisites()) {
            return false;
        }
        return sendPost(postTxt, (File) null);
    }

    /**
     * Sends a Post with an image
     *
     * @param postTxt
     *            text string to be sent as a Post
     * @param postPicture
     *            the path of the picture that needs to be attached (either an url,
     *            either a path pointing to a local file)
     *
     * @return <code>true</code>, if sending the post has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendPost(String postTxt, String postPicture) {
        if (!checkPrerequisites()) {
            return false;
        }

        // prepare the image attachment
        File fileToAttach = null;
        boolean deleteTemporaryFile = false;
        if (postPicture.startsWith("http://") || postPicture.startsWith("https://")) {
            try {
                // we have a remote url and need to download the remote file to a temporary location
                Path tDir = Files.createTempDirectory("TempDirectory");
                String path = tDir + File.separator + "openhab-x-remote_attached_file" + "."
                        + getExtension(postPicture);

                // URL url = new URL(postPicture);
                fileToAttach = new File(path);
                deleteTemporaryFile = true;

                RawType rawPicture = HttpUtil.downloadImage(postPicture);
                if (rawPicture != null) {
                    try (FileOutputStream fos = new FileOutputStream(path)) {
                        fos.write(rawPicture.getBytes(), 0, rawPicture.getBytes().length);
                    } catch (FileNotFoundException ex) {
                        logger.debug("Could not create {} in temp dir. {}", path, ex.getMessage());
                    } catch (IOException ex) {
                        logger.debug("Could not write {} to temp dir. {}", path, ex.getMessage());
                    }
                } else {
                    logger.debug("Could not download post file from {}", postPicture);
                }
            } catch (IOException ex) {
                logger.debug("Could not write {} to temp dir. {}", postPicture, ex.getMessage());
            }
        } else {
            // we have a local file and can just use it directly
            fileToAttach = new File(postPicture);
        }

        if (fileToAttach != null && fileToAttach.isFile()) {
            logger.debug("Image '{}' correctly found, will be included in post", postPicture);
        } else {
            logger.warn("Image '{}' not found, will only post text", postPicture);
        }

        // send the Post
        boolean result = sendPost(postTxt, fileToAttach);
        // delete temp file (if needed)
        if (deleteTemporaryFile) {
            if (fileToAttach != null) {
                try {
                    fileToAttach.delete();
                } catch (final Exception ignored) {
                    return false;
                }
            }
        }
        return result;
    }

    /**
     * Sends a DirectMessage
     *
     * @param recipientId
     *            recipient ID of the twitter user
     * @param messageTxt
     *            text string to be sent as a Direct Message
     *
     * @return <code>true</code>, if sending the direct message has been successful and
     *         <code>false</code> in all other cases.
     */
    public boolean sendDirectMessage(String recipientId, String messageTxt) {
        if (!checkPrerequisites()) {
            return false;
        }

        try {
            Twitter localClient = client;
            if (localClient != null) {
                // abbreviate the Post to meet the allowed character limit ...
                String abbreviatedMessageTxt = abbreviateString(messageTxt, CHARACTER_LIMIT);
                // send the direct message
                DirectMessage message = localClient.v1().directMessages().sendDirectMessage(recipientId,
                        abbreviatedMessageTxt);
                logger.debug("Successfully sent direct message '{}' to @'{}'", message.getText(),
                        message.getRecipientId());
                return true;
            }
        } catch (TwitterException e) {
            logger.warn("Failed to send Direct Message '{}' because of :'{}'", messageTxt, e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * check if X account was created with prerequisites
     * 
     * @return <code>true</code>, if X account was initialized
     *         <code>false</code> in all other cases.
     */
    private boolean checkPrerequisites() {
        if (client == null) {
            logger.debug("X client is not yet configured > execution aborted!");
            return false;
        }
        if (!isProperlyConfigured) {
            logger.debug("X client is not yet configured > execution aborted!");
            return false;
        }
        return true;
    }

    /**
     * Creates and returns a Twitter4J Twitter client.
     *
     * @return a new instance of a Twitter4J Twitter client.
     */
    private twitter4j.Twitter createClient() {
        Twitter client = Twitter.newBuilder().oAuthConsumer(config.consumerKey, config.consumerSecret)
                .oAuthAccessToken(config.accessToken, config.accessTokenSecret).build();

        return client;
    }

    public static String abbreviateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength);
        }
    }

    public static String getExtension(String filename) {
        if (filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        }
        return new String();
    }
}
