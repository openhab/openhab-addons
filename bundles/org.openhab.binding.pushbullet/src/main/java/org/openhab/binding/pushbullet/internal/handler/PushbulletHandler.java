/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.pushbullet.internal.PushbulletConfiguration;
import org.openhab.binding.pushbullet.internal.action.PushbulletActions;
import org.openhab.binding.pushbullet.internal.model.Push;
import org.openhab.binding.pushbullet.internal.model.PushResponse;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link PushbulletHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hakan Tandogan - Initial contribution
 */
@NonNullByDefault
public class PushbulletHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PushbulletHandler.class);

    private final Gson gson = new GsonBuilder().create();

    private static final Version VERSION = FrameworkUtil.getBundle(PushbulletHandler.class).getVersion();

    private static final Pattern CHANNEL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private @Nullable PushbulletConfiguration config;

    public PushbulletHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("About to handle {} on {}", command, channelUID);

        // Future improvement: If recipient is already set, send a push on a command channel change
        // check reconnect channel of the unifi binding for that

        logger.debug("The Pushbullet binding is a read-only binding and cannot handle command '{}'.", command);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(PushbulletConfiguration.class);

        // Name and Token are both "required", so set the Thing immediately ONLINE.
        updateStatus(ThingStatus.ONLINE);

        logger.debug("Finished initializing!");
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(PushbulletActions.class);
    }

    public boolean sendPush(@Nullable String recipient, @Nullable String message, String type) {
        return sendPush(recipient, "", message, type);
    }

    public boolean sendPush(@Nullable String recipient, @Nullable String title, @Nullable String message, String type) {
        boolean result = false;

        logger.debug("sendPush is called for ");
        logger.debug("Thing {}", thing);
        logger.debug("Thing Label: '{}'", thing.getLabel());

        PushbulletConfiguration configuration = getConfigAs(PushbulletConfiguration.class);
        logger.debug("CFG {}", configuration);

        Properties headers = prepareRequestHeaders(configuration);

        String request = prepareMessageBody(recipient, title, message, type);

        try (InputStream stream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))) {
            String pushAPI = configuration.getApiUrlBase() + "/" + API_METHOD_PUSHES;

            String responseString = HttpUtil.executeUrl(HttpMethod.POST.asString(), pushAPI, headers, stream,
                    MimeTypes.Type.APPLICATION_JSON.asString(), TIMEOUT);

            logger.debug("Got Response: {}", responseString);
            PushResponse response = gson.fromJson(responseString, PushResponse.class);

            logger.debug("Unpacked Response: {}", response);

            stream.close();

            if ((null != response) && (null == response.getPushError())) {
                result = true;
            }
        } catch (IOException e) {
            logger.warn("IO problems pushing note: {}", e.getMessage());
        }

        return result;
    }

    /**
     * helper method to populate the request headers
     *
     * @param configuration
     * @return
     */
    private Properties prepareRequestHeaders(PushbulletConfiguration configuration) {
        Properties headers = new Properties();
        headers.put(HttpHeader.USER_AGENT, "openHAB / Pushbullet binding " + VERSION);
        headers.put(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString());
        headers.put("Access-Token", configuration.getToken());

        logger.debug("Headers: {}", headers);

        return headers;
    }

    /**
     * helper method to create a message body from data to be transferred.
     *
     * @param recipient
     * @param title
     * @param message
     * @param type
     *
     * @return the message as a String to be posted
     */
    private String prepareMessageBody(@Nullable String recipient, @Nullable String title, @Nullable String message,
            String type) {
        logger.debug("Recipient is '{}'", recipient);
        logger.debug("Title is     '{}'", title);
        logger.debug("Message is   '{}'", message);

        Push push = new Push();
        push.setTitle(title);
        push.setBody(message);
        push.setType(type);

        if (recipient != null) {
            if (isValidEmail(recipient)) {
                logger.debug("Recipient is an email address");
                push.setEmail(recipient);
            } else if (isValidChannel(recipient)) {
                logger.debug("Recipient is a channel tag");
                push.setChannel(recipient);
            } else {
                logger.warn("Invalid recipient: {}", recipient);
                logger.warn("Message will be broadcast to all user's devices.");
            }
        }

        logger.debug("Push: {}", push);

        String request = gson.toJson(push);
        logger.debug("Packed Request: {}", request);

        return request;
    }

    /**
     * helper method checking if channel tag is valid.
     *
     * @param channel
     * @return
     */
    private static boolean isValidChannel(String channel) {
        Matcher m = CHANNEL_PATTERN.matcher(channel);
        return m.matches();
    }

    /**
     * helper method checking if email address is valid.
     *
     * @param email
     * @return
     */
    private static boolean isValidEmail(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
