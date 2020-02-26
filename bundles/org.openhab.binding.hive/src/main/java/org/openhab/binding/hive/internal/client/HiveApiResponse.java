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
package org.openhab.binding.hive.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A facade for HTTP responses from the Hive API
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface HiveApiResponse {
    /**
     * The HTTP response code of this response.
     */
    int getStatusCode();

    /**
     * Get the content of the response.
     *
     * @param contentType
     *      The class of the response content.
     *
     * @param <T>
     *      The type of response content.
     *
     * @return
     *
     * @throws IllegalArgumentException
     *      If the content of the response does not represent an instance
     *      of {@code T}.
     */
    <T> T getContent(Class<T> contentType);

    /**
     * Get the raw content of the response.
     *
     * @return
     */
    String getRawContent();
}
