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
package org.openhab.binding.automower.internal.things;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.joda.time.DateTime;
import org.openhab.binding.automower.internal.AutomowerBindingConstants;
import org.openhab.binding.automower.internal.bridge.AutomowerBridge;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerHandler extends BaseThingHandler {
    private static final String NO_ID = "NO_ID";
    private final Logger logger = LoggerFactory.getLogger(AutomowerHandler.class);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_AUTOMOWER);

    private @Nullable AutomowerConfiguration config;

    private AtomicReference<String> automowerId = new AtomicReference<String>(NO_ID);
    private AtomicLong commandDuration = new AtomicLong(60); // 60min default
    private long lastQueryTime = 0L;

    private @Nullable ScheduledFuture<?> automowerPollingJob;
    private long automowerPollingIntervalS = TimeUnit.HOURS.toSeconds(1);
    private long maxQueryFrequencyNanos = TimeUnit.MINUTES.toNanos(1);

    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,
            new Locale(System.getProperty("user.language")));

    public AutomowerHandler(Thing thing) {
        super(thing);
    }

    class AutomowerPollingRunnable implements Runnable {

        @Override
        public void run() {
            Bridge bridge = getBridge();
            if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
                updateAutomowerState();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }

        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannels(channelUID);
        } else {
            sendCommand(channelUID, command);
        }
    }

    private void sendCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_COMMAND_DURATION:
                if (command instanceof DecimalType) {
                    commandDuration.set(((DecimalType) command).longValue());
                    updateState(CHANNEL_COMMAND_DURATION, new DecimalType(commandDuration.get()));
                }
                break;

            case CHANNEL_COMMAND:
                if (command instanceof StringType) {
                    sendAutomowerCommand(command);
                } else {
                    logger.debug("Received command of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

        }
    }

    private void refreshChannels(ChannelUID channelUID) {
        updateAutomowerState();
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            config = getConfigAs(AutomowerConfiguration.class);

            final String configMowerId = config.getMowerId();
            if (configMowerId != null) {
                automowerId.set(configMowerId);

                // bridgeHandler.registerLightStatusListener(this);
                startAutomowerPolling();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/conf-error-no-mower-id");
            }

        } else {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }

    }

    @Nullable
    private AutomowerBridge getAutomowerBridge() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof AutomowerBridgeHandler) {
                AutomowerBridgeHandler bridgeHandler = (AutomowerBridgeHandler) handler;
                return bridgeHandler.getAutomowerBridge();
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (!automowerId.get().equals(NO_ID)) {
            // bridgeHandler.unregisterLightStatusListener(this);
            stopAutomowerPolling();
            automowerId.set(NO_ID);
        }
    }

    private void startAutomowerPolling() {
        if (automowerPollingJob == null || automowerPollingJob.isCancelled()) {
            if (config.getPollingInterval() < 1) {
                logger.info("No valid polling interval specified. Using default value: {}s", automowerPollingIntervalS);
            } else {
                automowerPollingIntervalS = config.getPollingInterval();
            }
            automowerPollingJob = scheduler.scheduleWithFixedDelay(new AutomowerPollingRunnable(), 1,
                    automowerPollingIntervalS, TimeUnit.SECONDS);
        }
    }

    private void stopAutomowerPolling() {
        if (automowerPollingJob != null && !automowerPollingJob.isCancelled()) {
            automowerPollingJob.cancel(true);
            automowerPollingJob = null;
        }
    }

    private boolean isValidResult(Mower mower) {
        return mower != null && mower.getAttributes() != null && mower.getAttributes().getMetadata() != null
                && mower.getAttributes().getBattery() != null && mower.getAttributes().getSystem() != null;
    }

    private boolean isConnected(Mower mower) {
        if (mower != null && mower.getAttributes() != null && mower.getAttributes().getMetadata() != null
                && mower.getAttributes().getMetadata().isConnected()) {
            return true;
        }
        return false;
    }

    private synchronized void updateAutomowerState() {
        if (System.nanoTime() - lastQueryTime > maxQueryFrequencyNanos) {
            lastQueryTime = System.nanoTime();
            String id = automowerId.get();
            try {
                Mower mower = getAutomowerBridge().getAutomowerStatus(id);

                if (isValidResult(mower)) {
                    initializeProperties(mower);

                    updateChannelState(mower);

                    if (isConnected(mower)) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/comm-error-mower-not-connected-to-cloud");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/comm-error-query-mower-failed");
                }
            } catch (AutomowerCommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error-query-mower-failed");
                logger.warn("Unable to query automower status for:  {}", id, e);
            }
        }
    }

    private synchronized void sendAutomowerCommand(Command command) {
        String id = automowerId.get();
        try {
            if (getAutomowerBridge().sendAutomowerCommand(id, command.toString(), commandDuration.get())) {
                updateState(CHANNEL_COMMAND_RESPONSE,
                        new StringType("Successfully sent command " + command.toString()));
            } else {
                updateState(CHANNEL_COMMAND_RESPONSE, new StringType("Unable to send command " + command.toString()));
            }
        } catch (AutomowerCommunicationException e) {
            updateState(CHANNEL_COMMAND_RESPONSE,
                    new StringType("Unable to send command " + command.toString() + ": " + e.getMessage()));

            logger.warn("Unable to send command to automower: {}", id, e);
        }

        updateAutomowerState();
    }

    private void updateChannelState(Mower mower) {
        if (isValidResult(mower)) {
            /*
             * if (info.getError() != null) {
             * updateErrorInfo(info.getError());
             * refreshLastErrorInfo();
             * } else {
             * clearErrorInfo();
             * }
             */
            updateState(CHANNEL_MOWER_NAME, new StringType(mower.getAttributes().getSystem().getName()));

            updateState(CHANNEL_STATUS_MODE, new StringType(mower.getAttributes().getMower().getMode().name()));
            updateState(CHANNEL_STATUS_ACTIVITY, new StringType(mower.getAttributes().getMower().getActivity().name()));
            updateState(CHANNEL_STATUS_STATE, new StringType(mower.getAttributes().getMower().getState().name()));

            updateState(CHANNEL_STATUS_LAST_UPDATE, new DateTimeType(
                    new DateTime(mower.getAttributes().getMetadata().getStatusTimestamp()).toString()));
            updateState(CHANNEL_STATUS_BATTERY,
                    new DecimalType(mower.getAttributes().getBattery().getBatteryPercent()));

            updateState(CHANNEL_STATUS_ERROR_CODE, new DecimalType(mower.getAttributes().getMower().getErrorCode()));
            updateState(CHANNEL_STATUS_ERROR_TIMESTAMP, new DateTimeType(
                    new DateTime(mower.getAttributes().getMower().getErrorCodeTimestamp()).toString()));

            updateState(CHANNEL_COMMAND_DURATION, new DecimalType(commandDuration.get()));
        }
    }

    private void initializeProperties(Mower mower) {
        Map<String, String> properties = editProperties();

        properties.put(AutomowerBindingConstants.AUTOMOWER_ID, mower.getId());

        if (mower.getAttributes() != null && mower.getAttributes().getSystem() != null) {
            properties.put(AutomowerBindingConstants.AUTOMOWER_SERIAL_NUMBER,
                    mower.getAttributes().getSystem().getSerialNumber());
            properties.put(AutomowerBindingConstants.AUTOMOWER_MODEL, mower.getAttributes().getSystem().getModel());
            properties.put(AutomowerBindingConstants.AUTOMOWER_NAME, mower.getAttributes().getSystem().getName());
        }

        updateProperties(properties);
    }

}
