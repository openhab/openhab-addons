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

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A factory for creating new requests to the Hive API.
 *
 * @see HiveApiRequest
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface HiveApiRequestFactory {
    /**
     * Create a new request to the Hive API.
     *
     * @param endpointPath
     *      The relative URI of the endpoint to make the request to.
     *      (relative to the Hive API base path).
     *
     * @return
     *      The new request.
     */
    HiveApiRequest newRequest(URI endpointPath);
}
