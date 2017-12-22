/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemValue;
import org.openhab.binding.dsmr.internal.device.cosem.CosemValueDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeter;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterConfiguration;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterConstants;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterListener;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MeterHandler will create logic DSMR meter ThingTypes
 *
 * @author M. Volaart - Initial contribution
 */
public class DSMRMeterHandler extends BaseThingHandler implements DSMRMeterListener {
    private final Logger logger = LoggerFactory.getLogger(DSMRMeterHandler.class);

    /**
     * The DSMRMeter instance
     */
    private DSMRMeter meter;

    /**
     * Timestamp when last values were received
     */
    private long lastValuesReceivedTs;

    /**
     * Reference to the meter watchdog
     */
    private ScheduledFuture<?> meterWatchdog;

    /**
     * Creates a new MeterHandler for the given Thing
     *
     * @param thing {@link Thing} to create the MeterHandler for
     */
    public DSMRMeterHandler(Thing thing) {
        super(thing);
    }

    /**
     * DSMR Meter don't support handling commands
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No comments can be handled
    }

    /**
     * Initializes a DSMR Meter
     *
     * This method will load the corresponding configuration
     */
    @Override
    public void initialize() {
        logger.debug("Initialize MeterHandler for Thing {}", getThing().getUID());

        Configuration config = getThing().getConfiguration();
        DSMRMeterConfiguration meterConfig = config.as(DSMRMeterConfiguration.class);
        DSMRMeterDescriptor meterDescriptor;
        DSMRMeterType meterType;

        try {
            meterType = DSMRMeterType.valueOf(getThing().getThingTypeUID().getId().toUpperCase());
        } catch (IllegalArgumentException iae) {
            logger.warn(
                    "{} could not be initialized due to an invalid meterType {}. Delete this Thing if the problem persists.",
                    getThing(), getThing().getThingTypeUID().getId().toUpperCase());
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This could not be initialized. Delete Thing if the problem persists.");
            return;
        }

        meterDescriptor = new DSMRMeterDescriptor(meterType, meterConfig.channel);
        meter = new DSMRMeter(meterDescriptor, this);

        // Initialize meter watchdog
        meterWatchdog = scheduler.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastValuesReceivedTs > DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT) {
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }, DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT, DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT,
                TimeUnit.MILLISECONDS);

        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Disposes this Meter Handler
     */
    @Override
    public void dispose() {
        if (meterWatchdog != null) {
            meterWatchdog.cancel(false);
            meterWatchdog = null;
        }
    }

    /**
     * Callback for received meter values
     *
     * In this method the conversion is done from the {@link CosemObjct} to the OpenHAB value.
     * For CosemObjects containing more then one value post processing is needed
     *
     */
    @Override
    public void meterValueReceived(CosemObject obj) {
        Map<String, ? extends CosemValue<? extends Object>> cosemValues = obj.getCosemValues();

        // Update the internal states
        if (!cosemValues.isEmpty()) {
            lastValuesReceivedTs = System.currentTimeMillis();

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            for (String key : cosemValues.keySet()) {
                String channel = obj.getType().name().toLowerCase();
                if (!key.equals(CosemValueDescriptor.DEFAULT_CHANNEL)) {
                    /* CosemObject has a specific sub channel */
                    channel += "_" + key;
                }
                State newState = cosemValues.get(key).getOpenHABValue();
                logger.debug("Updating state for channel {} to value {}", channel, newState);
                updateState(channel, newState);
            }
        } else {
            logger.warn("Invalid CosemObject size ({}) for CosemObject {}", cosemValues.size(), obj);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            // Set status to offline --> Thing will become online after receiving meter values
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Returns the DSMR meter
     *
     * @return {@link DSMRMeter}
     */
    public DSMRMeter getDSMRMeter() {
        return meter;
    }
}
