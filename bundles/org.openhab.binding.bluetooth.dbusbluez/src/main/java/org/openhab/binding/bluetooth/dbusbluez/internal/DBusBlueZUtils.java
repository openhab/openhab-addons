/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.dbusbluez.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 *
 * @author Benjamin Lafois
 *
 */
@NonNullByDefault
public class DBusBlueZUtils {

    private DBusBlueZUtils() {

    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns BluetoothAddress object from a DBus path
     * 
     * @param dbusPath
     * @return
     */
    public static @Nullable BluetoothAddress dbusPathToMac(String dbusPath) {
        Pattern p = Pattern.compile("/org/bluez/([^/]+)/dev_([^/]+).*");
        Matcher m = p.matcher(dbusPath);
        if (!m.matches()) {
            return null;
        } else {
            String s = m.group(2).replace("_", ":");
            return new BluetoothAddress(s);
        }
    }

}
