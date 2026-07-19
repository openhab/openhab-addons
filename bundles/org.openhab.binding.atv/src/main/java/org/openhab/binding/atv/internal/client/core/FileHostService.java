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
package org.openhab.binding.atv.internal.client.core;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Publishes a local file over HTTP so a receiver can fetch it, used by AirPlay {@code play_url}
 * when given a local file path.
 *
 * <p>
 * The library does not embed an HTTP server; the host application provides an implementation
 * (an openHAB binding backs it with the runtime's HTTP service). When no implementation is
 * available, {@code play_url} of a local file is unsupported.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface FileHostService {

    /**
     * Publishes a file and returns a handle exposing the URL a receiver can fetch it from.
     * Closing the handle stops serving the file.
     *
     * @param file the local file to publish
     * @return a handle to the hosted file
     * @throws IOException if the file cannot be published
     */
    HostedFile host(Path file) throws IOException;
}
