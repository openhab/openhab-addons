/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api;

/**
 * Represents version for the API of the evnotify online service.
 *
 * @author Michael Schmidt - Initial contribution
 */
public enum ApiVersion {

    V2,
    V3;

    public static ApiVersion getApiVersion(String searchVersion) {

        for (ApiVersion version : ApiVersion.values()) {
            if (version.name().equals(searchVersion)) {
                return version;
            }
        }

        throw new IllegalArgumentException(String.format("'%s' is not a valid version", searchVersion));
    }
}
