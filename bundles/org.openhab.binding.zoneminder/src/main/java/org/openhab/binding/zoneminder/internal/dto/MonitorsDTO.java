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
package org.openhab.binding.zoneminder.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MonitorsDTO} contains the list of monitors returned
 * from the Zoneminder API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class MonitorsDTO extends AbstractResponseDTO {

    /**
     * List of monitors
     */
    @SerializedName("monitors")
    public List<MonitorItemDTO> monitorItems;
}
