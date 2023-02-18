/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
/*
 * Imported from https://github.com/google-gson/typeadapters/tree/master/jsr310/src
 * and repackaged to avoid the default package.
 */
package org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
    public String preProcess(@Nullable String in) {
        if (in.endsWith("+0000")) {
            return in.substring(0, in.length() - 5) + "Z";
        }
        return in;
    }
}
