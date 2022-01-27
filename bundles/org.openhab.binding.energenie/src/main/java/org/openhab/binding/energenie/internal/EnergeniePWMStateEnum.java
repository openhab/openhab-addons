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
package org.openhab.binding.energenie.internal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergeniePWMStateEnum} contains informations for parsing the readState() result.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

@NonNullByDefault
public enum EnergeniePWMStateEnum {
    VOLTAGE("var V  = ", 9, 20, 10, Units.VOLT),
    CURRENT("var V  = ", 9, 20, 100, Units.AMPERE),
    POWER("var P=", 6, 20, 466, Units.WATT),
    ENERGY("var E=", 6, 20, 25600, Units.WATT_HOUR);

    private final Logger logger = LoggerFactory.getLogger(EnergeniePWMStateEnum.class);

    private final String stateResponseSearch;
    private final int start;
    private final int stop;
    private final int divisor;
    private final Unit<?> unit;

    private EnergeniePWMStateEnum(final String stateResponseSearch, final int start, final int stop, final int divisor,
            final Unit<?> unit) {
        this.stateResponseSearch = stateResponseSearch;
        this.start = start;
        this.stop = stop;
        this.divisor = divisor;
        this.unit = unit;
    }

    public String getStateResponseSearch() {
        return stateResponseSearch;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public int getDivisor() {
        return divisor;
    }

    public State readState(final String loginResponseString) {
        final int findState = loginResponseString.lastIndexOf(stateResponseSearch);

        if (findState > 0) {
            logger.trace("searchstring {} found at position {}", stateResponseSearch, findState);
            final String slicedResponseTmp = loginResponseString.substring(findState + start, findState + stop);
            logger.trace("transformed state response = {}", slicedResponseTmp);
            final String[] slicedResponse = slicedResponseTmp.split(";");
            logger.trace("transformed state response = {} - {}", slicedResponse[0], slicedResponse[1]);
            final double value;
            try {
                value = Double.parseDouble(slicedResponse[0]) / divisor;
                return QuantityType.valueOf(value, unit);
            } catch (NumberFormatException e) {
                logger.debug("Could not Parse State", e);
                return UnDefType.UNDEF;
            }
        } else {
            logger.trace("searchstring '{} not found", stateResponseSearch);
            return UnDefType.UNDEF;
        }
    }
}
