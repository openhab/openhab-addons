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
package org.openhab.binding.sleepiq.internal.api.impl.typeadapters;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract type adapter for jsr310 date-time types.
 *
 * @author Christophe Bornet - Initial contribution
 */
@NonNullByDefault
abstract class DateTimeTypeAdapter<T> extends TemporalTypeAdapter<T> {

    DateTimeTypeAdapter(Function<String, T> parseFunction) {
        super(parseFunction);
    }

    @Override
    public String preProcess(String in) {
        if (in.endsWith("+0000")) {
            return in.substring(0, in.length() - 5) + "Z";
        }
        return in;
    }
}
