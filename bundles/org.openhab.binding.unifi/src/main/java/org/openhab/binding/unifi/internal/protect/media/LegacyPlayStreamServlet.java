/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.protect.media;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;

/**
 * Identical in behaviour to {@link PlayStreamServlet}, but a distinct class so it can be registered
 * under the legacy {@code /unifiprotect/media/play} alias alongside the new {@code /unifi/media/play}
 * one. The OSGi {@code HttpService} derives a servlet's identity from its class name, so two
 * instances of the same class cannot both be registered.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LegacyPlayStreamServlet extends PlayStreamServlet {
    private static final long serialVersionUID = 1L;

    public LegacyPlayStreamServlet(UnifiMediaService mediaService, HttpClient httpClient) {
        super(mediaService, httpClient);
    }
}
