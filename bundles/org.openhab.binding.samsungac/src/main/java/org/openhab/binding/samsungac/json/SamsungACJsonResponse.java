/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.samsungac.json;

import java.math.BigDecimal;
import java.util.List;

import org.openhab.binding.samsungac.handler.PowerUsage;

/**
 *
 * The {@link SamsungACJsonResponse} class defines the Response Structure Samsung Digital Inverter
 * The class is the base class for the json response
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

public class SamsungACJsonResponse {

    private List<SamsungACJsonDevices> Devices;

    private PowerUsage powerUsage;

    private BigDecimal powerUsageDifference;

    /**
     * @return the previousPowerUsage
     */
    public BigDecimal getPowerUsageDifference() {
        return powerUsageDifference;
    }

    /**
     * @param previousPowerUsage the previousPowerUsage to set
     */
    public void setPowerUsageDifference(BigDecimal powerUsageDifference) {
        this.powerUsageDifference = powerUsageDifference;
    }

    public SamsungACJsonResponse() {
    }

    public List<SamsungACJsonDevices> getDevices() {
        return Devices;
    }

    public PowerUsage getPowerUsage() {
        return powerUsage;
    }

    public void setPowerUsage(PowerUsage powerUsage) {
        this.powerUsage = powerUsage;
    }
}
