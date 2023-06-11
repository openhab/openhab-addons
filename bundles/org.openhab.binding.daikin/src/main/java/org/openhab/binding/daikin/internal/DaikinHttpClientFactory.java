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
package org.openhab.binding.daikin.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;

/**
 * Factory class to create Jetty http clients
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public interface DaikinHttpClientFactory {

    /**
     * Returns the shared Jetty http client. You must not call any setter methods or {@code stop()} on it.
     * The returned client is already started.
     *
     * @return the shared Jetty http client
     */
    @Nullable
    HttpClient getHttpClient();
}
