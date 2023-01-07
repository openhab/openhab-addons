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
package org.openhab.binding.dsmr.internal.device.p1telegram;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface TelegramParser {

    /**
     *
     * @param data byte data to parse
     * @param length number of bytes to parse
     */
    void parse(byte[] data, int length);

    /**
     * Reset the current telegram state.
     */
    default void reset() {
    }

    /**
     * @param lenientMode the lenientMode to set
     */
    default void setLenientMode(boolean lenientMode) {
    }
}
