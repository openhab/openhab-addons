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
package org.openhab.binding.lutron.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Supply some string utility methods formerly provided by org.apache.commons.lang.StringUtils.
 *
 * @author Bob Adair - Initial contribution
 *
 */
@NonNullByDefault
public class StringUtils {

    public static boolean equals(@Nullable String s1, @Nullable String s2) {
        return Objects.equals(s1, s2);
    }

    public static boolean isEmpty(@Nullable String s1) {
        return (s1 == null || s1.isEmpty());
    }
}
