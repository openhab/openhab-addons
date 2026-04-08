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
package org.openhab.binding.hue.internal.api.dto.clip1;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.UpdateStatusV2;

/**
 * A 'special' DTO for bridge discovery to read the configuration from a bridge using API v1.0+
 *
 * @see <a href="https://developers.meethue.com/develop/software-update/">Developer documentation</a>
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class BridgeConfig {
    private @Nullable String swversion;
    private @Nullable BridgeSwUpdate swupdate2; // for API v1.20+ ('swupdate' is deprecated)

    public static final String DEVICE = "device";
    public static final String BRIDGE = "bridge";

    public @Nullable String getSoftwareVersion() {
        return swversion;
    }

    /**
     * Gets the update status map of the bridge and its devices if available, or null if not available.
     */
    public Map<String, @Nullable UpdateStatusV2> getUpdateStatusMap() {
        Map<String, @Nullable UpdateStatusV2> result = new HashMap<>();
        if (swupdate2 instanceof BridgeSwUpdate update) {
            result.put(DEVICE, update.getUpdateStatus());
            result.put(BRIDGE,
                    (update.getBridge() instanceof BridgeSwUpdateBridge bridge) ? bridge.getUpdateStatus() : null);
        }
        return result;
    }

    /**
     * Creates a swupdate2 field with install update flag set. Triggers the bridge to do an update.
     */
    public BridgeConfig setInstallUpdate() {
        swupdate2 = new BridgeSwUpdate().setInstallUpdate();
        return this;
    }
}
