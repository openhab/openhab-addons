/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.growatt.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GrowattDevice} is a DTO containing device data fields received from the Growatt cloud server.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattDevice {

    private @Nullable String deviceType;
    private @Nullable String deviceSn;

    public String getId() {
        String deviceSn = this.deviceSn;
        return deviceSn != null ? deviceSn : "";
    }

    public String getType() {
        String deviceType = this.deviceType;
        return deviceType != null ? deviceType : "";
    }
}
