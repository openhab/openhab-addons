/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.dto;

import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.NETATMO_BASE_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NASnapshot extends NAObject {
    private static String BASE_URL = NETATMO_BASE_URL + "/api/getcamerapicture?image_id=%s&key=%s";
    private @Nullable String key;

    public NASnapshot(String id, String key) {
        this.id = id;
        this.key = key;
    }

    public @Nullable String getUrl() {
        if (key != null) {
            return String.format(BASE_URL, id, key);
        }
        return null;
    }
}
