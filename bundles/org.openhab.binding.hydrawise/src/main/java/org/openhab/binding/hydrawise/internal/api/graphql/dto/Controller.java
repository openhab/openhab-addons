/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dan Cunningham - Initial contribution
 */

public class Controller {
    public Integer id;
    public String name;
    public ControllerStatus status;
    // the top level Gson instance uses FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES, which would otherwise
    // look for "last_contact_time" instead of the API's camelCase "lastContactTime"
    @SerializedName("lastContactTime")
    public Time lastContactTime;
    public Hardware hardware;
    public Location location;
    public List<Zone> zones = null;
    public List<Sensor> sensors = null;
}
