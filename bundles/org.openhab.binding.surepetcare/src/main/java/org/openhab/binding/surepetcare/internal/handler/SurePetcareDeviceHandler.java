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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceControl;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceControl.Bowls.BowlSettings;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceCurfew;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareDeviceHandler} is responsible for handling hubs and pet flaps created to represent Sure Petcare
 * devices.
 *
 * @author Rene Scherer - Initial Contribution
 * @author Holger Eisold - Added pet feeder status
 */
@NonNullByDefault
public class SurePetcareDeviceHandler extends SurePetcareBaseObjectHandler {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final float BATTERY_FULL_VOLTAGE = 4 * 1.5f; // 4x AA batteries of 1.5V each
    private static final float BATTERY_EMPTY_VOLTAGE = 4.2f; // 4x AA batteries of 1.05V each
    private static final float LOW_BATTERY_THRESHOLD = 4 * 1.1f;

    private final Logger logger = LoggerFactory.getLogger(SurePetcareDeviceHandler.class);

    public SurePetcareDeviceHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
        logger.debug("Created device handler for type {}", thing.getThingTypeUID().getAsString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        } else if (channelUID.getId().startsWith(DEVICE_CHANNEL_CURFEW_BASE)) {
            handleCurfewCommand(channelUID, command);
        } else {
            switch (channelUID.getId()) {
                case DEVICE_CHANNEL_LOCKING_MODE:
                    if (command instanceof StringType commandAsStringType) {
                        synchronized (petcareAPI) {
                            SurePetcareDevice device = petcareAPI.getDevice(thing.getUID().getId());
                            if (device != null) {
                                String newLockingModeIdStr = commandAsStringType.toString();
                                try {
                                    Integer newLockingModeId = Integer.valueOf(newLockingModeIdStr);
                                    petcareAPI.setDeviceLockingMode(device, newLockingModeId);
                                    updateState(DEVICE_CHANNEL_LOCKING_MODE,
                                            new StringType(device.status.locking.modeId.toString()));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid locking mode: {}, ignoring command", newLockingModeIdStr);
                                } catch (SurePetcareApiException e) {
                                    logger.warn(
                                            "Error from SurePetcare API. Can't update locking mode {} for device {}",
                                            newLockingModeIdStr, device, e);
                                }
                            }
                        }
                    }
                    break;
                case DEVICE_CHANNEL_LED_MODE:
                    if (command instanceof StringType) {
                        synchronized (petcareAPI) {
                            SurePetcareDevice device = petcareAPI.getDevice(thing.getUID().getId());
                            if (device != null) {
                                String newLedModeIdStr = command.toString();
                                try {
                                    Integer newLedModeId = Integer.valueOf(newLedModeIdStr);
                                    petcareAPI.setDeviceLedMode(device, newLedModeId);
                                    updateState(DEVICE_CHANNEL_LOCKING_MODE,
                                            new StringType(device.status.ledModeId.toString()));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid locking mode: {}, ignoring command", newLedModeIdStr);
                                } catch (SurePetcareApiException e) {
                                    logger.warn("Error from SurePetcare API. Can't update LED mode {} for device {}",
                                            newLedModeIdStr, device, e);
                                }
                            }
                        }
                    }
                    break;
                default:
                    logger.warn("Update on unsupported channel {}, ignoring command", channelUID.getId());
            }
        }
    }

    @Override
    protected void updateThing() {
        SurePetcareDevice device = petcareAPI.getDevice(thing.getUID().getId());
        if (device != null) {
            logger.debug("Updating all thing channels for device : {}", device);
            updateState(DEVICE_CHANNEL_ID, new DecimalType(device.id));
            updateState(DEVICE_CHANNEL_NAME, new StringType(device.name));
            updateState(DEVICE_CHANNEL_PRODUCT, new StringType(device.productId.toString()));
            updateState(DEVICE_CHANNEL_ONLINE, OnOffType.from(device.status.online));

            if (thing.getThingTypeUID().equals(THING_TYPE_HUB_DEVICE)) {
                updateState(DEVICE_CHANNEL_LED_MODE, new StringType(device.status.ledModeId.toString()));
                updateState(DEVICE_CHANNEL_PAIRING_MODE, new StringType(device.status.pairingModeId.toString()));
            } else {
                float batVol = device.status.battery;
                updateState(DEVICE_CHANNEL_BATTERY_VOLTAGE, new DecimalType(batVol));
                updateState(DEVICE_CHANNEL_BATTERY_LEVEL, new DecimalType(Math.min(
                        (batVol - BATTERY_EMPTY_VOLTAGE) / (BATTERY_FULL_VOLTAGE - BATTERY_EMPTY_VOLTAGE) * 100.0f,
                        100.0f)));
                updateState(DEVICE_CHANNEL_LOW_BATTERY, OnOffType.from(batVol < LOW_BATTERY_THRESHOLD));
                updateState(DEVICE_CHANNEL_DEVICE_RSSI,
                        QuantityType.valueOf(device.status.signal.deviceRssi, Units.DECIBEL_MILLIWATTS));
                updateState(DEVICE_CHANNEL_HUB_RSSI,
                        QuantityType.valueOf(device.status.signal.hubRssi, Units.DECIBEL_MILLIWATTS));

                if (thing.getThingTypeUID().equals(THING_TYPE_FLAP_DEVICE)) {
                    updateThingCurfews(device);
                    updateState(DEVICE_CHANNEL_LOCKING_MODE, new StringType(device.status.locking.modeId.toString()));
                } else if (thing.getThingTypeUID().equals(THING_TYPE_FEEDER_DEVICE)) {
                    int bowlId = device.control.bowls.bowlId;
                    List<BowlSettings> bowlSettings = device.control.bowls.bowlSettings;
                    int numBowls = bowlSettings.size();
                    if (numBowls > 0) {
                        if (bowlId == BOWL_ID_ONE_BOWL_USED) {
                            updateState(DEVICE_CHANNEL_BOWLS_FOOD,
                                    new StringType(bowlSettings.get(0).foodId.toString()));
                            updateState(DEVICE_CHANNEL_BOWLS_TARGET, new QuantityType<>(
                                    device.control.bowls.bowlSettings.get(0).targetId, SIUnits.GRAM));
                        } else if (bowlId == BOWL_ID_TWO_BOWLS_USED) {
                            updateState(DEVICE_CHANNEL_BOWLS_FOOD_LEFT,
                                    new StringType(bowlSettings.get(0).foodId.toString()));
                            updateState(DEVICE_CHANNEL_BOWLS_TARGET_LEFT,
                                    new QuantityType<>(bowlSettings.get(0).targetId, SIUnits.GRAM));
                            if (numBowls > 1) {
                                updateState(DEVICE_CHANNEL_BOWLS_FOOD_RIGHT,
                                        new StringType(bowlSettings.get(1).foodId.toString()));
                                updateState(DEVICE_CHANNEL_BOWLS_TARGET_RIGHT,
                                        new QuantityType<>(bowlSettings.get(1).targetId, SIUnits.GRAM));
                            }
                        }
                    }
                    updateState(DEVICE_CHANNEL_BOWLS, new StringType(device.control.bowls.bowlId.toString()));
                    updateState(DEVICE_CHANNEL_BOWLS_CLOSE_DELAY,
                            new StringType(device.control.lid.closeDelayId.toString()));
                    updateState(DEVICE_CHANNEL_BOWLS_TRAINING_MODE,
                            new StringType(device.control.trainingModeId.toString()));
                } else {
                    logger.warn("Unknown product type for device {}", thing.getUID().getAsString());
                }
            }
        }
    }

    private void updateThingCurfews(SurePetcareDevice device) {
        for (int i = 0; i < FLAP_MAX_NUMBER_OF_CURFEWS; i++) {
            SurePetcareDeviceCurfew curfew = device.control.curfewList.get(i);
            logger.debug("updateThingCurfews - Updating device curfew: {}", curfew.toString());
            updateState(DEVICE_CHANNEL_CURFEW_ENABLED + (i + 1), OnOffType.from(curfew.enabled));
            updateState(DEVICE_CHANNEL_CURFEW_LOCK_TIME + (i + 1),
                    new StringType(TIME_FORMATTER.format(curfew.lockTime)));
            updateState(DEVICE_CHANNEL_CURFEW_UNLOCK_TIME + (i + 1),
                    new StringType(TIME_FORMATTER.format(curfew.unlockTime)));
        }
    }

    private void handleCurfewCommand(ChannelUID channelUID, Command command) {
        String channelUIDBase = channelUID.getIdWithoutGroup().substring(0,
                channelUID.getIdWithoutGroup().length() - 1);
        int slot = Integer.parseInt(channelUID.getAsString().substring(channelUID.getAsString().length() - 1));

        synchronized (petcareAPI) {
            SurePetcareDevice device = petcareAPI.getDevice(thing.getUID().getId());
            if (device != null) {
                try {
                    SurePetcareDeviceControl existingControl = device.control;
                    SurePetcareDeviceCurfew curfew = existingControl.curfewList.get(slot - 1);
                    boolean requiresUpdate = false;
                    switch (channelUIDBase) {
                        case DEVICE_CHANNEL_CURFEW_ENABLED:
                            if (command instanceof OnOffType) {
                                if (curfew.enabled != command.equals(OnOffType.ON)) {
                                    logger.debug("Enabling curfew slot: {}", slot);
                                    requiresUpdate = true;
                                }
                                curfew.enabled = command.equals(OnOffType.ON);
                            }
                            break;
                        case DEVICE_CHANNEL_CURFEW_LOCK_TIME:
                            LocalTime newLockTime = LocalTime.parse(command.toString(), TIME_FORMATTER);
                            if (!(curfew.lockTime.equals(newLockTime)) && curfew.enabled) {
                                logger.debug("Changing curfew slot {} lock time to: {}", slot, newLockTime);
                                requiresUpdate = true;
                            }
                            curfew.lockTime = newLockTime;

                            break;
                        case DEVICE_CHANNEL_CURFEW_UNLOCK_TIME:
                            LocalTime newUnlockTime = LocalTime.parse(command.toString(), TIME_FORMATTER);
                            if (!(curfew.unlockTime.equals(newUnlockTime)) && curfew.enabled) {
                                logger.debug("Changing curfew slot {} unlock time to: {}", slot, newUnlockTime);
                                requiresUpdate = true;
                            }
                            curfew.unlockTime = newUnlockTime;

                            break;
                        default:
                            break;
                    }

                    if (requiresUpdate) {
                        try {
                            logger.debug("Updating curfews: {}", existingControl.curfewList.toString());
                            petcareAPI.setCurfews(device, existingControl.curfewList);
                            updateThingCurfews(device);
                        } catch (SurePetcareApiException e) {
                            logger.warn("Error from SurePetcare API. Can't update curfews for device {}: {}", device,
                                    e.getMessage());
                        }
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Incorrect curfew time format HH:mm: {}", e.getMessage());
                }
            }

        }
    }
}
