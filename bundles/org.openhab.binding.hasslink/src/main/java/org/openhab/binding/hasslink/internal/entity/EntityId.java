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
package org.openhab.binding.hasslink.internal.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parsed representation of a Home Assistant Entity ID.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record EntityId(String domain, String objectId) {
    public EntityId {
        if (domain.isBlank()) {
            throw new IllegalArgumentException("Domain cannot be blank");
        }
        if (objectId.isBlank()) {
            throw new IllegalArgumentException("Object ID cannot be blank");
        }
    }

    public String getFullId() {
        return domain + "." + objectId;
    }
}
