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
package org.openhab.binding.unifiprotect.internal.api.hybrid.devices;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectModel;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.Device;

/**
 * Base device for the hybrid API.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class BaseDevice<PRIV extends UniFiProtectModel, PUB extends Device> {
    public final PRIV privateDevice;
    public final PUB publicDevice;

    public BaseDevice(PRIV privateDevice, PUB publicDevice) {
        this.privateDevice = Objects.requireNonNull(privateDevice, "privateDevice");
        this.publicDevice = Objects.requireNonNull(publicDevice, "publicDevice");
    }

    public String getId() {
        return Objects.requireNonNull(privateDevice.id, "Device id must not be null");
    }
}
