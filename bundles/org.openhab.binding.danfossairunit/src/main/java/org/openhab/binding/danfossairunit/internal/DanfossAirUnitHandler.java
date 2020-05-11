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
package org.openhab.binding.danfossairunit.internal;

import static org.openhab.binding.danfossairunit.internal.DanfossAirUnitBindingConstants.PROPERTY_SERIAL;
import static org.openhab.binding.danfossairunit.internal.DanfossAirUnitBindingConstants.PROPERTY_UNIT_NAME;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanfossAirUnitHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 */
@NonNullByDefault
public class DanfossAirUnitHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitHandler.class);
    private @Nullable DanfossAirUnitConfiguration config;
    private @Nullable ValueCache valueCache;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable DanfossAirUnit hrv;

    public DanfossAirUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAllChannels();
        } else {
            try {
                if(hrv == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, "Air unit connection not initialized.");
                    return;
                }
                Channel channel = Channel.getByName(channelUID.getIdWithoutGroup());
                if (channel.getWriteAccessor() != null) {
                    updateState(channelUID, channel.getWriteAccessor().access(Objects.requireNonNull(hrv), command));
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Ignoring unknown channel id: {}", channelUID.getIdWithoutGroup(), e);
            } catch (IOException ioe) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(DanfossAirUnitConfiguration.class);
        valueCache = new ValueCache(config.updateUnchangedValuesEveryMillis);
        try {
            hrv = new DanfossAirUnit(InetAddress.getByName(config.host), 30046);
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Unknown host: " + config.host);
            return;
        }

        scheduler.execute(() -> {
            try {
                thing.setProperty(PROPERTY_UNIT_NAME, hrv.getUnitName());
                thing.setProperty(PROPERTY_SERIAL, hrv.getUnitSerialNumber());
                pollingJob = scheduler.scheduleWithFixedDelay(this::updateAllChannels, 5, config.refreshInterval,
                        TimeUnit.SECONDS);
                updateStatus(ThingStatus.ONLINE);

            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    private void updateAllChannels() {
        if (hrv == null) {
            return;
        }
        DanfossAirUnit danfossAirUnit = hrv;
        logger.debug("Updating DanfossHRV data '{}'", getThing().getUID());

        for (Channel channel : Channel.values()) {
            if (Thread.interrupted()) {
                logger.debug("Polling thread interrupted...");
                return;
            }
            try {
                updateState(channel.getGroup().getGroupName(), channel.getChannelName(),
                        channel.getReadAccessor().access(danfossAirUnit));
            } catch(UnexpectedResponseValueException e) {
                updateState(channel.getGroup().getGroupName(), channel.getChannelName(), UnDefType.UNDEF);
                logger.debug("Cannot update channel {}: an unexpected or invalid response has been received from the air unit: {}",
                        channel.getChannelName(), e.getMessage());
            } catch (IOException e) {
                updateState(channel.getGroup().getGroupName(), channel.getChannelName(), UnDefType.UNDEF);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                logger.debug("Cannot update channel {}: an error occurred retrieving the value: {}",
                        channel.getChannelName(), e.getMessage());
            }
        }

        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Danfoss HRV handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (hrv != null) {
            hrv.cleanUp();
            hrv = null;
        }
    }

    private void updateState(String groupId, String channelId, State state) {
        if (valueCache.updateValue(channelId, state)) {
            updateState(new ChannelUID(thing.getUID(), groupId, channelId), state);
        }
    }
}
