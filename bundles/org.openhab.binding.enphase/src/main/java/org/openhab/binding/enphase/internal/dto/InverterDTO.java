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
package org.openhab.binding.enphase.internal.dto;

/**
 * Data class for Enphase Inverter data.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class InverterDTO {
    public String serialNumber;
    public long lastReportDate;
    public int devType;
    public int lastReportWatts;
    public int maxReportWatts;

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }
}
