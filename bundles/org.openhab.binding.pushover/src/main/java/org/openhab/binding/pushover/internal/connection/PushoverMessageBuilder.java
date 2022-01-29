/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pushover.internal.connection;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.PathContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PushoverMessageBuilder} builds the body for Pushover Messages API requests.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverMessageBuilder {

    private final Logger logger = LoggerFactory.getLogger(PushoverMessageBuilder.class);

    public static final String MESSAGE_KEY_TOKEN = "token";
    private static final String MESSAGE_KEY_USER = "user";
    private static final String MESSAGE_KEY_MESSAGE = "message";
    private static final String MESSAGE_KEY_TITLE = "title";
    private static final String MESSAGE_KEY_DEVICE = "device";
    private static final String MESSAGE_KEY_PRIORITY = "priority";
    private static final String MESSAGE_KEY_RETRY = "retry";
    private static final String MESSAGE_KEY_EXPIRE = "expire";
    private static final String MESSAGE_KEY_URL = "url";
    private static final String MESSAGE_KEY_URL_TITLE = "url_title";
    private static final String MESSAGE_KEY_SOUND = "sound";
    private static final String MESSAGE_KEY_ATTACHMENT = "attachment";
    public static final String MESSAGE_KEY_HTML = "html";
    public static final String MESSAGE_KEY_MONOSPACE = "monospace";

    private static final int MAX_MESSAGE_LENGTH = 1024;
    private static final int MAX_TITLE_LENGTH = 250;
    private static final int MAX_DEVICE_LENGTH = 25;
    private static final List<Integer> VALID_PRIORITY_LIST = List.of(-2, -1, 0, 1, 2);
    private static final int DEFAULT_PRIORITY = 0;
    public static final int EMERGENCY_PRIORITY = 2;
    private static final int MIN_RETRY_SECONDS = 30;
    private static final int MAX_EXPIRE_SECONDS = 10800;
    private static final int MAX_URL_LENGTH = 512;
    private static final int MAX_URL_TITLE_LENGTH = 100;
    public static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private final MultiPartContentProvider body = new MultiPartContentProvider();

    private @Nullable String message;
    private @Nullable String title;
    private @Nullable String device;
    private int priority = DEFAULT_PRIORITY;
    private int retry = 300;
    private int expire = 3600;
    private @Nullable String url;
    private @Nullable String urlTitle;
    private @Nullable String sound;
    private @Nullable String attachment;
    private @Nullable String contentType;
    private boolean html = false;
    private boolean monospace = false;

    private PushoverMessageBuilder(String apikey, String user) throws ConfigurationException {
        body.addFieldPart(MESSAGE_KEY_TOKEN, new StringContentProvider(apikey), null);
        body.addFieldPart(MESSAGE_KEY_USER, new StringContentProvider(user), null);
    }

    public static PushoverMessageBuilder getInstance(@Nullable String apikey, @Nullable String user)
            throws ConfigurationException {
        if (apikey == null || apikey.isBlank()) {
            throw new ConfigurationException(TEXT_OFFLINE_CONF_ERROR_MISSING_APIKEY);
        }

        if (user == null || user.isBlank()) {
            throw new ConfigurationException(TEXT_OFFLINE_CONF_ERROR_MISSING_USER);
        }

        return new PushoverMessageBuilder(apikey, user);
    }

    public PushoverMessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public PushoverMessageBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public PushoverMessageBuilder withDevice(String device) {
        this.device = device;
        return this;
    }

    public PushoverMessageBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public PushoverMessageBuilder withRetry(int retry) {
        this.retry = retry;
        return this;
    }

    public PushoverMessageBuilder withExpire(int expire) {
        this.expire = expire;
        return this;
    }

    public PushoverMessageBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public PushoverMessageBuilder withUrlTitle(String urlTitle) {
        this.urlTitle = urlTitle;
        return this;
    }

    public PushoverMessageBuilder withSound(String sound) {
        this.sound = sound;
        return this;
    }

    public PushoverMessageBuilder withAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }

    public PushoverMessageBuilder withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public PushoverMessageBuilder withHtmlFormatting() {
        this.html = true;
        return this;
    }

    public PushoverMessageBuilder withMonospaceFormatting() {
        this.monospace = true;
        return this;
    }

    public ContentProvider build() throws CommunicationException {
        if (message != null) {
            if (message.length() > MAX_MESSAGE_LENGTH) {
                throw new IllegalArgumentException(String.format(
                        "Skip sending the message as 'message' is longer than %d characters.", MAX_MESSAGE_LENGTH));
            }
            body.addFieldPart(MESSAGE_KEY_MESSAGE, new StringContentProvider(message), null);
        }

        if (title != null) {
            if (title.length() > MAX_TITLE_LENGTH) {
                throw new IllegalArgumentException(String
                        .format("Skip sending the message as 'title' is longer than %d characters.", MAX_TITLE_LENGTH));
            }
            body.addFieldPart(MESSAGE_KEY_TITLE, new StringContentProvider(title), null);
        }

        if (device != null) {
            if (device.length() > MAX_DEVICE_LENGTH) {
                logger.warn("Skip 'device' as it is longer than {} characters. Got: {}.", MAX_DEVICE_LENGTH, device);
            } else {
                body.addFieldPart(MESSAGE_KEY_DEVICE, new StringContentProvider(device), null);
            }
        }

        if (priority != DEFAULT_PRIORITY) {
            if (VALID_PRIORITY_LIST.contains(priority)) {
                body.addFieldPart(MESSAGE_KEY_PRIORITY, new StringContentProvider(String.valueOf(priority)), null);

                if (priority == EMERGENCY_PRIORITY) {
                    if (retry < MIN_RETRY_SECONDS) {
                        logger.warn("Retry value of {} is too small. Using default value of {}.", retry,
                                MIN_RETRY_SECONDS);
                        body.addFieldPart(MESSAGE_KEY_RETRY,
                                new StringContentProvider(String.valueOf(MIN_RETRY_SECONDS)), null);
                    } else {
                        body.addFieldPart(MESSAGE_KEY_RETRY, new StringContentProvider(String.valueOf(retry)), null);
                    }

                    if (0 < expire && expire <= MAX_EXPIRE_SECONDS) {
                        body.addFieldPart(MESSAGE_KEY_EXPIRE, new StringContentProvider(String.valueOf(expire)), null);
                    } else {
                        logger.warn("Expire value of {} is invalid. Using default value of {}.", expire,
                                MAX_EXPIRE_SECONDS);
                        body.addFieldPart(MESSAGE_KEY_EXPIRE,
                                new StringContentProvider(String.valueOf(MAX_EXPIRE_SECONDS)), null);
                    }
                }
            } else {
                logger.warn("Invalid 'priority', skipping. Expected: {}. Got: {}.",
                        VALID_PRIORITY_LIST.stream().map(i -> i.toString()).collect(Collectors.joining(",")), priority);
            }
        }

        if (url != null) {
            if (url.length() > MAX_URL_LENGTH) {
                throw new IllegalArgumentException(String
                        .format("Skip sending the message as 'url' is longer than %d characters.", MAX_URL_LENGTH));
            }
            body.addFieldPart(MESSAGE_KEY_URL, new StringContentProvider(url), null);

            if (urlTitle != null) {
                if (urlTitle.length() > MAX_URL_TITLE_LENGTH) {
                    throw new IllegalArgumentException(
                            String.format("Skip sending the message as 'urlTitle' is longer than %d characters.",
                                    MAX_URL_TITLE_LENGTH));
                }
                body.addFieldPart(MESSAGE_KEY_URL_TITLE, new StringContentProvider(urlTitle), null);
            }
        }

        if (sound != null) {
            body.addFieldPart(MESSAGE_KEY_SOUND, new StringContentProvider(sound), null);
        }

        if (attachment != null) {
            String localAttachment = attachment;
            if (localAttachment.startsWith("http")) { // support data HTTP(S) scheme
                RawType rawImage = HttpUtil.downloadImage(attachment, 10000);
                if (rawImage == null) {
                    throw new IllegalArgumentException(
                            String.format("Skip sending the message as content '%s' does not exist.", attachment));
                }
                addFilePart(createTempFile(rawImage.getBytes()),
                        contentType == null ? rawImage.getMimeType() : contentType);
            } else if (localAttachment.startsWith("data:")) { // support data URI scheme
                try {
                    RawType rawImage = RawType.valueOf(localAttachment);
                    addFilePart(createTempFile(rawImage.getBytes()),
                            contentType == null ? rawImage.getMimeType() : contentType);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(String
                            .format("Skip sending the message because data URI scheme is invalid: %s", e.getMessage()));
                }
            } else {
                File file = new File(attachment);
                if (!file.exists()) {
                    throw new IllegalArgumentException(
                            String.format("Skip sending the message as file '%s' does not exist.", attachment));
                }
                addFilePart(file.toPath(), contentType);
            }
        }

        if (html) {
            body.addFieldPart(MESSAGE_KEY_HTML, new StringContentProvider("1"), null);
        } else if (monospace) {
            body.addFieldPart(MESSAGE_KEY_MONOSPACE, new StringContentProvider("1"), null);
        }

        return body;
    }

    private Path createTempFile(byte[] data) throws CommunicationException {
        try {
            Path tmpFile = Files.createTempFile("pushover-", ".tmp");
            return Files.write(tmpFile, data);
        } catch (IOException e) {
            logger.debug("IOException occurred while creating temp file - skip sending the message: {}", e.getMessage(),
                    e);
            throw new CommunicationException(TEXT_ERROR_SKIP_SENDING_MESSAGE, e.getCause(), e.getLocalizedMessage());
        }
    }

    private void addFilePart(Path path, @Nullable String contentType) throws CommunicationException {
        try {
            body.addFilePart(MESSAGE_KEY_ATTACHMENT, path.toFile().getName(),
                    new PathContentProvider(contentType == null ? DEFAULT_CONTENT_TYPE : contentType, path), null);
        } catch (IOException e) {
            logger.debug("IOException occurred while adding content - skip sending the message: {}", e.getMessage(), e);
            throw new CommunicationException(TEXT_ERROR_SKIP_SENDING_MESSAGE, e.getCause(), e.getLocalizedMessage());
        }
    }
}
