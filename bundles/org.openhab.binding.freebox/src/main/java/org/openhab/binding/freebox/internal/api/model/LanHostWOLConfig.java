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
package org.openhab.binding.freebox.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.api.RequestAnnotation;

/**
 * The {@link LanHostWOLConfig} is the Java class used to send a
 * WOL order to the given host
 *
 * @author Gaël L'hopital - Initial contribution
 */
@RequestAnnotation(responseClass = LanHostWOLResponse.class, relativeUrl = "lan/wol/pub/", retryAuth = true, method = "POST")
@NonNullByDefault
public class LanHostWOLConfig {
    protected final String mac;
    protected final String password;

    public LanHostWOLConfig(String mac, String password) {
        this.mac = mac;
        this.password = password;
    }

    public LanHostWOLConfig(String mac) {
        this(mac, "");
    }
}
