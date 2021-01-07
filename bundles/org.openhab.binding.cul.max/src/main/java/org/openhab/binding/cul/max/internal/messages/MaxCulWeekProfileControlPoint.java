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
package org.openhab.binding.cul.max.internal.messages;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
public class MaxCulWeekProfileControlPoint {

    private Integer hour;
    private Integer min;
    private float temperature;

    public void setMin(int i) {
        min = i;
    }

    public void setHour(int i) {
        hour = i;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getHour() {
        return hour;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTemperature() {
        return temperature;
    }
}
