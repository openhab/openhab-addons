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
package org.openhab.binding.homekit.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of the audio codecs defined by the HomeKit Accessory Protocol for camera audio streams.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum AudioCodecType {
    PCMU(0x00),
    PCMA(0x01),
    AAC_ELD(0x02),
    OPUS(0x03),
    MSBC(0x04),
    AMR(0x05),
    AMR_WB(0x06);

    public final int value;

    AudioCodecType(int value) {
        this.value = value;
    }
}
