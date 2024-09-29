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
package org.openhab.binding.verisure.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The battery status of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureBatteryStatusDTO {

    public boolean chosen;
    public @Nullable String id;
    public @Nullable String pictureBase;
    public @Nullable String translatedType;
    public @Nullable String location;
    public @Nullable String batteryInfo;
    public @Nullable String status;
    public @Nullable String alias;
    public int index;
    public boolean selectable;

    public @Nullable String getId() {
        return id;
    }

    public @Nullable String getLocation() {
        return location;
    }

    public @Nullable String getBatteryInfo() {
        return batteryInfo;
    }

    public @Nullable String getStatus() {
        return status;
    }
}
