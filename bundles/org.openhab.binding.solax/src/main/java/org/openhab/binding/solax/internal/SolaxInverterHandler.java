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
package org.openhab.binding.solax.internal;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolaxInverterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxInverterHandler extends BaseThingHandler implements InverterDataUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(SolaxInverterHandler.class);

    public SolaxInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void updateListener(InverterData data) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        logger.debug("Received a new inverter data object. Data = {}", data.toStringDetailed());
        transferInverterDataToChannels(data);
    }

    private void transferInverterDataToChannels(InverterData data) {
        // TODO check with reviewer if type and serial number is needed as a channel or as a property
        updateProperty(SolaxBindingConstants.SERIAL_NUMBER, data.getWifiSerial());
        updateProperty(SolaxBindingConstants.INVERTER_TYPE, data.getInverterType().name());

        updateState(SolaxBindingConstants.INVERTER_OUTPUT_POWER,
                new QuantityType<>(data.getInvertеrOutputPower(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_OUTPUT_CURRENT,
                new QuantityType<>(data.getInverterCurrent(), Units.AMPERE));
        updateState(SolaxBindingConstants.INVERTER_OUTPUT_VOLTAGE,
                new QuantityType<>(data.getInverterVoltage(), Units.VOLT));
        updateState(SolaxBindingConstants.INVERTER_OUTPUT_FREQUENCY,
                new QuantityType<>(data.getInverterFrequency(), Units.HERTZ));

        updateState(SolaxBindingConstants.INVERTER_PV1_POWER, new QuantityType<>(data.getPV1Power(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_PV1_CURRENT, new QuantityType<>(data.getPV1Current(), Units.AMPERE));
        updateState(SolaxBindingConstants.INVERTER_PV1_VOLTAGE, new QuantityType<>(data.getPV1Voltage(), Units.VOLT));

        updateState(SolaxBindingConstants.INVERTER_PV2_POWER, new QuantityType<>(data.getPV2Power(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_PV2_CURRENT, new QuantityType<>(data.getPV2Current(), Units.AMPERE));
        updateState(SolaxBindingConstants.INVERTER_PV2_VOLTAGE, new QuantityType<>(data.getPV2Voltage(), Units.VOLT));

        updateState(SolaxBindingConstants.INVERTER_PV_TOTAL_POWER,
                new QuantityType<>(data.getPVTotalPower(), Units.WATT));
        updateState(SolaxBindingConstants.INVERTER_PV_TOTAL_CURRENT,
                new QuantityType<>(data.getPVTotalCurrent(), Units.AMPERE));

        updateState(SolaxBindingConstants.BATTERY_POWER, new QuantityType<>(data.getBatteryPower(), Units.WATT));
        updateState(SolaxBindingConstants.BATTERY_CURRENT, new QuantityType<>(data.getBatteryCurrent(), Units.AMPERE));
        updateState(SolaxBindingConstants.BATTERY_VOLTAGE, new QuantityType<>(data.getBatteryVoltage(), Units.VOLT));
        updateState(SolaxBindingConstants.BATTERY_TEMPERATURE,
                new QuantityType<>(data.getBatteryTemperature(), SIUnits.CELSIUS));
        updateState(SolaxBindingConstants.BATTERY_STATE_OF_CHARGE,
                new QuantityType<>(data.getBatterySoC(), Units.PERCENT));

        updateState(SolaxBindingConstants.FEED_IN_POWER, new QuantityType<>(data.getFeedInPower(), Units.WATT));

        updateState(SolaxBindingConstants.TIMESTAMP, new DateTimeType(ZonedDateTime.now()));
        updateState(SolaxBindingConstants.RAW_DATA, new StringType(data.getRawData()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here as of now
    }
}
