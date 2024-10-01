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
package org.openhab.binding.dsmr.internal.meter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.internal.device.cosem.OBISIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSMR Meter represents a meter for this binding.
 *
 * A physical meter is a certain {@link DSMRMeterType} on a M-Bus channel. This is the {@link DSMRMeterDescriptor}
 * and is a private member of the {@link DSMRMeter}.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored class, removed actual handling and moved to handler class
 */
@NonNullByDefault
public class DSMRMeter {
    private final Logger logger = LoggerFactory.getLogger(DSMRMeter.class);

    /**
     * Meter identification.
     */
    private final DSMRMeterDescriptor meterDescriptor;

    /**
     * List of supported message identifiers for this meter
     */
    private List<OBISIdentifier> supportedIdentifiers = new ArrayList<>();

    /**
     * Creates a new DSMRMeter
     *
     * @param meterDescriptor {@link DSMRMeterDescriptor} containing the description of the new meter
     */
    public DSMRMeter(DSMRMeterDescriptor meterDescriptor) {
        this.meterDescriptor = meterDescriptor;

        for (CosemObjectType msgType : meterDescriptor.getMeterType().supportedCosemObjects) {
            OBISIdentifier obisId = msgType.obisId;
            if (msgType.obisId.getChannel() == null) {
                supportedIdentifiers.add(new OBISIdentifier(obisId.getGroupA(), obisId.getGroupC(), obisId.getGroupD(),
                        obisId.getGroupE()));
            } else {
                supportedIdentifiers.add(msgType.obisId);
            }
        }
    }

    /**
     * Returns a list of Cosem Objects this meter will handle and removed them from the passed {@link CosemObject} list.
     *
     * @param cosemObjects list of CosemObject that must be processed and where the objects of this meter are removed
     * @return List of CosemObject that this meter can process
     */
    public List<CosemObject> filterMeterValues(List<CosemObject> cosemObjects, int channel) {
        logger.trace("supported identifiers: {}, searching for objects {}", supportedIdentifiers, cosemObjects);
        return cosemObjects.stream()
                .filter(cosemObject -> (DSMRMeterConstants.UNKNOWN_CHANNEL == channel
                        || cosemObject.getObisIdentifier().getChannel() == channel)
                        && supportedIdentifiers.contains(cosemObject.getObisIdentifier().getReducedOBISIdentifier()))
                .collect(Collectors.toList());
    }

    /**
     * @return Returns the {@link DSMRMeterDescriptor} this object is configured with
     */
    public DSMRMeterDescriptor getMeterDescriptor() {
        return meterDescriptor;
    }

    @Override
    public String toString() {
        return meterDescriptor.toString();
    }
}
