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
package org.openhab.binding.echonetlite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public enum EchonetClass {
    AIRCON_HOMEAC(0x01, 0x30, (Epc[]) Epc.Device.values(), (Epc[]) Epc.AcGroup.values(), (Epc[]) Epc.HomeAc.values()),
    MANAGEMENT_CONTROLLER(0x05, 0xFF, new Epc[0], new Epc[0], new Epc[0]),
    NODE_PROFILE(0x0e, 0xf0, (Epc[]) Epc.Profile.values(), (Epc[]) Epc.ProfileGroup.values(),
            (Epc[]) Epc.NodeProfile.values());

    private final int groupCode;
    private final int classCode;
    private final Epc[] deviceProperties;
    private final Epc[] groupProperties;
    private final Epc[] classProperties;

    EchonetClass(final int groupCode, final int classCode, Epc[] deviceProperties, Epc[] groupProperties,
            Epc[] classProperties) {
        this.groupCode = groupCode;
        this.classCode = classCode;
        this.deviceProperties = deviceProperties;
        this.groupProperties = groupProperties;
        this.classProperties = classProperties;
    }

    public static EchonetClass resolve(final int groupCode, final int classCode) {
        final EchonetClass[] values = values();
        for (EchonetClass value : values) {
            if (value.groupCode == groupCode && value.classCode == classCode) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unable to find class: " + groupCode + "/" + classCode);
    }

    public int groupCode() {
        return groupCode;
    }

    public int classCode() {
        return classCode;
    }

    Epc[] deviceProperties() {
        return deviceProperties;
    }

    Epc[] groupProperties() {
        return groupProperties;
    }

    Epc[] classProperties() {
        return classProperties;
    }

    public String toString() {
        return name() + "{" + "groupCode=0x" + Integer.toHexString(groupCode) + ", classCode=0x"
                + Integer.toHexString(0xFF & classCode) + '}';
    }
}
