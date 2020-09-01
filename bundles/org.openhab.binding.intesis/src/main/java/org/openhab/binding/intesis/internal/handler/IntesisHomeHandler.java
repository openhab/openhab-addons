/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.intesis.internal.IntesisBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.intesis.internal.IntesisConfiguration;
import org.openhab.binding.intesis.internal.IntesisDynamicStateDescriptionProvider;
import org.openhab.binding.intesis.internal.IntesisHomeModeEnum;
import org.openhab.binding.intesis.internal.api.IntesisHomeHttpApi;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.data;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.datapoints;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.descr;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.dp;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.dpval;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.id;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.info;
import org.openhab.binding.intesis.internal.gson.IntesisHomeJSonDTO.response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link IntesisHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class IntesisHomeHandler extends BaseThingHandler implements DynamicStateDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(IntesisHomeHandler.class);
    private final IntesisHomeHttpApi api;
    private IntesisConfiguration config = new IntesisConfiguration();

    private final Map<ChannelUID, @Nullable StateDescription> descriptions = new ConcurrentHashMap<>();
    private final Map<String, String> properties = new HashMap<>();

    private IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider;

    private @Nullable ScheduledFuture<?> refreshJob;

    private String ipAddress = "";
    private String password = "";
    private String sessionId = "";

    final Gson gson = new Gson();

    public IntesisHomeHandler(final Thing thing, final HttpClient httpClient,
            IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider) {
        super(thing);
        this.api = new IntesisHomeHttpApi(config, httpClient);
        this.intesisStateDescriptionProvider = intesisStateDescriptionProvider;
    }

    private static String beautify(final String camelCaseWording) {
        final StringBuilder b = new StringBuilder();
        for (final String s : StringUtils.splitByCharacterTypeCamelCase(camelCaseWording)) {
            b.append(" ");
            b.append(s);
        }
        final StringBuilder bs = new StringBuilder();
        for (final String t : StringUtils.splitByWholeSeparator(b.toString(), " _")) {
            bs.append(" ");
            bs.append(t);
        }

        return WordUtils.capitalizeFully(bs.toString()).trim();
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);
        final IntesisConfiguration config = getConfigAs(IntesisConfiguration.class);
        ipAddress = config.ipAddress;
        password = config.password;
        if (ipAddress.isEmpty() || password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        // start background initialization:
        scheduler.schedule(() -> {
            try {
                String contentString = "{\"command\":\"getinfo\",\"data\":\"\"}";
                String response = api.postRequest(ipAddress, contentString);
                logger.trace("getInfo response : {}", response);
                if (response != null) {
                    boolean success = getSuccess(response);
                    if (success) {
                        response resp = gson.fromJson(response, response.class);
                        data data = gson.fromJson(resp.data.toString(), data.class);
                        info info = gson.fromJson(data.info.toString(), info.class);
                        properties.put(PROPERTY_VENDOR, "Intesis");
                        properties.put(PROPERTY_MODEL_ID, info.deviceModel);
                        properties.put(PROPERTY_SERIAL_NUMBER, info.sn);
                        properties.put(PROPERTY_FIRMWARE_VERSION, info.fwVersion);
                        properties.put(PROPERTY_MAC_ADDRESS, info.wlanSTAMAC);
                        updateProperties(properties);
                        login();
                        if (!sessionId.isEmpty()) {
                            updateStatus(ThingStatus.ONLINE);
                            contentString = "{\"command\":\"getavailabledatapoints\",\"data\":{\"sessionID\":\""
                                    + sessionId + "\"}}";
                            response = api.postRequest(ipAddress, contentString);
                            logger.trace("available Datapoints response : {}", response);
                            if (response != null) {
                                success = getSuccess(response);
                                if (success) {
                                    logout();
                                    resp = gson.fromJson(response, response.class);
                                    data = gson.fromJson(resp.data.toString(), data.class);
                                    dp dp = gson.fromJson(data.dp.toString(), dp.class);
                                    datapoints[] datapoints = gson.fromJson(dp.datapoints, datapoints[].class);
                                    for (int i = 0; i < datapoints.length; i++) {
                                        descr descr = gson.fromJson(datapoints[i].descr, descr.class);
                                        String channelId = "";
                                        String itemType = "String";
                                        switch (datapoints[i].uid) {
                                            case 2:
                                                List<String> opModes = new ArrayList<>();
                                                String[] modString = descr.states;
                                                for (int i1 = 0; i1 < modString.length; i1++) {
                                                    switch (modString[i1]) {
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
                                                }
                                                properties.put("Supported modes", opModes.toString());
                                                updateProperties(properties);
                                                channelId = CHANNEL_TYPE_MODE;
                                                addChannel(channelId, itemType, opModes);
                                                break;
                                            case 4:
                                                List<String> fanLevels = new ArrayList<>();
                                                String[] fanString = descr.states;
                                                for (int i1 = 0; i1 < fanString.length; i1++) {
                                                    if ("AUTO".contentEquals(fanString[i1])) {
                                                        fanLevels.add("AUTO");
                                                    } else {
                                                        fanLevels.add(fanString[i1]);
                                                    }
                                                }
                                                properties.put("Supported fan levels", fanLevels.toString());
                                                updateProperties(properties);
                                                channelId = CHANNEL_TYPE_FANSPEED;
                                                addChannel(channelId, itemType, fanLevels);
                                                break;
                                            case 5:
                                                List<String> swingUDModes = new ArrayList<>();
                                                String[] swingUDString = descr.states;
                                                for (int i1 = 0; i1 < swingUDString.length; i1++) {
                                                    if ("AUTO".contentEquals(swingUDString[i1])) {
                                                        swingUDModes.add("AUTO");
                                                    } else if ("10".contentEquals(swingUDString[i1])) {
                                                        swingUDModes.add("SWING");
                                                    } else if ("11".contentEquals(swingUDString[i1])) {
                                                        swingUDModes.add("SWIRL");
                                                    } else if ("12".contentEquals(swingUDString[i1])) {
                                                        swingUDModes.add("WIDE");
                                                    } else {
                                                        swingUDModes.add(swingUDString[i1]);
                                                    }
                                                }
                                                channelId = CHANNEL_TYPE_VANESUD;
                                                properties.put("Supported vane up/down modes", swingUDModes.toString());
                                                updateProperties(properties);
                                                addChannel(channelId, itemType, swingUDModes);
                                                break;
                                            case 6:
                                                List<String> swingLRModes = new ArrayList<>();
                                                String[] swingLRString = descr.states;
                                                for (int i1 = 0; i1 < swingLRString.length; i1++) {
                                                    if ("AUTO".contentEquals(swingLRString[i1])) {
                                                        swingLRModes.add("AUTO");
                                                    } else if ("10".contentEquals(swingLRString[i1])) {
                                                        swingLRModes.add("SWING");
                                                    } else if ("11".contentEquals(swingLRString[i1])) {
                                                        swingLRModes.add("SWIRL");
                                                    } else if ("12".contentEquals(swingLRString[i1])) {
                                                        swingLRModes.add("WIDE");
                                                    } else {
                                                        swingLRModes.add(swingLRString[i1]);
                                                    }
                                                }
                                                channelId = CHANNEL_TYPE_VANESLR;
                                                properties.put("Supported vane left/right modes",
                                                        swingLRModes.toString());
                                                updateProperties(properties);
                                                addChannel(channelId, itemType, swingLRModes);
                                                break;
                                            case 9:
                                                logger.trace("UID : {} ; minValue : {}", datapoints[i].uid,
                                                        descr.minValue);
                                                logger.trace("UID : {} ; maxValue : {}", datapoints[i].uid,
                                                        descr.maxValue);
                                                channelId = CHANNEL_TYPE_TARGETTEMP;
                                                itemType = "Number:Temperature";
                                                addChannel(channelId, itemType, null);
                                                break;
                                            case 10:
                                                logger.trace("UID : {} ; minValue : {}", datapoints[i].uid,
                                                        descr.minValue);
                                                logger.trace("UID : {} ; maxValue : {}", datapoints[i].uid,
                                                        descr.maxValue);
                                                channelId = CHANNEL_TYPE_AMBIENTTEMP;
                                                itemType = "Number:Temperature";
                                                addChannel(channelId, itemType, null);
                                                break;
                                            case 37:
                                                logger.trace("Add Channel Outdoor Temperature");
                                                logger.trace("UID : {} ; description : {}", datapoints[i].uid,
                                                        datapoints[i].descr);
                                                logger.trace("UID : {} ; minValue : {}", datapoints[i].uid,
                                                        descr.minValue);
                                                logger.trace("UID : {} ; maxValue : {}", datapoints[i].uid,
                                                        descr.maxValue);
                                                channelId = CHANNEL_TYPE_OUTDOORTEMP;
                                                itemType = "Number:Temperature";
                                                addChannel(channelId, itemType, null);
                                                break;
                                        }
                                    }
                                }
                            } else {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                            }
                            logger.trace("Start Refresh Job");
                            refreshJob = scheduler.scheduleWithFixedDelay(this::getAllUidValues, 0,
                                    INTESIS_REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } finally {
            }
        }, 0, TimeUnit.SECONDS);
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
            // getAllUidValues();
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
                    uid = 5;
                    if (("AUTO").equals(command.toString())) {
                        value = 0;
                    } else if (("SWING").equals(command.toString())) {
                        value = 10;
                    } else if (("SWIRL").equals(command.toString())) {
                        value = 11;
                    } else if (("WIDE").equals(command.toString())) {
                        value = 12;
                    } else {
                        value = Integer.parseInt(command.toString());
                    }
                    break;
                case CHANNEL_TYPE_VANESLR:
                    uid = 6;
                    if (("AUTO").equals(command.toString())) {
                        value = 0;
                    } else if (("SWING").equals(command.toString())) {
                        value = 10;
                    } else if (("SWIRL").equals(command.toString())) {
                        value = 11;
                    } else if (("WIDE").equals(command.toString())) {
                        value = 12;
                    } else {
                        value = Integer.parseInt(command.toString());
                    }
                    break;
                case CHANNEL_TYPE_TARGETTEMP:
                    uid = 9;
                    value = (Integer.parseInt(command.toString().replace(" °C", ""))) * 10;
                    break;
            }
        }
        if (uid != 0) {
            login();
            String contentString = "{\"command\":\"setdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId
                    + "\", \"uid\":" + uid + ",\"value\":" + value + "}}";
            String response = api.postRequest(ipAddress, contentString);
            if (response != null) {
                boolean success = getSuccess(response);
                if (!success) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    String sessionString = "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\""
                            + password + "\"}}";
                    response = api.postRequest(ipAddress, sessionString);
                } else {
                    logout();
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }
    }

    /**
     * Update device status and all channels
     */
    public void getAllUidValues() {
        logger.debug("Polling IntesisHome device");
        login();
        String contentString = "{\"command\":\"getdatapointvalue\",\"data\":{\"sessionID\":\"" + sessionId
                + "\", \"uid\":\"all\"}}";
        String response = api.postRequest(ipAddress, contentString);
        logger.debug("Thing {} received response {}", this.getThing().getUID(), response);
        if (response != null) {
            boolean success = getSuccess(response);
            if (!success) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                logout();
                response resp = gson.fromJson(response, response.class);
                data data = gson.fromJson(resp.data.toString(), data.class);
                dpval[] dpval = gson.fromJson(data.dpval, dpval[].class);
                for (int i = 0; i < dpval.length; i++) {
                    logger.trace("UID : {} ; value : {}", dpval[i].uid, dpval[i].value);
                    switch (dpval[i].uid) {
                        case 1:
                            updateState(CHANNEL_TYPE_POWER,
                                    String.valueOf(dpval[i].value).equals("0") ? OnOffType.OFF : OnOffType.ON);
                            break;
                        case 2:
                            switch (dpval[i].value) {
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
                            if ((dpval[i].value) == 0) {
                                updateState(CHANNEL_TYPE_FANSPEED, StringType.valueOf("AUTO"));
                            } else {
                                updateState(CHANNEL_TYPE_FANSPEED, StringType.valueOf(String.valueOf(dpval[i].value)));
                            }
                            break;
                        case 5:
                            if ((dpval[i].value) == 0) {
                                updateState(CHANNEL_TYPE_VANESUD, StringType.valueOf("AUTO"));
                            } else if ((dpval[i].value) == 10) {
                                updateState(CHANNEL_TYPE_VANESUD, StringType.valueOf("SWING"));
                            } else if ((dpval[i].value) == 11) {
                                updateState(CHANNEL_TYPE_VANESUD, StringType.valueOf("SWIRL"));
                            } else if ((dpval[i].value) == 12) {
                                updateState(CHANNEL_TYPE_VANESUD, StringType.valueOf("WIDE"));
                            } else {
                                updateState(CHANNEL_TYPE_VANESUD, StringType.valueOf(String.valueOf(dpval[i].value)));
                            }
                            break;
                        case 6:
                            if ((dpval[i].value) == 0) {
                                updateState(CHANNEL_TYPE_VANESLR, StringType.valueOf("AUTO"));
                            } else if ((dpval[i].value) == 10) {
                                updateState(CHANNEL_TYPE_VANESLR, StringType.valueOf("SWING"));
                            } else if ((dpval[i].value) == 11) {
                                updateState(CHANNEL_TYPE_VANESLR, StringType.valueOf("SWIRL"));
                            } else if ((dpval[i].value) == 12) {
                                updateState(CHANNEL_TYPE_VANESLR, StringType.valueOf("WIDE"));
                            } else {
                                updateState(CHANNEL_TYPE_VANESLR, StringType.valueOf(String.valueOf(dpval[i].value)));
                            }
                            break;
                        case 9:
                            int unit = Math.round((dpval[i].value) / 10);
                            State stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                            updateState(CHANNEL_TYPE_TARGETTEMP, stateValue);
                            break;
                        case 10:
                            unit = Math.round((dpval[i].value) / 10);
                            stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                            updateState(CHANNEL_TYPE_AMBIENTTEMP, stateValue);
                            break;
                        case 37:
                            unit = Math.round((dpval[i].value) / 10);
                            stateValue = QuantityType.valueOf(unit, SIUnits.CELSIUS);
                            updateState(CHANNEL_TYPE_OUTDOORTEMP, stateValue);
                            break;
                    }
                }
            }
        }
    }

    public void login() {
        String contentString = "{\"command\":\"login\",\"data\":{\"username\":\"Admin\",\"password\":\"" + password
                + "\"}}";
        String response = api.postRequest(ipAddress, contentString);
        if (response != null) {
            response resp = gson.fromJson(response, response.class);
            boolean success = resp.success;
            if (success) {
                data data = gson.fromJson(resp.data.toString(), data.class);
                id id = gson.fromJson(data.id.toString(), id.class);
                if (id.sessionID.toString() != null && !id.sessionID.toString().isEmpty()) {
                    sessionId = id.sessionID.toString();
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }
    }

    public @Nullable String logout() {
        String contentString = "{\"command\":\"logout\",\"data\":{\"sessionID\":\"" + sessionId + "\"}}";
        String response = api.postRequest(ipAddress, contentString);
        return response;
    }

    public boolean getSuccess(String response) {
        response resp = gson.fromJson(response, response.class);
        boolean success = resp.success;
        return success;
    }

    @SuppressWarnings("null")
    public void addChannel(String channelId, String itemType, @Nullable final Collection<?> options) {
        if (thing.getChannel(channelId) == null) {
            logger.trace("Channel '{}' for UID to be added", channelId);
            ThingBuilder thingBuilder = editThing();
            final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId), itemType)
                    .withType(channelTypeUID).withKind(ChannelKind.STATE).build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());

            if (getThing().getChannel(channelId) != null) {
                if (options != null) {
                    final List<StateOption> stateOptions = options.stream()
                            .map(e -> new StateOption(e.toString(),
                                    e instanceof String ? beautify((String) e) : e.toString()))
                            .collect(Collectors.toList());
                    updateStateDescription(thing.getChannel(channelId).getUID(), stateOptions);
                }
            }
        }
    }

    private void updateStateDescription(ChannelUID channelUID, List<StateOption> stateOptionList) {
        StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withReadOnly(false)
                .withOptions(stateOptionList).build().toStateDescription();
        intesisStateDescriptionProvider.setDescription(channelUID, stateDescription);
    }

    public void setDescription(ChannelUID channelUID, @Nullable StateDescription description) {
        logger.debug("Adding state description for channel {}", channelUID);
        descriptions.put(channelUID, description);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescription description = descriptions.get(channel.getUID());
        return description;
    }
}
