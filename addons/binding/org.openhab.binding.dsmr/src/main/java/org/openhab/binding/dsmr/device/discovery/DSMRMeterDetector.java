package org.openhab.binding.dsmr.device.discovery;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openhab.binding.dsmr.device.cosem.CosemObject;
import org.openhab.binding.dsmr.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.meter.DSMRMeterKind;
import org.openhab.binding.dsmr.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DSMRMeterDetector class contains the logic to auto discover DSMR Meters
 * from a list of CosemObjects
 *
 * @author Marcel Volaart
 * @since 2.0.0
 */
public class DSMRMeterDetector {
    public static final Logger logger = LoggerFactory.getLogger(DSMRMeterDetector.class);

    /**
     * Returns a list of {@link DSMRMeterDescriptor} that can handle the supplied list of CosemObjects.
     *
     * If no meters are detected an empty list is returned.
     *
     * @param cosemObjects the List of CosemObject to search meters for
     * @return list of detected {@link DSMRMeterDescriptor}
     */
    public static List<DSMRMeterDescriptor> detectMeters(List<CosemObject> messages) {
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
                        || (prevDetectedMeter != null && prevDetectedMeter.getChannel() == meterDescriptor.getChannel()
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