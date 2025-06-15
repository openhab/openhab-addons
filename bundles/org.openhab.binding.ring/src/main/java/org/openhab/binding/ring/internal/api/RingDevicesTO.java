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
package org.openhab.binding.ring.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RingDevicesTO} class is a TO containing all devices for an account
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RingDevicesTO {
    @SerializedName("doorbots")
    public List<RingDeviceTO> doorbells = List.of();
    public List<RingDeviceTO> chimes = List.of();
    @SerializedName("stickup_cams")
    public List<RingDeviceTO> stickupCams = List.of();
    public List<RingDeviceTO> other = List.of();
}
