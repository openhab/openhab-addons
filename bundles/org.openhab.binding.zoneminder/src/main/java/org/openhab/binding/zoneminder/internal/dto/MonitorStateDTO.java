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

import org.openhab.binding.zoneminder.internal.handler.MonitorState;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MonitorStateDTO} represents the current state of the monitor.
 * Possible states are IDLE, PREALERT, ALAARM, ALERT, TAPE, UNKNOWN
 *
 * @author Mark Hilbush - Initial contribution
 */
public class MonitorStateDTO extends AbstractResponseDTO {

    /**
     * The current state of the monitor (e.g. IDLE, ALERT, ALARM, etc.)
     */
    @SerializedName("status")
    public MonitorState state;
}
