/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal.handler;

import static org.openhab.binding.danfossairunit.internal.DanfossAirUnitBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.danfossairunit.internal.Channel;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnit;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitCommunicationController;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitConfiguration;
import org.openhab.binding.danfossairunit.internal.DanfossAirUnitWriteAccessor;
import org.openhab.binding.danfossairunit.internal.FixedTimeZoneProvider;
import org.openhab.binding.danfossairunit.internal.UnexpectedResponseValueException;
import org.openhab.binding.danfossairunit.internal.ValueCache;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanfossAirUnitHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 * @author Jacob Laursen - Refactoring, bugfixes and enhancements
 */
@NonNullByDefault
public class DanfossAirUnitHandler extends BaseThingHandler {

    private static final int POLLING_INTERVAL_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitHandler.class);
    private final TimeZoneProvider timeZoneProvider;

    private @NonNullByDefault({}) DanfossAirUnitConfiguration config;
    private @Nullable ValueCache valueCache;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable DanfossAirUnitCommunicationController communicationController;
    private @Nullable DanfossAirUnit airUnit;
    private boolean propertiesInitializedSuccessfully = false;

    public DanfossAirUnitHandler(final Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = Channel.getByName(channelUID.getIdWithoutGroup());

        if (command instanceof RefreshType) {
            updateChannel(channel, true);
            return;
        }

        try {
            DanfossAirUnit airUnit = this.airUnit;
            if (airUnit != null) {
                DanfossAirUnitWriteAccessor writeAccessor = channel.getWriteAccessor();
                if (writeAccessor != null) {
                    updateState(channelUID, writeAccessor.access(airUnit, command));
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE,
                        "@text/offline.connection-not-initialized");
            }
        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(DanfossAirUnitConfiguration.class);
        valueCache = new ValueCache(config.updateUnchangedValuesEveryMillis);
        try {
            var localCommunicationController = new DanfossAirUnitCommunicationController(
                    InetAddress.getByName(config.host));
            this.communicationController = localCommunicationController;
            TimeZoneProvider timeZoneProvider = config.timeZone.isBlank() ? this.timeZoneProvider
                    : FixedTimeZoneProvider.of(ZoneId.of(config.timeZone));

            airUnit = new DanfossAirUnit(localCommunicationController, timeZoneProvider);
            startPolling();
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.unknown-host [\"" + config.host + "\"]");
        } catch (DateTimeException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    private void updateAllChannels() {
        if (!initializeProperties()) {
            return;
        }

        logger.debug("Updating DanfossHRV data '{}'", getThing().getUID());

        for (Channel channel : Channel.values()) {
            if (Thread.interrupted()) {
                logger.debug("Polling thread interrupted...");
                return;
            }
            updateChannel(channel);
        }
    }

    private void updateChannel(Channel channel) {
        updateChannel(channel, false);
    }

    private void updateChannel(Channel channel, boolean forceUpdate) {
        DanfossAirUnit airUnit = this.airUnit;
        if (airUnit == null) {
            return;
        }

        ChannelUID channelUID = new ChannelUID(thing.getUID(), channel.getGroup().getGroupName(),
                channel.getChannelName());
        if (!isLinked(channelUID)) {
            return;
        }

        try {
            State state = channel.getReadAccessor().access(airUnit);
            if (forceUpdate) {
                forceUpdateState(channelUID, state);
            } else {
                updateState(channelUID, state);
            }
        } catch (UnexpectedResponseValueException e) {
            updateState(channelUID, UnDefType.UNDEF);
            logger.debug(
                    "Cannot update channel {}: an unexpected or invalid response has been received from the air unit: {}",
                    channel.getChannelName(), e.getMessage());
        } catch (IOException e) {
            updateState(channelUID, UnDefType.UNDEF);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Cannot update channel {}: an error occurred retrieving the value: {}",
                    channel.getChannelName(), e.getMessage());
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private synchronized boolean initializeProperties() {
        if (propertiesInitializedSuccessfully) {
            return true;
        }

        DanfossAirUnit localAirUnit = this.airUnit;
        if (localAirUnit == null) {
            return false;
        }

        logger.debug("Initializing DanfossHRV properties '{}'", getThing().getUID());

        try {
            Map<String, String> properties = new HashMap<>(5);
            properties.put(Thing.PROPERTY_MODEL_ID, localAirUnit.getUnitName());
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, localAirUnit.getHardwareRevision());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, localAirUnit.getSoftwareRevision());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, localAirUnit.getUnitSerialNumber());
            properties.put(PROPERTY_CCM_SERIAL_NUMBER, localAirUnit.getCCMSerialNumber());
            updateProperties(properties);
            propertiesInitializedSuccessfully = true;
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException | UnexpectedResponseValueException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Cannot initialize properties: an error occurred: {}", e.getMessage());
        }

        return propertiesInitializedSuccessfully;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Danfoss HRV handler '{}'", getThing().getUID());

        stopPolling();

        airUnit = null;

        DanfossAirUnitCommunicationController communicationController = this.communicationController;
        if (communicationController != null) {
            communicationController.disconnect();
        }
        this.communicationController = null;
    }

    private synchronized void startPolling() {
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::updateAllChannels, POLLING_INTERVAL_SECONDS,
                config.refreshInterval, TimeUnit.SECONDS);
    }

    private synchronized void stopPolling() {
        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }
        this.pollingJob = null;
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        if (updateCache(channelUID, state)) {
            super.updateState(channelUID, state);
        }
    }

    private void forceUpdateState(ChannelUID channelUID, State state) {
        updateCache(channelUID, state);
        super.updateState(channelUID, state);
    }

    private boolean updateCache(ChannelUID channelUID, State state) {
        ValueCache valueCache = this.valueCache;
        return valueCache != null && valueCache.updateValue(channelUID.getId(), state);
    }
}
