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
package org.openhab.binding.intesis.internal.handler;

import static org.openhab.binding.intesis.internal.IntesisBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.intesis.internal.IntesisDynamicStateDescriptionProvider;
import org.openhab.binding.intesis.internal.api.IntesisHomeHttpApi;
import org.openhab.binding.intesis.internal.config.IntesisHomeConfiguration;
import org.openhab.binding.intesis.internal.enums.IntesisHomeModeEnum;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Data;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Datapoints;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Descr;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Dp;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Dpval;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Id;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Info;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.Response;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
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
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link IntesisHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class IntesisHomeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IntesisHomeHandler.class);
    private final IntesisHomeHttpApi api;

    private final Map<String, String> properties = new HashMap<>();

    private final IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider;

    private final Gson gson = new Gson();

    private IntesisHomeConfiguration config = new IntesisHomeConfiguration();

    private @Nullable ScheduledFuture<?> refreshJob;

    public IntesisHomeHandler(final Thing thing, final HttpClient httpClient,
            IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider) {
        super(thing);
        this.api = new IntesisHomeHttpApi(config, httpClient);
        this.intesisStateDescriptionProvider = intesisStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(IntesisHomeConfiguration.class);
        if (config.ipAddress.isEmpty() && config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP-Address and password not set");
            return;
        } else if (config.ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP-Address not set");
            return;
        } else if (config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Password not set");
            return;
        } else {
            // start background initialization:
            scheduler.submit(() -> {
                populateProperties();
                // query available dataPoints and build dynamic channels
                postRequestInSession(sessionId -> "{\"command\":\"getavailabledatapoints\",\"data\":{\"sessionID\":\""
                        + sessionId + "\"}}", this::handleDataPointsResponse);
                updateProperties(properties);
            });
        }
    }

    @Override
    public void dispose() {
        logger.debug("IntesisHomeHandler disposed.");
        final ScheduledFuture<?> refreshJob = this.refreshJob;

        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int uid = 0;
        int value = 0;
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            getAllUidValues();
        } else {
            switch (channelId) {
                case CHANNEL_TYPE_POWER:
                    uid = 1;
                    value = command.equals(OnOffType.OFF) ? 0 : 1;
                    break;
                case CHANNEL_TYPE_MODE:
                    uid = 2;
                    value = IntesisHomeModeEnum.valueOf(command.toString()).getMode();
                    break;
                case CHANNEL_TYPE_FANSPEED:
                    uid = 4;
                    if (("AUTO").equals(command.toString())) {
                        value = 0;
                    } else {
                        value = Integer.parseInt(command.toString());
                    }
                    break;
                case CHANNEL_TYPE_VANESUD:
                case CHANNEL_TYPE_VANESLR:
                    switch (command.toString()) {
                        case "AUTO":
                            value = 0;
                            break;
                        case "1":
                        case "2":
                        case "3":
                        case "4":
                        case "5":
                        case "6":
                        case "7":
                        case "8":
                        case "9":
                            value = Integer.parseInt(command.toString());
                            break;
                        case "SWING":
                            value = 10;
                            break;
                        case "SWIRL":
                            value = 11;
                            break;
                        case "WIDE":
                            value = 12;
                            break;
                    }
                    switch (channelId) {
                        case CHANNEL_TYPE_VANESUD:
                            uid = 5;
                            break;
                        case CHANNEL_TYPE_VANESLR:
                            uid = 6;
                            break;
                    }
                    break;
                case CHANNEL_TYPE_TARGETTEMP:
                    uid = 9;
                    if (command instanceof QuantityType newVal) {
                        newVal = newVal.toUnit(SIUnits.CELSIUS);
                        if (newVal != null) {
                            value = newVal.intValue() * 10;
                        }
                    }
                    break;
            }
        }
        if (uid != 0) {
            final int uId = uid;
            final int newValue = value;
            scheduler.submit(() -> {
                postRequestInSession(
                        sessionId -> "{\"command\":\"setdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId
                                + "\", \"uid\":" + uId + ",\"value\":" + newValue + "}}",
                        r -> updateStatus(ThingStatus.ONLINE));
            });
        }
    }

    public @Nullable String login() {
        // lambda's can't modify local variables, so we use an array here to get around the issue
        String[] sessionId = new String[1];
        postRequest(
                "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\"" + config.password + "\"}}",
                resp -> {
                    Data data = gson.fromJson(resp.data, Data.class);
                    if (data != null) {
                        Id id = gson.fromJson(data.id, Id.class);
                        if (id != null) {
                            sessionId[0] = id.sessionID;
                        }
                    }
                });
        if (sessionId[0] != null && !sessionId[0].isEmpty()) {
            updateStatus(ThingStatus.ONLINE);
            return sessionId[0];
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "SessionId not received");
            return null;
        }
    }

    public @Nullable String logout(String sessionId) {
        String contentString = "{\"command\":\"logout\",\"data\":{\"sessionID\":\"" + sessionId + "\"}}";
        return api.postRequest(config.ipAddress, contentString);
    }

    public void populateProperties() {
        postRequest("{\"command\":\"getinfo\",\"data\":\"\"}", resp -> {
            Data data = gson.fromJson(resp.data, Data.class);
            if (data != null) {
                Info info = gson.fromJson(data.info, Info.class);
                if (info != null) {
                    properties.put(PROPERTY_VENDOR, "Intesis");
                    properties.put(PROPERTY_MODEL_ID, info.deviceModel);
                    properties.put(PROPERTY_SERIAL_NUMBER, info.sn);
                    properties.put(PROPERTY_FIRMWARE_VERSION, info.fwVersion);
                    properties.put(PROPERTY_MAC_ADDRESS, info.wlanSTAMAC);
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        });
    }

    public void getWiFiSignal() {
        postRequest("{\"command\":\"getinfo\",\"data\":\"\"}", resp -> {
            Data data = gson.fromJson(resp.data, Data.class);
            if (data != null) {
                Info info = gson.fromJson(data.info, Info.class);
                if (info != null) {
                    String rssi = info.rssi;
                    int dbm = Integer.valueOf(rssi);
                    int strength = -1;
                    if (dbm > -60) {
                        strength = 4;
                    } else if (dbm > -70) {
                        strength = 3;
                    } else if (dbm > -80) {
                        strength = 2;
                    } else if (dbm > -90) {
                        strength = 1;
                    } else {
                        strength = 0;
                    }
                    DecimalType signalStrength = new DecimalType(strength);
                    updateState(CHANNEL_TYPE_RSSI, signalStrength);

                }
            }
        });
    }

    public void addChannel(String channelId, String itemType, @Nullable final Collection<String> options) {
        if (thing.getChannel(channelId) == null) {
            logger.trace("Channel '{}' for UID to be added", channelId);
            ThingBuilder thingBuilder = editThing();
            final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId), itemType)
                    .withType(channelTypeUID).withKind(ChannelKind.STATE).build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }
        if (options != null) {
            final List<StateOption> stateOptions = options.stream()
                    .map(e -> new StateOption(e, e.substring(0, 1) + e.substring(1).toLowerCase()))
                    .collect(Collectors.toList());
            logger.trace("StateOptions : '{}'", stateOptions);
            intesisStateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId),
                    stateOptions);
        }
    }

    private void postRequest(String request, Consumer<Response> handler) {
        try {
            logger.trace("request : '{}'", request);
            String response = api.postRequest(config.ipAddress, request);
            if (response != null) {
                Response resp = gson.fromJson(response, Response.class);
                if (resp != null) {
                    boolean success = resp.success;
                    if (success) {
                        handler.accept(resp);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Request unsuccessful");
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No Response");
            }
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void postRequestInSession(UnaryOperator<String> requestFactory, Consumer<Response> handler) {
        String sessionId = login();
        if (sessionId != null) {
            try {
                String request = requestFactory.apply(sessionId);
                postRequest(request, handler);
            } finally {
                logout(sessionId);
            }
        }
    }

    private void handleDataPointsResponse(Response response) {
        try {
            Data data = gson.fromJson(response.data, Data.class);
            if (data != null) {
                Dp dp = gson.fromJson(data.dp, Dp.class);
                if (dp != null) {
                    Datapoints[] datapoints = gson.fromJson(dp.datapoints, Datapoints[].class);
                    if (datapoints != null) {
                        for (Datapoints datapoint : datapoints) {
                            Descr descr = gson.fromJson(datapoint.descr, Descr.class);
                            String channelId = "";
                            String itemType = "String";
                            switch (datapoint.uid) {
                                case 2:
                                    if (descr != null) {
                                        List<String> opModes = new ArrayList<>();
                                        for (String modString : descr.states) {
                                            switch (modString) {
                                                case "0":
                                                    opModes.add("AUTO");
                                                    break;
                                                case "1":
                                                    opModes.add("HEAT");
                                                    break;
                                                case "2":
                                                    opModes.add("DRY");
                                                    break;
                                                case "3":
                                                    opModes.add("FAN");
                                                    break;
                                                case "4":
                                                    opModes.add("COOL");
                                                    break;
                                            }
                                            properties.put("supported modes", opModes.toString());
                                            channelId = CHANNEL_TYPE_MODE;
                                            addChannel(channelId, itemType, opModes);
                                        }
                                    }
                                    break;
                                case 4:
                                    if (descr != null) {
                                        List<String> fanLevels = new ArrayList<>();
                                        for (String fanString : descr.states) {
                                            if ("AUTO".contentEquals(fanString)) {
                                                fanLevels.add("AUTO");
                                            } else {
                                                fanLevels.add(fanString);
                                            }
                                        }
                                        properties.put("supported fan levels", fanLevels.toString());
                                        channelId = CHANNEL_TYPE_FANSPEED;
                                        addChannel(channelId, itemType, fanLevels);
                                    }
                                    break;
                                case 5:
                                case 6:
                                    List<String> swingModes = new ArrayList<>();
                                    if (descr != null) {
                                        for (String swingString : descr.states) {
                                            if ("AUTO".contentEquals(swingString)) {
                                                swingModes.add("AUTO");
                                            } else if ("10".contentEquals(swingString)) {
                                                swingModes.add("SWING");
                                            } else if ("11".contentEquals(swingString)) {
                                                swingModes.add("SWIRL");
                                            } else if ("12".contentEquals(swingString)) {
                                                swingModes.add("WIDE");
                                            } else {
                                                swingModes.add(swingString);
                                            }

                                        }
                                    }
                                    switch (datapoint.uid) {
                                        case 5:
                                            channelId = CHANNEL_TYPE_VANESUD;
                                            properties.put("supported vane up/down modes", swingModes.toString());
                                            addChannel(channelId, itemType, swingModes);
                                            break;
                                        case 6:
                                            channelId = CHANNEL_TYPE_VANESLR;
                                            properties.put("supported vane left/right modes", swingModes.toString());
                                            addChannel(channelId, itemType, swingModes);
                                            break;
                                    }
                                    break;
                                case 9:
                                    channelId = CHANNEL_TYPE_TARGETTEMP;
                                    itemType = "Number:Temperature";
                                    addChannel(channelId, itemType, null);
                                    break;
                                case 10:
                                    channelId = CHANNEL_TYPE_AMBIENTTEMP;
                                    itemType = "Number:Temperature";
                                    addChannel(channelId, itemType, null);
                                    break;
                                case 14:
                                    channelId = CHANNEL_TYPE_ERRORSTATUS;
                                    itemType = "Switch";
                                    addChannel(channelId, itemType, null);
                                    break;
                                case 15:
                                    channelId = CHANNEL_TYPE_ERRORCODE;
                                    itemType = "String";
                                    addChannel(channelId, itemType, null);
                                    break;
                                case 37:
                                    channelId = CHANNEL_TYPE_OUTDOORTEMP;
                                    itemType = "Number:Temperature";
                                    addChannel(channelId, itemType, null);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        logger.trace("Start Refresh Job");
        refreshJob = scheduler.scheduleWithFixedDelay(this::getAllUidValues, 0, config.pollingInterval,
                TimeUnit.SECONDS);
    }

    /**
     * Update device status and all channels
     */
    private void getAllUidValues() {
        postRequestInSession(sessionId -> "{\"command\":\"getdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId
                + "\", \"uid\":\"all\"}}", this::handleDataPointValues);
        getWiFiSignal();
    }

    private void handleDataPointValues(Response response) {
        try {
            Data data = gson.fromJson(response.data, Data.class);
            if (data != null) {
                Dpval[] dpval = gson.fromJson(data.dpval, Dpval[].class);
                if (dpval != null) {
                    for (Dpval element : dpval) {
                        logger.trace("UID : {} ; value : {}", element.uid, element.value);
                        switch (element.uid) {
                            case 1:
                                updateState(CHANNEL_TYPE_POWER,
                                        OnOffType.from(!"0".equals(String.valueOf(element.value))));
                                break;
                            case 2:
                                switch (element.value) {
                                    case 0:
                                        updateState(CHANNEL_TYPE_MODE, StringType.valueOf("AUTO"));
                                        break;
                                    case 1:
                                        updateState(CHANNEL_TYPE_MODE, StringType.valueOf("HEAT"));
                                        break;
                                    case 2:
                                        updateState(CHANNEL_TYPE_MODE, StringType.valueOf("DRY"));
                                        break;
                                    case 3:
                                        updateState(CHANNEL_TYPE_MODE, StringType.valueOf("FAN"));
                                        break;
                                    case 4:
                                        updateState(CHANNEL_TYPE_MODE, StringType.valueOf("COOL"));
                                        break;
                                }
                                break;
                            case 4:
                                if ((element.value) == 0) {
                                    updateState(CHANNEL_TYPE_FANSPEED, StringType.valueOf("AUTO"));
                                } else {
                                    updateState(CHANNEL_TYPE_FANSPEED,
                                            StringType.valueOf(String.valueOf(element.value)));
                                }
                                break;
                            case 5:
                            case 6:
                                State state;
                                if ((element.value) == 0) {
                                    state = StringType.valueOf("AUTO");
                                } else if ((element.value) == 10) {
                                    state = StringType.valueOf("SWING");
                                } else if ((element.value) == 11) {
                                    state = StringType.valueOf("SWIRL");
                                } else if ((element.value) == 12) {
                                    state = StringType.valueOf("WIDE");
                                } else {
                                    state = StringType.valueOf(String.valueOf(element.value));
                                }
                                switch (element.uid) {
                                    case 5:
                                        updateState(CHANNEL_TYPE_VANESUD, state);
                                        break;
                                    case 6:
                                        updateState(CHANNEL_TYPE_VANESLR, state);
                                        break;
                                }
                                break;
                            case 9:
                                int unit = Math.round((element.value) / 10);
                                State stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                                updateState(CHANNEL_TYPE_TARGETTEMP, stateValue);
                                break;
                            case 10:
                                unit = Math.round((element.value) / 10);
                                stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                                updateState(CHANNEL_TYPE_AMBIENTTEMP, stateValue);
                                break;
                            case 14:
                                updateState(CHANNEL_TYPE_ERRORSTATUS,
                                        OnOffType.from(!"0".equals(String.valueOf(element.value))));
                                break;
                            case 15:
                                updateState(CHANNEL_TYPE_ERRORCODE, StringType.valueOf(String.valueOf(element.value)));
                                break;
                            case 37:
                                unit = Math.round((element.value) / 10);
                                stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                                updateState(CHANNEL_TYPE_OUTDOORTEMP, stateValue);
                                break;
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
