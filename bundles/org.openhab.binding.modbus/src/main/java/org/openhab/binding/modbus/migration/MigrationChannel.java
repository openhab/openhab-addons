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
package org.openhab.binding.modbus.migration;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** A target poller channel generated from one legacy data Thing. */
@NonNullByDefault
public record MigrationChannel(String sourceDataUid, String id, Map<String, Object> configuration, String itemType) {
    public MigrationChannel {
        configuration = Map.copyOf(configuration);
    }
}
