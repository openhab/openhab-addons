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
package org.openhab.binding.qolsysiq.internal.client.dto.model;

/**
 * A zone sensor
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Zone {
    public String id;
    public String type;
    public String name;
    public String group;
    public ZoneStatus status;
    public Integer state;
    public Integer zoneId;
    public Integer zonePhysicalType;
    public Integer zoneAlarmType;
    public ZoneType zoneType;
    public Integer partitionId;
}
