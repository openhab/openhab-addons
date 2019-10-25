/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import javax.measure.quantity.Mass;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDeviceControl;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDeviceControl.Bowls.BowlSettings;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDeviceCurfew;
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

    private static final String CURFEW_TIME_REGEXP = "^\\d\\d\\:\\d\\d$";
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
        logger.debug("DeviceHandler handleCommand called with command: {}", command.toFullString());

        if (command instanceof RefreshType) {
            synchronized (petcareAPI) {
                updateThing();
            }
        } else if (channelUID.getId().startsWith(DEVICE_CHANNEL_CURFEW_BASE)) {
            handleCurfewCommand(channelUID, command);
        } else {
            switch (channelUID.getId()) {
                case DEVICE_CHANNEL_LOCKING_MODE:
                    if (command instanceof StringType) {
                        synchronized (petcareAPI) {
                            SurePetcareDevice device = petcareAPI.getDevice(thing.getUID().getId());
                            if (device != null) {
                                String newLockingModeIdStr = ((StringType) command).toString();
                                try {
                                    Integer newLockingModeId = Integer.valueOf(newLockingModeIdStr);
                                    petcareAPI.setDeviceLockingMode(device, newLockingModeId);
                                    updateState(DEVICE_CHANNEL_LOCKING_MODE,
                                            new StringType(device.getStatus().getLocking().modeId.toString()));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid locking mode: {}, ignoring command", newLockingModeIdStr);
                                } catch (SurePetcareApiException e) {
                                    logger.warn(
                                            "Error from SurePetcare API. Can't update locking mode {} for device {}",
                                            newLockingModeIdStr, device);
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
                                String newLedModeIdStr = ((StringType) command).toString();
                                try {
                                    Integer newLedModeId = Integer.valueOf(newLedModeIdStr);
                                    petcareAPI.setDeviceLedMode(device, newLedModeId);
                                    updateState(DEVICE_CHANNEL_LOCKING_MODE,
                                            new StringType(device.getStatus().getLedModeId().toString()));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid locking mode: {}, ignoring command", newLedModeIdStr);
                                } catch (SurePetcareApiException e) {
                                    logger.warn("Error from SurePetcare API. Can't update LED mode {} for device {}",
                                            newLedModeIdStr, device);
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
            updateState(DEVICE_CHANNEL_ID, new DecimalType(device.getId()));
            updateState(DEVICE_CHANNEL_NAME, new StringType(device.getName()));
            updateState(DEVICE_CHANNEL_PRODUCT, new StringType(device.getProductId().toString()));
            updateState(DEVICE_CHANNEL_HARDWARE_VERSION,
                    new DecimalType(device.getStatus().getVersion().device.hardware));
            updateState(DEVICE_CHANNEL_FIRMWARE_VERSION,
                    new DecimalType(device.getStatus().getVersion().device.firmware));
            updateState(DEVICE_CHANNEL_ONLINE, OnOffType.from(device.getStatus().getOnline()));
            updateState(DEVICE_CHANNEL_CREATED_AT, new DateTimeType(device.getCreatedAtAsZonedDateTime()));
            updateState(DEVICE_CHANNEL_UPDATED_AT, new DateTimeType(device.getUpdatedAtAsZonedDateTime()));
            updateState(DEVICE_CHANNEL_SERIAL_NUMBER, new StringType(device.getSerialNumber()));
            updateState(DEVICE_CHANNEL_MAC_ADDRESS, new StringType(device.getMacAddress()));

            if (thing.getThingTypeUID().equals(THING_TYPE_HUB_DEVICE)) {
                updateState(DEVICE_CHANNEL_LED_MODE, new StringType(device.getStatus().getLedModeId().toString()));
                updateState(DEVICE_CHANNEL_PAIRING_MODE,
                        new StringType(device.getStatus().getPairingModeId().toString()));
            } else {
                float batVol = device.getStatus().getBattery();
                updateState(DEVICE_CHANNEL_BATTERY_VOLTAGE, new DecimalType(batVol));
                updateState(DEVICE_CHANNEL_BATTERY_LEVEL, new DecimalType(Math.min(
                        (batVol - BATTERY_EMPTY_VOLTAGE) / (BATTERY_FULL_VOLTAGE - BATTERY_EMPTY_VOLTAGE) * 100.0f,
                        100.0f)));
                updateState(DEVICE_CHANNEL_LOW_BATTERY, OnOffType.from(batVol < LOW_BATTERY_THRESHOLD));
                updateState(DEVICE_CHANNEL_PAIRING_AT, new DateTimeType(device.getPairingAtAsZonedDateTime()));
                updateState(DEVICE_CHANNEL_DEVICE_RSSI, new DecimalType(device.getStatus().getSignal().deviceRssi));
                updateState(DEVICE_CHANNEL_HUB_RSSI, new DecimalType(device.getStatus().getSignal().hubRssi));

                if (thing.getThingTypeUID().equals(THING_TYPE_FLAP_DEVICE)) {
                    updateThingCurfews(device);
                    updateState(DEVICE_CHANNEL_LOCKING_MODE,
                            new StringType(device.getStatus().getLocking().modeId.toString()));
                } else if (thing.getThingTypeUID().equals(THING_TYPE_FEEDER_DEVICE)) {
                    int numBowls = device.getControl().getBowls().getBowlSettings().size();
                    for (int i = 0; (i < 2) && (i < numBowls); i++) {
                        BowlSettings bowlSettings = device.getControl().getBowls().getBowlSettings().get(i);
                        if (device.getControl().getBowls().getBowlId().equals(1)) {
                            updateState(DEVICE_CHANNEL_BOWLS_FOOD, new StringType(bowlSettings.getFoodId().toString()));
                            updateState(DEVICE_CHANNEL_BOWLS_TARGET,
                                    new QuantityType<Mass>(bowlSettings.getTargetId(), SIUnits.GRAM));
                        } else if (device.getControl().getBowls().getBowlId().equals(4)) {
                            if ((i + 1) == 1) {
                                updateState(DEVICE_CHANNEL_BOWLS_FOOD_LEFT,
                                        new StringType(bowlSettings.getFoodId().toString()));
                                updateState(DEVICE_CHANNEL_BOWLS_TARGET_LEFT,
                                        new QuantityType<Mass>(bowlSettings.getTargetId(), SIUnits.GRAM));
                            }
                            if ((i + 1) == 2) {
                                updateState(DEVICE_CHANNEL_BOWLS_FOOD_RIGHT,
                                        new StringType(bowlSettings.getFoodId().toString()));
                                updateState(DEVICE_CHANNEL_BOWLS_TARGET_RIGHT,
                                        new QuantityType<Mass>(bowlSettings.getTargetId(), SIUnits.GRAM));
                            }
                        }
                    }
                    updateState(DEVICE_CHANNEL_BOWLS,
                            new StringType(device.getControl().getBowls().getBowlId().toString()));
                    updateState(DEVICE_CHANNEL_BOWLS_CLOSE_DELAY,
                            new StringType(device.getControl().getLid().getCloseDelayId().toString()));
                    updateState(DEVICE_CHANNEL_BOWLS_TRAINING_MODE,
                            new StringType(device.getControl().getTrainingModeId().toString()));
                } else {
                    logger.warn("Unknown product type for device {}", thing.getUID().getAsString());
                }
            }
        }
    }

    private void updateThingCurfews(SurePetcareDevice device) {
        for (int i = 0; i < FLAP_MAX_NUMBER_OF_CURFEWS; i++) {
            SurePetcareDeviceCurfew curfew = device.getControl().getCurfewList().get(i);

            updateState(DEVICE_CHANNEL_CURFEW_ENABLED + (i + 1), OnOffType.from(curfew.enabled));
            updateState(DEVICE_CHANNEL_CURFEW_LOCK_TIME + (i + 1), new StringType(curfew.lockTime));
            updateState(DEVICE_CHANNEL_CURFEW_UNLOCK_TIME + (i + 1), new StringType(curfew.unlockTime));
        }

    }

    private void handleCurfewCommand(ChannelUID channelUID, Command command) {
        String channelUIDBase = channelUID.getIdWithoutGroup().substring(0,
                channelUID.getIdWithoutGroup().length() - 1);
        int slot = Integer.parseInt(channelUID.getAsString().substring(channelUID.getAsString().length() - 1));

        synchronized (petcareAPI) {
            boolean requiresUpdate = false;
            SurePetcareDevice device = petcareAPI.getDevice(thing.getUID().getId());
            if (device != null) {
                SurePetcareDeviceControl existingControl = device.getControl();
                logger.debug("Old Curfew Control: {}", existingControl);

                SurePetcareDeviceCurfew curfew = existingControl.getCurfewList().get(slot - 1);
                logger.debug("channelUIDBase: {}", channelUIDBase);

                switch (channelUIDBase) {
                    case DEVICE_CHANNEL_CURFEW_ENABLED:
                        if (command instanceof OnOffType) {
                            logger.debug("old enables: {}, command: {}", curfew.enabled, command.equals(OnOffType.ON));
                            if (curfew.enabled != command.equals(OnOffType.ON)) {
                                requiresUpdate = true;
                            }
                            curfew.enabled = (command.equals(OnOffType.ON));
                        }
                        break;
                    case DEVICE_CHANNEL_CURFEW_LOCK_TIME:
                        if ((command instanceof StringType) && (command.toString().matches(CURFEW_TIME_REGEXP))) {
                            logger.debug("old locktime: {}, new locktime: {}", curfew.lockTime, command);

                            if (!(curfew.lockTime.equals(command.toString())) && curfew.enabled) {
                                requiresUpdate = true;
                            }
                            curfew.lockTime = command.toString();
                        } else {
                            logger.warn("Incorrect curfew time format HH:mm: {}", command);
                        }
                        break;
                    case DEVICE_CHANNEL_CURFEW_UNLOCK_TIME:
                        if ((command instanceof StringType) && (command.toString().matches(CURFEW_TIME_REGEXP))) {
                            if (!(curfew.unlockTime.equals(command.toString())) && curfew.enabled) {
                                requiresUpdate = true;
                            }
                            curfew.unlockTime = command.toString();
                        } else {
                            logger.warn("Incorrect curfew time format HH:mm: {}", command);
                        }
                        break;
                    default:
                        break;
                }
                logger.debug("requiresUpdate: {}", requiresUpdate);

                if (requiresUpdate) {
                    try {
                        petcareAPI.setCurfews(device, existingControl.getCurfewList());
                        updateThingCurfews(device);
                    } catch (SurePetcareApiException e) {
                        logger.warn("Error from SurePetcare API. Can't update curfews for device {}", device);
                    }
                }
            }

        }
    }

}
