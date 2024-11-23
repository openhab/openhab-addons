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
package org.openhab.binding.hydrawise.internal.api.local.dto;

/**
 * The {@link Forecast} class models a daily weather forecast
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Forecast {

    public String tempHi;

    public String tempLo;

    public String conditions;

    public String day;

    public Integer pop;

    public Integer humidity;

    public String wind;

    public String icon;

    public String iconLocal;
}
