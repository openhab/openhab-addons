/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the web application status and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class WebAppStatus {
    /** The url of the application */
    private @Nullable String url;

    /** Whether active or not */
    private @Nullable Boolean active;

    /**
     * Constructor used for deserialization only
     */
    public WebAppStatus() {
    }

    /**
     * Gets the url of the application
     *
     * @return the url of the application
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Checks if the application is active
     *
     * @return true if active, false otherwise
     */
    public @Nullable Boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "WebAppStatus [active=" + active + ", url=" + url + "]";
    }
}
