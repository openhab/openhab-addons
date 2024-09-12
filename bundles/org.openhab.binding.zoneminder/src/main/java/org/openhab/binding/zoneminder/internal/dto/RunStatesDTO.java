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
package org.openhab.binding.zoneminder.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RunStatesDTO} contains the list of run states.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class RunStatesDTO extends AbstractResponseDTO {

    /**
     * List of run states
     */
    @SerializedName("states")
    public List<RunStateDTO> runStatesList;
}
