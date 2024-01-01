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
package org.openhab.binding.spotify.internal.api.model;

/**
 * Spotify Web Api user data class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Me {
    private String displayName;
    private String id;
    private String product;

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }
}
