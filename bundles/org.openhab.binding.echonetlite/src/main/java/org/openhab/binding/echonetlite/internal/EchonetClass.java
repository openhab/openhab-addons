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
package org.openhab.binding.echonetlite.internal;

/**
 * @author Michael Barker - Initial contribution
 */
public enum EchonetClass {
    AIRCON_HOMEAC(0x01, 0x30),
    AIRCON_COMMERCIAL(0x01, 0x52),
    MANAGEMENT_CONTROLLER(0x05, 0xFF),
    NODE_PROFILE(0x0e, 0xf0);

    private final int groupCode;
    private final int classCode;

    EchonetClass(final int groupCode, final int classCode) {
        this.groupCode = groupCode;
        this.classCode = classCode;
    }

    public static EchonetClass resolve(final int groupCode, final int classCode) {
        final EchonetClass[] values = values();
        for (EchonetClass value : values) {
            if (value.groupCode == groupCode && value.classCode == classCode) {
                return value;
            }
        }

        throw new RuntimeException("Unable to find class: " + groupCode + "/" + classCode);
    }

    public int groupCode() {
        return groupCode;
    }

    public int classCode() {
        return classCode;
    }

    public String toString() {
        return name() + "{" + "groupCode=0x" + Integer.toHexString(groupCode) + ", classCode=0x"
                + Integer.toHexString(0xFF & classCode) + '}';
    }
}
