/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.handler;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.channel.SolarwattChannelTypeProvider;
import org.openhab.binding.solarwatt.internal.domain.model.Device;
import org.openhab.binding.solarwatt.internal.domain.model.EVStation;
import org.openhab.binding.solarwatt.internal.domain.model.Location;
import org.openhab.binding.solarwatt.internal.domain.model.PowerMeter;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The concrete device handlers process the location specific commands.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class LocationHandler extends SimpleDeviceHandler {

    public LocationHandler(Thing thing, SolarwattChannelTypeProvider channelTypeProvider) {
        super(thing, channelTypeProvider);
    }

    /**
     * Add calculated states for unmetered consum.
     *
     * First calculate and then call super to submit the state
     */
    @Override
    protected void updateDeviceChannels() {
        // add calculated states
        this.updateCalculated();

        // submits all states
        super.updateDeviceChannels();
    }

    /**
     * Add calculated channels for unmetered consum.
     *
     * First calculate and then call super to submit the channels
     */
    @Override
    protected void initDeviceChannels() {
        // add calculated channels
        final EnergyManagerHandler bridgeHandler = this.getEnergyManagerHandler();
        if (bridgeHandler != null) {
            // update the unmetered channel via subtracting the outerconsumers
            // from the powerConsumed
            Location locationDevice = (Location) this.getDevice();
            if (locationDevice != null) {
                locationDevice.addChannel(CHANNEL_POWER_DIRECT_CONSUMED.getChannelName(), Units.WATT,
                        Device.WATT_CATEGORY, false);
                locationDevice.addChannel(CHANNEL_WORK_DIRECT_CONSUMED.getChannelName(), Units.WATT_HOUR,
                        Device.WATT_HOUR_CATEGORY, false);
                locationDevice.addChannel(CHANNEL_POWER_CONSUMED_UNMETERED.getChannelName(), Units.WATT,
                        Device.WATT_CATEGORY, false);
                locationDevice.addChannel(CHANNEL_WORK_CONSUMED_UNMETERED.getChannelName(), Units.WATT_HOUR,
                        Device.WATT_HOUR_CATEGORY, false);
            }
        }

        // submit all channels
        super.initDeviceChannels();
    }

    private void updateCalculated() {
        final EnergyManagerHandler bridgeHandler = this.getEnergyManagerHandler();
        final Location locationDevice = (Location) this.getDevice();
        if (bridgeHandler != null && locationDevice != null) {
            this.calculateDirectConsumption(locationDevice);
            this.calculateUnmeteredConsumption(bridgeHandler, locationDevice);
        }
    }

    private void calculateUnmeteredConsumption(EnergyManagerHandler bridgeHandler, Location locationDevice) {
        // update the unmetered channels via subtracting
        // the outerconsumers from the powerConsumed
        BigDecimal powerConsumed = locationDevice.getBigDecimalFromChannel(CHANNEL_POWER_CONSUMED.getChannelName());
        BigDecimal workConsumed = locationDevice.getBigDecimalFromChannel(CHANNEL_WORK_CONSUMED.getChannelName());

        final List<BigDecimal> powerOuter = new ArrayList<>();
        final List<BigDecimal> workOuter = new ArrayList<>();

        Set<String> outerConsumers = locationDevice.getDevicesMap().getOuterConsumer();
        if (outerConsumers != null) {
            outerConsumers.stream().map(bridgeHandler::getDeviceFromGuid).forEach(outerDevice -> {
                if (outerDevice instanceof PowerMeter) {
                    powerOuter.add(outerDevice.getBigDecimalFromChannel(CHANNEL_POWER_IN.getChannelName()));
                    workOuter.add(outerDevice.getBigDecimalFromChannel(CHANNEL_WORK_IN.getChannelName()));
                } else if (outerDevice instanceof EVStation) {
                    powerOuter.add(outerDevice.getBigDecimalFromChannel(CHANNEL_POWER_AC_IN.getChannelName()));
                    workOuter.add(outerDevice.getBigDecimalFromChannel(CHANNEL_WORK_AC_IN.getChannelName()));
                }
            });

            BigDecimal powerConsumedUnmetered = powerOuter.stream().reduce(powerConsumed, BigDecimal::subtract);
            if (powerConsumedUnmetered.compareTo(BigDecimal.ONE) > 0) {
                // sometimes the powerConsumed is exactly the power of the unmetered devices
                // the resulting zero for unmetered consumption is not correct
                locationDevice.addStateBigDecimal(CHANNEL_POWER_CONSUMED_UNMETERED, powerConsumedUnmetered, Units.WATT);
            }
            locationDevice.addStateBigDecimal(CHANNEL_WORK_CONSUMED_UNMETERED,
                    workOuter.stream().reduce(workConsumed, BigDecimal::subtract), Units.WATT_HOUR);
        }
    }

    private void calculateDirectConsumption(Location locationDevice) {
        // calculate direct consumption for display purposes
        locationDevice.addState(CHANNEL_POWER_DIRECT_CONSUMED.getChannelName(),
                this.calculateQuantityDifference(locationDevice.getState(CHANNEL_POWER_SELF_CONSUMED.getChannelName()),
                        locationDevice.getState(CHANNEL_POWER_BUFFERED_FROM_PRODUCERS.getChannelName())));

        locationDevice.addState(CHANNEL_WORK_DIRECT_CONSUMED.getChannelName(),
                this.calculateQuantityDifference(locationDevice.getState(CHANNEL_WORK_SELF_CONSUMED.getChannelName()),
                        locationDevice.getState(CHANNEL_WORK_BUFFERED_FROM_PRODUCERS.getChannelName())));
    }

    /**
     * Helper to generate a new state calculated from the difference between two states.
     *
     * channelTarget = channelValue - channelSubtract
     *
     * @param stateValue value from which we subtract
     * @param stateSubtract value to substrct
     * @return {@link State} of calculated value
     */
    private @Nullable State calculateQuantityDifference(@Nullable State stateValue, @Nullable State stateSubtract) {
        if (stateValue != null && stateSubtract != null) {
            @SuppressWarnings("rawtypes")
            QuantityType quantityValue = stateValue.as(QuantityType.class);
            @SuppressWarnings("rawtypes")
            QuantityType quantitySubtract = stateSubtract.as(QuantityType.class);

            if (quantityValue != null && quantitySubtract != null) {
                return quantityValue.subtract(quantitySubtract);
            }
        }

        return null;
    }
}
