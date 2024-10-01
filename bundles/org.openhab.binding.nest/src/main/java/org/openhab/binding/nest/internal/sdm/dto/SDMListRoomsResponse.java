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
package org.openhab.binding.nest.internal.sdm.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Lists rooms managed by the enterprise.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.structures.rooms/list">
 *      https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.structures.rooms/list</a>
 */
@NonNullByDefault
public class SDMListRoomsResponse {
    /**
     * The list of rooms.
     */
    public List<SDMRoom> rooms = List.of();

    /**
     * The pagination token to retrieve the next page of results.
     */
    public String nextPageToken = "";
}
