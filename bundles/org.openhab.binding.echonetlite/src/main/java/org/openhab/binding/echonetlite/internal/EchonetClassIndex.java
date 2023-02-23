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
package org.openhab.binding.echonetlite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public enum EchonetClassIndex {
    INSTANCE;

    private static final EchonetClass[] INDEX = new EchonetClass[1 << 16];
    static {
        final EchonetClass[] values = EchonetClass.values();
        for (final EchonetClass value : values) {
            INDEX[codeToIndex(value.groupCode(), value.classCode())] = value;
        }
    }

    public static int codeToIndex(final int groupCode, final int classCode) {
        return ((0xFF & groupCode) << 8) + (0xFF & classCode);
    }

    public EchonetClass lookup(final int groupCode, final int classCode) {
        return INDEX[codeToIndex(groupCode, classCode)];
    }
}
