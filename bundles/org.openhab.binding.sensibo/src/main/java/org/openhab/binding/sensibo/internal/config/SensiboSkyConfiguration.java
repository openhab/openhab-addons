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
package org.openhab.binding.sensibo.internal.config;

/**
 * The {@link SensiboSkyConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 */
public class SensiboSkyConfiguration {
    /*
     * SensiboSky MAC address
     */
    public String macAddress;

    @Override
    public String toString() {
        return "SensiboSkyConfiguration [macAddress=" + macAddress + "]";
    }
}
