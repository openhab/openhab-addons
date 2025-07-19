/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.util;

import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpFields;

/**
 * The {@link HttpUtil} implements utility methods for HTTP requests
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpUtil {

    private HttpUtil() {
        // prevent instantiation
    }

    public static String logToString(HttpFields httpFields) {
        return "[" + httpFields.stream().map(field -> {
            String headerName = field.getName();
            String value = field.getValue();
            return headerName + "=" + value;
        }).collect(Collectors.joining(",")) + "]";
    }
}
