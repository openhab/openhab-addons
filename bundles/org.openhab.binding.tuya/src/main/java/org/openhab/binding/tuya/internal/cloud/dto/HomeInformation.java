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
package org.openhab.binding.tuya.internal.cloud.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HomeInformation} encapsulates the reported home information
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class HomeInformation {
    public String groupId = "";
    public String id = "";
    public String lat = "";
    public String lon = "";
    public String name = "";
    public String ownerId = "";

    @Override
    public String toString() {
        return "HomeInformation{groupId='" + groupId + "', id='" + id + "', lat='" + lat + "', lon='" + lon
                + "', name='" + name + "', ownerId='" + ownerId + "'}";
    }
}
