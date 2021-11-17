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

<<<<<<< HEAD
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
import org.openhab.core.types.RefreshType;
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

=======
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

>>>>>>> inital commit of skeleton
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

<<<<<<< HEAD
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
=======
    public GuntamaticHandler(Thing thing) {
        super(thing);
>>>>>>> inital commit of skeleton
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
<<<<<<< HEAD
        if (!(command instanceof RefreshType)) {
            if (!config.key.isEmpty()) {
                String param;
                String channelID = channelUID.getId();
                switch (channelID) {
                    case CHANNEL_CONTROLBOILERAPPROVAL:
                        param = getThing().getProperties().get(PARAMETER_BOILERAPPROVAL);
                        break;
                    case CHANNEL_CONTROLPROGRAM:
                        param = getThing().getProperties().get(PARAMETER_PROGRAM);
                        break;
                    case CHANNEL_CONTROLHEATCIRCPROGRAM0:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM1:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM2:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM3:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM4:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM5:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM6:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM7:
                    case CHANNEL_CONTROLHEATCIRCPROGRAM8:
                        param = getThing().getProperties().get(PARAMETER_HEATCIRCPROGRAM).replace("x",
                                channelID.substring(channelID.length() - 1));
                        break;
                    case CHANNEL_CONTROLWWHEAT0:
                    case CHANNEL_CONTROLWWHEAT1:
                    case CHANNEL_CONTROLWWHEAT2:
                        param = getThing().getProperties().get(PARAMETER_WWHEAT).replace("x",
                                channelID.substring(channelID.length() - 1));
                        break;
                    case CHANNEL_CONTROLEXTRAWWHEAT0:
                    case CHANNEL_CONTROLEXTRAWWHEAT1:
                    case CHANNEL_CONTROLEXTRAWWHEAT2:
                        param = getThing().getProperties().get(PARAMETER_EXTRAWWHEAT).replace("x",
                                channelID.substring(channelID.length() - 1));
                        break;
                    default:
                        return;
                }
                String response = sendGetRequest(PARSET_URL, "syn=" + param + "&value=" + command.toString());
                // logger.warn("{} syn={}&value={}", PARSET_URL, param, command.toString());
                State newState = new StringType(response);
                updateState(channelID, newState);
            } else {
                logger.warn("A 'key' needs to be configured in order to control the Guntamatic Heating System");
            }
        }
    }

    private void parseAndUpdate(String html) {
        String[] daqdata = html.split("\\n");

        for (Integer i : channels.keySet()) {
            String channel = channels.get(i);
            String unit = units.get(i);
            if ((channel != null) && (unit != null)) {
                String value = daqdata[i].trim();
                Channel chn = thing.getChannel(channel);
                if ((chn != null) && ("Switch".equals(chn.getAcceptedItemType()))) {
                    // Guntamatic uses German OnOff when configured to German, and English OnOff for all other languages
                    value = value.replace("AUS", "OFF").replace("EIN", "ON");
                }
                State newState = new StringType(value + unit);
                updateState(channel, newState);
                // if ("CLS".equals(value))
                // logger.warn("Supported Channel: Name: {}, Data: {}, Unit: {}", channel, value, unit);
            } else {
                logger.warn("Data for not intialized ChannelId '{}' received", i);
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

        for (String channelID : CHANNELIDS) {
            Channel channel = thing.getChannel(channelID);
            if (channel == null) {
                logger.warn("Static Channel '{}' is not present: remove and re-add Thing", channelID);
            } else {
                channelList.add(channel);
            }
        }

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

                boolean channelInitialized = (channels.containsValue(channel));
                if (!channelInitialized) {
                    String itemType;
                    String pattern;
                    String type = types.get(i);
                    if (type == null) {
                        type = "";
                    }

                    if ("boolean".equals(type)) {
                        itemType = "Switch";
                        pattern = "";
                    } else if ("integer".equals(type)) {
                        pattern = "%d";
                        if (unit.isEmpty()) {
                            itemType = "Number";
                        } else {
                            itemType = guessQuantityType("Number", unit);
                            pattern += " %unit%";
                        }
                    } else if ("float".equals(type)) {
                        pattern = "%.2f";
                        if (unit.isEmpty()) {
                            itemType = "Number";
                        } else {
                            itemType = guessQuantityType("Number", unit);
                            pattern += " %unit%";
                        }
                    } else if ("string".equals(type)) {
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
                    channels.put(i, channel);
                    units.put(i, unit);
                    /*
                     * logger.warn(
                     * "Supported Channel: Idx: '{}', Name: '{}'/'{}', Type: '{}'/'{}', Pattern '{}', Unit: '{}'",
                     * String.format("%03d", i), param[0], channel, type, itemType, pattern, unit);
                     */
                    logger.debug("Supported Channel: Idx: {}, Name: {}, Type: {}", String.format("%03d", i), channel,
                            itemType);
                    // thingBuilder.withoutChannel(newChannel.getUID());
                    // thingBuilder.withChannel(newChannel);
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

    private @Nullable String sendGetRequest(String url, String... params) {
        String errorReason = "";
        if (config == null) {
            errorReason = "Invalid Binding configuration";
        } else if (config.hostname.isEmpty()) {
            errorReason = "Invalid hostname configuration";
        } else {
            String req = "http://" + config.hostname + url;
            if (!config.key.isEmpty()) {
                req += "?key=" + config.key;
            }
            for (int i = 0; i < params.length; i++) {
                req += "&" + params[i];
            }

            Request request = httpClient.newRequest(req);
            request.method(HttpMethod.GET).timeout(5, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");

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
                        } else if (url == PARSET_URL) {
                            // via return
                        }
                        return response;
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
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorReason);
        return null;
    }

    private void pollGuntamatic() {
        // logger.warn("pollGuntamatic: initalized: {}", initalized);
        if (initalized == false) {
            if (!config.key.isEmpty()) {
                sendGetRequest(DAQEXTDESC_URL);
            }
            sendGetRequest(DAQDESC_URL);
        } else {
            sendGetRequest(DAQDATA_URL);
=======
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
>>>>>>> inital commit of skeleton
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GuntamaticConfiguration.class);
<<<<<<< HEAD
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
=======

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
>>>>>>> inital commit of skeleton
    }
}
