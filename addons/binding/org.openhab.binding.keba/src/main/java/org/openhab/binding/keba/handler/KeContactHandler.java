/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.keba.handler;

import static org.openhab.binding.keba.KebaBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.keba.KebaBindingConstants.KebaFirmware;
import org.openhab.binding.keba.KebaBindingConstants.KebaSeries;
import org.openhab.binding.keba.KebaBindingConstants.KebaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link KeContactHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class KeContactHandler extends BaseThingHandler {

    public static final String IP_ADDRESS = "ipAddress";
    public static final String POLLING_REFRESH_INTERVAL = "refreshInterval";
    public static final int REPORT_INTERVAL = 3000;
    public static final int PING_TIME_OUT = 3000;
    public static final int BUFFER_SIZE = 1024;
    public static final int REMOTE_PORT_NUMBER = 7090;
    private static final String CACHE_REPORT_1 = "REPORT_1";
    private static final String CACHE_REPORT_2 = "REPORT_2";
    private static final String CACHE_REPORT_3 = "REPORT_3";

    private final Logger logger = LoggerFactory.getLogger(KeContactHandler.class);

    protected JsonParser parser = new JsonParser();

    private ScheduledFuture<?> pollingJob;
    private static KeContactTransceiver transceiver = new KeContactTransceiver();
    private ExpiringCacheMap<String, ByteBuffer> cache;

    private int maxPresetCurrent = 0;
    private int maxSystemCurrent = 63000;
    private KebaType type;
    private KebaFirmware firmware;
    private KebaSeries series;

    @SuppressWarnings("null")
    public KeContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (getConfig().get(IP_ADDRESS) != null && !getConfig().get(IP_ADDRESS).equals("")) {
            transceiver.registerHandler(this);

            if (pollingJob == null || pollingJob.isCancelled()) {
                try {
                    pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0,
                            ((BigDecimal) getConfig().get(POLLING_REFRESH_INTERVAL)).intValue(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An exception occurred while scheduling the polling job");
                }
            }

            cache = new ExpiringCacheMap<>(
                    Math.max((((BigDecimal) getConfig().get(POLLING_REFRESH_INTERVAL)).intValue()) - 5, 0) * 1000);

            cache.put(CACHE_REPORT_1, () -> transceiver.send("report 1", getHandler()));
            cache.put(CACHE_REPORT_2, () -> transceiver.send("report 2", getHandler()));
            cache.put(CACHE_REPORT_3, () -> transceiver.send("report 3", getHandler()));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP address or port number not set");
        }
    }

    @Override
    public void dispose() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        transceiver.unRegisterHandler(this);
    }

    public String getIPAddress() {
        return getConfig().get(IP_ADDRESS) != null ? (String) getConfig().get(IP_ADDRESS) : "";
    }

    private KeContactHandler getHandler() {
        return this;
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    protected Configuration getConfig() {
        return super.getConfig();
    }

    private Runnable pollingRunnable = () -> {
        try {
            long stamp = System.currentTimeMillis();
            if (!InetAddress.getByName(((String) getConfig().get(IP_ADDRESS))).isReachable(PING_TIME_OUT)) {
                logger.debug("Ping timed out after '{}' milliseconds", System.currentTimeMillis() - stamp);
                transceiver.unRegisterHandler(getHandler());
            } else {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    ByteBuffer response = cache.get(CACHE_REPORT_1);
                    if (response != null) {
                        onData(response);
                    }

                    Thread.sleep(REPORT_INTERVAL);

                    response = cache.get(CACHE_REPORT_2);
                    if (response != null) {
                        onData(response);
                    }

                    Thread.sleep(REPORT_INTERVAL);

                    response = cache.get(CACHE_REPORT_3);
                    if (response != null) {
                        onData(response);
                    }
                }
            }
        } catch (InterruptedException | NumberFormatException | IOException e) {
            logger.debug("An exception occurred while polling the KEBA KeContact '{}': {}", getThing().getUID(),
                    e.getMessage(), e);
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "An exception occurred while while polling the charging station");
        }
    };

    protected void onData(ByteBuffer byteBuffer) {
        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());
        response = StringUtils.chomp(response);

        if (response.contains("TCH-OK")) {
            // ignore confirmation messages which are not JSON
            return;
        }

        try {
            JsonObject readObject = parser.parse(response).getAsJsonObject();

            for (Entry<String, JsonElement> entry : readObject.entrySet()) {
                switch (entry.getKey()) {
                    case "Product": {
                        Map<String, String> properties = editProperties();
                        String product = entry.getValue().getAsString().trim();
                        properties.put(CHANNEL_MODEL, product);
                        updateProperties(properties);
                        if (product.contains("P20")) {
                            type = KebaType.P20;
                        } else if (product.contains("P30")) {
                            type = KebaType.P30;
                        }
                        series = KebaSeries.getSeries(product.substring(13, 14).charAt(0));
                        break;
                    }
                    case "Serial": {
                        Map<String, String> properties = editProperties();
                        properties.put(CHANNEL_SERIAL, entry.getValue().getAsString());
                        updateProperties(properties);
                        break;
                    }
                    case "Firmware": {
                        Map<String, String> properties = editProperties();
                        properties.put(CHANNEL_FIRMWARE, entry.getValue().getAsString());
                        updateProperties(properties);
                        firmware = KebaFirmware.getFirmware(entry.getValue().getAsString());
                        break;
                    }
                    case "Plug": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 0: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.OFF);
                                break;
                            }
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.OFF);
                                break;
                            }
                            case 3: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.ON);
                                break;
                            }
                            case 5: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.OFF);
                                break;
                            }
                            case 7: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.ON);
                                break;
                            }
                        }
                        break;
                    }
                    case "State": {
                        State newState = new DecimalType(entry.getValue().getAsInt());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_STATE), newState);
                        break;
                    }
                    case "Enable sys": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ENABLED), OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ENABLED), OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Curr HW": {
                        int state = entry.getValue().getAsInt();
                        maxSystemCurrent = state;
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_SYSTEM_CURRENT), newState);
                        if (maxSystemCurrent < maxPresetCurrent) {
                            transceiver.send("curr " + String.valueOf(maxSystemCurrent), this);
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT),
                                    new DecimalType(maxSystemCurrent));
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT_RANGE),
                                    new PercentType((maxSystemCurrent - 6000) * 100 / (maxSystemCurrent - 6000)));
                        }
                        break;
                    }
                    case "Curr user": {
                        int state = entry.getValue().getAsInt();
                        maxPresetCurrent = state;
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT),
                                new DecimalType(state));
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT_RANGE),
                                new PercentType(Math.min(100, (state - 6000) * 100 / (maxSystemCurrent - 6000))));
                        break;
                    }
                    case "Curr FS": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_FAILSAFE_CURRENT), newState);
                        break;
                    }
                    case "Max curr": {
                        int state = entry.getValue().getAsInt();
                        maxPresetCurrent = state;
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PILOT_CURRENT), new DecimalType(state));
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PILOT_PWM), new DecimalType(state));
                        break;
                    }
                    case "Output": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT), OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT), OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Input": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_INPUT), OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_INPUT), OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Sec": {
                        long state = entry.getValue().getAsLong();

                        Calendar uptime = Calendar.getInstance();
                        uptime.setTimeZone(TimeZone.getTimeZone("GMT"));
                        uptime.setTimeInMillis(state * 1000);
                        SimpleDateFormat pFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        pFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_UPTIME),
                                new DateTimeType(pFormatter.format(uptime.getTime())));
                        break;
                    }
                    case "U1": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_U1), newState);
                        break;
                    }
                    case "U2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_U2), newState);
                        break;
                    }
                    case "U3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_U3), newState);
                        break;
                    }
                    case "I1": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I1), newState);
                        break;
                    }
                    case "I2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I2), newState);
                        break;
                    }
                    case "I3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I3), newState);
                        break;
                    }
                    case "P": {
                        long state = entry.getValue().getAsLong();
                        State newState = new DecimalType(state / 1000);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_POWER), newState);
                        break;
                    }
                    case "PF": {
                        int state = entry.getValue().getAsInt();
                        State newState = new PercentType(state / 10);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_POWER_FACTOR), newState);
                        break;
                    }
                    case "E pres": {
                        long state = entry.getValue().getAsLong();
                        State newState = new DecimalType(state / 10);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SESSION_CONSUMPTION), newState);
                        break;
                    }
                    case "E total": {
                        long state = entry.getValue().getAsLong();
                        State newState = new DecimalType(state / 10);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TOTAL_CONSUMPTION), newState);
                        break;
                    }
                }
            }
        } catch (JsonParseException e) {
            logger.debug("Invalid JSON data will be ignored: '{}'", response);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof RefreshType)) {
            scheduler.schedule(pollingRunnable, 0, TimeUnit.SECONDS);
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_MAX_PRESET_CURRENT: {
                    if (command instanceof DecimalType) {
                        transceiver.send(
                                "curr " + String.valueOf(
                                        Math.min(Math.max(6000, ((DecimalType) command).intValue()), maxSystemCurrent)),
                                this);
                    }
                    break;
                }
                case CHANNEL_MAX_PRESET_CURRENT_RANGE: {
                    if (command instanceof OnOffType || command instanceof IncreaseDecreaseType
                            || command instanceof PercentType) {
                        int newValue = 6000;
                        if (command == IncreaseDecreaseType.INCREASE) {
                            newValue = Math.min(Math.max(6000, maxPresetCurrent + 1), maxSystemCurrent);
                        } else if (command == IncreaseDecreaseType.DECREASE) {
                            newValue = Math.min(Math.max(6000, maxPresetCurrent - 1), maxSystemCurrent);
                        } else if (command == OnOffType.ON) {
                            newValue = maxSystemCurrent;
                        } else if (command == OnOffType.OFF) {
                            newValue = 6000;
                        } else if (command instanceof PercentType) {
                            newValue = 6000 + (maxSystemCurrent - 6000) * ((PercentType) command).intValue() / 100;
                        } else {
                            return;
                        }

                        transceiver.send("curr " + String.valueOf(newValue), this);
                    }
                    break;
                }
                case CHANNEL_ENABLED: {
                    if (command instanceof OnOffType) {
                        if (command == OnOffType.ON) {
                            transceiver.send("ena 1", this);
                        } else if (command == OnOffType.OFF) {
                            transceiver.send("ena 0", this);
                        } else {
                            return;
                        }
                    }
                    break;
                }
                case CHANNEL_OUTPUT: {
                    if (command instanceof OnOffType) {
                        if (command == OnOffType.ON) {
                            transceiver.send("output 1", this);
                        } else if (command == OnOffType.OFF) {
                            transceiver.send("output 0", this);
                        } else {
                            return;
                        }
                    }
                    break;
                }
                case CHANNEL_DISPLAY: {
                    if (command instanceof StringType) {
                        if (type == KebaType.P30 && (series == KebaSeries.C || series == KebaSeries.X)) {
                            String cmd = command.toString();
                            int maxLength = (cmd.length() < 23) ? cmd.length() : 23;
                            transceiver.send("display 0 0 0 0 " + cmd.substring(0, maxLength), this);
                        } else {
                            logger.warn("'Display' is not supported on a KEBA KeContact {}:{}", type, series);
                        }
                    }
                    break;
                }
            }
        }
    }
}
