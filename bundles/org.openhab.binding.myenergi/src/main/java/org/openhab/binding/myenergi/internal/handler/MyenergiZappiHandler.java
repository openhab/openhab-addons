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
package org.openhab.binding.myenergi.internal.handler;

import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGER_IS_LOCKED;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGER_LOCKED_WHEN_PLUGGED;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGER_LOCKED_WHEN_UNPLUGGED;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGER_STATUS;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGE_ADDED;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGE_WHEN_LOCKED;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CHARGING_MODE;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_NAME_1;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_NAME_2;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_NAME_3;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_NAME_4;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_NAME_5;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_NAME_6;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_POWER_1;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_POWER_2;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_POWER_3;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_POWER_4;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_POWER_5;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CLAMP_POWER_6;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_CONSUMED_POWER;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_DIVERTED_POWER;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_DIVERTER_PRIORITY;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_GENERATED_POWER;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_GRID_POWER;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_LAST_COMMAND_STATUS;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_LAST_UPDATED_TIME;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_PLUG_STATUS;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_SMART_BOOST_CHARGE;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_SMART_BOOST_DURATION;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_SUPPLY_FREQUENCY;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_SUPPLY_VOLTAGE;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_TIMED_BOOST_CHARGE;
import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.ZAPPI_CHANNEL_TIMED_BOOST_DURATION;
import static org.openhab.core.library.unit.Units.HERTZ;
import static org.openhab.core.library.unit.Units.KILOWATT_HOUR;
import static org.openhab.core.library.unit.Units.VOLT;
import static org.openhab.core.library.unit.Units.WATT;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.InvalidDataException;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;
import org.openhab.binding.myenergi.internal.model.ZappiChargingMode;
import org.openhab.binding.myenergi.internal.model.ZappiSummary;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiZappiHandler} is responsible for handling things created
 * to represent Zappis.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class MyenergiZappiHandler extends MyenergiBaseDeviceHandler {

    private static final int LOCKING_MODE_BIT_CHARGER_IS_LOCKED = 0;
    private static final int LOCKING_MODE_BIT_CHARGER_LOCKED_WHEN_PLUGGED = 1;
    private static final int LOCKING_MODE_BIT_CHARGER_LOCKED_WHEN_UNPLUGGED = 2;
    private static final int LOCKING_MODE_BIT_CHARGE_WHEN_LOCKED = 3;

    private final Logger logger = LoggerFactory.getLogger(MyenergiZappiHandler.class);

    public MyenergiZappiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyenergiZappiActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        } else {
            switch (channelUID.getId()) {
                case ZAPPI_CHANNEL_CHARGING_MODE:
                    logger.debug("Received ZAPPI_CHANNEL_CHARGING_MODE update command: {}", command.toString());
                    try {
                        ZappiChargingMode newMode;
                        if (command instanceof DecimalType) {
                            switch (((DecimalType) command).intValue()) {
                                case 0:
                                    newMode = ZappiChargingMode.BOOST;
                                    break;
                                case 1:
                                    newMode = ZappiChargingMode.FAST;
                                    break;
                                case 2:
                                    newMode = ZappiChargingMode.ECO;
                                    break;
                                case 3:
                                    newMode = ZappiChargingMode.ECO_PLUS;
                                    break;
                                case 4:
                                    newMode = ZappiChargingMode.STOP;
                                    break;
                                default:
                                    throw new InvalidDataException();
                            }
                        } else if (command instanceof StringType) {
                            switch (((StringType) command).toString()) {
                                case "Boost":
                                case "BOOST":
                                case "0":
                                    newMode = ZappiChargingMode.BOOST;
                                    break;
                                case "Fast":
                                case "FAST":
                                case "1":
                                    newMode = ZappiChargingMode.FAST;
                                    break;
                                case "Eco":
                                case "ECO":
                                case "2":
                                    newMode = ZappiChargingMode.ECO;
                                    break;
                                case "Eco+":
                                case "ECO_PLUS":
                                case "3":
                                    newMode = ZappiChargingMode.ECO_PLUS;
                                    break;
                                case "Stop":
                                case "STOP":
                                case "4":
                                    newMode = ZappiChargingMode.STOP;
                                    break;
                                default:
                                    throw new InvalidDataException();
                            }
                        } else {
                            throw new InvalidDataException();
                        }
                        MyenergiBridgeHandler bh = getBridgeHandler();
                        if (bh == null) {
                            logger.warn("No bridge handler available");
                            throw new ApiException("No bridge handler available");
                        }
                        bh.setZappiChargingMode(serialNumber, newMode);
                    } catch (InvalidDataException e) {
                        logger.info("Invalid ZAPPI_CHANNEL_CHARGING_MODE update received, mode was: {} ({})",
                                command.toString(), command.getClass().toString());
                    } catch (ApiException e) {
                        logger.warn("Exception from API - {}", getThing().getUID(), e);
                    }
                    break;
                // add more command handling here (priority, mgl, boost times, ...)
            }
        }
    }

    @Override
    protected void updateThing() {
        try {
            logger.debug("Updating all thing channels for device : {}", serialNumber);
            MyenergiBridgeHandler bh = getBridgeHandler();
            if (bh == null) {
                logger.warn("No bridge handler available");
                return;
            }
            ZappiSummary device = bh.getData().getZappiBySerialNumber(serialNumber);

            updateDateTimeState(ZAPPI_CHANNEL_LAST_UPDATED_TIME, device.getLastUpdateTime());
            final Float supplyVoltageInTenthVolt = device.supplyVoltageInTenthVolt;
            if (supplyVoltageInTenthVolt != null) {
                updateElectricPotentialState(ZAPPI_CHANNEL_SUPPLY_VOLTAGE, supplyVoltageInTenthVolt / 10.0f, VOLT);
            }
            updateFrequencyState(ZAPPI_CHANNEL_SUPPLY_FREQUENCY, device.supplyFrequency, HERTZ);

            final Integer lockingMode = device.lockingMode;
            if (lockingMode != null) {
                updateSwitchState(ZAPPI_CHANNEL_CHARGER_IS_LOCKED,
                        ((lockingMode & (1 << LOCKING_MODE_BIT_CHARGER_IS_LOCKED)) != 0));
                updateSwitchState(ZAPPI_CHANNEL_CHARGER_LOCKED_WHEN_PLUGGED,
                        ((lockingMode & (1 << LOCKING_MODE_BIT_CHARGER_LOCKED_WHEN_PLUGGED)) != 0));
                updateSwitchState(ZAPPI_CHANNEL_CHARGER_LOCKED_WHEN_UNPLUGGED,
                        ((lockingMode & (1 << LOCKING_MODE_BIT_CHARGER_LOCKED_WHEN_UNPLUGGED)) != 0));
                updateSwitchState(ZAPPI_CHANNEL_CHARGE_WHEN_LOCKED,
                        ((lockingMode & (1 << LOCKING_MODE_BIT_CHARGE_WHEN_LOCKED)) != 0));
            }
            updateStringState(ZAPPI_CHANNEL_CHARGING_MODE, device.chargingMode);
            updateStringState(ZAPPI_CHANNEL_CHARGER_STATUS, device.status);
            updateStringState(ZAPPI_CHANNEL_PLUG_STATUS, device.plugStatus);

            final Integer commandTries = device.commandTries;
            if (commandTries != null) {
                switch (commandTries) {
                    case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> updateStringState(ZAPPI_CHANNEL_LAST_COMMAND_STATUS,
                            "Attempting to process a command (" + commandTries + " tries left)");
                    case 253 -> updateStringState(ZAPPI_CHANNEL_LAST_COMMAND_STATUS, "Command acknowledged and Failed");
                    case 254 -> updateStringState(ZAPPI_CHANNEL_LAST_COMMAND_STATUS, "Command acknowledged and OK");
                    case 255 -> updateStringState(ZAPPI_CHANNEL_LAST_COMMAND_STATUS, "No command has ever been sent");
                    default ->
                        updateStringState(ZAPPI_CHANNEL_LAST_COMMAND_STATUS, "Unknown command status: " + commandTries);
                }
            }
            updateIntegerState(ZAPPI_CHANNEL_DIVERTER_PRIORITY, device.diverterPriority);
            updatePercentageState(ZAPPI_CHANNEL_MINIMUM_GREEN_LEVEL, device.minimumGreenLevel);

            updatePowerState(ZAPPI_CHANNEL_GRID_POWER, device.gridPower, WATT);
            updatePowerState(ZAPPI_CHANNEL_GENERATED_POWER, device.generatedPower, WATT);
            updatePowerState(ZAPPI_CHANNEL_DIVERTED_POWER, device.divertedPower, WATT);
            final Integer gridPower = device.gridPower;
            final Integer generatedPower = device.generatedPower;
            int consumedPower = ((gridPower != null) ? gridPower : 0) + ((generatedPower != null) ? generatedPower : 0);
            updatePowerState(ZAPPI_CHANNEL_CONSUMED_POWER, consumedPower, WATT);

            updateEnergyState(ZAPPI_CHANNEL_CHARGE_ADDED, device.chargeAdded, KILOWATT_HOUR);

            updateDurationState(ZAPPI_CHANNEL_SMART_BOOST_DURATION, device.smartBoostHour, device.smartBoostMinute);
            updateEnergyState(ZAPPI_CHANNEL_SMART_BOOST_CHARGE, device.smartBoostCharge, KILOWATT_HOUR);
            updateDurationState(ZAPPI_CHANNEL_TIMED_BOOST_DURATION, device.timedBoostHour, device.timedBoostMinute);
            updateEnergyState(ZAPPI_CHANNEL_TIMED_BOOST_CHARGE, device.timedBoostCharge, KILOWATT_HOUR);

            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_1, device.clampName1);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_2, device.clampName2);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_3, device.clampName3);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_4, device.clampName4);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_5, device.clampName5);
            updateStringState(ZAPPI_CHANNEL_CLAMP_NAME_6, device.clampName6);

            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_1, device.clampPower1, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_2, device.clampPower2, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_3, device.clampPower3, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_4, device.clampPower4, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_5, device.clampPower5, WATT);
            updatePowerState(ZAPPI_CHANNEL_CLAMP_POWER_6, device.clampPower6, WATT);
        } catch (

        RecordNotFoundException e) {
            logger.debug("Trying to update unknown device: {}", thing.getUID().getId());
        }
    }

    @Override
    protected void refreshMeasurements() throws ApiException {
        try {
            MyenergiBridgeHandler bh = getBridgeHandler();
            if (bh == null) {
                logger.warn("No bridge handler available");
                throw new ApiException("No bridge handler available");
            }
            bh.updateZappiSummary(serialNumber);
        } catch (RecordNotFoundException e) {
            logger.warn("invalid serial number: {}", serialNumber, e);
        }
    }
}
