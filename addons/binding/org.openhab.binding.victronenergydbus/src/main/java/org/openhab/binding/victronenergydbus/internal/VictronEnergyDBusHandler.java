/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergydbus.internal;

import static org.openhab.binding.victronenergydbus.internal.VictronEnergyDBusBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VictronEnergyDBusHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Samuel Lueckoff - Initial contribution
 */
@NonNullByDefault
public class VictronEnergyDBusHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VictronEnergyDBusHandler.class);

    private static final int DEFAULT_REFRESH_RATE = 30;

    @SuppressWarnings("unused")
    private @Nullable VictronEnergyDBusConfiguration config;
    @SuppressWarnings("unused")
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable VictronEnergyDBusSolarCharger sc;

    public VictronEnergyDBusHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no command-handling needed. Readonly at the moment.
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing Victron Energy DBus handler.");
        config = getConfigAs(VictronEnergyDBusConfiguration.class);

        final Object port = getConfig().get(PORT);

        this.sc = new VictronEnergyDBusSolarCharger(port.toString());

        if (sc != null) {
            updateStatus(ThingStatus.ONLINE);
            pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, DEFAULT_REFRESH_RATE, TimeUnit.SECONDS);
        }

    }

    @SuppressWarnings("null")
    private void updateData() {
        sc.connect();
        updateState(CHANNEL_STATE, new DecimalType(this.sc.getState()));
        updateState(CHANNEL_STATE_STRING, new StringType(this.sc.getStateStr()));
        updateState(CHANNEL_DC_VOLTAGE, new DecimalType(this.sc.getDcV()));
        updateState(CHANNEL_DC_CURRENT, new DecimalType(this.sc.getDcI()));
        updateState(CHANNEL_PV_VOLTAGE, new DecimalType(this.sc.getPvV()));
        updateState(CHANNEL_PV_CURRENT, new DecimalType(this.sc.getPvI()));
        updateState(CHANNEL_YIELD_POWER, new DecimalType(this.sc.getYP()));
        updateState(CHANNEL_YIELD_USER, new DecimalType(this.sc.getYU()));
        updateState(CHANNEL_YIELD_SYSTEM, new DecimalType(this.sc.getYS()));
        updateState(CHANNEL_YIELD_TODAY, new DecimalType(this.sc.getYT()));
        updateState(CHANNEL_MAXIMUM_POWER_TODAY, new DecimalType(this.sc.getMPT()));
        updateState(CHANNEL_TIME_IN_FLOAT_TODAY, new DecimalType(this.sc.getTIFT()));
        updateState(CHANNEL_TIME_IN_ABSORPTION_TODAY, new DecimalType(this.sc.getTIAT()));
        updateState(CHANNEL_TIME_IN_BULK_TODAY, new DecimalType(this.sc.getTIBT()));
        updateState(CHANNEL_MAXIMUM_PV_VOLTAGE_TODAY, new DecimalType(this.sc.getMPVT()));
        updateState(CHANNEL_MAXIMUM_BATTERY_CURRENT_TODAY, new DecimalType(this.sc.getMBCT()));
        updateState(CHANNEL_MINIMUM_BATTERY_VOLTAGE_TODAY, new DecimalType(this.sc.getMinBVT()));
        updateState(CHANNEL_MAXIMUM_BATTERY_VOLTAGE_TODAY, new DecimalType(this.sc.getMaxBVT()));
        updateState(CHANNEL_YIELD_YESTERDAY, new DecimalType(this.sc.getYY()));
        updateState(CHANNEL_MAXIMUM_POWER_YESTERDAY, new DecimalType(this.sc.getMPT()));
        updateState(CHANNEL_TIME_IN_FLOAT_YESTERDAY, new DecimalType(this.sc.getTIFY()));
        updateState(CHANNEL_TIME_IN_ABSORPTION_YESTERDAY, new DecimalType(this.sc.getTIAY()));
        updateState(CHANNEL_TIME_IN_BULK_YESTERDAY, new DecimalType(this.sc.getTIBY()));
        updateState(CHANNEL_MAXIMUM_PV_VOLTAGE_YESTERDAY, new DecimalType(this.sc.getMPVY()));
        updateState(CHANNEL_MAXIMUM_BATTERY_CURRENT_YESTERDAY, new DecimalType(this.sc.getMBCY()));
        updateState(CHANNEL_MINIMUM_BATTERY_VOLTAGE_YESTERDAY, new DecimalType(this.sc.getMinBVY()));
        updateState(CHANNEL_MAXIMUM_BATTERY_VOLTAGE_YESTERDAY, new DecimalType(this.sc.getMaxBVY()));
        updateState(CHANNEL_SERIAL, new StringType(this.sc.getSerial()));
        updateState(CHANNEL_FIRMWARE_VERSION, new DecimalType(this.sc.getFwV()));
        updateState(CHANNEL_PRODUCT_ID, new DecimalType(this.sc.getPId()));
        updateState(CHANNEL_DEVICE_INSTANCE, new DecimalType(this.sc.getDI()));
        updateState(CHANNEL_PRODUCTNAME, new StringType(this.sc.getPn()));
        updateState(CHANNEL_ERROR, new DecimalType(this.sc.getErr()));
        updateState(CHANNEL_ERROR_STRING, new StringType(this.sc.getErrString()));
        sc.disconnect();
    }

}
