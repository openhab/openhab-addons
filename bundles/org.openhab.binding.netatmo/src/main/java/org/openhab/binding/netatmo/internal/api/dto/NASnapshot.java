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

import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.*;

import java.net.MalformedURLException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NASnapshot extends NAObject {
    private static UriBuilder URI_BUILDER = UriBuilder.fromUri(URL_API).path(PATH_API).path(SPATH_GETCAMERAPICTURE);
    private final Logger logger = LoggerFactory.getLogger(NASnapshot.class);
    private @Nullable String key;

    public NASnapshot(String id, String key) {
        this.id = id;
        this.key = key;
    }

    public @Nullable String getUrl() {
        if (key != null) {
            URI uri = URI_BUILDER.clone().queryParam("image_id", id).queryParam("key", key).build();
            try {
                return uri.toURL().toString();
            } catch (MalformedURLException e) {
                logger.warn("Malformed URI in snapshot : {}", uri);
            }
        }
        return null;
    }
}
