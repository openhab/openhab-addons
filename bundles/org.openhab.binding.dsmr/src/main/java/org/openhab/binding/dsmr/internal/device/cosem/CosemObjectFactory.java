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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for constructing Cosem Objects from Strings
 *
 * @author M. Volaart - Initial contribution
 */
@NonNullByDefault
public class CosemObjectFactory {
    private final Logger logger = LoggerFactory.getLogger(CosemObjectFactory.class);

    /**
     * Lookup cache for fixed OBIS Identifiers
     */
    private final Map<OBISIdentifier, CosemObjectType> obisLookupTableFixed = new HashMap<>();

    /**
     * Lookup cache for fixed OBIS Identifiers that has the same id for different data types
     */
    private final Map<OBISIdentifier, List<CosemObjectType>> obisLookupTableMultipleFixed = new HashMap<>();

    /**
     * Creates a new CosemObjectFactory
     */
    public CosemObjectFactory() {
        /*
         * Fill lookup tables. There are 3 entities:
         * - obisLookupTableFixed. This lookup table contains all CosemObjectType with a fixed OBISIdentifier
         * (i.e. groupA != null && groupB != null && groupC != null).
         * - obisLookupTableDynamic. This lookup table contains all CosemObjectType with a wildcard OBISIdentifier
         * (i.e. groupA == null || groupB == null || groupC == null). This lookuptable will be filled
         * dynamically with unique wildcard OBISIdentifiers when values are received and matches a particular real
         * device (if the device is changed, this lookupTable must be cleared by removing the corresponding DSMRDevice
         * Thing from the configuration.)
         * - obisWildCardCosemTypeList. This is the list of all wild card Cosem Object types. Multiple Cosem Object
         * Types can have the same wild card OBISIdentifer.
         *
         * To facilitate autodiscovery the list has all supported CosemObjectTypes. To improve performance once the
         * correct OBISIdentifier is discovered for a certain OBISMsgType this is added to the obisLookupTableDynamic.
         */
        for (CosemObjectType msgType : CosemObjectType.values()) {
            if (msgType.obisId.isConflict()) {
                obisLookupTableMultipleFixed.computeIfAbsent(msgType.obisId, r -> new ArrayList<>()).add(msgType);
            } else {
                obisLookupTableFixed.put(msgType.obisId, msgType);
            }
        }
    }

    /**
     * Return Cosem Object from specified string or null if string couldn't be
     * parsed correctly or no corresponding Cosem Object was found
     *
     * @param obisIdString String containing the OBIS message identifier
     * @param cosemStringValues String containing Cosem values
     * @return CosemObject or null if parsing failed
     */
    public @Nullable CosemObject getCosemObject(String obisIdString, String cosemStringValues) {
        OBISIdentifier obisId;
        OBISIdentifier reducedObisId;
        OBISIdentifier reducedObisIdGroupE;

        try {
            obisId = new OBISIdentifier(obisIdString);
            reducedObisId = obisId.getReducedOBISIdentifier();
            reducedObisIdGroupE = obisId.getReducedOBISIdentifierGroupE();
        } catch (final ParseException pe) {
            logger.debug("Received invalid OBIS identifier: {}", obisIdString);
            return null;
        }

        logger.trace("Received obisIdString {}, obisId: {}, values: {}", obisIdString, obisId, cosemStringValues);

        CosemObjectType objectType = obisLookupTableFixed.get(reducedObisId);
        if (objectType != null) {
            logger.trace("Found obisId {} in the fixed lookup table", reducedObisId);
            return getCosemObjectInternal(objectType, obisId, cosemStringValues);
        }

        List<CosemObjectType> objectTypeList = obisLookupTableMultipleFixed.get(reducedObisId);
        if (objectTypeList != null) {
            for (CosemObjectType cosemObjectType : objectTypeList) {
                CosemObject cosemObject = getCosemObjectInternal(cosemObjectType, obisId, cosemStringValues);
                if (cosemObject != null) {
                    logger.trace("Found obisId {} in the fixed lookup table", reducedObisId);
                    return cosemObject;
                }
            }
        }

        objectType = obisLookupTableFixed.get(reducedObisIdGroupE);
        if (objectType != null) {
            return getCosemObjectInternal(objectType, obisId, cosemStringValues);
        }

        logger.debug("Received unknown Cosem Object(OBIS id: {})", obisId);

        return null;
    }

    /**
     * Constructs a CosemObject from the given type, OBISIdentifier and the values
     *
     * @param cosemObjectType the type of the CosemObject
     * @param obisIdentifier the actual OBISIdentifier how this cosemObjectType is identified
     * @param cosemStringValues the values of the CosemObject
     *
     * @return a CosemObject or null if parsing failed
     */
    private @Nullable CosemObject getCosemObjectInternal(CosemObjectType cosemObjectType, OBISIdentifier obisIdentifier,
            String cosemStringValues) {
        CosemObject obj = new CosemObject(cosemObjectType, obisIdentifier);

        try {
            logger.trace("Parse values for Cosem Object type: {}", cosemObjectType);
            obj.parseCosemValues(cosemStringValues);

            return obj;
        } catch (ParseException pe) {
            logger.trace("Failed to construct Cosem Object for type {}, values: {}", cosemObjectType, cosemStringValues,
                    pe);
        }
        return null;
    }
}
