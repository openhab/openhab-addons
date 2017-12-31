/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.meter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.internal.device.cosem.OBISIdentifier;
import org.openhab.binding.dsmr.internal.discovery.DSMRMeterDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSMR Meter represents a meter for this binding.
 *
 * Since the DSMR specification has evolved over time in combination we would
 * help the user to easily add things by auto discovery DSMR meters
 * {@link DSMRMeterDetector}.
 * To be able to discover meters and handle a correct administration of detected meters
 * the DSMRMeter class has the following helper classes:
 * - {@link DSMRMeterType} describing the meter type following a certain DSMR specification
 * and provides the information which of the {@link CosemObject} that are part of the P1 telegram
 * are supported for this meter
 * - {@link DSMRMeterKind} describing what kind of meter (electricity, gas, etc.) this is. This information
 * is mainly needed for the auto discovery process since a set of {@link CosemObject} could fit multiple meter types
 * (i.e. Electricity for DSMR can have meter type ELECTRICITY_V4 and ELECTRICITY_V404. The meter kind for both types
 * is ELECTRICITY. The auto discovery process knows know both types describe the same kind and can find the most
 * appropriate one.
 * - {@link DSMRMeterIdentification} describing the identification of this meter.
 *
 * A physical meter is a certain {@link DSMRMeterType} on a M-Bus channel. This is the {@link DSMRMeterDescriptor}
 * and is a private member of the DSMRMeter
 *
 * @author M. Volaart - Initial contribution
 */
public class DSMRMeter {
    private final Logger logger = LoggerFactory.getLogger(DSMRMeter.class);

    /**
     * Meter identification
     */
    private final DSMRMeterDescriptor meterDescriptor;

    /**
     * List of supported message identifiers for this meter
     */
    private List<OBISIdentifier> supportedIdentifiers;

    /**
     * Listener of new meter values
     */
    private DSMRMeterListener meterListener;

    /**
     * Creates a new DSMRMeter
     *
     * @param meterDescriptor {@link DSMRMeterDescriptor} containing the description of the new meter
     * @param meterListener {@link DSMRMeterListener} containing the listener for new Cosem Objects
     */
    public DSMRMeter(DSMRMeterDescriptor meterDescriptor, DSMRMeterListener meterListener) {
        if (meterListener == null) {
            throw new IllegalArgumentException("meterListener can not be null");
        }
        this.meterDescriptor = meterDescriptor;
        this.meterListener = meterListener;

        supportedIdentifiers = new LinkedList<>();

        for (CosemObjectType msgType : meterDescriptor.getMeterType().supportedCosemObjects) {
            OBISIdentifier obisId = msgType.obisId;
            if (msgType.obisId.getGroupB() == null) {
                supportedIdentifiers.add(new OBISIdentifier(obisId.getGroupA(), meterDescriptor.getChannel(),
                        obisId.getGroupC(), obisId.getGroupD(), obisId.getGroupE(), msgType.obisId.getGroupF()));
            } else {
                supportedIdentifiers.add(msgType.obisId);
            }
        }
    }

    /**
     * Returns a list of Cosem Objects this meter will handle
     *
     * @param cosemObjects the list of CosemObject that must be processed
     * @return List of CosemObject that this meter can process
     */
    private List<CosemObject> filterMeterValues(List<CosemObject> cosemObjects) {
        logger.trace("supported identifiers: {}, searching for objects {}", supportedIdentifiers, cosemObjects);
        return cosemObjects.stream()
                .filter(cosemObject -> supportedIdentifiers
                        .contains(cosemObject.getObisIdentifier().getReducedOBISIdentifier()))
                .collect(Collectors.toList());
    }

    /**
     * Handles the new CosemObjects
     *
     * This method will process the messages specific for this DSMRMeter
     *
     * @param cosemObjects List of CosemObject to process
     *
     * @return List of OBISMessages that were processed by this DSMRMeter
     */
    public List<CosemObject> handleCosemObjects(List<CosemObject> cosemObjects) {
        List<CosemObject> filteredValues = filterMeterValues(cosemObjects);

        for (CosemObject newValue : filteredValues) {
            logger.trace("Send new meter value {}", newValue);
            meterListener.meterValueReceived(newValue);
        }
        return filteredValues;
    }

    /**
     * Returns the Meter identification {@link DSMRMeterIdentification}
     *
     * @return the identification of this meter
     */
    public DSMRMeterDescriptor getMeterDescriptor() {
        return meterDescriptor;
    }

    /**
     * Returns true if the identification and metertype are equal
     *
     * @return true if the identification and metertype are equal
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DSMRMeter)) {
            return false;
        }
        DSMRMeter o = (DSMRMeter) other;

        return meterDescriptor.equals(o.meterDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meterDescriptor);
    }

    @Override
    public String toString() {
        return meterDescriptor.toString();
    }
}
