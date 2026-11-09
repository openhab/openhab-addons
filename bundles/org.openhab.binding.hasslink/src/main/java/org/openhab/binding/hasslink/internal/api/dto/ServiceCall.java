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
package org.openhab.binding.hasslink.internal.api.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ServiceCall} DTO represents a Home Assistant service call, which can be sent to the Home Assistant
 * WebSocket API to invoke a specific service on a given entity.
 *
 * @param domain the Home Assistant domain (e.g., "light", "switch", "script")
 * @param service the service name to invoke (e.g., "turn_on", "turn_off", "lock", "unlock")
 * @param entityId the target entity ID for the service
 * @param serviceData optional additional data to include in the service call payload
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record ServiceCall(String domain, String service, @Nullable String entityId,
        @Nullable Map<String, Object> serviceData) {

    public ServiceCall(String domain, String service, @Nullable String entityId) {
        this(domain, service, entityId, null);
    }
}
