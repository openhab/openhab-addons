/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal;

/**
 * The {@link AirQualityConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class AirQualityConfiguration {

    public static final String LOCATION = "location";

    public String apikey;
    public String location;
    public Integer stationId;
    public Integer refresh;
}
