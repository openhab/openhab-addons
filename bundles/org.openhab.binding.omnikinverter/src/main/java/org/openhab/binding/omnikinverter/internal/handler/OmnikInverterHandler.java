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
package org.openhab.binding.omnikinverter.internal.handler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnikinverter.internal.OmnikInverter;
import org.openhab.binding.omnikinverter.internal.OmnikInverterBindingConstants;
import org.openhab.binding.omnikinverter.internal.OmnikInverterConfiguration;
import org.openhab.binding.omnikinverter.internal.OmnikInverterMessage;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OmnikInverterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans van den Bogert - Initial contribution
 */
@NonNullByDefault
public class OmnikInverterHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OmnikInverterHandler.class);

    private @Nullable OmnikInverter inverter;
    private @Nullable ScheduledFuture<?> pollJob;

    public OmnikInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels depend on data gotten from `updateData()`
        if (command instanceof RefreshType) {
            updateData();
        }
    }

    @Override
    public void initialize() {
        OmnikInverterConfiguration config = getConfigAs(OmnikInverterConfiguration.class);

        inverter = new OmnikInverter(config.hostname, config.port, config.serial);
        updateStatus(ThingStatus.UNKNOWN);
        pollJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollJob = this.pollJob;
        if (pollJob != null) {
            pollJob.cancel(true);
            this.pollJob = null;
        }
        super.dispose();
    }

    private void updateData() {
        try {
            if (inverter != null) {
                OmnikInverterMessage message = inverter.pullCurrentStats();

                updateStatus(ThingStatus.ONLINE);

                /**
                 * AC
                 **/
                QuantityType<Power> powerQuantity = new QuantityType<>(message.getPower(), Units.WATT);
                updateState(OmnikInverterBindingConstants.CHANNEL_POWER, powerQuantity);

                QuantityType<Power> powerQuantity1 = new QuantityType<>(message.getPowerAC1(), Units.WATT);
                updateState(OmnikInverterBindingConstants.CHANNEL_POWER_AC1, powerQuantity1);

                QuantityType<Power> powerQuantity2 = new QuantityType<>(message.getPowerAC2(), Units.WATT);
                updateState(OmnikInverterBindingConstants.CHANNEL_POWER_AC2, powerQuantity2);

                QuantityType<Power> powerQuantity3 = new QuantityType<>(message.getPowerAC3(), Units.WATT);
                updateState(OmnikInverterBindingConstants.CHANNEL_POWER_AC3, powerQuantity3);

                QuantityType<ElectricPotential> voltageQuantity1 = new QuantityType<>(message.getVoltageAC1(),
                        Units.VOLT);
                updateState(OmnikInverterBindingConstants.CHANNEL_VOLTAGE_AC1, voltageQuantity1);

                QuantityType<ElectricPotential> voltageQuantity2 = new QuantityType<>(message.getVoltageAC2(),
                        Units.VOLT);
                updateState(OmnikInverterBindingConstants.CHANNEL_VOLTAGE_AC2, voltageQuantity2);

                QuantityType<ElectricPotential> voltageQuantity3 = new QuantityType<>(message.getVoltageAC3(),
                        Units.VOLT);
                updateState(OmnikInverterBindingConstants.CHANNEL_VOLTAGE_AC3, voltageQuantity3);

                QuantityType<ElectricCurrent> currentQuantity1 = new QuantityType<>(message.getCurrentAC1(),
                        Units.AMPERE);
                updateState(OmnikInverterBindingConstants.CHANNEL_CURRENT_AC1, currentQuantity1);

                QuantityType<ElectricCurrent> currentQuantity2 = new QuantityType<>(message.getCurrentAC2(),
                        Units.AMPERE);
                updateState(OmnikInverterBindingConstants.CHANNEL_CURRENT_AC2, currentQuantity2);

                QuantityType<ElectricCurrent> currentQuantity3 = new QuantityType<>(message.getCurrentAC3(),
                        Units.AMPERE);
                updateState(OmnikInverterBindingConstants.CHANNEL_CURRENT_AC3, currentQuantity3);

                QuantityType<Frequency> frequencyQuantity1 = new QuantityType<>(message.getFrequencyAC1(), Units.HERTZ);
                updateState(OmnikInverterBindingConstants.CHANNEL_FREQUENCY_AC1, frequencyQuantity1);

                QuantityType<Frequency> frequencyQuantity2 = new QuantityType<>(message.getFrequencyAC2(), Units.HERTZ);
                updateState(OmnikInverterBindingConstants.CHANNEL_FREQUENCY_AC2, frequencyQuantity2);

                QuantityType<Frequency> frequencyQuantity3 = new QuantityType<>(message.getFrequencyAC3(), Units.HERTZ);
                updateState(OmnikInverterBindingConstants.CHANNEL_FREQUENCY_AC3, frequencyQuantity3);

                /**
                 * PV
                 **/

                QuantityType<ElectricCurrent> pvAmp1 = new QuantityType<>(message.getCurrentPV1(), Units.AMPERE);
                updateState(OmnikInverterBindingConstants.CHANNEL_CURRENT_PV1, pvAmp1);

                QuantityType<ElectricCurrent> pvAmp2 = new QuantityType<>(message.getCurrentPV2(), Units.AMPERE);
                updateState(OmnikInverterBindingConstants.CHANNEL_CURRENT_PV2, pvAmp2);

                QuantityType<ElectricCurrent> pvAmp3 = new QuantityType<>(message.getCurrentPV3(), Units.AMPERE);
                updateState(OmnikInverterBindingConstants.CHANNEL_CURRENT_PV3, pvAmp3);

                QuantityType<ElectricPotential> pvVoltage1 = new QuantityType<>(message.getVoltagePV1(), Units.VOLT);
                updateState(OmnikInverterBindingConstants.CHANNEL_VOLTAGE_PV1, pvVoltage1);

                QuantityType<ElectricPotential> pvVoltage2 = new QuantityType<>(message.getVoltagePV2(), Units.VOLT);
                updateState(OmnikInverterBindingConstants.CHANNEL_VOLTAGE_PV2, pvVoltage2);

                QuantityType<ElectricPotential> pvVoltage3 = new QuantityType<>(message.getVoltagePV3(), Units.VOLT);
                updateState(OmnikInverterBindingConstants.CHANNEL_VOLTAGE_PV3, pvVoltage3);

                /**
                 * MISC
                 **/
                updateState(OmnikInverterBindingConstants.CHANNEL_ENERGY_TODAY,
                        new QuantityType<>(message.getEnergyToday(), Units.KILOWATT_HOUR));

                updateState(OmnikInverterBindingConstants.CHANNEL_ENERGY_TOTAL,
                        new QuantityType<>(message.getTotalEnergy(), Units.KILOWATT_HOUR));

                updateState(OmnikInverterBindingConstants.CHANNEL_TEMPERATURE,
                        new QuantityType<>(message.getTemperature(), SIUnits.CELSIUS));

                updateState(OmnikInverterBindingConstants.CHANNEL_HOURS_TOTAL,
                        new QuantityType<>(message.getHoursTotal(), Units.HOUR));
            }
        } catch (UnknownHostException | NoRouteToHostException | ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            logger.debug("Unknown exception when pulling data from the inverter: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Unknown error: " + e.getMessage());
        }
    }
}
