/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.solarview.handler.SolarviewBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combined set of energy information provided by the <B>Solarview</B> bridge.
 *
 * @author Guenther Schreiner - initial contribution.
 */
public class Energy {
    private final Logger logger = LoggerFactory.getLogger(Energy.class);

    // Response message constants
    private static final String SOLARVIEW_RESPONSE_ITEM_DELIM = ",";
    private static final int SOLARVIEW_RESPONSE_EXPECTED_NO_ITEMS = 20;

    /**
     * Enumeration of the common set of energy information items.
     *
     * <p>
     * Designed along the output format of the <b>Solarview</b> server:
     *
     * <pre>
     * { WR, Day, Month, Year, Hour, Minute, KDY, KMT, KYR, KT0, PAC, UDC, IDC, UDCB, IDCB, UDCC, IDCC, UL1, IL1, TKK }
     * </pre>
     *
     * A sample response of the <b>Solarview</b> server could be
     *
     * <pre>
     * {00,07,06,2017,12,26,0008.2,00169,002467,00024890,00121,000,000.0,000,000.0,000,000.0,000,000.0,00},
     * </pre>
     */
    public static enum Channel {
        SOURCE(0),
        DAY(1),
        MONTH(2),
        YEAR(3),
        HOUR(4),
        MINUTE(5),
        KT0(9),
        KYR(8),
        KMT(7),
        KDY(6),
        PAC(10),
        UDC(11),
        IDC(12),
        UDCB(13),
        IDCB(14),
        UDCC(15),
        IDCC(16),
        UL1(17),
        IL1(18),
        TKK(19);

        private final int position;

        Channel(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

    /** Flag about validity of the Energy information. */
    private boolean informationAvailable;
    /** Energy information as an array indexed by {@link Channel} of Strings. */
    private String[] information;

    public Energy() {
    }

    /**
     * Initializes the {@link Energy} class.
     *
     * @param informationString
     *            string containing energy values as returned from the Solarview server via the
     *            {@link SolarviewBridgeHandler}.
     */
    public Energy(String informationString) {
        this.setInformation(informationString);
    }

    /**
     * Stores a set of {@link Energy} information item values.
     *
     * @param informationString
     *            string containing energy values separated by {@link SOLARVIEW_RESPONSE_ITEM_DELIM}.
     */
    public void setInformation(String informationString) {
        logger.trace("setInformation({}) called.", informationString);

        if (informationString != null) {
            informationString = informationString.replaceAll("^.*\\{", "").replaceAll("\\}.*$", "");
            logger.trace("working on string {}.", informationString);
            this.information = (StringUtils.split(informationString, SOLARVIEW_RESPONSE_ITEM_DELIM));
            logger.trace("found {} information items.", this.information.length);
            if (this.information.length == SOLARVIEW_RESPONSE_EXPECTED_NO_ITEMS) {
                logger.trace(
                        "information items marked as available due to the expected number of items has been found.");
                informationAvailable = true;
            }
        } else {
            informationAvailable = false;
        }
    }

    /**
     * Retrieves the value of the desired {@link Channel} of {@link Energy} information.
     *
     * @param item
     *            channel of which value should to be fetched.
     * @return channelValue - as State (but - in fact - it is a DecimalType), or
     *         {@link UnDefType}.UNDEF in case that there is no valid information stored.
     */
    public State getChannelValue(Channel item) {
        logger.trace("getChannelValue({}) called.", item);

        if (!informationAvailable) {
            return UnDefType.UNDEF;
        }
        DecimalType val;
        String value = getNthValue(this.information, item.getPosition());
        logger.trace("getChannelValue() returns {}.", value);
        if (value == null) {
            return UnDefType.UNDEF;
        }
        try {
            val = new DecimalType(value);
        } catch (NumberFormatException nx) {
            return UnDefType.UNDEF;
        }
        return val;
    }

    /**
     * Retrieves the value of the desired {@link Channel} of {@link Energy} information.
     *
     * @param channelString
     *            channel (as String) of which value should to be fetched.
     * @return channelValue - as State (but - in fact - it is a DecimalType), or
     *         {@link UnDefType}.UNDEF in case that there is no valid information stored, or
     *         {@link UnDefType}.NULL in case that there is no constant with the specified name.
     */
    public State getChannelValue(String channelString) {
        logger.trace("getChannelValue(string {}) called.", channelString);

        Channel thisChannel;
        try {
            thisChannel = Channel.valueOf(channelString);
        } catch (IllegalArgumentException e) {
            logger.error("getChannelValue() returns UnDefType.NULL due to unknown channelString {}.", channelString);
            return UnDefType.NULL;
        }
        return getChannelValue(thisChannel);
    }

    /**
     * Returns the (n)th part of information.
     *
     * @param parts
     *            Array of {@link String} elements,
     * @param nth
     *            Number of element to be returned.
     *
     * @return the value of the n-th information element, or, in case of failure, {@link null}.
     */
    private String getNthValue(String[] parts, int nth) {
        if (nth < 0) {
            logger.error("getNthValue() returns null as requested index less than zero.");
            return null;
        } else if (nth > parts.length) {
            logger.error("getNthValue() returns null as requested index greater than number of elements.");
            return null;
        }
        return parts[nth];
    }
}
/**
 * end-of-Energy.java
 */
