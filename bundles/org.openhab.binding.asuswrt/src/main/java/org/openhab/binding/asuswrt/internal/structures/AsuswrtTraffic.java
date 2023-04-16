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

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils;
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
    private Date zeroHourDate = new Date();
    private Long lastUpdate = 0L;
    private String representationProperty = "";

    /**
     * INIT CLASS
     */
    public AsuswrtTraffic() {
    }

    public AsuswrtTraffic(String representationProperty) {
        this.representationProperty = representationProperty.toLowerCase();
    }

    /**
     * 
     * INIT CLASS
     * 
     * @param jsonObject jsonObject data is stored
     * @param representationProperty representationProperty of DEvice (i.E. interfaceName)
     */
    public AsuswrtTraffic(JsonObject jsonObject, String representationProperty) {
        this.representationProperty = representationProperty;
        setData(jsonObject);
    }

    /**
     * SetData from jsonObject
     */
    public void setData(JsonObject jsonObject) {
        Integer intRX;
        Integer intTX;
        if (representationProperty.startsWith(INTERFACE_LAN)) {
            intRX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_RX);
            intTX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_TX);
            this.curRX = calculateCurrentTraffic(intRX, this.totalRX);
            this.curTX = calculateCurrentTraffic(intTX, this.totalTX);
            this.totalRX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_RX);
            this.totalTX = getTrafficFromHex(jsonObject, JSON_MEMBER_LAN_TX);
        } else if (representationProperty.startsWith(INTERFACE_WAN)) {
            intRX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_RX);
            intTX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_TX);
            this.curRX = calculateCurrentTraffic(intRX, this.totalRX);
            this.curTX = calculateCurrentTraffic(intTX, this.totalTX);
            this.totalRX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_RX);
            this.totalTX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_TX);
        } else if (representationProperty.startsWith(INTERFACE_WLAN)) {
            for (int i = 0; i < 1; i++) {
                intRX = getTrafficFromHex(jsonObject, JSON_MEMBER_WLAN_RX.replace("{}", Integer.toString(i)));
                intTX = getTrafficFromHex(jsonObject, JSON_MEMBER_WLAN_TX.replace("{}", Integer.toString(i)));
                this.curRX = calculateCurrentTraffic(intRX, this.totalRX);
                this.curTX = calculateCurrentTraffic(intTX, this.totalTX);
                this.totalRX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_RX);
                this.totalTX = getTrafficFromHex(jsonObject, JSON_MEMBER_INET_TX);
            }
        } else if (representationProperty.equals(INTERFACE_CLIENT)) {
            this.curRX = Double.valueOf(jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RXCUR, -1));
            this.curTX = Double.valueOf(jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_TXCUR, -1));
            this.totalRX = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_RXTOTAL, -1);
            this.totalTX = jsonObjectToInt(jsonObject, JSON_MEMBER_CLIENT_TXTOTAL, -1);
        } else {
            logger.trace("({}) can't set Trafficdata", representationProperty);
        }
        this.lastUpdate = System.currentTimeMillis();
        setZeroHourTraffic(this.totalRX, this.totalTX);
    }

    /**
     * Set Zero Hour Traffic.
     * Save traffic value at new day
     */
    private void setZeroHourTraffic(Integer totalRX, Integer totalTX) {
        Date now = new Date();
        if (!AsuswrtUtils.isSameDay(now, this.zeroHourDate) || this.zeroHourRX > totalRX) {
            this.zeroHourRX = totalRX;
            this.zeroHourTX = totalTX;
            this.zeroHourDate = now;
        }
    }

    /**
     * Get value from HEXvalue
     * 
     * @param jsonObject jsonObejct has values
     * @param jsonMember name of jsonMember value is stored
     * @return (Long) traffic
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
     * Calculate Traffic from Total Traffic and Time
     * 
     * @param actVal actual Value to calculate
     * @param oldVal old Value to calculate
     * @return (Double) current traffic
     */
    private Double calculateCurrentTraffic(Integer actVal, Integer oldVal) {
        if (lastUpdate > 0) {
            Long timeSpan = (System.currentTimeMillis() - this.lastUpdate) / 1000;
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

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

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
        return totalRX - this.zeroHourRX;
    }

    public Integer getTodayTX() {
        return totalTX - this.zeroHourTX;
    }
}
