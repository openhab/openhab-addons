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
package org.openhab.binding.millheat.internal.config;

/**
 * The {@link MillheatHeaterConfiguration} class contains heater thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 */
public class MillheatHeaterConfiguration {
    /*
     * Wi-Fi mac address
     */
    public String macAddress;
    /*
     * Wi-Fi heater id - found in logs
     */
    public Long heaterId;
    /*
     * Nominal heater panel power
     */
    public Integer power;

    @Override
    public String toString() {
        return "MillheatHeaterConfiguration [macAddress=" + macAddress + ", heaterId=" + heaterId + ", power=" + power
                + "]";
    }
}
