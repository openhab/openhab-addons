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
package org.openhab.binding.onkyo.internal;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;

/**
 * Helper to parse messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class OnkyoParserHelper {

    /**
     * Slices the string, removing empty values
     *
     * @param data comma separated string
     * @param startIndex initial index of the range to be copied
     * @param endIndex final index of the range to be copied (inclusive)
     * @return formatted StringType
     */
    public static StringType infoBuilder(String data, int startIndex, int endIndex) {
        String[] params = data.split(",");
        int toIndex = endIndex < params.length ? endIndex + 1 : params.length;
        if (params.length >= startIndex) {
            return new StringType(Stream.of(Arrays.copyOfRange(params, startIndex, toIndex))
                    .filter(p -> p.trim().length() > 0).map(p -> p.trim()).collect(Collectors.joining(", ", "", "")));
        }
        return StringType.EMPTY;
    }
}
