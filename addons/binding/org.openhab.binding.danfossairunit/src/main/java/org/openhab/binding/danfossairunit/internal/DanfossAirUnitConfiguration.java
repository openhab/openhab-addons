/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.danfossairunit.internal;

/**
 * The {@link DanfossAirUnitConfiguration} class contains fields mapping thing configuration paramters.
 *
 * @author Ralf Duckstein - Initial contribution
 */
public class DanfossAirUnitConfiguration {
    /**
     * Host address or IP of the CCM
     */
    public String host;

    /**
     * Port of web service of the CCM
     */
    public int port = 30046;

    /**
     * Polling interval for values from sensors
     */
    public int sensorPolling = 300;

    /**
     * Polling interval for values from settings
     */
    public int settingPolling = 60;

}
