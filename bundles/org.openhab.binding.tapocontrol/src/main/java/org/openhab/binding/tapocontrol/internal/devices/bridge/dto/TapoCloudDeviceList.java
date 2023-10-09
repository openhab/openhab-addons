/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.devices.bridge.dto;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * TapoCloud DeviceList Data Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoCloudDeviceList(@Expose List<TapoCloudDevice> deviceList) implements Iterable<TapoCloudDevice> {

    /* init new emty list */
    public TapoCloudDeviceList() {
        this(List.of());
    }

    @Override
    public Iterator<TapoCloudDevice> iterator() {
        return deviceList.iterator();
    }

    /**********************************************
     * Return default data if recordobject is null
     **********************************************/
    @Override
    public List<TapoCloudDevice> deviceList() {
        return Objects.requireNonNullElse(deviceList, List.of());
    }
}
