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
package org.openhab.binding.dsmr.internal.device.p1telegram;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;

/**
 * Data class containing a Telegram with CosemObjects and TelegramState and if in lenient mode also the raw telegram
 * data.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class P1Telegram {

    /**
     * The TelegramState described the meta data of the P1Telegram
     */

    private final List<CosemObject> cosemObjects;
    private final String rawTelegram;
    private final List<Entry<String, String>> unknownCosemObjects;

    public P1Telegram(final List<CosemObject> cosemObjects) {
        this(cosemObjects, "", Collections.emptyList());
    }

    public P1Telegram(final List<CosemObject> cosemObjects, final String rawTelegram,
            final List<Entry<String, String>> unknownCosemObjects) {
        this.cosemObjects = cosemObjects;
        this.rawTelegram = rawTelegram;
        this.unknownCosemObjects = unknownCosemObjects;
    }

    /**
     * @return The list of CosemObjects
     */
    public List<CosemObject> getCosemObjects() {
        return cosemObjects;
    }

    /**
     * @return The raw telegram data.
     */
    public String getRawTelegram() {
        return rawTelegram;
    }

    /**
     * @return The list of CosemObject found in the telegram but not known to the binding
     */
    public List<Entry<String, String>> getUnknownCosemObjects() {
        return unknownCosemObjects;
    }
}
