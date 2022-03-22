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
package org.openhab.binding.elroconnects.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ElroConnectsUtil} contains a few utility methods for the ELRO Connects binding.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public final class ElroConnectsUtil {

    public static int encode(int value) {
        return (((value ^ 0xFFFFFFFF) + 0x10000) ^ 0x123) ^ 0x1234;
    }

    public static String stringOrEmpty(@Nullable String data) {
        return (data == null ? "" : data);
    }

    public static int intOrZero(@Nullable Integer data) {
        return (data == null ? 0 : data);
    }
}
