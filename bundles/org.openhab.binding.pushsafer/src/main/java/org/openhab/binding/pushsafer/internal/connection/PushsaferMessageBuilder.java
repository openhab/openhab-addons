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
package org.openhab.binding.pushsafer.internal.connection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PushsaferMessageBuilder} builds the body for Pushsafer Messages API requests.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@NonNullByDefault
public class PushsaferMessageBuilder {

    private final Logger logger = LoggerFactory.getLogger(PushsaferMessageBuilder.class);

    public static final String MESSAGE_KEY_TOKEN = "k";
    public static final String MESSAGE_KEY_USER = "u";
    private static final String MESSAGE_KEY_MESSAGE = "m";
    private static final String MESSAGE_KEY_TITLE = "t";
    private static final String MESSAGE_KEY_DEVICE = "d";
    private static final String MESSAGE_KEY_ICON = "i";
    private static final String MESSAGE_KEY_COLOR = "c";
    private static final String MESSAGE_KEY_VIBRATION = "v";
    private static final String MESSAGE_KEY_PRIORITY = "pr";
    private static final String MESSAGE_KEY_RETRY = "re";
    private static final String MESSAGE_KEY_EXPIRE = "ex";
    private static final String MESSAGE_KEY_URL = "u";
    private static final String MESSAGE_KEY_URL_TITLE = "ut";
    private static final String MESSAGE_KEY_SOUND = "s";
    private static final String MESSAGE_KEY_TIME2LIVE = "l";
    private static final String MESSAGE_KEY_ANSWER = "a";
    private static final String MESSAGE_KEY_CONFIRM = "cr";
    private static final String MESSAGE_KEY_ATTACHMENT = "p";
    public static final String MESSAGE_KEY_HTML = "html";
    public static final String MESSAGE_KEY_MONOSPACE = "monospace";

    private static final int MAX_MESSAGE_LENGTH = 4096;
    private static final int MAX_TITLE_LENGTH = 250;
    private static final int MAX_DEVICE_LENGTH = 25;
    private static final List<Integer> VALID_PRIORITY_LIST = Arrays.asList(-2, -1, 0, 1, 2);
    private static final int DEFAULT_PRIORITY = 0;
    public static final int EMERGENCY_PRIORITY = 2;
    private static final int MIN_RETRY_SECONDS = 0;
    private static final int MAX_EXPIRE_SECONDS = 10800;
    private static final int MAX_URL_LENGTH = 512;
    private static final int MAX_URL_TITLE_LENGTH = 100;
    public static final String DEFAULT_CONTENT_TYPE = "jpeg";
    public static final String DEFAULT_AUTH = "";

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
    private @Nullable String icon;
    private int confirm;
    private int time2live;
    private boolean answer;
    private @Nullable String color;
    private @Nullable String vibration;
    private @Nullable String attachment;
    private String contentType = DEFAULT_CONTENT_TYPE;
    private String authentication = DEFAULT_AUTH;
    private boolean html = false;
    private boolean monospace = false;

    private PushsaferMessageBuilder(String apikey, String device) throws PushsaferConfigurationException {
        body.addFieldPart(MESSAGE_KEY_TOKEN, new StringContentProvider(apikey), null);
        body.addFieldPart(MESSAGE_KEY_DEVICE, new StringContentProvider(device), null);
    }

    public static PushsaferMessageBuilder getInstance(@Nullable String apikey, @Nullable String device)
            throws PushsaferConfigurationException {
        if (apikey == null || apikey.isEmpty()) {
            throw new PushsaferConfigurationException("@text/offline.conf-error-missing-apikey");
        }

        if (device == null || device.isEmpty()) {
            throw new PushsaferConfigurationException("@text/offline.conf-error-missing-device");
        }

        return new PushsaferMessageBuilder(apikey, device);
    }

    public PushsaferMessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public PushsaferMessageBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public PushsaferMessageBuilder withDevice(String device) {
        this.device = device;
        return this;
    }

    public PushsaferMessageBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public PushsaferMessageBuilder withRetry(int retry) {
        this.retry = retry;
        return this;
    }

    public PushsaferMessageBuilder withExpire(int expire) {
        this.expire = expire;
        return this;
    }

    public PushsaferMessageBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public PushsaferMessageBuilder withUrlTitle(String urlTitle) {
        this.urlTitle = urlTitle;
        return this;
    }

    public PushsaferMessageBuilder withSound(String sound) {
        this.sound = sound;
        return this;
    }

    public PushsaferMessageBuilder withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public PushsaferMessageBuilder withColor(String color) {
        this.color = color;
        return this;
    }

    public PushsaferMessageBuilder withVibration(String vibration) {
        this.vibration = vibration;
        return this;
    }

    public PushsaferMessageBuilder withAnswer(boolean answer) {
        this.answer = answer;
        return this;
    }

    public PushsaferMessageBuilder withTime2live(int time2live) {
        this.time2live = time2live;
        return this;
    }

    public PushsaferMessageBuilder withConfirm(int confirm) {
        this.confirm = confirm;
        return this;
    }

    public PushsaferMessageBuilder withAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }

    public PushsaferMessageBuilder withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public PushsaferMessageBuilder withAuthentication(String authentication) {
        this.authentication = authentication;
        return this;
    }

    public PushsaferMessageBuilder withHtmlFormatting() {
        this.html = true;
        return this;
    }

    public PushsaferMessageBuilder withMonospaceFormatting() {
        this.monospace = true;
        return this;
    }

    public ContentProvider build() throws PushsaferCommunicationException {
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

        if (icon != null) {
            body.addFieldPart(MESSAGE_KEY_ICON, new StringContentProvider(icon), null);
        }

        if (color != null) {
            body.addFieldPart(MESSAGE_KEY_COLOR, new StringContentProvider(color), null);
        }

        if (vibration != null) {
            body.addFieldPart(MESSAGE_KEY_VIBRATION, new StringContentProvider(vibration), null);
        }

        body.addFieldPart(MESSAGE_KEY_CONFIRM, new StringContentProvider(String.valueOf(confirm)), null);

        body.addFieldPart(MESSAGE_KEY_ANSWER, new StringContentProvider(String.valueOf(answer)), null);

        body.addFieldPart(MESSAGE_KEY_TIME2LIVE, new StringContentProvider(String.valueOf(time2live)), null);
        String attachment = this.attachment;
        if (attachment != null) {
            String localAttachment = attachment;
            final String encodedString;
            try {
                if (localAttachment.startsWith("http")) {
                    Properties headers = new Properties();
                    headers.put("User-Agent", "Mozilla/5.0");
                    if (!authentication.isBlank()) {
                        headers.put("Authorization", "Basic "
                                + Base64.getEncoder().encodeToString(authentication.getBytes(StandardCharsets.UTF_8)));
                    }
                    String content = HttpUtil.executeUrl("GET", attachment, headers, null, null, 10);
                    if (content == null) {
                        throw new IllegalArgumentException(
                                String.format("Skip sending the message as content '%s' does not exist.", attachment));
                    }
                    encodedString = "data:image/" + contentType + ";base64," + content;
                } else if (localAttachment.startsWith("data:")) {
                    encodedString = localAttachment;
                } else {
                    File file = new File(attachment);
                    if (!file.exists()) {
                        throw new IllegalArgumentException(
                                String.format("Skip sending the message as file '%s' does not exist.", attachment));
                    }
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    encodedString = "data:image/" + contentType + ";base64,"
                            + Base64.getEncoder().encodeToString(fileContent);
                }
                body.addFieldPart(MESSAGE_KEY_ATTACHMENT, new StringContentProvider(encodedString), null);
            } catch (IOException e) {
                logger.debug("IOException occurred - skip sending message: {}", e.getLocalizedMessage(), e);
                throw new PushsaferCommunicationException(
                        String.format("Skip sending the message: %s", e.getLocalizedMessage()), e);
            }
        }

        if (html) {
            body.addFieldPart(MESSAGE_KEY_HTML, new StringContentProvider("1"), null);
        } else if (monospace) {
            body.addFieldPart(MESSAGE_KEY_MONOSPACE, new StringContentProvider("1"), null);
        }

        body.close();
        return body;
    }
}
