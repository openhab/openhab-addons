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
package org.openhab.binding.emby.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EmbyDeviceEncoder} is responsible for encoding device identifiers
 * for communication with the Emby server. It transforms a given device ID into
 * a format acceptable by the Emby protocol by replacing non-alphanumeric
 * characters with a fixed token.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyDeviceEncoder {

    /**
     * Encodes the specified device identifier by replacing any character
     * that is not an ASCII letter (A-Z, a-z) or digit (0-9) with the token
     * {@code UYHJKU}. This ensures the device ID conforms to the Emby protocol
     * requirements.
     *
     * @param deviceID the original device identifier to encode
     * @return the encoded device identifier, where all non-alphanumeric characters
     *         have been replaced with {@code UYHJKU}
     */
    public String encodeDeviceID(String deviceID) {
        return deviceID.replaceAll("[^A-Za-z0-9]", "UYHJKU");
    }
}
