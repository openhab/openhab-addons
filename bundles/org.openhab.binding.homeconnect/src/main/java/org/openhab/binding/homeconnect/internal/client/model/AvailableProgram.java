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
package org.openhab.binding.homeconnect.internal.client.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * AvailableProgram model
 *
 * @author Jonas Brüstel - Initial contribution
 * @author Laurent Garnier - field "supported" added
 *
 */
@NonNullByDefault
public class AvailableProgram {
    private final String key;
    private final boolean supported;
    private final boolean available;
    private final String execution;

    public AvailableProgram(String key, boolean supported, boolean available, String execution) {
        this.key = key;
        this.supported = supported;
        this.available = available;
        this.execution = execution;
    }

    public AvailableProgram(String key, boolean available, String execution) {
        this(key, true, available, execution);
    }

    public AvailableProgram(String key, boolean supported) {
        this(key, supported, true, "");
    }

    public String getKey() {
        return key;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getExecution() {
        return execution;
    }

    @Override
    public String toString() {
        return "AvailableProgram [key=" + key + ", supported=" + supported + ", available=" + available + ", execution="
                + execution + "]";
    }
}
