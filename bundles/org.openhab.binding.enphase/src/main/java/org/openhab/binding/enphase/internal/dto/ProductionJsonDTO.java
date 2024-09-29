/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.enphase.internal.dto;

/**
 * Data class for Envoy production and consumption data from production.json api call.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class ProductionJsonDTO {

    public static class DataDTO {
        public String type;
        public int activeCount;
        public float whLifetime;
        public float whLastSevenDays;
        public float whToday;
        public float wNow;
        public float rmsCurrent;
        public float rmsVoltage;
        public float reactPwr;
        public float apprntPwr;
        public float pwrFactor;
        public long readingTime;
        public float varhLeadToday;
        public float varhLagToday;
        public float vahToday;
        public float varhLeadLifetime;
        public float varhLagLifetime;
        public float vahLifetime;
    }

    public DataDTO[] production;
    public DataDTO[] consumption;
}
