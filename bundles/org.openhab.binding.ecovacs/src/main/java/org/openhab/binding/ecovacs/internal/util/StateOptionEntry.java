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
package org.openhab.binding.ecovacs.internal.util;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;

/**
 * A mapping of an binding internal enum value to a user visible (item value) string
 * 
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class StateOptionEntry<T extends Enum<T>> {
    public final T enumValue;
    public final String value;
    public final Optional<DeviceCapability> capability;

    public StateOptionEntry(T enumValue, String value) {
        this(enumValue, value, null);
    }

    public StateOptionEntry(T enumValue, String value, @Nullable DeviceCapability capability) {
        this.enumValue = enumValue;
        this.value = value;
        this.capability = Optional.ofNullable(capability);
    }
}
