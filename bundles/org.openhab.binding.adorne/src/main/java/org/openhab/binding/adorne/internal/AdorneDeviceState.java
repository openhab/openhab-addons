/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.adorne.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AdorneDeviceState} class defines a simple POJO representing the Adorne device state.
 *
 * @author Mark Theiding - Initial contribution
 */
@NonNullByDefault
public class AdorneDeviceState {
    public final int zoneId;
    public final String name;
    public final ThingTypeUID deviceType;
    public final boolean onOff;
    public final int brightness;

    public AdorneDeviceState(int zoneId, String name, ThingTypeUID deviceType, boolean onOff, int brightness) {
        this.zoneId = zoneId;
        this.name = name;
        this.deviceType = deviceType;
        this.onOff = onOff;
        this.brightness = brightness;
    }
}
