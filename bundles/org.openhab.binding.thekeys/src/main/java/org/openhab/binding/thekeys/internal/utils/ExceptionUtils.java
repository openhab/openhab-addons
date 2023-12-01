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
package org.openhab.binding.thekeys.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utils for handling exception
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * Get the root cause of an exception
     */
    public static @Nullable Throwable getRootCause(@Nullable Throwable throwable) {
        if (throwable == null || throwable.getCause() == null || throwable.getCause().equals(throwable)) {
            return throwable;
        }
        return getRootCause(throwable.getCause());
    }
}
