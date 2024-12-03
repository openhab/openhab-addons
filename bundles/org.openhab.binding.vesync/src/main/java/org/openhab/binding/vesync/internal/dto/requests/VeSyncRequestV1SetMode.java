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
package org.openhab.binding.vesync.internal.dto.requests;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncRequestV1SetMode} is the Java class as a DTO define a V1 Set Mode command for the Vesync
 * API.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncRequestV1SetMode extends VeSyncRequestV1Command {

    @SerializedName("mode")
    public String mode = null;

    public VeSyncRequestV1SetMode(final String deviceUuid, final String mode) {
        super(deviceUuid);
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
