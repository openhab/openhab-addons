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
package org.openhab.binding.dsmr.internal.discovery;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterKind;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRMeterDetector} class contains the logic to discover DSMR Meters from a list of CosemObjects.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
class DSMRMeterDetector {
    private final Logger logger = LoggerFactory.getLogger(DSMRMeterDetector.class);

    /**
     * Returns a collection of {@link DSMRMeterDescriptor} that can handle the supplied list of CosemObjects.
     *
     * If no meters are detected an empty collection is returned.
     *
     * @param telegram The received telegram
     * @return collection of detected {@link DSMRMeterDescriptor}
     */
    public Entry<Collection<DSMRMeterDescriptor>, Map<CosemObjectType, CosemObject>> detectMeters(P1Telegram telegram) {
        final Map<DSMRMeterKind, DSMRMeterDescriptor> detectedMeters = new HashMap<>();
        final Map<CosemObjectType, CosemObject> availableCosemObjects = new HashMap<>();
        final Map<CosemObjectType, CosemObject> undetectedCosemObjects = new HashMap<>();

        // Fill hashmap for fast comparing the set of received Cosem objects to the required set of Cosem Objects
        telegram.getCosemObjects().forEach(msg -> availableCosemObjects.put(msg.getType(), msg));
        undetectedCosemObjects.putAll(availableCosemObjects);

        // Find compatible meters
        for (DSMRMeterType meterType : DSMRMeterType.values()) {
            logger.trace("Trying if meter type {} is compatible", meterType);
            final DSMRMeterDescriptor meterDescriptor = meterType.isCompatible(availableCosemObjects);

            if (meterDescriptor == null) {
                logger.trace("Meter type {} is not compatible", meterType);
            } else {
                logger.debug("Meter type {} is compatible", meterType);

                final DSMRMeterDescriptor prevDetectedMeter = detectedMeters.get(meterType.meterKind);

                if (prevDetectedMeter == null // First meter of this kind, add it
                        || (prevDetectedMeter.getChannel() == meterDescriptor.getChannel())
                                && meterType.requiredCosemObjects.length > prevDetectedMeter
                                        .getMeterType().requiredCosemObjects.length) {
                    logger.debug("New compatible meter: {}", meterDescriptor);
                    detectedMeters.put(meterType.meterKind, meterDescriptor);
                    for (CosemObjectType cot : meterDescriptor.getMeterType().supportedCosemObjects) {
                        undetectedCosemObjects.remove(cot);
                    }
                }
            }
        }
        logger.trace("Telegram as received from the device:\n{}\n", telegram.getRawTelegram());
        return new SimpleEntry<Collection<DSMRMeterDescriptor>, Map<CosemObjectType, CosemObject>>(
                detectedMeters.values(), undetectedCosemObjects);
    }
}
