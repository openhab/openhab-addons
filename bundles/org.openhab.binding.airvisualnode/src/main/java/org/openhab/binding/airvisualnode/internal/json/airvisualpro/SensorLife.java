/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.airvisualnode.internal.json.airvisualpro;

/**
 * Sensor Usage/Life data
 *
 * @author Oleg Davydyuk - Initial contribution
 */
public class SensorLife {

    private long pm25;

    public SensorLife(long pm25) {
        this.pm25 = pm25;
    }

    public long getPm25() {
        return pm25;
    }

    public void setPm25(long pm25) {
        this.pm25 = pm25;
    }
}
