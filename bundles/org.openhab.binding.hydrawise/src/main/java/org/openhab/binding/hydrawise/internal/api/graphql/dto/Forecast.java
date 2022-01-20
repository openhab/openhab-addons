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
package org.openhab.binding.hydrawise.internal.api.graphql.dto;

/**
 * @author Dan Cunningham - Initial contribution
 */
public class Forecast {
    public String time;
    public String updateTime;
    public String conditions;
    public UnitValue highTemperature;
    public UnitValue lowTemperature;
    public UnitValue evapotranspiration;
    public Integer probabilityOfPrecipitation;
    public UnitValue precipitation;
    public Number averageHumidity;
    public UnitValue averageWindSpeed;
}
