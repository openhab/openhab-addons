/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EventsDTO} contains the list of events that match the
 * query's selection criteria.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class EventsDTO extends AbstractResponseDTO {

    /**
     * List of events matching the selection criteria
     */
    @SerializedName("events")
    public List<EventContainerDTO> eventsList;

    /**
     * Pagination information (currently not used)
     */
    @SerializedName("pagination")
    public Object pagination;
}
