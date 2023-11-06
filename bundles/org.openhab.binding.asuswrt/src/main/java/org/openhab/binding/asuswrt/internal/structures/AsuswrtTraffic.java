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
package org.openhab.binding.asuswrt.internal.structures;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtTraffic} class handles traffic statistics
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtTraffic {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtTraffic.class);
    private Double curTX = 0.0;
    private Double curRX = 0.0;
    private Integer totalTX = 0;
    private Integer totalRX = 0;
    private Integer zeroHourTX = 0;
    private Integer zeroHourRX = 0;
    private LocalDate zeroHourDate = LocalDate.now();
    private Long lastUpdate = 0L;
    private String representationProperty = "";

    public AsuswrtTraffic() {
    }

    /**
     * Constructor.
     *
     * @param representationProperty representationProperty of the device (i.e. interfaceName)
     */
    public AsuswrtTraffic(String representationProperty) {
        this.representationProperty = representationProperty.toLowerCase();
    }

    /**
     * Constructor.
     *
     * @param jsonObject stores the data
     * @param representationProperty representationProperty of the device (i.e. interfaceName)
     */
    public AsuswrtTraffic(JsonObject jsonObject, String representationProperty) {
        this.representationProperty = representationProperty;
        setData(jsonObject);
    }

    public void setData(JsonObject jsonObject) {
        Integer intRX;
        Integer intTX;
        if (representationProperty.startsWith(INTERFACE_LAN)) {
            intRX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_RX);
            intTX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_TX);
            curRX = calculateCurrentTraffic(intRX, totalRX);
            curTX = calculateCurrentTraffic(intTX, totalTX);
            totalRX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_RX);
            totalTX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_TX);
        } else if (representationProperty.startsWith(INTERFACE_WAN)) {
            intRX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_RX);
            intTX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_TX);
            curRX = calculateCurrentTraffic(intRX, totalRX);
            curTX = calculateCurrentTraffic(intTX, totalTX);
            totalRX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_RX);
            totalTX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_TX);
        } else if (representationProperty.startsWith(INTERFACE_WLAN)) {
            for (int i = 0; i < 1; i++) {
                intRX = getTrafficFromHex(jsonObject, JSON_MEMBER_WLAN_RX.replace("{}", Integer.toString(i)));
                intTX = getTrafficFromHex(jsonObject, JSON_MEMBER_WLAN_TX.replace("{}", Integer.toString(i)));
                curRX = calculateCurrentTraffic(intRX, totalRX);
                curTX = calculateCurrentTraffic(intTX, totalTX);
                totalRX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_RX);
                totalTX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_TX);
            }
        } else if (INTERFACE_CLIENT.equals(representationProperty)) {
            curRX = Double.valueOf(jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RXCUR, -1));
            curTX = Double.valueOf(jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_TXCUR, -1));
            totalRX = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RXTOTAL, -1);
            totalTX = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_TXTOTAL, -1);
        } else {
            logger.trace("({}) can't set Trafficdata", representationProperty);
        }
        lastUpdate = System.currentTimeMillis();
        setZeroHourTraffic(totalRX, totalTX);
    }

    /**
     * Saves the traffic values at the start of a new day.
     */
    private void setZeroHourTraffic(Integer totalRX, Integer totalTX) {
        LocalDate today = LocalDate.now();
        if (today.isAfter(zeroHourDate) || zeroHourRX > totalRX) {
            zeroHourRX = totalRX;
            zeroHourTX = totalTX;
            zeroHourDate = today;
        }
    }

    /**
     * Gets the traffic as {@link Integer} value from a hexadecimal value in a {@link JsonObject}.
     *
     * @param jsonObject the object containing the values
     * @param jsonMember the name of the key that stores the value
     * @return the traffic value
     */
    private Integer getTrafficFromHex(JsonObject jsonObject, String jsonMember) {
        Long lngVal;
        if (jsonObject.has(jsonMember)) {
            String hex = jsonObjectToString(jsonObject, jsonMember);
            try {
                lngVal = Long.decode(hex);
                lngVal = lngVal * 8 / 1024 / 1024 / 2;
                return lngVal.intValue();
            } catch (Exception e) {
                logger.debug("({}) error calculating traffic from hex '{}'", representationProperty, hex);
            }
        }
        return -1;
    }

    /**
     * Calculates the traffic from the actual and old total traffic using the time span.
     *
     * @param actVal the actual value
     * @param oldVal the old value
     * @return the current traffic value
     */
    private Double calculateCurrentTraffic(Integer actVal, Integer oldVal) {
        if (lastUpdate > 0) {
            Long timeSpan = (System.currentTimeMillis() - lastUpdate) / 1000;
            Integer div = 0;
            try {
                if (actVal >= 0) {
                    div = actVal - oldVal;
                    return Double.valueOf(div / timeSpan.intValue());
                }
            } catch (Exception e) {
                logger.debug("({}) error calculating traffic from timeSpan '{}/{}'", representationProperty, div,
                        timeSpan);
            }
        }
        return -1.0;
    }

    /*
     * Getters
     */

    public Double getCurrentRX() {
        return curRX;
    }

    public Double getCurrentTX() {
        return curTX;
    }

    public Integer getTotalRX() {
        return totalRX;
    }

    public Integer getTotalTX() {
        return totalTX;
    }

    public Integer getTodayRX() {
        return totalRX - zeroHourRX;
    }

    public Integer getTodayTX() {
        return totalTX - zeroHourTX;
    }
}
