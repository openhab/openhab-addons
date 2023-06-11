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
package org.openhab.binding.hydrawise.internal.api.local.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * The {@link StatusScheduleResponse} class models the Status and Schedule response message
 *
 * @author Dan Cunningham - Initial contribution
 */
public class StatusScheduleResponse extends LocalScheduleResponse {

    public Integer controllerId;

    public Integer customerId;

    public Integer userId;

    public Integer nextpoll;

    public List<Sensor> sensors = new LinkedList<Sensor>();

    public String message;

    public String obsRain;

    public String obsRainWeek;

    public String obsMaxtemp;

    public Integer obsRainUpgrade;

    public String obsRainText;

    public String obsCurrenttemp;

    public String wateringTime;

    public Integer waterSaving;

    public String lastContact;

    public List<Forecast> forecast = new LinkedList<Forecast>();

    public String status;

    public String statusIcon;
}
