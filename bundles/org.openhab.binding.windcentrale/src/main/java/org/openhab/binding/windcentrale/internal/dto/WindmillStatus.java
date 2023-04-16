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
package org.openhab.binding.windcentrale.internal.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The live {@link WindmillStatus} provided by the Windcentrale API.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindmillStatus {

    public int power;

    public int powerPerShare;

    public int powerPercentage;

    public ZonedDateTime timestamp = ZonedDateTime.now();

    public int totalRuntime;

    public String windDirection = "";

    public int windPower;

    public int yearProduction;

    public double yearRuntime;

    @Override
    public String toString() {
        return "WindmillStatus [power=" + power + ", powerPerShare=" + powerPerShare + ", powerPercentage="
                + powerPercentage + ", timestamp=" + timestamp + ", totalRuntime=" + totalRuntime + ", windDirection="
                + windDirection + ", windPower=" + windPower + ", yearProduction=" + yearProduction + ", yearRuntime="
                + yearRuntime + "]";
    }
}
