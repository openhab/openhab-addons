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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link WakeWordTO} encapsulates a single wake word definition
 *
 * @author Jan N. Klug - Initial contribution
 */
public class WakeWordTO {
    public boolean active;
    public String deviceSerialNumber;
    public String deviceType;
    public Object midFieldState;
    public String wakeWord;

    @Override
    public @NonNull String toString() {
        return "WakeWordTO{active=" + active + ", deviceSerialNumber='" + deviceSerialNumber + "', deviceType='"
                + deviceType + "', midFieldState=" + midFieldState + ", wakeWord='" + wakeWord + "'}";
    }
}
