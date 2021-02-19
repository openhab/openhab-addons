/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain.model;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.SolarwattChannel;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Class that aggregates all devices which are found in one location
 * and are working together to produce power.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.devices.location.Location=[
 * WorkBufferedFromProducers,
 * WorkOutFromProducers,
 * PowerBufferedFromGrid,
 * WorkProduced,
 * WorkConsumedFromStorage,
 * PowerSelfConsumed,
 * PowerConsumedFromProducers,
 * PowerConsumedFromStorage,
 * PowerOutFromProducers,
 * DatePowerProductionForecastStart,
 * PowerOut,
 * PowerProductionForecastNow,
 * PowerOutFromStorage,
 * IdDevicesMap,
 * PowerBuffered,
 * TimePowerProductionForecastGranularity,
 * WorkOutFromStorage,
 * CountPersons,
 * DatePowerConsumptionForecastStart,
 * PowerConsumptionForecastNow,
 * PowerProduced,
 * WorkBuffered,
 * WorkConsumedFromProducers,
 * PowerConsumed,
 * WorkConsumedFromGrid,
 * PowerConsumptionForecastValues,
 * PowerSelfSupplied,
 * WorkReleased,
 * PowerConsumedFromGrid,
 * TimePowerConsumptionForecastGranularity,
 * WorkConsumed,
 * AddressLocation,
 * WorkIn,
 * PriceWorkIn,
 * PowerIn,
 * WorkBufferedFromGrid,
 * WorkSelfConsumed,
 * PowerProductionForecastValues,
 * LocationGeographical,
 * PowerBufferedFromProducers,
 * PowerReleased,
 * WorkOut,
 * WorkSelfSupplied
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class Location extends Device {
    private final Logger logger = LoggerFactory.getLogger(Location.class);
    public static final String solarWattClassname = "com.kiwigrid.devices.location.Location";

    private @Nullable IdDevicesMap devicesMap;

    public Location(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    public void update(DeviceDTO deviceDTO) {
        super.update(deviceDTO);

        // values to put on a overview dashboard
        this.addWattQuantity(CHANNEL_POWER_BUFFERED, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_SELF_CONSUMED, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_SELF_SUPPLIED, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_CONSUMED_FROM_GRID, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_CONSUMED_FROM_STORAGE, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_CONSUMED, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_PRODUCED, deviceDTO);
        this.addWattQuantity(CHANNEL_POWER_OUT, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_BUFFERED, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_SELF_CONSUMED, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_SELF_SUPPLIED, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_CONSUMED_FROM_GRID, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_CONSUMED_FROM_STORAGE, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_CONSUMED, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_PRODUCED, deviceDTO);
        this.addWattHourQuantity(CHANNEL_WORK_OUT, deviceDTO);

        // not necessary for a dashboard, so marked as advanced
        this.addWattQuantity(CHANNEL_POWER_BUFFERED_FROM_GRID, deviceDTO, true);
        this.addWattQuantity(CHANNEL_POWER_BUFFERED_FROM_PRODUCERS, deviceDTO, true);
        this.addWattQuantity(CHANNEL_POWER_CONSUMED_FROM_PRODUCERS, deviceDTO, true);
        this.addWattQuantity(CHANNEL_POWER_IN, deviceDTO, true);
        this.addWattQuantity(CHANNEL_POWER_OUT_FROM_PRODUCERS, deviceDTO, true);
        this.addWattQuantity(CHANNEL_POWER_OUT_FROM_STORAGE, deviceDTO, true);
        this.addWattQuantity(CHANNEL_POWER_RELEASED, deviceDTO, true);
        this.addWattHourQuantity(CHANNEL_WORK_BUFFERED_FROM_GRID, deviceDTO, true);
        this.addWattHourQuantity(CHANNEL_WORK_BUFFERED_FROM_PRODUCERS, deviceDTO, true);
        this.addWattHourQuantity(CHANNEL_WORK_CONSUMED_FROM_PRODUCERS, deviceDTO, true);
        this.addWattHourQuantity(CHANNEL_WORK_OUT_FROM_PRODUCERS, deviceDTO, true);
        this.addWattHourQuantity(CHANNEL_WORK_OUT_FROM_STORAGE, deviceDTO, true);
        this.addWattHourQuantity(CHANNEL_WORK_RELEASED, deviceDTO, true);

        // calculate direct consumption for display purposes
        this.calculateDifferenceToState(CHANNEL_POWER_SELF_CONSUMED.getChannelName(),
                CHANNEL_POWER_BUFFERED.getChannelName(), CHANNEL_POWER_DIRECT_CONSUMED.getChannelName(), "energy");
        this.calculateDifferenceToState(CHANNEL_WORK_SELF_CONSUMED.getChannelName(),
                CHANNEL_WORK_BUFFERED.getChannelName(), CHANNEL_WORK_DIRECT_CONSUMED.getChannelName(), "energy");

        // read IdDevicesMap to find out which devices are located/metered where
        // to get the unmetered consumption we need to take Location.(Work|Power)Consumed
        // and subtract the (Work|Power)(AC)In of the OUTER_CONSUMERs
        try {
            JsonObject rawDevicesMap = deviceDTO.getJsonObjectFromTag("IdDevicesMap");
            Gson gson = new GsonBuilder().create();
            this.devicesMap = gson.fromJson(rawDevicesMap, IdDevicesMap.class);
        } catch (Exception ex) {
            this.devicesMap = null;
            this.logger.error("Could not read IdDevicesMap", ex);
        }
    }

    private void calculateDifferenceToState(String channelValue, String channelSubtract, String channelTarget,
            String category) {
        State stateValue = this.stateValues.get(channelValue);
        State stateSubtract = this.stateValues.get(channelSubtract);
        if (stateValue != null && stateSubtract != null) {
            @SuppressWarnings("rawtypes")
            @Nullable
            QuantityType quantityValue = stateValue.as(QuantityType.class);
            @SuppressWarnings("rawtypes")
            @Nullable
            QuantityType qunatitySubtract = stateSubtract.as(QuantityType.class);

            if (quantityValue != null && qunatitySubtract != null) {
                @SuppressWarnings("rawtypes")
                QuantityType quantityTarget = new QuantityType(
                        quantityValue.toBigDecimal().subtract(qunatitySubtract.toBigDecimal()),
                        qunatitySubtract.getUnit());
                this.stateValues.put(channelTarget, quantityTarget);

                if (!this.solarwattChannelSet.containsKey(channelTarget)) {
                    this.solarwattChannelSet.put(channelTarget,
                            new SolarwattChannel(channelTarget, qunatitySubtract.getUnit(), category));
                }
            }
        }
    }

    public @Nullable IdDevicesMap getDevicesMap() {
        return this.devicesMap;
    }

    @Override
    protected String getSolarWattLabel() {
        return "Location";
    }

    public static class IdDevicesMap {
        private @Nullable Set<String> INNER_BUFFER;
        private @Nullable Set<String> INNER_CONSUMER;
        private @Nullable Set<String> POWERMETER_CONSUMPTION;
        private @Nullable Set<String> OUTER_CONSUMER;
        private @Nullable Set<String> OUTER_BUFFER;
        private @Nullable Set<String> POWERMETER_PRODUCTION;
        private @Nullable Set<String> OUTER_PRODUCER;
        private @Nullable Set<String> INNER_PRODUCER;

        public @Nullable Set<String> getInnerBuffer() {
            return this.INNER_BUFFER;
        }

        public @Nullable Set<String> getInnerConsumer() {
            return this.INNER_CONSUMER;
        }

        public @Nullable Set<String> getPowermeterConsumption() {
            return this.POWERMETER_CONSUMPTION;
        }

        public @Nullable Set<String> getOuterConsumer() {
            return this.OUTER_CONSUMER;
        }

        public @Nullable Set<String> getOuterBuffer() {
            return this.OUTER_BUFFER;
        }

        public @Nullable Set<String> getPowermeterProduction() {
            return this.POWERMETER_PRODUCTION;
        }

        public @Nullable Set<String> getOuterProducer() {
            return this.OUTER_PRODUCER;
        }

        public @Nullable Set<String> getInnerProducer() {
            return this.INNER_PRODUCER;
        }
    }
}
