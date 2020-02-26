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
import org.openhab.binding.hive.internal.client.exception.HiveClientRequestException;

/**
 * A facade for HTTP requests to the Hive API
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface HiveApiRequest {
    /**
     * Sets the media type this request accepts (expects in response).
     *
     * @param mediaType
     *      The media type this request accepts.
     *
     * @return
     *      This request.
     */
    HiveApiRequest accept(String mediaType);

    /**
     * Make the request to the Hive API using the method GET.
     *
     * @return
     *      The response to this request.
     *
     * @throws org.openhab.binding.hive.internal.client.exception.HiveClientRequestException
     *      If something goes wrong with making the request to the Hive API
     *      (before the Hive API has a chance to respond)
     *      e.g. The connection times out.
     */
    HiveApiResponse get() throws HiveClientRequestException;

    /**
     * Make the request to the Hive API using the method POST.
     *
     * @return
     *      The response to this request.
     *
     * @throws org.openhab.binding.hive.internal.client.exception.HiveClientRequestException
     *      If something goes wrong with making the request to the Hive API
     *      (before the Hive API has a chance to respond)
     *      e.g. The connection times out.
     */
    HiveApiResponse post(Object requestBody) throws HiveClientRequestException;

    /**
     * Make the request to the Hive API using the method PUT.
     *
     * @return
     *      The response to this request.
     *
     * @throws org.openhab.binding.hive.internal.client.exception.HiveClientRequestException
     *      If something goes wrong with making the request to the Hive API
     *      (before the Hive API has a chance to respond)
     *      e.g. The connection times out.
     */
    HiveApiResponse put(Object requestBody) throws HiveClientRequestException;

    /**
     * Make the request to the Hive API using the method DELETE.
     *
     * @return
     *      The response to this request.
     *
     * @throws org.openhab.binding.hive.internal.client.exception.HiveClientRequestException
     *      If something goes wrong with making the request to the Hive API
     *      (before the Hive API has a chance to respond)
     *      e.g. The connection times out.
     */
    HiveApiResponse delete() throws HiveClientRequestException;
}
