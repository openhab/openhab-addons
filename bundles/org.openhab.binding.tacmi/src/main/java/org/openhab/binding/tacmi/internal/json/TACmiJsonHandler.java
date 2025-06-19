/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal.json;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tacmi.internal.json.obj.IO;
import org.openhab.binding.tacmi.internal.json.obj.JsonResponse;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handler for the TA Json bindung
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 *
 */
@NonNullByDefault
public class TACmiJsonHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TACmiJsonHandler.class);
    private HttpClient httpClient;
    private @Nullable String authHeader;
    private @Nullable String url;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private int pollInterval = 60;

    public TACmiJsonHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        final Config config = getConfigAs(Config.class);

        if (config.host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No host configured!");
            return;
        }
        if (config.username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No username configured!");
            return;
        }
        if (config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No password configured!");
            return;
        }
        if (config.params.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No params configured!");
            return;
        }

        if (config.nodeId == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "NodeId not configured!");
            return;
        }

        if (config.pollInterval < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Poll interval to short");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((config.username + ":" + config.password).getBytes(StandardCharsets.ISO_8859_1));

        this.url = "http://" + config.host + "/INCLUDE/api.cgi?" + "jsonnode=" + config.nodeId + "&jsonparam="
                + config.params;

        logger.debug("URL: {}", this.url);

        this.pollInterval = config.pollInterval;

        // we want to trigger the initial refresh 'at once'
        this.scheduledFuture = scheduler.scheduleWithFixedDelay(this::refreshData, 0, pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // We do not support commands
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            this.scheduledFuture = null;
        }
        super.dispose();
    }

    private void refreshData() {
        try {
            final Request req = httpClient.newRequest(this.url).method(HttpMethod.GET).timeout(10000,
                    TimeUnit.MILLISECONDS);
            req.header(HttpHeader.ACCEPT_LANGUAGE, "en");
            final String authH = this.authHeader;
            if (authH != null) {
                req.header(HttpHeader.AUTHORIZATION, authH);
            }

            final ContentResponse httpResponse = req.send();
            if (httpResponse.getStatus() != 200) {
                logger.warn("Error requesting update {} / {} \n{}", httpResponse.getStatus(), httpResponse.getReason(),
                        httpResponse.getContentAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            logger.trace("Reply:\n{}", httpResponse.getContentAsString());
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            JsonResponse jsonResponse = gson.fromJson(httpResponse.getContentAsString(), JsonResponse.class);
            if (jsonResponse == null) {
                logger.warn("Response is Empty");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            if (jsonResponse.statusCode != 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, jsonResponse.status);
                return;
            }
            updateChannels(jsonResponse);
            updateStatus(ThingStatus.ONLINE);
        } catch (final InterruptedException e) {
            // binding shutdown is in progress
            updateStatus(ThingStatus.OFFLINE);
        } catch (final Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateChannels(JsonResponse jsonResponse) throws Exception {
        final var allChans = new ArrayList<Channel>();

        BiFunction<Collection<IO>, String, Boolean> chanhandler = (jsonArray, prefix) -> {
            var channelChanged = false;
            for (var inputItem : jsonArray) {
                var name = prefix.charAt(0) + inputItem.number.toString();
                final var type = inputItem.getType();
                final var channelType = inputItem.getChannelType();
                Channel channel = this.getThing().getChannel(name);
                final var currentChannelType = channel != null ? channel.getChannelTypeUID() : null;
                // The null checker does not detect that if channel is null, currentChannelType is also null.
                if (channel == null || currentChannelType == null || !currentChannelType.equals(channelType)) {
                    logger.debug("Creating / updating channel {} of type {} for '{}'", name, type, inputItem.getDesc());
                    ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), name);
                    ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, type);
                    channelBuilder.withLabel(prefix + " " + inputItem.number.toString());
                    channelBuilder.withDescription(inputItem.getDesc());
                    channelBuilder.withType(channelType);
                    channel = channelBuilder.build();
                    channelChanged = true;
                }
                allChans.add(channel);
                updateState(channel.getUID(), inputItem.getState());
            }
            return channelChanged;
        };

        boolean modified = false;
        modified |= chanhandler.apply(jsonResponse.data.inputs, "Input");
        modified |= chanhandler.apply(jsonResponse.data.outputs, "Output");
        modified |= chanhandler.apply(jsonResponse.data.general, "Global");

        if (modified) {
            final var eThing = editThing();
            eThing.withChannels(allChans);
            updateThing(eThing.build());
        }
    }
}
