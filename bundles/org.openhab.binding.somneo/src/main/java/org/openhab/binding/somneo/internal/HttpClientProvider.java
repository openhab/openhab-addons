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
package org.openhab.binding.somneo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;

/**
 * The {@link HttpClientProvider} defines the interface for providing
 * {@link HttpClient} instances to thing handlers.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public interface HttpClientProvider {

    /**
     * Get the secure http client
     *
     * @return a HttpClient
     */
    HttpClient getSecureClient();

    /**
     * Get the insecure http client (ignores SSL errors)
     *
     * @return a HttpClient
     */
    HttpClient getInsecureClient();
}
