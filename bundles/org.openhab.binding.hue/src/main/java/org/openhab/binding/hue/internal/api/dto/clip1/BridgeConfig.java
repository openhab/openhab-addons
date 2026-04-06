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

    public @Nullable String getSoftwareVersion() {
        return swversion;
    }

    public @Nullable UpdateStatusV2 getUpdateStatus() {
        return (swupdate2 instanceof BridgeSwUpdate update)
                && (update.getBridge() instanceof BridgeSwUpdateBridge bridge) ? bridge.getUpdateStatus() : null;
    }

    /**
     * Creates a swupdate2 field with check for update flag set. Triggers the bridge to check for updates.
     */
    public BridgeConfig setCheckForUpdate() {
        swupdate2 = new BridgeSwUpdate().setCheckForUpdate();
        return this;
    }

    /**
     * Creates a swupdate2 field with install update flag set. Triggers the bridge to do an update.
     */
    public BridgeConfig setInstallUpdate() {
        swupdate2 = new BridgeSwUpdate().setInstallUpdate();
        return this;
    }
}
