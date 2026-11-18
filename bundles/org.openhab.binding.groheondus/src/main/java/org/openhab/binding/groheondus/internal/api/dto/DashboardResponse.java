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
package org.openhab.binding.groheondus.internal.api.dto;

import java.util.List;

/**
 * Response of the dashboard endpoint listing all locations, rooms and appliances of the account.
 *
 * @author Arne Seime - Initial contribution
 */
public class DashboardResponse {
    public List<Location> locations;

    public static class Location {
        public Integer id;
        public List<Room> rooms;
    }

    public static class Room {
        public Integer id;
        public List<ApplianceResponse> appliances;
    }
}
