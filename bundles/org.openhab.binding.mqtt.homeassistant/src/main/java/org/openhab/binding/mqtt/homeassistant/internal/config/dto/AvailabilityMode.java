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
package org.openhab.binding.mqtt.homeassistant.internal.config.dto;

import com.google.gson.annotations.SerializedName;

/**
 * controls the conditions needed to set the entity to available
 *
 * @author Anton Kharuzhy - Initial contribution
 */
public enum AvailabilityMode {
    /**
     * payload_available must be received on all configured availability topics before the entity is marked as online
     */
    @SerializedName("all")
    ALL,

    /**
     * payload_available must be received on at least one configured availability topic before the entity is marked as
     * online
     */
    @SerializedName("any")
    ANY,

    /**
     * the last payload_available or payload_not_available received on any configured availability topic controls the
     * availability
     */
    @SerializedName("latest")
    LATEST
}
