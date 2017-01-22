/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.device.cosem;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for constructing Cosem Objects from Strings
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class CosemObjectFactory {
    /* logger */
    private static final Logger logger = LoggerFactory.getLogger(CosemObjectFactory.class);

    /* internal lookup cache */
    private final HashMap<OBISIdentifier, CosemObjectType> obisLookupTableFixed;
    private final HashMap<OBISIdentifier, CosemObjectType> obisLookupTableDynamic;
    private final List<CosemObjectType> obisWildcardCosemTypeList;

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
         * Thing from the configuration.
         * - obisWildCardCosemTypeList. This is the list of all wil card Cosem Object types. Multiple Cosem Object Types
         * can have the same wild card OBISIdentifer.
         *
         * To facilitate autodiscovery the list has all supported CosemObjectTypes. To improve performance once the
         * correct OBISIdentifier is discovered for a certain OBISMsgType this is added to the obisLookupTableDynamic.
         */
        obisLookupTableFixed = new HashMap<OBISIdentifier, CosemObjectType>();
        obisLookupTableDynamic = new HashMap<OBISIdentifier, CosemObjectType>();
        obisWildcardCosemTypeList = new LinkedList<CosemObjectType>();

        for (CosemObjectType msgType : CosemObjectType.values()) {
            if (!msgType.obisId.reducedOBISIdentifierIsWildCard()) {
                obisLookupTableFixed.put(msgType.obisId, msgType);
            } else {
                obisWildcardCosemTypeList.add(msgType);
            }
        }
    }

    /**
     * Return Cosem Object from specified string or null if string couldn't be
     * parsed correctly or no corresponding Cosem Object was found
     *
     * @param obisIdString
     *            String containing the OBIS message identifier
     * @param cosemStringValues
     *            String containing Cosem values
     * @return CosemObject or null if parsing failed
     */
    public CosemObject getCosemObject(String obisIdString, String cosemStringValues) {
        OBISIdentifier obisId = null;
        OBISIdentifier reducedObisId = null;

        try {
            obisId = new OBISIdentifier(obisIdString);
            reducedObisId = obisId.getReducedOBISIdentifier();
        } catch (ParseException pe) {
            logger.error("Received invalid OBIS identifier: {}", obisIdString);

            return null;
        }

        logger.debug("Received obisIdString {}, obisId: {}, values: {}", obisIdString, obisId, cosemStringValues);

        CosemObject cosemObject = null;

        if (obisLookupTableFixed.containsKey(reducedObisId)) {
            cosemObject = getCosemObjectInternal(obisLookupTableFixed.get(reducedObisId), obisId, cosemStringValues);
        } else if (obisLookupTableDynamic.containsKey(reducedObisId)) {
            logger.debug("Find reducedObisId {} in the dynamic lookup table", reducedObisId);
            cosemObject = getCosemObjectInternal(obisLookupTableDynamic.get(reducedObisId), obisId, cosemStringValues);
        } else {
            for (CosemObjectType obisMsgType : obisWildcardCosemTypeList) {
                if (obisMsgType.obisId.equalsWildCard(reducedObisId)) {
                    cosemObject = getCosemObjectInternal(obisMsgType, obisId, cosemStringValues);
                    logger.debug("Search reducedObisId {} in the wild card type list, result: {}", reducedObisId,
                            cosemObject);
                    if (cosemObject != null) {
                        obisLookupTableDynamic.put(reducedObisId, obisMsgType);
                        break;
                    }
                }
            }
        }

        if (cosemObject == null) {
            logger.warn("Received unknown Cosem Object(OBIS id: {})", obisId);
        }

        return cosemObject;
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
    private CosemObject getCosemObjectInternal(CosemObjectType cosemObjectType, OBISIdentifier obisIdentifier,
            String cosemStringValues) {
        CosemObject obj = new CosemObject(cosemObjectType, obisIdentifier);

        try {
            logger.debug("Parse values for Cosem Object type: {}", cosemObjectType);

            obj.parseCosemValues(cosemStringValues);

            return obj;
        } catch (ParseException pe) {
            logger.debug("Failed to construct Cosem Object for type {}, values: {}", cosemObjectType, cosemStringValues,
                    pe);
        }
        return null;
    }
}
