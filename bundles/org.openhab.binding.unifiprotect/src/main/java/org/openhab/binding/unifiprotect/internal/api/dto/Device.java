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
package org.openhab.binding.unifiprotect.internal.api.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Base type for all device-like DTOs discriminated by modelKey.
 *
 * Contains common fields shared across many devices.
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class Device {
    public String id;
    public ModelKey modelKey;
    public @Nullable DeviceState state;
    public String name;
}
