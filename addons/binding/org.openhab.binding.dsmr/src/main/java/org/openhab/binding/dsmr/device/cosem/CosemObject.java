/**
  * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.device.cosem;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for Cosem Object implementation
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class CosemObject {
    // logger
    private static final Logger logger = LoggerFactory.getLogger(CosemObject.class);

    // Identifier of the first power failure date element
    public static final int FIRST_POWER_FAILURE_DATE = 2;
    // Identifier of the first power failure duration element
    public static final int FIRST_POWER_FAILURE_DURATION = 3;

    // Regular expression for finding CosemValues
    private static final Pattern cosemValuesPattern = Pattern.compile("(\\(([^\\(\\)]*)\\))");

    // CosemObject yype
    private final CosemObjectType type;

    // The actual OBISIdentifier for this CosemObject
    private final OBISIdentifier obisIdentifier;

    // List of COSEM value in this message
    private List<CosemValue<? extends Object>> cosemValues;

    /**
     * Construct a new CosemObject with the specified OBIS Message Type
     *
     * @param msgType
     *            {@link CosemObjectType}
     */
    public CosemObject(CosemObjectType msgType, OBISIdentifier obisIdentifier) {
        this.type = msgType;
        this.obisIdentifier = obisIdentifier;

        cosemValues = new ArrayList<CosemValue<? extends Object>>();
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
    public List<? extends CosemValue<? extends Object>> getCosemValues() {
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
        logger.debug("Parsing CosemValue string {}", cosemValueString);

        Matcher cosemValueMatcher = cosemValuesPattern.matcher(cosemValueString);

        int nrOfCosemValues = 0;
        while (cosemValueMatcher.find()) {
            nrOfCosemValues++;
        }
        // We need the matcher again, reset to initial state
        cosemValueMatcher.reset();

        if (type.supportsNrOfValues(nrOfCosemValues)) {
            logger.debug("Received items: {} is supported", nrOfCosemValues);

            int cosemValueItr = 0;
            while (cosemValueMatcher.find()) {
                String cosemStringValue = cosemValueMatcher.group(2);

                CosemValue<? extends Object> cosemValue = parseCosemValue(type.getDescriptor(cosemValueItr),
                        cosemStringValue);

                if (cosemValue != null) {
                    cosemValues.add(cosemValue);
                } else {
                    logger.error("Failed to parse: {} for OBISMsgType: {}", cosemStringValue, type);
                }
                cosemValueItr++;
            }
        } else {
            if (type.repeatingDescriptors.size() > 0) {
                throw new ParseException(
                        "Received items:" + nrOfCosemValues + ", Needed items:" + type.descriptors.size(), 0);
            } else {
                throw new ParseException(
                        "Received items:" + nrOfCosemValues + ", Needed items:" + (type.descriptors.size() - 1)
                                + ", Required also a list with a multiple of: " + type.repeatingDescriptors.size(),
                        0);
            }
        }

        /*
         * Here we do a post processing on the values
         */
        switch (type) {
            case EMETER_POWER_FAILURE_LOG:
                postProcessKaifaE0003();
                break;
            default:
                break;
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

    /**
     * On the Kaifa E0003 we have seen power failure entries that occurred at
     * 1-1-1970 and have a 2^32 - 1 duration
     *
     * This method filters the values belonging to this entry
     */
    private void postProcessKaifaE0003() {
        logger.debug("postProcessKaifaE0003");

        /*
         * The list of cosemValues for this Cosem Object is:
         * - [0] Number of entries in the list
         * - [1] Cosem Identifier
         * - [2] power failure date entry 1 [Optional]
         * - [3] power failure duration entry 1 [Optional]
         * - [entry n * 2] power failure date entry n
         * - [entry n * 2 + 1] power failure duration entry n
         */

        // First check of there is at least one entry of a power failure present
        // (i.e. date at idx 2 and duration at idx 3)
        if (cosemValues.size() > FIRST_POWER_FAILURE_DURATION) {
            CosemDate powerFailureDate = (CosemDate) cosemValues.get(FIRST_POWER_FAILURE_DATE);
            CosemInteger powerFailureDuration = (CosemInteger) cosemValues.get(FIRST_POWER_FAILURE_DURATION);

            Calendar epoch = Calendar.getInstance();
            epoch.setTime(new Date(0));

            // Check if the first entry it as epoc and has a 2^32-1 value
            // If so, filter this value, since it has no added value
            if (powerFailureDate.getOpenHABValue().getCalendar().before(epoch)
                    && powerFailureDuration.getValue().intValue() == Integer.MAX_VALUE) {
                logger.debug("Filter invalid power failure entry");
                cosemValues.remove(FIRST_POWER_FAILURE_DURATION);
                cosemValues.remove(FIRST_POWER_FAILURE_DATE);
            }
        }
    }
}
