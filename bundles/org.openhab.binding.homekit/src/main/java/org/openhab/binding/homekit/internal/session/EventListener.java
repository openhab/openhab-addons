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
 * Callback interface for handling HTTP 'EVENT' message contents.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface EventListener {

    /*
     * Method invoked when an event occurs.
     *
     * @param jsonContent string containing the HTTP json content.
     */
    public void onEvent(String jsonContent);
}
