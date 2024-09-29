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
package org.openhab.binding.dsmr.internal.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dsmr.internal.device.connector.DSMRErrorStatus;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.meter.DSMRMeter;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterConfiguration;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterConstants;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MeterHandler will create logic DSMR meter ThingTypes
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Separated thing state update cycle from meter values received cycle
 */
@NonNullByDefault
public class DSMRMeterHandler extends BaseThingHandler implements P1TelegramListener {

    private final Logger logger = LoggerFactory.getLogger(DSMRMeterHandler.class);

    /**
     * The DSMRMeter instance.
     */
    private @NonNullByDefault({}) DSMRMeter meter;

    /**
     * Last received cosem objects.
     */
    private List<CosemObject> lastReceivedValues = Collections.emptyList();

    /**
     * Reference to the meter watchdog.
     */
    private @NonNullByDefault({}) ScheduledFuture<?> meterWatchdog;

    /**
     * The M-bus channel this meter is on, or if the channel is irrelevant is set to unknown channel
     */
    private int channel = DSMRMeterConstants.UNKNOWN_CHANNEL;

    /**
     * Creates a new MeterHandler for the given Thing.
     *
     * @param thing {@link Thing} to create the MeterHandler for
     */
    public DSMRMeterHandler(final Thing thing) {
        super(thing);
    }

    /**
     * DSMR Meter don't support handling commands
     */
    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command == RefreshType.REFRESH) {
            updateState();
        }
    }

    /**
     * Initializes a DSMR Meter
     *
     * This method will load the corresponding configuration
     */
    @Override
    public void initialize() {
        logger.debug("Initialize MeterHandler for Thing {}", getThing().getUID());
        DSMRMeterType meterType;

        try {
            meterType = DSMRMeterType.valueOf(getThing().getThingTypeUID().getId().toUpperCase());
        } catch (final IllegalArgumentException iae) {
            logger.warn(
                    "{} could not be initialized due to an invalid meterType {}. Delete this Thing if the problem persists.",
                    getThing(), getThing().getThingTypeUID().getId().toUpperCase());
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/addon.dsmr.error.configuration.invalidmetertype");
            return;
        }
        final DSMRMeterConfiguration meterConfig = getConfigAs(DSMRMeterConfiguration.class);
        channel = meterType.meterKind.isChannelRelevant() ? meterConfig.channel : DSMRMeterConstants.UNKNOWN_CHANNEL;
        final DSMRMeterDescriptor meterDescriptor = new DSMRMeterDescriptor(meterType, channel);
        meter = new DSMRMeter(meterDescriptor);
        meterWatchdog = scheduler.scheduleWithFixedDelay(this::updateState, meterConfig.refresh, meterConfig.refresh,
                TimeUnit.SECONDS);
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        if (meterWatchdog != null) {
            meterWatchdog.cancel(false);
            meterWatchdog = null;
        }
    }

    /**
     * Updates the state of all channels from the last received Cosem values from the meter. The lastReceivedValues are
     * cleared after processing here so when it does contain values the next time this method is called and it contains
     * values those are new values.
     */
    private synchronized void updateState() {
        logger.trace("Update state for device: {}", getThing().getThingTypeUID().getId());
        if (!lastReceivedValues.isEmpty()) {
            for (final CosemObject cosemObject : lastReceivedValues) {
                for (final Entry<String, ? extends State> entry : cosemObject.getCosemValues().entrySet()) {
                    final String channel = cosemObject.getType().name().toLowerCase()
                            /* CosemObject has a specific sub channel if key not empty */
                            + (entry.getKey().isEmpty() ? "" : "_" + entry.getKey());

                    final State newState = entry.getValue();
                    logger.debug("Updating state for channel {} to value {}", channel, newState);
                    updateState(channel, newState);
                }
            }
            if (ThingHandlerHelper.isHandlerInitialized(getThing()) && getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            lastReceivedValues = Collections.emptyList();
        }
    }

    /**
     * Callback for received meter values. When this method is called but the telegram has no values for this meter this
     * meter is set to offline because something is wrong, possible the meter has been removed.
     *
     * @param telegram The received telegram
     */
    @Override
    public void telegramReceived(final P1Telegram telegram) {
        lastReceivedValues = Collections.emptyList();
        final DSMRMeter localMeter = meter;

        if (localMeter == null) {
            return;
        }
        final List<CosemObject> filteredValues = localMeter.filterMeterValues(telegram.getCosemObjects(), channel);

        if (filteredValues.isEmpty()) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                setDeviceOffline(ThingStatusDetail.COMMUNICATION_ERROR, "@text/addon.dsmr.error.thing.nodata");
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Received {} objects for {}", filteredValues.size(), getThing().getThingTypeUID().getId());
            }
            lastReceivedValues = filteredValues;
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateState();
            }
        }
    }

    @Override
    public void onError(final DSMRErrorStatus state, final String message) {
        // Error is handled in other places.
    }

    @Override
    public synchronized void bridgeStatusChanged(final ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            // Set status to offline --> Thing will become online after receiving meter values
            setDeviceOffline(ThingStatusDetail.NONE, null);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            setDeviceOffline(ThingStatusDetail.BRIDGE_OFFLINE, null);
        }
    }

    /**
     * @return Returns the {@link DSMRMeterDescriptor} this object is configured with
     */
    public @Nullable DSMRMeterDescriptor getMeterDescriptor() {
        return meter == null ? null : meter.getMeterDescriptor();
    }

    /**
     * Convenience method to set the meter off line.
     *
     * @param status off line status
     * @param details off line detailed message
     */
    private void setDeviceOffline(final ThingStatusDetail status, @Nullable final String details) {
        updateStatus(ThingStatus.OFFLINE, status, details);
        getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.NULL));
    }
}
