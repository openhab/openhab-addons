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
package org.openhab.binding.freeboxos.internal.api.lan;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WakeOnLineData} is the Java class used to send a
 * WOL order to the given host
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class WakeOnLineData {
    protected final String mac;
    protected final String password;

    WakeOnLineData(String mac) {
        this(mac, "");
    }

    private WakeOnLineData(String mac, String password) {
        this.mac = mac;
        this.password = password;
    }
}
