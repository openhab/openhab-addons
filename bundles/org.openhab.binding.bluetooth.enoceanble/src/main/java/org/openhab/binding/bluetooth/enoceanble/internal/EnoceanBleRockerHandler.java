/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.enoceanble.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnoceanBleRockerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class EnoceanBleRockerHandler extends BeaconBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(EnoceanBleRockerHandler.class);
    private int lastSequence = Integer.MIN_VALUE;

    public EnoceanBleRockerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        super.onScanRecordReceived(scanNotification);
        final byte[] manufacturerData = scanNotification.getManufacturerData();
        logger.debug("Received new scan notification for {}: {}", device.getAddress(), manufacturerData);
        try {
            if (manufacturerData != null && manufacturerData.length > 0) {
                EnoceanBlePtm215Event event = new EnoceanBlePtm215Event(manufacturerData);
                logger.debug("Parsed manufacturer data to PTM215B event: {}", event);
                synchronized (this) {
                    if (event.getSequence() > lastSequence) {
                        lastSequence = event.getSequence();
                        triggerChannel(resolveChannel(event), resolveTriggerEvent(event));
                    }
                }
            }
        } catch (IllegalStateException e) {
            logger.warn("PTM215B event could not be parsed correctly, exception occured:", e);
        }
    }

    protected String resolveChannel(EnoceanBlePtm215Event event) {
        if (event.isButton1()) {
            return EnoceanBleBindingConstants.CHANNEL_ID_ROCKER1;
        }
        if (event.isButton2()) {
            return EnoceanBleBindingConstants.CHANNEL_ID_ROCKER2;
        }
        throw new IllegalStateException(
                "PTM215B event cannot be resolved correctly to a channel, probably received message is invalid");
    }

    protected String resolveTriggerEvent(EnoceanBlePtm215Event event) {
        if (event.isDir1()) {
            if (event.isPressed()) {
                return CommonTriggerEvents.DIR1_PRESSED;
            } else {
                return CommonTriggerEvents.DIR1_RELEASED;
            }
        }
        if (event.isDir2()) {
            if (event.isPressed()) {
                return CommonTriggerEvents.DIR2_PRESSED;
            } else {
                return CommonTriggerEvents.DIR2_RELEASED;
            }
        }
        throw new IllegalStateException(
                "PTM215B event cannot be resolved correctly to an openHAB event, probably received message is invalid");
    }
}
