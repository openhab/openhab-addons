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
 * This class represents an application and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ApplicationList {
    /** The application title */
    private @Nullable String title;

    /** The application uri */
    private @Nullable String uri;

    /** The application icon */
    private @Nullable String icon;

    /** The application data */
    private @Nullable String data;

    /**
     * Constructor used for deserialization only
     */
    public ApplicationList() {
    }

    /**
     * Gets the application title
     *
     * @return the application title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Gets the application uri
     *
     * @return the application uri
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gets the application icon
     *
     * @return the application icon
     */
    public @Nullable String getIcon() {
        return icon;
    }

    /**
     * Gets the application data
     *
     * @return the application data
     */
    public @Nullable String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ApplicationListItem [title=" + title + ", uri=" + uri + ", icon=" + icon + ", data=" + data + "]";
    }
}
