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
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private HashMap<Integer, String[]> channels = new HashMap<Integer, String[]>();

    public GuntamaticHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_Betrieb.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    private void parseAndUpdate(String html) {
        String[] daqdata = html.split("\\n");

        for (Integer i : channels.keySet()) {
            String[] channel = channels.get(i);
            State newState = new StringType(daqdata[i].trim() + channel[1]);
            updateState((@NonNull String) channel[0], newState);
        }

        /*
         * State newState = new StringType(daqdata[0].trim());
         * updateState(CHANNEL_Betrieb, newState);
         * newState = new StringType(daqdata[1].trim());
         * updateState(CHANNEL_Aussentemperatur, newState);
         */
    }

    /*
     * protected List<Channel> createDynamicChannels() {
     * List<Channel> channels = new ArrayList<>();
     * channels.add(buildChannel("test2", "String"));
     * return channels;
     * }
     * 
     * private Channel buildChannel(String channelType, String itemType) {
     * return ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelType), itemType).withType(new
     * ChannelTypeUID("guntamatic", "test")).build();
     * }
     */
    private void parseAndInit(String html) {
        String[] daqdesc = html.split("\\n");
        for (int i = 0; i < daqdesc.length; i++) {
            String[] param = daqdesc[i].split(";");
            if (!"reserved".equals(param[0])) {
                String[] channel = new String[] { null, null };
                channel[0] = param[0].replaceAll("[^\\w]", "");
                if ((param.length == 1) || (param[1].trim().isEmpty())) {
                    channel[1] = "";
                } else {
                    channel[1] = param[1].trim().replace("m3", "m³");
                }
                if (CHANNELS.contains(channel[0])) {
                    channels.put(i, channel);
                    logger.warn("Supported Channel: ID: {}, Name: {}, Unit: {}", String.format("%03d", i), channel[0],
                            channel[1]);
                } else {
                    logger.warn("Unsupported Channel: ID: {}, Name: {}, Unit: {}", String.format("%03d", i), channel[0],
                            channel[1]);
                }
            }
        }
        /*
         * ThingBuilder builder = editThing();
         * boolean changed = false;
         * for (Channel channel : createDynamicChannels()) {
         * // we only want to add each channel, not replace all of them
         * //if (getThing().getChannel(channel.getUID()) == null)
         * {
         * builder.withChannel(channel);
         * changed = true;
         * }
         * }
         * if (changed) {
         * updateThing(builder.build());
         * }
         */
        /*
         * ThingBuilder thingBuilder = editThing();
         * ChannelType type = ChannelTypeBuilder.state("test", "test", "String");
         * Channel channel = ChannelBuilder
         * .create(new ChannelUID(thing.getUID(), "test"), "String").withType(type).build();
         * //ChannelUID channelId = channel.getUID();
         * //List<Channel> channelss = new ArrayList<>(getThing().getChannels());
         * //if (channelss.stream().filter((element) -> element.getUID().equals(channelId)).count() == 0) {
         * thingBuilder.withChannel(channel);
         * updateThing(thingBuilder.build());
         * 
         * //thingBuilder.withChannel(channel);
         * initalized = true;
         * //updateThing(thingBuilder.build());
         */

        /*
         * for (Integer i : channels.keySet()) {
         * String[] line = channels.get(i);
         * String name = line[0].replaceAll("[^\\w]", "");
         * String unit;
         * if ((line.length == 1) || ("".equals(line[1].trim())))
         * {
         * unit = "n/a";
         * }
         * else
         * {
         * unit = line[1].replace("m3", "m³");
         * }
         * logger.warn("ID: {}, Name: {}, Unit: {}", String.format("%03d", i), name, unit);
         */
        /*
         * Channel channel = ChannelBuilder
         * .create(new ChannelUID(thing.getUID(), String.format("ch%03d_%s", i, channels.get(i)[0].replaceAll("[^\\w-]",
         * ""))
         * ), "String")
         * .build();
         * thingBuilder.withChannel(channel);
         */
        /*
         * 
         * logger.warn(
         * "key: " + i.toString() + " value: " + (channels.get(i))[0].toString().replaceAll("[^\\w-]", ""));
         * 
         * // logger.warn("Adding channel: {} with item type: {}", obisChannelString, itemType);
         * 
         * // channel has not been created yet
         * ChannelBuilder channelBuilder = ChannelBuilder
         * .create(new ChannelUID(thing.getUID(), channels.get(i)[0].replaceAll("[^\\w-]", "")), "String");
         * // .withType(channelTypeId);
         * /*
         * Configuration configuration = new Configuration();
         * configuration.put(SmartMeterBindingConstants.CONFIGURATION_CONVERSION, 1);
         * channelBuilder.withConfiguration(configuration);
         * channelBuilder.withLabel(obis);
         * Map<String, String> channelProps = new HashMap<>();
         * channelProps.put(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
         * channelBuilder.withProperties(channelProps);
         * channelBuilder.withDescription(
         * MessageFormat.format("Value for OBIS code: {0} with Unit: {1}", obis, value.getUnit()));
         */

        /*
         * Channel channel = channelBuilder.build();
         * ChannelUID channelId = channel.getUID();
         * 
         * // add all valid channels to the thing builder
         * List<Channel> channels = new ArrayList<>(getThing().getChannels());
         * if (channels.stream().filter((element) -> element.getUID().equals(channelId)).count() == 0) {
         * channels.add(channel);
         * ThingBuilder thingBuilder = editThing();
         * thingBuilder.withChannels(channels);
         * updateThing(thingBuilder.build());
         * }
         */
        // }

        // logger.warn(channels.toString());
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
                // Byte[] response = ;

                String response = null;
                try {
                    response = new String(contentResponse.getContent(), Charset.forName(config.encoding));
                    response = replaceUmlaut(response);

                    if (url == DAQDATA_URL) {
                        parseAndUpdate(response);// contentResponse.getContentAsString());
                    } else if (url == DAQDESC_URL) {
                        parseAndInit(response);// contentResponse.getContentAsString());
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
        sendGetRequest(DAQDATA_URL);
    }

    @Override
    public void initialize() {
        config = getConfigAs(GuntamaticConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        // if (initalized == false)
        {
            logger.warn("init");
            sendGetRequest(DAQDESC_URL);
            pollingFuture = scheduler.scheduleWithFixedDelay(this::pollGuntamatic, 1, config.refreshInterval,
                    TimeUnit.SECONDS);
            logger.warn("initialized");
        }
        // else
        {

        }
        /*
         * pollingFuture = scheduler.scheduleWithFixedDelay(this::pollGuntamatic, 1, config.refreshInterval,
         * TimeUnit.SECONDS);
         */

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
     * if (initalized == false)
     * {
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
