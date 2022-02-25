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
package org.openhab.binding.sunsa.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Response object for <code>PutDevice</code> API.
 *
 * @author jirom - Initial contribution
 */
@JsonSerialize(as = ImmutablePutDeviceResponse.class)
@JsonDeserialize(builder = ImmutablePutDeviceResponse.Builder.class)
@Value.Immutable
@NonNullByDefault
public interface PutDeviceResponse {
    @JsonProperty("device")
    public Device getDevice();
}
