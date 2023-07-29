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
package org.openhab.binding.intesis.internal.handler;

import static org.openhab.binding.intesis.internal.IntesisBindingConstants.*;
import static org.openhab.binding.intesis.internal.api.IntesisBoxMessage.*;
import static org.openhab.core.thing.Thing.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intesis.internal.IntesisDynamicStateDescriptionProvider;
import org.openhab.binding.intesis.internal.api.IntesisBoxChangeListener;
import org.openhab.binding.intesis.internal.api.IntesisBoxMessage;
import org.openhab.binding.intesis.internal.api.IntesisBoxSocketApi;
import org.openhab.binding.intesis.internal.config.IntesisBoxConfiguration;
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
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IntesisBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cody Cutrer - Initial contribution
 * @author Rocky Amatulli - additions to include id message handling, dynamic channel options based on limits.
 * @author Hans-Jörg Merk - refactored for openHAB 3.0 compatibility
 *
 */
@NonNullByDefault
public class IntesisBoxHandler extends BaseThingHandler implements IntesisBoxChangeListener {

    private final Logger logger = LoggerFactory.getLogger(IntesisBoxHandler.class);
    private @Nullable IntesisBoxSocketApi intesisBoxSocketApi;

    private final Map<String, String> properties = new HashMap<>();
    private final Map<String, List<String>> limits = new HashMap<>();

    private final IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider;

    private IntesisBoxConfiguration config = new IntesisBoxConfiguration();

    private double minTemp = 0.0, maxTemp = 0.0;

    private boolean hasProperties = false;

    private @Nullable ScheduledFuture<?> pollingTask;

    public IntesisBoxHandler(Thing thing, IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider) {
        super(thing);
        this.intesisStateDescriptionProvider = intesisStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        config = getConfigAs(IntesisBoxConfiguration.class);

        if (!config.ipAddress.isEmpty()) {

            updateStatus(ThingStatus.UNKNOWN);
            scheduler.submit(() -> {

                String readerThreadName = "OH-binding-" + getThing().getUID().getAsString();

                IntesisBoxSocketApi intesisLocalApi = intesisBoxSocketApi = new IntesisBoxSocketApi(config.ipAddress,
                        config.port, readerThreadName);
                intesisLocalApi.addIntesisBoxChangeListener(this);
                try {
                    intesisLocalApi.openConnection();
                    intesisLocalApi.sendId();
                    intesisLocalApi.sendLimitsQuery();
                    intesisLocalApi.sendAlive();

                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
            });
            pollingTask = scheduler.scheduleWithFixedDelay(this::polling, 3, 45, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No IP address specified)");
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> pollingTask = this.pollingTask;

        IntesisBoxSocketApi api = this.intesisBoxSocketApi;

        if (pollingTask != null) {
            pollingTask.cancel(true);
            this.pollingTask = null;
        }
        if (api != null) {
            api.closeConnection();
            api.removeIntesisBoxChangeListener(this);
        }
        super.dispose();
    }

    private synchronized void polling() {
        IntesisBoxSocketApi api = this.intesisBoxSocketApi;
        if (api != null) {
            if (!api.isConnected()) {
                try {
                    api.openConnection();
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
            api.sendAlive();
            api.sendId();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IntesisBoxSocketApi api = this.intesisBoxSocketApi;
        if (api != null) {
            if (!api.isConnected()) {
                logger.trace("Sending command failed, not connected");
                return;
            }
            if (command instanceof RefreshType) {
                logger.trace("Refresh channel {}", channelUID.getId());
                api.sendQuery(channelUID.getId());
                return;
            }
        }
        String value = "";
        String function = "";
        switch (channelUID.getId()) {
            case CHANNEL_TYPE_POWER:
                if (command instanceof OnOffType) {
                    function = "ONOFF";
                    value = command == OnOffType.ON ? "ON" : "OFF";
                }
                break;
            case CHANNEL_TYPE_TARGETTEMP:
                if (command instanceof QuantityType) {
                    QuantityType<?> celsiusTemperature = (QuantityType<?>) command;
                    celsiusTemperature = celsiusTemperature.toUnit(SIUnits.CELSIUS);
                    if (celsiusTemperature != null) {
                        double doubleValue = celsiusTemperature.doubleValue();
                        logger.trace("targetTemp double value = {}", doubleValue);
                        doubleValue = Math.max(minTemp, Math.min(maxTemp, doubleValue));
                        value = String.format("%.0f", doubleValue * 10);
                        function = "SETPTEMP";
                        logger.trace("targetTemp raw string = {}", value);
                    }
                }
                break;
            case CHANNEL_TYPE_MODE:
                function = "MODE";
                value = command.toString();
                break;
            case CHANNEL_TYPE_FANSPEED:
                function = "FANSP";
                value = command.toString();
                break;
            case CHANNEL_TYPE_VANESUD:
                function = "VANEUD";
                value = command.toString();
                break;
            case CHANNEL_TYPE_VANESLR:
                function = "VANELR";
                value = command.toString();
                break;
        }
        if (!value.isEmpty() || function.isEmpty()) {
            if (api != null) {
                logger.trace("Sending command {} to function {}", value, function);
                api.sendCommand(function, value);
            } else {
                logger.warn("Sending command failed, could not get API");
            }
        }
    }

    private void populateProperties(String[] value) {
        properties.put(PROPERTY_VENDOR, "Intesis");
        properties.put(PROPERTY_MODEL_ID, value[0]);
        properties.put(PROPERTY_MAC_ADDRESS, value[1]);
        properties.put("ipAddress", value[2]);
        properties.put("protocol", value[3]);
        properties.put(PROPERTY_FIRMWARE_VERSION, value[4]);
        properties.put("hostname", value[6]);
        updateProperties(properties);
        hasProperties = true;
    }

    private void receivedUpdate(String function, String receivedValue) {
        String value = receivedValue;
        logger.trace("receivedUpdate(): {} {}", function, value);
        switch (function) {
            case "ONOFF":
                updateState(CHANNEL_TYPE_POWER, OnOffType.from(value));
                break;

            case "SETPTEMP":
                if (value.equals("32768")) {
                    value = "0";
                }
                updateState(CHANNEL_TYPE_TARGETTEMP,
                        new QuantityType<Temperature>(Double.valueOf(value) / 10.0d, SIUnits.CELSIUS));
                break;
            case "AMBTEMP":
                if (Double.valueOf(value).isNaN()) {
                    value = "0";
                }
                updateState(CHANNEL_TYPE_AMBIENTTEMP,
                        new QuantityType<Temperature>(Double.valueOf(value) / 10.0d, SIUnits.CELSIUS));
                break;
            case "MODE":
                updateState(CHANNEL_TYPE_MODE, new StringType(value));
                break;
            case "FANSP":
                updateState(CHANNEL_TYPE_FANSPEED, new StringType(value));
                break;
            case "VANEUD":
                updateState(CHANNEL_TYPE_VANESUD, new StringType(value));
                break;
            case "VANELR":
                updateState(CHANNEL_TYPE_VANESLR, new StringType(value));
                break;
            case "ERRCODE":
                updateState(CHANNEL_TYPE_ERRORCODE, new StringType(value));
                break;
            case "ERRSTATUS":
                updateState(CHANNEL_TYPE_ERRORSTATUS, new StringType(value));
                if ("ERR".equals(value)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "device reported an error");
                }
                break;
        }
    }

    private void handleMessage(String data) {
        logger.debug("handleMessage(): Message received - {}", data);
        if (data.equals("ACK") || data.equals("")) {
            return;
        }
        if (data.startsWith(ID + ':')) {
            String[] value = data.substring(3).split(",");
            if (!hasProperties) {
                populateProperties(value);
            }
            DecimalType signalStrength = mapSignalStrength(Integer.parseInt(value[5]));
            updateState(CHANNEL_TYPE_RSSI, signalStrength);
            return;
        }
        IntesisBoxMessage message = IntesisBoxMessage.parse(data);
        if (message != null) {
            switch (message.getCommand()) {
                case LIMITS:
                    logger.debug("handleMessage(): Limits received - {}", data);
                    String function = message.getFunction();
                    if (function.equals("SETPTEMP")) {
                        List<Double> limits = message.getLimitsValue().stream().map(l -> Double.valueOf(l) / 10.0d)
                                .collect(Collectors.toList());
                        if (limits.size() == 2) {
                            minTemp = limits.get(0);
                            maxTemp = limits.get(1);
                        }
                        logger.trace("Property target temperatures {} added", message.getValue());
                        properties.put("targetTemperature limits", "[" + minTemp + "," + maxTemp + "]");
                        addChannel(CHANNEL_TYPE_TARGETTEMP, "Number:Temperature");
                    } else {
                        switch (function) {
                            case "MODE":
                                properties.put("supported modes", message.getValue());
                                limits.put(CHANNEL_TYPE_MODE, message.getLimitsValue());
                                addChannel(CHANNEL_TYPE_MODE, "String");
                                break;
                            case "FANSP":
                                properties.put("supported fan levels", message.getValue());
                                limits.put(CHANNEL_TYPE_FANSPEED, message.getLimitsValue());
                                addChannel(CHANNEL_TYPE_FANSPEED, "String");
                                break;
                            case "VANEUD":
                                properties.put("supported vane up/down modes", message.getValue());
                                limits.put(CHANNEL_TYPE_VANESUD, message.getLimitsValue());
                                addChannel(CHANNEL_TYPE_VANESUD, "String");
                                break;
                            case "VANELR":
                                properties.put("supported vane left/right modes", message.getValue());
                                limits.put(CHANNEL_TYPE_VANESLR, message.getLimitsValue());
                                addChannel(CHANNEL_TYPE_VANESLR, "String");
                                break;
                        }
                    }
                    updateProperties(properties);
                    break;
                case CHN:
                    receivedUpdate(message.getFunction(), message.getValue());
                    break;
            }
        }
    }

    public void addChannel(String channelId, String itemType) {
        if (thing.getChannel(channelId) == null) {
            logger.trace("Channel '{}' for UID to be added", channelId);
            ThingBuilder thingBuilder = editThing();
            final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId), itemType)
                    .withType(channelTypeUID).withKind(ChannelKind.STATE).build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }
        if (limits.containsKey(channelId)) {
            List<StateOption> options = new ArrayList<>();
            for (String mode : limits.get(channelId)) {
                options.add(
                        new StateOption(mode, mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase()));
            }
            intesisStateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId), options);
        }
    }

    @Override
    public void messageReceived(String messageLine) {
        logger.trace("messageReceived() : {}", messageLine);
        handleMessage(messageLine);
    }

    @Override
    public void connectionStatusChanged(ThingStatus status, @Nullable String message) {
        if (message != null) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
        this.updateStatus(status);
    }

    public static DecimalType mapSignalStrength(int dbm) {
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
        return new DecimalType(strength);
    }
}
