/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openhab.binding.netatmo.NetatmoBindingConstants;

/**
 * Java Bean to represent a JSON part of message describing a Netatmo module
 * properties
 *
 * @author Andreas Brenk
 * @author Gaël L'hopital
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetatmoModule extends AbstractDevice {
    // battery_vp, rf_status and wifi_status:
    // http://forum.netatmo.com/viewtopic.php?f=5&t=2290&sid=bb1c0f95abcf3198908829eb89adf1a1

    // Returned threshold values for low, medium, high, full
    private static final int[] RF_STATUS_THRESHOLDS = { 60, 70, 80, 90 };

    /**
     * <code>battery_vp</code> threshold for type NAModule4: full
     */
    private static final int BATTERY_MODULE_1_THRESHOLD_0 = 5500;
    /**
     * <code>battery_vp</code> threshold for type NAModule4: high
     */
    private static final int BATTERY_MODULE_1_THRESHOLD_1 = 5000;
    /**
     * <code>battery_vp</code> threshold for type NAModule4: medium
     */
    private static final int BATTERY_MODULE_1_THRESHOLD_2 = 4500;
    /**
     * <code>battery_vp</code> threshold for type NAModule4: low, otherwise
     * verylow
     */
    private static final int BATTERY_MODULE_1_THRESHOLD_3 = 4000;

    /**
     * <code>battery_vp</code> threshold for type NAModule4: full
     */
    private static final int BATTERY_MODULE_4_THRESHOLD_0 = 5640;
    /**
     * <code>battery_vp</code> threshold for type NAModule4: high
     */
    private static final int BATTERY_MODULE_4_THRESHOLD_1 = 5280;
    /**
     * <code>battery_vp</code> threshold for type NAModule4: medium
     */
    private static final int BATTERY_MODULE_4_THRESHOLD_2 = 4920;
    /**
     * <code>battery_vp</code> threshold for type NAModule4: low, otherwise
     * verylow
     */
    private static final int BATTERY_MODULE_4_THRESHOLD_3 = 4560;

    public String getMainDevice() {
        return main_device;
    }

    public Integer getRfStatus() {
        return rf_status;
    }

    public Integer getBatteryVp() {
        return battery_vp;
    }

    protected String main_device;
    protected Integer rf_status;
    protected Integer battery_vp;
    protected Date last_message;
    protected Date last_seen;

    /*
     * "main_device": "f0:4d:a2:ee:bc:49"
     *
     *
     * public String getMainDevice() { return this.mainDevice; }
     */

    /*
     * "rf_status": 161
     *
     *
     * public Integer getRfStatus() { return this.rfStatus; }
     */

    public int getRfStatusAsPercent() {
        int level = 100;
        int step = 100 / RF_STATUS_THRESHOLDS.length;
        for (int i : RF_STATUS_THRESHOLDS) {
            if (getRfStatus() < i)
                break;
            level -= step;
        }
        return level;
    }

    /**
     * "battery_vp"
     */

    public int getBatteryVpAsPercent() {
        int value;
        int minima;
        int spread;
        if (this.type.equalsIgnoreCase(NetatmoBindingConstants.THING_TYPE_MODULE_1)) {
            value = Math.min(getBatteryVp(), BATTERY_MODULE_1_THRESHOLD_0);
            minima = BATTERY_MODULE_1_THRESHOLD_3 + BATTERY_MODULE_1_THRESHOLD_2 - BATTERY_MODULE_1_THRESHOLD_1;
            spread = BATTERY_MODULE_1_THRESHOLD_0 - minima;
        } else {
            value = Math.min(getBatteryVp(), BATTERY_MODULE_4_THRESHOLD_0);
            minima = BATTERY_MODULE_4_THRESHOLD_3 + BATTERY_MODULE_4_THRESHOLD_2 - BATTERY_MODULE_4_THRESHOLD_1;
            spread = BATTERY_MODULE_4_THRESHOLD_0 - minima;
        }
        return 100 * (value - minima) / spread;
    }

    public BigDecimal getbatteryvp() {
        return new BigDecimal(getBatteryVpAsPercent());
    }

    public BigDecimal getrfstatus() {
        return new BigDecimal(getRfStatusAsPercent());
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = createToStringBuilder();
        builder.appendSuper(super.toString());

        builder.append("mainDevice", this.getMainDevice());
        builder.append("rfStatus", this.getRfStatus());
        builder.append("batteryVp", this.getBatteryVp());

        return builder.toString();
    }
}
