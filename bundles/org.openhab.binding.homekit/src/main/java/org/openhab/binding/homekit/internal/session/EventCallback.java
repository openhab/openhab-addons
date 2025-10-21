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
package org.openhab.binding.homekit.internal.session;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Callback interface for handling HTTP 'EVENT' messages with associated HTTP headers and HTTP contents.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface EventCallback {

    /*
     * Method invoked when an event occurs. Receives a 3D byte array where the first element is the HTTP
     * headers, the second element is the content, and the third is the raw trace (if enabled).
     *
     * @param eventData a 3D array of byte arrays.
     */
    public void onEvent(byte[][] eventData);
}
