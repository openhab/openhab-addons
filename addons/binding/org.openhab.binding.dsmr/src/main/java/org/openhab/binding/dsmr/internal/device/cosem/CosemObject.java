/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
  * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.cosem;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for Cosem Object implementation
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class CosemObject {
    // logger
    private final Logger logger = LoggerFactory.getLogger(CosemObject.class);

    // Regular expression for finding CosemValues
    private static final Pattern cosemValuesPattern = Pattern.compile("(\\(([^\\(\\)]*)\\))");

    // CosemObject yype
    private final CosemObjectType type;

    // The actual OBISIdentifier for this CosemObject
    private final OBISIdentifier obisIdentifier;

    // List of COSEM value in this message
    private Map<String, CosemValue<? extends Object>> cosemValues;

    /**
     * Construct a new CosemObject with the specified OBIS Message Type
     *
     * @param msgType
     *            {@link CosemObjectType}
     */
    public CosemObject(CosemObjectType msgType, OBISIdentifier obisIdentifier) {
        this.type = msgType;
        this.obisIdentifier = obisIdentifier;

        cosemValues = new HashMap<String, CosemValue<? extends Object>>();
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
        return "Cosem Object(type:" + type.toString() + ", cosemValues:" + cosemValues + ")";
    }

    /**
     * Returns the Cosem values that are part of this Cosem Object
     *
     * @return List of {@link CosemValue} that are part of this Cosem Object
     */
    public Map<String, ? extends CosemValue<? extends Object>> getCosemValues() {
        return cosemValues;
    }

    /**
     * Returns the specified Cosem value from the available Cosem values
     *
     * Returns null if the requested index is not available
     *
     * @param index the index of the CosemValue to get
     *
     * @return {@link CosemValue} or null if not exist
     */
    public CosemValue<? extends Object> getCosemValue(int index) {
        if (index >= 0 && index < cosemValues.size()) {
            return cosemValues.get(index);
        }
        return null;
    }

    /**
     * Parses the List of COSEM String value to internal openHAB values.
     * <p>
     * When the parser has problems it throws an {@link ParseException}. The
     * already parsed values will still be available. It is up to the caller how
     * to handle a partially parsed message.
     *
     * @param cosemStringValues
     *            the List of COSEM String values
     * @throws ParseException
     *             if parsing fails
     */
    public void parseCosemValues(String cosemValueString) throws ParseException {
        logger.trace("Parsing CosemValue string {}", cosemValueString);

        Matcher cosemValueMatcher = cosemValuesPattern.matcher(cosemValueString);

        int nrOfCosemValues = 0;
        while (cosemValueMatcher.find()) {
            nrOfCosemValues++;
        }
        // We need the matcher again, reset to initial state
        cosemValueMatcher.reset();

        if (type.supportsNrOfValues(nrOfCosemValues)) {
            logger.trace("Received items: {} is supported", nrOfCosemValues);

            int cosemValueItr = 0;
            while (cosemValueMatcher.find()) {
                String cosemStringValue = cosemValueMatcher.group(2);
                CosemValueDescriptor valueDescriptor = type.getDescriptor(cosemValueItr);

                CosemValue<? extends Object> cosemValue = parseCosemValue(valueDescriptor, cosemStringValue);

                if (cosemValue != null) {
                    if (!cosemValues.containsKey(valueDescriptor.getChannelId())) {
                        cosemValues.put(valueDescriptor.getChannelId(), cosemValue);
                    } else {
                        logger.warn("Value for descriptor {} already exists, dropping value {}", valueDescriptor,
                                cosemValue);
                    }
                } else {
                    logger.warn("Failed to parse: {} for OBISMsgType: {}", cosemStringValue, type);
                }
                cosemValueItr++;
            }
        } else {
            throw new ParseException(type + " does not support " + nrOfCosemValues + " items", 0);
        }
    }

    /**
     * Creates an empty CosemValue object
     *
     * @param cosemValueDescriptor
     *            the CosemValueDescriptor object that describes the CosemValue
     * @return the instantiated CosemValue based on the specified
     *         CosemValueDescriptor
     * @throws ParseException if a CosemValue could not be created
     */
    private CosemValue<? extends Object> parseCosemValue(CosemValueDescriptor cosemValueDescriptor,
            String cosemValueString) throws ParseException {
        Class<? extends CosemValue<? extends Object>> cosemValueClass = cosemValueDescriptor.getCosemValueClass();

        String unit = cosemValueDescriptor.getUnit();

        try {
            Constructor<? extends CosemValue<? extends Object>> c = cosemValueClass.getConstructor(String.class);

            CosemValue<? extends Object> cosemValue = c.newInstance(unit);
            cosemValue.setValue(cosemValueString);

            return cosemValue;
        } catch (ReflectiveOperationException roe) {
            logger.error("Failed to create {} message", type.obisId, roe);
        }
        return null;
    }
}
