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

/**
 * Control action request for legacy protocol vehicles.
 *
 * @author Florian Hotze - Initial contribution
 */
public record ControlRequest(UUID deviceId, String action, Integer hvacType, Options options, String tempCode,
        String unit) {
    public record Options(boolean defrost, int heating1) {
    }
}
