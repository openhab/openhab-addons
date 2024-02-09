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
package org.openhab.binding.bosesoundtouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OperationModeType} class is holding all OperationModes
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
@NonNullByDefault
public enum OperationModeType {
    OFFLINE,
    STANDBY,
    INTERNET_RADIO,
    BLUETOOTH,
    AUX,
    AUX1,
    AUX2,
    AUX3,
    SPOTIFY,
    PANDORA,
    DEEZER,
    SIRIUSXM,
    STORED_MUSIC,
    AMAZON,
    TV,
    HDMI1,
    TUNEIN,
    ALEXA,
    OTHER;

    private String name;

    private OperationModeType() {
        this.name = name();
    }

    @Override
    public String toString() {
        return name;
    }
}
