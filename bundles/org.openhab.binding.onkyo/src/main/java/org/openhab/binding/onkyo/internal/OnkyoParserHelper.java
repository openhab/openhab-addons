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
package org.openhab.binding.onkyo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Helper to parse messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class OnkyoParserHelper {

    public static State infoBuilder(String data, int from, int to) {
        StringBuilder builder = new StringBuilder();
        int end = to;
        String[] element = data.split(",");
        if (element.length < from) {
            return StringType.EMPTY;
        }
        if (element.length < end) {
            end = element.length;
        }
        boolean firstElement = true;
        for (int i = from; i < end; i++) {
            if (!element[i].isEmpty() && !firstElement) {
                builder.append(", ");
            }
            builder.append(element[i]);
            firstElement = false;
        }
        return new StringType(builder.toString());
    }
}
