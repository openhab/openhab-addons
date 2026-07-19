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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Handle to a file published by a {@link FileHostService}. Closing it stops serving the file.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface HostedFile extends AutoCloseable {

    /**
     * Returns the URL a receiver can fetch the hosted file from.
     *
     * @return the fetch URL
     */
    String url();

    /**
     * Stops serving the file and releases any resources.
     */
    @Override
    void close();
}
