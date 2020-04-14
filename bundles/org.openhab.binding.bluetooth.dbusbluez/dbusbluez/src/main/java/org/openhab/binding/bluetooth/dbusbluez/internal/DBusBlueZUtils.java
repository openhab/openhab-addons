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

import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 *
 * @author blafois
 *
 */
public class DBusBlueZUtils {

    private DBusBlueZUtils() {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static BluetoothAddress dbusPathToMac(String dbusPath) {

        if (dbusPath == null) {
            return null;
        }

        // /org/bluez/hci0/dev_00_CC_3F_B2_7E_60
        // /org/bluez/hci0/dev_A4_34_D9_ED_D3_74/service0026/char0027
        try {
            Pattern p = Pattern.compile("/org/bluez/([^/]+)/dev_([^/]+).*");
            Matcher m = p.matcher(dbusPath);
            if (!m.matches()) {
                return null;
            } else {
                String s = m.group(2).replace("_", ":");
                return new BluetoothAddress(s);
            }

        } catch (Exception e) {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
