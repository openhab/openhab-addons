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
package org.openhab.binding.moduledsmarthome.internal;

import static org.openhab.binding.moduledsmarthome.internal.ModuledSmartHomeBindingConstants.*;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.*;
import org.eclipse.jetty.client.util.*;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.moduledsmarthome.internal.enums.FAN_DIRECTION;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ModuledSmartHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Conrado Costa - Initial contribution
 */
@NonNullByDefault
public class ModuledSmartHomeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ModuledSmartHomeHandler.class);
    private final HttpClient httpClient;

    private @Nullable ModuledSmartHomeConfiguration config;

    // private @Nullable HttpClient httpClient = new HttpClient();

    public ModuledSmartHomeHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;

        config = getConfigAs(ModuledSmartHomeConfiguration.class);

        try {
            httpClient.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("HTTP STARTING: {}", e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (FAN_DIRECTION_CHANNEL.equals(channelUID.getId())) {
            switch (channelUID.getId()) {
                case FAN_DIRECTION_CHANNEL:
                    if (command instanceof StringType) {
                        changeFanDirection(FAN_DIRECTION.valueOf(command.toString()));
                        // if (command.toString().equals("UP")) {
                        // changeFanDirection(FAN_DIRECTION.UP);
                    }
                    break;
                default:
                    break;
            }
            if (command instanceof RefreshType) {
                switch (channelUID.getId()) {
                    case FAN_DIRECTION_CHANNEL:
                        logger.debug("Receiving RefreshType for FAN_DIRECTION");
                        updateState(channelUID, new StringType(FAN_DIRECTION.UP.toString()));
                        break;
                }
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = false; // <background task with long running initialization here>
            logger.info("Hostname length: {}", config.hostname.length());
            if (config.hostname.length() == 0) {
                logger.info("Hostname is no informed.");
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR);
                return;
            }

            String uri_str = "http://" + config.hostname + "/api/handshake";
            logger.info("uri: {}", uri_str);

            URI uri = URI.create(uri_str);

            logger.info("uri Host: {}", uri.getHost());
            logger.info("uri Path: {}", uri.getPath());

            Request req = httpClient.newRequest(uri).method(HttpMethod.GET).accept("*").header("token", config.password)
                    .timeout(5, TimeUnit.SECONDS);

            try {
                ContentResponse res = req.send();
                String resValue = res.getContentAsString();
                if (resValue.equals("ACK")) {
                    updateStatus(ThingStatus.ONLINE);
                } else if (resValue.equals("INVALID_TOKEN")) {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR);
                } else {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

            // when done do:
            // if (thingReachable) {
            // updateStatus(ThingStatus.ONLINE);
            // } else {
            // updateStatus(ThingStatus.OFFLINE);
            // }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void changeFanDirection(FAN_DIRECTION direction) {
        // calls api to change fan direction
        logger.info("Setting fan direction to {}", direction.name());

        String cmd;
        switch (direction.name()) {
            case "STOP":
                cmd = "{\"state\": 0, \"dir\": 0}";
                break;
            case "UP":
                cmd = "{\"state\": 1, \"dir\": 1}";
                break;
            case "DOWN":
                cmd = "{\"state\": 1, \"dir\": 2}";
                break;
            default:
                cmd = "{\"state\": 0, \"dir\": 0}";
                break;
        }
        AbstractTypedContentProvider content = new StringContentProvider(cmd);
        logger.info("Content sent: {}", cmd);

        if (config.hostname == null)
            return;

        try {
            ContentResponse res = httpClient.POST("http://192.168.100.37/api/fan_control")
                    .content(content, "application/json").accept("*").send();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.error("HTTP SENDING: {}", e.getMessage());
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            logger.error("HTTP SENDING: {}", e.getMessage());
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            logger.error("HTTP SENDING: {}", e.getMessage());
        }
        updateState(FAN_DIRECTION_CHANNEL, new StringType(direction.name()));
    }
}
