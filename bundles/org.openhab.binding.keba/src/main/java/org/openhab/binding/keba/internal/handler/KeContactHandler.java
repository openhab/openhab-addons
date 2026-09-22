/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal.handler;

import static org.openhab.binding.keba.internal.KebaBindingConstants.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.keba.internal.KebaBindingConstants.KebaSeries;
import org.openhab.binding.keba.internal.KebaBindingConstants.KebaType;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.util.StringUtils;
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
@NonNullByDefault
public class KeContactHandler extends BaseThingHandler {

    public static final String IP_ADDRESS = "ipAddress";
    public static final String POLLING_REFRESH_INTERVAL = "refreshInterval";
    public static final int POLLING_REFRESH_INTERVAL_DEFAULT = 15;
    public static final int REPORT_INTERVAL = 3000;
    public static final int BUFFER_SIZE = 1024;
    public static final int REMOTE_PORT_NUMBER = 7090;
    private static final String CACHE_REPORT_1 = "REPORT_1";
    private static final String CACHE_REPORT_2 = "REPORT_2";
    private static final String CACHE_REPORT_3 = "REPORT_3";
    private static final String CACHE_REPORT_100 = "REPORT_100";
    public static final int SOCKET_TIME_OUT_MS = 3000;
    public static final int SOCKET_CHECK_PORT_NUMBER = 80;
    private static final int MAX_CONSECUTIVE_COMMUNICATION_FAILURES = 3;

    private final Logger logger = LoggerFactory.getLogger(KeContactHandler.class);

    private final KeContactTransceiver transceiver;

    private @Nullable ScheduledFuture<?> pollingJob;
    private final List<ScheduledFuture<?>> reportTasks = Collections.synchronizedList(new ArrayList<>());
    private @Nullable ExpiringCacheMap<String, ByteBuffer> cache;

    private String ipAddress = "";
    private int refreshInterval = POLLING_REFRESH_INTERVAL_DEFAULT;
    private volatile int maxPresetCurrent = 0;
    private volatile int maxSystemCurrent = 63000;
    private @Nullable KebaType type;
    private @Nullable KebaSeries series;
    private volatile int lastState = -1; // trigger a report100 at startup
    private final AtomicBoolean isReport100needed = new AtomicBoolean(true);
    private final AtomicInteger consecutiveCommunicationFailures = new AtomicInteger();

    public KeContactHandler(Thing thing, KeContactTransceiver transceiver) {
        super(thing);
        this.transceiver = transceiver;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        dispose();
        initialize();
    }

    @Override
    public void initialize() {
        KeContactConfiguration configuration = getConfigAs(KeContactConfiguration.class);
        String configuredIpAddress = configuration.ipAddress;
        ipAddress = configuredIpAddress != null ? configuredIpAddress : "";
        refreshInterval = configuration.refreshInterval;

        // the reachability check and UDP registration involve blocking I/O, so run them in the background instead
        // of blocking the calling thread
        scheduler.execute(() -> {
            try {
                if (isKebaReachable()) {
                    transceiver.registerHandler(this);

                    ExpiringCacheMap<String, ByteBuffer> localCache = new ExpiringCacheMap<>(
                            Math.max(refreshInterval - 5, 0) * 1000);
                    cache = localCache;

                    localCache.put(CACHE_REPORT_1, () -> transceiver.send("report 1", getHandler()));
                    localCache.put(CACHE_REPORT_2, () -> transceiver.send("report 2", getHandler()));
                    localCache.put(CACHE_REPORT_3, () -> transceiver.send("report 3", getHandler()));
                    localCache.put(CACHE_REPORT_100, () -> transceiver.send("report 100", getHandler()));

                    ScheduledFuture<?> localPollingJob = pollingJob;
                    if (localPollingJob == null || localPollingJob.isCancelled()) {
                        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingRunnable, 0, refreshInterval,
                                TimeUnit.SECONDS);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-address-port");
                }
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-init [\"" + e.getMessage() + "\"]");
            }
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(KeContactActions.class);
    }

    private boolean isKebaReachable() throws IOException {
        boolean isReachable = false;
        SocketAddress sockAddr = new InetSocketAddress(getIPAddress(), SOCKET_CHECK_PORT_NUMBER);
        Socket socket = new Socket();
        try {
            socket.connect(sockAddr, SOCKET_TIME_OUT_MS);
            isReachable = true;
        } finally {
            socket.close();
        }
        logger.debug("isKebaReachable() returns {}", isReachable);
        return isReachable;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            localPollingJob.cancel(true);
            pollingJob = null;
        }
        synchronized (reportTasks) {
            reportTasks.forEach(task -> task.cancel(true));
            reportTasks.clear();
        }

        transceiver.unRegisterHandler(this);
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    private KeContactHandler getHandler() {
        return this;
    }

    void updateStatusFromTransceiver(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        updateStatus(status, statusDetail, description);
    }

    private void pollingRunnable() {
        try {
            logger.debug("Running pollingRunnable to connect Keba wallbox");
            long stamp = System.currentTimeMillis();
            if (!isKebaReachable()) {
                logger.debug("isKebaReachable() timed out after '{}' milliseconds", System.currentTimeMillis() - stamp);
                markCommunicationFailure(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error-timeout");
            } else {
                ExpiringCacheMap<String, ByteBuffer> localCache = cache;
                if (localCache == null) {
                    return;
                }

                processReport(localCache, CACHE_REPORT_1);
                scheduleReport(localCache, CACHE_REPORT_2, REPORT_INTERVAL);
                scheduleReport(localCache, CACHE_REPORT_3, REPORT_INTERVAL * 2L);
                if (isReport100needed.getAndSet(false)) {
                    scheduleReport(localCache, CACHE_REPORT_100, REPORT_INTERVAL * 3L);
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.debug("Polling job has been interrupted for handler of thing '{}'.", getThing().getUID());
        } catch (IOException e) {
            logger.debug("An error occurred while polling the KEBA KeContact '{}': {}", getThing().getUID(),
                    e.getMessage(), e);
            markCommunicationFailure(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error-polling");
        }
    }

    private void markCommunicationFailure(ThingStatusDetail detail, String description) {
        if (consecutiveCommunicationFailures.incrementAndGet() >= MAX_CONSECUTIVE_COMMUNICATION_FAILURES) {
            updateStatus(ThingStatus.OFFLINE, detail, description);
        }
    }

    private void markCommunicationSuccess() {
        consecutiveCommunicationFailures.set(0);
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void scheduleReport(ExpiringCacheMap<String, ByteBuffer> localCache, String report, long delayMillis) {
        ScheduledFuture<?> task = scheduler.schedule(() -> processReport(localCache, report), delayMillis,
                TimeUnit.MILLISECONDS);
        synchronized (reportTasks) {
            reportTasks.removeIf(ScheduledFuture::isDone);
            reportTasks.add(task);
        }
    }

    private void processReport(ExpiringCacheMap<String, ByteBuffer> localCache, String report) {
        ByteBuffer response = localCache.get(report);
        if (response == null) {
            logger.debug("Missing response from Keba station for '{}'", report);
        } else {
            onData(response);
        }
    }

    protected void onData(ByteBuffer byteBuffer) {
        markCommunicationSuccess();

        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());
        response = Objects.requireNonNull(StringUtils.chomp(response));

        if (response.contains("TCH-OK")) {
            // ignore confirmation messages which are not JSON
            return;
        }

        try {
            JsonObject readObject = JsonParser.parseString(response).getAsJsonObject();

            for (Entry<String, JsonElement> entry : readObject.entrySet()) {
                switch (entry.getKey()) {
                    case "Product": {
                        Map<String, String> properties = editProperties();
                        String product = entry.getValue().getAsString().trim();
                        properties.put(PROPERTY_MODEL, product);
                        updateProperties(properties);
                        if (product.contains("P20")) {
                            type = KebaType.P20;
                        } else if (product.contains("P30")) {
                            type = KebaType.P30;
                        }
                        if (product.length() > 13) {
                            try {
                                series = KebaSeries.getSeries(product.charAt(13));
                            } catch (IllegalArgumentException e) {
                                logger.debug("Unknown KEBA product series in product '{}'.", product);
                            }
                        } else {
                            logger.debug("KEBA product value is too short to determine its series: '{}'.", product);
                        }
                        break;
                    }
                    case "Serial": {
                        Map<String, String> properties = editProperties();
                        properties.put(PROPERTY_SERIAL, entry.getValue().getAsString());
                        updateProperties(properties);
                        break;
                    }
                    case "Firmware": {
                        Map<String, String> properties = editProperties();
                        properties.put(PROPERTY_FIRMWARE, entry.getValue().getAsString());
                        updateProperties(properties);
                        break;
                    }
                    case "Plug": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 0: {
                                updateState(CHANNEL_WALLBOX, OnOffType.OFF);
                                updateState(CHANNEL_VEHICLE, OnOffType.OFF);
                                updateState(CHANNEL_PLUG_LOCKED, OnOffType.OFF);
                                break;
                            }
                            case 1: {
                                updateState(CHANNEL_WALLBOX, OnOffType.ON);
                                updateState(CHANNEL_VEHICLE, OnOffType.OFF);
                                updateState(CHANNEL_PLUG_LOCKED, OnOffType.OFF);
                                break;
                            }
                            case 3: {
                                updateState(CHANNEL_WALLBOX, OnOffType.ON);
                                updateState(CHANNEL_VEHICLE, OnOffType.OFF);
                                updateState(CHANNEL_PLUG_LOCKED, OnOffType.ON);
                                break;
                            }
                            case 5: {
                                updateState(CHANNEL_WALLBOX, OnOffType.ON);
                                updateState(CHANNEL_VEHICLE, OnOffType.ON);
                                updateState(CHANNEL_PLUG_LOCKED, OnOffType.OFF);
                                break;
                            }
                            case 7: {
                                updateState(CHANNEL_WALLBOX, OnOffType.ON);
                                updateState(CHANNEL_VEHICLE, OnOffType.ON);
                                updateState(CHANNEL_PLUG_LOCKED, OnOffType.ON);
                                break;
                            }
                        }
                        break;
                    }
                    case "State": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(CHANNEL_STATE, newState);
                        if (lastState != state) {
                            // the state is different from the last one, so we will trigger a report100
                            isReport100needed.set(true);
                            lastState = state;
                        }
                        break;
                    }
                    case "Enable sys": {
                        updateState(CHANNEL_ENABLED_SYSTEM, OnOffType.from(entry.getValue().getAsInt() == 1));
                        break;
                    }
                    case "Enable user": {
                        updateState(CHANNEL_ENABLED_USER, OnOffType.from(entry.getValue().getAsInt() == 1));
                        break;
                    }
                    case "Curr HW": {
                        int state = entry.getValue().getAsInt();
                        maxSystemCurrent = state;
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_MAX_SYSTEM_CURRENT, newState);
                        if (maxSystemCurrent != 0) {
                            if (maxSystemCurrent < maxPresetCurrent) {
                                transceiver.send("curr " + maxSystemCurrent, this);
                                updateState(CHANNEL_MAX_PRESET_CURRENT,
                                        new QuantityType<>(maxSystemCurrent / 1000.0, Units.AMPERE));
                                updateState(CHANNEL_MAX_PRESET_CURRENT_RANGE, new QuantityType<>(
                                        (maxSystemCurrent - 6000) * 100 / (maxSystemCurrent - 6000), Units.PERCENT));
                            }
                        } else {
                            logger.debug("maxSystemCurrent is 0. Ignoring.");
                        }
                        break;
                    }
                    case "Curr user": {
                        int state = entry.getValue().getAsInt();
                        maxPresetCurrent = state;
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_MAX_PRESET_CURRENT, newState);
                        if (maxSystemCurrent != 0) {
                            updateState(CHANNEL_MAX_PRESET_CURRENT_RANGE, new QuantityType<>(
                                    Math.min(100, (state - 6000) * 100 / (maxSystemCurrent - 6000)), Units.PERCENT));
                        }
                        break;
                    }
                    case "Curr FS": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_FAILSAFE_CURRENT, newState);
                        break;
                    }
                    case "Max curr": {
                        int state = entry.getValue().getAsInt();
                        maxPresetCurrent = state;
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_PILOT_CURRENT, newState);
                        break;
                    }
                    case "Max curr %": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 10.0, Units.PERCENT);
                        updateState(CHANNEL_PILOT_PWM, newState);
                        break;
                    }
                    case "Output": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(CHANNEL_OUTPUT, OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(CHANNEL_OUTPUT, OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Input": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(CHANNEL_INPUT, OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(CHANNEL_INPUT, OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Sec": {
                        long state = entry.getValue().getAsLong();
                        State newState = new QuantityType<>(state, Units.SECOND);
                        updateState(CHANNEL_UPTIME, newState);
                        break;
                    }
                    case "U1": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state, Units.VOLT);
                        updateState(CHANNEL_U1, newState);
                        break;
                    }
                    case "U2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state, Units.VOLT);
                        updateState(CHANNEL_U2, newState);
                        break;
                    }
                    case "U3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state, Units.VOLT);
                        updateState(CHANNEL_U3, newState);
                        break;
                    }
                    case "I1": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_I1, newState);
                        break;
                    }
                    case "I2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_I2, newState);
                        break;
                    }
                    case "I3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 1000.0, Units.AMPERE);
                        updateState(CHANNEL_I3, newState);
                        break;
                    }
                    case "P": {
                        long state = entry.getValue().getAsLong();
                        State newState = new QuantityType<>(state / 1000.0, Units.WATT);
                        updateState(CHANNEL_POWER, newState);
                        break;
                    }
                    case "PF": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 10.0, Units.PERCENT);
                        updateState(CHANNEL_POWER_FACTOR, newState);
                        break;
                    }
                    case "E pres": {
                        long state = entry.getValue().getAsLong();
                        State newState = new QuantityType<>(state / 10.0, Units.WATT_HOUR);
                        updateState(CHANNEL_SESSION_CONSUMPTION, newState);
                        break;
                    }
                    case "E total": {
                        long state = entry.getValue().getAsLong();
                        State newState = new QuantityType<>(state / 10.0, Units.WATT_HOUR);
                        updateState(CHANNEL_TOTAL_CONSUMPTION, newState);
                        break;
                    }
                    case "AuthON": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(CHANNEL_AUTHON, newState);
                        break;
                    }
                    case "Authreq": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(CHANNEL_AUTHREQ, newState);
                        break;
                    }
                    case "RFID tag": {
                        String state = entry.getValue().getAsString().trim();
                        State newState = new StringType(state);
                        updateState(CHANNEL_SESSION_RFID_TAG, newState);
                        break;
                    }
                    case "RFID class": {
                        String state = entry.getValue().getAsString().trim();
                        State newState = new StringType(state);
                        updateState(CHANNEL_SESSION_RFID_CLASS, newState);
                        break;
                    }
                    case "Session ID": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(CHANNEL_SESSION_SESSION_ID, newState);
                        break;
                    }
                    case "Setenergy": {
                        int state = entry.getValue().getAsInt();
                        State newState = new QuantityType<>(state / 10.0, Units.WATT_HOUR);
                        updateState(CHANNEL_SETENERGY, newState);
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
            // let's assume we do frequent enough polling and ignore the REFRESH request here
            // in order to prevent too many channel state updates
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_MAX_PRESET_CURRENT: {
                    if (command instanceof QuantityType<?> quantityCommand) {
                        QuantityType<?> value = Objects.requireNonNull(quantityCommand.toUnit("mA"));

                        transceiver.send("curr " + Math.min(Math.max(6000, value.intValue()), maxSystemCurrent), this);
                    }
                    break;
                }
                case CHANNEL_MAX_PRESET_CURRENT_RANGE: {
                    if (command instanceof OnOffType || command instanceof IncreaseDecreaseType
                            || command instanceof QuantityType<?>) {
                        long newValue = 6000;
                        if (command == IncreaseDecreaseType.INCREASE) {
                            newValue = Math.min(Math.max(6000, maxPresetCurrent + 1), maxSystemCurrent);
                        } else if (command == IncreaseDecreaseType.DECREASE) {
                            newValue = Math.min(Math.max(6000, maxPresetCurrent - 1), maxSystemCurrent);
                        } else if (command == OnOffType.ON) {
                            newValue = maxSystemCurrent;
                        } else if (command == OnOffType.OFF) {
                            newValue = 6000;
                        } else if (command instanceof QuantityType<?> quantityCommand) {
                            QuantityType<?> value = Objects.requireNonNull(quantityCommand.toUnit("%"));
                            newValue = Math.round(6000 + (maxSystemCurrent - 6000) * value.doubleValue() / 100.0);
                        } else {
                            return;
                        }
                        transceiver.send("curr " + newValue, this);
                    }
                    break;
                }
                case CHANNEL_ENABLED_USER: {
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
                        setDisplay(command.toString(), -1, -1);
                    }
                    break;
                }
                case CHANNEL_SETENERGY: {
                    if (command instanceof QuantityType<?> quantityCommand) {
                        QuantityType<?> value = Objects.requireNonNull(quantityCommand.toUnit(Units.WATT_HOUR));
                        transceiver.send(
                                "setenergy " + Math.min(Math.max(0, Math.round(value.doubleValue() * 10.0)), 999999999),
                                this);
                    }
                    break;
                }
                case CHANNEL_AUTHENTICATE: {
                    if (command instanceof StringType) {
                        String cmd = command.toString();
                        // cmd must contain ID + CLASS (works only if the RFID TAG is in the whitelist of the Keba
                        // station)
                        transceiver.send("start " + cmd, this);
                    }
                    break;
                }
            }
        }
    }

    public void setDisplay(@Nullable String text, int durationMin, int durationMax) {
        String displayText = text != null ? text : "";
        if (type == KebaType.P30 && (series == KebaSeries.C || series == KebaSeries.X)) {
            int maxLength = (displayText.length() < 23) ? displayText.length() : 23;
            int a = 1;
            if (durationMin < 0 || durationMax < 0) {
                a = 0;
                durationMin = 0;
                durationMax = 0;
            }
            transceiver.send("display " + a + " " + durationMin + " " + durationMax + " 0 "
                    + displayText.substring(0, maxLength), this);
        } else {
            logger.warn("'Display' is not supported on a KEBA KeContact {}:{}", type, series);
        }
    }
}
