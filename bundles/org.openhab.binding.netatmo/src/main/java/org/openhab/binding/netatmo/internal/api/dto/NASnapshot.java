/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SUB_PATH_GETCAMERAPICTURE;

import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.RestManager;

/**
 * The {@link NASnapshot} holds data related to a snapshot.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NASnapshot extends NAObject {
    private @Nullable String url;

    public NASnapshot(String id, String key) {
        this.id = id;
        this.description = key;
    }

    public @Nullable String getUrl() {
        if (url != null) {
            return url;
        } else if (description != null) {
            URI uri = RestManager.getApiUriBuilder(SUB_PATH_GETCAMERAPICTURE, "image_id", id, "key", description)
                    .build();
            try {
                return uri.toURL().toString();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Malformed URI in snapshot, file a bug : " + uri);
            }
        }
        return null;
    }
}
