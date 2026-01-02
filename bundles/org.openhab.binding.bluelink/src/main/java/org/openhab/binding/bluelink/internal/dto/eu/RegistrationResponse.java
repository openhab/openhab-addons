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
package org.openhab.binding.bluelink.internal.dto.eu;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;

/**
 * Device registration response from the EU API.
 *
 * @author Florian Hotze - Initial contribution
 */
public record RegistrationResponse(@SerializedName("deviceId") UUID deviceId) {
}
