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
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice.ProductType;
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
            updateState("id", new DecimalType(device.getId()));
            updateState("name", new StringType(device.getName()));
            updateState("product", new StringType(ProductType.findByTypeId(device.getProductId()).getName()));
            if (thing.getThingTypeUID().equals(SurePetcareConstants.THING_TYPE_HUB_DEVICE)) {
                updateState("ledMode", new DecimalType(device.getStatus().ledMode));
                updateState("pairingMode", new DecimalType(device.getStatus().pairingMode));
                updateState("hardwareVersion", new StringType(device.getStatus().version.device.hardware));
                updateState("firmwareVersion", new StringType(device.getStatus().version.device.firmware));
                updateState("online", OnOffType.from(device.getStatus().online));
            } else if (thing.getThingTypeUID().equals(SurePetcareConstants.THING_TYPE_FLAP_DEVICE)) {
                int numCurfews = device.getControl().curfew.size();
                for (int i = 0; (i < 4) && (i < numCurfews); i++) {
                    Curfew curfew = device.getControl().curfew.get(i);
                    updateState("curfewEnabled" + (i + 1), OnOffType.from(device.getStatus().online));
                    updateState("curfewLockTime" + (i + 1), new StringType(curfew.lockTime));
                    updateState("curfewUnlockTime" + (i + 1), new StringType(curfew.unlockTime));
                }
                updateState("lockingMode", new DecimalType(device.getStatus().locking.mode));
                updateState("hardwareVersion", new StringType(device.getStatus().version.device.hardware));
                updateState("firmwareVersion", new StringType(device.getStatus().version.device.firmware));

                float batVol = device.getStatus().battery;
                updateState("batteryVoltage", new DecimalType(batVol));
                updateState("batteryLevel", new DecimalType(Math.min(batVol / BATTERY_FULL_VOLTAGE * 100.0f, 100.0f)));
                updateState("lowBattery", OnOffType.from(batVol < LOW_BATTERY_THRESHOLD));

                updateState("online", OnOffType.from(device.getStatus().online));
                updateState("deviceRSSI", new DecimalType(device.getStatus().signal.deviceRssi));
                updateState("hubRSSI", new DecimalType(device.getStatus().signal.hubRssi));
            } else {
                logger.warn("Unknown product type for device {}", thing.getUID().getAsString());
            }
            logger.debug("updating all thing channels for device : {}", device.toString());
            updateState("name", new StringType(device.getName()));
        }
    }

}
