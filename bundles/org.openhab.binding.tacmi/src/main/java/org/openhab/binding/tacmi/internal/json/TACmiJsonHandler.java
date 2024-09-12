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
import org.openhab.binding.tacmi.internal.TACmiChannelTypeProvider;
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
    @SuppressWarnings("unused")
    private TACmiChannelTypeProvider channelTypeProvider;
    private @Nullable String authHeader;
    private @Nullable String url;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private int pollInterval = 60;

    public TACmiJsonHandler(Thing thing, HttpClient httpClient, TACmiChannelTypeProvider channelTypeProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.channelTypeProvider = channelTypeProvider;

        final Config config = getConfigAs(Config.class);

        if (config.host.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No host configured!");
            return;
        }
        if (config.username.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No username configured!");
            return;
        }
        if (config.password.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No password configured!");
            return;
        }
        if (config.params.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No params configured!");
            return;
        }

        if (config.nodeId == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "NodeId not configured!");
            return;
        }

        if (config.pollInterval < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Poll interval to low");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((config.username + ":" + config.password).getBytes(StandardCharsets.ISO_8859_1));

        this.url = "http://" + config.host + "/INCLUDE/api.cgi?" + "jsonnode=" + config.nodeId + "&jsonparam="
                + config.params;

        logger.debug("URL: {}", this.url);

        this.pollInterval = config.pollInterval;
    }

    @Override
    public void initialize() {
        if (this.url == null) {
            return;
        }
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
            final String ah = this.authHeader;
            if (ah != null) {
                req.header(HttpHeader.AUTHORIZATION, ah);
            }

            final ContentResponse res = req.send();
            if (res.getStatus() != 200) {
                logger.warn("Error requesting update {} / {} \n{}", res.getStatus(), res.getReason(),
                        res.getContentAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            logger.trace("Reply:\n{}", res.getContentAsString());
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            JsonResponse resp = gson.fromJson(res.getContentAsString(), JsonResponse.class);
            if (resp == null) {
                logger.error("Response is Empty");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            if (resp.statusCode != 0) {
                logger.error("Response Error: {} ({})", resp.statusCode, resp.status);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, resp.status);
                return;
            }
            updateChannels(resp);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } catch (final InterruptedException e) {
            // binding shutdown is in progress
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        } catch (final Exception e) {
            logger.trace("Exception:\n{}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error: " + e.getMessage());
        }
    }

    private void updateChannels(JsonResponse jr) throws Exception {
        final var allchans = new ArrayList<Channel>();
        boolean mod = false;

        BiFunction<Collection<IO>, String, Boolean> chanhandler = (coll, pre) -> {
            var lmod = false;
            for (var inp : coll) {
                var name = pre.charAt(0) + inp.number.toString();
                final var type = inp.getType();
                final var ctype = inp.getChannelType();
                Channel channel = this.getThing().getChannel(name);
                if (channel == null || channel.getChannelTypeUID() != ctype) {
                    logger.debug("Creating / updating channel {} of type {} for '{}'", name, type, inp.getDesc());
                    ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), name);
                    ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, type);
                    channelBuilder.withLabel(pre + " " + inp.number.toString());
                    channelBuilder.withDescription(inp.getDesc());
                    channelBuilder.withType(ctype);
                    channel = channelBuilder.build();
                    lmod = true;
                }
                allchans.add(channel);
                updateState(channel.getUID(), inp.getState());
            }
            return lmod;
        };

        mod |= chanhandler.apply(jr.data.inputs, "Input");
        mod |= chanhandler.apply(jr.data.outputs, "Output");
        mod |= chanhandler.apply(jr.data.general, "Global");

        if (mod) {
            var tb = editThing();
            tb.withChannels(allchans);
            updateThing(tb.build());
        }
    }
}
