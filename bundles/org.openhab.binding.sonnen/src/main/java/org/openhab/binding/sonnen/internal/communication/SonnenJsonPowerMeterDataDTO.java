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
package org.openhab.binding.sonnen.internal.communication;

/**
 * The {@link SonnenJsonPowerMeterDataDTO} is the Java class used to map the JSON
 * response from the API to a PowerMeter Object.
 *
 * @author Christian Feininger - Initial contribution
 */
public class SonnenJsonPowerMeterDataDTO {

    float kwh_exported;
    float kwh_imported;

    /**
     * @return the kwh_exported
     */
    public float getKwh_exported() {
        return kwh_exported;
    }

    /**
     * @return the kwh_imported
     */
    public float getKwh_imported() {
        return kwh_imported;
    }
}
