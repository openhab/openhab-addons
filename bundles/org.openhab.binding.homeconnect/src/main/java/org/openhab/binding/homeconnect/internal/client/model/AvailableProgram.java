/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 *
 */
@NonNullByDefault
public class AvailableProgram {
    private final String key;
    private final boolean available;
    private final String execution;

    public AvailableProgram(String key, boolean available, String execution) {
        this.key = key;
        this.available = available;
        this.execution = execution;
    }

    public String getKey() {
        return key;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getExecution() {
        return execution;
    }

    @Override
    public String toString() {
        return "AvailableProgram [key=" + key + ", available=" + available + ", execution=" + execution + "]";
    }

}
