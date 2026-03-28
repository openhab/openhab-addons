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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Light;

/**
 * Light device for the hybrid API.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LightDevice extends BaseDevice<Light, org.openhab.binding.unifiprotect.internal.api.pub.dto.Light> {
    public LightDevice(Light privateLight, org.openhab.binding.unifiprotect.internal.api.pub.dto.Light publicLight) {
        super(privateLight, publicLight);
    }
}
