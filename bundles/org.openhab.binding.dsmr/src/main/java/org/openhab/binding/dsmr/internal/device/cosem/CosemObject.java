/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for Cosem Object implementation
 *
 * @author M. Volaart - Initial contribution
 */
@NonNullByDefault
public class CosemObject {

    /**
     * Regular expression for finding CosemValues
     */
    private static final Pattern COSEM_VALUES_PATTERN = Pattern.compile("(\\(([^\\(\\)]*)\\))");

    private final Logger logger = LoggerFactory.getLogger(CosemObject.class);

    /**
     * CosemObject type
     */
    private final CosemObjectType type;

    /**
     * The actual OBISIdentifier for this CosemObject
     */
    private final OBISIdentifier obisIdentifier;

    /**
     * List of COSEM value in this message
     */
    private Map<String, State> cosemValues;

    /**
     * Construct a new CosemObject with the specified OBIS Message Type
     *
     * @param msgType
     *            {@link CosemObjectType}
     */
    public CosemObject(CosemObjectType msgType, OBISIdentifier obisIdentifier) {
        this.type = msgType;
        this.obisIdentifier = obisIdentifier;

        cosemValues = new HashMap<>();
    }

    /**
     * Return the {@link CosemObjectType} for this Cosem Object
     *
     * @return the {@link CosemObjectType} for this Cosem Object
     */
    public CosemObjectType getType() {
        return type;
    }

    /**
     * @return the obisIdentifier
     */
    public OBISIdentifier getObisIdentifier() {
        return obisIdentifier;
    }

    /**
     * Returns string representation of this Cosem Object
     *
     * @return string representation of this Cosem Object
     */
    @Override
    public String toString() {
        return "Cosem Object(type:" + type + ", cosemValues:" + cosemValues + ")";
    }

    /**
     * Returns the Cosem values that are part of this Cosem Object
     *
     * @return Map of channel keys with state values that are part of this Cosem Object
     */
    public Map<String, ? extends State> getCosemValues() {
        return cosemValues;
    }

    /**
     * Parses the List of COSEM String value to COSEM objects values.
     * <p>
     * When the parser has problems it throws a {@link ParseException}. The
     * already parsed values will still be available. It is up to the caller how
     * to handle a partially parsed message.
     *
     * @param cosemValueString the List of COSEM String values
     * @throws ParseException if parsing fails
     */
    public void parseCosemValues(String cosemValueString) throws ParseException {
        logger.trace("Parsing CosemValue string {}", cosemValueString);

        Matcher cosemValueMatcher = COSEM_VALUES_PATTERN.matcher(cosemValueString);
        int nrOfCosemValues = countCosemValues(cosemValueMatcher);

        if (type.supportsNrOfValues(nrOfCosemValues)) {
            logger.trace("Received items: {} is supported", nrOfCosemValues);

            int cosemValueItr = 0;
            while (cosemValueMatcher.find()) {
                final Entry<String, CosemValueDescriptor<?>> valueDescriptorEntry = type.getDescriptor(cosemValueItr);
                final State cosemValue = valueDescriptorEntry.getValue().getStateValue(cosemValueMatcher.group(2));

                if (!cosemValues.containsKey(valueDescriptorEntry.getKey())) {
                    cosemValues.put(valueDescriptorEntry.getKey(), cosemValue);
                } else {
                    logger.warn("Value for descriptor {} already exists, dropping value {}", valueDescriptorEntry,
                            cosemValue);
                }
                cosemValueItr++;
            }
        } else {
            throw new ParseException(type + " does not support " + nrOfCosemValues + " items", 0);
        }
    }

    private int countCosemValues(Matcher cosemValueMatcher) {
        int nrOfCosemValues = 0;

        while (cosemValueMatcher.find()) {
            nrOfCosemValues++;
        }
        // We need the matcher again, reset to initial state
        cosemValueMatcher.reset();
        return nrOfCosemValues;
    }
}
