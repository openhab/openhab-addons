/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SunSynkPublicKey} is the internal class for the SunSynk public key
 * for a SunSynk Connect Account.
 * Login via Username and Password
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkPublicKey {
    private int code;
    private String msg = "";
    private boolean success;
    private String data = "";

    public String getPublicKey() {
        return this.data;
    }

    @Override
    public String toString() {
        return "code = " + this.code + " message = " + this.msg + " success = " + this.success;
    }
}
