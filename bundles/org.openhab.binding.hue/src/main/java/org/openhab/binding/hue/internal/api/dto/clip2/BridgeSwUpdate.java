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
package org.openhab.binding.hue.internal.api.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * DTO for an API v1.20+ bridge configuration response's software update part.
 * 
 * @see <a href="https://developers.meethue.com/develop/software-update/">Developer documentation</a>
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class BridgeSwUpdate {
    private @Nullable BridgeSwUpdateBridge bridge;

    public @Nullable BridgeSwUpdateBridge getBridge() {
        return bridge;
    }
}
