/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterKind;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DSMRMeterDetector class contains the logic to auto discover DSMR Meters
 * from a list of CosemObjects
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRMeterDetector {
    private final Logger logger = LoggerFactory.getLogger(DSMRMeterDetector.class);

    /**
     * Returns a list of {@link DSMRMeterDescriptor} that can handle the supplied list of CosemObjects.
     *
     * If no meters are detected an empty list is returned.
     *
     * @param cosemObjects the List of CosemObject to search meters for
     * @return list of detected {@link DSMRMeterDescriptor}
     */
    public List<DSMRMeterDescriptor> detectMeters(List<CosemObject> messages) {
        Map<DSMRMeterKind, DSMRMeterDescriptor> detectedMeters = new HashMap<>();
        Map<CosemObjectType, CosemObject> availableCosemObjects = new HashMap<>();

        // Fill hashmap for fast comparing the set of received Cosem objects to the required set of Cosem Objects
        for (CosemObject msg : messages) {
            availableCosemObjects.put(msg.getType(), msg);
        }

        // Find compatible meters
        for (DSMRMeterType meterType : DSMRMeterType.values()) {
            logger.debug("Trying if meter type {} is compatible", meterType);
            DSMRMeterDescriptor meterDescriptor = meterType.isCompatible(availableCosemObjects);

            if (meterDescriptor != null) {
                logger.debug("Meter type {} is compatible", meterType);

                DSMRMeterDescriptor prevDetectedMeter = detectedMeters.get(meterType.meterKind);

                if (prevDetectedMeter == null // First meter of this kind, add it
                        || (prevDetectedMeter != null
                                && prevDetectedMeter.getChannel().equals(meterDescriptor.getChannel())
                                && meterType.requiredCosemObjects.length > prevDetectedMeter
                                        .getMeterType().requiredCosemObjects.length)) {
                    logger.debug("New compatible meter descriptor {}", meterDescriptor);
                    detectedMeters.put(meterType.meterKind, meterDescriptor);
                }
            } else {
                logger.debug("Meter type {} is not compatible", meterType);
            }
        }
        return new LinkedList<DSMRMeterDescriptor>(detectedMeters.values());
    }
}