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
package org.openhab.binding.salus.internal.handler;

import java.util.Optional;
import java.util.SortedSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public interface CloudApi {
    SortedSet<Device> findDevices();

    Optional<Device> findDevice(String dsn);

    boolean setValueForProperty(String dsn, String propertyName, Object value);

    SortedSet<DeviceProperty<?>> findPropertiesForDevice(String dsn);
}
