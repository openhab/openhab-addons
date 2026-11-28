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
package org.openhab.binding.atv.internal.client.dto;

/**
 * Information about an audio output device (e.g. an AirPlay speaker).
 *
 * @param identifier unique identifier of the output device
 * @param name user friendly device name (may be {@code null})
 * @param volume current volume in percent [0.0-100.0]
 *
 * @author Dan Cunningham - Initial contribution
 */
public record OutputDevice(String identifier, String name, double volume) {

    /**
     * Creates an output device with an unknown name and zero volume.
     *
     * @param identifier unique identifier of the output device
     */
    public OutputDevice(String identifier) {
        this(identifier, null, 0.0);
    }

    @Override
    public String toString() {
        return "Device: " + name + " (" + identifier + ")";
    }
}
