/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.impl;

import org.eclipse.jdt.annotation.Nullable;

import de.afrouper.server.sungrow.api.dto.Device;

/**
 * @author Christian Kemper - Initial contribution
 */
public class DeviceConfiguration {

    @Nullable
    private Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean isValid() {
        return device != null;
    }
}
