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
package org.openhab.binding.pushbullet.internal.handler;

import static org.openhab.binding.pushbullet.internal.PushbulletBindingConstants.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pushbullet.internal.PushbulletConfiguration;
import org.openhab.binding.pushbullet.internal.PushbulletHttpClient;
import org.openhab.binding.pushbullet.internal.action.PushbulletActions;
import org.openhab.binding.pushbullet.internal.exception.PushbulletApiException;
import org.openhab.binding.pushbullet.internal.exception.PushbulletAuthenticationException;
import org.openhab.binding.pushbullet.internal.model.PushRequest;
import org.openhab.binding.pushbullet.internal.model.PushResponse;
import org.openhab.binding.pushbullet.internal.model.PushType;
import org.openhab.binding.pushbullet.internal.model.UploadRequest;
import org.openhab.binding.pushbullet.internal.model.UploadResponse;
import org.openhab.binding.pushbullet.internal.model.User;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PushbulletHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Jeremy Setton - Add link and file push type support
 */
@NonNullByDefault
public class PushbulletHandler extends BaseThingHandler {

    private static final Pattern CHANNEL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final Logger logger = LoggerFactory.getLogger(PushbulletHandler.class);

    private final PushbulletHttpClient httpClient;

    private int maxUploadSize;

    public PushbulletHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = new PushbulletHttpClient(httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // do nothing
    }

    @Override
    public void initialize() {
        logger.debug("Starting {}", thing.getUID());

        PushbulletConfiguration config = getConfigAs(PushbulletConfiguration.class);

        if (config.getAccessToken().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Undefined access token.");
            return;
        }

        httpClient.setConfiguration(config);

        scheduler.execute(() -> retrieveAccountInfo());

        updateStatus(ThingStatus.UNKNOWN);
    }

    private void retrieveAccountInfo() {
        try {
            User user = httpClient.executeRequest(API_ENDPOINT_USERS_ME, User.class);

            maxUploadSize = Objects.requireNonNullElse(user.getMaxUploadSize(), MAX_UPLOAD_SIZE);

            logger.debug("Set maximum upload size for {} to {} bytes", thing.getUID(), maxUploadSize);

            updateProperty(PROPERTY_NAME, user.getName());
            updateProperty(PROPERTY_EMAIL, user.getEmail());

            logger.debug("Updated properties for {} to {}", thing.getUID(), thing.getProperties());

            updateStatus(ThingStatus.ONLINE);
        } catch (PushbulletAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid access token.");
        } catch (PushbulletApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to retrieve account info.");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PushbulletActions.class);
    }

    /**
     * Sends a push note
     *
     * @param recipient the recipient
     * @param title the title
     * @param message the message
     * @return true if successful
     */
    public boolean sendPushNote(@Nullable String recipient, @Nullable String title, String message) {
        PushRequest request = newPushRequest(recipient, title, message, PushType.NOTE);

        return sendPush(request);
    }

    /**
     * Sends a push link
     *
     * @param recipient the recipient
     * @param title the title
     * @param message the message
     * @param url the message url
     * @return true if successful
     */
    public boolean sendPushLink(@Nullable String recipient, @Nullable String title, @Nullable String message,
            String url) {
        PushRequest request = newPushRequest(recipient, title, message, PushType.LINK);
        request.setUrl(url);

        return sendPush(request);
    }

    /**
     * Sends a push file
     *
     * @param recipient the recipient
     * @param title the title
     * @param message the message
     * @param content the file content
     * @param fileName the file name
     * @return true if successful
     */
    public boolean sendPushFile(@Nullable String recipient, @Nullable String title, @Nullable String message,
            String content, @Nullable String fileName) {
        UploadResponse upload = uploadFile(content, fileName);
        if (upload == null) {
            return false;
        }

        PushRequest request = newPushRequest(recipient, title, message, PushType.FILE);
        request.setFileName(upload.getFileName());
        request.setFileType(upload.getFileType());
        request.setFileUrl(upload.getFileUrl());

        return sendPush(request);
    }

    /**
     * Helper method to send a push request
     *
     * @param request the push request
     * @return true if successful
     */
    private boolean sendPush(PushRequest request) {
        logger.debug("Sending push notification for {}", thing.getUID());
        logger.debug("Push Request: {}", request);

        try {
            httpClient.executeRequest(API_ENDPOINT_PUSHES, request, PushResponse.class);
            return true;
        } catch (PushbulletApiException e) {
            return false;
        }
    }

    /**
     * Helper method to upload a file to use in push message
     *
     * @param content the file content
     * @param fileName the file name
     * @return the upload response if successful, otherwise null
     */
    private @Nullable UploadResponse uploadFile(String content, @Nullable String fileName) {
        RawType data = getContentData(content);
        if (data == null) {
            logger.warn("Failed to get content data from '{}'", content);
            return null;
        }

        logger.debug("Content Data: {}", data);

        int size = data.getBytes().length;
        if (size > maxUploadSize) {
            logger.warn("Content data size {} is greater than maximum upload size {}", size, maxUploadSize);
            return null;
        }

        try {
            UploadRequest request = new UploadRequest();
            request.setFileName(fileName != null ? fileName : getContentFileName(content));
            request.setFileType(data.getMimeType());

            logger.debug("Upload Request: {}", request);

            UploadResponse response = httpClient.executeRequest(API_ENDPOINT_UPLOAD_REQUEST, request,
                    UploadResponse.class);

            String uploadUrl = response.getUploadUrl();
            if (uploadUrl == null) {
                throw new PushbulletApiException("Undefined upload url");
            }

            httpClient.uploadFile(uploadUrl, data);

            return response;
        } catch (PushbulletApiException e) {
            return null;
        }
    }

    /**
     * Helper method to get the data for a given content
     *
     * @param content the file content
     * @return the data raw type if available, otherwise null
     */
    private @Nullable RawType getContentData(String content) {
        try {
            if (content.startsWith("data:")) {
                return RawType.valueOf(content);
            } else if (content.startsWith("http")) {
                return HttpUtil.downloadImage(content);
            } else {
                Path path = Path.of(content);
                byte[] bytes = Files.readAllBytes(path);
                String mimeType = Files.probeContentType(path);
                return new RawType(bytes, mimeType);
            }
        } catch (IllegalArgumentException | IOException e) {
            logger.debug("Failed to get content data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to get the file name for a given content
     *
     * @param content the file content
     * @return the file name if available, otherwise null
     */
    private @Nullable String getContentFileName(String content) {
        if (content.startsWith("data:")) {
            return IMAGE_FILE_NAME;
        }
        try {
            Path fileName = Path.of(content.startsWith("http") ? new URL(content).getPath() : content).getFileName();
            if (fileName != null) {
                return fileName.toString();
            }
        } catch (MalformedURLException e) {
            logger.debug("Malformed url content: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Helper method to create a push request
     *
     * @param recipient the recipient
     * @param title the title
     * @param message the message
     * @param type the push type
     *
     * @return the push request object
     */
    private PushRequest newPushRequest(@Nullable String recipient, @Nullable String title, @Nullable String message,
            PushType type) {
        logger.debug("Recipient is '{}'", recipient);
        logger.debug("Title is     '{}'", title);
        logger.debug("Message is   '{}'", message);
        logger.debug("Type is      '{}'", type);

        PushRequest request = new PushRequest();
        request.setTitle(title);
        request.setBody(message);
        request.setType(type);

        if (recipient != null) {
            if (isValidEmail(recipient)) {
                logger.debug("Recipient is an email address");
                request.setEmail(recipient);
            } else if (isValidChannel(recipient)) {
                logger.debug("Recipient is a channel tag");
                request.setChannel(recipient);
            } else {
                logger.warn("Invalid recipient: {}", recipient);
                logger.warn("Message will be broadcast to all user's devices.");
            }
        }

        return request;
    }

    /**
     * Helper method checking if channel tag is valid
     *
     * @param channel the channel tag
     * @return true if matches pattern
     */
    private static boolean isValidChannel(String channel) {
        return CHANNEL_PATTERN.matcher(channel).matches();
    }

    /**
     * Helper method checking if email address is valid
     *
     * @param email the email address
     * @return true if parsed successfully
     */
    private static boolean isValidEmail(String email) {
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
