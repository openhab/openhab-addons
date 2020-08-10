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
package org.openhab.binding.deconz.internal;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Util} class defines common utility methods
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {
    public static String buildUrl(String host, int port, String... urlParts) {
        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(host).append(":").append(port);
        url.append("/api/");
        if (urlParts.length > 0) {
            url.append(Stream.of(urlParts).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("/")));
        }

        return url.toString();
    }

    public static int miredToKelvin(int miredValue) {
        return (int) (1000000.0 / miredValue);
    }

    public static int kelvinToMired(int kelvinValue) {
        return (int) (1000000.0 / kelvinValue);
    }

    public static int constrainToRange(int intValue, int min, int max) {
        return Math.max(min, Math.min(intValue, max));
    }
}
