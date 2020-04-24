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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice.Control.Curfew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareDeviceHandler} is responsible for handling hubs and pet flaps created to represent Sure Petcare
 * devices.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class SurePetcareDeviceHandler extends SurePetcareBaseObjectHandler {

    private static final float BATTERY_FULL_VOLTAGE = 4 * 1.5f; // 4x AA batteries of 1.5V each
    private static final float LOW_BATTERY_THRESHOLD = BATTERY_FULL_VOLTAGE * 0.6f;

    private final Logger logger = LoggerFactory.getLogger(SurePetcareDeviceHandler.class);

    public SurePetcareDeviceHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
        logger.debug("Created device handler for type {}", thing.getThingTypeUID().getAsString());
    }

    @Override
    public void updateThing() {
        SurePetcareDevice device = petcareAPI.retrieveDevice(thing.getUID().getId());
        if (device != null) {
            logger.debug("updating all thing channels for device : {}", device.toString());
            updateState(SurePetcareConstants.DEVICE_CHANNEL_ID, new DecimalType(device.getId()));
            updateState(SurePetcareConstants.DEVICE_CHANNEL_NAME, new StringType(device.getName()));
            updateState(SurePetcareConstants.DEVICE_CHANNEL_PRODUCT_ID, new DecimalType(device.getProductId()));
            if (thing.getThingTypeUID().equals(SurePetcareConstants.THING_TYPE_HUB_DEVICE)) {
                updateState(SurePetcareConstants.DEVICE_CHANNEL_LED_MODE, new DecimalType(device.getStatus().ledMode));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_PAIRING_MODE,
                        new DecimalType(device.getStatus().pairingMode));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_HARDWARE_VERSION,
                        new StringType(device.getStatus().version.device.hardware));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_FIRMWARE_VERSION,
                        new StringType(device.getStatus().version.device.firmware));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_ONLINE, OnOffType.from(device.getStatus().online));
            } else if (thing.getThingTypeUID().equals(SurePetcareConstants.THING_TYPE_FLAP_DEVICE)) {
                int numCurfews = device.getControl().curfew.size();
                for (int i = 0; (i < 4) && (i < numCurfews); i++) {
                    Curfew curfew = device.getControl().curfew.get(i);
                    updateState(SurePetcareConstants.DEVICE_CHANNEL_CURFEW_ENABLED + (i + 1),
                            OnOffType.from(device.getStatus().online));
                    updateState(SurePetcareConstants.DEVICE_CHANNEL_CURFEW_LOCK_TIME + (i + 1),
                            new StringType(curfew.lockTime));
                    updateState(SurePetcareConstants.DEVICE_CHANNEL_CURFEW_UNLOCK_TIME + (i + 1),
                            new StringType(curfew.unlockTime));
                }
                updateState(SurePetcareConstants.DEVICE_CHANNEL_LOCKING_MODE,
                        new DecimalType(device.getStatus().locking.mode));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_HARDWARE_VERSION,
                        new StringType(device.getStatus().version.device.hardware));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_FIRMWARE_VERSION,
                        new StringType(device.getStatus().version.device.firmware));

                float batVol = device.getStatus().battery;
                updateState(SurePetcareConstants.DEVICE_CHANNEL_BATTERY_VOLTAGE, new DecimalType(batVol));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_BATTERY_LEVEL,
                        new DecimalType(Math.min(batVol / BATTERY_FULL_VOLTAGE * 100.0f, 100.0f)));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_LOW_BATTERY,
                        OnOffType.from(batVol < LOW_BATTERY_THRESHOLD));

                updateState(SurePetcareConstants.DEVICE_CHANNEL_ONLINE, OnOffType.from(device.getStatus().online));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_DEVICE_RSSI,
                        new DecimalType(device.getStatus().signal.deviceRssi));
                updateState(SurePetcareConstants.DEVICE_CHANNEL_HUB_RSSI,
                        new DecimalType(device.getStatus().signal.hubRssi));
            } else {
                logger.warn("Unknown product type for device {}", thing.getUID().getAsString());
            }
            logger.debug("Updating all thing channels for device : {}", device.toString());
            updateState(SurePetcareConstants.DEVICE_CHANNEL_NAME, new StringType(device.getName()));
        }
    }

}
