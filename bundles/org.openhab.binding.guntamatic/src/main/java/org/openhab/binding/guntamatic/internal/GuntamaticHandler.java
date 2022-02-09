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
package org.openhab.binding.guntamatic.internal;

import static org.openhab.binding.guntamatic.internal.GuntamaticBindingConstants.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
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

/**
 * The {@link GuntamaticHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
public class GuntamaticHandler extends BaseThingHandler {

    private static final String NUMBER_TEMPERATURE = CoreItemFactory.NUMBER + ":Temperature";
    private static final String NUMBER_VOLUME = CoreItemFactory.NUMBER + ":Volume";
    private static final String NUMBER_TIME = CoreItemFactory.NUMBER + ":Time";
    private static final String NUMBER_DIMENSIONLESS = CoreItemFactory.NUMBER + ":Dimensionless";

    private static final Map<String, Unit<?>> MAP_UNIT = Map.of("%", Units.PERCENT, "°C", SIUnits.CELSIUS, "°F",
            ImperialUnits.FAHRENHEIT, "m3", SIUnits.CUBIC_METRE, "d", Units.DAY, "h", Units.HOUR);
    private static final Map<Unit<?>, String> MAP_UNIT_ITEMTYPE = Map.of(Units.PERCENT, NUMBER_DIMENSIONLESS,
            SIUnits.CELSIUS, NUMBER_TEMPERATURE, ImperialUnits.FAHRENHEIT, NUMBER_TEMPERATURE, SIUnits.CUBIC_METRE,
            NUMBER_VOLUME, Units.DAY, NUMBER_TIME, Units.HOUR, NUMBER_TIME);

    private static final Map<String, String> MAP_COMMAND_PARAM_APPROVAL = Map.of("AUTO", "0", "OFF", "1", "ON", "2");
    private static final Map<String, String> MAP_COMMAND_PARAM_PROG = Map.of("OFF", "0", "NORMAL", "1", "WARMWATER",
            "2", "MANUAL", "8");
    private static final Map<String, String> MAP_COMMAND_PARAM_PROG_WOMANU = Map.of("OFF", "0", "NORMAL", "1",
            "WARMWATER", "2");
    private static final Map<String, String> MAP_COMMAND_PARAM_HC = Map.of("OFF", "0", "NORMAL", "1", "HEAT", "2",
            "LOWER", "3");
    private static final Map<String, String> MAP_COMMAND_PARAM_WW = Map.of("RECHARGE", "0");

    private final Logger logger = LoggerFactory.getLogger(GuntamaticHandler.class);
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> pollingFuture = null;

    private List<String> staticChannelIDs;
    private GuntamaticConfiguration config = new GuntamaticConfiguration();
    private Boolean channelsInitialized = false;
    private GuntamaticChannelTypeProvider guntamaticChannelTypeProvider;
    private Map<Integer, String> channels = new HashMap<>();
    private Map<Integer, String> types = new HashMap<>();
    private Map<Integer, Unit<?>> units = new HashMap<>();

    public GuntamaticHandler(Thing thing, HttpClient httpClient,
            GuntamaticChannelTypeProvider guntamaticChannelTypeProvider, List<String> staticChannelIDs) {
        super(thing);
        this.httpClient = httpClient;
        this.guntamaticChannelTypeProvider = guntamaticChannelTypeProvider;
        this.staticChannelIDs = staticChannelIDs;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            if (!config.key.isBlank()) {
                String param;
                Map<String, String> map;
                String channelID = channelUID.getId();
                switch (channelID) {
                    case CHANNEL_CONTROLBOILERAPPROVAL:
                        param = getThing().getProperties().get(PARAMETER_BOILERAPPROVAL);
                        map = MAP_COMMAND_PARAM_APPROVAL;
                        break;
                    case CHANNEL_CONTROLPROGRAM:
                        param = getThing().getProperties().get(PARAMETER_PROGRAM);
                        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();

                        if (THING_TYPE_BIOSTAR.equals(thingTypeUID) || THING_TYPE_POWERCHIP.equals(thingTypeUID)
                                || THING_TYPE_POWERCORN.equals(thingTypeUID) || THING_TYPE_BIOCOM.equals(thingTypeUID)
                                || THING_TYPE_PRO.equals(thingTypeUID) || THING_TYPE_THERM.equals(thingTypeUID)) {
                            map = MAP_COMMAND_PARAM_PROG;
                        } else {
                            map = MAP_COMMAND_PARAM_PROG_WOMANU;
                        }

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
                        map = MAP_COMMAND_PARAM_HC;
                        break;
                    case CHANNEL_CONTROLWWHEAT0:
                    case CHANNEL_CONTROLWWHEAT1:
                    case CHANNEL_CONTROLWWHEAT2:
                        param = getThing().getProperties().get(PARAMETER_WWHEAT).replace("x",
                                channelID.substring(channelID.length() - 1));
                        map = MAP_COMMAND_PARAM_WW;
                        break;
                    case CHANNEL_CONTROLEXTRAWWHEAT0:
                    case CHANNEL_CONTROLEXTRAWWHEAT1:
                    case CHANNEL_CONTROLEXTRAWWHEAT2:
                        param = getThing().getProperties().get(PARAMETER_EXTRAWWHEAT).replace("x",
                                channelID.substring(channelID.length() - 1));
                        map = MAP_COMMAND_PARAM_WW;
                        break;
                    default:
                        return;
                }
                String cmd = command.toString().trim();
                if (map.containsValue(cmd)) {
                    // cmd = cmd;
                } else if (map.containsKey(cmd)) {
                    cmd = map.get(cmd);
                } else {
                    logger.warn("Invalid command '{}' for channel '{}' received ", cmd, channelID);
                    return;
                }

                String response = sendGetRequest(PARSET_URL, "syn=" + param, "value=" + cmd);
                if (response != null) {
                    State newState = new StringType(response);
                    updateState(channelID, newState);
                }
            } else {
                logger.warn("A 'key' needs to be configured in order to control the Guntamatic Heating System");
            }
        }
    }

    private void parseAndUpdate(String html) {
        String[] daqdata = html.split("\\n");

        for (Integer i : channels.keySet()) {
            String channel = channels.get(i);
            Unit<?> unit = units.get(i);
            if ((channel != null) && (i < daqdata.length)) {
                String value = daqdata[i];
                Channel chn = thing.getChannel(channel);
                if ((chn != null) && (value != null)) {
                    value = value.trim();
                    String typeName = chn.getAcceptedItemType();
                    try {
                        State newState = null;
                        if (typeName != null) {
                            switch (typeName) {
                                case CoreItemFactory.SWITCH:
                                    // Guntamatic uses German OnOff when configured to German and English OnOff for
                                    // all other languages
                                    if ("ON".equals(value) || "EIN".equals(value)) {
                                        newState = OnOffType.ON;
                                    } else if ("OFF".equals(value) || "AUS".equals(value)) {
                                        newState = OnOffType.OFF;
                                    }
                                    break;
                                case CoreItemFactory.NUMBER:
                                    newState = new DecimalType(value);
                                    break;
                                case NUMBER_DIMENSIONLESS:
                                case NUMBER_TEMPERATURE:
                                case NUMBER_VOLUME:
                                case NUMBER_TIME:
                                    if (unit != null) {
                                        newState = new QuantityType<>(Double.parseDouble(value), unit);
                                    }
                                    break;
                                case CoreItemFactory.STRING:
                                    newState = new StringType(value);
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (newState != null) {
                            updateState(channel, newState);
                        } else {
                            logger.warn("Data for unknown typeName '{}' or unknown unit received", typeName);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("NumberFormatException: {}", ((e.getMessage() != null) ? e.getMessage() : ""));
                    }
                }
            } else {
                logger.warn("Data for not intialized ChannelId '{}' received", i);
            }
        }
    }

    private void parseAndJsonInit(String html) {
        try {
            // remove non JSON compliant, empty element ",,"
            JsonArray json = JsonParser.parseString(html.replace(",,", ",")).getAsJsonArray();
            for (int i = 1; i < json.size(); i++) {
                JsonObject points = json.get(i).getAsJsonObject();
                if (points.has("id") && points.has("type")) {
                    int id = points.get("id").getAsInt();
                    String type = points.get("type").getAsString();
                    types.put(id, type);
                }
            }
        } catch (JsonParseException | IllegalStateException | ClassCastException e) {
            logger.warn("Invalid JSON data will be ignored: '{}'", html.replace(",,", ","));
        }
    }

    private void parseAndInit(String html) {
        String[] daqdesc = html.split("\\n");
        List<Channel> channelList = new ArrayList<>();

        // make sure that static channels are present
        for (String channelID : staticChannelIDs) {
            Channel channel = thing.getChannel(channelID);
            if (channel == null) {
                logger.warn("Static Channel '{}' is not present: remove and re-add Thing", channelID);
            } else {
                channelList.add(channel);
            }
        }

        // add dynamic channels, based on data provided by Guntamatic Heating System
        for (int i = 0; i < daqdesc.length; i++) {
            String[] param = daqdesc[i].split(";");
            String label = param[0].replace("C02", "CO2");

            if (!"reserved".equals(label)) {
                String channel = toLowerCamelCase(replaceUmlaut(label));
                label = label.substring(0, 1).toUpperCase() + label.substring(1);

                String unitStr = ((param.length == 1) || param[1].isBlank()) ? "" : param[1].trim();
                Unit<?> unit = guessUnit(unitStr);

                boolean channelInitialized = channels.containsValue(channel);
                if (!channelInitialized) {
                    String itemType;
                    String pattern;
                    String type = types.get(i);
                    if (type == null) {
                        type = "";
                    }

                    if ("boolean".equals(type)) {
                        itemType = CoreItemFactory.SWITCH;
                        pattern = "";
                    } else if ("integer".equals(type)) {
                        itemType = guessItemType(unit);
                        pattern = "%d";
                        if (unit != null) {
                            pattern += " %unit%";
                        }
                    } else if ("float".equals(type)) {
                        itemType = guessItemType(unit);
                        pattern = "%.2f";
                        if (unit != null) {
                            pattern += " %unit%";
                        }
                    } else if ("string".equals(type)) {
                        itemType = CoreItemFactory.STRING;
                        pattern = "%s";
                    } else {
                        if (unitStr.isBlank()) {
                            itemType = CoreItemFactory.STRING;
                            pattern = "%s";
                        } else {
                            itemType = guessItemType(unit);
                            pattern = "%.2f";
                            if (unit != null) {
                                pattern += " %unit%";
                            }
                        }
                    }

                    ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channel);
                    guntamaticChannelTypeProvider.addChannelType(channelTypeUID, channel, itemType,
                            "Guntamatic " + label, false, pattern);
                    Channel newChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channel), itemType)
                            .withType(channelTypeUID).withKind(ChannelKind.STATE).withLabel(label).build();
                    channelList.add(newChannel);
                    channels.put(i, channel);
                    if (unit != null) {
                        units.put(i, unit);
                    }

                    logger.debug(
                            "Supported Channel: Idx: '{}', Name: '{}'/'{}', Type: '{}'/'{}', Unit: '{}', Pattern '{}' ",
                            String.format("%03d", i), label, channel, type, itemType, unitStr, pattern);
                }
            }
        }
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
        channelsInitialized = true;
    }

    private @Nullable Unit<?> guessUnit(String unit) {
        Unit<?> finalUnit = MAP_UNIT.get(unit);
        if (!unit.isBlank() && (finalUnit == null)) {
            logger.warn("Unsupported unit '{}' detected", unit);
        }
        return finalUnit;
    }

    private String guessItemType(@Nullable Unit<?> unit) {
        String itemType = (unit != null) ? MAP_UNIT_ITEMTYPE.get(unit) : CoreItemFactory.NUMBER;
        if (itemType == null) {
            itemType = CoreItemFactory.NUMBER;
            logger.warn("Unsupported unit '{}' detected: using native '{}' type", unit, itemType);
        }
        return itemType;
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

    private String toLowerCamelCase(String input) {
        char delimiter = ' ';
        String output = input.replace("´", "").replaceAll("[^\\w]", String.valueOf(delimiter));

        StringBuilder builder = new StringBuilder();
        boolean nextCharLow = true;

        for (int i = 0; i < output.length(); i++) {
            char currentChar = output.charAt(i);
            if (delimiter == currentChar) {
                nextCharLow = false;
            } else if (nextCharLow) {
                builder.append(Character.toLowerCase(currentChar));
            } else {
                builder.append(Character.toUpperCase(currentChar));
                nextCharLow = true;
            }
        }
        return builder.toString();
    }

    private @Nullable String sendGetRequest(String url, String... params) {
        String errorReason = "";
        String req = "http://" + config.hostname + url;

        if (!config.key.isBlank()) {
            req += "?key=" + config.key;
        }

        for (int i = 0; i < params.length; i++) {
            if ((i == 0) && config.key.isBlank()) {
                req += "?";
            } else {
                req += "&";
            }
            req += params[i];
        }

        Request request = httpClient.newRequest(req);
        request.method(HttpMethod.GET).timeout(30, TimeUnit.SECONDS).header(HttpHeader.ACCEPT_ENCODING, "gzip");

        try {
            ContentResponse contentResponse = request.send();
            if (HttpStatus.OK_200 == contentResponse.getStatus()) {
                if (!this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                }
                try {
                    String response = new String(contentResponse.getContent(), Charset.forName(config.encoding));
                    if (url.equals(DAQEXTDESC_URL)) {
                        parseAndJsonInit(response);
                    } else if (url.equals(DAQDATA_URL)) {
                        parseAndUpdate(response);
                    } else if (url.equals(DAQDESC_URL)) {
                        parseAndInit(response);
                    } else {
                        logger.debug(req);
                        // PARSET_URL via return
                    }
                    return response;
                } catch (IllegalArgumentException e) {
                    errorReason = String.format("IllegalArgumentException: %s",
                            ((e.getMessage() != null) ? e.getMessage() : ""));
                }
            } else {
                errorReason = String.format("Guntamatic request failed with %d: %s", contentResponse.getStatus(),
                        ((contentResponse.getReason() != null) ? contentResponse.getReason() : ""));
            }
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: Guntamatic was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", ((e.getMessage() != null) ? e.getMessage() : ""));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorReason = String.format("InterruptedException: %s", ((e.getMessage() != null) ? e.getMessage() : ""));
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorReason);
        return null;
    }

    private void pollGuntamatic() {
        if (!channelsInitialized) {
            if (!config.key.isBlank()) {
                sendGetRequest(DAQEXTDESC_URL);
            }
            sendGetRequest(DAQDESC_URL);
        } else {
            sendGetRequest(DAQDATA_URL);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GuntamaticConfiguration.class);
        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid hostname configuration");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            pollingFuture = scheduler.scheduleWithFixedDelay(this::pollGuntamatic, 1, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingFuture;
        if (job != null) {
            job.cancel(true);
            pollingFuture = null;
        }
        channelsInitialized = false;
    }
}
