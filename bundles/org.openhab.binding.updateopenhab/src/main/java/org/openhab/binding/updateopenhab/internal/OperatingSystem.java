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
package org.openhab.binding.updateopenhab.internal;

/**
 * The {@link OperatingSystem} determines what type of update to apply
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public enum OperatingSystem {
    UNKNOWN,
    UNIX,
    WINDOWS,
    SOLARIS,
    MAC;

    public static OperatingSystem getOperatingSystemVersion() {
        String os = System.getProperty("os.name");
        if (os == null) {
            return OperatingSystem.UNKNOWN;
        }
        os = os.toLowerCase();
        if (os.contains("win")) {
            return OperatingSystem.WINDOWS;
        }
        if (os.contains("mac")) {
            return OperatingSystem.MAC;
        }
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return OperatingSystem.UNIX;
        }
        if (os.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        }
        return OperatingSystem.UNKNOWN;
    }
}
