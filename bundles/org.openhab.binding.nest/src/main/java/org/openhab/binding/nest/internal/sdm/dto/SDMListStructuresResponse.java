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
 * Lists structures managed by the enterprise.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.structures/list">
 *      https://developers.google.com/nest/device-access/reference/rest/v1/enterprises.structures/list</a>
 */
@NonNullByDefault
public class SDMListStructuresResponse {
    /**
     * The list of structures.
     */
    public List<SDMStructure> structures = List.of();

    /**
     * The pagination token to retrieve the next page of results.
     */
    public String nextPageToken = "";
}
