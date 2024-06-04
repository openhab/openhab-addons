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
package org.openhab.binding.plugwise.internal.handler;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.PlugwiseDeviceTask;
import org.openhab.binding.plugwise.internal.PlugwiseUtils;
import org.openhab.binding.plugwise.internal.config.PlugwiseRelayConfig;
import org.openhab.binding.plugwise.internal.config.PlugwiseRelayConfig.PowerStateChanging;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage.ExtensionCode;
import org.openhab.binding.plugwise.internal.protocol.ClockGetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.ClockGetResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.ClockSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.PowerBufferRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerBufferResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerCalibrationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerCalibrationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerChangeRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerInformationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerInformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.PowerLogIntervalSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.RealTimeClockGetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.RealTimeClockGetResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.RealTimeClockSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.Energy;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.binding.plugwise.internal.protocol.field.PowerCalibration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The {@link PlugwiseRelayDeviceHandler} handles channel updates and commands for a Plugwise device with a relay.
 * Relay devices are the Circle, Circle+ and Stealth.
 * </p>
 * <p>
 * A Circle maintains current energy usage by counting 'pulses' in a one or eight-second interval. Furthermore, it
 * stores hourly energy usage as well in a buffer. Each entry in the buffer contains usage for the last 4 full hours of
 * consumption. In order to convert pulses to energy (kWh) or power (W), a calculation is made in the {@link Energy}
 * class with {@link PowerCalibration} data.
 * </p>
 * <p>
 * A Circle+ is a special Circle. There is one Circle+ in a Plugwise network. The Circle+ serves as a master controller
 * in a Plugwise network. It also provides clock data to the other devices and sends messages from and to the Stick.
 * </p>
 * <p>
 * A Stealth behaves like a Circle but it has a more compact form factor.
 * </p>
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class PlugwiseRelayDeviceHandler extends AbstractPlugwiseThingHandler {

    private static final int INVALID_WATT_THRESHOLD = 10000;
    private static final int POWER_STATE_RETRIES = 3;

    private class PendingPowerStateChange {
        final OnOffType onOff;
        int retries;

        PendingPowerStateChange(OnOffType onOff) {
            this.onOff = onOff;
        }
    }

    private final PlugwiseDeviceTask clockUpdateTask = new PlugwiseDeviceTask("Clock update", scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return getChannelUpdateInterval(CHANNEL_CLOCK);
        }

        @Override
        public void runTask() {
            sendMessage(new ClockGetRequestMessage(macAddress));
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == ONLINE && isLinked(CHANNEL_CLOCK);
        }
    };

    private final PlugwiseDeviceTask currentPowerUpdateTask = new PlugwiseDeviceTask("Current power update",
            scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return getChannelUpdateInterval(CHANNEL_POWER);
        }

        @Override
        public void runTask() {
            if (isCalibrated()) {
                sendMessage(new PowerInformationRequestMessage(macAddress));
            }
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == ONLINE && (isLinked(CHANNEL_POWER)
                    || configuration.getPowerStateChanging() != PowerStateChanging.COMMAND_SWITCHING);
        }
    };

    private final PlugwiseDeviceTask energyUpdateTask = new PlugwiseDeviceTask("Energy update", scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return getChannelUpdateInterval(CHANNEL_ENERGY);
        }

        @Override
        public void runTask() {
            if (isRecentLogAddressKnown()) {
                updateEnergy();
            }
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == ONLINE && isLinked(CHANNEL_ENERGY);
        }
    };

    private final PlugwiseDeviceTask informationUpdateTask = new PlugwiseDeviceTask("Information update", scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return PlugwiseUtils.minComparable(getChannelUpdateInterval(CHANNEL_STATE),
                    getChannelUpdateInterval(CHANNEL_ENERGY));
        }

        @Override
        public void runTask() {
            updateInformation();
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == ONLINE && (isLinked(CHANNEL_STATE) || isLinked(CHANNEL_ENERGY));
        }
    };

    private final PlugwiseDeviceTask realTimeClockUpdateTask = new PlugwiseDeviceTask("Real-time clock update",
            scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return getChannelUpdateInterval(CHANNEL_REAL_TIME_CLOCK);
        }

        @Override
        public void runTask() {
            sendMessage(new RealTimeClockGetRequestMessage(macAddress));
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == ONLINE && deviceType == DeviceType.CIRCLE_PLUS
                    && isLinked(CHANNEL_REAL_TIME_CLOCK);
        }
    };

    private final PlugwiseDeviceTask setClockTask = new PlugwiseDeviceTask("Set clock", scheduler) {
        @Override
        public Duration getConfiguredInterval() {
            return Duration.ofDays(1);
        }

        @Override
        public void runTask() {
            if (deviceType == DeviceType.CIRCLE_PLUS) {
                // The Circle+ real-time clock needs to be updated first to prevent clock sync issues
                sendCommandMessage(new RealTimeClockSetRequestMessage(macAddress, LocalDateTime.now()));
                scheduler.schedule(
                        () -> sendCommandMessage(new ClockSetRequestMessage(macAddress, LocalDateTime.now())), 5,
                        TimeUnit.SECONDS);
            } else {
                sendCommandMessage(new ClockSetRequestMessage(macAddress, LocalDateTime.now()));
            }
        }

        @Override
        public boolean shouldBeScheduled() {
            return thing.getStatus() == ONLINE;
        }
    };

    private final List<PlugwiseDeviceTask> recurringTasks = List.of(clockUpdateTask, currentPowerUpdateTask,
            energyUpdateTask, informationUpdateTask, realTimeClockUpdateTask, setClockTask);

    private final Logger logger = LoggerFactory.getLogger(PlugwiseRelayDeviceHandler.class);
    private final DeviceType deviceType;

    private int recentLogAddress = -1;

    private @NonNullByDefault({}) PlugwiseRelayConfig configuration;
    private @NonNullByDefault({}) MACAddress macAddress;

    private @Nullable PowerCalibration calibration;
    private @Nullable Energy energy;
    private @Nullable PendingPowerStateChange pendingPowerStateChange;

    // Flag that keeps track of the pending "measurement interval" device configuration update. When the corresponding
    // Thing configuration parameter changes it is set to true. When the Circle/Stealth goes online a command is sent to
    // update the device configuration. When the Circle/Stealth acknowledges the command the flag is again set to false.
    private boolean updateMeasurementInterval;

    public PlugwiseRelayDeviceHandler(Thing thing) {
        super(thing);
        deviceType = getDeviceType();
    }

    private void calibrate() {
        sendFastUpdateMessage(new PowerCalibrationRequestMessage(macAddress));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        updateTasks(recurringTasks);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        updateTasks(recurringTasks);
    }

    private void correctPowerState(OnOffType powerState) {
        if (configuration.getPowerStateChanging() == PowerStateChanging.ALWAYS_OFF && (powerState != OnOffType.OFF)) {
            logger.debug("Correcting power state of {} ({}) to off", deviceType, macAddress);
            handleOnOffCommand(OnOffType.OFF);
        } else if (configuration.getPowerStateChanging() == PowerStateChanging.ALWAYS_ON
                && (powerState != OnOffType.ON)) {
            logger.debug("Correcting power state of {} ({}) to on", deviceType, macAddress);
            handleOnOffCommand(OnOffType.ON);
        }
    }

    private double correctSign(double value) {
        return configuration.isSuppliesPower() ? -Math.abs(value) : Math.abs(value);
    }

    @Override
    public void dispose() {
        stopTasks(recurringTasks);
        super.dispose();
    }

    @Override
    protected MACAddress getMACAddress() {
        return macAddress;
    }

    private void handleAcknowledgement(AcknowledgementMessage message) {
        boolean oldConfigurationPending = isConfigurationPending();

        ExtensionCode extensionCode = message.getExtensionCode();
        switch (extensionCode) {
            case CLOCK_SET_ACK:
                logger.debug("Received ACK for clock set of {} ({})", deviceType, macAddress);
                sendMessage(new ClockGetRequestMessage(macAddress));
                break;
            case ON_ACK:
                logger.debug("Received ACK for switching on {} ({})", deviceType, macAddress);
                updateState(CHANNEL_STATE, OnOffType.ON);
                break;
            case ON_OFF_NACK:
                logger.debug("Received NACK for switching on/off {} ({})", deviceType, macAddress);
                break;
            case OFF_ACK:
                logger.debug("Received ACK for switching off {} ({})", deviceType, macAddress);
                updateState(CHANNEL_STATE, OnOffType.OFF);
                break;
            case POWER_LOG_INTERVAL_SET_ACK:
                logger.debug("Received ACK for power log interval set of {} ({})", deviceType, macAddress);
                updateMeasurementInterval = false;
                break;
            case REAL_TIME_CLOCK_SET_ACK:
                logger.debug("Received ACK for setting real-time clock of {} ({})", deviceType, macAddress);
                sendMessage(new RealTimeClockGetRequestMessage(macAddress));
                break;
            case REAL_TIME_CLOCK_SET_NACK:
                logger.debug("Received NACK for setting real-time clock of {} ({})", deviceType, macAddress);
                break;
            default:
                logger.debug("{} ({}) {} acknowledgement", deviceType, macAddress, extensionCode);
                break;
        }

        boolean newConfigurationPending = isConfigurationPending();

        if (oldConfigurationPending != newConfigurationPending && !newConfigurationPending) {
            Configuration newConfiguration = editConfiguration();
            newConfiguration.put(CONFIG_PROPERTY_UPDATE_CONFIGURATION, false);
            updateConfiguration(newConfiguration);
        }

        updateStatusOnDetailChange();
    }

    private void handleCalibrationResponse(PowerCalibrationResponseMessage message) {
        boolean wasCalibrated = isCalibrated();
        calibration = message.getCalibration();
        logger.debug("{} ({}) calibrated: {}", deviceType, macAddress, calibration);
        if (!wasCalibrated) {
            if (isRecentLogAddressKnown()) {
                updateEnergy();
            } else {
                updateInformation();
            }
            sendFastUpdateMessage(new PowerInformationRequestMessage(macAddress));
        }
    }

    private void handleClockGetResponse(ClockGetResponseMessage message) {
        updateState(CHANNEL_CLOCK, new StringType(message.getTime()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command '{}' for {} ({}) channel '{}'", command, deviceType, macAddress,
                channelUID.getId());
        if (CHANNEL_STATE.equals(channelUID.getId()) && (command instanceof OnOffType onOffCommand)) {
            if (configuration.getPowerStateChanging() == PowerStateChanging.COMMAND_SWITCHING) {
                pendingPowerStateChange = new PendingPowerStateChange(onOffCommand);
                handleOnOffCommand(onOffCommand);
            } else {
                OnOffType onOff = configuration.getPowerStateChanging() == PowerStateChanging.ALWAYS_ON ? OnOffType.ON
                        : OnOffType.OFF;
                logger.debug("Ignoring {} ({}) power state change (always {})", deviceType, macAddress, onOff);
                updateState(CHANNEL_STATE, onOff);
            }
        }
    }

    private void handleInformationResponse(InformationResponseMessage message) {
        recentLogAddress = message.getLogAddress();
        OnOffType powerState = OnOffType.from(message.getPowerState());

        PendingPowerStateChange change = pendingPowerStateChange;
        if (change != null) {
            if (powerState == change.onOff) {
                pendingPowerStateChange = null;
            } else {
                // Power state change message may be lost or the informationUpdateTask may have queried the power
                // state just before the power state change message arrived
                if (change.retries < POWER_STATE_RETRIES) {
                    change.retries++;
                    logger.warn("Retrying to switch {} ({}) {} (retry #{})", deviceType, macAddress, change.onOff,
                            change.retries);
                    handleOnOffCommand(change.onOff);
                } else {
                    logger.warn("Failed to switch {} ({}) {} after {} retries", deviceType, macAddress, change.onOff,
                            change.retries);
                    pendingPowerStateChange = null;
                }
            }
        }

        if (pendingPowerStateChange == null) {
            updateState(CHANNEL_STATE, powerState);
            correctPowerState(powerState);
        }

        if (energy == null && isCalibrated()) {
            updateEnergy();
        }

        updateProperties(message);
    }

    private void handleOnOffCommand(OnOffType command) {
        sendCommandMessage(new PowerChangeRequestMessage(macAddress, command == OnOffType.ON));
        sendFastUpdateMessage(new InformationRequestMessage(macAddress));

        // Measurements take 2 seconds to become stable
        scheduler.schedule(() -> sendFastUpdateMessage(new PowerInformationRequestMessage(macAddress)), 2,
                TimeUnit.SECONDS);
    }

    private void handlePowerBufferResponse(PowerBufferResponseMessage message) {
        PowerCalibration localCalibration = calibration;
        if (localCalibration == null) {
            calibrate();
            return;
        }

        Energy mostRecentEnergy = message.getMostRecentDatapoint();

        if (mostRecentEnergy != null) {
            // When the current time is '11:44:55.888' and the measurement interval 1 hour, then the end of the most
            // recent energy measurement interval is at '11:00:00.000'
            LocalDateTime oneIntervalAgo = LocalDateTime.now().minus(configuration.getMeasurementInterval());

            boolean isLastInterval = mostRecentEnergy.getEnd().isAfter(oneIntervalAgo);
            if (isLastInterval) {
                mostRecentEnergy.setInterval(configuration.getMeasurementInterval());
                energy = mostRecentEnergy;
                logger.trace("Updating {} ({}) energy with: {}", deviceType, macAddress, mostRecentEnergy);
                updateState(CHANNEL_ENERGY,
                        new QuantityType<>(correctSign(mostRecentEnergy.tokWh(localCalibration)), Units.KILOWATT_HOUR));
                LocalDateTime start = mostRecentEnergy.getStart();
                updateState(CHANNEL_ENERGY_STAMP,
                        start != null ? PlugwiseUtils.newDateTimeType(start) : UnDefType.NULL);
            } else {
                logger.trace("Most recent energy in buffer of {} ({}) is older than one interval ago: {}", deviceType,
                        macAddress, mostRecentEnergy);
            }
        } else {
            logger.trace("Most recent energy in buffer of {} ({}) is null", deviceType, macAddress);
        }
    }

    private void handlePowerInformationResponse(PowerInformationResponseMessage message) {
        PowerCalibration localCalibration = calibration;
        if (localCalibration == null) {
            calibrate();
            return;
        }

        Energy one = message.getOneSecond();
        double watt = one.toWatt(localCalibration);
        if (watt > INVALID_WATT_THRESHOLD) {
            logger.debug("{} ({}) is in a kind of error state, skipping power information response", deviceType,
                    macAddress);
            return;
        }

        updateState(CHANNEL_POWER, new QuantityType<>(correctSign(watt), Units.WATT));
    }

    private void handleRealTimeClockGetResponse(RealTimeClockGetResponseMessage message) {
        updateState(CHANNEL_REAL_TIME_CLOCK, PlugwiseUtils.newDateTimeType(message.getDateTime()));
    }

    @Override
    public void handleResponseMessage(Message message) {
        updateLastSeen();

        switch (message.getType()) {
            case ACKNOWLEDGEMENT_V1, ACKNOWLEDGEMENT_V2:
                handleAcknowledgement((AcknowledgementMessage) message);
                break;
            case CLOCK_GET_RESPONSE:
                handleClockGetResponse(((ClockGetResponseMessage) message));
                break;
            case DEVICE_INFORMATION_RESPONSE:
                handleInformationResponse((InformationResponseMessage) message);
                break;
            case POWER_BUFFER_RESPONSE:
                handlePowerBufferResponse((PowerBufferResponseMessage) message);
                break;
            case POWER_CALIBRATION_RESPONSE:
                handleCalibrationResponse(((PowerCalibrationResponseMessage) message));
                break;
            case POWER_INFORMATION_RESPONSE:
                handlePowerInformationResponse((PowerInformationResponseMessage) message);
                break;
            case REAL_TIME_CLOCK_GET_RESPONSE:
                handleRealTimeClockGetResponse((RealTimeClockGetResponseMessage) message);
                break;
            default:
                logger.trace("Received unhandled {} message from {} ({})", message.getType(), deviceType, macAddress);
                break;
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(PlugwiseRelayConfig.class);
        macAddress = configuration.getMACAddress();
        if (!isInitialized()) {
            setUpdateCommandFlags(null, configuration);
        }
        if (configuration.isTemporarilyNotInNetwork()) {
            updateStatus(OFFLINE);
        }
        updateTasks(recurringTasks);
        super.initialize();
    }

    private boolean isCalibrated() {
        return calibration != null;
    }

    @Override
    protected boolean isConfigurationPending() {
        return updateMeasurementInterval;
    }

    private boolean isRecentLogAddressKnown() {
        return recentLogAddress >= 0;
    }

    @Override
    protected void sendConfigurationUpdateCommands() {
        logger.debug("Sending {} ({}) configuration update commands", deviceType, macAddress);

        if (updateMeasurementInterval) {
            logger.debug("Sending command to update {} ({}) power log measurement interval", deviceType, macAddress);
            Duration consumptionInterval = configuration.isSuppliesPower() ? Duration.ZERO
                    : configuration.getMeasurementInterval();
            Duration productionInterval = configuration.isSuppliesPower() ? configuration.getMeasurementInterval()
                    : Duration.ZERO;
            sendCommandMessage(
                    new PowerLogIntervalSetRequestMessage(macAddress, consumptionInterval, productionInterval));
        }

        super.sendConfigurationUpdateCommands();
    }

    private void setUpdateCommandFlags(@Nullable PlugwiseRelayConfig oldConfiguration,
            PlugwiseRelayConfig newConfiguration) {
        boolean fullUpdate = newConfiguration.isUpdateConfiguration() && !isConfigurationPending();
        if (fullUpdate) {
            logger.debug("Updating all configuration properties of {} ({})", deviceType, macAddress);
        }

        updateMeasurementInterval = fullUpdate || (oldConfiguration != null
                && (!oldConfiguration.getMeasurementInterval().equals(newConfiguration.getMeasurementInterval())));
        if (updateMeasurementInterval) {
            logger.debug("Updating {} ({}) power log interval when online", deviceType, macAddress);
        }
    }

    @Override
    protected boolean shouldOnlineTaskBeScheduled() {
        Bridge bridge = getBridge();
        return !configuration.isTemporarilyNotInNetwork() && (bridge != null && bridge.getStatus() == ONLINE);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        PlugwiseRelayConfig oldConfiguration = this.configuration;
        PlugwiseRelayConfig newConfiguration = configuration.as(PlugwiseRelayConfig.class);

        setUpdateCommandFlags(oldConfiguration, newConfiguration);

        configuration.put(CONFIG_PROPERTY_UPDATE_CONFIGURATION, isConfigurationPending());

        super.updateConfiguration(configuration);
    }

    private void updateEnergy() {
        int previousLogAddress = recentLogAddress - 1;
        while (previousLogAddress <= recentLogAddress) {
            PowerBufferRequestMessage message = new PowerBufferRequestMessage(macAddress, previousLogAddress);
            previousLogAddress = previousLogAddress + 1;
            sendMessage(message);
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);

        if (status == ONLINE) {
            if (!isCalibrated()) {
                calibrate();
            }
            if (editProperties().isEmpty()) {
                updateInformation();
            }
        }

        updateTasks(recurringTasks);
    }
}
