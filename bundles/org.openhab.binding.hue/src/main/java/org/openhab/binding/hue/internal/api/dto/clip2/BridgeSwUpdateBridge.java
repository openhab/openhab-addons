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
import org.openhab.binding.hue.internal.api.dto.clip2.enums.UpdateStatusV1;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.UpdateStatusV2;

/**
 * DTO for a v1 protocol bridge configuration response's software update bridge part.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class BridgeSwUpdateBridge {
    private @Nullable String state;

    /**
     * Reads the update status of the bridge in v1 protocol form and converts it to v2 protocol form.
     */
    public @Nullable UpdateStatusV2 getUpdateStatus() {
        return UpdateStatusV2.of(UpdateStatusV1.of(state));
    }
}
