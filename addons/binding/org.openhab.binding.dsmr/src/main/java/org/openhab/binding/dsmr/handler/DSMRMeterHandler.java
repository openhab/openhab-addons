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
 */
public class DSMRMeterHandler extends BaseThingHandler implements DSMRMeterListener {
    // logger
    private final Logger logger = LoggerFactory.getLogger(DSMRMeterHandler.class);

    // The DSMRMeter instance
    private DSMRMeter meter = null;

    // Timestamp when last values were received
    private long lastValuesReceivedTs = 0;

    // Reference to the meter watchdog
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
        DSMRMeterConfiguration meterConfig = null;
        DSMRMeterDescriptor meterDescriptor = null;

        if (config != null) {
            meterConfig = config.as(DSMRMeterConfiguration.class);
        } else {
            logger.warn("{} does not have a configuration", getThing());
        }

        if (meterConfig != null) {
            DSMRMeterType meterType = null;

            try {
                meterType = DSMRMeterType.valueOf(getThing().getThingTypeUID().getId().toUpperCase());
            } catch (Exception exception) {
                logger.error("Invalid meterType", exception);
            }

            if (meterType != null) {
                meterDescriptor = new DSMRMeterDescriptor(meterType, meterConfig.channel);
            }
        } else {
            logger.warn("Invalid meter configuration for {}", getThing());
        }
        if (meterDescriptor != null) {
            meter = new DSMRMeter(meterDescriptor, this);

            // Initialize meter watchdog
            meterWatchdog = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis()
                            - lastValuesReceivedTs > DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT) {
                        if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    }
                }
            }, DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT, DSMRMeterConstants.METER_VALUES_RECEIVED_TIMEOUT,
                    TimeUnit.MILLISECONDS);

            updateStatus(ThingStatus.OFFLINE);
        } else {
            logger.warn("{} could not be initialized. Delete this Thing if the problem persists.", getThing());
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This could not be initialized. Delete Thing if the problem persists.");
        }
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
     * Remove the Meter Thing
     */
    @Override
    public void handleRemoval() {
        // Stop the timeout timer
        if (meterWatchdog != null) {
            meterWatchdog.cancel(false);
            meterWatchdog = null;
        }
        updateStatus(ThingStatus.REMOVED);
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
        if (cosemValues.size() > 0) {
            lastValuesReceivedTs = System.currentTimeMillis();

            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
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
