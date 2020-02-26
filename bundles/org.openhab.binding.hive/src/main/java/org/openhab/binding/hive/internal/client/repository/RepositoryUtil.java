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
package org.openhab.binding.hive.internal.client.repository;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.HiveApiResponse;
import org.openhab.binding.hive.internal.client.exception.HiveApiNotAuthorisedException;
import org.openhab.binding.hive.internal.client.exception.HiveApiUnknownException;
import org.openhab.binding.hive.internal.client.exception.HiveClientUnknownException;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class RepositoryUtil {
    public static void checkResponse(final String method, final HiveApiResponse response)
            throws HiveApiNotAuthorisedException, HiveApiUnknownException, HiveClientUnknownException {
        if (response.getStatusCode() == HiveApiConstants.STATUS_CODE_401_UNAUTHORIZED) {
            throw new HiveApiNotAuthorisedException();
        } else if (HiveApiConstants.isServerError(response.getStatusCode())) {
            throw new HiveApiUnknownException(MessageFormat.format(
                    "{0} failed. Something went wrong with the Hive API. Status code: {1} Content: {2}",
                    method,
                    response.getStatusCode(),
                    response.getRawContent()
            ));
        } else if (response.getStatusCode() != HiveApiConstants.STATUS_CODE_200_OK) {
            throw new HiveClientUnknownException(MessageFormat.format(
                    "{0} failed. Got an unexpected status code from the Hive API. Status code: {1} Content: {2}",
                    method,
                    response.getStatusCode(),
                    response.getRawContent()
            ));
        }
    }

    private RepositoryUtil() {
        throw new AssertionError();
    }
}
