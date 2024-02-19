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
package org.openhab.binding.openweathermap.internal.dto.onecall;

/**
 * Holds the data from the <code>minutely</code> object of the JSON response of the One Call APIs.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class Minutely {
    private int dt;
    private double precipitation;

    public int getDt() {
        return dt;
    }

    public double getPrecipitation() {
        return precipitation;
    }
}
