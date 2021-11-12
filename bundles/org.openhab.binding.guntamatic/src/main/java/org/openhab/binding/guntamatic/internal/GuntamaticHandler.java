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
package org.openhab.binding.guntamatic.internal;

import static org.openhab.binding.guntamatic.internal.GuntamaticBindingConstants.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

//import java.util.ArrayList;
//import org.openhab.core.thing.type.ChannelTypeBuilder;
//import java.util.List;
//import org.openhab.core.thing.type.ChannelTypeUID;
//import java.io.UnsupportedEncodingException;

/**
 * The {@link GuntamaticHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
public class GuntamaticHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GuntamaticHandler.class);

    private @Nullable GuntamaticConfiguration config;

    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private Boolean initalized = false;
    private final HttpClient httpClient;
    private GuntamaticChannelTypeProvider guntamaticChannelTypeProvider;
    private HashMap<Integer, String> channels = new HashMap<Integer, String>();
    private HashMap<Integer, String> types = new HashMap<Integer, String>();
    private HashMap<Integer, String> units = new HashMap<Integer, String>();

    public GuntamaticHandler(Thing thing, HttpClient httpClient,
            GuntamaticChannelTypeProvider guntamaticChannelTypeProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.guntamaticChannelTypeProvider = guntamaticChannelTypeProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * if (CHANNEL_Betrieb.equals(channelUID.getId())) {
         * if (command instanceof RefreshType) {
         * // TODO: handle data refresh
         * }
         * 
         * // TODO: handle command
         * 
         * // Note: if communication with thing fails for some reason,
         * // indicate that by setting the status with detail information:
         * // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
         * // "Could not control device at IP address x.x.x.x");
         * }
         */
    }

    private void parseAndUpdate(String html) {
        String[] daqdata = html.split("\\n");

        for (Integer i : channels.keySet()) {
            String channel = channels.get(i);
            if (channel != null) {
                String unit = units.get(i);
                /*
                 * if (i == 1)
                 * logger.warn("Supported Channel: Name: {}, Data: {}, Unit: {}", channel[0], daqdata[i].trim(),
                 * channel[1]);
                 */
                String value = daqdata[i].trim();
                if ("Switch".equals(thing.getChannel(channel).getAcceptedItemType())) {
                    // Guntamatic uses German OnOff when configred to German, and English OnOff for all other languages
                    value = value.replace("AUS", "OFF").replace("EIN", "ON");
                }
                State newState = new StringType(value + unit);
                updateState(channel, newState);
            } else {
                logger.warn("Data for not intialized ChannelId received: {}", i);
            }
            // TypeParser.parseState(this.acceptedDataTypes, sensorValue);
        }
    }

    private void parseAndJsonInit(String html) {
        try {
            // remove non JSON compliant empty element ",,"
            JsonArray json = JsonParser.parseString(html.replace(",,", ",")).getAsJsonArray();
            for (int i = 1; i < json.size(); i++) {
                JsonObject points = json.get(i).getAsJsonObject();
                int id = points.get("id").getAsInt();
                String type = points.get("type").getAsString();
                // logger.warn("TypeId: {}, Type {}", id, type);
                types.put(id, type);
            }
        } catch (JsonParseException e) {
            logger.warn("Invalid JSON data will be ignored: '{}'", html.replace(",,", ","));
        }
    }

    private void parseAndInit(String html) {
        String[] daqdesc = html.split("\\n");
        ArrayList<Channel> channelList = new ArrayList<Channel>();
        ArrayList<String> channelIdList = new ArrayList<String>();
        for (int i = 0; i < daqdesc.length; i++) {
            String[] param = daqdesc[i].split(";");
            if (!"reserved".equals(param[0])) {
                String channel = param[0].replaceAll("[^\\w]", "").toLowerCase();
                String unit;
                if ((param.length == 1) || (param[1].isEmpty())) {
                    unit = "";
                } else {
                    unit = param[1].trim().replace("m3", "m³");
                }

                boolean channelInitialized = (channelIdList.contains(channel));
                if (!channelInitialized) {
                    String itemType;
                    String pattern;
                    String type;
                    if (types.containsKey(i)) {
                        type = types.get(i);
                    } else {
                        type = "";
                    }

                    if (type.equals("boolean")) {
                        itemType = "Switch";
                        pattern = "";
                    } else if (type.equals("integer")) {
                        pattern = "%d";
                        if (unit.isEmpty()) {
                            itemType = "Number";
                        } else {
                            itemType = guessQuantityType("Number", unit);
                            pattern += " %unit%";
                        }
                    } else if (type.equals("float")) {
                        pattern = "%.2f";
                        if (unit.isEmpty()) {
                            itemType = "Number";
                        } else {
                            itemType = guessQuantityType("Number", unit);
                            pattern += " %unit%";
                        }
                    } else if (type.equals("string")) {
                        itemType = "String";
                        pattern = "";
                    } else {
                        if (unit.isEmpty()) {
                            itemType = "String";
                            pattern = "";
                        } else {
                            itemType = guessQuantityType("", unit);
                            pattern = "%.2f %unit%";
                        }
                    }

                    ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channel);
                    guntamaticChannelTypeProvider.addChannelType(channelTypeUID, channel, itemType,
                            "Guntamatic " + param[0], false, pattern);
                    Channel newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channel), itemType)
                            .withType(channelTypeUID).withKind(ChannelKind.STATE).withLabel(param[0]).build();
                    channelList.add(newChannel);
                    channelIdList.add(channel);
                    /*
                     * logger.warn(
                     * "Supported Channel: Idx: '{}', Name: '{}'/'{}', Type: '{}'/'{}', Pattern '{}', Unit: '{}'",
                     * String.format("%03d", i), param[0], channel, type, itemType, pattern, unit);
                     */
                    logger.warn("Supported Channel: Idx: {}, Name: {}, Type: {}", String.format("%03d", i), channel,
                            itemType);
                    channels.put(i, channel);
                    units.put(i, unit);
                }
            }
        }
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannels(channelList);
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
        initalized = true;
    }

    private static String guessQuantityType(String type, String unit) {
        String quantityType;

        if (type.isEmpty()) {
            type = "Number";
        }

        if ("%".equals(unit)) {
            quantityType = type + ":Dimensionless";
        } else if ("°C".equals(unit) || "°F".equals(unit)) {
            quantityType = type + ":Temperature";
        } else if ("m³".equals(unit)) {
            quantityType = type + ":Volume";
        } else if ("d".equals(unit) || "h".equals(unit)) {
            quantityType = type + ":Time";
        } else {
            quantityType = type;
        }

        return quantityType;
    }

    private static String replaceUmlaut(String input) {

        // replace all lower Umlauts
        String output = input.replace("ü", "ue").replace("ö", "oe").replace("ä", "ae").replace("ß", "ss");

        // first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        output = output.replaceAll("Ü(?=[a-zäöüß ])", "Ue").replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                .replaceAll("Ä(?=[a-zäöüß ])", "Ae");

        // now replace all the other capital umlaute
        output = output.replace("Ü", "UE").replace("Ö", "OE").replace("Ä", "AE");

        return output;
    }

    private void sendGetRequest(String url) {
        String req = "http://" + config.hostname + url;
        if ((config.key != null)) {
            req += "?key=" + config.key;
        }

        Request request = httpClient.newRequest(req);
        request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                if (!this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                }
                try {
                    String response = new String(contentResponse.getContent(), Charset.forName(config.encoding));
                    response = replaceUmlaut(response);
                    if (url == DAQEXTDESC_URL) {
                        parseAndJsonInit(response);
                    } else if (url == DAQDATA_URL) {
                        parseAndUpdate(response);
                    } else if (url == DAQDESC_URL) {
                        parseAndInit(response);
                    }
                    return;
                } catch (IllegalArgumentException e) {
                    errorReason = String.format("IllegalArgumentException: %s", e.getMessage());
                }
            } else {
                errorReason = String.format("Guntamatic request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: Guntamatic was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorReason);
    }

    private void pollGuntamatic() {
        // logger.warn("pollGuntamatic: initalized: {}", initalized);
        if (initalized == false) {
            sendGetRequest(DAQEXTDESC_URL);
            sendGetRequest(DAQDESC_URL);
        } else {
            sendGetRequest(DAQDATA_URL);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GuntamaticConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        pollingFuture = scheduler.scheduleWithFixedDelay(this::pollGuntamatic, 1, config.refreshInterval,
                TimeUnit.SECONDS);

        // These logging types should be primarily used by bindings
        /*
         * logger.trace("Example trace message");
         * logger.debug("Example debug message");
         * logger.warn("Example warn message");
         */
    }

    /*
     * @Override
     * public void thingUpdated(Thing thing) {
     * this.thing = thing;
     * logger.warn("thingUpdated: initalized: {}", initalized);
     * if (initalized == false) {
     * initialize();
     * }
     * }
     */
    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingFuture;
        if (job != null) {
            job.cancel(true);
            pollingFuture = null;
        }
        initalized = false;
    }
}
